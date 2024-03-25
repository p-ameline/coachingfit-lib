package com.coachingfit.shared.database ;

import com.google.gwt.user.client.rpc.IsSerializable ;

/**
 * A TraineeByCoachData object represents a trainee that can be trained by a coach that is not her main coach
 * 
 * Author: PA
 * 
 */
public class TraineeByCoachData implements IsSerializable 
{
	private int    _iId ;
	
	private int    _iTraineeId ;
	private int    _iCoachId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public TraineeByCoachData() {
		reset() ;
	}
		
	/**
	 * Plain vanilla constructor 
	 */
	public TraineeByCoachData(final int iID, final int iTraineeId, final int iCoachId) 
	{
		_iId        = iID ;
		
		_iTraineeId = iTraineeId ;
		_iCoachId   = iCoachId ;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param model TraineeByCoachData to initialize from 
	 */
	public TraineeByCoachData(final TraineeByCoachData model) 
	{
		reset() ;
		
		initFromOther(model) ;
	}
			
	/**
	 * Initialize all information from another TraineeData
	 * 
	 * @param model TraineeByCoachData to initialize from 
	 */
	public void initFromOther(final TraineeByCoachData model)
	{
		reset() ;
		
		if (null == model)
			return ;
		
		_iId        = model._iId ;
		_iTraineeId = model._iTraineeId ;
		_iCoachId   = model._iCoachId ;
	}
		
	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		_iId        = -1 ;
		_iTraineeId = -1 ;
		_iCoachId   = -1 ;
	}
	
	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all crucial data are zeros, false if not
	 */
	public boolean isEmpty()
	{
		if ((-1 == _iId)        &&
			(-1 == _iTraineeId) &&
			(-1 == _iCoachId))
			return true ;
		
		return false ;
	}

	public int getId() {
		return _iId ;
	}
	public void setId(final int iId) {
		_iId = iId ;
	}

	public int getTraineeId() {
		return _iTraineeId ;
	}
	public void setTraineeId(final int iTraineeId) {
		_iTraineeId = iTraineeId ;
	}

	public int getCoachId() {
		return _iCoachId ;
	}
	public void setCoachId(final int iCoachId) {
		_iCoachId = iCoachId ;
	}
	
	/**
	  * Determine whether two TraineeByCoachData are exactly similar
	  * 
	  * @return <code>true</code> if all data are the same, <code>false</code> if not
	  * @param  otherData TraineeByCoachData to compare with
	  * 
	  */
	public boolean equals(final TraineeByCoachData otherData)
	{
		if (this == otherData) {
			return true ;
		}
		if (null == otherData) {
			return false ;
		}
		
		return (_iId        == otherData._iId)        &&
			   (_iTraineeId == otherData._iTraineeId) &&
			   (_iCoachId   == otherData._iCoachId) ;
	}

	/**
	  * Determine whether this TraineeByCoachData is exactly similar to another object
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

		final TraineeByCoachData formData = (TraineeByCoachData) o ;

		return equals(formData) ;
	}
}
