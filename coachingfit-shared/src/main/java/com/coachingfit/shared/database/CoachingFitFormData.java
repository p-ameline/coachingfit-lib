package com.coachingfit.shared.database ;

import com.google.gwt.user.client.rpc.IsSerializable ;

import com.primege.shared.GlobalParameters;
import com.primege.shared.database.FormDataModel;

/**
 * A FormData object represents the documentary label of a form stored in database
 * 
 * Created: 16 May 2016
 * Author: PA
 * 
 */
public class CoachingFitFormData extends FormDataModel implements Comparable<CoachingFitFormData>, IsSerializable  
{
	private int     _iTraineeId ;
	private int     _iSeniorTraineeId ;
	private int     _iRegionId ;
	private String  _sCoachingDate ;
		
	/**
	 * Default constructor (with zero information)
	 */
	public CoachingFitFormData()
	{
		super() ;
		reset() ;
	}
		
	/**
	 * Plain vanilla constructor 
	 */
	public CoachingFitFormData(final int iID, final String sActionId, final String sRoot, final int iTraineeID, final int iRegionID, final String sCoachingDate, final int iAuthorID, final int iSeniorTraineeId, final String sEntryDateHour, final int iArchetypeID, final FormDataModel.FormStatus iStatus) 
	{
		super(iID, sActionId, sRoot, iAuthorID, sEntryDateHour, iArchetypeID, iStatus) ;
		
		_iTraineeId       = iTraineeID ;
		_iRegionId        = iRegionID ;
		_iSeniorTraineeId = iSeniorTraineeId ; 
		_sCoachingDate    = sCoachingDate ;		
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param model FormData to initialize from 
	 */
	public CoachingFitFormData(final CoachingFitFormData model) 
	{
		reset() ;
		
		initFromFormData(model) ;
	}
			
	/**
	 * Initialize all information from another FormData
	 * 
	 * @param model FormData to initialize from 
	 */
	public void initFromFormData(final CoachingFitFormData model)
	{
		reset() ;
		
		if (null == model)
			return ;
		
		initFromFormDataModel((FormDataModel) model) ;
		
		_iTraineeId       = model._iTraineeId ;
		_iSeniorTraineeId = model._iSeniorTraineeId ;
		_iRegionId        = model._iRegionId ;
		_sCoachingDate    = model._sCoachingDate ;
	}
		
	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		super.reset() ;
		
		_iTraineeId       = -1 ;
		_iSeniorTraineeId = -1 ;
		_iRegionId        = -1 ;
		_sCoachingDate    = "" ;
	}
	
	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all data are zeros, false if not
	 */
	public boolean isEmpty()
	{
		if (super.isEmpty()     &&
				(-1 == _iTraineeId) &&
				(-1 == _iSeniorTraineeId) &&
				("".equals(_sCoachingDate)))
			return true ;
		
		return false ;
	}
	
	/**
	 * Build the dynamic label of this form 
	 * 
	 * @return The label dynamically built from site, city, etc.
	 */
	public String getLabel() {
		return "" ;
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
	
	public int getRegionId() {
		return _iRegionId ;
	}
	public void setRegionId(final int iRegionId) {
		_iRegionId = iRegionId ;
	}
	
	public String getCoachingDate() {
  	return _sCoachingDate ;
  }
	public void setCoachingDate(final String sCoachingDate) {
		_sCoachingDate = sCoachingDate ;
  }
	
	/**
	  * Determine whether two FormData are exactly similar
	  * 
	  * @return true if all data are the same, false if not
	  * @param  formData FormData to compare with
	  * 
	  */
	public boolean equals(final CoachingFitFormData formData)
	{
		if (this == formData) {
			return true ;
		}
		if (null == formData) {
			return false ;
		}
		
		FormDataModel model     = (FormDataModel) formData ;
		FormDataModel modelThis = (FormDataModel) this ;
		
		return (modelThis.equals(model))  &&
					 (_iTraineeId == formData._iTraineeId) &&
					 (_iSeniorTraineeId == formData._iSeniorTraineeId) &&
					 (_iRegionId  == formData._iRegionId)  &&
		       GlobalParameters.areStringsEqual(_sCoachingDate, formData._sCoachingDate) ;
	}

	/**
	  * Determine whether this FormData is exactly similar to another object
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

		final CoachingFitFormData formData = (CoachingFitFormData) o ;

		return equals(formData) ;
	}

	@Override
	public int compareTo(final CoachingFitFormData n) {
		return _sCoachingDate.compareTo(n._sCoachingDate) ;
	}
}
