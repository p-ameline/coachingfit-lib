package com.coachingfit.shared.rpc;

import java.io.Serializable;
import java.util.List;

import com.coachingfit.shared.database.TraineeData;

import net.customware.gwt.dispatch.shared.Result;

public class GetTraineesListResult implements Result, Serializable
{
    private static final long serialVersionUID = 1112672197938519480L;
    
	private String            _sMessage ;
    private List<TraineeData> _aTrainees ;

    public GetTraineesListResult()
    {
        super() ;
        
        _sMessage  = "" ;
        _aTrainees = null ;
    }

    public GetTraineesListResult(final String sMessage, List<TraineeData> aTrainees)
    {
        super() ;

        _sMessage  = sMessage ;
        _aTrainees = aTrainees ;
    }

    public String getMessage() {
        return _sMessage ;
    }
    public void setMessage(final String sMessage) {
        _sMessage = (null == sMessage) ? "" : sMessage ;
    }

    public List<TraineeData> getTrainees() {
        return _aTrainees ;
    }
    public void setTrainees(List<TraineeData> aTrainees) {
        _aTrainees = aTrainees ;
    }
}
