package com.coachingfit.client.widgets;

import java.util.List;

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
public class SelectZoneControl extends ListBox implements ControlModel
{
	private final CoachingFitConstants constants = GWT.create(CoachingFitConstants.class) ;

	public static class ZoneData
	{
		protected String _sZones ;
		protected String _sLabel ;

		public ZoneData(String sZones) {
			initFromZones(sZones) ;
		}

		public String getZones() { return _sZones ; }
		public String getLabel() { return _sLabel ; }

		public void initFromZones(final String sZones)
		{
			_sZones = sZones ;
			_sLabel = "" ;

			if (null == sZones)
				return ;

			int iZoneLen = _sZones.length() ;
			if (0 == iZoneLen)
				return ;

			if (1 == iZoneLen)
				_sLabel = "Zone " ;
			else
				_sLabel = "Zones " ;

			for (int i = 0 ; i < iZoneLen ; i++)
			{
				if (i > 0)
				{
					if (i == iZoneLen - 1)
						_sLabel += " et " ;
					else
						_sLabel += ", " ;
				}
				_sLabel += sZones.substring(i, i + 1) ;
			}
		}
	}

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
			if      ("zones".equalsIgnoreCase(sParam))
				_sAllowedZones = sVal ; 
		}
	}

	private LocalControlBase _base ;
	private List<ZoneData>   _aZones ;

	private String           _sAllowedZones ;

	/**
	 * Default Constructor
	 *
	 */
	public SelectZoneControl(List<ZoneData> aRegions, final String sPath)
	{
		super() ;

		_base   = new LocalControlBase(sPath) ;
		_aZones = aRegions ;

		init() ;

		setVisibleItemCount(1) ;
		setItemSelected(0, true) ;
	}

	public boolean isSingleData() {
		return true ;
	}

	/**
	 * Initialize the list with trainees - the first being "undefined" 
	 *
	 */
	public void init()
	{
		addItem(constants.Undefined()) ;

		if ((null == _aZones) || _aZones.isEmpty())
			return ;

		for (ZoneData zone : _aZones)
			addItem(zone.getLabel()) ;
	}

	/**
	 * Return the selected region Id is any, or <code>-1</code> if none
	 *
	 */
	public String getSelectedRegionId()
	{
		String sSelectedRegion = getSelectedValue() ;

		if ("".equals(sSelectedRegion) || sSelectedRegion.equals(constants.Undefined()))
			return "" ;

		for (ZoneData region : _aZones)
			if (sSelectedRegion.equals(region.getLabel()))
				return region.getZones() ;

		return "" ; 
	}

	/**
	 * Get region information from it's identifier 
	 *
	 */
	public ZoneData getZoneFromId(final String sZoneId)
	{
		if ((null == _aZones) || _aZones.isEmpty())
			return null ;

		for (ZoneData zone : _aZones)
			if (zone.getZones().equals(sZoneId))
				return zone ;

		return null ;
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
		String sRegionId = "" ;
		if (null == content)
			sRegionId = _base.getDefaultValue() ;
		else
			sRegionId = content.getValue() ;

		if ((null == sRegionId) || "".equals(sRegionId))
		{
			setItemSelected(0, true) ;
			return ;
		}

		ZoneData zone = getZoneFromId(sRegionId) ;
		if (null == zone)
			return ;

		String sZoneLabel = zone.getLabel() ;		
		if ("".equals(sZoneLabel))
			return ;

		int iSize = getItemCount() ;
		for (int i = 0 ; i < iSize ; i++)
			if (getItemText(i).equals(sZoneLabel))
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
		String sSelectedRegionId = getSelectedRegionId() ;
		if ("".equals(sSelectedRegionId))
			return null ;

		FormDataData formData = new FormDataData() ;
		formData.setPath(_base.getPath()) ;
		formData.setValue(sSelectedRegionId) ;
		return formData ;
	}
}
