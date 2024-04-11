package com.coachingfit.server.handler;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.RecordTraineeAction;
import com.coachingfit.shared.rpc.RecordTraineeResult;

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
public class RecordTraineeHandler extends GetFormsHandlerBase implements ActionHandler<RecordTraineeAction, RecordTraineeResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public RecordTraineeHandler(final Provider<ServletContext>     servletContext,
			                                     final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public RecordTraineeResult execute(RecordTraineeAction action, ExecutionContext context) throws ActionException 
	{
		// String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;
		
		int         iUserId = action.getUserId() ;
		TraineeData trainee = action.getTrainee() ;

		if (null == trainee)
			return new RecordTraineeResult("server error: empty query", null) ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		TraineeDataManager traineesManager = new TraineeDataManager(iUserId, dbConnector) ;
		
		int iTraineeId = trainee.getId() ;
		
		if (iTraineeId > 0)
		{
			Logger.trace("RecordTraineeHandler: updating trainee " + iTraineeId, iUserId, Logger.TraceLevel.STEP) ;
			if (false == traineesManager.updateData(trainee))
			{
				Logger.trace("RecordTraineeHandler: updating trainee " + iTraineeId + " failed.", iUserId, Logger.TraceLevel.ERROR) ;
				return new RecordTraineeResult("server error: update failed", null) ;
			}
		}
		else
		{
			Logger.trace("RecordTraineeHandler: creating trainee `" + trainee.getLabel() + "`.", iUserId, Logger.TraceLevel.STEP) ;
			if (false == traineesManager.insertData(trainee))
			{
				Logger.trace("RecordTraineeHandler: creating trainee `" + trainee.getLabel() + "` failed.", iUserId, Logger.TraceLevel.ERROR) ;
				return new RecordTraineeResult("server error: creation failed", null) ;
			}
		}
		
		return new RecordTraineeResult("", trainee) ;
	}
	
	/**
	 * Fill a list of {@link TraineeData} from a list of identifiers
	 * 
	 * @param aTraineesIds List of identifiers
	 * @param aTrainees    List of trainees to be filled
	 * @param iUserId      User identifier, for tracing purposes
	 * 
	 * @return <code>true</code> if all went well, <code>false</code> if not
	 */
	public static boolean getTraineesFromList(List<Integer> aTraineesIds, List<TraineeData> aTrainees, int iUserId)
	{
		if ((null == aTraineesIds) || (null == aTrainees))
			return false ;
		if (aTraineesIds.isEmpty())
			return true ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		TraineeDataManager traineesManager = new TraineeDataManager(iUserId, dbConnector) ;
		
		for (Integer iTraineeId : aTraineesIds)
		{
			TraineeData traineeData = new TraineeData() ;
		
			if (traineesManager.existData(iTraineeId, traineeData) && (false == aTrainees.contains(traineeData)))
				aTrainees.add(traineeData) ;
		}
		
		return true ;
	}
	
	@Override
	public Class<RecordTraineeAction> getActionType() {
		return RecordTraineeAction.class;
	}

	@Override
	public void rollback(RecordTraineeAction action, RecordTraineeResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
