package com.coachingfit.shared.rpc;

import com.coachingfit.shared.database.TraineeData;

import net.customware.gwt.dispatch.shared.Action;

public class RecordTraineeAction implements Action<RecordTraineeResult> 
{	
	private int         _iUserId ;
    private TraineeData _trainee ;

    public RecordTraineeAction() 
    {
        super() ;

        _iUserId = -1 ;
        _trainee = null ;
    }

    public RecordTraineeAction(int iUserId, TraineeData trainee)
    {
        super() ;

        _iUserId = iUserId ;
        _trainee = trainee ;
    }

    public int getUserId() {
        return _iUserId ;
    }

    public TraineeData getTrainee() {
        return _trainee ;
    }
}
