package com.coachingfit.shared.rpc;

import com.primege.shared.model.DashboardChart;

import net.customware.gwt.dispatch.shared.Action;

public class GetCoachingFitDashboardChartAction implements Action<GetCoachingFitDashboardChartResult> 
{	
	private int            _iUserId ;
	private int            _iCoachId ;
	private int            _iPivotId ;
	private String         _sYear ;
	private boolean        _bOnlyAuthoredInfo ;
	
	private DashboardChart _chart ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitDashboardChartAction() 
	{
		super() ;
		
		_iUserId           = -1 ;
		_iCoachId          = -1 ;
		_iPivotId          = -1 ;
		_sYear             = "" ;
		_bOnlyAuthoredInfo = true ;
		_chart             = new DashboardChart() ;
	}
	
	/**
	 * Constructor 
	 */
	public GetCoachingFitDashboardChartAction(final int iUserId, final int iPivotId, final int iCoachId, final String sYear, final boolean bOnlyAuthoredInfo, final DashboardChart chart) 
	{
		super() ;
		
		_iUserId           = iUserId ;
		_iCoachId          = iCoachId ;
		_iPivotId          = iPivotId ;
		_sYear             = sYear ;
		_bOnlyAuthoredInfo = bOnlyAuthoredInfo ;
		_chart             = new DashboardChart(chart) ;
	}
	
	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}
	
	public String getYear() {
		return _sYear ;
	}
	
	public boolean onlyAuthoredInfo() {
		return _bOnlyAuthoredInfo ;
	}
	
	public int getPivotId() {
		return _iPivotId ;
	}
	
	public int getCoachId() {
		return _iCoachId ;
	}
	
	public DashboardChart getChart() {
		return _chart ;
	}
}
