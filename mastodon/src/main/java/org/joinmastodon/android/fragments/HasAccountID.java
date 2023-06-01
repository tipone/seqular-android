package org.joinmastodon.android.fragments;

import org.joinmastodon.android.api.session.AccountSession;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.Instance;

import java.util.Optional;

public interface HasAccountID {
    String getAccountID();

    default AccountSession getSession() {
        return AccountSessionManager.getInstance().getAccount(getAccountID());
    }

    default boolean isInstanceAkkoma() {
        return getInstance().map(Instance::isAkkoma).orElse(false);
    }

    default Optional<Instance> getInstance() {
        return getSession().getInstance();
    }
}
