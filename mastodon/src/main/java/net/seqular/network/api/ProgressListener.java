package net.seqular.network.api;

public interface ProgressListener{
	void onProgress(long transferred, long total);
}
