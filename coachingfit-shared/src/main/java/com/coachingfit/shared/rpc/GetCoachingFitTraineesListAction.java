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
public class GetCoachingFitTraineesListAction implements Action<GetCoachingFitTraineesListResult> 
{	
	private int           _iUserId ;	
	private List<Integer> _aTraineesIds = new ArrayList<Integer>() ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitTraineesListAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFitTraineesListAction(final int iUserId, final List<Integer> aTraineesIds) 
	{
		super() ;
		
		_iUserId = iUserId ;
		setTraineesIds(aTraineesIds) ;
	}

	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId    = -1 ;
		_aTraineesIds.clear() ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public List<Integer> getTraineesIds() {
		return _aTraineesIds ;
	}
	public void setTraineesIds(final List<Integer> aTraineesIds)
	{
		_aTraineesIds.clear() ;
		
		if ((null == aTraineesIds) || aTraineesIds.isEmpty())
			return ;
		
		for (Integer traineeId : aTraineesIds)
			_aTraineesIds.add(new Integer(traineeId)) ;
	}
}
