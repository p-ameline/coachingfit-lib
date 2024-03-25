package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import com.coachingfit.shared.database.TraineeData;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Object that return information from a query for a list of trainees<br>
 * <br>
 * Created: 2 November 2019<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitTraineesForCoachResult implements Result 
{
	private List<TraineeData> _aTraineesData = new ArrayList<TraineeData>() ;
	private String            _sMessage ;

	public GetCoachingFitTraineesForCoachResult()
	{
		super() ;

		_sMessage = "" ;
	}

	public GetCoachingFitTraineesForCoachResult(final String sMessage, final ArrayList<TraineeData> aTraineesData) 
	{
		_sMessage = sMessage ;

		setTraineesData(aTraineesData) ;
	}

	public List<TraineeData> getTraineesData() {
		return _aTraineesData  ;
	}
	public void setTraineesData(final ArrayList<TraineeData> aTraineesData)
	{
		_aTraineesData.clear() ;

		if ((null == aTraineesData) || aTraineesData.isEmpty())
			return ;

		for (TraineeData trainee : aTraineesData)
			_aTraineesData.add(new TraineeData(trainee)) ;
	}
	public void addTraineeData(final TraineeData traineeData)
	{
		if (null == traineeData)
			return ;

		if (false == _aTraineesData.contains(traineeData))
			_aTraineesData.add(new TraineeData(traineeData)) ;
	}
	
	/**
	 * Does a trainee exist in the list?
	 * @param iTraineeId Identifier of trainee to check the existence of
	 * @return <code>true</code> if a trainee by this identifier exists in the list, <code>false</code> if not
	 */
	public boolean existTrainee(int iTraineeId)
	{
		for (TraineeData trainee : _aTraineesData)
			if (trainee.getId() == iTraineeId)
				return true ;
		
		return false ;
	}

	public String getMessage() {
		return _sMessage ;
	}
}
