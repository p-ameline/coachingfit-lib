package com.coachingfit.shared.util;

/**
 * <p>
 * FieldVerifier validates that the name the user enters is valid.
 * </p>
 * <p>
 * This class is in the <code>shared</code> packing because we use it in both
 * the client code and on the server. Client side, we verify that the name is
 * valid before sending an RPC request so the user doesn't have to wait for a
 * network round trip to get feedback. Server side, we verify that the name is
 * correct to ensure that the input is correct regardless of where the RPC
 * originates.
 * </p>
 * <p>
 * When creating a class that is used on both the client and the server, be sure
 * that all code is translatable and does not use native JavaScript. Code that
 * is not translatable (such as code that interacts with a database or the file
 * system) cannot be compiled into client side JavaScript. Code that uses native
 * JavaScript (such as Widgets) cannot be run on the server.
 * </p>
 */
public class MiscellanousFcts {

	// private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String EMAIL_LEFT_PATTERN  = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.-_" ;
	private static final String EMAIL_RIGHT_PATTERN = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ." ;
		
	public static enum STRIP_DIRECTION { stripLeft, stripRight, stripBoth } ;
	
	/**
	 * Verifies that the specified mail address is valid
	 * 	  
	 * @param name the name to validate
	 * @return true if valid, false if invalid
	 */
	public static boolean isValidMailAddress(String sMail) 
	{
		if ((null == sMail) || (sMail.equals(""))) 
			return false ;
		
		// Check that there is a '@' and only one
		//
		String[] tokens = sMail.split("@") ;
    if ((2 != tokens.length) || tokens[0].equals("") || tokens[1].equals("")) 
    	return false ;

    // Test if chars are valid
    //
    if ((false == isValidString(tokens[0], EMAIL_LEFT_PATTERN, false)) ||
        (false == isValidString(tokens[1], EMAIL_RIGHT_PATTERN, false)))
    	return false ;
    
    // Check '.' position validity
		//
    if ((tokens[0].charAt(tokens[0].length() - 1) == '.') ||
    		(tokens[1].charAt(tokens[1].length() - 1) == '.'))
    	return false ;
    
    String[] left  = tokens[0].split("\\.") ;
		String[] right = tokens[1].split("\\.") ;
    
		int iLL = left.length ;
		int iRL = right.length ;
		
		// Check that there is a '.' or two to the right
		//
		if ((iRL < 2) || (iRL > 3))    	
			return false ;
		
		// Check that '.' are not contiguous, and not beginning and not terminating
		//
		for (int i = 0 ; i < iRL ; i++)
			if (right[i].equals(""))
				return false ;
		
		for (int i = 0 ; i < iLL ; i++)
			if (left[i].equals(""))
				return false ;
    
		// Check that final block is a valid extension (at least 2 chars, and if 2, no digit) 
    //
    if (right[iRL-1].length() < 2)
    	return false ;
        
    if ((right[iRL-1].length() == 2) && (Character.isDigit(right[iRL-1].charAt(0)) ||
    		                                 Character.isDigit(right[iRL-1].charAt(1))))
    	return false ;
   
    return true ;
	}
	
	/**
	 * Check if both Strings are equal and not null
	 * 	  
	 * @return true if valid, false if invalid
	 */
	public static boolean areIdenticalStrings(String element, String confirmedElement) 
	{
		if ((null == element) || (null == confirmedElement))
			return false ;
		
		return element.equals(confirmedElement) ;
	}
	
