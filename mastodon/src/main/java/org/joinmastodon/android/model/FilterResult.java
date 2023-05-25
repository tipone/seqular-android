package org.joinmastodon.android.model;

import org.joinmastodon.android.api.ObjectValidationException;
import org.parceler.Parcel;

@Parcel
public class FilterResult extends BaseModel {
    public Filter filter;

    @Override
    public void postprocess() throws ObjectValidationException {
        super.postprocess();
        if (filter != null) filter.postprocess();
    }
}
