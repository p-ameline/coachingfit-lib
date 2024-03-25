package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Object to query information about a trainee<br>
 * <br>
 * Created: 16 May 2016<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitJobs4ArchetypeAction implements Action<GetCoachingFitJobs4ArchetypeResult> 
{	
	private int _iUserId ;	
	private int _iArchetypeId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitJobs4ArchetypeAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFitJobs4ArchetypeAction(final int iUserId, final int iArchetypeId) 
	{
		super() ;
		
		_iUserId      = iUserId ;
		_iArchetypeId = iArchetypeId ;
	}

	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId      = -1 ;
		_iArchetypeId = -1 ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public int getArchetypeId() {
		return _iArchetypeId ;
	}
	public void setArchetypeId(int iArchetypeId) {
		_iArchetypeId = iArchetypeId ;
	}
}