	/**
	 * Returns a formated date (à la dd/MM/yyyy) from a native date (yyyyMMdd)
	 * 	  
	 * @param sNativeDate the native date (whose format is yyyyMMdd), for example "20151202"
	 * @param sDateFormat the format to conform to, for example "dd/MM/yyyy"
	 * 
	 * @return "" if something went wrong or the formated date, if successful (for example "02/12/2015") 
	 */
	public static String dateFromNativeToFormated(final String sNativeDate, final String sDateFormat)
	{
		if ((null == sDateFormat) || "".equals(sDateFormat))
			return "" ;
		
		// null or "" are considered as 00000000
		//
		String sWorkDate = "00000000" ;
		if ((null != sNativeDate) && (false == "".equals(sNativeDate)))
			sWorkDate = sNativeDate ;
		
		// If even not the length or yyyy (ie 4) or not only digits, better fail
		//
		if ((false == isDigits(sWorkDate)) || (sWorkDate.length() < 4))
			return "" ;
		
		// Completing to 8 digits, for example, 2015 become 20150000 and 201512 becomes 20151200
		//
		if (sWorkDate.length() == 4)
			sWorkDate += "0000" ;
		if (sWorkDate.length() == 4)
			sWorkDate += "0000" ;
		
		// If, even when completed, the String cannot comply to the yyyyMMdd format, then fail
		//
		if (sWorkDate.length() != 8)
			return "" ;
		
		String sReturn = sDateFormat ;
		
		sReturn = sReturn.replace("yyyy", sWorkDate.substring(0, 4)) ;
		sReturn = sReturn.replace("MM",   sWorkDate.substring(4, 6)) ;
		sReturn = sReturn.replace("dd",   sWorkDate.substring(6, 8)) ;
		
		return sReturn ;
	}
	
	/**
	 * Returns a native date (yyyyMMdd) from a formated date (à la dd/MM/yyyy) 
	 * 	  
	 * @param sFormatedDate the formated date, for example "24/12/2015"
	 * @param sDateFormat the format to conform to, for example "dd/MM/yyyy"
	 * 
	 * @return "" if something went wrong or the native date, if successful (for example "20151224") 
	 */
	public static String dateFromFormatedToNative(final String sFormatedDate, final String sDateFormat)
	{
		if ((null == sDateFormat) || "".equals(sDateFormat))
			return "" ;
		if ((null == sFormatedDate) || "".equals(sFormatedDate))
			return "" ;
		
		int iYearsPos   = sDateFormat.indexOf("yyyy") ;
		int iMonthssPos = sDateFormat.indexOf("MM") ;
		int iDaysPos    = sDateFormat.indexOf("dd") ;
		
		// Fail if the format is not valid
		//
		if ((-1 == iYearsPos) || (-1 == iMonthssPos) || (-1 == iDaysPos))
			return "" ;
		
		String sYears  = sFormatedDate.substring(iYearsPos,   iYearsPos   + 4) ;
		String sMonths = sFormatedDate.substring(iMonthssPos, iMonthssPos + 2) ;
		String sDays   = sFormatedDate.substring(iDaysPos,    iDaysPos    + 2) ;
		
		if ("0000".equals(sYears) && "00".equals(sMonths) && "00".equals(sDays))
			return "" ;
		
		if ((false == isDigits(sYears)) || (false == isDigits(sMonths)) || (false == isDigits(sDays)))
			return "" ;
		
		return sYears + sMonths + sDays ;
	}
	
	/**
	 * Returns the position of the first char that is not cChar
	 * 	  
	 * @param sModel the String to explore
	 * @param cChar  the char to find the first occurrence "not of" in sModel
	 * 
	 * @return -1 if there is a problem, the size of the string if the string only contains cChars, an in-between integer in other cases
	 */
	public static int find_first_not_of(final String sModel, char cChar) 
	{
		if ((null == sModel) || "".equals(sModel))
			return -1 ;
		
		int iLen = sModel.length() ;
		
		int i = 0 ;
		for ( ; (i < iLen) && (sModel.charAt(i) == cChar) ; i++) ;
		
		return i ;
	}
	
	/**
	 * Returns the position of the first trailing char that is not cChar
	 * 	  
	 * @param sModel the String to explore
	 * @param cChar  the char to find the first occurrence "not of" in the trailing part of sModel
	 * 
	 * @return -1 if there is a problem or if the string only contains cChars, a positive or null integer in other cases
	 */
	public static int find_last_not_of(final String sModel, char cChar) 
	{
		if ((null == sModel) || "".equals(sModel))
			return -1 ;
		
		int i = sModel.length() - 1 ;
		for ( ; (i >= 0) && (sModel.charAt(i) == cChar) ; i--) ;
		
		return i ;
	}
	
