package com.coachingfit.shared.model ;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable ;

/**
 * All sessions for a given coach and/or a period of time<br>
 * <br>
 * 
 * Created: 23 Feb 2017<br>
 * Author: PA
 * 
 */
public class CoachingFitDashboardBlocks implements IsSerializable 
{
	private int                       _iConstantCoachId ;
	private String                    _sStartingDate ;
	private String                    _sEndingDate ;
	private ArrayList<CoachForDateBlock> _aData ;

	/**
	 * Default constructor (with zero information)
	 */
	public CoachingFitDashboardBlocks() 
	{
		_sStartingDate    = "" ;
		_sEndingDate      = "" ;
		_iConstantCoachId = -1 ;
		_aData            = null ;
	}

	/**
	 * Plain vanilla constructor 
	 */
	public CoachingFitDashboardBlocks(final int iCoachId, final String sStartingDate, final String sEndingDate, ArrayList<CoachForDateBlock> aData) 
	{
		_iConstantCoachId = iCoachId ;
		_sStartingDate    = sStartingDate ;
		_sEndingDate      = sEndingDate ;
		_aData            = null ;
		
		setInformation(aData) ;
	}

	/**
	 * Copy constructor 
	 */
	public CoachingFitDashboardBlocks(final CoachingFitDashboardBlocks model) 
	{
		_aData    = null ;
		
		initFromDashboardBlocks(model) ;
	}
	
	public boolean isEmpty() {
		return ((null == _aData) || _aData.isEmpty()) ;
	}
	
	public void reset()
	{
		_sStartingDate    = "" ;
		_sEndingDate      = "" ;
		_iConstantCoachId = -1 ;

		if (null != _aData)
			_aData.clear() ;
	}
	
	public void initFromDashboardBlocks(final CoachingFitDashboardBlocks model)
	{
		reset() ;
		
		if (null == model)
			return ;
		
		_sStartingDate    = model._sStartingDate ;
		_sEndingDate      = model._sEndingDate ;
		_iConstantCoachId = model._iConstantCoachId ;
		
		setInformation(model._aData) ;
	}
	
	public String getStartingDate() {
		return _sStartingDate ;
	}
	public void setStartingDate(final String sDate) {
		_sStartingDate = sDate ;
	}
	
	public String getEndingDate() {
		return _sEndingDate ;
	}
	public void setEndingDate(final String sDate) {
		_sEndingDate = sDate ;
	}
	
	public int getConstantCoachId() {
		return _iConstantCoachId ;
	}
	public void setConstantCoachId(final int iCoachId) {
		_iConstantCoachId = iCoachId ;
	}
		
	public ArrayList<CoachForDateBlock> getInformation() {
		return _aData ;
	}
	public void setInformation(final ArrayList<CoachForDateBlock> aData)
	{
		if (null == aData)
			return ;

		if (null == _aData)
			_aData = new ArrayList<CoachForDateBlock>() ;

		if (aData.isEmpty())
			return ;

		for (CoachForDateBlock coachForDate : aData)
			_aData.add(new CoachForDateBlock(coachForDate)) ;
	}
	public void addData(CoachForDateBlock formData)
	{
		if (null == _aData)
			_aData = new ArrayList<CoachForDateBlock>() ;
			
		_aData.add(formData) ;
	}
}
