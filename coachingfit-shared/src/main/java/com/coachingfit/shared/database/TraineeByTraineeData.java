package com.coachingfit.shared.database ;

import com.google.gwt.user.client.rpc.IsSerializable ;

/**
 * A TraineeByTraineeData object represents a trainee that can be trained by a senior trainee
 * 
 * Author: PA
 * 
 */
public class TraineeByTraineeData implements IsSerializable 
{
	private int    _iId ;
	
	private int    _iTraineeId ;
	private int    _iSeniorTraineeId ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public TraineeByTraineeData() {
		reset() ;
	}
		
	/**
	 * Plain vanilla constructor 
	 */
	public TraineeByTraineeData(final int iID, final int iTraineeId, final int iSeniorTraineeId) 
	{
		_iId              = iID ;
		
		_iTraineeId       = iTraineeId ;
		_iSeniorTraineeId = iSeniorTraineeId ;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public TraineeByTraineeData(final TraineeByTraineeData model) 
	{
		reset() ;
		
		initFromOther(model) ;
	}
			
	/**
	 * Initialize all information from another TraineeData
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public void initFromOther(final TraineeByTraineeData model)
	{
		reset() ;
		
		if (null == model)
			return ;
		
		_iId              = model._iId ;
		_iTraineeId       = model._iTraineeId ;
		_iSeniorTraineeId = model._iSeniorTraineeId ;
	}
		
	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		_iId              = -1 ;
		_iTraineeId       = -1 ;
		_iSeniorTraineeId = -1 ;
	}
	
	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all crucial data are zeros, false if not
	 */
	public boolean isEmpty()
	{
		if ((-1 == _iId)      &&
				(-1 == _iTraineeId) &&
				(-1 == _iSeniorTraineeId))
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

	public int getSeniorTraineeId() {
		return _iSeniorTraineeId ;
	}
	public void setSeniorTraineeId(final int iSeniorTraineeId) {
		_iSeniorTraineeId = iSeniorTraineeId ;
	}
	
	/**
	  * Determine whether two TraineeByTraineeData are exactly similar
	  * 
	  * @return <code>true</code> if all data are the same, <code>false</code> if not
	  * @param  otherData TraineeByTraineeData to compare with
	  * 
	  */
	public boolean equals(final TraineeByTraineeData otherData)
	{
		if (this == otherData) {
			return true ;
		}
		if (null == otherData) {
			return false ;
		}
		
		return (_iId              == otherData._iId)        &&
					 (_iTraineeId       == otherData._iTraineeId) &&
					 (_iSeniorTraineeId == otherData._iSeniorTraineeId) ;
	}

	/**
	  * Determine whether this TraineeByTraineeData is exactly similar to another object
	  * 
	  * @return true if all data are the same, false if not
	  * @param o Object to compare with
	  * 
	  */
	public boolean equals(final Object o) 
	{
		if (this == o) {
			return true ;
		}
		if (null == o || getClass() != o.getClass()) {
			return false;
		}

		final TraineeByTraineeData formData = (TraineeByTraineeData) o ;

		return equals(formData) ;
	}
}
