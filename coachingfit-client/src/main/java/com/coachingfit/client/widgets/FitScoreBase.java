package com.coachingfit.client.widgets;

import com.coachingfit.shared.util.CoachingFitDelay;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HorizontalPanel;

import com.google.inject.Inject;

import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.ControlBaseWithParams;
import com.primege.client.widgets.ControlModel;
import com.primege.shared.database.FormDataData;

/**
 * N buttons score setting widget with a "score" range from 0 to 1
 * 
 */
public class FitScoreBase extends HorizontalPanel implements ControlModel, HasClickHandlers
{
	/**
	 * Specific parameters manager
	 * 
	 * @author Philippe
	 */
	protected class LocalControlBase extends ControlBaseWithParams
	{
		public LocalControlBase(final String sPath) {
			super(sPath) ;
		}

		/**
		 * Initialize a parameter from its name
		 * 
		 * @param sParam Name of parameter to initialize
		 * @param sValue Value to initialize this parameter with (can be null to "default value")
		 */
		public void fillParam(final String sParam, final String sValue)
		{
			if ((null == sParam) || "".equals(sParam))
				return ;

			// Don't forget to call superclass function in order to initialize global parameters,
			// such as mandatory status or default value 
			//
			super.fillParam(sParam, sValue) ;

			String sVal = "" ;
			if (null != sValue)
				sVal = sValue ;

			// "weight=3|semiFitDelay=001800|fitDelay=002400"
			if      ("weight".equalsIgnoreCase(sParam))
				_sWeight = sVal ; 
			else if ("semiFitDelay".equalsIgnoreCase(sParam))
			{
				// Could have done "_sExpectedSemiFit = sVal ;" but we would not be certain that the delay is normalized
				CoachingFitDelay delay = new CoachingFitDelay(sVal) ;
				_sExpectedSemiFit = delay.getAsString() ;
			}
			else if ("fitDelay".equalsIgnoreCase(sParam))
			{
				// _sExpectedFit = sVal ;
				CoachingFitDelay delay = new CoachingFitDelay(sVal) ;
				_sExpectedFit = delay.getAsString() ;
			}
			else if ("autoUnfit".equalsIgnoreCase(sParam)) 
			{
				if ("lock".equalsIgnoreCase(sVal))
				{
					_bAutoUnfit     = true ;
					_bLockAutoUnfit = true ;
				}
				else
					_bAutoUnfit = isTrue(sVal) ;
			}
			else if ("autoSemiFit".equalsIgnoreCase(sParam)) {
				_bAutoSemiFit = isTrue(sVal) ;
			}
		}
	}

	/** Specific parameters manager */
	protected LocalControlBase _base ;

	// Scoring information
	private   String  _sScore ;
	protected String  _sWeight ;           // score multiplier
	protected String  _sExpectedSemiFit ;  // delay (in a "YYMMDD" format) from which a semi-fit score is expected 
	protected String  _sExpectedFit ;      // delay (in a "YYMMDD" format) from which a fit score is expected
	protected boolean _bAutoUnfit ;        // when below the semi-fit seniority, should the unfit score be preset?
	protected boolean _bLockAutoUnfit ;    // when the unfit score is preset, should it be read only?
	protected boolean _bAutoSemiFit ;      // when below the fit delay, should the semi-fit score be preset
	protected boolean _bForbiddenFit ;     // is the fit button forbidden?

	protected final String _sFitScore     = "1" ;
	protected final String _sSemiFitScore = "0.5" ;
	protected final String _sUnfitScore   = "0" ;

	/**
	 * Default Constructor
	 *
	 */
	@Inject
	public FitScoreBase(final String sPath) 
	{
		super() ;

		_base = new LocalControlBase(sPath) ;

		createButtons() ;

		_sScore           = "" ;
		_sWeight          = "1" ;

		_sExpectedSemiFit = "" ;
		_sExpectedFit     = "" ;

		_bAutoUnfit       = false ;
		_bLockAutoUnfit   = false ;
		_bAutoSemiFit     = false ;
		_bForbiddenFit    = false ;

		setChangeHandlers() ;

		refresh() ;
	}

