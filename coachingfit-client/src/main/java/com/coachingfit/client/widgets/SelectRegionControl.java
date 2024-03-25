package com.coachingfit.client.widgets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.shared.database.RegionData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.ControlBaseWithParams;
import com.primege.client.widgets.ControlModel;
import com.primege.shared.database.FormDataData;

/**
 * TextBox with a drop down list that displays coaches and coaching trainees 
 * 
 * Inspired from http://sites.google.com/site/gwtcomponents/auto-completiontextbox
 */
public class SelectRegionControl extends ListBox implements ControlModel
{
	private final CoachingFitConstants constants = GWT.create(CoachingFitConstants.class) ;

	private ControlBaseWithParams _base ;

	private RegionData            _region ;
	
	private List<RegionData>      _aRegions ;
	
	/**
	 * Default Constructor
	 *
	 */
	public SelectRegionControl(final List<RegionData> aRegions, final RegionData region, final String sPath)
	{
		super() ;

		_base     = new ControlBaseWithParams(sPath) ;
		_aRegions = aRegions ;
		_region   = region ;

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
		
		_aRegions.clear() ;
		
		if ((null != _aRegions) && (false == _aRegions.isEmpty()))
			for (RegionData region : _aRegions)
				_aRegions.add(new RegionData(region)) ;
		
		if (null != _region)
			_aRegions.add(new RegionData(_region)) ;
		
		Comparator<RegionData> regionNameComparator = new Comparator<RegionData>()
		{
	    @Override
	    public int compare(RegionData left, RegionData right) {
	      return left.getLabel().compareTo(right.getLabel()) ;
	    }
		};

		Collections.sort(_aRegions, regionNameComparator) ;
		
		for (RegionData region : _aRegions)
			addItem(region.getLabel()) ;
	}

	/**
	 * Return the selected region, or <code>null</code> if none
	 */
	public RegionData getSelectedRegion()
	{
		String sSelectedRegion = getSelectedValue() ;

		if ("".equals(sSelectedRegion) || sSelectedRegion.equals(constants.Undefined()))
			return null ;

		if ((null == _aRegions) || _aRegions.isEmpty())
			return null ;

		for (RegionData region : _aRegions)
			if (sSelectedRegion.equals(region.getLabel()))
				return region ;

		return null ; 
	}

	/**
	 * Return the selected region identifier is any, or <code>-1</code> if none
	 */
	public int getSelectedRegionId()
	{
		RegionData selectedRegion = getSelectedRegion() ;
		
		if (null == selectedRegion)
			return -1 ;
		
		return selectedRegion.getId() ;
	}
		
	/**
	 * Get region information from it's identifier 
	 */
	public RegionData getRegionFromId(final int iRegionId)
	{
		if ((null == _aRegions) || _aRegions.isEmpty())
			return null ;

		for (RegionData region : _aRegions)
			if (region.getId() == iRegionId)
				return region ;

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
		if (null == content)
		{
			setItemSelected(0, true) ;
			return ;
		}

		String sRegionId = content.getValue() ;

		if ((null == sRegionId) || "".equals(sRegionId))
			sRegionId = _base.getDefaultValue() ;

		if ((null == sRegionId) || "".equals(sRegionId))
		{
			setItemSelected(0, true) ;
			return ;
		}

		RegionData region = getRegionFromId(Integer.parseInt(sRegionId)) ;
		if (null == region)
			return ;

		String sRegionLabel = region.getLabel() ;
		if ("".equals(sRegionLabel))
			return ;

		int iSize = getItemCount() ;
		for (int i = 0 ; i < iSize ; i++)
			if (getItemText(i).equals(sRegionLabel))
			{
				setItemSelected(i, true) ;
				return ;
			}
	}

	public void resetContent() {
		setItemSelected(0, true) ;
	}

	@Override
	public FormDataData getContent() 
	{
		int iSelectedRegionId = getSelectedRegionId() ;
		if (-1 == iSelectedRegionId)
			return null ;

		FormDataData formData = new FormDataData() ;
		formData.setPath(_base.getPath()) ;
		formData.setValue(Integer.toString(iSelectedRegionId)) ;
		return formData ;
	}
}
