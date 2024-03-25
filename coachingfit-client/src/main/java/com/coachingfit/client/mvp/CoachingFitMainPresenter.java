package com.coachingfit.client.mvp;

import java.util.logging.Logger ;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.coachingfit.client.event.GotoAdminPageEvent;
import com.coachingfit.client.event.GotoAdminPageEventHandler;
import com.coachingfit.client.event.TraineesListEvent;
import com.coachingfit.client.gin.CoachingFitGinjector;
import com.coachingfit.client.global.CoachingFitSupervisor;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;

import com.primege.client.event.BackToWelcomePageEvent;
import com.primege.client.event.BackToWelcomePageEventHandler;
import com.primege.client.event.EditFormEvent;
import com.primege.client.event.GoToEditFormEvent;
import com.primege.client.event.GoToEditFormEventHandler;
import com.primege.client.event.GoToLoginResponseEvent;
import com.primege.client.event.GoToLoginResponseEventHandler;
import com.primege.client.event.GoToNewFormEvent;
import com.primege.client.event.GoToNewFormEventHandler;
import com.primege.client.event.GoToOpenDashboardEvent;
import com.primege.client.event.GoToOpenDashboardEventHandler;
import com.primege.client.event.LoginPageEvent;
import com.primege.client.event.LoginSuccessEvent;
import com.primege.client.event.OpenDashboardEvent;
import com.primege.client.event.PostLoginHeaderEvent;
import com.primege.client.event.WelcomePageEvent;
import com.primege.shared.database.FormLink;

public class CoachingFitMainPresenter extends WidgetPresenter<CoachingFitMainPresenter.Display> 
{
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	/*private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";
	 */
	public interface Display extends WidgetDisplay 
	{
		public FlowPanel getWorkspace() ;
		public FlowPanel getHeader() ;
		public FlowPanel getFooter() ; 
		//public VerticalPanel getBody();
	}

	private final CoachingFitSupervisor _supervisor ;
	private       Logger                _logger = Logger.getLogger("") ;

	private       boolean           _isGoogleChartsLoaded ;

	private       boolean          _isWelcomePageCreated ;
	private       boolean          _isLoginCreated ;
	private       boolean          _isPostLoginPageCreated ;
	private       boolean          _isPostLoginHeaderCreated ;
	private       boolean          _isNewFormPageCreated ;
	private       boolean          _isOpenDashboardPageCreated ;
	private       boolean          _isAdminPagePageCreated ;

	private       ScheduledCommand _pendingEvents ;

	@Inject
	public CoachingFitMainPresenter(final Display          display, 
			final EventBus         eventBus, 
			final DispatchAsync    dispatcher,
			final CoachingFitSupervisor supervisor) 
	{
		super(display, eventBus) ;

		_isGoogleChartsLoaded       = false ;

		_isLoginCreated             = false ;
		_isPostLoginPageCreated     = false ;
		_isPostLoginHeaderCreated   = false ;
		_isWelcomePageCreated       = false ;
		_isNewFormPageCreated       = false ;
		_isOpenDashboardPageCreated = false ;
		_isAdminPagePageCreated     = false ;

		_supervisor = supervisor ;

		bind() ;
	}

	@Override
	protected void onBind() 
	{
		eventBus.addHandler(BackToWelcomePageEvent.TYPE, new BackToWelcomePageEventHandler() {
			@Override
			public void onBackToWelcome(BackToWelcomePageEvent event) 
			{
				_logger.info("Back to welcome page") ;
				doLoad() ;	
			}
		});

		eventBus.addHandler(GoToLoginResponseEvent.TYPE, new GoToLoginResponseEventHandler() 
		{
			@Override
			public void onGoToLoginResponse(GoToLoginResponseEvent event) 
			{
				_logger.info("Call to go to post login page") ;
				goToPostLoginHeader() ;
				goToPostLoginPage() ;	
			}
		});

		eventBus.addHandler(GoToOpenDashboardEvent.TYPE, new GoToOpenDashboardEventHandler() 
		{
			@Override
			public void onGoToOpenDashboard(GoToOpenDashboardEvent event) 
			{
				_logger.info("Call to open a dashboard") ;
				goToOpenDashboard(event.getArchetypeId()) ;	
			}
		});

		eventBus.addHandler(GoToNewFormEvent.TYPE, new GoToNewFormEventHandler() 
		{
			@Override
			public void onGoToNewForm(GoToNewFormEvent event) 
			{
				_logger.info("Call to go to new form") ;
				goToNewForm(event.getArchetypeId(), event.getFormLink()) ;	
			}
		});

		eventBus.addHandler(GoToEditFormEvent.TYPE, new GoToEditFormEventHandler() 
		{
			@Override
			public void onGoToEditForm(GoToEditFormEvent event) 
			{
				_logger.info("Call to go to edit form") ;
				int iFormId = event.getFormId() ;
				goToEditForm(iFormId) ;	
			}
		});

		eventBus.addHandler(GotoAdminPageEvent.TYPE, new GotoAdminPageEventHandler() 
		{
			@Override
			public void onGotoAdminPage(GotoAdminPageEvent event) 
			{
				_logger.info("Call to go to administration page") ;
				goToAdminPage() ;
			}
		});
		
		doLoad() ;	
		doLogin() ;

		loadGoogleCharts() ;
	}

