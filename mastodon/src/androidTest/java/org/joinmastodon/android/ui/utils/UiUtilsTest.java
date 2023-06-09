package org.joinmastodon.android.ui.utils;

import static org.junit.Assert.*;

import android.util.Pair;

import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.Account;
import org.joinmastodon.android.model.Instance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

public class UiUtilsTest {
	@BeforeClass
	public static void createDummySession() {
		Instance dummyInstance = new Instance();
		dummyInstance.uri = "test.tld";
		Account dummyAccount = new Account();
		dummyAccount.id = "123456";
		AccountSessionManager.getInstance().addAccount(dummyInstance, null, dummyAccount, null, null);
	}

	@AfterClass
	public static void cleanUp() {
		AccountSessionManager.getInstance().removeAccount("test.tld_123456");
	}

	@Test
	public void parseFediverseHandle() {
		assertEquals(
				Optional.of(Pair.create("megalodon", Optional.of("floss.social"))),
				UiUtils.parseFediverseHandle("megalodon@floss.social")
		);

		assertEquals(
				Optional.of(Pair.create("megalodon", Optional.of("floss.social"))),
				UiUtils.parseFediverseHandle("@megalodon@floss.social")
		);

		assertEquals(
				Optional.of(Pair.create("megalodon", Optional.empty())),
				UiUtils.parseFediverseHandle("@megalodon")
		);

		assertEquals(
				Optional.of(Pair.create("megalodon", Optional.of("floss.social"))),
				UiUtils.parseFediverseHandle("mailto:megalodon@floss.social")
		);

		assertEquals(
				Optional.empty(),
				UiUtils.parseFediverseHandle("megalodon")
		);

		assertEquals(
				Optional.empty(),
				UiUtils.parseFediverseHandle("this is not a fedi handle")
		);

		assertEquals(
				Optional.empty(),
				UiUtils.parseFediverseHandle("not@a-domain")
		);
	}

	@Test
	public void acctMatches() {
		assertTrue("local account, domain not specified", UiUtils.acctMatches(
				"test.tld_123456",
				"someone",
				"someone",
				null
		));

		assertTrue("domain not specified", UiUtils.acctMatches(
				"test.tld_123456",
				"someone@somewhere.social",
				"someone",
				null
		));

		assertTrue("local account, domain specified, different casing", UiUtils.acctMatches(
				"test.tld_123456",
				"SomeOne",
				"someone",
				"Test.TLD"
		));

		assertFalse("username doesn't match", UiUtils.acctMatches(
				"test.tld_123456",
				"someone-else@somewhere.social",
				"someone",
				"somewhere.social"
		));

		assertFalse("domain doesn't match", UiUtils.acctMatches(
				"test.tld_123456",
				"someone@somewhere.social",
				"someone",
				"somewhere.else"
		));
	}
}