	public boolean isSingleData() {
		return true ;
	}

	/**
	 * Create controls
	 * 
	 * To be instantiated by subclasses
	 */
	protected void createButtons() {
	}

	public void init(final String sContent)
	{
		if (null == sContent)
			_sScore = "" ;
		else
			_sScore = sContent ;

		refresh() ;
	}

	/**
	 * To be instantiated by subclasses
	 */
	protected void refresh() {
	}

	public boolean isActivated() {
		return (false == "".equals(_sScore)) ;
	}

	/**
	 * Return selected score as weight * "", "0", "0.5" or "1"
	 *
	 */
	public String getContentAsString() {
		return getWeightedContentAsString(_sScore) ;
	}

	/**
	 * Return content as a double
	 */
	public double getContentAsDouble() 
	{
		try 
		{
			double dScore = Double.parseDouble(_sScore) ;
			return getWeightedContent(dScore) ;
		} 
		catch (NumberFormatException e) {
			return 0 ;
		}
	}

	/**
	 * Return expected score, based on seniority, as weight * "", "0", "0.5" or "1"
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

		return getWeightedContentAsString(_sUnfitScore) ;
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

		if ("".equals(sExpectedScore))
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
	 * Return a weighted score as n * "", "0", "0.5" or "1"
	 *
	 */
	protected String getWeightedContentAsString(final String sScore) 
	{
		if ((null == _sWeight) || "".equals(_sWeight) || "1".equals(_sWeight))
			return sScore ;

		if ("".equals(sScore))
			return "" ;

		Double dContent = (double) 0 ; 

		try {
			dContent = Double.parseDouble(sScore) ;
		} catch (NumberFormatException e) {
			return sScore ;
		}

		Double dWeight = (double) 0 ; 

		try {
			dWeight = Double.parseDouble(_sWeight) ;
		} catch (NumberFormatException e) {
			return sScore ;
		}

		Double dScore = dContent * dWeight ;

		return Double.toString(dScore) ;
	}

	/**
	 * Return a FormDataData which value is filled with content
	 *
	 */
	public FormDataData getContent()
	{
		if (false == isActivated())
			return null ;

		FormDataData formData = new FormDataData() ;
		formData.setPath(_base.getPath()) ;
		formData.setValue(getContentAsString()) ;
		return formData ;
	}

	/**
	 * Initialize the Fit score control from a content and a default value
	 * 
	 * @param content       Information to be edited through this control
	 * @param sDefaultValue Miscellaneous parameters as set in the "value" archetype parameter for this control
	 */
	public void setContent(final FormDataData content, final String sDefaultValue)  
	{
		_base.parseParams(sDefaultValue) ;

		setContent(content) ;
	}

	/**
	 * Initialize the Fit score control from a content
	 * 
	 * @param content Information to be edited through this control
	 *
	 */
	public void setContent(final FormDataData content)
	{
		if (null == content)
		{
			if ("".equals(_base.getDefaultValue()))
				return ;
			initScoreFromContent(_base.getDefaultValue()) ;
		}

		initScoreFromContent(content.getValue()) ;

		refresh() ;
	}

	public void resetContent() {
		_sScore = "" ;
	}

	public String getPath() {   
		return _base.getPath() ;
	}

	/**
	 * Manage clicks on one of the N buttons
	 * 
	 * To be instantiated by subclasses
	 */
	protected void setChangeHandlers() {
	}

	/**
	 * Change from current score to a new target score (and update display)
	 */
	protected void ManageChange(final String sTarget) 
	{
		// If the score is already equals to the target, it means that the button was unselected
		//
		if (_sScore.equals(sTarget))
			_sScore = "" ;
		else
			_sScore = sTarget ;

		refresh() ;
	}

