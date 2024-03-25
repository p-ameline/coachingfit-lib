package com.coachingfit.shared.rpc;

import com.primege.shared.model.DashboardTable;

import net.customware.gwt.dispatch.shared.Action;

public class GetCoachingFitDashboardTableAction implements Action<GetCoachingFitDashboardTableResult> 
{	
	private int            _iUserId ;
	private int            _iPivotId ;
	private String         _sYear ;
	private boolean        _bOnlyAuthoredInfo ;
	
	private DashboardTable _table ;
	
	/**
	 * Default constructor (with zero information)
	 */
	public GetCoachingFitDashboardTableAction() 
	{
		super() ;
		
		_iUserId           = -1 ;
		_iPivotId          = -1 ;
		_sYear             = "" ;
		_bOnlyAuthoredInfo = true ;
		_table             = new DashboardTable() ;
	}
	
	/**
	 * Constructor 
	 */
	public GetCoachingFitDashboardTableAction(final int iUserId, final int iPivotId, final String sYear, final boolean bOnlyAuthoredInfo, final DashboardTable table) 
	{
		super() ;
		
		_iUserId           = iUserId ;
		_iPivotId          = iPivotId ;
		_sYear             = sYear ;
		_bOnlyAuthoredInfo = bOnlyAuthoredInfo ;
		_table             = new DashboardTable(table) ;
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
	
	public DashboardTable getTable() {
		return _table ;
	}
}
