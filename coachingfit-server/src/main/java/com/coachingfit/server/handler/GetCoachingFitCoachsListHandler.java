package com.coachingfit.server.handler;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.shared.rpc.GetCoachingFitCoachsListAction;
import com.coachingfit.shared.rpc.GetCoachingFitCoachsListResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;
import com.primege.server.model.UserManager;
import com.primege.shared.database.UserData;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetCoachingFitCoachsListHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitCoachsListAction, GetCoachingFitCoachsListResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitCoachsListHandler(final Provider<ServletContext>     servletContext,
			                                   final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitCoachsListResult execute(GetCoachingFitCoachsListAction action, ExecutionContext context) throws ActionException 
	{
		// String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;
		
		int iUserId              = action.getUserId() ;
		List<Integer> aCoachsIds = action.getCoachsIds() ;

		Logger.trace("GetCoachingFitCoachsListHandler: looking for " + aCoachsIds.size() + " coach(es)", iUserId, Logger.TraceLevel.STEP) ;
		
		if (((null == aCoachsIds) || aCoachsIds.isEmpty()) && (false == action.getAllActive()))
			return new GetCoachingFitCoachsListResult("server error: empty query", null) ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		UserManager userManager = new UserManager(iUserId, dbConnector) ;
		
		GetCoachingFitCoachsListResult result = new GetCoachingFitCoachsListResult() ;
		
		// Get users from a list of identifiers
		//
		if ((null != aCoachsIds) && (false == aCoachsIds.isEmpty()))
		{
			for (Integer iCoachId : aCoachsIds)
			{
				UserData coachData = new UserData() ;
		
				if (userManager.existUser(iCoachId, coachData))
					result.addCoachData(coachData) ;
			}
				
			return result ;
		}
		
		// Get all active users
		//
		List<UserData> allUsers = new ArrayList<>() ;
		if (false == userManager.getThemAll(allUsers))
			return new GetCoachingFitCoachsListResult("server error", null) ;
		
		for (UserData user : allUsers)
			if (false == user.getPassword().isEmpty())
				result.addCoachData(user) ;
		
		return result ;
	}
			
	@Override
	public Class<GetCoachingFitCoachsListAction> getActionType() {
		return GetCoachingFitCoachsListAction.class;
	}

	@Override
	public void rollback(GetCoachingFitCoachsListAction action, GetCoachingFitCoachsListResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
