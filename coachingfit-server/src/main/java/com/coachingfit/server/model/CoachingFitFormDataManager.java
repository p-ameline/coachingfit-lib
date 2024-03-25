package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.coachingfit.shared.database.CoachingFitFormData;

import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>form</code> table 
 *   
 */
public class CoachingFitFormDataManager  
{	
	protected final DBConnector _dbConnector ;
	protected final int         _iUserId ;
	
	/**
	 * Constructor 
	 */
	public CoachingFitFormDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}
	
	/**
	  * Insert a CoachingFitFormData object in database
	  * 
	  * @return true if successful, false if not
	  * @param dataToInsert CoachingFitFormData to be inserted
	  * 
	  */
	public boolean insertData(final CoachingFitFormData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;
		
		String sFctName = "CoachingFitFormDataManager.insertData" ;
		
		String sQuery = "INSERT INTO form (archetypeID, action, root, traineeID, regionID, formDate, userID, seniorTraineeID, formEntryDate, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}
		
		_dbConnector.setStatememtInt(1, dataToInsert.getArchetypeId()) ;
		_dbConnector.setStatememtString(2, dataToInsert.getActionId()) ;
		_dbConnector.setStatememtString(3, dataToInsert.getRoot()) ;
		_dbConnector.setStatememtInt(4, dataToInsert.getTraineeId()) ;
		_dbConnector.setStatememtInt(5, dataToInsert.getRegionId()) ;
		_dbConnector.setStatememtString(6, dataToInsert.getCoachingDate()) ;
		_dbConnector.setStatememtInt(7, dataToInsert.getAuthorId()) ;
		_dbConnector.setStatememtInt(8, dataToInsert.getSeniorTraineeId()) ;
		_dbConnector.setStatememtString(9, dataToInsert.getEntryDateHour()) ;
		_dbConnector.setStatememtString(10, dataToInsert.getStatusAsString()) ;
		
		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(true) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}
		
		int iDataId = 0 ;
		
		ResultSet rs = _dbConnector.getResultSet() ;
		try
    {
			if (rs.next())
			{
				iDataId = rs.getInt(1) ;
				dataToInsert.setFormId(iDataId) ;
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
		
		Logger.trace(sFctName + ": user " + _iUserId + " successfuly recorded form " + iDataId, _iUserId, Logger.TraceLevel.STEP) ;
		
		return true ;
	}
	
	/**
	  * Update a CoachingFitFormData in database
	  * 
	  * @return true if successful, false if not
	  * @param dataToUpdate CoachingFitFormData to be updated
	  * 
	  */
	public boolean updateData(CoachingFitFormData dataToUpdate)
	{
		String sFctName = "CoachingFitFormDataManager.updateData" ;
		
		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		CoachingFitFormData foundData = new CoachingFitFormData() ;
		if (false == existData(dataToUpdate.getFormId(), foundData))
			return false ;
		
		if (foundData.equals(dataToUpdate))
		{
			Logger.trace(sFctName + ": FormData to update (id = " + dataToUpdate.getFormId() + ") unchanged; nothing to do", _iUserId, Logger.TraceLevel.SUBSTEP) ;
			return true ;
		}
		
		return forceUpdateData(dataToUpdate) ;
	}
		
	/**
	  * Check if there is any FormData with this Id in database and, if true get its content
	  * 
	  * @return True if found, else false
	  * @param iDataId ID of FormData to check
	  * @param foundData FormData to get existing information
	  * 
	  */
	public boolean existData(int iDataId, CoachingFitFormData foundData)
	{
		String sFctName = "CoachingFitFormDataManager.existData" ;
		
		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		String sQuery = "SELECT * FROM form WHERE id = ?" ;
		
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
			Logger.trace(sFctName + ": no FormData found for id = " + iDataId, _iUserId, Logger.TraceLevel.WARNING) ;
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
	  * Update a FormData in database
	  * 
	  * @return <code>true</code> if creation succeeded, <code>false</code> if not
	  * @param  dataToUpdate FormData to update
	  * 
	  */
	private boolean forceUpdateData(CoachingFitFormData dataToUpdate)
	{
		String sFctName = "CoachingFitFormDataManager.forceUpdateData" ;
		
		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
			
		// Prepare SQL query
		//
		String sQuery = "UPDATE form SET archetypeID = ?, action = ?, root = ?, traineeID = ?, regionID = ?, formDate = ?, userID = ?, seniorTraineeID = ?, formEntryDate = ?, deleted = ?" +
				                          " WHERE " +
				                               "id = '" + dataToUpdate.getFormId() + "'" ; 
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
		
		_dbConnector.setStatememtInt(1, dataToUpdate.getArchetypeId()) ;
		_dbConnector.setStatememtString(2, dataToUpdate.getActionId()) ;
		_dbConnector.setStatememtString(3, dataToUpdate.getRoot()) ;
		_dbConnector.setStatememtInt(4, dataToUpdate.getTraineeId()) ;
		_dbConnector.setStatememtInt(5, dataToUpdate.getRegionId()) ;
		_dbConnector.setStatememtString(6, dataToUpdate.getCoachingDate()) ;
		_dbConnector.setStatememtInt(7, dataToUpdate.getAuthorId()) ;
		_dbConnector.setStatememtInt(8, dataToUpdate.getSeniorTraineeId()) ;
		_dbConnector.setStatememtString(9, dataToUpdate.getEntryDateHour()) ;
		_dbConnector.setStatememtString(10, dataToUpdate.getStatusAsString()) ;
				
		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(false) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		Logger.trace(sFctName + ": updated data for FormData " + dataToUpdate.getFormId(), _iUserId, Logger.TraceLevel.SUBSTEP) ;
		
		_dbConnector.closePreparedStatement() ;
		
		return true ;
	}

	/**
	  * Get all {@link CoachingFitFormData} in database
	  * 
	  * @param aUsers     Array of CoachingFitFormData to fill with database content
	  * @param bOnlyValid If <code>true</code>, discard draft and deleted forms, if <code>false</code> get them all
	  * 
	  * @return <code>true</code> if found, <code>false</code> if not
	  */
	public boolean getThemAll(List<CoachingFitFormData> aForms, boolean bOnlyValid)
	{
		String sFctName = "CoachingFitFormDataManager.getThemAll" ;
		
		if ((null == _dbConnector) || (null == aForms))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		String sQuery = "SELECT * FROM form" ;
		
		if (bOnlyValid)
			sQuery += " WHERE deleted = '0'" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no CoachingFitFormData found", _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
		
		try
		{
			int iCount = 0 ;
			
	    while (rs.next())
	    {
	    	iCount++ ;
	    	
	    	CoachingFitFormData foundData = new CoachingFitFormData() ;
	    	fillDataFromResultSet(rs, foundData) ;
	    	aForms.add(foundData) ;	    	
	    }
	    
	    _dbConnector.closeResultSet() ;
	    _dbConnector.closePreparedStatement() ;
 	
	    Logger.trace(sFctName + ": found " + iCount + " objects.", _iUserId, Logger.TraceLevel.SUBSTEP) ;
	    
	    return true ;	
		} 
		catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
				
		return false ;
	}
	
	/**
	  * Initialize an FormData from a query ResultSet 
	  * 
	  * @param rs        ResultSet of a query
	  * @param foundData FormData to fill
	  * 
	  */
	public void fillDataFromResultSet(final ResultSet rs, CoachingFitFormData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;
		
		try
		{
			foundData.setFormId(rs.getInt("id")) ;
			foundData.setArchetypeId(rs.getInt("archetypeID")) ;
			foundData.setActionId(rs.getString("action")) ;
			foundData.setRoot(rs.getString("root")) ;
			foundData.setTraineeId(rs.getInt("traineeID")) ;
			foundData.setRegionId(rs.getInt("regionID")) ;
			foundData.setCoachingDate(rs.getString("formDate")) ;
			foundData.setAuthorId(rs.getInt("userID")) ;
			foundData.setSeniorTraineeId(rs.getInt("seniorTraineeID")) ;
			foundData.setEntryDateHour(rs.getString("formEntryDate")) ;
			foundData.setStatusFromString(rs.getString("deleted")) ;
		} 
		catch (SQLException e) {
			Logger.trace("CoachingFitFormDataManager.fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}
}
