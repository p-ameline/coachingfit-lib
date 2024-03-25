package com.coachingfit.client.mvp;

import java.util.logging.Logger ;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class CoachingFitAppPresenter 
{
	private HasWidgets               _container ;
	private CoachingFitMainPresenter _mainPresenter ;
	private Logger                   _logger = Logger.getLogger("") ;
	
	@Inject
	public CoachingFitAppPresenter(final DispatchAsync dispatcher, final CoachingFitMainPresenter mainPresenter) 
	{
		_mainPresenter = mainPresenter ;		
	}
	
	private void showMain() 
	{
		_container.clear() ;
		_container.add(_mainPresenter.getDisplay().asWidget()) ;
	}
		
	public void go(final HasWidgets container, String sStep, String sId) 
	{
		_container = container ;	
		
		showMain() ;
		
		// For testing only
		// sStep = "creation" ;
		// sId   = "reg0" ;
		// End For testing only
		
		if ((null != sStep) && (sStep.equals("creation")))
		{
			_logger.info("Creation phase") ;
			_mainPresenter.goToNewUserParamsPage(sId) ;
		}
	}
}
