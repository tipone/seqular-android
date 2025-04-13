package net.seqular.network.ui.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.model.Emoji;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.V;

public class CustomEmojiSpan extends ReplacementSpan{
	public final Emoji emoji;
	protected Drawable drawable;

	public CustomEmojiSpan(Emoji emoji){
		this.emoji=emoji;
	}

	@Override
	public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm){
		int size = Math.round(paint.descent()-paint.ascent());
		return drawable!=null ? (int) (drawable.getIntrinsicWidth()*(size/(float) drawable.getIntrinsicHeight())) : size;
	}

	@Override
	public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint){
		int size=Math.round(paint.descent()-paint.ascent());
		if(drawable==null){
			int alpha=paint.getAlpha();
			paint.setAlpha(alpha >> 1);
			canvas.drawRoundRect(x, top, x+size, top+size, V.dp(2), V.dp(2), paint);
			paint.setAlpha(alpha);
		}else{
			// AnimatedImageDrawable doesn't like when its bounds don't start at (0, 0)
			Rect bounds=drawable.getBounds();
			int dw=drawable.getIntrinsicWidth();
			int dh=drawable.getIntrinsicHeight();
			if(bounds.left!=0 || bounds.top!=0 || bounds.right!=dw || bounds.left!=dh){
				drawable.setBounds(0, 0, dw, dh);
			}
			canvas.save();
			canvas.translate(x, top);
			float scale = size/(float)dh;
			canvas.scale(scale, scale, 0f, 0f);
			drawable.draw(canvas);
			canvas.restore();
		}
	}

	public void setDrawable(Drawable drawable){
		this.drawable=drawable;
	}

	public UrlImageLoaderRequest createImageLoaderRequest(){
		return new UrlImageLoaderRequest(GlobalUserPreferences.playGifs ? emoji.url : emoji.staticUrl, 0, V.dp(20));
	}
}
