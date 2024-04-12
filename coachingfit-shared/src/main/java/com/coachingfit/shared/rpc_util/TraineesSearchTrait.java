package com.coachingfit.shared.rpc_util;

import java.io.Serializable;
import java.util.Objects ;

/**
 * Object used to transmit search traits from client to server
 * 
 * @author Philippe
 *
 */
public class TraineesSearchTrait implements Serializable
{
    private static final long serialVersionUID = 5724164644834278901L;

    private String  _sFirstName ;
    private String  _sLastName ;
    private int     _iRegionId ;
    private int     _iCoachId ;
    private String  _sCategory ;
    private boolean _bIsActive ;

    /**
     * Plain vanilla constructor 
     */
    public TraineesSearchTrait(final String sFirstName, final String sLastName, int iRegionId, int iCoachId, final String sCategory, final boolean bIsActive)
    {
    	_sFirstName = sFirstName ;
        _sLastName  = sLastName ;
        _iRegionId  = iRegionId ;
        _iCoachId   = iCoachId ;
        _sCategory  = sCategory ;
        _bIsActive  = bIsActive ;
    }

    /**
     * Void constructor
     */
    public TraineesSearchTrait() {
    	reset() ;
    }

    private void reset()
    {
    	_sFirstName = "" ;
        _sLastName  = "" ;
        _iRegionId  = -1 ;
        _iCoachId   = -1 ;
        _sCategory  = "" ;
        _bIsActive  = false ;
    }
    
    public String getFirstName() {
        return _sFirstName ;
    }
    public void setFirstName(final String sFirstName) {
        _sFirstName = (null == sFirstName) ? "" : sFirstName ;
    }
    
    public String getLastName() {
        return _sLastName ;
    }
    public void setLastName(final String sLastName) {
        _sLastName = (null == sLastName) ? "" : sLastName ;
    }

    public int getRegionId() {
        return _iRegionId ;
    }
    public void setRegionId(final int iRegionId) {
        _iRegionId = iRegionId ;
    }
    
    public int getCoachId() {
        return _iCoachId ;
    }
    public void setCoachId(final int iCoachId) {
        _iCoachId = iCoachId ;
    }
    
    public String getCategory() {
        return _sCategory ;
    }
    public void setCategory(final String sCategory) {
        _sCategory = (null == sCategory) ? "" : sCategory ;
    }

    public boolean isToBeActive() {
        return _bIsActive ;
    }
    public void setMustBeActive(final boolean bIsActive) {
        _bIsActive = bIsActive ;
    }
    
    public boolean isEmpty() {
    	return _sFirstName.isEmpty() && _sLastName.isEmpty() && (_iCoachId <= 0) && (_iRegionId <= 0) && _sCategory.isEmpty() && (false == _bIsActive) ;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(_bIsActive, _iCoachId, _iRegionId, _sCategory, _sFirstName, _sLastName) ;
    }
    
    /**
     * Determine whether two ApnolabSearchTrait are exactly similar
     * 
     * @param other Other ApnolabSearchTrait to compare to
     * 
     * @return <code>true</code> if all data are the same, <code>false</code> if not
     */
    public boolean equals(final TraineesSearchTrait other) 
    { 
        if (null == other)
            return false ;

        if (this == other)
            return true ;

        return (_iRegionId == other._iRegionId) &&
               (_iCoachId  == other._iCoachId)  &&
               (_bIsActive == other._bIsActive) &&
               _sFirstName.equals(other._sFirstName) &&
               _sLastName.equals(other._sLastName) &&
               _sCategory.equals(other._sCategory) ;
    }

    /**
     * Determine whether an object is similar to this one
     * 
     * @param o Object to compare to
     * 
     * @return <code>true</code> if all data are the same, <code>false</code> if not
     */
    @Override
    public boolean equals(Object o) 
    {
        if (this == o)
            return true ;
        
        if ((null == o) || (getClass() != o.getClass()))
            return false ;
        
        final TraineesSearchTrait other = (TraineesSearchTrait) o ;
        
        return equals(other) ;
    }
}
