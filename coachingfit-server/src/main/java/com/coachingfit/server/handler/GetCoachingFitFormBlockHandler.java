package com.coachingfit.server.handler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.GetCoachingFitFormInBase;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.shared.rpc.GetFormBlockAction;
import com.primege.shared.rpc.GetFormBlockResult;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetCoachingFitFormBlockHandler implements ActionHandler<GetFormBlockAction, GetFormBlockResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitFormBlockHandler(final Provider<ServletContext> servletContext,
			                                  final Provider<HttpServletRequest> servletRequest)
	{
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetFormBlockResult execute(GetFormBlockAction action, ExecutionContext context) throws ActionException 
	{
		GetFormBlockResult result = new GetFormBlockResult() ;
		
		int iUserId = action.getUserId() ;
		int iFormId = action.getFormId() ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		
		GetCoachingFitFormInBase formGetter = new GetCoachingFitFormInBase(iUserId, dbConnector) ;
		formGetter.GetForm(iFormId, result.getFormBlock()) ;
				
		return result ;
	}
		
	@Override
	public Class<GetFormBlockAction> getActionType() {
		return GetFormBlockAction.class;
	}

	@Override
	public void rollback(GetFormBlockAction action, GetFormBlockResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
