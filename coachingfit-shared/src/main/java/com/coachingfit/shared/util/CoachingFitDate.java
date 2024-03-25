package com.coachingfit.shared.util;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Manage delays in a years, months and days format
 * 
 * @author Philippe
 *
 */
public class CoachingFitDate implements IsSerializable
{
	protected int _iYear ;
	protected int _iMonth ;
	protected int _iDay ;
	
	private static int[][] daytab = {
	    {0,31,28,31,30,31,30,31,31,30,31,30,31},
	    {0,31,29,31,30,31,30,31,31,30,31,30,31}
	  } ;
	
	/**
	 * Plain vanilla constructor 
	 */
	public CoachingFitDate(final int iYear, final int iMonth, final int iDay)
	{
		_iYear  = iYear ;
		_iMonth = iMonth ;
		_iDay   = iDay ;
	}
	
	/**
	 * Constructor from a string
	 * 
	 * @param sDate YYYYMMDD formated string
	 */
	public CoachingFitDate(final String sDate) {
		initFromString(sDate) ;
	}
	
	/**
	 * Zero-args constructors - needed for serializable objects 
	 */
	public CoachingFitDate() {
		init() ;
	}
	
	/**
	 * Initialize as a zero duration delay 
	 * 
	 **/
	public void init()
	{ 
		_iYear  = 0 ;
		_iMonth = 0 ;
		_iDay   = 0 ;
	}
	
	/**
	 * Initialize from a string
	 * 
	 * @param sDate YYYYMMDD formated string 
	 **/
	public void initFromString(final String sDate)
	{
		init() ;
		
		// Check length
		if ((null == sDate) || (sDate.length() != 8))
			return ;
			
		// Check that there are digits only
		for (int i = 0 ; i < 8 ; i++)
			if (false == Character.isDigit(sDate.charAt(i)))
				return ;
			
		_iYear  = Integer.parseInt(sDate.substring(0, 4)) ;
		_iMonth = Integer.parseInt(sDate.substring(4, 6)) ;
		_iDay   = Integer.parseInt(sDate.substring(6, 8)) ;
	}

	/**
	 * Initialize as today
	 **/
	@SuppressWarnings("deprecation")
	public void initAsToday()
	{
		init() ;
		
		// Get current date
		//
		Date tNow = new Date() ;
				
		// So far, neither SimpleDateFormat nor Calendar are available with GWT
		// We have to use deprecated methods from Date
		//
		_iYear  = tNow.getYear() + 1900 ;
		_iMonth = tNow.getMonth() + 1 ;
		_iDay   = tNow.getDate() ;
	}

	/**
	 * Is it leap year?
	 * 
	 * @return <code>true</code> if a leap year, <code>false</code> if not
	 **/
	public boolean isLeapYear()
	{
		// Rule : If year is a multiple of 4 AND not a multiple of 100 OR a multiple of 400, it is a leap year
		//
		return (_iYear % 4) == 0 && (_iYear % 100 != 0 || _iYear % 400 == 0) ;
	}
	
	/**
	 * Get the count of days (in the 28 - 31 interval) in the given month  
	 * 
	 * @return The count of days if month is valid, <code>0</code> if not 
	 **/
	public int daysCountWithinMonth()
	{
		if ((_iMonth < 1) || (_iMonth > 12))
			return 0 ;
		
		int iLeap = 0 ;
		if ((2 == _iMonth) && isLeapYear())   // no need to evaluate leap year if month is not February
			iLeap++ ;
		
		return daytab[iLeap][_iMonth] ;
	}
	
	// Getters and Setters
	//
	public int getYear() {
		return _iYear ;
	}
	public void setYear(int iYear) {
		_iYear = iYear ;
	}

	public int getMonth() {
		return _iMonth ;
	}
	public void setMonth(int iMonth) {
		_iMonth = iMonth ;
	}

	public int getDay() {
		return _iDay ;
	}
	public void setDay(int iDay) {
		_iDay = iDay ;
	}
}
