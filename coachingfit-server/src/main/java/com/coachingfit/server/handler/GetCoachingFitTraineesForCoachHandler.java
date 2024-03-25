package com.coachingfit.server.handler;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.TraineeByCoachDataManager;
import com.coachingfit.server.model.TraineeByCoachGenericDataManager;
import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesForCoachAction;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesForCoachResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Get an array of trainees information from a list of trainees IDs 
 * 
 * @author Philippe
 */
public class GetCoachingFitTraineesForCoachHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitTraineesForCoachAction, GetCoachingFitTraineesForCoachResult> 
{	
	private final Provider<ServletContext>     _servletContext ;
	private final Provider<HttpServletRequest> _servletRequest ;
	
	private DBConnector _dbConnector ;
	private int         _iUserId ;
	
	@Inject
	public GetCoachingFitTraineesForCoachHandler(final Provider<ServletContext>     servletContext,
			                                     final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitTraineesForCoachResult execute(GetCoachingFitTraineesForCoachAction action, ExecutionContext context) throws ActionException 
	{
		// String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;
		
		_iUserId = action.getUserId() ;
		
		int iCoachId = action.getCoachId() ;

		Logger.trace("GetCoachingFitCoachsListHandler: looking for trainees for coach " + iCoachId, _iUserId, Logger.TraceLevel.STEP) ;
		
		_dbConnector = new DBConnector(false) ;
		
		GetCoachingFitTraineesForCoachResult result = new GetCoachingFitTraineesForCoachResult() ;
		
		if (false == action.mustOnlyIncludeGenericRules())
		{
			// First step, get all trainees whose this coach is the main coach
			//
			TraineeDataManager traineesManager = new TraineeDataManager(_iUserId, _dbConnector) ;
			traineesManager.fillTraineesForCoach(_iUserId, result.getTraineesData(), iCoachId) ;

			// Second step, get all trainees who have this coach as a secondary coach
			//
			getSecondaryCoachTrainees(iCoachId, result, traineesManager) ;
		}
		
		// Finally, if explicitly asked for, get all trainees that fit generic rules for this coach
		//
		if (action.mustOnlyIncludeGenericRules() || action.mustIncludeGenericRules())
		{
			TraineeByCoachGenericDataManager rulesManager = new TraineeByCoachGenericDataManager(_iUserId, _dbConnector) ;
			rulesManager.fillTraineesForCoach(result.getTraineesData(), iCoachId) ;
		}

		return result ;
	}
	
	/**
	 * Get all trainees who have this coach as a secondary coach
	 * 
	 * @param iCoachId        Coach's identifier
	 * @param result          Result to fill
	 * @param traineesManager Manager of the trainee table
	 */
	private void getSecondaryCoachTrainees(int iCoachId, GetCoachingFitTraineesForCoachResult result, TraineeDataManager traineesManager)
	{
		List<TraineeData> aTrainees = result.getTraineesData() ;
		
		TraineeByCoachDataManager traineesByCoachManager = new TraineeByCoachDataManager(_iUserId, _dbConnector) ;
		traineesByCoachManager.fillTraineesForCoach(aTrainees, iCoachId, null) ;
	}
	
	@Override
	public Class<GetCoachingFitTraineesForCoachAction> getActionType() {
		return GetCoachingFitTraineesForCoachAction.class;
	}

	@Override
	public void rollback(GetCoachingFitTraineesForCoachAction action, GetCoachingFitTraineesForCoachResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
