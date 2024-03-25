package com.coachingfit.server.handler ;

import junit.framework.TestCase ;

public class GetCoachingFitDashboardChartHandlerTest extends TestCase
{
  public void testConstructors() 
  {
  }
  
  public void testSubstractMonths() 
  {
  	assertEquals("",         GetCoachingFitDashboardChartHandler.substractMonths(null, 0)) ;
  	assertEquals("2018",     GetCoachingFitDashboardChartHandler.substractMonths("2018", 0)) ;
  	assertEquals("20181231", GetCoachingFitDashboardChartHandler.substractMonths("20181231", 0)) ;
  	
  	assertEquals("20180801", GetCoachingFitDashboardChartHandler.substractMonths("20181101", 3)) ;
  	assertEquals("20180801", GetCoachingFitDashboardChartHandler.substractMonths("20191101", 15)) ;
  	assertEquals("20180801", GetCoachingFitDashboardChartHandler.substractMonths("20201101", 27)) ;
  }
  
  public void testAddMonths() 
  {
  	assertEquals("",         GetCoachingFitDashboardChartHandler.addMonths(null, 0)) ;
  	assertEquals("2018",     GetCoachingFitDashboardChartHandler.addMonths("2018", 0)) ;
  	assertEquals("20181231", GetCoachingFitDashboardChartHandler.addMonths("20181231", 0)) ;
  	
  	assertEquals("20180201", GetCoachingFitDashboardChartHandler.addMonths("20171101", 3)) ;
  	assertEquals("20180201", GetCoachingFitDashboardChartHandler.addMonths("20161101", 15)) ;
  	assertEquals("20180201", GetCoachingFitDashboardChartHandler.addMonths("20151101", 27)) ;
  }
}
