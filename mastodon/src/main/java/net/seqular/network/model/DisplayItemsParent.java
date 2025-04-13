package net.seqular.network.model;

import net.seqular.network.ui.displayitems.StatusDisplayItem;

/**
 * A model object from which {@link StatusDisplayItem}s can be generated.
 */
public interface DisplayItemsParent{
	String getID();

	default String getAccountID(){
		return null;
	}
}
