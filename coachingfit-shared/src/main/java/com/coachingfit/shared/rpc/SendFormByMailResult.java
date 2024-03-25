package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;

public class SendFormByMailResult implements Result 
{
	private String _sMessage ;
	
	public SendFormByMailResult()
	{
		super() ;
		
		_sMessage = "" ;
	}
	
	public SendFormByMailResult(final String sMessage) 
	{
		super() ;
		
		_sMessage = sMessage ;
	}

	public String getMessage() {
  	return _sMessage ;
  }
	public void setMessage(String sMessage) {
  	_sMessage = sMessage ;
  }
}
