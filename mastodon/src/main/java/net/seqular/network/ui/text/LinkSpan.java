package net.seqular.network.ui.text;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.view.View;

import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.model.Hashtag;
import net.seqular.network.ui.utils.UiUtils;

public class LinkSpan extends CharacterStyle {

	private int color=0xFF00FF00;
	private OnLinkClickListener listener;
	private String link;
	private Type type;
	private String accountID;
	private Object linkObject;
	private Object parentObject;
	private String text;

	public LinkSpan(String link, OnLinkClickListener listener, Type type, String accountID, Object linkObject, Object parentObject, String text){
		this.listener=listener;
		this.link=link;
		this.type=type;
		this.accountID=accountID;
		this.linkObject=linkObject;
		this.parentObject=parentObject;
		this.text=text;
	}

	public int getColor(){
		return color;
	}

	@Override
	public void updateDrawState(TextPaint tp) {
		tp.setColor(color=tp.linkColor);
		tp.setUnderlineText(GlobalUserPreferences.underlinedLinks);
	}
	
	public void onClick(Context context){
		switch(getType()){
			case URL -> UiUtils.openURL(context, accountID, link);
			case MENTION -> UiUtils.openProfileByID(context, accountID, link);
			case HASHTAG -> {
				if(linkObject instanceof Hashtag ht)
					UiUtils.openHashtagTimeline(context, accountID, ht);
				else
					UiUtils.openHashtagTimeline(context, accountID, link);
			}
			case CUSTOM -> listener.onLinkClick(this);
		}
	}

	public void onLongClick(View view) {
		if(linkObject instanceof Hashtag ht)
			UiUtils.copyText(view, ht.name);
		else
			UiUtils.copyText(view, link);
	}

	public String getLink(){
		return link;
	}

	public String getText() {
		return text;
	}

	public Type getType(){
		return type;
	}

	public void setListener(OnLinkClickListener listener){
		this.listener=listener;
	}

	public interface OnLinkClickListener{
		void onLinkClick(LinkSpan span);
	}

	public enum Type{
		URL,
		MENTION,
		HASHTAG,
		CUSTOM
	}
}
