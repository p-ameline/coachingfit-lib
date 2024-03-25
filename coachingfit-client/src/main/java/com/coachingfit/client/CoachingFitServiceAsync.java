package com.coachingfit.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>EcogenService</code>.
 */
public interface CoachingFitServiceAsync {
	void primegeServer(String input, AsyncCallback<String> callback);
}
