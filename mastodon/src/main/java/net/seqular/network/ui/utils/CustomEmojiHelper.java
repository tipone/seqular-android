package net.seqular.network.ui.utils;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.text.Spanned;

import net.seqular.network.ui.text.CustomEmojiSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;

public class CustomEmojiHelper{
	public List<List<CustomEmojiSpan>> spans=new ArrayList<>();
	public List<ImageLoaderRequest> requests=new ArrayList<>();

	public void setText(CharSequence text){
		spans.clear();
		requests.clear();
		if(!(text instanceof Spanned))
			return;
		CustomEmojiSpan[] spans=((Spanned) text).getSpans(0, text.length(), CustomEmojiSpan.class);
		for(List<CustomEmojiSpan> group:Arrays.stream(spans).collect(Collectors.groupingBy(s->s.emoji)).values()){
			this.spans.add(group);
			requests.add(group.get(0).createImageLoaderRequest());
		}
	}

	public void addText(CharSequence text) {
		if(!(text instanceof Spanned))
			return;
		CustomEmojiSpan[] spans=((Spanned) text).getSpans(0, text.length(), CustomEmojiSpan.class);
		for(List<CustomEmojiSpan> group:Arrays.stream(spans).collect(Collectors.groupingBy(s->s.emoji)).values()){
			this.spans.add(group);
			requests.add(group.get(0).createImageLoaderRequest());
		}
	}

	public int getImageCount(){
		return requests.size();
	}

	public ImageLoaderRequest getImageRequest(int image){
		return image<requests.size() ? requests.get(image) : null; // TODO fix this in the image loader
	}

	public void setImageDrawable(int image, Drawable drawable){
		if(spans.isEmpty())
			return;
		for(CustomEmojiSpan span:spans.get(image)){
			span.setDrawable(drawable);
		}
		if(drawable instanceof Animatable && !((Animatable) drawable).isRunning())
			((Animatable) drawable).start();
	}
}
