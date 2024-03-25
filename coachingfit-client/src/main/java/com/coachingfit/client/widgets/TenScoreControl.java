package com.coachingfit.client.widgets;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

/**
 * 11 buttons score setting widget: 0 to 10 a percentage of expected score
 * 
 */
public class TenScoreControl extends FitScoreBase
{
  protected ArrayList<Button> _aButtons ;
  
  /**
   * Default Constructor
   *
   */
  @Inject
  public TenScoreControl(final String sPath) {
  	super(sPath) ;
  }
  
  /**
   * Create buttons "0" to "10"
   */
  protected void createButtons()
  {
  	_aButtons = new ArrayList<Button>() ;
  	
  	for (int i = 0 ; i < 11 ; i++)
  	{
  		Button scoreButton = new Button("" + i) ;
  		
  		String sTarget = "" ;
  		if      (0 == i)
  			sTarget = "0" ;
  		else if (10 == i)
  			sTarget = "1" ;
  		else
  			sTarget = "0." + i ;
  		
  		final String sButtonId = "button_" + sTarget ;
  		scoreButton.getElement().setId(sButtonId) ;
  		add(scoreButton) ;
 /* 		
  		scoreButton.addClickHandler(new ClickHandler() {
  			public void onClick(final ClickEvent event)
  			{
  				ManageChange(sButtonId) ;
  			}
  		});
 */ 		
  		_aButtons.add(scoreButton) ;
  	}
  }
  
  protected void refresh() {
  	initButtons() ;
  }
  
  protected void initButtons()
  {
  	for (Button button : _aButtons)
  		initButton(button) ;
  }  
	
  /**
   * Initialize button's state depending on current score
   * 
   * @param fitButton  Button to initialize (either unfit, semi-fit or fit)
   */
  protected void initButton(Button button) 
  {
  	if (null == button)
  		return ;
  	
  	String sTarget = getButtonTarget(button) ;
  	
  	String sStyleName = getBasicStyleForScore(sTarget) ;
  	
  	if (false == sTarget.equals(getScore()))
  	{
  		button.removeStyleName(sStyleName) ;
  		button.addStyleName("fitbutton fitwhite") ;
  		return ;
  	}
  	
  	button.removeStyleName("fitbutton " + getStyleForScore("")) ;
  	button.addStyleName(sStyleName) ;
  }
  
  /**
   * Manage clicks on one of the 3 buttons
   */
	protected void setChangeHandlers()
	{
		for (Button button : _aButtons)
			button.addClickHandler(new ClickHandler() {
				public void onClick(final ClickEvent event)
				{
					if (false == _base.isReadOnly())
						ManageChange(getButtonTarget(button)) ;
				}
			});
	}
	
	/**
	 * Change from current score to a new target score (and update display)
	 */
	protected void ManageChange(final String sButtonTarget) 
	{
		if ((null == sButtonTarget) || "".equals(sButtonTarget))
			return ;
		
		// If the score is already equals to the target, it means that the button was unselected
		//
		if (getScore().equals(sButtonTarget))
			setScore("") ;
		else
			setScore(sButtonTarget) ;
		
		refresh() ;
  }
	
	/**
	 * Get expected target of a button
	 */
	protected String getButtonTarget(final Button button)
	{
		if (null == button)
			return "" ;
		
		// Button's identifier is of the form "button_target", for example button_0.4
		//
		String sButtonId = button.getElement().getId() ;
		String[] decomposition = sButtonId.split("_") ;
		
		return decomposition[1] ;
	}
	
	protected Button getButtonForTarget(final String sTarget)
	{
		if ((null == sTarget) || "".equals(sTarget))
			return null ;
		
		for (Button button : _aButtons)
			if (sTarget.equals(getButtonTarget(button)))
				return button ;
		
		return null ;
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) 
	{
		for (Button button : _aButtons)
			button.addClickHandler(handler) ;
		
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
		
		if ("1".equals(sScore))
			return "10" ;
		if ("0".equals(sScore))
			return "0" ;
		
		if ((sScore.length() == 3) && ('0' == sScore.charAt(0)) && ('.' == sScore.charAt(1)))
			return "" + sScore.charAt(2) ;
		
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
			return "fitorange" + sRO ;
		if (_sSemiFitScore.equals(sScore))
			return "fitpalegreen" + sRO ;
		if (_sFitScore.equals(sScore))
			return "fitgreen" + sRO ;
		
		if ((sScore.length() == 3) && ('0' == sScore.charAt(0)) && ('.' == sScore.charAt(1)))
			return "fit" + sScore.charAt(2) + sRO ;
		
		return "fitwhite" + sRO ;
	}
	
	/**
   * Return a (button) style that corresponds to a given score<br>
   * (for example "fitred" (red background) or "fitredReadOnly" for "0")
   *
   */
	public String getBasicStyleForScore(final String sScore)
	{
		if ((null == sScore) || "".equals(sScore))
			return "fitwhite" ;
		
		if (_sUnfitScore.equals(sScore))
			return "fitorange" ;
		if (_sSemiFitScore.equals(sScore))
			return "fitpalegreen" ;
		if (_sFitScore.equals(sScore))
			return "fitgreen" ;
		
		if ((sScore.length() == 3) && ('0' == sScore.charAt(0)) && ('.' == sScore.charAt(1)))
			return "fit" + sScore.charAt(2) ;
		
		return "fitwhite" ;
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
			// Auto semi-fit just means that all the buttons above semi-fit are desactivated
			//
			if (_bAutoSemiFit)
			{
				// _sScore = _sSemiFitScore ;
				// initSemifit() ;
				
				for (Button button : _aButtons)
				{
					String sButtonTarget = this.getButtonTarget(button) ;
					if (_sSemiFitScore.compareTo(sButtonTarget) < 0)
						button.removeStyleName("fitbutton fitwhite") ;
				}
				
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
			
			Button unfitButton = getButtonForTarget("0") ;
			initButton(unfitButton) ;
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
		
		initButtons() ;
		
		// Remove the white style, so the read only style can apply
		//
		for (Button button : _aButtons)
			button.removeStyleName("fitbutton fitwhite") ;
	}
	
	protected void switchToWriteMode()
	{
		if (false == _base.isReadOnly())
			return ;
		
		preInitializeInReadWriteMode() ;
		_base.setReadOnly(false) ;
	}
	
	/**
	 * Prepare the control to switch in Read Write mode
	 */
	protected void preInitializeInReadWriteMode()
	{
		// Set all buttons to white
		//
		setScore("") ;
		
		initButtons() ;
		
		// Remove the white style, so the read only style can apply
		//
		for (Button button : _aButtons)
			button.removeStyleName("fitbutton fitwhiteReadOnly") ;
	}
}
