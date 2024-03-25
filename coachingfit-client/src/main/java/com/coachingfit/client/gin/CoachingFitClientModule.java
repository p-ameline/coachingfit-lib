package com.coachingfit.client.gin;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.mvp.CoachingFitAppPresenter;
import com.coachingfit.client.mvp.CoachingFitDashboardPresenter;
import com.coachingfit.client.mvp.CoachingFitDashboardView;
import com.coachingfit.client.mvp.CoachingFitFormPresenter;
import com.coachingfit.client.mvp.CoachingFitFormView;
import com.coachingfit.client.mvp.CoachingFitLoginHeaderPresenter;
import com.coachingfit.client.mvp.CoachingFitLoginHeaderView;
import com.coachingfit.client.mvp.CoachingFitLoginResponsePresenter;
import com.coachingfit.client.mvp.CoachingFitLoginResponseView;
import com.coachingfit.client.mvp.CoachingFitPostLoginHeaderPresenter;
import com.coachingfit.client.mvp.CoachingFitPostLoginHeaderView;
import com.coachingfit.client.mvp.CoachingFitTraineesListPresenter;
import com.coachingfit.client.mvp.CoachingFitTraineesListView;
import com.coachingfit.client.mvp.CoachingFitWelcomePagePresenter;
import com.coachingfit.client.mvp.CoachingFitWelcomePageView;
import com.coachingfit.client.mvp.CoachingFitMainPresenter;
import com.coachingfit.client.mvp.CoachingFitMainView;

import com.google.inject.Singleton;

import com.primege.client.CachingDispatchAsync;

public class CoachingFitClientModule extends AbstractPresenterModule 
{
	@Override
	protected void configure() 
	{		
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class) ;
		
		bindPresenter(CoachingFitMainPresenter.class,            CoachingFitMainPresenter.Display.class,            CoachingFitMainView.class) ;		
		bindPresenter(CoachingFitWelcomePagePresenter.class,     CoachingFitWelcomePagePresenter.Display.class,     CoachingFitWelcomePageView.class) ;
		bindPresenter(CoachingFitLoginHeaderPresenter.class,     CoachingFitLoginHeaderPresenter.Display.class,     CoachingFitLoginHeaderView.class) ;
		bindPresenter(CoachingFitPostLoginHeaderPresenter.class, CoachingFitPostLoginHeaderPresenter.Display.class, CoachingFitPostLoginHeaderView.class) ;
		bindPresenter(CoachingFitLoginResponsePresenter.class,   CoachingFitLoginResponsePresenter.Display.class,   CoachingFitLoginResponseView.class) ;
		bindPresenter(CoachingFitFormPresenter.class,            CoachingFitFormPresenter.Display.class,            CoachingFitFormView.class) ;
		bindPresenter(CoachingFitDashboardPresenter.class,       CoachingFitDashboardPresenter.Display.class,       CoachingFitDashboardView.class) ;
		bindPresenter(CoachingFitTraineesListPresenter.class,    CoachingFitTraineesListPresenter.Display.class,    CoachingFitTraineesListView.class) ;
		
		bind(CoachingFitAppPresenter.class).in(Singleton.class) ;
		bind(CoachingFitSupervisor.class).in(Singleton.class) ;
		bind(CachingDispatchAsync.class) ;		
	}
}
