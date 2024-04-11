package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Object to query information about a list of regions<br>
 * Author: PA<br>
 */
public class GetCoachingFitRegionsListAction implements Action<GetCoachingFitRegionsListResult> 
{	
	private int           _iUserId ;
	private List<Integer> _aRegionsIds = new ArrayList<>() ;
	private boolean       _bAllActives ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitRegionsListAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFitRegionsListAction(final int iUserId, final List<Integer> aCoachsIds) 
	{
		super() ;
		
		_iUserId = iUserId ;
		setCoachsIds(aCoachsIds) ;
		_bAllActives = false ;
	}

	/**
	 * Constructor to query all active users 
	 */
	public GetCoachingFitRegionsListAction(final int iUserId, final boolean bActive) 
	{
		super() ;
		
		_iUserId = iUserId ;
		_bAllActives = true ;
	}
	
	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId    = -1 ;
		_aRegionsIds.clear() ;
		_bAllActives = false ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public List<Integer> getRegionsIds() {
		return _aRegionsIds ;
	}
	public void setCoachsIds(final List<Integer> aCoachsIds)
	{
		_aRegionsIds.clear() ;
		
		if ((null == aCoachsIds) || aCoachsIds.isEmpty())
			return ;
		
		for (Integer coachId : aCoachsIds)
			if (false == _aRegionsIds.contains(coachId))
				_aRegionsIds.add(coachId) ;
	}
	
	public boolean getAllActive() {
		return _bAllActives ;
	}
}
