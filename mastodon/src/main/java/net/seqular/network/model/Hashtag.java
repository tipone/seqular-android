package net.seqular.network.model;

import net.seqular.network.api.RequiredField;
import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Hashtag extends BaseModel implements DisplayItemsParent{
	@RequiredField
	public String name;
	@RequiredField
	public String url;
	public boolean following;
	public List<History> history;
	public int statusesCount;

	@Override
	public String toString(){
		return "Hashtag{"+
				"name='"+name+'\''+
				", url='"+url+'\''+
				", following="+following+
				", history="+history+
				", statusesCount="+statusesCount+
				", following="+following+
				'}';
	}

	@Override
	public String getID(){
		return name;
	}

	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(o==null || getClass()!=o.getClass()) return false;

		Hashtag hashtag=(Hashtag) o;

		return name.equals(hashtag.name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	public int getWeekPosts(){
		return history.stream().mapToInt(h->h.uses).sum();
	}
}
