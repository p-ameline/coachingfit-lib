package com.coachingfit.client.gin;

import net.customware.gwt.dispatch.client.gin.StandardDispatchModule;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.mvp.CoachingFitAppPresenter;
import com.coachingfit.client.mvp.CoachingFitDashboardPresenter;
import com.coachingfit.client.mvp.CoachingFitFormPresenter;
import com.coachingfit.client.mvp.CoachingFitLoginHeaderPresenter;
import com.coachingfit.client.mvp.CoachingFitLoginResponsePresenter;
import com.coachingfit.client.mvp.CoachingFitPostLoginHeaderPresenter;
import com.coachingfit.client.mvp.CoachingFitTraineesListPresenter;
import com.coachingfit.client.mvp.CoachingFitWelcomePagePresenter;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules({ StandardDispatchModule.class, CoachingFitClientModule.class })
public interface CoachingFitGinjector extends Ginjector 
{
	CoachingFitSupervisor               getPrimegeSupervisor() ;

	CoachingFitAppPresenter             getAppPresenter() ;

	CoachingFitLoginHeaderPresenter     getLoginPresenter() ;	
	CoachingFitLoginResponsePresenter   getLoginResponsePresenter() ;
	CoachingFitPostLoginHeaderPresenter getPostLoginHeaderPresenter() ;
	CoachingFitFormPresenter            getFormPresenter() ;
	CoachingFitDashboardPresenter       getDashboardPresenter() ;
	CoachingFitWelcomePagePresenter     getWelcomePagePresenter() ;	
	CoachingFitTraineesListPresenter    getAdminPagePresenter() ;
}
