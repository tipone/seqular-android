package net.seqular.network.api.requests.statuses;

import android.net.Uri;
import android.text.TextUtils;

import net.seqular.network.api.ContentUriRequestBody;
import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.api.ProgressListener;
import net.seqular.network.api.ResizedImageRequestBody;
import net.seqular.network.model.Attachment;
import net.seqular.network.ui.utils.UiUtils;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadAttachment extends MastodonAPIRequest<Attachment>{
	private Uri uri;
	private ProgressListener progressListener;
	private int maxImageSize;
	private String description;

	public UploadAttachment(Uri uri){
		super(HttpMethod.POST, "/media", Attachment.class);
		this.uri=uri;
	}

	public UploadAttachment(Uri uri, int maxImageSize, String description){
		this(uri);
		this.maxImageSize=maxImageSize;
		this.description=description;
	}

	public UploadAttachment setProgressListener(ProgressListener progressListener){
		this.progressListener=progressListener;
		return this;
	}

	@Override
	protected String getPathPrefix(){
		return "/api/v2";
	}

	@Override
	public void validateAndPostprocessResponse(Attachment respObj, Response httpResponse) throws IOException{
		if(respObj.url==null)
			respObj.url="";
		super.validateAndPostprocessResponse(respObj, httpResponse);
	}

	@Override
	public RequestBody getRequestBody() throws IOException{
		MultipartBody.Builder builder=new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", UiUtils.getFileName(uri), maxImageSize>0 ? new ResizedImageRequestBody(uri, maxImageSize, progressListener) : new ContentUriRequestBody(uri, progressListener));
		if(!TextUtils.isEmpty(description))
			builder.addFormDataPart("description", description);
		return builder.build();
	}
}
