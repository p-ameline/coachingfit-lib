package com.coachingfit.client.mvp;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.coachingfit.client.global.CoachingFitSupervisor;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

import com.primege.client.event.PostLoginHeaderDisplayEvent;
import com.primege.client.event.PostLoginHeaderDisplayEventHandler;
import com.primege.client.event.PostLoginHeaderEvent;
import com.primege.client.event.PostLoginHeaderEventHandler;

public class CoachingFitPostLoginHeaderPresenter extends WidgetPresenter<CoachingFitPostLoginHeaderPresenter.Display> 
{	
	public interface Display extends WidgetDisplay 
	{	
		FlowPanel getPanel() ;
		
		void      setWelcomeText(String sPseudo) ;
		void      setText(String sText) ;
	}

	private final CoachingFitSupervisor _supervisor ;
	
	@Inject
	public CoachingFitPostLoginHeaderPresenter(final Display               display, 
						                                 final EventBus              eventBus,
						                                 final DispatchAsync         dispatcher,
						                                 final CoachingFitSupervisor supervisor) 
	{
		super(display, eventBus) ;
		
		_supervisor = supervisor ;
		
		display.setWelcomeText(_supervisor.getUserPseudo()) ;
		
		bind() ;
	}
	
	@Override
	protected void onBind() 
	{
		eventBus.addHandler(PostLoginHeaderEvent.TYPE, new PostLoginHeaderEventHandler(){
			public void onPostLoginHeader(final PostLoginHeaderEvent event) 
			{
				FlowPanel workSpace = event.getHeader() ;
				workSpace.add(getDisplay().asWidget()) ;
				display.setWelcomeText(_supervisor.getUserPseudo()) ;
			}
		});
		
		eventBus.addHandler(PostLoginHeaderDisplayEvent.TYPE, new PostLoginHeaderDisplayEventHandler(){
			public void onPostLoginHeaderDisplay(final PostLoginHeaderDisplayEvent event) 
			{
				String sTextToDisplay = event.getDisplayedText() ;
				
				if ("".equals(sTextToDisplay))				
					display.setWelcomeText(_supervisor.getUserPseudo()) ;
				else
					display.setText(sTextToDisplay) ;
			}
		});
	}

	@Override
  protected void onUnbind() {
	  // TODO Auto-generated method stub
  }

	@Override
  public void revealDisplay() {
	  // TODO Auto-generated method stub
  }

	@Override
	protected void onRevealDisplay() {
		// TODO Auto-generated method stub
	}	
}
