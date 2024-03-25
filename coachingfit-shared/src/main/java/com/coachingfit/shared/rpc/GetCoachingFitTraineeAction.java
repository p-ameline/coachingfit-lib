package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Object to query information about a trainee<br>
 * <br>
 * Created: 16 May 2016<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitTraineeAction implements Action<GetCoachingFitTraineeResult> 
{	
	private int _iUserId ;	
	private int _iTraineeId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitTraineeAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFitTraineeAction(final int iUserId, final int iTraineeId) 
	{
		super() ;
		
		_iUserId    = iUserId ;
		_iTraineeId = iTraineeId ;		
	}

	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId    = -1 ;
		_iTraineeId = -1 ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public int getTraineeId() {
		return _iTraineeId ;
	}
	public void setTraineeId(int iTraineeId) {
		_iTraineeId = iTraineeId ;
	}
}