	/**
	 * Returns a String made of iLenght occurrences of cChar 
	 * 	  
	 * @return the String
	 */
	public static String getNChars(final int iLength, char cChar) 
	{
		if (iLength <= 0)
			return "" ;
		
		StringBuffer outputBuffer = new StringBuffer(iLength) ;
		for (int i = 0; i < iLength ; i++) {
		   outputBuffer.append(cChar) ;
		}
		
		return outputBuffer.toString() ;
	}
	
	/**
	 * Returns a String made of the content of a String with a char replaced by another one 
	 * 	  
	 * @param str      Initial String
	 * @param index    Position of the char to replace (zero based)
	 * @param cReplace Char to replace the index-th char with
	 * 
	 * @return the String
	 */
	public static String replace(String str, int index, char cReplace)
	{     
    if (null == str)
    	return str ;
    if ((index < 0) || (index >= str.length()))
    	return str ;
    
    char[] chars = str.toCharArray() ;
    chars[index] = cReplace ;
    
    return String.valueOf(chars) ;       
	}
	
	/**
	 * Returns a string whose value is this string, with any leading and/or trailing cStripChar removed
	 * 	  
	 * @return true if valid, false if invalid
	 */
	public static String strip(final String sModel, STRIP_DIRECTION stripDir, char cStripChar) 
	{
		if (null == sModel)
			return null ;
		
		if ("".equals(sModel))
			return "" ;
		
		if ((STRIP_DIRECTION.stripBoth == stripDir) && (' ' == cStripChar))
			return sModel.trim() ;
		
		// First check if the String only contains cStripChars
		//
		int iFirstNotC = find_first_not_of(sModel, cStripChar) ;
		if (sModel.length() == iFirstNotC)
			return "" ;
		
		String sReturn ;
		
		// If we have to strip left, already use this information
		//
		if ((STRIP_DIRECTION.stripLeft == stripDir) || (STRIP_DIRECTION.stripBoth == stripDir))
		{
			sReturn = sModel.substring(iFirstNotC) ;
			if (STRIP_DIRECTION.stripLeft == stripDir)
				return sReturn ;
		}
		else
			sReturn = sModel ;
		
		int iLastNotC = find_last_not_of(sReturn, cStripChar) ;
		if (-1 == iLastNotC)
			return "" ;
		if (sReturn.length() - 1 == iLastNotC)
			return sReturn ;
		
		return sReturn.substring(0, iLastNotC + 1) ;
	}
	
	/**
	 * Checks if a String is only made of one or several digits
	 * 
	 * @param sValue the String to check
	 * @return <code>true</code> if valid, <code>false</code> if invalid<br>Typically returns <code>false</code> for "", "foo", "aa345bbb"
	 */
	public static boolean isDigits(final String sValue)
	{
		if ((null == sValue) || ("".equals(sValue))) 
			return false ;
		
		// return sValue.matches("\\d+") ; // Easy to write, but slow
		
		int iLen = sValue.length() ;
		for (int i = 0 ; i < iLen ; i++)
			if ((sValue.charAt(i) < '0') || (sValue.charAt(i) > '9'))
				return false ;
		
		return true ;
	}
	
	/**
	 * Check that every char in sTest belongs to sModel
	 * 	  
	 * @param sTest  string to be tested
	 * @param sModel model string
	 * @param bEmptyAccepted true if empty strings are valid
	 * @return true if valid, false if invalid
	 */
	public static boolean isValidString(String sTest, String sModel, boolean bEmptyAccepted)
	{
		if ((null == sTest) || "".equals(sTest))
			return bEmptyAccepted ;
		
		if ((null == sModel) || "".equals(sModel))
			return false ;
		
		// No need to count it for each loop since they don't change
		//
		int iLen      = sTest.length() ;
		int iModelLen = sModel.length() ; 
		
		// Check that each char in sTest belongs to sModel  
		//
		for (int i = 0 ; i < iLen ; i++)
		{
			char c = sTest.charAt(i) ;
			
			int j = 0 ;
			for ( ; j < iModelLen ; j++)
				if (sModel.charAt(j) == c)
					break ;
			
			if (j == iModelLen)
				return false ;
		}
		
		return true ;
	}
}
