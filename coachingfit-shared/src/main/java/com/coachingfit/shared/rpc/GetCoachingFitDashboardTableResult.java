package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.Iterator;

import com.primege.shared.model.DashboardTable;
import com.primege.shared.model.DashboardTableLine;

import net.customware.gwt.dispatch.shared.Result;

public class GetCoachingFitDashboardTableResult implements Result 
{
	private DashboardTableLine            _titleLine ; 
	private ArrayList<DashboardTableLine> _aLines    = new ArrayList<DashboardTableLine>() ;
	
	private DashboardTable _table ;
	private String         _sMessage ; 
	
	public GetCoachingFitDashboardTableResult()
	{
		super() ;
		
		_titleLine = null ;
		_table     = null ;
		_sMessage  = "" ;
	}
	
	public GetCoachingFitDashboardTableResult(final String sMessage) 
	{
		super() ;
		
		_titleLine = null ;
		_table     = null ;
		_sMessage  = sMessage ;
	}

	public DashboardTable getDashboardTable() {
  	return _table ;
  }
	public void setDashboardTable(final DashboardTable dashboardTable) 
	{
		if (null == _table)
			_table = new DashboardTable(dashboardTable) ;
		else
			_table.initFromDashboardTable(dashboardTable);
  }

	public String getMessage() {
  	return _sMessage ;
  }
	public void setMessage(final String sMessage) {
  	_sMessage = sMessage ;
  }
	
	public ArrayList<DashboardTableLine> getLines() {
		return _aLines ;
	}
	public void addLine(final DashboardTableLine tableLine)
	{
		if (null == tableLine)
			return ;
		
		_aLines.add(new DashboardTableLine(tableLine)) ;
	}
	public void setLines(final ArrayList<DashboardTableLine> aLines)
	{
		_aLines.clear() ;
		
		if ((null == aLines) || aLines.isEmpty())
			return ;
		
		for (Iterator<DashboardTableLine> it = aLines.iterator() ; it.hasNext() ; )
			_aLines.add(new DashboardTableLine(it.next())) ;
	}
	
	public DashboardTableLine getTitleLine() {
		return _titleLine ;
	}
	public void setTitleLine(final DashboardTableLine line) 
	{
		if (null == _titleLine)
			_titleLine = new DashboardTableLine(line) ;
		else
			_titleLine.initFromOther(line) ;
	}
}
