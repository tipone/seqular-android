package net.seqular.network.ui.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import net.seqular.network.model.Account;
import net.seqular.network.model.Emoji;

import androidx.annotation.NonNull;

public class AvatarSpan extends CustomEmojiSpan{

	public AvatarSpan(Account account){
		//this is a hacky solution to allow loading of avatars in the middle of strings,
		//using already existing code for loading emojis
		super(new Emoji(account.avatarStatic, account.avatar, account.avatarStatic));
	}

	@Override
	public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint){
		//modified draw of a CustomEmojiSpan, drawing a circular image instead.
		if(drawable==null)
			return;
		top += 4;
		int size=Math.round(paint.descent()-paint.ascent());
		Rect bounds=drawable.getBounds();
		int dw=drawable.getIntrinsicWidth();
		int dh=drawable.getIntrinsicHeight();
		if(bounds.left!=0 || bounds.top!=0 || bounds.right!=dw || bounds.left!=dh){
			drawable.setBounds(0, 0, dw, dh);
		}
		canvas.save();
		float radius = size / 2f;
		Path clipPath = new Path();
		clipPath.addCircle(x + radius, top + radius, radius, Path.Direction.CW);
		canvas.clipPath(clipPath);
		canvas.translate(x, top);
		canvas.scale(size/(float)dw, size/(float)dh, 0f, 0f);
		drawable.draw(canvas);
		canvas.restore();
	}

}
