package com.coachingfit.client.utilities ;

/**
 */
public class LocalFunctions
{
    /**
     * Verifies that the specified name is valid for our service.
     * In this example, we only require that the name is at least four characters.
     * 
     * @param name the name to validate
     * @return true if valid, false if invalid
     */
    public static String getMailFromName(final String sFirstName, final String sSecondName, final String sExtension)
    {
        if ((null == sFirstName) && (null == sSecondName))
            return null ;
        
        if (null == sExtension)
            return null ;

        String sMail = (null == sFirstName) ? "" : sFirstName.trim().toLowerCase() ;
        
        if (null != sSecondName)
        {
            String sLower = sSecondName.trim().toLowerCase() ;
            if (false == sLower.isEmpty())
                sMail += sMail.isEmpty() ? sLower : "." + sLower ;
        }
        
        if (sMail.isEmpty())
            return null ;
        
        return sMail + "@" + sExtension ;
    }
}
