package org.joinmastodon.android.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.joinmastodon.android.api.RequiredField;
import org.parceler.Parcel;

@Parcel
public class CustomLocalTimeline extends BaseModel {
    @RequiredField
    public String domain;
    @RequiredField
    public String title;

    @NonNull
    @Override
    public String toString() {
        return "CustomLocalTimeline{" +
                "domain='" + domain + '\'' +
                ", title='" + title +
                '}';
    }

}
