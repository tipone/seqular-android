package net.seqular.network.model.catalog;

import net.seqular.network.api.AllFieldsAreRequired;
import net.seqular.network.model.BaseModel;

@AllFieldsAreRequired
public class CatalogDefaultInstance extends BaseModel{
	public String domain;
	public float weight;
}
