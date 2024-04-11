package com.coachingfit.server.handler;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.rpc.GetCoachingFitJobsListAction;
import com.coachingfit.shared.rpc.GetCoachingFitJobsListResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetCoachingFitJobsListHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitJobsListAction, GetCoachingFitJobsListResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitJobsListHandler(final Provider<ServletContext>     servletContext,
			                                   final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitJobsListResult execute(GetCoachingFitJobsListAction action, ExecutionContext context) throws ActionException 
	{
		// String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;
		
		int iUserId              = action.getUserId() ;

		Logger.trace("GetCoachingFitJobsListHandler: looking for the list of jobs", iUserId, Logger.TraceLevel.STEP) ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		TraineeDataManager traineeManager = new TraineeDataManager(iUserId, dbConnector) ;
		
		GetCoachingFitJobsListResult result = new GetCoachingFitJobsListResult() ;
		
		// Get all jobs
		//
		List<String> aJobs = traineeManager.getJobsList() ;
		if (null == aJobs)
			return new GetCoachingFitJobsListResult("server error", null) ;
		
		result.setJobs(aJobs) ;
		
		return result ;
	}
			
	@Override
	public Class<GetCoachingFitJobsListAction> getActionType() {
		return GetCoachingFitJobsListAction.class;
	}

	@Override
	public void rollback(GetCoachingFitJobsListAction action, GetCoachingFitJobsListResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
