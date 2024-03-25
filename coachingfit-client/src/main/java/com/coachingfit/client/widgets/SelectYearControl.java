package com.coachingfit.client.widgets;

import java.util.Date;

import com.coachingfit.client.loc.CoachingFitConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;
import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.ControlBaseWithParams;
import com.primege.client.widgets.ControlModel;
import com.primege.shared.database.FormDataData;

/**
 * TextBox with a drop down list from Lexicon
 * 
 * Inspired from http://sites.google.com/site/gwtcomponents/auto-completiontextbox
 */
public class SelectYearControl extends ListBox implements ControlModel
{
	protected final CoachingFitConstants constants = GWT.create(CoachingFitConstants.class) ;

	protected ControlBaseWithParams _base ;
	protected int                   _iStartYear ;

	/**
	 * Default Constructor
	 *
	 */
	public SelectYearControl(int iStartYear, final String sPath)
	{
		super() ;

		_base       = new ControlBaseWithParams(sPath) ;
		_iStartYear = iStartYear ;

		init() ;

		setVisibleItemCount(1) ;
	}

	/**
	 * Initialize the list with a set of years 
	 */
	public void init()
	{
		Date tNow = new Date() ;
		@SuppressWarnings("deprecation")
		int iYearIndex = tNow.getYear() + 1900 ;

		if (_iStartYear < iYearIndex)
		{
			for (int i = _iStartYear ; i <= iYearIndex ; i++) 
				addItem("" + i) ;
			setItemSelected(iYearIndex - _iStartYear, true) ;
		}
		else if (_iStartYear == iYearIndex)
		{
			addItem("" + iYearIndex) ;
			setItemSelected(0, true) ;
		}
		else if (_iStartYear > iYearIndex)
		{
			for (int i = iYearIndex ; i <= _iStartYear ; i++) 
				addItem("" + i) ;
			setItemSelected(0, true) ;
		}
	}

	public boolean isSingleData() {
		return true ;
	}

	public ControlBase getControlBase() {
		return _base ;
	}

	public void setInitFromPrev(boolean bInitFromPrev) {
		_base.setInitFromPrev(bInitFromPrev) ;
	}

	public boolean getInitFromPrev() {
		return _base.getInitFromPrev() ;
	}

	/**
	 * Initialize state from a content and a default value
	 *
	 * @param content       FormDataData used to initialize the control
	 * @param sDefaultValue Configuration parameters, including default value in case there is no content 
	 */
	@Override
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
	@Override
	public void setContent(final FormDataData content)
	{
		String sYear = "" ;
		if (null == content)
			sYear = _base.getDefaultValue() ;
		else
			sYear = content.getValue() ;

		if ((null == sYear) || "".equals(sYear))
		{
			setItemSelected(0, true) ;
			return ;
		}

		int iSize = getItemCount() ;
		for (int i = 0 ; i < iSize ; i++)
			if (getItemText(i).equals(sYear))
			{
				setItemSelected(i, true) ;
				return ;
			}
	}

	@Override
	public void resetContent() {
		setItemSelected(0, true) ;
	}

	@Override
	public FormDataData getContent() 
	{
		String sSelectedYear = getSelectedValue() ;

		FormDataData formData = new FormDataData() ;
		formData.setPath(_base.getPath()) ;
		formData.setValue(sSelectedYear) ;
		return formData ;
	}
}
