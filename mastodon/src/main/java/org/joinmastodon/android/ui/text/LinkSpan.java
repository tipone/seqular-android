package org.joinmastodon.android.ui.text;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.view.View;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.model.Hashtag;
import org.joinmastodon.android.ui.utils.UiUtils;

public class LinkSpan extends CharacterStyle {

	private int color=0xFF00FF00;
	private OnLinkClickListener listener;
	private String link;
	private Type type;
	private String accountID;
	private Object linkObject;
	private Object parentObject;

	public LinkSpan(String link, OnLinkClickListener listener, Type type, String accountID, Object linkObject, Object parentObject){
		this.listener=listener;
		this.link=link;
		this.type=type;
		this.accountID=accountID;
		this.linkObject=linkObject;
		this.parentObject=parentObject;
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
			case URL -> UiUtils.openURL(context, accountID, link, parentObject);
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
		return parentObject.toString();
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
