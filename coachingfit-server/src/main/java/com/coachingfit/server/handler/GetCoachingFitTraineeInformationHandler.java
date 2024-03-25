package com.coachingfit.server.handler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.GetCoachingFitTraineeAction;
import com.coachingfit.shared.rpc.GetCoachingFitTraineeResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Get trainee information from her ID 
 * 
 * @author Philippe
 */
public class GetCoachingFitTraineeInformationHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitTraineeAction, GetCoachingFitTraineeResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitTraineeInformationHandler(final Provider<ServletContext>     servletContext,
			                                           final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitTraineeResult execute(GetCoachingFitTraineeAction action, ExecutionContext context) throws ActionException 
	{
		// String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;
		
		int iUserId    = action.getUserId() ;
		int iTraineeId = action.getTraineeId() ;
		
		TraineeData traineeData = new TraineeData() ; 
		
		DBConnector dbConnector = new DBConnector(false) ;
		TraineeDataManager traineesManager = new TraineeDataManager(iUserId, dbConnector) ;
		  
		if (false == traineesManager.existData(iTraineeId, traineeData))
			return new GetCoachingFitTraineeResult("server error: trainee not found", null) ;
		
		return new GetCoachingFitTraineeResult("", traineeData) ;		
	}
			
	@Override
	public Class<GetCoachingFitTraineeAction> getActionType() {
		return GetCoachingFitTraineeAction.class;
	}

	@Override
	public void rollback(GetCoachingFitTraineeAction action, GetCoachingFitTraineeResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
