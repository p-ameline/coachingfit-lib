package com.coachingfit.shared.rpc;

import java.io.Serializable;

import com.coachingfit.shared.database.TraineeData;

import net.customware.gwt.dispatch.shared.Result;

public class RecordTraineeResult implements Result, Serializable
{
	private static final long serialVersionUID = 2954529744459409826L;
	
	private String      _sMessage ;
    private TraineeData _trainee ;

    public RecordTraineeResult()
    {
        super() ;
        
        _sMessage = "" ;
        _trainee  = null ;
    }

    public RecordTraineeResult(final String sMessage, TraineeData trainee)
    {
        super() ;

        _sMessage = sMessage ;
        _trainee  = trainee ;
    }

    public String getMessage() {
        return _sMessage ;
    }
    public void setMessage(final String sMessage) {
        _sMessage = (null == sMessage) ? "" : sMessage ;
    }

    public TraineeData getTrainee() {
        return _trainee ;
    }
    public void setTrainee(TraineeData trainee) {
    	_trainee = trainee ;
    }
}
