package com.coachingfit.shared.rpc;

import com.coachingfit.shared.database.TraineeData;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Object that return information from a query for a trainee's information<br>
 * <br>
 * Created: 16 May 2016<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitTraineeResult implements Result 
{
	private TraineeData _traineeData  ;
	private String      _sMessage ;
	
	public GetCoachingFitTraineeResult()
	{
		super() ;
		
		_traineeData = null ;
		_sMessage    = "" ;
	}
	
	public GetCoachingFitTraineeResult(final String sMessage, final TraineeData traineeData) 
	{
		_sMessage    = sMessage ;
		if (null != traineeData)
			_traineeData = new TraineeData(traineeData) ;
		else
			_traineeData = null ;
	}

	public TraineeData getTraineeData() {
		return _traineeData  ;
	}
	
	public String getMessage() {
  	return _sMessage ;
  }
}
