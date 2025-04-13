package net.seqular.network.events;

import androidx.recyclerview.widget.RecyclerView;
import net.seqular.network.model.EmojiReaction;
import java.util.List;

public class EmojiReactionsUpdatedEvent{
	public final String id;
	public final List<EmojiReaction> reactions;
	public final boolean updateTextPadding;
	public RecyclerView.ViewHolder viewHolder;

	public EmojiReactionsUpdatedEvent(String id, List<EmojiReaction> reactions, boolean updateTextPadding, RecyclerView.ViewHolder viewHolder){
		this.id=id;
		this.reactions=reactions;
		this.updateTextPadding=updateTextPadding;
		this.viewHolder=viewHolder;
	}
}
