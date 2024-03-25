package com.coachingfit.shared.database ;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable ;
import com.primege.shared.GlobalParameters;

/**
 * An ArchetypeForJobData object represents the kind of trainee job type an archetype applies to
 * 
 * Created: 19 Mar 2017
 * Author: PA
 * 
 */
public class ArchetypeForJobData implements IsSerializable 
{
	private int    _iId ;

	private int    _iArchetypeId ;
	private String _sJobType ;

	/**
	 * Default constructor (with zero information)
	 */
	public ArchetypeForJobData() {
		reset() ;
	}

	/**
	 * Plain vanilla constructor 
	 */
	public ArchetypeForJobData(final int iID, final int iArchetypeId, final String sJobType) 
	{
		_iId          = iID ;

		setJobType(sJobType) ;
		_iArchetypeId = iArchetypeId ;
	}

	/**
	 * Copy constructor
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public ArchetypeForJobData(final ArchetypeForJobData model) 
	{
		reset() ;

		initFromModelData(model) ;
	}

	/**
	 * Initialize all information from another TraineeData
	 * 
	 * @param model TraineeData to initialize from 
	 */
	public void initFromModelData(final ArchetypeForJobData model)
	{
		reset() ;

		if (null == model)
			return ;

		_iId          = model._iId ;
		_sJobType     = model._sJobType ;
		_iArchetypeId = model._iArchetypeId ;
	}

	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		_iId          = -1 ;
		_sJobType     = "" ;
		_iArchetypeId = -1 ;
	}

	/**
	 * From a global list of trainees, get the ones that fit with this archetype
	 * 
	 * @param aTrainees Global list of trainees to find valid trainees from
	 * 
	 * @return The list of valid trainees
	 */
	public List<TraineeData> getValidTrainees(List<TraineeData> aTrainees)
	{
		List<TraineeData> aSelectedTrainees = new ArrayList<TraineeData>() ;

		// If job type contains a '*', it means that it is to be taken as a model
		//
		int iJoker = _sJobType.indexOf('*') ;

		// No Joker, add trainees with exact match
		//
		if (-1 == iJoker)
		{
			for (TraineeData trainee : aTrainees)
				if (_sJobType.equals(trainee.getJobType()))
					aSelectedTrainees.add(trainee) ;

			return aSelectedTrainees ;
		}

		// If there is a Joker, get trainees whose job starts with the model
		//
		String sModel = "" ;
		if (iJoker > 0)
			sModel = _sJobType.substring(0, iJoker) ;

		for (TraineeData trainee : aTrainees)
			if ((trainee.getJobType().length() >= iJoker) && (trainee.getJobType().substring(0, iJoker).equals(sModel)))
				aSelectedTrainees.add(trainee) ;

		return aSelectedTrainees ;
	}

	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all data are zeros, false if not
	 */
	public boolean isEmpty()
	{
		if ((-1 == _iId)      &&
				(-1 == _iArchetypeId) &&
				("".equals(_sJobType)))
			return true ;

		return false ;
	}

	public int getId() {
		return _iId ;
	}
	public void setId(int iId) {
		_iId = iId ;
	}

	public int getArchetypeId() {
		return _iArchetypeId ;
	}
	public void setArchetypeId(int iArchetypeId) {
		_iArchetypeId = iArchetypeId ;
	}

	public String getJobType() {
		return _sJobType ;
	}
	public void setJobType(final String sJobType) {
		_sJobType = ((null == sJobType) ? "" : sJobType).trim() ;
	}

	/**
	 * Determine whether two ArchetypeForJobData are exactly similar
	 * 
	 * @return true if all data are the same, false if not
	 * @param  otherData ArchetypeForJobData to compare with
	 * 
	 */
	public boolean equals(final ArchetypeForJobData otherData)
	{
		if (this == otherData) {
			return true ;
		}
		if (null == otherData) {
			return false ;
		}

		return (_iId          == otherData._iId)          &&
				(_iArchetypeId == otherData._iArchetypeId) &&
				GlobalParameters.areStringsEqual(_sJobType, otherData._sJobType) ;
	}

	/**
	 * Determine whether this ArchetypeForJobData is exactly similar to another object
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

		final ArchetypeForJobData formData = (ArchetypeForJobData) o ;

		return equals(formData) ;
	}
}
