package com.coachingfit.client.widgets;

import com.coachingfit.client.loc.CoachingFitConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;

import com.google.inject.Inject;

/**
 * 3 buttons score setting widget: 0, 1/2, 1 for unfit, semi-fit, fit
 * 
 */
public class FitScoreControl extends FitScoreBase
{
	// Controls
  protected Button  _ButtonUnfit ;
  protected Button  _ButtonSemiFit ;
  protected Button  _ButtonFit ;
  
  /**
   * Default Constructor
   *
   */
  @Inject
  public FitScoreControl(final String sPath) {
  	super(sPath) ;
  }
  
  protected void createButtons()
  {
  	_ButtonUnfit   = new Button(getLabelForScore(_sUnfitScore)) ;
  	_ButtonSemiFit = new Button(getLabelForScore(_sSemiFitScore)) ;
  	_ButtonFit     = new Button(getLabelForScore(_sFitScore)) ;
    
    add(_ButtonUnfit) ;
    add(_ButtonSemiFit) ;
    add(_ButtonFit) ;
  }
  
  protected void refresh()
  {
  	initUnfit() ;
  	initSemifit() ;
  	initFit() ;
  }
  
	/**
	 * Initialize the unfit button depending on current score
	 */
  protected void initUnfit() {
  	initButton(_ButtonUnfit, _sUnfitScore, "fitbutton " + getStyleForScore(_sUnfitScore)) ;
  }
  
  /**
   * Initialize the semi-fit button depending on current score
   */
  protected void initSemifit() {
  	initButton(_ButtonSemiFit, _sSemiFitScore, "fitbutton " + getStyleForScore(_sSemiFitScore)) ;
  }
  
  /**
   * Initialize the fit button depending on current score
   */
  protected void initFit() {
  	if (false == _bForbiddenFit)
  		initButton(_ButtonFit, _sFitScore, "fitbutton " + getStyleForScore(_sFitScore)) ;
  }
  
  /**
   * Initialize button's state depending on current score
   * 
   * @param fitButton  Button to initialize (either unfit, semi-fit or fit)
   * @param sTarget    Target score for the button to be 'on'
   * @param sStyleName Style that visually sets the button 'on'
   */
  protected void initButton(Button fitButton, final String sTarget, final String sStyleName) 
  {
  	if ((null == fitButton) || (null == sTarget))
  		return ;
  	
  	if (false == sTarget.equals(getScore()))
  	{
  		fitButton.removeStyleName(sStyleName) ;
  		fitButton.addStyleName("fitbutton fitwhite") ;
  		return ;
  	}
  	
  	fitButton.removeStyleName("fitbutton " + getStyleForScore("")) ;
  	fitButton.addStyleName(sStyleName) ;
  }

