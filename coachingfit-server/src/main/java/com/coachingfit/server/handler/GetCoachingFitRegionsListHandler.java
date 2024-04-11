package com.coachingfit.server.handler;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.RegionDataManager;
import com.coachingfit.shared.database.RegionData;
import com.coachingfit.shared.rpc.GetCoachingFitRegionsListAction;
import com.coachingfit.shared.rpc.GetCoachingFitRegionsListResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetCoachingFitRegionsListHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitRegionsListAction, GetCoachingFitRegionsListResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitRegionsListHandler(final Provider<ServletContext>     servletContext,
			                                final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitRegionsListResult execute(GetCoachingFitRegionsListAction action, ExecutionContext context) throws ActionException 
	{
		// String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;
		
		int iUserId               = action.getUserId() ;
		List<Integer> aRegionsIds = action.getRegionsIds() ;

		Logger.trace("GetCoachingFitRegionsListHandler: looking for " + aRegionsIds.size() + " coach(s)", iUserId, Logger.TraceLevel.STEP) ;
		
		if (((null == aRegionsIds) || aRegionsIds.isEmpty()) && (false == action.getAllActive()))
			return new GetCoachingFitRegionsListResult("server error: empty query", null) ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		RegionDataManager regionManager = new RegionDataManager(iUserId, dbConnector) ;
		
		GetCoachingFitRegionsListResult result = new GetCoachingFitRegionsListResult() ;
		
		// Get users from a list of identifiers
		//
		if ((null != aRegionsIds) && (false == aRegionsIds.isEmpty()))
		{
			for (Integer iCoachId : aRegionsIds)
			{
				RegionData regionData = new RegionData() ;
		
				if (regionManager.existData(iCoachId, regionData))
					result.addRegionData(regionData) ;
			}
				
			return result ;
		}
		
		// Get all active users
		//
		List<RegionData> allRegions = new ArrayList<>() ;
		if (false == regionManager.getThemAll(allRegions))
			return new GetCoachingFitRegionsListResult("server error", null) ;
		
		for (RegionData region : allRegions)
			result.addRegionData(region) ;
		
		return result ;
	}
			
	@Override
	public Class<GetCoachingFitRegionsListAction> getActionType() {
		return GetCoachingFitRegionsListAction.class;
	}

	@Override
	public void rollback(GetCoachingFitRegionsListAction action, GetCoachingFitRegionsListResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
