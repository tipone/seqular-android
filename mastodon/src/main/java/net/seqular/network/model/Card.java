package net.seqular.network.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import net.seqular.network.api.ObjectValidationException;
import net.seqular.network.api.RequiredField;
import net.seqular.network.ui.utils.BlurHashDecoder;
import net.seqular.network.ui.utils.BlurHashDrawable;
import org.parceler.Parcel;

import java.time.Instant;
import java.util.List;

@Parcel
public class Card extends BaseModel{
	@RequiredField
	public String url;
	@RequiredField
	public String title;
	@RequiredField
	public String description;
	@RequiredField
	public Type type;
	public String authorName;
	public String authorUrl;
	public String providerName;
	public String providerUrl;
//	public String html;
	public int width;
	public int height;
	public String image;
	public String embedUrl;
	public String blurhash;
	public List<History> history;
	public Instant publishedAt;

	public transient Drawable blurhashPlaceholder;

	@Override
	public void postprocess() throws ObjectValidationException{
		if(type==null)
			type=Type.LINK;
		super.postprocess();
		if(blurhash!=null){
			Bitmap placeholder=BlurHashDecoder.decode(blurhash, 16, 16);
			if(placeholder!=null)
				blurhashPlaceholder=new BlurHashDrawable(placeholder, width, height);
		}
	}

	public boolean isHashtagUrl(String statusUrl){
		Uri parsedUrl=Uri.parse(url);
		Uri parsedStatusUrl=Uri.parse(statusUrl);
		if(parsedUrl.getHost()==null || parsedUrl.getPath()==null || parsedStatusUrl.getHost()==null) return false;
		return title.equals("Akkoma") && parsedUrl.getHost().equals(parsedStatusUrl.getHost()) && parsedUrl.getPath().startsWith("/tag/");
	}

	@Override
	public String toString(){
		return "Card{"+
				"url='"+url+'\''+
				", title='"+title+'\''+
				", description='"+description+'\''+
				", type="+type+
				", authorName='"+authorName+'\''+
				", authorUrl='"+authorUrl+'\''+
				", providerName='"+providerName+'\''+
				", providerUrl='"+providerUrl+'\''+
				", width="+width+
				", height="+height+
				", image='"+image+'\''+
				", embedUrl='"+embedUrl+'\''+
				", blurhash='"+blurhash+'\''+
				", history="+history+
				", publishedAt="+publishedAt+
				'}';
	}

	public enum Type{
		@SerializedName("link")
		LINK,
		@SerializedName("photo")
		PHOTO,
		@SerializedName("video")
		VIDEO,
		@SerializedName("rich")
		RICH
	}
}
