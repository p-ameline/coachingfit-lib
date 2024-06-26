package com.coachingfit.client.mvp;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.primege.client.loc.PrimegeViewConstants;

public class CoachingFitPostLoginHeaderView extends Composite implements CoachingFitPostLoginHeaderPresenter.Display 
{	
	private final PrimegeViewConstants constants = GWT.create(PrimegeViewConstants.class) ;
	
	private FlowPanel _hPanel ;
		
	private Label     _successWelcomeLabel ;
	private String    _sWelcomeText ;
	
	public CoachingFitPostLoginHeaderView() 
	{
		_successWelcomeLabel = new Label("Login successful, welcome to the map page") ;
		_successWelcomeLabel.setStyleName("successWelcomeLabel") ;
		
		_hPanel = new FlowPanel() ;
		_hPanel.add(_successWelcomeLabel) ;
		
		initWidget(_hPanel) ;
	}
		
	@Override
	public Widget asWidget() {
		return this ;
	}

	@Override
	public FlowPanel getPanel() {
		return _hPanel ;
	}
	
	@Override
	public void setWelcomeText(String sPseudo)
	{
		String sUserName ;
		if (null == sPseudo)
			sUserName = "" ;
		else
			sUserName = sPseudo ;
		
		_sWelcomeText = constants.welcomePseudo() ;
		if (_sWelcomeText.equals(""))
			return ;
				
		if (_sWelcomeText.contains("pseudo"))
			 _sWelcomeText = _sWelcomeText.replaceAll("pseudo", sUserName) ;
			
		_successWelcomeLabel.setText(_sWelcomeText) ;
	}
	
	@Override
	public void setText(String sText)
	{
		_successWelcomeLabel.setText(sText) ;
	}
}
