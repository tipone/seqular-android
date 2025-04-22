package net.seqular.network.model.catalog;

import net.seqular.network.api.AllFieldsAreRequired;
import net.seqular.network.model.BaseModel;

@AllFieldsAreRequired
public class CatalogCategory extends BaseModel{
	public String category;
	public int serversCount;

	@Override
	public String toString(){
		return "CatalogCategory{"+
				"category='"+category+'\''+
				", serversCount="+serversCount+
				'}';
	}
}
