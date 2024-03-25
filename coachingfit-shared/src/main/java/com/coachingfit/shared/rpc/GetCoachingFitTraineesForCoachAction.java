package com.coachingfit.shared.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Object to query the list of trainees for a given coach<br>
 * <br>
 * Created: 21 November 2019<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitTraineesForCoachAction implements Action<GetCoachingFitTraineesForCoachResult>, IsSerializable
{	
	private int     _iUserId ;
	private int     _iCoachId ;
	
	private boolean _bIncludeGenericRules ;
	private boolean _bGenericRulesOnly ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitTraineesForCoachAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Usual constructor (no generic rules)
	 */
	public GetCoachingFitTraineesForCoachAction(final int iUserId, final int iCoachId) 
	{
		super() ;
		
		_iUserId  = iUserId ;
		_iCoachId = iCoachId ;
		
		_bIncludeGenericRules = false ;
		_bGenericRulesOnly    = false ;
	}
	
	/**
	 * Plain vanilla constructor
	 */
	public GetCoachingFitTraineesForCoachAction(final int iUserId, final int iCoachId, boolean bIncludeGenericRules, boolean bGenericRulesOnly) 
	{
		super() ;
		
		_iUserId  = iUserId ;
		_iCoachId = iCoachId ;
		
		_bIncludeGenericRules = bIncludeGenericRules ;
		_bGenericRulesOnly    = bGenericRulesOnly ;
	}

	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId  = -1 ;
		_iCoachId = -1 ;
		
		_bIncludeGenericRules = false ;
		_bGenericRulesOnly    = false ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public int getCoachId() {
		return _iCoachId ;
	}
	public void setCoachId(int iCoachId) {
		_iCoachId = iCoachId ;
	}
	
	public boolean mustIncludeGenericRules() {
		return _bIncludeGenericRules ;
	}
	public void setIncludeGenericRules(boolean bIncludeGenericRules) {
		_bIncludeGenericRules = bIncludeGenericRules ;
	}
	
	public boolean mustOnlyIncludeGenericRules() {
		return _bGenericRulesOnly ;
	}
	public void setIncludeOnlyGenericRules(boolean bGenericRulesOnly) {
		_bGenericRulesOnly = bGenericRulesOnly ;
	}
}
