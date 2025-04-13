package net.seqular.network.api.requests.filters;

import net.seqular.network.model.FilterAction;
import net.seqular.network.model.FilterContext;

import java.util.EnumSet;
import java.util.List;

import androidx.annotation.Keep;

@Keep
class FilterRequest{
	public String title;
	public EnumSet<FilterContext> context;
	public FilterAction filterAction;
	public Integer expiresIn;
	public List<KeywordAttribute> keywordsAttributes;

	public FilterRequest(String title, EnumSet<FilterContext> context, FilterAction filterAction, Integer expiresIn, List<KeywordAttribute> keywordsAttributes){
		this.title=title;
		this.context=context;
		this.filterAction=filterAction;
		this.expiresIn=expiresIn;
		this.keywordsAttributes=keywordsAttributes;
	}
}
