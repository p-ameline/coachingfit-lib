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
public class GetCoachingFitCoachsListAction implements Action<GetCoachingFitCoachsListResult> 
{	
	private int           _iUserId ;	
	private List<Integer> _aCoachsIds = new ArrayList<Integer>() ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitCoachsListAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFitCoachsListAction(final int iUserId, final List<Integer> aCoachsIds) 
	{
		super() ;
		
		_iUserId = iUserId ;
		setCoachsIds(aCoachsIds) ;
	}

	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId    = -1 ;
		_aCoachsIds.clear() ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public List<Integer> getCoachsIds() {
		return _aCoachsIds ;
	}
	public void setCoachsIds(final List<Integer> aCoachsIds)
	{
		_aCoachsIds.clear() ;
		
		if ((null == aCoachsIds) || aCoachsIds.isEmpty())
			return ;
		
		for (Integer coachId : aCoachsIds)
			if (false == _aCoachsIds.contains(coachId))
				_aCoachsIds.add(coachId) ;
	}
}
