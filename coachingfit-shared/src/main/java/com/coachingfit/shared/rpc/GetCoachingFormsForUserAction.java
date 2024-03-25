package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Object to query information for the set of forms a user is allowed to display
 * 
 * Created: 9 July 2019
 * Author: PA
 * 
 */
public class GetCoachingFormsForUserAction implements Action<GetCoachingFormsForUserResult> 
{	
	private int    _iUserId ;
	
	private int    _iAuthorId ;
	private int    _iSeniorTraineeId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFormsForUserAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Constructor for all forms of current user 
	 */
	public GetCoachingFormsForUserAction(int iUserId) 
	{
		super() ;
		reset() ;
		
		_iUserId   = iUserId ;
		_iAuthorId = iUserId ; 
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFormsForUserAction(final int iUserId, final int iAuthorID, final int iSeniorTraineeId) 
	{
		super() ;
		
		_iUserId          = iUserId ;
		
		_iAuthorId        = iAuthorID ;
		_iSeniorTraineeId = iSeniorTraineeId ;
	}

	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId          = -1 ;
		_iAuthorId        = -1 ;
		_iSeniorTraineeId = -1 ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public int getAuthorId() {
		return _iAuthorId ;
	}
	public void setAuthorId(int iAuthorId) {
		_iAuthorId = iAuthorId ;
	}

	public int getSeniorTraineeId() {
		return _iSeniorTraineeId ;
	}
	public void setSeniorTraineeId(int iSeniorTraineeId) {
		_iSeniorTraineeId = iSeniorTraineeId ;
	}
}
