package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

public class CoachingFitLoginUserInfo implements Action<CoachingFitLoginUserResult> 
{
	private String _sLogin ;
	private String _sPassword ;

	public CoachingFitLoginUserInfo(final String username, final String password) 
	{
		super() ;
		
		_sLogin    = username ;
		_sPassword = password ;
	}

	@SuppressWarnings("unused")
	private CoachingFitLoginUserInfo() 
	{
	}

	public String getUserName() {
		return _sLogin ;
	}

	public String getPassWord() {
		return _sPassword ;
	}
}
