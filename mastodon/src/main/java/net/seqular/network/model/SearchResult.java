package net.seqular.network.model;

import net.seqular.network.api.ObjectValidationException;
import net.seqular.network.api.RequiredField;

public class SearchResult extends BaseModel implements DisplayItemsParent{
	public Account account;
	public Hashtag hashtag;
	public Status status;
	@RequiredField
	public Type type;

	public transient String id;
	public transient boolean firstInSection;

	public SearchResult(){}

	public SearchResult(Account acc){
		account=acc;
		type=Type.ACCOUNT;
		generateID();
	}

	public SearchResult(Hashtag tag){
		hashtag=tag;
		type=Type.HASHTAG;
		generateID();
	}

	public SearchResult(Status status){
		this.status=status;
		type=Type.STATUS;
		generateID();
	}

	@Override
	public String getID(){
		return id;
	}

	@Override
	public String getAccountID(){
		if(type==Type.STATUS)
			return status.getAccountID();
		return null;
	}

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		if(account!=null)
			account.postprocess();
		if(hashtag!=null)
			hashtag.postprocess();
		if(status!=null)
			status.postprocess();
		generateID();
	}

	private void generateID(){
		id=switch(type){
			case ACCOUNT -> "acc_"+account.id;
			case HASHTAG -> "tag_"+hashtag.name.hashCode();
			case STATUS -> "post_"+status.id;
		};
	}

	public enum Type{
		ACCOUNT,
		HASHTAG,
		STATUS
	}
}
