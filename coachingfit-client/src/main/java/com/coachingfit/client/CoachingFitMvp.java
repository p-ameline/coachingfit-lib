package com.coachingfit.client;

import java.util.logging.Logger ;

import com.coachingfit.client.gin.CoachingFitGinjector;
import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.mvp.CoachingFitAppPresenter;
import com.coachingfit.client.ui.CoachingFitResources;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class CoachingFitMvp implements EntryPoint 
{
	private final CoachingFitGinjector _injector = GWT.create(CoachingFitGinjector.class) ;
	private       Logger               _logger   = Logger.getLogger("") ;

	public void onModuleLoad() 
	{
		_logger.info("onModuleLoad") ;

		CoachingFitResources.INSTANCE.css().ensureInjected() ;

		final CoachingFitSupervisor supervisor = _injector.getPrimegeSupervisor() ;

		supervisor.setInjector(_injector) ;
		final CoachingFitAppPresenter appPresenter = _injector.getAppPresenter() ;

		String sId   = "" ;
		String sStep = Window.Location.getParameter("step") ;
		if (null != sStep)
		{
			_logger.info("step parameter detected") ;
			if (sStep.equals("creation"))
			{
				_logger.info("switching to creation mode") ;
				sId = Window.Location.getParameter("pid") ;
			}
		}
		else
			_logger.info("step parameter not detected") ;

		appPresenter.go(RootPanel.get(), sStep, sId) ;
	}
}
