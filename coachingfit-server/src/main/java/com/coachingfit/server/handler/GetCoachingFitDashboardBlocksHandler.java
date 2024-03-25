package com.coachingfit.server.handler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.GetCoachingFitDashboardBlocksInBase;
import com.coachingfit.shared.model.CoachingFitDashboardBlocks;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardBlocksAction;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardBlocksResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.shared.model.DashboardTable;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/** 
 * Object in charge of getting all forms for a given event that share a common city or a common date 
 *   
 */
public class GetCoachingFitDashboardBlocksHandler implements ActionHandler<GetCoachingFitDashboardBlocksAction, GetCoachingFitDashboardBlocksResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitDashboardBlocksHandler(final Provider<ServletContext>     servletContext,
			                                        final Provider<HttpServletRequest> servletRequest)
	{
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitDashboardBlocksResult execute(GetCoachingFitDashboardBlocksAction action, ExecutionContext context) throws ActionException 
	{
		GetCoachingFitDashboardBlocksResult result = new GetCoachingFitDashboardBlocksResult() ;
		
		int    iUserId       = action.getUserId() ;
		
		String sRoots        = action.getRoots() ;
		
		int    iCoachId      = action.getCoachId() ;
		int    iTraineeId    = action.getTraineeId() ;
		String sStartingDate = action.getStartingDate() ;
		String sEndingDate   = action.getEndingDate() ;
		
		// Initialize the CoachingFitDashboardBlocks
		//
		CoachingFitDashboardBlocks DashBlocks = result.getDashboardsBlocks() ;
		DashBlocks.setConstantCoachId(iCoachId) ;
		DashBlocks.setStartingDate(sStartingDate) ;
		DashBlocks.setEndingDate(sEndingDate) ;
		
		// The coach or the period of time can be non specified, but not both
		//
		if ((iCoachId <= 0) && ((null == sStartingDate) || "".equals(sStartingDate)) && ((null == sEndingDate) || "".equals(sEndingDate)))
			return result ;
		
		String[] aRoots = DashboardTable.getRoots(sRoots) ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		
		GetCoachingFitDashboardBlocksInBase dashboardBlocksManager = new GetCoachingFitDashboardBlocksInBase(iUserId, dbConnector) ;
		dashboardBlocksManager.GetDashboardBlocks(iCoachId, aRoots, iTraineeId, sStartingDate, sEndingDate, -1, DashBlocks) ;
		
		return result ;
	}
	
	@Override
	public Class<GetCoachingFitDashboardBlocksAction> getActionType() {
		return GetCoachingFitDashboardBlocksAction.class;
	}

	@Override
	public void rollback(GetCoachingFitDashboardBlocksAction action, GetCoachingFitDashboardBlocksResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
