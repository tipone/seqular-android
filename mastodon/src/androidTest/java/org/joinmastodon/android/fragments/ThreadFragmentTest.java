package org.joinmastodon.android.fragments;

import static org.junit.Assert.*;

import android.util.Pair;

import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.model.StatusContext;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class ThreadFragmentTest {

	private Status fakeStatus(String id, String inReplyTo) {
		Status status = Status.ofFake(id, null, null);
		status.inReplyToId = inReplyTo;
		return status;
	}

	private ThreadFragment.NeighborAncestryInfo fakeInfo(Status s, Status d, Status a) {
		ThreadFragment.NeighborAncestryInfo info = new ThreadFragment.NeighborAncestryInfo(s, d, a);
		return info;
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