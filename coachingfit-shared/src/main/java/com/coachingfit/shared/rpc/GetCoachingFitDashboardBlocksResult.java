package com.coachingfit.shared.rpc;

import com.coachingfit.shared.model.CoachingFitDashboardBlocks;

import net.customware.gwt.dispatch.shared.Result;

public class GetCoachingFitDashboardBlocksResult implements Result 
{
	private CoachingFitDashboardBlocks _aDashboardBlocks = new CoachingFitDashboardBlocks() ;
	private String                     _sMessage ;
	
	public GetCoachingFitDashboardBlocksResult()
	{
		super() ;
		
		_sMessage = "" ;
	}
	
	public GetCoachingFitDashboardBlocksResult(final String sMessage) 
	{
		super() ;
		
		_sMessage = sMessage ;
	}

	public CoachingFitDashboardBlocks getDashboardsBlocks() {
  	return _aDashboardBlocks ;
  }
	public void setDashboardsBlocks(final CoachingFitDashboardBlocks dashboardBlocks) {
		_aDashboardBlocks.initFromDashboardBlocks(dashboardBlocks) ;
  }

	public String getMessage() {
  	return _sMessage ;
  }
	public void setMessage(final String sMessage) {
  	_sMessage = sMessage ;
  }
}
