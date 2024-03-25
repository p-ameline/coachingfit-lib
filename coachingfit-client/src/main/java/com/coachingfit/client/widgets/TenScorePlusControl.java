package com.coachingfit.client.widgets;

import com.coachingfit.client.loc.CoachingFitConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;

import com.google.inject.Inject;

/**
 * 4 buttons score setting widget: N for "not evaluated", then 0, 1/2, 1 for unfit, semi-fit, fit
 * 
 */
public class TenScorePlusControl extends TenScoreControl
{
	// Controls
	protected Button _ButtonNotEvaluated ;
	
	protected final String _sNotEvaluatedScore = "E" ;
  
  /**
   * Default Constructor
   *
   */
  @Inject
  public TenScorePlusControl(final String sPath) {
  	super(sPath) ;
  }

  protected void createButtons()
  {
  	_ButtonNotEvaluated = new Button(getLabelForScore(_sNotEvaluatedScore)) ;
    add(_ButtonNotEvaluated) ;
    
    super.createButtons() ;
  }
  
  protected void refresh()
  {
  	initNonEvaluated() ;
  	
  	super.refresh() ;
  }
  
  /**
   * Return selected score as weight * "", "E", "0", "0.5" or "1"
   */
	public String getContentAsString()
	{
		if (_sNotEvaluatedScore.equals(getScore()))
			return _sNotEvaluatedScore ;
		
		return getWeightedContentAsString(getScore()) ;
	}
	
	/**
   * Return content as a double
   */
	public double getContentAsDouble() 
	{
		if (_sNotEvaluatedScore.equals(getScore()))
			return 0 ;
		
		return super.getContentAsDouble() ;
	}
  
	/**
   * Return selected expected score from seniority as weight * "", "0", "0.5" or "1"
   * 
   * @param sSeniority Seniority expressed as a YYMMDD string
   *
   */
	public String getExpectedContentAsString(final String sSeniority) 
	{
		if (_sNotEvaluatedScore.equals(getScore()))
			return _sNotEvaluatedScore ;
		
		if ((null == sSeniority) || "".equals(sSeniority))
			return getWeightedContentAsString(_sFitScore) ;
		
		if (sSeniority.compareTo(_sExpectedFit) >= 0)
			return getWeightedContentAsString(_sFitScore) ;
		
		if (sSeniority.compareTo(_sExpectedSemiFit) >= 0)
			return getWeightedContentAsString(_sSemiFitScore) ;
		
		return _sNotEvaluatedScore ;
	}
	
	/**
   * Return selected expected score from seniority as weight * 0, 0.5 or 1
   * 
   * @param sSeniority Seniority expressed as a YYMMDD string
   *
   */
	public double getExpectedContentAsDouble(final String sSeniority) 
	{
		String sExpectedScore = getExpectedContentAsString(sSeniority) ;
		
		if ("".equals(sExpectedScore) || _sNotEvaluatedScore.equals(sExpectedScore))
			return 0 ;
		
		try {
			return Double.parseDouble(sExpectedScore) ;
		} catch (NumberFormatException e) {
			return 0 ;
		}
	}
	
	public double getWeightAsDouble()
	{
		if (_sNotEvaluatedScore.equals(getScore()))
			return 0 ;
		
		return super.getWeightAsDouble() ;
	}
	
	/**
   * Return a weighted score as n * "", "A", "0", "0.5" or "1"
   *
   */
	protected String getWeightedContentAsString(final String sScore) 
	{
		if (_sNotEvaluatedScore.equals(sScore))
			return _sNotEvaluatedScore ;
		
		return super.getWeightedContentAsString(sScore) ;
	}
	
	/**
	 * Initialize the "non evaluated" button depending on current score
	 */
  protected void initNonEvaluated() {
  	initButton(_ButtonNotEvaluated, _sNotEvaluatedScore, "fitbutton " + getStyleForScore(_sNotEvaluatedScore)) ;
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
   * Manage clicks on one of the 4 buttons
   */
	protected void setChangeHandlers()
	{
		_ButtonNotEvaluated.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent event)
			{
				if (false == _base.isReadOnly())
					ManageChange(_sNotEvaluatedScore) ;
			}
		});
		
		super.setChangeHandlers() ;
	}
	
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) 
	{
		_ButtonNotEvaluated.addClickHandler(handler) ;
		
		return super.addClickHandler(handler) ;
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
		if (_sNotEvaluatedScore.equals(sScore))
			return constants.scoreNotEvaluable() ;
		
		return "" ;
	}
	
	/**
   * Return a (button) style that corresponds to a given score (for example "fitorange" (red background) for "0")
   *
   */
	public String getStyleForScore(final String sScore)
	{
		String sRO = "" ;
		if (_base.isReadOnly())
			sRO = "ReadOnly" ;
		
		if ((null == sScore) || "".equals(sScore))
			return "fitwhite" + sRO ;
		
		if (_sNotEvaluatedScore.equals(sScore))
			return "fitblue" + sRO ;
		if (_sUnfitScore.equals(sScore))
			return "fitorange" + sRO ;
		if (_sSemiFitScore.equals(sScore))
			return "fitpalegreen" + sRO ;
		if (_sFitScore.equals(sScore))
			return "fitgreen" + sRO ;
		
		return "fitwhite" + sRO ;
	}
	
	protected void initScoreFromContent(final String sContent)
	{
		setScore("") ;
		
		if ((null == sContent) || "".equals(sContent))
			return ;
		
		setScore(sContent) ;
		
		if (_sNotEvaluatedScore.equals(getScore()))
			return ;
		
		super.initScoreFromContent(sContent) ;
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
			
			setScore(_sNotEvaluatedScore) ;
			initNonEvaluated() ;
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
		initNonEvaluated() ;
		
		// Remove the white style, so the read only style can apply
		//
		for (Button button : _aButtons)
			button.removeStyleName("fitbutton fitwhite") ;
		_ButtonNotEvaluated.removeStyleName("fitbutton fitwhite") ;
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
		initNonEvaluated() ;
		
		// Remove the white style, so the read only style can apply
		//
		for (Button button : _aButtons)
			button.removeStyleName("fitbutton fitwhiteReadOnly") ;
		
		_ButtonNotEvaluated.removeStyleName("fitbutton fitwhiteReadOnly") ;
	}
}
