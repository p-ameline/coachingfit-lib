package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Object that return information from a query for a list of trainees<br>
 * <br>
 * Created: 2 November 2019<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitJobsListResult implements Result 
{
	private List<String> _aJobs = new ArrayList<>() ;
	private String       _sMessage ;
	
	public GetCoachingFitJobsListResult()
	{
		super() ;
		
		_sMessage = "" ;
	}
	
	public GetCoachingFitJobsListResult(final String sMessage, final List<String> aJobs) 
	{
		_sMessage = (null == sMessage) ? "" : sMessage ;
		
		setJobs(aJobs) ;
	}

	public List<String> getJobs() {
		return _aJobs  ;
	}
	public void setJobs(final List<String> aJobs)
	{
		_aJobs.clear() ;
		
		if ((null == aJobs) || aJobs.isEmpty())
			return ;
		
		_aJobs.addAll(aJobs) ;
	}
	public void addJob(final String sJob)
	{
		if (null == sJob)
			return ;
		
		_aJobs.add(sJob) ;
	}
	
	public String getMessage() {
		return _sMessage ;
  }
}
