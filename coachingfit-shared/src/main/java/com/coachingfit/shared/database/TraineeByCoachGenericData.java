package com.coachingfit.shared.database ;

import com.google.gwt.user.client.rpc.IsSerializable ;

/**
 * A CoachByCoachGenericData object represents a rule that defines a set of Coachs that can be trained by a coach that is not her main coach
 * 
 * Author: PA
 * 
 */
public class TraineeByCoachGenericData implements IsSerializable 
{
	private int    _iId ;

	private int    _iCoachId ;
	private String _sParameterType ;
	private int    _iParameterId ;

	/**
	 * Default constructor (with zero information)
	 */
	public TraineeByCoachGenericData() {
		reset() ;
	}

	/**
	 * Plain vanilla constructor 
	 */
	public TraineeByCoachGenericData(final int iID, final int iCoachId, final String sParameterType, final int iParameterId) 
	{
		_iId            = iID ;

		_iCoachId     = iCoachId ;
		_sParameterType = sParameterType ;
		_iParameterId   = iParameterId ;
	}

	/**
	 * Copy constructor
	 * 
	 * @param model CoachData to initialize from 
	 */
	public TraineeByCoachGenericData(final TraineeByCoachGenericData model) 
	{
		reset() ;

		initFromOther(model) ;
	}

	/**
	 * Initialize all information from another CoachData
	 * 
	 * @param model CoachData to initialize from 
	 */
	public void initFromOther(final TraineeByCoachGenericData model)
	{
		reset() ;

		if (null == model)
			return ;

		_iId            = model._iId ;
		_iCoachId       = model._iCoachId ;
		_sParameterType = model._sParameterType ;
		_iParameterId   = model._iParameterId ;
	}

	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		_iId            = -1 ;
		_iCoachId       = -1 ;
		_sParameterType = "" ;
		_iParameterId   = -1 ;
	}

	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all crucial data are zeros, false if not
	 */
	public boolean isEmpty()
	{
		if ((-1 == _iId)        &&
			(-1 == _iCoachId) &&
			(-1 == _iParameterId) &&
			_sParameterType.isEmpty())
			return true ;

		return false ;
	}

	public int getId() {
		return _iId ;
	}
	public void setId(final int iId) {
		_iId = iId ;
	}

	public int getCoachId() {
		return _iCoachId ;
	}
	public void setCoachId(final int iCoachId) {
		_iCoachId = iCoachId ;
	}

	public String getParameterType() {
		return _sParameterType ;
	}
	public void setParameterType(final String sParameterType) {
		_sParameterType = (null == sParameterType) ? "" : sParameterType ;
	}

	public int getParameterId() {
		return _iParameterId ;
	}
	public void setParameterId(final int iParameterId) {
		_iParameterId = iParameterId ;
	}

	/**
	 * Determine whether two CoachByCoachGenericData are exactly similar
	 * 
	 * @return <code>true</code> if all data are the same, <code>false</code> if not
	 * @param  otherData CoachByCoachGenericData to compare with
	 * 
	 */
	public boolean equals(final TraineeByCoachGenericData otherData)
	{
		if (this == otherData) {
			return true ;
		}
		if (null == otherData) {
			return false ;
		}

		return (_iId          == otherData._iId)        &&
			   (_iCoachId     == otherData._iCoachId) &&
			   (_iParameterId == otherData._iParameterId) &&
			   _sParameterType.equals(otherData._sParameterType) ;
	}

	/**
	 * Determine whether this CoachByCoachGenericData is exactly similar to another object
	 * 
	 * @return true if all data are the same, false if not
	 * @param o Object to compare with
	 * 
	 */
	@Override
	public boolean equals(final Object o) 
	{
		if (this == o) {
			return true ;
		}
		if (null == o || getClass() != o.getClass()) {
			return false;
		}

		final TraineeByCoachGenericData formData = (TraineeByCoachGenericData) o ;

		return equals(formData) ;
	}
}
