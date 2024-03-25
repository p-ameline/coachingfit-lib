package com.coachingfit.client.mvp;

import java.util.logging.Level;
import java.util.logging.Logger ;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.CoachingFitLoginUserInfo;
import com.coachingfit.shared.rpc.CoachingFitLoginUserResult;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

import com.primege.client.event.GoToLoginResponseEvent;
import com.primege.client.event.LoginPageEvent;
import com.primege.client.event.LoginPageEventHandler;
import com.primege.client.event.LoginSentEvent;
import com.primege.client.event.LoginSentEventHandler;
import com.primege.shared.database.UserData;

public class CoachingFitLoginHeaderPresenter extends WidgetPresenter<CoachingFitLoginHeaderPresenter.Display>
{
	public interface Display extends WidgetDisplay 
	{	
		public String             getUser() ;
		public String             getPassWord() ;
		public FlexTable          getLoginTable() ;
		public void               showBadVersionMessage(final String sClientVersion, final String sServerVersion) ;

		public Button             getSendLogin() ;
		public HasKeyDownHandlers getPassKeyDown() ;

		public DialogBox          getErrDialogBox() ;
		public Button             getErrDialogBoxOkButton() ;
		public Button             getErrDialogBoxSendIdsButton() ;
		public void               showWaitCursor() ;
		public void               showDefaultCursor() ;
	}

	private final DispatchAsync         _dispatcher ;
	private final CoachingFitSupervisor _supervisor ;
	private       Logger                _logger = Logger.getLogger("") ;

	@Inject
	public CoachingFitLoginHeaderPresenter(final Display               display, 
			                               final EventBus              eventBus,
			                               final DispatchAsync         dispatcher,
			                               final CoachingFitSupervisor supervisor)
	{
		super(display, eventBus) ;

		_dispatcher = dispatcher ;
		_supervisor = supervisor ;

		bind() ;
	}

	@Override
	protected void onBind() 
	{
		_logger.info("Entering CoachingFitLoginHeaderPresenter::onBind()") ;

		eventBus.addHandler(LoginPageEvent.TYPE, new LoginPageEventHandler() {
			public void onLogin(LoginPageEvent event) 
			{
				_logger.info("Creating Login Label");
				event.getHeader().clear() ;
				FlowPanel Header = event.getHeader() ;
				Header.add(display.getLoginTable()) ;
				_logger.info("Creating Login Label success");
			}
		});

		display.getSendLogin().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event)
			{
				display.showWaitCursor() ;
				eventBus.fireEvent(new LoginSentEvent(display.getUser(),display.getPassWord())) ;
			}
		});

		/**
		 * Get key down from password Textbox and start searching when enter key is detected 
		 * */
		display.getPassKeyDown().addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) 
			{
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					display.showWaitCursor() ;
					eventBus.fireEvent(new LoginSentEvent(display.getUser(),display.getPassWord())) ;
				}
			}
		}) ;

		display.getErrDialogBoxOkButton().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event)
			{
				display.getErrDialogBox().hide() ;
			}
		});

		eventBus.addHandler(LoginSentEvent.TYPE, new LoginSentEventHandler() {
			public void onSendLogin(LoginSentEvent event) 
			{
				_logger.info("Sending User and PassWord") ;
				doSendLogin(event.getName(), event.getPassword()) ;
			}
		});
	}

	public void doSendLogin(String UserName, String PassWord) 
	{
		_logger.info("Calling doLogin") ;		
		_logger.log(Level.CONFIG, "doSend(): LoginPresenter") ;
		System.out.println("before dispatcher.execute()") ;

		_dispatcher.execute(new CoachingFitLoginUserInfo(UserName, PassWord), new LoginUserCallback()) ;

		//Window.alert("connect!");
		_logger.info("first!") ;
		System.out.println("after dispatcher.execute()") ;
	}

	@Override
	protected void onUnbind() {
		// Add unbind functionality here for more complex presenters.
	}

	public void refreshDisplay() {
		// This is called when the presenter should pull the latest data
		// from the server, etc. In this case.
	}

	public void revealDisplay() {
		// Nothing to do. This is more useful in UI which may be buried
		// in a tab bar, tree, etc.
	}

	public class LoginUserCallback implements AsyncCallback<CoachingFitLoginUserResult>
	{
		public LoginUserCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause);
			_logger.info("error!!");
			display.showDefaultCursor() ;
		}

		@Override
		public void onSuccess(CoachingFitLoginUserResult value) 
		{
			display.showDefaultCursor() ;

			// No user found, display an error message
			//
			UserData    userData = value.getUserData() ;
			TraineeData senior   = value.getTrainee() ;

			if (((null == userData) || userData.isEmpty()) && (null == senior))
			{
				display.getErrDialogBox().show() ;
				return ;
			}

			_logger.info("Congratulation!") ;	

			_supervisor.setCoachingFitUser(value.getUser()) ;
			_supervisor.setTraineeUser(senior) ;
			_supervisor.setServerVersion(value.getVersion()) ;

			_logger.info("Successfully logged! 1") ;	
			display.getLoginTable().setVisible(false) ;
			eventBus.fireEvent(new GoToLoginResponseEvent()) ;
			_logger.info("Successfully logged! 2") ;

			if (false == _supervisor.getClientVersion().equals(_supervisor.getServerVersion()))
				display.showBadVersionMessage(_supervisor.getClientVersion(), _supervisor.getServerVersion());
		}
	}

	protected void onPlaceRequest(final PlaceRequest request) {
		// this is a popup
	}

	@Override
	protected void onRevealDisplay()
	{
		// TODO Auto-generated method stub

	}	
}
