package com.coachingfit.shared.database ;

import com.google.gwt.user.client.rpc.IsSerializable ;
import com.primege.shared.GlobalParameters;

/**
 * A TraineeData object represents a trained person
 * 
 * Created: 18 Feb 2017
 * Author: PA
 * 
 */
public class TraineeData implements IsSerializable 
{
	private int    _iId ;

	private String _sLabel ;
	private String _sFirstName ;
	private int    _iCoachId ;
	private int    _iRegionId ;
	private String _sEMail ;
	private String _sJobType ;
	private String _sJobStartDate ;
	private String _sPassword ;

	/**
	 * Default constructor (with zero information)
	 */
	public TraineeData() {
		reset() ;
	}

	/**
	 * Plain vanilla constructor 
	 */
	public TraineeData(final int iID, final String sLabel, final String sFirstName, final int iCoachId, final int iRegionId, final String sEMail, final String sJobType, final String sJobStartDate, final String sPassword) 
	{
		_iId           = iID ;

		_sLabel        = sLabel ;
		_sFirstName    = sFirstName ;
		_iCoachId      = iCoachId ;
		_iRegionId     = iRegionId ;
		_sEMail        = sEMail ;
		_sJobType      = sJobType ;
		_sJobStartDate = sJobStartDate ;
		_sPassword     = sPassword ;
	}

	/**
	 * Copy constructor
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public TraineeData(final TraineeData model) 
	{
		reset() ;

		initFromOther(model) ;
	}

	/**
	 * Initialize all information from another TraineeData
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public void initFromOther(final TraineeData model)
	{
		reset() ;

		if (null == model)
			return ;

		_iId           = model._iId ;
		_sLabel        = model._sLabel ;
		_sFirstName    = model._sFirstName ;
		_iCoachId      = model._iCoachId ;
		_iRegionId     = model._iRegionId ;
		_sEMail        = model._sEMail ;
		_sJobType      = model._sJobType ;
		_sJobStartDate = model._sJobStartDate ;
		_sPassword     = model._sPassword ;
	}

	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		_iId           = -1 ;
		_sLabel        = "" ;
		_sFirstName    = "" ;
		_iCoachId      = -1 ;
		_iRegionId     = -1 ;
		_sEMail        = "" ;
		_sJobType      = "" ;
		_sJobStartDate = "" ;
		_sPassword     = "" ;
	}

	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all crucial data are zeros, false if not
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
	public void setId(final int iId) {
		_iId = iId ;
	}

	public int getCoachId() {
		return _iCoachId ;
	}
	public void setCoachId(final int iCoachId) {
		_iCoachId = iCoachId ;
	}

	public int getRegionId() {
		return _iRegionId ;
	}
	public void setRegionId(final int iRegionId) {
		_iRegionId = iRegionId ;
	}

	public String getLabel() {
		return _sLabel ;
	}
	public void setLabel(final String sLabel) {
		_sLabel = (null == sLabel) ? "" : sLabel ;
	}

	public String getFirstName() {
		return _sFirstName ;
	}
	public void setFirstName(final String sFirstName) {
		_sFirstName = (null == sFirstName) ? "" : sFirstName ;
	}

	public String getLastName()
	{
		if (false == _sLabel.startsWith(_sFirstName))
			return "" ;
		
		return _sLabel.substring(_sFirstName.length()).trim() ;
	}
	
	public String getEMail() {
		return _sEMail ;
	}
	public void setEMail(final String sEMail) {
		_sEMail = sEMail ;
	}

	public String getJobType() {
		return _sJobType ;
	}
	public void setJobType(final String sJobType) {
		_sJobType = sJobType ;
	}

	public String getJobStartDate() {
		return _sJobStartDate ;
	}
	public void setJobStartDate(final String sJobStartDate) {
		_sJobStartDate = sJobStartDate ;
	}

	public String getPassword() {
		return _sPassword ;
	}
	public void setPassword(final String sPassword) {
		_sPassword = sPassword ;
	}

	/**
	 * Determine whether two TraineeData are exactly similar
	 * 
	 * @return true if all data are the same, false if not
	 * @param  otherData TraineeData to compare with
	 * 
	 */
	public boolean equals(final TraineeData otherData)
	{
		if (this == otherData) {
			return true ;
		}
		if (null == otherData) {
			return false ;
		}

		return (_iId       == otherData._iId)       &&
				(_iCoachId  == otherData._iCoachId)  &&
				(_iRegionId == otherData._iRegionId) &&
				GlobalParameters.areStringsEqual(_sLabel,        otherData._sLabel) &&
				GlobalParameters.areStringsEqual(_sFirstName,    otherData._sFirstName) &&
				GlobalParameters.areStringsEqual(_sEMail,        otherData._sEMail) && 
				GlobalParameters.areStringsEqual(_sJobType,      otherData._sJobType) &&
				GlobalParameters.areStringsEqual(_sJobStartDate, otherData._sJobStartDate) &&
				GlobalParameters.areStringsEqual(_sPassword,     otherData._sPassword) ;
	}

	/**
	 * Determine whether this TraineeData is exactly similar to another object
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

		final TraineeData formData = (TraineeData) o ;

		return equals(formData) ;
	}
}
