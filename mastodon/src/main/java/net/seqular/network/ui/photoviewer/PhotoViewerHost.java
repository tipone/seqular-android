package net.seqular.network.ui.photoviewer;

import net.seqular.network.model.Status;
import net.seqular.network.ui.displayitems.MediaGridStatusDisplayItem;

public interface PhotoViewerHost{
	void openPhotoViewer(String parentID, Status status, int attachmentIndex, MediaGridStatusDisplayItem.Holder gridHolder);
}
