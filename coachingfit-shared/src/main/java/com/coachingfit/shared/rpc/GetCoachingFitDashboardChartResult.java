package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.Iterator;

import com.primege.shared.model.DashboardChart;
import com.primege.shared.model.DashboardTableLine;

import net.customware.gwt.dispatch.shared.Result;

public class GetCoachingFitDashboardChartResult implements Result 
{
	private DashboardTableLine            _titleLine ; 
	private ArrayList<DashboardTableLine> _aLines    = new ArrayList<DashboardTableLine>() ;
	
	private DashboardChart _chart ;
	private String         _sMessage ; 
	
	public GetCoachingFitDashboardChartResult()
	{
		super() ;
		
		_titleLine = null ;
		_chart     = null ;
		_sMessage  = "" ;
	}
	
	public GetCoachingFitDashboardChartResult(final String sMessage) 
	{
		super() ;
		
		_titleLine = null ;
		_chart     = null ;
		_sMessage  = sMessage ;
	}

	public DashboardChart getDashboardChart() {
  	return _chart ;
  }
	public void setDashboardChart(final DashboardChart dashboardChart) 
	{
		if (null == _chart)
			_chart = new DashboardChart(dashboardChart) ;
		else
			_chart.initFromDashboardTable(dashboardChart) ;
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
