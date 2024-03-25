package com.coachingfit.server.handler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.primege.server.handler.DeleteFormBlockHandlerBase;
import com.primege.shared.rpc.DeleteFormAction;
import com.primege.shared.rpc.DeleteFormResult;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class CoachingFitDeleteFormBlockHandler extends DeleteFormBlockHandlerBase implements ActionHandler<DeleteFormAction, DeleteFormResult> 
{	
	@Inject
	public CoachingFitDeleteFormBlockHandler(final Provider<ServletContext>     servletContext,
                                           final Provider<HttpServletRequest> servletRequest)
	{
		super(servletContext, servletRequest) ;
	}
	
	@Override
	public DeleteFormResult execute(DeleteFormAction action, ExecutionContext context) throws ActionException 
	{
		return deleteForm(action, context) ;
	}
			
	@Override
	public Class<DeleteFormAction> getActionType() {
		return DeleteFormAction.class;
	}

	@Override
	public void rollback(DeleteFormAction action, DeleteFormResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
