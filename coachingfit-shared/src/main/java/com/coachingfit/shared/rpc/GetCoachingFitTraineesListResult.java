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
public class GetCoachingFitTraineesListResult implements Result 
{
	private List<TraineeData> _aTraineesData = new ArrayList<TraineeData>() ;
	private String            _sMessage ;

	public GetCoachingFitTraineesListResult()
	{
		super() ;

		_sMessage = "" ;
	}

	public GetCoachingFitTraineesListResult(final String sMessage, final List<TraineeData> aTraineesData)
	{
		_sMessage = (null == sMessage) ? "" : sMessage ;

		setTraineesData(aTraineesData) ;
	}

	public List<TraineeData> getTraineesData() {
		return _aTraineesData  ;
	}
	public void setTraineesData(final List<TraineeData> aTraineesData)
	{
		_aTraineesData.clear() ;

		if ((null == aTraineesData) || aTraineesData.isEmpty())
			return ;

		for (TraineeData trainee : aTraineesData)
			_aTraineesData.add(new TraineeData(trainee)) ;
	}
	public void addTraineeData(final TraineeData traineeData)
	{
		if ((null == traineeData) || _aTraineesData.contains(traineeData))
			return ;

		_aTraineesData.add(new TraineeData(traineeData)) ;
	}

	public String getMessage() {
		return _sMessage ;
	}
	public void setMessage(final String sMessage) {
		_sMessage = (null == sMessage) ? "" : sMessage ;
	}
}
