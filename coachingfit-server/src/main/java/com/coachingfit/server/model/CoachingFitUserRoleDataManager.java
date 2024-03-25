package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.coachingfit.shared.database.CoachingFitUserRoleData;

import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>userRole</code> table 
 *   
 */
public class CoachingFitUserRoleDataManager  
{	
	protected final DBConnector _dbConnector ;
	protected final int         _iUserId ;
	
	/**
	 * Constructor 
	 */
	public CoachingFitUserRoleDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}
	
	/**
	  * Insert a UserRoleData object in database
	  * 
	  * @return true if successful, false if not
	  * @param dataToInsert UserRoleData to be inserted
	  * 
	  */
	public boolean insertData(CoachingFitUserRoleData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;
		
		String sFctName = "CoachingFitUserRoleDataManager.insertData" ;
		
		String sQuery = "INSERT INTO userRole (userID, archeID, userRole) VALUES (?, ?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}
		
		_dbConnector.setStatememtInt(1, dataToInsert.getUserId()) ;
		_dbConnector.setStatememtInt(2, dataToInsert.getArchetypeId()) ;
		_dbConnector.setStatememtString(3, dataToInsert.getUserRole()) ;
		
		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(true) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}
		
		int iRoleDataId = 0 ;
		
		ResultSet rs = _dbConnector.getResultSet() ;
		try
    {
			if (rs.next())
			{
				iRoleDataId = rs.getInt(1) ;
				dataToInsert.setId(iRoleDataId) ;
			}
			else
				Logger.trace(sFctName + ": cannot get Id after query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
    } 
		catch (SQLException e)
    {
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
    }
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
		
		Logger.trace(sFctName + ": user " + _iUserId + " successfuly recorded role " + iRoleDataId, _iUserId, Logger.TraceLevel.STEP) ;
		
		return true ;
	}
	
	/**
	  * Update a UserRoleData in database
	  * 
	  * @return true if successful, false if not
	  * @param dataToUpdate UserRoleData to be updated
	  * 
	  */
	public boolean updateData(CoachingFitUserRoleData dataToUpdate)
	{
		String sFctName = "CoachingFitUserRoleDataManager.updateData" ;
		
		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		CoachingFitUserRoleData foundData = new CoachingFitUserRoleData() ;
		if (false == existData(dataToUpdate.getId(), foundData))
			return false ;
		
		if (foundData.equals(dataToUpdate))
		{
			Logger.trace(sFctName + ": UserRoleData to update (id = " + dataToUpdate.getId() + ") unchanged; nothing to do", _iUserId, Logger.TraceLevel.SUBSTEP) ;
			return true ;
		}
		
		return forceUpdateData(dataToUpdate) ;
	}
		
	/**
	  * Check if there is any UserRoleData with this Id in database and, if true get its content
	  * 
	  * @return True if found, else false
	  * 
	  * @param iDataId ID of UserRoleData to check
	  * @param foundData UserRoleData to get existing information
	  * 
	  */
	private boolean existData(int iDataId, CoachingFitUserRoleData foundData)
	{
		String sFctName = "CoachingFitUserRoleDataManager.existData" ;
		
		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		String sQuery = "SELECT * FROM userRole WHERE id = ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iDataId) ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no SiteData found for id = " + iDataId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
		
		try
		{
	    if (rs.next())
	    {
	    	fillDataFromResultSet(rs, foundData) ;
	    	
	    	_dbConnector.closeResultSet() ;
	    	_dbConnector.closePreparedStatement() ;
	    	
	    	return true ;	    	
	    }
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
				
		return false ;
	}
		
	/**
	  * Fill a structure with all the different roles for a same user 
	  * 
	  * @param iUserId  ID of user to get roles for
	  * @param aRoles   UserRoleData array to fill
	  * 
	  */
	public void fillRolesForUser(int iUserID, List<CoachingFitUserRoleData> aRoles)
	{
		String sFctName = "CoachingFitUserRoleDataManager.fillRolesForUser" ;
		
		if ((null == _dbConnector) || (-1 == iUserID) || (null == aRoles))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		String sQuery = "SELECT * FROM userRole WHERE userID = ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iUserID) ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no role found for user " + iUserID, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
		
		try
		{
	    while (rs.next())
	    {
	    	CoachingFitUserRoleData foundData = new CoachingFitUserRoleData() ;
	    	fillDataFromResultSet(rs, foundData) ;
	    	aRoles.add(foundData) ;
	    }
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
	}
	
	/**
	  * Fill a structure with all the different roles for a same archetype 
	  * 
	  * @param iArchetypeID  ID of archetype to get roles for
	  * @param aRoles        CoachingFitUserRoleData array to fill
	  * 
	  */
	public void fillRolesForArchetype(int iArchetypeID, List<CoachingFitUserRoleData> aRoles)
	{
		String sFctName = "CoachingFitUserRoleDataManager.fillRolesForArchetype" ;
		
		if ((null == _dbConnector) || (-1 == iArchetypeID) || (null == aRoles))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		String sQuery = "SELECT * FROM userRole WHERE archeID = ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iArchetypeID) ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no role found for archetype " + iArchetypeID, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
		
		try
		{
	    while (rs.next())
	    {
	    	CoachingFitUserRoleData foundData = new CoachingFitUserRoleData() ;
	    	fillDataFromResultSet(rs, foundData) ;
	    	aRoles.add(foundData) ;
	    }
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
	}
	
	/**
	  * Fill a structure with all the different roles for a common role pattern 
	  * 
	  * @param iArchetypeID  ID of archetype to get roles for
	  * @param aRoles        CoachingFitUserRoleData array to fill
	  * 
	  */
	public void fillRolesForRolePattern(final String sPattern, boolean bExact, List<CoachingFitUserRoleData> aRoles)
	{
		String sFctName = "CoachingFitUserRoleDataManager.fillRolesForRolePattern" ;
		
		if ((null == _dbConnector) || (null == sPattern) || (null == aRoles))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		String sQuery = "SELECT * FROM userRole WHERE userRole" ;
		
		if (bExact)
			sQuery += " = ?" ;
		else
			sQuery += " LIKE ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		
		if (bExact)
			_dbConnector.setStatememtString(1, sPattern) ;
		else
			_dbConnector.setStatememtString(1, sPattern + "%") ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			if (bExact)
				Logger.trace(sFctName + ": no role found for exact pattern " + sPattern, _iUserId, Logger.TraceLevel.WARNING) ;
			else
				Logger.trace(sFctName + ": no role found for pattern " + sPattern, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
		
		try
		{
	    while (rs.next())
	    {
	    	CoachingFitUserRoleData foundData = new CoachingFitUserRoleData() ;
	    	fillDataFromResultSet(rs, foundData) ;
	    	aRoles.add(foundData) ;
	    }
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
	}
	
	/**
	  * Return the role, if any; for a given user and a given archetype 
	  * 
	  * @param iUserId      ID of user to get role for
	  * @param iArchetypeID ID of archetype to get role for
	  * 
	  * @return The CoachingFitUserRoleData if found, <code>null</code> if not.
	  * 
	  */
	public CoachingFitUserRoleData getRoleForUserAndArchetype(int iUserID, int iArchetypeID)
	{
		String sFctName = "CoachingFitUserRoleDataManager.getRoleForUserAndArchetype" ;
		
		if ((null == _dbConnector) || (-1 == iUserID) || (-1 == iArchetypeID))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return null ;
		}
		
		String sQuery = "SELECT * FROM userRole WHERE userID = ? AND archeID = ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iUserID) ;
		_dbConnector.setStatememtInt(2, iArchetypeID) ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return null ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no role found for user " + iUserID + " and archetype " + iArchetypeID, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return null ;
		}
		
		try
		{
	    if (rs.next())
	    {
	    	CoachingFitUserRoleData foundData = new CoachingFitUserRoleData() ;
	    	fillDataFromResultSet(rs, foundData) ;
	    	
	    	_dbConnector.closeResultSet() ;
	  		_dbConnector.closePreparedStatement() ;
	    	
	    	return foundData ;
	    }
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
				
		return null ;
	}
	
	/**
	  * Return the role, if any; for a given user and a given archetype 
	  * 
	  * @param iUserId      ID of user to get role for
	  * @param iArchetypeID ID of archetype to get role for
	  * 
	  * @return The CoachingFitUserRoleData if found, <code>null</code> if not.
	  * 
	  */
	public CoachingFitUserRoleData getRoleForTraineeJobAndArchetype(final String sTraineeJob, int iArchetypeID)
	{
		String sFctName = "CoachingFitUserRoleDataManager.getRoleForTraineeJobAndArchetype" ;
		
		if ((null == _dbConnector) || (null == sTraineeJob) || "".equals(sTraineeJob) || (-1 == iArchetypeID))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return null ;
		}
		
		String sQuery = "SELECT * FROM userRole WHERE userID = ? AND archeID = ? AND userRole = ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, 0) ;
		_dbConnector.setStatememtInt(2, iArchetypeID) ;
		_dbConnector.setStatememtString(3, sTraineeJob) ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return null ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no role found for trainee job " + sTraineeJob + " and archetype " + iArchetypeID, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return null ;
		}
		
		try
		{
	    if (rs.next())
	    {
	    	CoachingFitUserRoleData foundData = new CoachingFitUserRoleData() ;
	    	fillDataFromResultSet(rs, foundData) ;
	    	
	    	_dbConnector.closeResultSet() ;
	  		_dbConnector.closePreparedStatement() ;
	    	
	    	return foundData ;
	    }
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
				
		return null ;
	}
	
	/**
	  * Update a UserRoleData in database
	  * 
	  * @return <code>true</code> if creation succeeded, <code>false</code> if not
	  * @param  dataToUpdate UserRoleData to update
	  * 
	  */
	private boolean forceUpdateData(CoachingFitUserRoleData dataToUpdate)
	{
		String sFctName = "CoachingFitUserRoleDataManager.forceUpdateData" ;
		
		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
			
		// Prepare SQL query
		//
		String sQuery = "UPDATE userRole SET userID = ?, archeID = ?, userRole = ?" +
				                          " WHERE " +
				                               "id = '" + dataToUpdate.getId() + "'" ; 
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
		
		_dbConnector.setStatememtInt(1, dataToUpdate.getUserId()) ;
		_dbConnector.setStatememtInt(2, dataToUpdate.getArchetypeId()) ;
		_dbConnector.setStatememtString(3, dataToUpdate.getUserRole()) ;
				
		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(false) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		Logger.trace(sFctName + ": updated data for UserRoleData " + dataToUpdate.getId(), _iUserId, Logger.TraceLevel.SUBSTEP) ;
		
		_dbConnector.closePreparedStatement() ;
		
		return true ;
	}
	
	/**
	  * Initialize a UserRoleData from a query ResultSet 
	  * 
	  * @param rs        ResultSet of a query
	  * @param foundData UserRoleData to fill
	  * 
	  */
	protected void fillDataFromResultSet(ResultSet rs, CoachingFitUserRoleData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;
		
		try
		{
			foundData.setId(rs.getInt("id")) ;
    	foundData.setUserId(rs.getInt("userID")) ;
    	foundData.setArchetypeId(rs.getInt("archeID")) ;
    	foundData.setUserRole(rs.getString("userRole")) ;
		} 
		catch (SQLException e) {
			Logger.trace("CoachingFitUserRoleDataManager.fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}
}
