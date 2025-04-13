package net.seqular.network.api;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.BuildConfig;
import net.seqular.network.MastodonApp;
import net.seqular.network.api.requests.lists.GetLists;
import net.seqular.network.api.requests.notifications.GetNotifications;
import net.seqular.network.api.requests.timelines.GetHomeTimeline;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.model.CacheablePaginatedResponse;
import net.seqular.network.model.FilterContext;
import net.seqular.network.model.FollowList;
import net.seqular.network.model.Instance;
import net.seqular.network.model.Notification;
import net.seqular.network.model.PaginatedResponse;
import net.seqular.network.model.SearchResult;
import net.seqular.network.model.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.utils.WorkerThread;

public class CacheController{
	private static final String TAG="CacheController";
	private static final int DB_VERSION=4;
	private static final WorkerThread databaseThread=new WorkerThread("databaseThread");
	private static final Handler uiHandler=new Handler(Looper.getMainLooper());

	private final String accountID;
	private DatabaseHelper db;
	private final Runnable databaseCloseRunnable=this::closeDatabase;
	private boolean loadingNotifications;
	private final ArrayList<Callback<PaginatedResponse<List<Notification>>>> pendingNotificationsCallbacks=new ArrayList<>();
	private List<FollowList> lists;

	private static final int POST_FLAG_GAP_AFTER=1;

	static{
		databaseThread.start();
	}

	public CacheController(String accountID){
		this.accountID=accountID;
	}

	public void getHomeTimeline(String maxID, int count, boolean forceReload, Callback<CacheablePaginatedResponse<List<Status>>> callback){
		cancelDelayedClose();
		databaseThread.postRunnable(()->{
			try{
				if(!forceReload){
					SQLiteDatabase db=getOrOpenDatabase();
					try(Cursor cursor=db.query("home_timeline", new String[]{"json", "flags"}, maxID==null ? null : "`id`<?", maxID==null ? null : new String[]{maxID}, null, null, "`time` DESC", count+"")){
						if(cursor.getCount()==count){
							ArrayList<Status> result=new ArrayList<>();
							cursor.moveToFirst();
							String newMaxID;
							do{
								Status status=MastodonAPIController.gson.fromJson(cursor.getString(0), Status.class);
								status.postprocess();
								int flags=cursor.getInt(1);
								status.hasGapAfter=((flags & POST_FLAG_GAP_AFTER)!=0) ? status.id : null;
								newMaxID=status.id;
								result.add(status);
							}while(cursor.moveToNext());
							String _newMaxID=newMaxID;
							uiHandler.post(()->callback.onSuccess(new CacheablePaginatedResponse<>(result, _newMaxID, true)));
							return;
						}
					}catch(IOException x){
						Log.w(TAG, "getHomeTimeline: corrupted status object in database", x);
					}
				}
				new GetHomeTimeline(maxID, null, count, null, AccountSessionManager.get(accountID).getLocalPreferences().timelineReplyVisibility)
						.setCallback(new Callback<>(){
							@Override
							public void onSuccess(List<Status> result){
								callback.onSuccess(new CacheablePaginatedResponse<>(result, result.isEmpty() ? null : result.get(result.size()-1).id, false));
								putHomeTimeline(result, maxID==null);
							}

							@Override
							public void onError(ErrorResponse error){
								callback.onError(error);
							}
						})
						.exec(accountID);
			}catch(SQLiteException x){
				Log.w(TAG, x);
				uiHandler.post(()->callback.onError(new MastodonErrorResponse(x.getLocalizedMessage(), 500, x)));
			}finally{
				closeDelayed();
			}
		}, 0);
	}

