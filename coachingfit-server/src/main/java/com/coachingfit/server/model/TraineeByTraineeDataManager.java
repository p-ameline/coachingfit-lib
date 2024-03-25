package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.coachingfit.shared.database.TraineeByTraineeData;

import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>traineeByTrainee</code> table 
 *   
 */
public class TraineeByTraineeDataManager  
{	
	protected final DBConnector _dbConnector ;
	protected final int         _iUserId ;
	
	/**
	 * Constructor 
	 */
	public TraineeByTraineeDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}
	
	/**
	  * Insert a TraineeByTraineeData object in database
	  * 
	  * @return true if successful, false if not
	  * @param dataToInsert TraineeByTraineeData to be inserted
	  * 
	  */
	public boolean insertData(TraineeByTraineeData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;
		
		String sFctName = "TraineeByTraineeDataManager.insertData" ;
		
		String sQuery = "INSERT INTO traineesByTrainee (seniorTraineeID, traineeID) VALUES (?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}
		
		_dbConnector.setStatememtInt(1, dataToInsert.getSeniorTraineeId()) ;
		_dbConnector.setStatememtInt(2, dataToInsert.getTraineeId()) ;
		
		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(true) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}
		
		int iTraineesByTraineeDataId = 0 ;
		
		ResultSet rs = _dbConnector.getResultSet() ;
		try
    {
			if (rs.next())
			{
				iTraineesByTraineeDataId = rs.getInt(1) ;
				dataToInsert.setId(iTraineesByTraineeDataId) ;
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
		
		Logger.trace(sFctName + ": user " + _iUserId + " successfuly recorded traineesByTrainee " + iTraineesByTraineeDataId, _iUserId, Logger.TraceLevel.STEP) ;
		
		return true ;
	}
	
	/**
	  * Update a TraineeByTraineeData in database
	  * 
	  * @return true if successful, false if not
	  * @param dataToUpdate TraineeByTraineeData to be updated
	  * 
	  */
	public boolean updateData(TraineeByTraineeData dataToUpdate)
	{
		String sFctName = "TraineeByTraineeDataManager.updateData" ;
		
		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		TraineeByTraineeData foundData = new TraineeByTraineeData() ;
		if (false == existData(dataToUpdate.getId(), foundData))
			return false ;
		
		if (foundData.equals(dataToUpdate))
		{
			Logger.trace(sFctName + ": TraineeByTraineeData to update (id = " + dataToUpdate.getId() + ") unchanged; nothing to do", _iUserId, Logger.TraceLevel.SUBSTEP) ;
			return true ;
		}
		
		return forceUpdateData(dataToUpdate) ;
	}
		
	/**
	  * Check if there is any TraineeByTraineeData with this Id in database and, if true get its content
	  * 
	  * @return <code>true</code> if found, else <code>false</code>
	  * 
	  * @param iDataId ID of RegionData to check
	  * @param foundData TraineeByTraineeData to get existing information
	  * 
	  */
	public boolean existData(int iDataId, TraineeByTraineeData foundData)
	{
		String sFctName = "TraineeByTraineeDataManager.existData" ;
		
		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		String sQuery = "SELECT * FROM traineesByTrainee WHERE id = ?" ;
		
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
			Logger.trace(sFctName + ": no TraineeByTraineeData found for id = " + iDataId, _iUserId, Logger.TraceLevel.WARNING) ;
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
	  * Update a SiteData in database
	  * 
	  * @return <code>true</code> if creation succeeded, <code>false</code> if not
	  * @param  dataToUpdate RegionData to update
	  * 
	  */
	private boolean forceUpdateData(TraineeByTraineeData dataToUpdate)
	{
		String sFctName = "TraineeByTraineeDataManager.forceUpdateData" ;
		
		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
			
		// Prepare SQL query
		//
		String sQuery = "UPDATE traineesByTrainee SET seniorTraineeID = ?, traineeID = ?" +
				                          " WHERE " +
				                               "id = '" + dataToUpdate.getId() + "'" ; 
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
		
		_dbConnector.setStatememtInt(1, dataToUpdate.getSeniorTraineeId()) ;
		_dbConnector.setStatememtInt(2, dataToUpdate.getTraineeId()) ;
				
		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(false) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		Logger.trace(sFctName + ": updated data for TraineeByTraineeData " + dataToUpdate.getId(), _iUserId, Logger.TraceLevel.SUBSTEP) ;
		
		_dbConnector.closePreparedStatement() ;
		
		return true ;
	}
	
	/**
	  * Fill a structure with all the trainees attributed to a given senior 
	  * 
	  * @param iUserId    ID of user
	  * @param aTrainees  TraineeByTraineeData array to fill
	  * @param iSeniorId  ID of the senior to get traineesByTrainee records for 
	  * 
	  */
	public void fillTraineesForSenior(int iUserID, List<TraineeByTraineeData> aTrainees, int iSeniorId)
	{
		String sFctName = "TraineeByTraineeDataManager.fillRegionsForCoach" ;
		
		if ((null == _dbConnector) || (-1 == iSeniorId) || (null == aTrainees))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		String sQuery = "SELECT * FROM traineesByTrainee WHERE seniorTraineeID = ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iSeniorId) ;
	   		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
	   		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no traineesByTrainee found for coach " + iSeniorId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
		
		try
		{
	    while (rs.next())
	    {
	    	TraineeByTraineeData foundData = new TraineeByTraineeData() ;
	    	fillDataFromResultSet(rs, foundData) ;
	    	aTrainees.add(foundData) ;
	    }
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
				
		return ;
	}
	
	/**
	  * Initialize a TraineeByTraineeData from a query ResultSet 
	  * 
	  * @param rs        ResultSet of a query
	  * @param foundData TraineeByTraineeData to fill
	  * 
	  */
	protected void fillDataFromResultSet(ResultSet rs, TraineeByTraineeData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;
		
		try
		{
			foundData.setId(rs.getInt("id")) ;
    	foundData.setSeniorTraineeId(rs.getInt("seniorTraineeID")) ;
    	foundData.setTraineeId(rs.getInt("traineeID")) ;
		} 
		catch (SQLException e) {
			Logger.trace("TraineeByTraineeDataManager.fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}
}
