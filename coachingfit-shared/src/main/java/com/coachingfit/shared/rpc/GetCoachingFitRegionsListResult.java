package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import com.coachingfit.shared.database.RegionData;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Object that return information from a query for a list of regions<br>
 * <br>
 * Author: PA<br>
 */
public class GetCoachingFitRegionsListResult implements Result 
{
	private List<RegionData> _aRegionsData = new ArrayList<>() ;
	private String           _sMessage ;
	
	public GetCoachingFitRegionsListResult()
	{
		super() ;
		
		_sMessage = "" ;
	}
	
	public GetCoachingFitRegionsListResult(final String sMessage, final List<RegionData> aRegionsData) 
	{
		_sMessage = sMessage ;
		
		setRegionsData(aRegionsData) ;
	}

	public List<RegionData> getRegionsData() {
		return _aRegionsData  ;
	}
	public void setRegionsData(final List<RegionData> aRegionsData)
	{
		_aRegionsData.clear() ;
		
		if ((null == aRegionsData) || aRegionsData.isEmpty())
			return ;
		
		for (RegionData region : aRegionsData)
			_aRegionsData.add(new RegionData(region)) ;
	}
	public void addRegionData(final RegionData regionData)
	{
		if (null == regionData)
			return ;
		
		_aRegionsData.add(new RegionData(regionData)) ;
	}
	
	public String getMessage() {
  	return _sMessage ;
  }
}
