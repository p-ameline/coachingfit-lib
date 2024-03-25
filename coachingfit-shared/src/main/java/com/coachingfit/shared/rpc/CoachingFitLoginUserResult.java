package com.coachingfit.shared.rpc;

import java.util.List;

import com.coachingfit.shared.database.CoachingFitUserRoleData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;

import com.primege.shared.database.ArchetypeData;
import com.primege.shared.database.UserData;

import net.customware.gwt.dispatch.shared.Result;

public class CoachingFitLoginUserResult implements Result
{
	private CoachingFitUser _user = new CoachingFitUser() ;
	private TraineeData     _trainee ;
	private String          _sVersion ;
	
	/**
	 * */
	public CoachingFitLoginUserResult()
	{
		super() ;
		
		_trainee  = null ;
		_sVersion = "" ;
	}
		
	public CoachingFitUser getUser() {
		return _user ;
	}
	public void setUser(CoachingFitUser user) {
		_user.initFromUser(user) ;
	}
	
	public TraineeData getTrainee() {
		return _trainee ;
	}
	public void setTrainee(TraineeData trainee)
	{
		if (null == _trainee)
			_trainee = new TraineeData(trainee) ;
		else
			_trainee.initFromOther(trainee) ;
	}
	
	public String getVersion() {
		return _sVersion ;
	}
	public void setVersion(String sVersion) {
		_sVersion = sVersion ;
	}
	
	public void setUserData(UserData userData) {
		_user.setUserData(userData) ;
	}
	public UserData getUserData() {
		return _user.getUserData() ;
	}
	
	public List<CoachingFitUserRoleData> getRoles() {
		return _user.getRoles() ;
	}
	public void setRoles(List<CoachingFitUserRoleData> aRoles) {
		_user.setRoles(aRoles) ;
	}
	
	public List<ArchetypeData> getArchetypes() {
		return _user.getArchetypes() ;
	}
	public void setArchetypes(List<ArchetypeData> aArchetypes) {
		_user.setArchetypes(aArchetypes) ;
	}
	
	public List<TraineeData> getTrainees() {
		return _user.getTrainees() ;
	}
	public void setTrainees(List<TraineeData> aTrainees) {
		_user.setTrainees(aTrainees) ;
	}
	
	public List<TraineeData> getAllowedTrainees() {
		return _user.getAllowedTrainees() ;
	}
	public void setAllowedTrainees(List<TraineeData> aAllowedTrainees) {
		_user.setAllowedTrainees(aAllowedTrainees) ;
	}
}
