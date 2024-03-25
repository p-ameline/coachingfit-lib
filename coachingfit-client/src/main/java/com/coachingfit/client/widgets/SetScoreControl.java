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
 * 4 buttons score setting widget: A for "still learning", then 0, 1/2, 1 for unfit, semi-fit, fit
 * 
 */
public class SetScoreControl extends FitScoreControl implements ControlModel, HasClickHandlers
{
	// Controls
	protected Button _ButtonApprentice ;
	
	protected final String _sApprenticeScore = "A" ;
  
  /**
   * Default Constructor
   *
   */
  @Inject
  public SetScoreControl(final String sPath) {
  	super(sPath) ;
  }

  protected void createButtons()
  {
  	_ButtonApprentice = new Button(getLabelForScore(_sApprenticeScore)) ;
    add(_ButtonApprentice) ;
    
    super.createButtons() ;
  }
  
  protected void refresh()
  {
  	initApprentice() ;
  	
  	super.refresh() ;
  }
  
  /**
   * Return selected score as weight * "", "A", "0", "0.5" or "1"
   */
	public String getContentAsString()
	{
		if (_sApprenticeScore.equals(getScore()))
			return _sApprenticeScore ;
		
		return getWeightedContentAsString(getScore()) ;
	}
	
	/**
   * Return content as a double
   */
	public double getContentAsDouble() 
	{
		if (_sApprenticeScore.equals(getScore()))
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
		
		return _sApprenticeScore ;
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
		
		if ("".equals(sExpectedScore) || _sApprenticeScore.equals(sExpectedScore))
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
		if (_sApprenticeScore.equals(sScore))
			return _sApprenticeScore ;
		
		return super.getWeightedContentAsString(sScore) ;
	}
	
	/**
	 * Initialize the apprentice button depending on current score
	 */
  protected void initApprentice() {
  	initButton(_ButtonApprentice, _sApprenticeScore, "fitbutton " + getStyleForScore(_sApprenticeScore)) ;
  }
    
  /**
   * Manage clicks on one of the 4 buttons
   */
	protected void setChangeHandlers()
	{
		_ButtonApprentice.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent event)
			{
				if (false == _base.isReadOnly())
					ManageChange(_sApprenticeScore) ;
			}
		});
		
		super.setChangeHandlers() ;
	}
	
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) 
	{
		_ButtonApprentice.addClickHandler(handler) ;
		
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
			return constants.scoreSetUnfit() ;
		if (_sSemiFitScore.equals(sScore))
			return constants.scoreSetSemiFit() ;
		if (_sFitScore.equals(sScore))
			return constants.scoreSetFit() ;
		if (_sApprenticeScore.equals(sScore))
			return constants.scoreSetApprentice() ;
		
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
		
		if (_sApprenticeScore.equals(sScore))
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
		
		if (_sApprenticeScore.equals(getScore()))
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
		initApprentice() ;
		
		// Remove the white style, so the read only style can apply
		//
		_ButtonUnfit.removeStyleName("fitbutton fitwhite");
		_ButtonSemiFit.removeStyleName("fitbutton fitwhite") ;
		_ButtonFit.removeStyleName("fitbutton fitwhite") ;
		_ButtonApprentice.removeStyleName("fitbutton fitwhite") ;
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
		initApprentice() ;
		
		// Remove the white style, so the read only style can apply
		//
		_ButtonUnfit.removeStyleName("fitbutton fitwhiteReadOnly");
		_ButtonSemiFit.removeStyleName("fitbutton fitwhiteReadOnly") ;
		_ButtonFit.removeStyleName("fitbutton fitwhiteReadOnly") ;
		_ButtonApprentice.removeStyleName("fitbutton fitwhiteReadOnly") ;
	}	
}
