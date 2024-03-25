package com.coachingfit.shared.util;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Manage delays in a years, months and days format
 * 
 * @author Philippe
 *
 */
public class CoachingFitDelay implements IsSerializable
{
	protected int _iYears ;
	protected int _iMonths ;
	protected int _iDays ;
	
	/**
	 * Plain vanilla constructor 
	 */
	public CoachingFitDelay(final int iYears, final int iMonths, final int iDays)
	{
		_iYears  = iYears ;
		_iMonths = iMonths ;
		_iDays   = iDays ;
	}
	
	/**
	 * Constructor from a string
	 * 
	 * @param sDelay YYMMDD formated string
	 */
	public CoachingFitDelay(final String sDelay) {
		initFromString(sDelay) ;
	}
	
	/**
	 * Zero-args constructors - needed for serializable objects 
	 */
	public CoachingFitDelay() {
		init() ;
	}
	
	/**
	 * Initialize as a zero duration delay 
	 * 
	 **/
	public void init()
	{ 
		_iYears  = 0 ;
		_iMonths = 0 ;
		_iDays   = 0 ;
	}
	
	/**
	 * Initialize from a string
	 * 
	 * @param sDelay YYMMDD formated string 
	 **/
	public void initFromString(final String sDelay)
	{
		init() ;
		
		// Check length
		if ((null == sDelay) || (sDelay.length() != 6))
			return ;
			
		// Check that there are digits only
		for (int i = 0 ; i < 6 ; i++)
			if (false == Character.isDigit(sDelay.charAt(i)))
				return ;
			
		_iYears  = Integer.parseInt(sDelay.substring(0, 2)) ;
		_iMonths = Integer.parseInt(sDelay.substring(2, 4)) ;
		_iDays   = Integer.parseInt(sDelay.substring(4, 6)) ;
		
		normalize() ;
	}
	
	/**
	 * Initialize from a string
	 * 
	 * @param sDelay YYMMDD formated string 
	 **/
	public void initFromDateInterval(final CoachingFitDate dateFrom, final CoachingFitDate dateTo)
	{
		init() ;
		
		if ((null == dateFrom) || (null == dateTo))
			return ;
		
		// TODO check that dateTo > dateFrom
		//
		_iYears = dateTo.getYear() - dateFrom.getYear() ;
		
		if      (dateTo.getMonth() > dateFrom.getMonth())
			_iMonths = dateTo.getMonth() - dateFrom.getMonth() ;
		else if (dateTo.getMonth() < dateFrom.getMonth())
		{
			_iYears-- ;
			_iMonths = dateTo.getMonth() + 12 - dateFrom.getMonth() ;
		}
		
		if      (dateTo.getDay() > dateFrom.getDay())
			_iDays = dateTo.getDay() - dateFrom.getDay() ;
		else if (dateTo.getDay() < dateFrom.getDay())
		{
			_iMonths-- ;
			if (_iMonths < 0)
			{
				_iMonths = 11 ;
				_iYears-- ;
			}
			
			_iDays = dateTo.getDay() + dateFrom.daysCountWithinMonth() - dateFrom.getDay() ;
		}
	}
	
	/**
	 * Get this delay as a YYMMDD string 
	 * 
	 * @return The YYMMDD string if all went well, <code>""</code> if not
	 */
	public String getAsString() 
	{
		String sReturn = get2DigitsString(_iYears) + get2DigitsString(_iMonths) + get2DigitsString(_iDays) ; 
		
		if (sReturn.length() == 6)
			return sReturn ;
		
		return "" ;
	}
	
	/**
	 * Return an int as a 2 digits strings (i.e. 5 -> "05")
	 * 
	 * @return The two digits strings if 0 <= iCount < 10, <code>""</code> if not 
	 */
	public static String get2DigitsString(int iCount)
	{
		if ((iCount < 0) || (iCount > 99))
			return "" ;
		
		if (iCount < 10)
			return "0" + iCount ;
		else
			return "" + iCount ;
	}

	/**
	 * Make certain that the delay is "normal" (days count < 31, months count < 12) and, if not, normalize it
	 */
	protected void normalize()
	{
		// Normalizing days is based on 31 days long months (could be 30 or 30.5)
		//
		if (_iDays > 30)
		{
			_iMonths += Math.floorDiv(_iDays, 31) ;
			_iDays = Math.floorMod(_iDays, 31) ;
		}
		
		if (_iMonths > 11)
		{
			_iYears += Math.floorDiv(_iMonths, 12) ;
			_iMonths = Math.floorMod(_iMonths, 12) ;
		}
	}

	// Getters and Setters
	//
	public int getYears() {
		return _iYears ;
	}
	public void setYears(int iYears) {
		_iYears = iYears ;
	}

	public int getMonths() {
		return _iMonths ;
	}
	public void setMonths(int iMonths) {
		_iMonths = iMonths ;
	}

	public int getDays() {
		return _iDays ;
	}
	public void setDays(int iDays) {
		_iDays = iDays ;
	}
}
