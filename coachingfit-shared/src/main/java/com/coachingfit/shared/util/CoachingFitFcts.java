package com.coachingfit.shared.util;

/**
 */
public class CoachingFitFcts
{	
	/**
	 * Get the delay between a reference date and now
	 * 
	 * @param sReferenceDate The reference date (as a YYYYMMDD string)
	 * 
	 * @return The delay as a YYMMDD string is all went well, or <code>""</code> if not
	 */
	public static String getTimeSince(final String sReferenceDate)
	{
		// Check if the reference date is OK
		//
		if ((null == sReferenceDate) || "".equals(sReferenceDate))
			return "" ;
		
		CoachingFitDate dateFrom = new CoachingFitDate(sReferenceDate) ;
		
		// Date to is now
		//
		CoachingFitDate dateTo = new CoachingFitDate() ;
		dateTo.initAsToday() ;
		
		// Get date interval
		//
		CoachingFitDelay delay = new CoachingFitDelay() ;
		delay.initFromDateInterval(dateFrom, dateTo) ;
		
		return delay.getAsString() ;
	}
	
	/**
	 * Is the string a proper date
	 * 
	 * @param sDate a date, in the YYYYMMDD form 
	 * 
	 * @return <code>true</code> if the string is properly formated for a date, <code>false</code> if not 
	 */
	public static boolean checkProperDateString(final String sDate)
	{
		if (null == sDate)
			return false ;
		
		if (false == sDate.matches("\\d+"))
    	return false ;
		
		return true ;
	}
}