  /**
   * Manage clicks on one of the 3 buttons
   */
	protected void setChangeHandlers()
	{
		_ButtonUnfit.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent event)
			{
				if (false == _base.isReadOnly())
					ManageChange(_sUnfitScore) ;
			}
		});
		
		_ButtonSemiFit.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent event) 
			{
				if (false == _base.isReadOnly())
					ManageChange(_sSemiFitScore) ;
			}
		});
		
		_ButtonFit.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent event) 
			{
				if ((false == _base.isReadOnly()) && (false == _bForbiddenFit))
					ManageChange(_sFitScore) ;
			}
		});
	}
	
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) 
	{
		_ButtonUnfit.addClickHandler(handler) ;
		_ButtonSemiFit.addClickHandler(handler) ;
		_ButtonFit.addClickHandler(handler) ;
		
		return null ;
	}
	
	/**
   * Return a (button) label that corresponds to a given score (for example "semi-fit" for "1" with a weight of 2)
   *
   */
	protected String getLabelForScore(final String sScore)
	{
		if ((null == sScore) || "".equals(sScore))
			return "" ;
		
		CoachingFitConstants constants = GWT.create(CoachingFitConstants.class) ;
		
		if (_sUnfitScore.equals(sScore))
			return constants.scoreUnfit() ;
		if (_sSemiFitScore.equals(sScore))
			return constants.scoreSemiFit() ;
		if (_sFitScore.equals(sScore))
			return constants.scoreFit() ;
		
		return "" ;
	}
	
	/**
   * Return a (button) style that corresponds to a given score<br>
   * (for example "fitred" (red background) or "fitredReadOnly" for "0")
   *
   */
	public String getStyleForScore(final String sScore)
	{
		String sRO = "" ;
		if (_base.isReadOnly())
			sRO = "ReadOnly" ;
		
		if ((null == sScore) || "".equals(sScore))
			return "fitwhite" + sRO ;
		
		if (_sUnfitScore.equals(sScore))
			return "fitred" + sRO ;
		if (_sSemiFitScore.equals(sScore))
			return "fitpalegreen" + sRO ;
		if (_sFitScore.equals(sScore))
		{
			if (_bForbiddenFit)
				sRO = "ReadOnly" ;
			return "fitgreen" + sRO ;
		}
		
		return "fitwhite" + sRO ;
	}
	
	/**
	 * If the control is not already set (as unfit, semi-fit or fit), and has the auto-unfit or auto-semi-fit on, initialize it
	 * depending on seniority  
	 */
	public void initIfUnset(final String sSeniority)
	{
		switchToWriteMode() ;
		
		_bForbiddenFit = false ;
		
		if ((null == sSeniority) || "".equals(sSeniority))
			return ;
		
		// If already set, leave
		//
		if (false == "".equals(getScore()))
			return ;
		
		// If not auto-unfit and not auto-semi-fit, leave
		//
		if ((false == _bAutoUnfit) && (false == _bAutoSemiFit))
			return ;
		
		// If the expected score is fit, nothing to do
		//
		if (sSeniority.compareTo(_sExpectedFit) >= 0)
			return ;
		
		// If the expected score is semi-fit, set it if auto-semi-fit 
		//
		if (sSeniority.compareTo(_sExpectedSemiFit) >= 0)
		{
			// Auto semi-fit just means that the "fit" button is unactivated
			//
			if (_bAutoSemiFit)
			{
				// _sScore = _sSemiFitScore ;
				// initSemifit() ;
				
				_ButtonFit.removeStyleName("fitbutton fitwhite") ;
				_bForbiddenFit = true ;
			}
			
			return ;
		}
		
		// If there, it means that the expected score is unfit or not evaluable (if this option exists)
		//
		if (_bAutoUnfit)
		{
			if (_bLockAutoUnfit)
			{
				preInitializeInReadOnlyMode() ;
				_base.setReadOnly(true) ;
			}
			
			setScore(_sUnfitScore) ;
			initUnfit() ;
		}
	}
	
	/**
	 * If the control has been "forced" to unfit due to seniority, mark it read only so that it can be published 
	 * with the proper style
	 */
	public void initForScreenShotMode(final String sSeniority)
	{
		if ((null == sSeniority) || "".equals(sSeniority))
			return ;
		
		// If not auto-unfit and not auto-semi-fit, leave
		//
		if ((false == _bAutoUnfit) && (false == _bAutoSemiFit))
			return ;
		
		// If the score is fit, nothing to do
		//
		if (_sFitScore.equals(getScore()))
			return ;
		
		// If the score is semi-fit, check if it was forced 
		//
		if (_sSemiFitScore.equals(getScore())) 
		{
			if (_bAutoSemiFit && (sSeniority.compareTo(_sExpectedSemiFit) >= 0))
				_base.setReadOnly(true) ;
			return ;
		}
		
		// If there, it means that the score is unfit
		//
		if (_sUnfitScore.equals(getScore())) 
		{
			if (_bLockAutoUnfit && _bAutoUnfit && (sSeniority.compareTo(_sExpectedSemiFit) < 0))
				_base.setReadOnly(true) ;
		}
	}
	
	/**
	 * Prepare the control to switch in Read Only mode
	 */
	protected void preInitializeInReadOnlyMode()
	{
		// Set all buttons to white
		//
		setScore("") ;
		
		initFit() ;
		initSemifit() ;
		initUnfit() ;
		
		// Remove the white style, so the read only style can apply
		//
		_ButtonUnfit.removeStyleName("fitbutton fitwhite") ;
		_ButtonSemiFit.removeStyleName("fitbutton fitwhite") ;
		_ButtonFit.removeStyleName("fitbutton fitwhite") ;		
	}
	
	/**
	 * Prepare the control to switch in Read Write mode
	 */
	protected void preInitializeInReadWriteMode()
	{
		// Set all buttons to white
		//
		setScore("") ;
		
		initFit() ;
		initSemifit() ;
		initUnfit() ;
		
		// Remove the white style, so the read only style can apply
		//
		_ButtonUnfit.removeStyleName("fitbutton fitwhiteReadOnly") ;
		_ButtonSemiFit.removeStyleName("fitbutton fitwhiteReadOnly") ;
		
		if (false == _bForbiddenFit)
			_ButtonFit.removeStyleName("fitbutton fitwhiteReadOnly") ;		
	}
}
