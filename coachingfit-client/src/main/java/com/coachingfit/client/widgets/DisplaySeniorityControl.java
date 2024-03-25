package com.coachingfit.client.widgets;

import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.shared.util.CoachingFitDelay;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.primege.client.util.FormControlOption;
import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.ControlBaseWithParams;
import com.primege.client.widgets.ControlModel;
import com.primege.shared.database.FormDataData;

/**
 * TextBox
 * 
 */
public class DisplaySeniorityControl extends TextBox implements ControlModel
{
	protected ControlBaseWithParams _base ;
	protected String                _sContent ;

	protected final CoachingFitConstants localConstants = GWT.create(CoachingFitConstants.class) ;

	/**
	 * Default Constructor
	 *
	 */
	public DisplaySeniorityControl(String sPath)
	{
		super() ;

		_base = new ControlBaseWithParams(sPath) ;

		setReadOnly(true) ;
		_sContent = "" ;
	}

	public boolean isSingleData() {
		return true ;
	}

	/**
	 * Get content as FormDataData if content is an integer or "ND", <code>null</code> if not
	 *
	 */
	public FormDataData getContent()
	{
		if ("".equals(_sContent))
			return null ;

		FormDataData formData = new FormDataData() ;
		formData.setPath(_base.getPath()) ;
		formData.setValue(_sContent) ;

		return formData ;
	}

	/**
	 * Initialize state from a content and a default value
	 *
	 * @param content       FormDataData used to initialize the control
	 * @param sDefaultValue Configuration parameters, including default value in case there is no content 
	 */
	public void setContent(final FormDataData content, final String sDefaultValue)  
	{
		_base.parseParams(sDefaultValue) ;

		setContent(content) ;
	}

	/**
	 * Initialize state from a content
	 *
	 * @param content FormDataData used to initialize the control
	 */
	public void setContent(final FormDataData content)
	{
		if ((null == content) || (null == content.getValue()))
		{
			setText(_base.getDefaultValue()) ;
			_sContent = "" ;
			return ;
		}

		_sContent = content.getValue() ;

		displayContent() ;
	}

	public void resetContent()
	{
		setText(_base.getDefaultValue()) ;
		_sContent = "" ;
	}

	/**
	 * Set content's value
	 * 
	 * @param sSeniority Seniority in the YYMMDD format
	 */
	public void setSeniority(final String sSeniority)
	{
		if ((null == sSeniority) || "".equals(sSeniority))
			_sContent = "" ;
		else
			_sContent = sSeniority ;

		displayContent() ;
	}

	public ControlBase getControlBase() {
		return _base ;
	}

	public void setInitFromPrev(final boolean bInitFromPrev) {
		_base.setInitFromPrev(bInitFromPrev) ;
	}

	public boolean getInitFromPrev() {
		return _base.getInitFromPrev() ;
	}

	/**
	 * Display content in a natural language format (from 030200 to "3 years 2 months")
	 */
	public void displayContent() {
		setText(getLabel(_sContent, localConstants.generalYear(), localConstants.generalYears(), localConstants.generalMonth(), localConstants.generalMonths())) ;
	}

	/**
	 * Build the label in natural language 
	 * 
	 * @param sContent Delay in the YYMMDD format
	 * 
	 * @return The text in the "X years Y months" format if all went well, <code>""</code> if not
	 */
	public static String getLabel(final String sContent, final String sYearWord, final String sYearsWord, final String sMonthWord, final String sMonthsWord)
	{
		if ("".equals(sContent))
			return "?" ;

		CoachingFitDelay delay = new CoachingFitDelay(sContent) ;

		String sText = "" ;

		if (delay.getYears() > 0)
		{
			sText = "" + delay.getYears() ;
			if (delay.getYears() == 1)
				sText += " " + sYearWord ;
			else
				sText += " " + sYearsWord ;
		}

		if (delay.getMonths() > 0)
		{
			if (false == "".equals(sText))
				sText += " " ;

			sText += "" + delay.getMonths() ;
			if (delay.getMonths() == 1)
				sText += " " + sMonthWord ;
			else
				sText += " " + sMonthsWord ;
		}

		return sText ;
	}
}
