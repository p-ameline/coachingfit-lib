package com.coachingfit.shared.util;

import java.util.Random;

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
    
    /**
     * Get a strong password of desired length (at least 8)
     * 
     * @param iLength Password desired length
     * 
     * @return A randomly generated password of 8 characters or more
     */
    static public String buildPassword(int iLength)
    {
        //minimum length of 8
        if (iLength < 8) 
            iLength = 8;

        final char[] lowercase = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        final char[] uppercase = "ABCDEFGJKLMNPRSTUVWXYZ".toCharArray();
        final char[] numbers = "0123456789".toCharArray();
        final char[] symbols = "!@#$%&*()_+-=[]|,./?><".toCharArray();
        final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789!@#$%&*()_+-=[]|,./?><".toCharArray();

        // Use cryptographically secure random number generator
        Random random = new Random();

        StringBuilder password = new StringBuilder(); 

        for (int i = 0; i < iLength-4; i++) {
            password.append(allAllowed[random.nextInt(allAllowed.length)]);
        }

        // Ensure password policy is met by inserting required random chars in random positions
        password.insert(random.nextInt(password.length()), lowercase[random.nextInt(lowercase.length)]);
        password.insert(random.nextInt(password.length()), uppercase[random.nextInt(uppercase.length)]);
        password.insert(random.nextInt(password.length()), numbers[random.nextInt(numbers.length)]);
        password.insert(random.nextInt(password.length()), symbols[random.nextInt(symbols.length)]);

        return password.toString();
    }
}
