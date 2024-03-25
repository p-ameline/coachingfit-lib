package com.coachingfit.server.handler;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.ArchetypeForJobDataManager;
import com.coachingfit.shared.database.ArchetypeForJobData;
import com.coachingfit.shared.rpc.GetCoachingFitJobs4ArchetypeAction;
import com.coachingfit.shared.rpc.GetCoachingFitJobs4ArchetypeResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetCoachingFitJobsForArchetypeHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitJobs4ArchetypeAction, GetCoachingFitJobs4ArchetypeResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;

	@Inject
	public GetCoachingFitJobsForArchetypeHandler(final Provider<ServletContext>     servletContext,
												 final Provider<HttpServletRequest> servletRequest)
	{
		super() ;

		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitJobs4ArchetypeResult execute(GetCoachingFitJobs4ArchetypeAction action, ExecutionContext context) throws ActionException 
	{
		String sFctName = "GetCoachingFitJobsForArchetypeHandler.execute" ;

		int iUserId      = action.getUserId() ;
		int iArchetypeId = action.getArchetypeId() ;

		DBConnector dbConnector = new DBConnector(false) ;
		ArchetypeForJobDataManager Archetype4JobsManager = new ArchetypeForJobDataManager(iUserId, dbConnector) ;

		List<ArchetypeForJobData> aAFJ = new ArrayList<ArchetypeForJobData>() ;

		Archetype4JobsManager.fillDataForArchetype(iUserId, aAFJ, iArchetypeId) ;

		GetCoachingFitJobs4ArchetypeResult result = new GetCoachingFitJobs4ArchetypeResult() ;

		if (aAFJ.isEmpty())
		{
			Logger.trace(sFctName + ": no archetypeForJob entry found for archetype " + iArchetypeId, iUserId, Logger.TraceLevel.WARNING) ;
			return result ;
		}

		String sList = "" ;
		for (ArchetypeForJobData afj : aAFJ)
		{
			if (false == sList.isEmpty())
				sList += ", " ;
			sList += afj.getJobType() ;
		}

		Logger.trace(sFctName + ": found " + aAFJ.size() + " archetypeForJob entries for archetype " + iArchetypeId + " (" + sList + ").", iUserId, Logger.TraceLevel.STEP) ;

		result.initFromJobsTypes(aAFJ) ;

		return result ;		
	}

	@Override
	public Class<GetCoachingFitJobs4ArchetypeAction> getActionType() {
		return GetCoachingFitJobs4ArchetypeAction.class;
	}

	@Override
	public void rollback(GetCoachingFitJobs4ArchetypeAction action, GetCoachingFitJobs4ArchetypeResult result, ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
