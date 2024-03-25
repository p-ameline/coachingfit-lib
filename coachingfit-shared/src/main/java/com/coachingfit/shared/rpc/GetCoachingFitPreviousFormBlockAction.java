package com.coachingfit.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

public class GetCoachingFitPreviousFormBlockAction implements Action<GetCoachingFitPreviousFormBlockResult> 
{	
	private int _iUserId ;
	
	private int _iTraineeId ;
	private int _iArchetypeId ;
	
	public GetCoachingFitPreviousFormBlockAction() 
	{
		super() ;
		
		_iUserId      = -1 ;
		_iTraineeId   = -1 ;
		_iArchetypeId = -1 ;
	}
	
	public GetCoachingFitPreviousFormBlockAction(int iUserId, int iTraineeId, int iArchetypeId) 
	{
		_iUserId      = iUserId ;
		_iTraineeId   = iTraineeId ;
		_iArchetypeId = iArchetypeId ;
	}

	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public int getTraineeId() {
		return _iTraineeId ;
	}
	public void setTraineeId(int iTraineeId) {
		_iTraineeId = iTraineeId ;
	}
	
	public int getArchetypeId() {
		return _iArchetypeId ;
	}
	public void setArchetypeId(int iArchetypeId) {
		_iArchetypeId = iArchetypeId ;
	}
}
