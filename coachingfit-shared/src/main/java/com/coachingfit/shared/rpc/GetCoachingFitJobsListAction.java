package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Object to query information about a list of trainee<br>
 * <br>
 * Created: 2 November 2019<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitJobsListAction implements Action<GetCoachingFitJobsListResult> 
{	
	private int           _iUserId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitJobsListAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFitJobsListAction(final int iUserId) 
	{
		super() ;
		
		_iUserId = iUserId ;
	}
	
	/**
	 * Zeros all information
	 */
	public void reset() {
		_iUserId = -1 ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}
}
