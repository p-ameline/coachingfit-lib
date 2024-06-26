package com.coachingfit.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ecogen")
public interface CoachingFitService extends RemoteService {
	String primegeServer(String name);
}
