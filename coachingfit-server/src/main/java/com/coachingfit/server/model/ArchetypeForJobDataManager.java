package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.coachingfit.shared.database.ArchetypeForJobData;

import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>archetypeForJob</code> table 
 *   
 */
public class ArchetypeForJobDataManager  
{	
	protected final DBConnector _dbConnector ;
	protected final int         _iUserId ;

	/**
	 * Constructor 
	 */
	public ArchetypeForJobDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}

	/**
	 * Insert a ArchetypeForJobData object in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToInsert ArchetypeForJobData to be inserted
	 * 
	 */
	public boolean insertData(ArchetypeForJobData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;

		String sFctName = "ArchetypeForJobDataManager.insertData" ;

		String sQuery = "INSERT INTO archetypeForJob (archeID, jobType) VALUES (?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		_dbConnector.setStatememtInt(1, dataToInsert.getArchetypeId()) ;
		_dbConnector.setStatememtString(2, dataToInsert.getJobType()) ;

		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(true) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		int iRegionDataId = 0 ;

		ResultSet rs = _dbConnector.getResultSet() ;
		try
		{
			if (rs.next())
			{
				iRegionDataId = rs.getInt(1) ;
				dataToInsert.setId(iRegionDataId) ;
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

		Logger.trace(sFctName + ": user " + _iUserId + " successfuly recorded Region " + iRegionDataId, _iUserId, Logger.TraceLevel.STEP) ;

		return true ;
	}

	/**
	 * Update a ArchetypeForJobData in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToUpdate SiteData to be updated
	 * 
	 */
	public boolean updateData(ArchetypeForJobData dataToUpdate)
	{
		String sFctName = "ArchetypeForJobDataManager.updateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		ArchetypeForJobData foundData = new ArchetypeForJobData() ;
		if (false == existData(dataToUpdate.getId(), foundData))
			return false ;

		if (foundData.equals(dataToUpdate))
		{
			Logger.trace(sFctName + ": RegionData to update (id = " + dataToUpdate.getId() + ") unchanged; nothing to do", _iUserId, Logger.TraceLevel.SUBSTEP) ;
			return true ;
		}

		return forceUpdateData(dataToUpdate) ;
	}

	/**
	 * Check if there is any ArchetypeForJobData with this Id in database and, if true get its content
	 * 
	 * @return True if found, else false
	 * @param iDataId ID of RegionData to check
	 * @param foundData ArchetypeForJobData to get existing information
	 * 
	 */
	public boolean existData(int iDataId, ArchetypeForJobData foundData)
	{
		String sFctName = "ArchetypeForJobDataManager.existData" ;

		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM archetypeForJob WHERE id = ?" ;

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
			Logger.trace(sFctName + ": no RegionData found for id = " + iDataId, _iUserId, Logger.TraceLevel.WARNING) ;
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
	 * Update a ArchetypeForJobData in database
	 * 
	 * @return <code>true</code> if creation succeeded, <code>false</code> if not
	 * @param  dataToUpdate ArchetypeForJobData to update
	 * 
	 */
	private boolean forceUpdateData(ArchetypeForJobData dataToUpdate)
	{
		String sFctName = "ArchetypeForJobDataManager.forceUpdateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		// Prepare SQL query
		//
		String sQuery = "UPDATE archetypeForJob SET archeID = ?, jobType = ?" +
				" WHERE " +
				"id = '" + dataToUpdate.getId() + "'" ; 

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		_dbConnector.setStatememtInt(1, dataToUpdate.getArchetypeId()) ;
		_dbConnector.setStatememtString(2, dataToUpdate.getJobType()) ;

		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(false) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		Logger.trace(sFctName + ": updated data for RegionData " + dataToUpdate.getId(), _iUserId, Logger.TraceLevel.SUBSTEP) ;

		_dbConnector.closePreparedStatement() ;

		return true ;
	}

	/**
	 * Fill a structure with all the different ArchetypeForJobData for a given archetype 
	 * 
	 * @param iUserId   ID of user
	 * @param aAFJ      ArchetypeForJobData array to fill
	 * @param iCoachId  ID of the coach to get Regions for 
	 * 
	 */
	public void fillDataForArchetype(int iUserID, List<ArchetypeForJobData> aAFJ, int iArchetypeId)
	{
		String sFctName = "ArchetypeForJobDataManager.fillDataForArchetype" ;

		if ((null == _dbConnector) || (-1 == iArchetypeId) || (null == aAFJ))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		String sQuery = "SELECT * FROM archetypeForJob WHERE archeID = ?" ;

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iArchetypeId) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no archetypeForJob found for archetype " + iArchetypeId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		try
		{
			while (rs.next())
			{
				ArchetypeForJobData foundData = new ArchetypeForJobData() ;
				fillDataFromResultSet(rs, foundData) ;
				aAFJ.add(foundData) ;
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
	 * Get all ArchetypeForJobData in database
	 * 
	 * @return True if found, else false
	 * @param aAFJ Array of ArchetypeForJobData to fill with database content
	 * 
	 */
	public boolean getThemAll(List<ArchetypeForJobData> aAFJ)
	{
		String sFctName = "ArchetypeForJobDataManager.getThemAll" ;

		if ((null == _dbConnector) || (null == aAFJ))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM archetypeForJob" ;

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
			Logger.trace(sFctName + ": no RegionData found", _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		try
		{
			while (rs.next())
			{
				ArchetypeForJobData foundData = new ArchetypeForJobData() ;
				fillDataFromResultSet(rs, foundData) ;
				aAFJ.add(foundData) ;	    	
			}

			_dbConnector.closeResultSet() ;
			_dbConnector.closePreparedStatement() ;

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
	 * Initialize a ArchetypeForJobData from a query ResultSet 
	 * 
	 * @param rs        ResultSet of a query
	 * @param foundData ArchetypeForJobData to fill
	 * 
	 */
	protected void fillDataFromResultSet(ResultSet rs, ArchetypeForJobData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;

		try
		{
			foundData.setId(rs.getInt("id")) ;
			foundData.setArchetypeId(rs.getInt("archeID")) ;
			foundData.setJobType(rs.getString("jobType")) ;
		} 
		catch (SQLException e) {
			Logger.trace("ArchetypeForJobDataManager.fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}
}
