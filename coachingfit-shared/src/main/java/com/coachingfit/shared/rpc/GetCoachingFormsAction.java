package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Object to query information for a set of forms
 * 
 * Created: 16 May 2016
 * Author: PA
 * 
 */
public class GetCoachingFormsAction implements Action<GetCoachingFormsResult> 
{	
	private int    _iUserId ;
	
	private int    _iTraineeId ;
	private String _sSessionDateFrom ;
	private String _sSessionDateTo ;
	
	private int    _iAuthorId ;
	private int    _iSeniorTraineeId ;
	private String _sEntryDateFrom ;
	private String _sEntryDateTo ;
	
	private int    _iArchetypeId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFormsAction() 
	{
		super() ;
		reset() ;
	}
	
	/**
	 * Constructor for all forms of current user 
	 */
	public GetCoachingFormsAction(int iUserId) 
	{
		super() ;
		reset() ;
		
		_iUserId   = iUserId ;
		_iAuthorId = iUserId ; 
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public GetCoachingFormsAction(final int iUserId, 
			                          final int iTraineeId,
			                          final String sSessionDateFrom, final String sSessionDateTo, 
			                          final int iAuthorID,
			                          final int iSeniorTraineeId,
			                          final String sEntryDateFrom, final String sEntryDateTo, 
			                          final int iArchetypeID) 
	{
		super() ;
		
		_iUserId          = iUserId ;
		
		_iTraineeId       = iTraineeId ;
		
		_sSessionDateFrom = sSessionDateFrom ;
		_sSessionDateTo   = sSessionDateTo ;
		
		_iAuthorId        = iAuthorID ;
		_iSeniorTraineeId = iSeniorTraineeId ;
		_sEntryDateFrom   = sEntryDateFrom ;
		_sEntryDateTo     = sEntryDateTo ;
		
		_iArchetypeId     = iArchetypeID ;
	}

	/**
	 * Zeros all information
	 */
	public void reset()
	{
		_iUserId          = -1 ;
		_iTraineeId       = -1 ;
		_sSessionDateFrom = "" ;
		_sSessionDateTo   = "" ;
		_iAuthorId        = -1 ;
		_iSeniorTraineeId = -1 ;
		_sEntryDateFrom   = "" ;
		_sEntryDateTo     = "" ;
		_iArchetypeId     = -1 ; 
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

	public String getSessionDateFrom() {
		return _sSessionDateFrom ;
	}
	public void setSessionDateFrom(String sSessionDateFrom) {
		_sSessionDateFrom = sSessionDateFrom ;
	}

	public String getSessionDateTo() {
		return _sSessionDateTo ;
	}
	public void setSessionDateTo(String sSessionDateTo) {
		_sSessionDateTo = sSessionDateTo ;
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
	
	public String getEntryDateFrom() {
		return _sEntryDateFrom ;
	}
	public void setEntryDateFrom(String sEntryDateFrom) {
		_sEntryDateFrom = sEntryDateFrom ;
	}

	public String getEntryDateTo() {
		return _sEntryDateTo ;
	}
	public void setEntryDateTo(String sEntryDateTo) {
		_sEntryDateTo = sEntryDateTo ;
	}

	public int getArchetypeId() {
		return _iArchetypeId ;
	}
	public void setArchetypeId(int iArchetypeId) {
		_iArchetypeId = iArchetypeId ;
	}
}