	/**
	 * To be instantiated by subclasses
	 */
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return null ;
	}

	/**
	 * Return a label that corresponds to current score
	 *
	 */
	public String getLabel() {
		return getLabelForScore(_sScore) ;
	}

	/**
	 * Return a (button) label that corresponds to a given score (for example "semi-fit" for "1" with a weight of 2)
	 *
	 * To be instantiated by subclasses
	 */
	protected String getLabelForScore(final String sScore) {
		return "" ;
	}

	/**
	 * Return a style that corresponds to current score
	 */
	public String getStyle() {
		return getStyleForScore(_sScore) ;
	}

	/**
	 * Return a (button) style that corresponds to a given score<br>
	 * (for example "fitred" (red background) or "fitredReadOnly" for "0")
	 *
	 * To be instantiated by subclasses
	 *
	 */
	public String getStyleForScore(final String sScore)
	{
		String sRO = "" ;
		if (_base.isReadOnly())
			sRO = "ReadOnly" ;

		return "fitwhite" + sRO ;
	}

	/**
	 * Content being the score multiplied by the weight, extract the score from a given content
	 * 
	 * @param sContent Total value of this control (score * weight)
	 */
	protected void initScoreFromContent(final String sContent)
	{
		_sScore = "" ;

		if ((null == sContent) || "".equals(sContent))
			return ;

		_sScore = sContent ;

		if ((null == _sWeight) || "".equals(_sWeight) || "1".equals(_sWeight))
			return ;

		Double dContent = (double) 0 ; 

		try {
			dContent = Double.parseDouble(_sScore) ;
		} catch (NumberFormatException e) {
			return ;
		}

		Double dWeight = (double) 0 ; 

		try {
			dWeight = Double.parseDouble(_sWeight) ;
		} catch (NumberFormatException e) {
			return ;
		}

		if (dWeight < (double) 0.001)
			return ;

		Double dScore = dContent / dWeight ;

		_sScore = Double.toString(dScore) ;
	}

	/**
	 * If the control is not already set (undefined score),
	 * and has the auto-unfit or auto-semi-fit on, initialize it depending on seniority
	 * 
	 * To be instantiated by subclasses
	 */
	public void initIfUnset(final String sSeniority) {
	}

	/**
	 * If the control has been "forced" to unfit due to seniority, mark it read only so that it can be
	 * published with the proper style
	 * 
	 * To be instantiated by subclasses
	 */
	public void initForScreenShotMode(final String sSeniority) {
	}

	/**
	 * Prepare the control to switch in Read Only mode
	 * 
	 * To be instantiated by subclasses
	 */
	protected void preInitializeInReadOnlyMode() {
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
	 * 
	 * To be instantiated by subclasses
	 */
	protected void preInitializeInReadWriteMode() {
	}

	public void setInitFromPrev(boolean bInitFromPrev) {
		_base.setInitFromPrev(bInitFromPrev) ;
	}

	public boolean getInitFromPrev() {
		return _base.getInitFromPrev() ;
	}

	public String getScore() {
		return _sScore ;
	}
	public void setScore(final String sScore)
	{
		if (null == sScore)
			_sScore = "" ;
		else
			_sScore = sScore ;
	}

	public String getWeight() {
		return _sWeight ;
	}

	public double getWeightAsDouble()
	{
		if ((null == _sWeight) || "".equals(_sWeight))
			return -1 ;

		try {
			return Double.parseDouble(_sWeight) ;
		} catch (NumberFormatException e) {
			return -1 ;
		}
	}

	public ControlBase getControlBase() {
		return _base ;
	}

	public boolean isReadOnly() {
		return _base.isReadOnly() ;
	}

	public String getExpectedSemiFit() {
		return _sExpectedSemiFit ;
	}

	public String getExpectedFit() {
		return _sExpectedFit ;
	}

	public boolean getAutoUnfit() {
		return _bAutoUnfit ;
	}

	public boolean getAutoSemifit() {
		return _bAutoSemiFit ;
	}
}
