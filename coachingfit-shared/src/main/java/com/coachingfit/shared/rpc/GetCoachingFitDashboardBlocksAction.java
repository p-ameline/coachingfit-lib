package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

public class GetCoachingFitDashboardBlocksAction implements Action<GetCoachingFitDashboardBlocksResult> 
{	
	private int    _iUserId ;
	
	private String _sRoots ;
	
	private int    _iConstantTraineeId ;
	private int    _iConstantCoachId ;
	private String _sStartingDate ;
	private String _sEndingDate ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitDashboardBlocksAction() 
	{
		super() ;
		
		_iUserId            = -1 ;
		
		_sRoots             = "" ;
		
		_iConstantTraineeId = -1 ;
		_iConstantCoachId   = -1 ;
		_sStartingDate      = "" ;
		_sEndingDate        = "" ;
	}
	
	/**
	 * Constructor that specify that we want all sessions for a given trainee
	 */
	public GetCoachingFitDashboardBlocksAction(final int iUserId, final int iTraineeId, final String sRoots) 
	{
		super() ;
		
		_iUserId            = iUserId ;
		_sRoots             = sRoots ;
		_iConstantTraineeId = iTraineeId ;
		_iConstantCoachId   = -1 ;
		_sStartingDate      = "" ;
		_sEndingDate        = "" ;
	}
	
	/**
	 * Constructor that specify that we want the sessions by a given coach (possibly with a starting and/or an ending date)
	 */
	public GetCoachingFitDashboardBlocksAction(int iUserId, final String sRoots, final int iCoachId, final String sStartingDate, final String sEndingDate) 
	{
		super() ;
		
		_iUserId            = iUserId ;
		_sRoots             = sRoots ;
		_iConstantTraineeId = -1 ;
		_iConstantCoachId   = iCoachId ;
		_sStartingDate      = sStartingDate ;
		_sEndingDate        = sEndingDate ;
	}

	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}
	
	public String getRoots() {
		return _sRoots ;
	}
	public void setRoots(final String sRoots) {
		_sRoots = sRoots ;
	}
	
	public int getTraineeId() {
		return _iConstantTraineeId ;
	}
	public void setTraineeId(int iTraineeId) {
		_iConstantTraineeId = iTraineeId ;
	}

	public int getCoachId() {
		return _iConstantCoachId ;
	}
	public void setCoachId(int iCoachId) {
		_iConstantCoachId = iCoachId ;
	}
	
	public String getStartingDate() {
		return _sStartingDate ;
	}
	public void setStartingDate(final String sStartingDate) {
		_sStartingDate = sStartingDate ;
	}
	
	public String getEndingDate() {
		return _sEndingDate ;
	}
	public void setEndingDate(final String sEndingDate) {
		_sEndingDate = sEndingDate ;
	}
}
