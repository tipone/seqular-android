package org.joinmastodon.android.events;

import android.net.Uri;

import org.joinmastodon.android.model.Poll;

public class PictureTakenEvent {
    public Uri uri;

    public PictureTakenEvent(Uri uri){
        this.uri = uri;
    }
}
