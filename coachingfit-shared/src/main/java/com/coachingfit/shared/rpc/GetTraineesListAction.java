package com.coachingfit.shared.rpc;

import java.util.List ;

import com.coachingfit.shared.rpc_util.TraineesSearchTrait;

import net.customware.gwt.dispatch.shared.Action;

public class GetTraineesListAction implements Action<GetTraineesListResult> 
{	
	private int                 _iUserId ;
    private TraineesSearchTrait _traits ;

    public GetTraineesListAction() 
    {
        super() ;

        _iUserId = -1 ;
        _traits = null ;
    }

    public GetTraineesListAction(int iUserId, TraineesSearchTrait traits)
    {
        super() ;

        _iUserId = iUserId ;
        _traits = traits ;
    }

    public int getUserId() {
        return _iUserId ;
    }

    public TraineesSearchTrait getSearchTraits() {
        return _traits ;
    }
}
