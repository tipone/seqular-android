package net.seqular.network.fragments;

import net.seqular.network.api.session.AccountLocalPreferences;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.model.Instance;

import java.util.Optional;

public interface HasAccountID {
    String getAccountID();

    default AccountSession getSession() {
        return AccountSessionManager.getInstance().getAccount(getAccountID());
    }

    default boolean isInstanceAkkoma() {
        return getInstance().map(Instance::isAkkoma).orElse(false);
    }

    default boolean isInstancePixelfed() {
        return getInstance().map(Instance::isPixelfed).orElse(false);
    }

	default boolean isInstanceIceshrimp() {
		return getInstance().map(Instance::isIceshrimp).orElse(false);
	}

	default boolean isInstanceIceshrimpJs() {
		return getInstance().map(Instance::isIceshrimpJs).orElse(false);
	}

    default Optional<Instance> getInstance() {
        return getSession().getInstance();
    }

	default AccountLocalPreferences getLocalPrefs() {
		return AccountSessionManager.get(getAccountID()).getLocalPreferences();
	}
}
