package net.seqular.network.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;

public class HeaderPaginationList<T> extends ArrayList<T>{
	public Uri nextPageUri, prevPageUri;

	public HeaderPaginationList(int initialCapacity){
		super(initialCapacity);
	}

	public HeaderPaginationList(){
		super();
	}

	public HeaderPaginationList(@NonNull Collection<? extends T> c){
		super(c);
	}

	public String getNextPageMaxID(){
		if(nextPageUri==null)
			return null;
		return nextPageUri.getQueryParameter("max_id");
	}
}
