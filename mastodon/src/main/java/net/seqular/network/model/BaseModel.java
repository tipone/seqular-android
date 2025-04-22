package net.seqular.network.model;

import net.seqular.network.api.AllFieldsAreRequired;
import net.seqular.network.api.ObjectValidationException;
import net.seqular.network.api.RequiredField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import net.seqular.network.api.MastodonAPIRequest;

public abstract class BaseModel implements Cloneable{

	/**
	 * indicates the profile has been fetched from a foreign instance.
	 *
	 * @see MastodonAPIRequest#execRemote
	 */
	public transient boolean isRemote;

	@CallSuper
	public void postprocess() throws ObjectValidationException{
		try{
			boolean allRequired=getClass().isAnnotationPresent(AllFieldsAreRequired.class);
			for(Field fld:getClass().getFields()){
				if(!fld.getType().isPrimitive() && !Modifier.isTransient(fld.getModifiers()) && (allRequired || fld.isAnnotationPresent(RequiredField.class))){
					if(fld.get(this)==null){
						throw new ObjectValidationException("Required field '"+fld.getName()+"' of type "+fld.getType().getSimpleName()+" was null in "+getClass().getSimpleName());
					}
				}
			}
		}catch(IllegalAccessException ignore){}
	}

	@NonNull
	@Override
	public Object clone(){
		try{
			return super.clone();
		}catch(CloneNotSupportedException x){
			throw new RuntimeException(x);
		}
	}
}
