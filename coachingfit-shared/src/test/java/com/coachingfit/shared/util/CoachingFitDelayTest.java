package com.coachingfit.shared.util ;

import junit.framework.TestCase ;

public class CoachingFitDelayTest extends TestCase
{
  public void testConstructors() 
  {
  	// Test default constructor
  	//
  	CoachingFitDelay delay1 = new CoachingFitDelay(20, 3, 2) ;
  	assertTrue(delay1.getYears()  == 20) ;
  	assertTrue(delay1.getMonths() == 3) ;
  	assertTrue(delay1.getDays()   == 2) ;
  	
  	// Test no-args constructor
  	//
  	CoachingFitDelay delay2 = new CoachingFitDelay() ;
  	assertTrue(delay2.getYears()  == 0) ;
  	assertTrue(delay2.getMonths() == 0) ;
  	assertTrue(delay2.getDays()   == 0) ;
  	
  	// Test constructor from a string
   	//
   	CoachingFitDelay delay3 = new CoachingFitDelay("150512") ;
   	assertTrue(delay3.getYears()  == 15) ;
   	assertTrue(delay3.getMonths() == 5) ;
   	assertTrue(delay3.getDays()   == 12) ;
   	
   	// Test invalid constructors from a string
   	//
   	CoachingFitDelay delay4 = new CoachingFitDelay("15051203") ;
   	assertTrue(delay4.getYears()  == 0) ;
   	assertTrue(delay4.getMonths() == 0) ;
   	assertTrue(delay4.getDays()   == 0) ;
   	
   	// Test invalid constructors from a string
   	//
   	CoachingFitDelay delay5 = new CoachingFitDelay("ABCDEF") ;
   	assertTrue(delay5.getYears()  == 0) ;
   	assertTrue(delay5.getMonths() == 0) ;
   	assertTrue(delay5.getDays()   == 0) ;
  }
  
  public void testDateInterval() 
  {
  	CoachingFitDate dateFrom = new CoachingFitDate("20170517") ;
  	CoachingFitDate dateTo   = new CoachingFitDate("20191125") ;
  	
  	CoachingFitDelay delay = new CoachingFitDelay() ;
  	delay.initFromDateInterval(dateFrom, dateTo) ;
  	
  	assertEquals(delay.getAsString(), "020608") ;
  	
  	CoachingFitDate dateFrom2 = new CoachingFitDate("20181231") ;
  	CoachingFitDate dateTo2   = new CoachingFitDate("20190101") ;
  	
  	CoachingFitDelay delay2 = new CoachingFitDelay() ;
  	delay2.initFromDateInterval(dateFrom2, dateTo2) ;
  	
  	assertEquals(delay2.getAsString(), "000001") ;
  }
  
  public void testNormalize() 
  {
  	CoachingFitDelay delay = new CoachingFitDelay("001800") ;
  	assertEquals(delay.getAsString(), "010600") ;
  	  	
  	CoachingFitDelay delay2 = new CoachingFitDelay("002400") ;
  	assertEquals(delay2.getAsString(), "020000") ;
  }
}
