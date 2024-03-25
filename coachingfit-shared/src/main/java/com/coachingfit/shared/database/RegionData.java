package com.coachingfit.shared.database ;

import com.google.gwt.user.client.rpc.IsSerializable ;
import com.primege.shared.GlobalParameters;

/**
 * A RegionData object represents the selling area directed by a coach
 * 
 * Created: 19 Mar 2017
 * Author: PA
 * 
 */
public class RegionData implements IsSerializable 
{
	private int    _iId ;
	
	private String _sLabel ;
	private int    _iCoachId ;
	private int    _iZoneId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public RegionData() {
		reset() ;
	}
		
	/**
	 * Plain vanilla constructor 
	 */
	public RegionData(final int iID, final String sLabel, final int iCoachId, final int iZoneId) 
	{
		_iId      = iID ;
		
		_sLabel   = sLabel ;
		_iCoachId = iCoachId ;
		_iZoneId  = iZoneId ;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public RegionData(final RegionData model) 
	{
		reset() ;
		
		initFromModelData(model) ;
	}
			
	/**
	 * Initialize all information from another TraineeData
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public void initFromModelData(final RegionData model)
	{
		reset() ;
		
		if (null == model)
			return ;
		
		_iId      = model._iId ;
		_sLabel   = model._sLabel ;
		_iCoachId = model._iCoachId ;
		_iZoneId  = model._iZoneId ;
	}
		
	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		_iId      = -1 ;
		_sLabel   = "" ;
		_iCoachId = -1 ;
		_iZoneId  = -1 ;
	}
	
	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all data are zeros, false if not
	 */
	public boolean isEmpty()
	{
		if ((-1 == _iId)      &&
				(-1 == _iCoachId) &&
				("".equals(_sLabel)))
			return true ;
		
		return false ;
	}

	public int getId() {
		return _iId ;
	}
	public void setId(int iId) {
		_iId = iId ;
	}

	public int getCoachId() {
		return _iCoachId ;
	}
	public void setCoachId(int iCoachId) {
		_iCoachId = iCoachId ;
	}

	public String getLabel() {
  	return _sLabel ;
  }
	public void setLabel(String sLabel) {
		_sLabel = sLabel ;
  }
	
	public int getZoneId() {
		return _iZoneId ;
	}
	public void setZoneId(final int iZoneId) {
		_iZoneId = iZoneId ;
	}
	
	/**
	  * Determine whether two RegionData are exactly similar
	  * 
	  * @return true if all data are the same, false if not
	  * @param  otherData RegionData to compare with
	  * 
	  */
	public boolean equals(RegionData otherData)
	{
		if (this == otherData) {
			return true ;
		}
		if (null == otherData) {
			return false ;
		}
		
		return (_iId      == otherData._iId)      &&
					 (_iCoachId == otherData._iCoachId) &&
					 (_iZoneId  == otherData._iZoneId)  &&
		       GlobalParameters.areStringsEqual(_sLabel, otherData._sLabel) ;
	}

	/**
	  * Determine whether this RegionData is exactly similar to another object
	  * 
	  * @return true if all data are the same, false if not
	  * @param o Object to compare with
	  * 
	  */
	public boolean equals(Object o) 
	{
		if (this == o) {
			return true ;
		}
		if (null == o || getClass() != o.getClass()) {
			return false;
		}

		final RegionData formData = (RegionData) o ;

		return equals(formData) ;
	}
}