	public void putHomeTimeline(List<Status> posts, boolean clear){
		runOnDbThread((db)->{
			if(clear)
				db.delete("home_timeline", null, null);
			ContentValues values=new ContentValues(4);
			for(Status s:posts){
				values.put("id", s.id);
				values.put("json", MastodonAPIController.gson.toJson(s));
				int flags=0;
				if(Objects.equals(s.hasGapAfter, s.id))
					flags|=POST_FLAG_GAP_AFTER;
				values.put("flags", flags);
				values.put("time", s.createdAt.getEpochSecond());
				db.insertWithOnConflict("home_timeline", null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}
			if(!clear)
				db.delete("home_timeline", "`id` NOT IN (SELECT `id` FROM `home_timeline` ORDER BY `time` DESC LIMIT ?)", new String[]{"1000"});
		});
	}

	public void updateStatus(Status status) {
		runOnDbThread((db)->{
			ContentValues statusUpdate=new ContentValues(1);
			statusUpdate.put("json", MastodonAPIController.gson.toJson(status));
			db.update("home_timeline", statusUpdate, "id = ?", new String[] { status.id });
		});
	}

	public void updateNotification(Notification notification) {
		runOnDbThread((db)->{
			ContentValues notificationUpdate=new ContentValues(1);
			notificationUpdate.put("json", MastodonAPIController.gson.toJson(notification));
			String[] notificationArgs = new String[] { notification.id };
			db.update("notifications_all", notificationUpdate, "id = ?", notificationArgs);
			db.update("notifications_mentions", notificationUpdate, "id = ?", notificationArgs);
			db.update("notifications_posts", notificationUpdate, "id = ?", notificationArgs);

			ContentValues statusUpdate=new ContentValues(1);
			statusUpdate.put("json", MastodonAPIController.gson.toJson(notification.status));
			db.update("home_timeline", statusUpdate, "id = ?", new String[] { notification.status.id });
		});
	}

	public void getNotifications(String maxID, int count, boolean onlyMentions, boolean onlyPosts, boolean forceReload, Callback<PaginatedResponse<List<Notification>>> callback){
		cancelDelayedClose();
		databaseThread.postRunnable(()->{
			try{
				if(!onlyMentions && !onlyPosts && loadingNotifications){
					synchronized(pendingNotificationsCallbacks){
						pendingNotificationsCallbacks.add(callback);
					}
					return;
				}
				if(!forceReload){
					SQLiteDatabase db=getOrOpenDatabase();
					String table=onlyPosts ? "notifications_posts" : onlyMentions ? "notifications_mentions" : "notifications_all";
					try(Cursor cursor=db.query(table, new String[]{"json"}, maxID==null ? null : "`id`<?", maxID==null ? null : new String[]{maxID}, null, null, "`time` DESC", count+"")){
						if(cursor.getCount()==count){
							ArrayList<Notification> result=new ArrayList<>();
							cursor.moveToFirst();
							String newMaxID;
							do{
								Notification ntf=MastodonAPIController.gson.fromJson(cursor.getString(0), Notification.class);
								ntf.postprocess();
								newMaxID=ntf.id;
								result.add(ntf);
							}while(cursor.moveToNext());
							String _newMaxID=newMaxID;
							AccountSessionManager.get(accountID).filterStatusContainingObjects(result, n->n.status, FilterContext.NOTIFICATIONS);
							uiHandler.post(()->callback.onSuccess(new PaginatedResponse<>(result, _newMaxID)));
							return;
						}
					}catch(IOException x){
						Log.w(TAG, "getNotifications: corrupted notification object in database", x);
					}
				}
				if(!onlyMentions && !onlyPosts)
					loadingNotifications=true;
				boolean isAkkoma = AccountSessionManager.get(accountID).getInstance().map(Instance::isAkkoma).orElse(false);
				new GetNotifications(maxID, count, onlyPosts ? EnumSet.of(Notification.Type.STATUS) : onlyMentions ? EnumSet.of(Notification.Type.MENTION): EnumSet.allOf(Notification.Type.class), isAkkoma)
						.setCallback(new Callback<>(){
							@Override
							public void onSuccess(List<Notification> result){
								ArrayList<Notification> filtered=new ArrayList<>(result);
								AccountSessionManager.get(accountID).filterStatusContainingObjects(filtered, n->n.status, FilterContext.NOTIFICATIONS);
								PaginatedResponse<List<Notification>> res=new PaginatedResponse<>(filtered, result.isEmpty() ? null : result.get(result.size()-1).id);
								callback.onSuccess(res);
								putNotifications(result, onlyMentions, onlyPosts, maxID==null);
								if(!onlyMentions){
									loadingNotifications=false;
									synchronized(pendingNotificationsCallbacks){
										for(Callback<PaginatedResponse<List<Notification>>> cb:pendingNotificationsCallbacks){
											cb.onSuccess(res);
										}
										pendingNotificationsCallbacks.clear();
									}
								}
							}

							@Override
							public void onError(ErrorResponse error){
								callback.onError(error);
								if(!onlyMentions){
									loadingNotifications=false;
									synchronized(pendingNotificationsCallbacks){
										for(Callback<PaginatedResponse<List<Notification>>> cb:pendingNotificationsCallbacks){
											cb.onError(error);
										}
										pendingNotificationsCallbacks.clear();
									}
								}
							}
						})
						.exec(accountID);
			}catch(SQLiteException x){
				Log.w(TAG, x);
				uiHandler.post(()->callback.onError(new MastodonErrorResponse(x.getLocalizedMessage(), 500, x)));
			}finally{
				closeDelayed();
			}
		}, 0);
	}

	private void putNotifications(List<Notification> notifications, boolean onlyMentions, boolean onlyPosts, boolean clear){
		runOnDbThread((db)->{
			String table=onlyPosts ? "notifications_posts" : onlyMentions ? "notifications_mentions" : "notifications_all";
			if(clear)
				db.delete(table, null, null);
			ContentValues values=new ContentValues(4);
			for(Notification n:notifications){
				if(n.type==null){
					continue;
				}
				values.put("id", n.id);
				values.put("json", MastodonAPIController.gson.toJson(n));
				values.put("type", n.type.ordinal());
				values.put("time", n.createdAt.getEpochSecond());
				db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}
		});
	}

	public void getRecentSearches(Consumer<List<SearchResult>> callback){
		runOnDbThread((db)->{
			try(Cursor cursor=db.query("recent_searches", new String[]{"json"}, null, null, null, null, "time DESC")){
				List<SearchResult> results=new ArrayList<>();
				while(cursor.moveToNext()){
					SearchResult result=MastodonAPIController.gson.fromJson(cursor.getString(0), SearchResult.class);
					result.postprocess();
					results.add(result);
				}
				uiHandler.post(()->callback.accept(results));
			}
		});
	}

	public void putRecentSearch(SearchResult result){
		runOnDbThread((db)->{
			ContentValues values=new ContentValues(4);
			values.put("id", result.getID());
			values.put("json", MastodonAPIController.gson.toJson(result));
			values.put("time", (int)(System.currentTimeMillis()/1000));
			db.insertWithOnConflict("recent_searches", null, values, SQLiteDatabase.CONFLICT_REPLACE);
		});
	}

	public void deleteStatus(String id){
		runOnDbThread((db)->{
			String gapId=null;
			int gapFlags=0;
			// select to-be-removed and newer row
			try(Cursor cursor=db.query("home_timeline", new String[]{"id", "flags"}, "`time`>=(SELECT `time` FROM `home_timeline` WHERE `id`=?)", new String[]{id}, null, null, "`time` ASC", "2")){
				boolean hadGapAfter=false;
				// always either one or two iterations (only one if there's no newer post)
				while(cursor.moveToNext()){
					String currentId=cursor.getString(0);
					int currentFlags=cursor.getInt(1);
					if(currentId.equals(id)){
						hadGapAfter=((currentFlags & POST_FLAG_GAP_AFTER)!=0);
					}else if(hadGapAfter){
						gapFlags=currentFlags|POST_FLAG_GAP_AFTER;
						gapId=currentId;
					}
				}
			}
			if(gapId!=null){
				ContentValues values=new ContentValues();
				values.put("flags", gapFlags);
				db.update("home_timeline", values, "`id`=?", new String[]{gapId});
			}
			db.delete("home_timeline", "`id`=?", new String[]{id});
		});
	}

	public void clearRecentSearches(){
		runOnDbThread((db)->db.delete("recent_searches", null, null));
	}

	private void closeDelayed(){
		databaseThread.postRunnable(databaseCloseRunnable, 10_000);
	}

	public void closeDatabase(){
		if(db!=null){
			if(BuildConfig.DEBUG)
				Log.d(TAG, "closeDatabase");
			db.close();
			db=null;
		}
	}

	private void cancelDelayedClose(){
		if(db!=null){
			databaseThread.handler.removeCallbacks(databaseCloseRunnable);
		}
	}

	private SQLiteDatabase getOrOpenDatabase(){
		if(db==null)
			db=new DatabaseHelper();
		return db.getWritableDatabase();
	}

	private void runOnDbThread(DatabaseRunnable r){
		runOnDbThread(r, null);
	}

	private void runOnDbThread(DatabaseRunnable r, Consumer<Exception> onError){
		cancelDelayedClose();
		databaseThread.postRunnable(()->{
			try{
				SQLiteDatabase db=getOrOpenDatabase();
				r.run(db);
			}catch(SQLiteException|IOException x){
				Log.w(TAG, x);
				if(onError!=null)
					onError.accept(x);
			}finally{
				closeDelayed();
			}
		}, 0);
	}

	public void reloadLists(Callback<List<FollowList>> callback){
		new GetLists()
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(List<FollowList> result){
						result.sort(Comparator.comparing(l->l.title));
						lists=result;
						if(callback!=null)
							callback.onSuccess(result);
						writeListsToFile();
					}

					@Override
					public void onError(ErrorResponse error){
						if(callback!=null)
							callback.onError(error);
					}
				})
				.exec(accountID);
	}

	private List<FollowList> loadListsFromFile(){
		File file=getListsFile();
		if(!file.exists())
			return null;
		try(InputStreamReader in=new InputStreamReader(new FileInputStream(file))){
			return MastodonAPIController.gson.fromJson(in, new TypeToken<List<FollowList>>(){}.getType());
		}catch(Exception x){
			Log.w(TAG, "failed to read lists from cache file", x);
			return null;
		}
	}

	private void writeListsToFile(){
		databaseThread.postRunnable(()->{
			try(OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(getListsFile()))){
				MastodonAPIController.gson.toJson(lists, out);
			}catch(IOException x){
				Log.w(TAG, "failed to write lists to cache file", x);
			}
		}, 0);
	}

	public void getLists(Callback<List<FollowList>> callback){
		if(lists!=null){
			if(callback!=null)
				callback.onSuccess(lists);
			return;
		}
		databaseThread.postRunnable(()->{
			List<FollowList> lists=loadListsFromFile();
			if(lists!=null){
				this.lists=lists;
				if(callback!=null)
					uiHandler.post(()->callback.onSuccess(lists));
				return;
			}
			reloadLists(callback);
		}, 0);
	}

	public File getListsFile(){
		return new File(MastodonApp.context.getFilesDir(), "lists_"+accountID+".json");
	}

	public void addList(FollowList list){
		if(lists==null)
			return;
		lists.add(list);
		lists.sort(Comparator.comparing(l->l.title));
		writeListsToFile();
	}

	public void deleteList(String id){
		if(lists==null)
			return;
		lists.removeIf(l->l.id.equals(id));
		writeListsToFile();
	}

	public void updateList(FollowList list){
		if(lists==null)
			return;
		for(int i=0;i<lists.size();i++){
			if(lists.get(i).id.equals(list.id)){
				lists.set(i, list);
				lists.sort(Comparator.comparing(l->l.title));
				writeListsToFile();
				break;
			}
		}
	}

	private class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(){
			super(MastodonApp.context, accountID+".db", null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL("""
						CREATE TABLE `home_timeline` (
							`id` VARCHAR(25) NOT NULL PRIMARY KEY,
							`json` TEXT NOT NULL,
							`flags` INTEGER NOT NULL DEFAULT 0,
							`time` INTEGER NOT NULL
						)""");
			db.execSQL("""
						CREATE TABLE `notifications_all` (
							`id` VARCHAR(25) NOT NULL PRIMARY KEY,
							`json` TEXT NOT NULL,
							`flags` INTEGER NOT NULL DEFAULT 0,
							`type` INTEGER NOT NULL,
							`time` INTEGER NOT NULL
						)""");
			db.execSQL("""
						CREATE TABLE `notifications_mentions` (
							`id` VARCHAR(25) NOT NULL PRIMARY KEY,
							`json` TEXT NOT NULL,
							`flags` INTEGER NOT NULL DEFAULT 0,
							`type` INTEGER NOT NULL,
							`time` INTEGER NOT NULL
						)""");
			createRecentSearchesTable(db);
			createPostsNotificationsTable(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			if(oldVersion<2){
				createRecentSearchesTable(db);
			}
			if(oldVersion<3){
				// MEGALODON
				createPostsNotificationsTable(db);
			}
			if(oldVersion<4){
				addTimeColumns(db);
			}
		}

		private void createRecentSearchesTable(SQLiteDatabase db){
			db.execSQL("""
						CREATE TABLE `recent_searches` (
							`id` VARCHAR(50) NOT NULL PRIMARY KEY,
							`json` TEXT NOT NULL,
							`time` INTEGER NOT NULL
						)""");
		}

		private void createPostsNotificationsTable(SQLiteDatabase db){
			db.execSQL("""
						CREATE TABLE `notifications_posts` (
							`id` VARCHAR(25) NOT NULL PRIMARY KEY,
							`json` TEXT NOT NULL,
							`flags` INTEGER NOT NULL DEFAULT 0,
							`type` INTEGER NOT NULL,
							`time` INTEGER NOT NULL
						)""");
		}

		private void addTimeColumns(SQLiteDatabase db){
			db.execSQL("DELETE FROM `home_timeline`");
			db.execSQL("DELETE FROM `notifications_all`");
			db.execSQL("DELETE FROM `notifications_mentions`");
			db.execSQL("DELETE FROM `notifications_posts`");
			db.execSQL("ALTER TABLE `home_timeline` ADD `time` INTEGER NOT NULL DEFAULT 0");
			db.execSQL("ALTER TABLE `notifications_all` ADD `time` INTEGER NOT NULL DEFAULT 0");
			db.execSQL("ALTER TABLE `notifications_mentions` ADD `time` INTEGER NOT NULL DEFAULT 0");
			db.execSQL("ALTER TABLE `notifications_posts` ADD `time` INTEGER NOT NULL DEFAULT 0");
		}
	}

	@FunctionalInterface
	private interface DatabaseRunnable{
		void run(SQLiteDatabase db) throws IOException;
	}
}
