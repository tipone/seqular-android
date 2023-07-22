package org.joinmastodon.android.utils;

import static org.joinmastodon.android.model.Filter.FilterAction.HIDE;
import static org.joinmastodon.android.model.Filter.FilterAction.WARN;
import static org.joinmastodon.android.model.Filter.FilterContext.HOME;
import static org.joinmastodon.android.model.Filter.FilterContext.PUBLIC;
import static org.joinmastodon.android.model.Filter.FilterContext.THREAD;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.drawable.ColorDrawable;

import org.joinmastodon.android.model.Attachment;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.Status;
import org.junit.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

public class StatusFilterPredicateTest {

    private static final Filter hideMeFilter = new Filter(), warnMeFilter = new Filter();
    private static final List<Filter> allFilters = List.of(hideMeFilter, warnMeFilter);

    private static final Status
            hideInHomePublic = Status.ofFake(null, "hide me, please", Instant.now()),
            warnInHomePublic = Status.ofFake(null, "display me with a warning", Instant.now()),
            noAltText = Status.ofFake(null, "display me with a warning", Instant.now()),
            withAltText = Status.ofFake(null, "display me with a warning", Instant.now());

    static {
        hideMeFilter.phrase = "hide me";
        hideMeFilter.filterAction = HIDE;
        hideMeFilter.context = EnumSet.of(PUBLIC, HOME);

        warnMeFilter.phrase = "warning";
        warnMeFilter.filterAction = WARN;
        warnMeFilter.context = EnumSet.of(PUBLIC, HOME);

        noAltText.mediaAttachments = Attachment.createFakeAttachments("fakeurl", new ColorDrawable());
        withAltText.mediaAttachments = Attachment.createFakeAttachments("fakeurl", new ColorDrawable());
        for (Attachment mediaAttachment : withAltText.mediaAttachments) {
            mediaAttachment.description = "Alt Text";
        }
    }

    @Test
    public void testHide() {
        assertFalse("should not pass because matching filter applies to given context",
                new StatusFilterPredicate(allFilters, HOME).test(hideInHomePublic));
    }

    @Test
    public void testHideRegardlessOfContext() {
        assertTrue("filters without context should always pass",
                new StatusFilterPredicate(allFilters, null).test(hideInHomePublic));
    }

    @Test
    public void testHideInDifferentContext() {
        assertTrue("should pass because matching filter does not apply to given context",
                new StatusFilterPredicate(allFilters, THREAD).test(hideInHomePublic));
    }

    @Test
    public void testHideWithWarningText() {
        assertTrue("should pass because matching filter is for warnings",
                new StatusFilterPredicate(allFilters, HOME).test(warnInHomePublic));
    }

    @Test
    public void testWarn() {
        assertFalse("should not pass because filter applies to given context",
                new StatusFilterPredicate(allFilters, HOME, WARN).test(warnInHomePublic));
    }

    @Test
    public void testWarnRegardlessOfContext() {
        assertTrue("filters without context should always pass",
                new StatusFilterPredicate(allFilters, null, WARN).test(warnInHomePublic));
    }

    @Test
    public void testWarnInDifferentContext() {
        assertTrue("should pass because filter does not apply to given context",
                new StatusFilterPredicate(allFilters, THREAD, WARN).test(warnInHomePublic));
    }

    @Test
    public void testWarnWithHideText() {
        assertTrue("should pass because matching filter is for hiding",
                new StatusFilterPredicate(allFilters, HOME, WARN).test(hideInHomePublic));
    }

    @Test
    public void testAltTextFilterNoPass() {
        assertFalse("should not pass because of no alt text",
                new StatusFilterPredicate(allFilters, HOME).test(noAltText));
    }

    @Test
    public void testAltTextFilterPass() {
        assertTrue("should pass because of alt text",
                new StatusFilterPredicate(allFilters, HOME).test(withAltText));
    }
}