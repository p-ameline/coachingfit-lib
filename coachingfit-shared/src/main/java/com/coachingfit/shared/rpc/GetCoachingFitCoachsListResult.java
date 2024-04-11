package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import com.primege.shared.database.UserData;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Object that return information from a query for a list of trainees<br>
 * <br>
 * Created: 2 November 2019<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitCoachsListResult implements Result 
{
	private List<UserData> _aCoachsData = new ArrayList<>() ;
	private String         _sMessage ;
	
	public GetCoachingFitCoachsListResult()
	{
		super() ;
		
		_sMessage = "" ;
	}
	
	public GetCoachingFitCoachsListResult(final String sMessage, final List<UserData> aCoachsData) 
	{
		_sMessage = sMessage ;
		
		setCoachsData(aCoachsData) ;
	}

	public List<UserData> getCoachsData() {
		return _aCoachsData  ;
	}
	public void setCoachsData(final List<UserData> aCoachsData)
	{
		_aCoachsData.clear() ;
		
		if ((null == aCoachsData) || aCoachsData.isEmpty())
			return ;
		
		for (UserData user : aCoachsData)
			_aCoachsData.add(new UserData(user)) ;
	}
	public void addCoachData(final UserData coachData)
	{
		if (null == coachData)
			return ;
		
		_aCoachsData.add(new UserData(coachData)) ;
	}
	
	public String getMessage() {
  	return _sMessage ;
  }
}
