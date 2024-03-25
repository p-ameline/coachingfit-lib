package com.coachingfit.client.widgets;

import com.coachingfit.client.loc.CoachingFitConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;

import com.google.inject.Inject;

import com.primege.client.widgets.ControlModel;

/**
 * 4 buttons score setting widget: -1 for "not observed", then 0, 1/2, 1 for dangerous context, dangerous act, good point
 * 
 */
public class VscScoreControl extends FitScoreControl implements ControlModel, HasClickHandlers
{
	// Controls
	protected Button _ButtonNotObserved ;
	
	protected final String _sNotObservedScore = "-1" ;
  
  /**
   * Default Constructor
   *
   */
  @Inject
  public VscScoreControl(final String sPath) {
  	super(sPath) ;
  }

  protected void createButtons()
  {
  	_ButtonNotObserved = new Button(getLabelForScore(_sNotObservedScore)) ;
    add(_ButtonNotObserved) ;
    
    // super.createButtons() ;  Rewritten since buttons order is not the same
    //
    _ButtonUnfit   = new Button(getLabelForScore(_sUnfitScore)) ;
  	_ButtonSemiFit = new Button(getLabelForScore(_sSemiFitScore)) ;
  	_ButtonFit     = new Button(getLabelForScore(_sFitScore)) ;
    
  	add(_ButtonFit) ;
  	add(_ButtonSemiFit) ;
  	add(_ButtonUnfit) ;
  }
  
  protected void refresh()
  {
  	initNotObserved() ;
  	
  	super.refresh() ;
  }
  
  /**
   * Return selected score as weight * "", "A", "0", "0.5" or "1"
   */
	public String getContentAsString()
	{
		if (_sNotObservedScore.equals(getScore()))
			return _sNotObservedScore ;
		
		return getWeightedContentAsString(getScore()) ;
	}
	
	/**
   * Return content as a double
   */
	public double getContentAsDouble() 
	{
		if (_sNotObservedScore.equals(getScore()))
			return 0 ;
		
		try 
		{
			double dScore = Double.parseDouble(getScore()) ;
			return getWeightedContent(dScore) ;
		} 
		catch (NumberFormatException e) {
			return 0 ;
		}
	}
  
	/**
   * Return selected expected score from seniority as weight * "", "0", "0.5" or "1"
   * 
   * @param sSeniority Seniority expressed as a YYMMDD string
   *
   */
	public String getExpectedContentAsString(final String sSeniority) 
	{
		if ((null == sSeniority) || "".equals(sSeniority))
			return getWeightedContentAsString(_sFitScore) ;
		
		if (sSeniority.compareTo(_sExpectedFit) >= 0)
			return getWeightedContentAsString(_sFitScore) ;
		
		if (sSeniority.compareTo(_sExpectedSemiFit) >= 0)
			return getWeightedContentAsString(_sSemiFitScore) ;
		
		return _sNotObservedScore ;
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
		
		if ("".equals(sExpectedScore) || _sNotObservedScore.equals(sExpectedScore))
			return 0 ;
		
		try {
			return Double.parseDouble(sExpectedScore) ;
		} catch (NumberFormatException e) {
			return 0 ;
		}
	}
	
	/**
   * Return a weighted score as n * 0, 0.5 or 1
   *
   */
	protected double getWeightedContent(final double dScore) 
	{
		if ((null == _sWeight) || "".equals(_sWeight) || "1".equals(_sWeight))
			return dScore ;
		
		Double dWeight = (double) 0 ; 
		
		try {
			dWeight = Double.parseDouble(_sWeight) ;
		} catch (NumberFormatException e) {
			return dScore ;
		}
		
		return dScore * dWeight ;
	}
	
	/**
   * Return a weighted score as n * "", "A", "0", "0.5" or "1"
   *
   */
	protected String getWeightedContentAsString(final String sScore) 
	{
		if (_sNotObservedScore.equals(sScore))
			return _sNotObservedScore ;
		
		return super.getWeightedContentAsString(sScore) ;
	}
	
	/**
	 * Initialize the "not observed" button depending on current score
	 */
  protected void initNotObserved() {
  	initButton(_ButtonNotObserved, _sNotObservedScore, "fitbutton " + getStyleForScore(_sNotObservedScore)) ;
  }
    
  /**
   * Manage clicks on one of the 4 buttons
   */
	protected void setChangeHandlers()
	{
		_ButtonNotObserved.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent event)
			{
				if (false == _base.isReadOnly())
					ManageChange(_sNotObservedScore) ;
			}
		});
		
		super.setChangeHandlers() ;
	}
	
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) 
	{
		_ButtonNotObserved.addClickHandler(handler) ;
		
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
			return constants.scoreVscDangerContext() ;
		if (_sSemiFitScore.equals(sScore))
			return constants.scoreVscDangerAct() ;
		if (_sFitScore.equals(sScore))
			return constants.scoreVscGood() ;
		if (_sNotObservedScore.equals(sScore))
			return constants.scoreVscNotObserved() ;
		
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
		
		if (_sNotObservedScore.equals(sScore))
			return "fitblue" + sRO ;
		if (_sUnfitScore.equals(sScore))
			return "fitred" + sRO ;
		if (_sSemiFitScore.equals(sScore))
			return "fitorange" + sRO ;
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
		
		if (_sNotObservedScore.equals(getScore()))
			return ;
		
		super.initScoreFromContent(sContent) ;
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
		initNotObserved() ;
		
		// Remove the white style, so the read only style can apply
		//
		_ButtonUnfit.removeStyleName("fitbutton fitwhite");
		_ButtonSemiFit.removeStyleName("fitbutton fitwhite") ;
		_ButtonFit.removeStyleName("fitbutton fitwhite") ;
		_ButtonNotObserved.removeStyleName("fitbutton fitwhite") ;
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
		initNotObserved() ;
		
		// Remove the white style, so the read only style can apply
		//
		_ButtonUnfit.removeStyleName("fitbutton fitwhiteReadOnly");
		_ButtonSemiFit.removeStyleName("fitbutton fitwhiteReadOnly") ;
		_ButtonFit.removeStyleName("fitbutton fitwhiteReadOnly") ;
		_ButtonNotObserved.removeStyleName("fitbutton fitwhiteReadOnly") ;
	}	
}