	public void doLoad()
	{
		_logger.info("Calling Load");
		if ((false == _isWelcomePageCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getWelcomePagePresenter() ; 
			_isWelcomePageCreated = true ;
		}
		display.getWorkspace().clear() ;

		// If LoginSuccessEvent is not handled yet, we have to defer fireEvent
		//
		if (false == eventBus.isEventHandled(WelcomePageEvent.TYPE))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new WelcomePageEvent(display.getWorkspace())) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
		}
		else
			eventBus.fireEvent(new WelcomePageEvent(display.getWorkspace())) ;		
	}

	public void goToPostLoginPage()
	{
		display.getWorkspace().clear() ;
		if ((false == _isPostLoginPageCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			_isPostLoginPageCreated = true ;
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getLoginResponsePresenter() ;
		}

		// If LoginSuccessEvent is not handled yet, we have to defer fireEvent
		//
		if (false == eventBus.isEventHandled(LoginSuccessEvent.TYPE))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new LoginSuccessEvent(display.getWorkspace())) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
			else
			{
				// Create a new timer that calls goToPostLoginPage() again later.
				Timer t = new Timer() {
					public void run() {
						goToPostLoginPage() ;
					}
				};
				// Schedule the timer to run once in 5 seconds.
				t.schedule(1000);
			}
		}
		else
			eventBus.fireEvent(new LoginSuccessEvent(display.getWorkspace())) ;	
	}

	public void goToNewUserParamsPage(String sId)
	{
		if ((null == sId) || sId.equals(""))
			return ;

		// validateNewUser(sId) ;
	}

	public void goToPostLoginHeader()
	{
		_logger.info("Switch to post-login header") ;
		display.getHeader().clear() ;
		if ((false == _isPostLoginHeaderCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getPostLoginHeaderPresenter() ;
			_isPostLoginHeaderCreated = true ;
		}

		// If UserParamsSentEvent is not handled yet, we have to defer fireEvent
		//
		if (false == eventBus.isEventHandled(PostLoginHeaderEvent.TYPE))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new PostLoginHeaderEvent(display.getHeader())) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
			else
			{
				// Create a new timer that calls goToPostLoginHeader() again later.
				Timer t = new Timer() {
					public void run() {
						goToPostLoginHeader() ;
					}
				};
				// Schedule the timer to run once in 5 seconds.
				t.schedule(1000);
			}
		}
		else
			eventBus.fireEvent(new PostLoginHeaderEvent(display.getHeader())) ;	
	}

	/**
	 * New encounter
	 * */
	public void goToNewForm(final int iArchetypeId, final FormLink formLink)
	{
		_logger.info("Going to new form page") ;

		display.getWorkspace().clear() ;

		if ((false == _isNewFormPageCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getFormPresenter() ;
			_isNewFormPageCreated = true ;
		}

		// If UserParamsSentEvent is not handled yet, we have to defer fireEvent
		//
		if (false == eventBus.isEventHandled(EditFormEvent.TYPE))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new EditFormEvent(display.getWorkspace(), -1, iArchetypeId, formLink)) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
			else
			{
				// Create a new timer that calls goToNewEncounter() again later.
				Timer t = new Timer() {
					public void run() {
						goToNewForm(iArchetypeId, formLink) ;
					}
				};
				// Schedule the timer to run once in 5 seconds.
				t.schedule(1000);
			}
		}
		else
			eventBus.fireEvent(new EditFormEvent(display.getWorkspace(), -1, iArchetypeId, formLink)) ;	
	}

	/**
	 * Edit existing encounter
	 * 
	 * @param iPatientMessageId : Id of patient message to be modified
	 * 
	 * */
	public void goToEditForm(final int iFormId)
	{
		if (-1 == iFormId)
			return ;

		_logger.info("Going to edit encounter page") ;

		display.getWorkspace().clear() ;

		if ((false == _isNewFormPageCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getFormPresenter() ;
			_isNewFormPageCreated = true ;
		}

		// If UserParamsSentEvent is not handled yet, we have to defer fireEvent
		//
		if (false == eventBus.isEventHandled(EditFormEvent.TYPE))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new EditFormEvent(display.getWorkspace(), iFormId, -1, null)) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
		}
		else
			eventBus.fireEvent(new EditFormEvent(display.getWorkspace(), iFormId, -1, null)) ;
	}
	
	/**
	 * Open dashboard
	 * */
	public void goToOpenDashboard(final int iArchetypeId)
	{
		_logger.info("Going to dashboard page") ;

		display.getWorkspace().clear() ;

		if ((false == _isOpenDashboardPageCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getDashboardPresenter() ;
			_isOpenDashboardPageCreated = true ;
		}

		// If Google Chart API not loaded or OpenDashboardEvent not handled yet, we have to defer fireEvent
		//
		if ((false == _isGoogleChartsLoaded) || (false == eventBus.isEventHandled(OpenDashboardEvent.TYPE)))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new OpenDashboardEvent(display.getWorkspace(), iArchetypeId)) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
			else
			{
				// Create a new timer that calls goToNewEncounter() again later.
				Timer t = new Timer() {
					public void run() {
						goToOpenDashboard(iArchetypeId) ;
					}
				};
				// Schedule the timer to run once in 5 seconds.
				t.schedule(1000);
			}
		}
		else
			eventBus.fireEvent(new OpenDashboardEvent(display.getWorkspace(), iArchetypeId)) ;	
	}

	public void doLogin() 
	{
		_logger.info("Calling doLogin") ;
		if ((false == _isLoginCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getLoginPresenter() ;
			_isLoginCreated = true ;
		}

		// If UserParamsSentEvent is not handled yet, we have to defer fireEvent
		//
		if (false == eventBus.isEventHandled(LoginPageEvent.TYPE))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new LoginPageEvent(display.getHeader())) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
		}
		else
			eventBus.fireEvent(new LoginPageEvent(display.getHeader())) ;

		if (false == eventBus.isEventHandled(LoginPageEvent.TYPE))
			_logger.info("Error in eventBus") ;
	}

	/**
	 * Open the administration page
	 * 
	 * */
	public void goToAdminPage()
	{
		_logger.info("Going to administration page") ;

		display.getWorkspace().clear() ;

		if ((false == _isAdminPagePageCreated) && (null != _supervisor) && (null != _supervisor.getInjector()))
		{
			CoachingFitGinjector injector = _supervisor.getInjector() ;
			injector.getAdminPagePresenter() ;
			_isAdminPagePageCreated = true ;
		}

		// If UserParamsSentEvent is not handled yet, we have to defer fireEvent
		//
		if (false == eventBus.isEventHandled(TraineesListEvent.TYPE))
		{
			if (null == _pendingEvents) 
			{
				_pendingEvents = new ScheduledCommand() 
				{
					public void execute() {
						_pendingEvents = null ;
						eventBus.fireEvent(new TraineesListEvent(display.getWorkspace())) ;
					}
				};
				Scheduler.get().scheduleDeferred(_pendingEvents) ;
			}
		}
		else
			eventBus.fireEvent(new TraineesListEvent(display.getWorkspace())) ;
	}
	
	/**
	 * Load the Google Charts library
	 */
	protected void loadGoogleCharts()
	{
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART, ChartPackage.TIMELINE) ;
		chartLoader.loadApi(new Runnable() {
			public void run() {
				_isGoogleChartsLoaded = true ;
				_logger.info("Google Charts loaded.") ;
			} 
		});
	}

	@Override
	protected void onUnbind() {
	}

	public void refreshDisplay() {	
	}

	public void revealDisplay() {
	}

	protected void onPlaceRequest(final PlaceRequest request) {	
	}

	@Override
	protected void onRevealDisplay()
	{
		// TODO Auto-generated method stub
	}
}
