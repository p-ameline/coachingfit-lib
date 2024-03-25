package com.coachingfit.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.DoubleBox;

import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.ControlBaseWithParams;
import com.primege.client.widgets.ControlModel;
import com.primege.shared.database.FormDataData;

import java.text.ParseException;

/**
 * A textBox that contains the expected score for a block of controls
 * 
 */
public class GlobalExpectedScoreControl extends DoubleBox implements ControlModel
{
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

			if      ("target".equals(sParam))
				_sTargetRoot = sVal ; 
		}
	}

	protected LocalControlBase _base ;

	protected String           _sTargetRoot ;

	/**
	 * Default Constructor
	 *
	 */
	public GlobalExpectedScoreControl(String sPath)
	{
		super() ;

		_base        = new LocalControlBase(sPath) ;
		_sTargetRoot = "" ;

		setReadOnly(true) ;

		addIntegrityChecker() ;
	}

	public boolean isSingleData() {
		return true ;
	}

	/**
	 * Get content as FormDataData 
	 * 
	 * @return The FormDataData if content is a double or "ND", <code>null</code> if not
	 */
	public FormDataData getContent()
	{
		// Content is simply what appears in text box
		//
		String sContent = getText() ;
		if ("".equals(sContent))
			return null ;

		// if not specifically "undetermined", check if content is formated as a double
		//
		if (false == "ND".equals(sContent))
		{
			try {
				Double.parseDouble(sContent) ;
			} catch (NumberFormatException e) {
				return null ;
			}
		}

		FormDataData formData = new FormDataData() ;
		formData.setPath(_base.getPath()) ;
		formData.setValue(sContent) ;

		return formData ;
	}

	/**
	 * Initialize state from a content and a default value
	 * 
	 * @param content       Content to initialize from, pass <code>null</code> to reset
	 * @param sDefaultValue Control parameters initialization string (à la <code>"target="SCCR""</code>)
	 */
	public void setContent(final FormDataData content, final String sDefaultValue)  
	{
		_base.parseParams(sDefaultValue) ;

		setContent(content) ;
	}

	/**
	 * Initialize state from a content
	 * 
	 * @param content       Content to initialize from, pass <code>null</code> to reset
	 */
	public void setContent(final FormDataData content)
	{
		if ((null == content) || (null == content.getValue()))
			setText(_base.getDefaultValue()) ;
		else
			setText(content.getValue()) ;
	}

	public void resetContent() {
		setText("") ;
	}

	protected void paintItRegular() {
		paintIt("#FFFFFF") ;
	}

	protected void paintItWarning() {
		paintIt("#FF0000") ;
	}

	protected void paintIt(final String sBgColor)
	{
		Element contentElement = getElement() ;
		if (null == contentElement)
			return ;

		if ((null != sBgColor) && (false == "".equals(sBgColor)))
			contentElement.getStyle().setBackgroundColor(sBgColor) ;
	}

	protected void addIntegrityChecker()
	{
		addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				try 
				{
					String sContent = getText() ;
					if ("ND".equals(sContent))
					{
						paintItRegular() ;
						return ;
					}

					getValueOrThrow() ;
					paintItRegular() ;
				} 
				catch(ParseException e) {
					paintItWarning() ;
				}
			}
		});
	}

	/**
	 * Add an increment to score's current value
	 * 
	 * @param dIncrement The increment as a double
	 * 
	 * @return The incremented value
	 */
	public String addToContent(final double dIncrement)
	{
		double dFinalValue = getContentAsDouble() + dIncrement ;
		String sNewContent = "" + dFinalValue ; 

		setText(sNewContent) ;

		return sNewContent ;
	}

	/**
	 * Get content as a double 
	 * 
	 * @return <code>0</code> if content is <code>""</code> or <code>"ND"</code> or not a double, the value if a double
	 */
	protected double getContentAsDouble()
	{
		String sContent = getText() ;
		if ("".equals(sContent) || "ND".equals(sContent))
			return 0 ;

		try {
			return Double.parseDouble(sContent) ;
		} catch (NumberFormatException e) {
			return 0 ;
		}
	}

	public ControlBase getControlBase() {
		return _base ;
	}

	public String getTargetRoot() {
		return _sTargetRoot ;
	}

	public void setInitFromPrev(final boolean bInitFromPrev) {
		_base.setInitFromPrev(bInitFromPrev) ;
	}

	public boolean getInitFromPrev() {
		return _base.getInitFromPrev() ;
	}
}
