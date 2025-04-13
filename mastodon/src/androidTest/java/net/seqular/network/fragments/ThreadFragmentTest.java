package net.seqular.network.fragments;

import static org.junit.Assert.*;

import net.seqular.network.events.StatusCountersUpdatedEvent;
import net.seqular.network.events.StatusUpdatedEvent;
import net.seqular.network.model.Status;
import net.seqular.network.model.StatusContext;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

public class ThreadFragmentTest {

	private Status fakeStatus(String id, String inReplyTo) {
		Status status = Status.ofFake(id, null, null);
		status.inReplyToId = inReplyTo;
		return status;
	}

	private ThreadFragment.NeighborAncestryInfo fakeInfo(Status s, Status d, Status a) {
		return new ThreadFragment.NeighborAncestryInfo(s, d, a);
	}

	@Test
	public void mapNeighborhoodAncestry() {
		StatusContext context = new StatusContext();
		context.ancestors = List.of(
				fakeStatus("oldest ancestor", null),
				fakeStatus("younger ancestor", "oldest ancestor")
		);
		Status mainStatus = fakeStatus("main status", "younger ancestor");
		context.descendants = List.of(
				fakeStatus("first reply", "main status"),
				fakeStatus("reply to first reply", "first reply"),
				fakeStatus("third level reply", "reply to first reply"),
				fakeStatus("another reply", "main status")
		);

		List<ThreadFragment.NeighborAncestryInfo> neighbors =
				ThreadFragment.mapNeighborhoodAncestry(mainStatus, context);

		assertEquals(List.of(
				fakeInfo(context.ancestors.get(0), context.ancestors.get(1), null),
				fakeInfo(context.ancestors.get(1), mainStatus, context.ancestors.get(0)),
				fakeInfo(mainStatus, context.descendants.get(0), context.ancestors.get(1)),
				fakeInfo(context.descendants.get(0), context.descendants.get(1), mainStatus),
				fakeInfo(context.descendants.get(1), context.descendants.get(2), context.descendants.get(0)),
				fakeInfo(context.descendants.get(2), null, context.descendants.get(1)),
				fakeInfo(context.descendants.get(3), null, null)
		), neighbors);
	}

	@Test
	public void maybeApplyMainStatus() {
		ThreadFragment fragment = new ThreadFragment();
		fragment.contextInitiallyRendered = true;
		fragment.mainStatus = Status.ofFake("123456", "original text", Instant.EPOCH);

		Status update1 = Status.ofFake("123456", "updated text", Instant.EPOCH);
		update1.editedAt = Instant.ofEpochSecond(1);
		fragment.updatedStatus = update1;
		StatusUpdatedEvent event1 = (StatusUpdatedEvent) fragment.maybeApplyMainStatus();
		assertEquals("fired update event", update1, event1.status);
		assertEquals("updated main status", update1, fragment.mainStatus);

		Status update2 = Status.ofFake("123456", "updated text", Instant.EPOCH);
		update2.favouritesCount = 123;
		fragment.updatedStatus = update2;
		StatusCountersUpdatedEvent event2 = (StatusCountersUpdatedEvent) fragment.maybeApplyMainStatus();
		assertEquals("only fired counter update event", update2.id, event2.id);
		assertEquals("updated counter is correct", 123, event2.favorites);
		assertEquals("updated main status", update2, fragment.mainStatus);

		Status update3 = Status.ofFake("123456", "whatever", Instant.EPOCH);
		fragment.contextInitiallyRendered = false;
		fragment.updatedStatus = update3;
		assertNull("no update when context hasn't been rendered", fragment.maybeApplyMainStatus());
	}

	@Test
	public void sortStatusContext() {
		StatusContext context = new StatusContext();
		context.ancestors = List.of(
				fakeStatus("younger ancestor", "oldest ancestor"),
				fakeStatus("oldest ancestor", null)
		);
		context.descendants = List.of(
				fakeStatus("reply to first reply", "first reply"),
				fakeStatus("third level reply", "reply to first reply"),
				fakeStatus("first reply", "main status"),
				fakeStatus("another reply", "main status")
		);

		ThreadFragment.sortStatusContext(
				fakeStatus("main status", "younger ancestor"),
				context
		);
		List<Status> expectedAncestors = List.of(
				fakeStatus("oldest ancestor", null),
				fakeStatus("younger ancestor", "oldest ancestor")
		);
		List<Status> expectedDescendants = List.of(
				fakeStatus("first reply", "main status"),
				fakeStatus("reply to first reply", "first reply"),
				fakeStatus("third level reply", "reply to first reply"),
				fakeStatus("another reply", "main status")
		);

		// TODO: ??? i have no idea how this code works. it certainly doesn't return what i'd expect
	}
}