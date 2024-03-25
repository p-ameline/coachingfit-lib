package com.coachingfit.shared.model ;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable ;
import com.primege.shared.database.FormDataData;
import com.primege.shared.model.FormBlock;

/**
 * Information from all trainees for a given coach in a given day
 * 
 * Created: 2 Jun 2016
 *
 * Author: PA
 * 
 */
public class CoachForDateBlock implements java.lang.Comparable<CoachForDateBlock>, IsSerializable 
{
	private int                                _iCoachId ;
	private String                             _sSessionDate ;
	private ArrayList<FormBlock<FormDataData>> _aTraineesData ;

	/**
	 * Default constructor (with zero information)
	 */
	public CoachForDateBlock() 
	{
		_iCoachId      = -1 ;
		_sSessionDate  = "" ;
		_aTraineesData = null ;
	}

	/**
	 * Plain vanilla constructor 
	 */
	public CoachForDateBlock(int iCoachId, String sDate, ArrayList<FormBlock<FormDataData>> aData) 
	{
		_iCoachId      = iCoachId ;
		_sSessionDate  = sDate ;
		_aTraineesData = null ;
		
		setInformation(aData) ;
	}
	
	/**
	 * Copy constructor 
	 */
	public CoachForDateBlock(final CoachForDateBlock model) 
	{
		_aTraineesData = null ;
		
		initFromCoach4DatesBlock(model) ;
	}
	
	public boolean isEmpty() {
		return ((null == _aTraineesData) || _aTraineesData.isEmpty()) ;
	}
	
	protected void reset()
	{
		_iCoachId     = -1 ;
		_sSessionDate = "" ;
		if (null != _aTraineesData)
			_aTraineesData.clear() ;
	}
	
	public void initFromCoach4DatesBlock(final CoachForDateBlock model)
	{
		reset() ;
		
		if (null == model)
			return ;
		
		_iCoachId     = model._iCoachId ;
		_sSessionDate = model._sSessionDate ;
		
		setInformation(model._aTraineesData) ;
	}
	
	public int getCoachId() {
		return _iCoachId ;
	}
	public void setCoachId(int iCoachId) {
		_iCoachId = iCoachId ;
	}
	
	public String getSessionDate() {
		return _sSessionDate ;
	}
	public void setSessionDate(final String sSessionDate) {
		_sSessionDate = sSessionDate ;
	}
	
	public ArrayList<FormBlock<FormDataData>> getInformation() {
		return _aTraineesData ;
	}
	public void setInformation(ArrayList<FormBlock<FormDataData>> aData)
	{
		if (null == aData)
			return ;

		if (null == _aTraineesData)
			_aTraineesData = new ArrayList<FormBlock<FormDataData>>() ;

		if (aData.isEmpty())
			return ;

		for (FormBlock<FormDataData> formBlock : aData)
			_aTraineesData.add(new FormBlock<FormDataData>(formBlock)) ;
	}
	public void addData(FormBlock<FormDataData> blockData)
	{
		if (null == _aTraineesData)
			_aTraineesData = new ArrayList<FormBlock<FormDataData>>() ;
			
		_aTraineesData.add(blockData) ;
	}
	
	@Override
	public int compareTo(CoachForDateBlock otherBlock)
	{
		if (null == otherBlock)
			return 1 ;
		
		if ((-1 != _iCoachId) && (_iCoachId != otherBlock._iCoachId))
			return (_iCoachId - otherBlock._iCoachId) ;
		
		if (false == "".equals(_sSessionDate))
			return _sSessionDate.compareTo(otherBlock._sSessionDate) ;
		
		return 0 ;
	}
}
