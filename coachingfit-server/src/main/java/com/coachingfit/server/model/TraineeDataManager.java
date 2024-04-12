package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import com.coachingfit.shared.database.RegionData;
import com.coachingfit.shared.database.TraineeData;

import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>trainee</code> table 
 *   
 */
public class TraineeDataManager
{
	private final static String _sClassName = "TraineeDataManager" ;

	private final DBConnector _dbConnector ;
	private final int         _iUserId ;

	/**
	 * Constructor 
	 */
	public TraineeDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}

	/**
	 * Insert a TraineeData object in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToInsert TraineeData to be inserted
	 * 
	 */
	public boolean insertData(TraineeData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;

		String sFctName = _sClassName + ".insertData" ;

		String sQuery = "INSERT INTO trainee (traineeLabel, traineeFirst, coachID, regionID, email, jobType, jobStartDate, traineePass) VALUES (?, ?, ?, ?, ?, ?, ?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		_dbConnector.setStatememtString(1, dataToInsert.getLabel()) ;
		_dbConnector.setStatememtString(2, dataToInsert.getFirstName()) ;
		_dbConnector.setStatememtInt(3, dataToInsert.getCoachId()) ;
		_dbConnector.setStatememtInt(4, dataToInsert.getRegionId()) ;
		_dbConnector.setStatememtString(5, dataToInsert.getEMail()) ;
		_dbConnector.setStatememtString(6, dataToInsert.getJobType()) ;
		_dbConnector.setStatememtString(7, dataToInsert.getJobStartDate()) ;
		_dbConnector.setStatememtString(8, dataToInsert.getPassword()) ;

		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(true) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		int iTraineeDataId = 0 ;

		ResultSet rs = _dbConnector.getResultSet() ;
		try
		{
			if (rs.next())
			{
				iTraineeDataId = rs.getInt(1) ;
				dataToInsert.setId(iTraineeDataId) ;
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

		Logger.trace(sFctName + ": user " + _iUserId + " successfuly recorded trainee " + iTraineeDataId, _iUserId, Logger.TraceLevel.STEP) ;

		return true ;
	}

	/**
	 * Update a SiteData in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToUpdate SiteData to be updated
	 * 
	 */
	public boolean updateData(TraineeData dataToUpdate)
	{
		String sFctName = _sClassName + ".updateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		TraineeData foundData = new TraineeData() ;
		if (false == existData(dataToUpdate.getId(), foundData))
			return false ;

		if (foundData.equals(dataToUpdate))
		{
			Logger.trace(sFctName + ": SiteData to update (id = " + dataToUpdate.getId() + ") unchanged; nothing to do", _iUserId, Logger.TraceLevel.SUBSTEP) ;
			return true ;
		}

		return forceUpdateData(dataToUpdate) ;
	}

	/**
	 * Check if there is any TraineeData with this Id in database and, if true get its content
	 * 
	 * @return True if found, else false
	 * @param iDataId ID of TraineeData to check
	 * @param foundData TraineeData to get existing information
	 * 
	 */
	public boolean existData(int iDataId, TraineeData foundData)
	{
		String sFctName = _sClassName + ".existData" ;

		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM trainee WHERE id = ?" ;

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
			Logger.trace(sFctName + ": no TraineeData found for id = " + iDataId, _iUserId, Logger.TraceLevel.WARNING) ;
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
	 * @param  dataToUpdate TraineeData to update
	 * 
	 */
	private boolean forceUpdateData(TraineeData dataToUpdate)
	{
		String sFctName = _sClassName + ".forceUpdateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		// Prepare SQL query
		//
		String sQuery = "UPDATE trainee SET traineeLabel = ?, traineeFirst = ?, coachID = ?, regionID = ?, email = ?, jobType = ?, jobStartDate = ?, traineePass = ?" +
				" WHERE " +
				"id = '" + dataToUpdate.getId() + "'" ; 

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		_dbConnector.setStatememtString(1, dataToUpdate.getLabel()) ;
		_dbConnector.setStatememtString(2, dataToUpdate.getFirstName()) ;
		_dbConnector.setStatememtInt(3, dataToUpdate.getCoachId()) ;
		_dbConnector.setStatememtInt(4, dataToUpdate.getRegionId()) ;
		_dbConnector.setStatememtString(5, dataToUpdate.getEMail()) ;
		_dbConnector.setStatememtString(6, dataToUpdate.getJobType()) ;
		_dbConnector.setStatememtString(7, dataToUpdate.getJobStartDate()) ;
		_dbConnector.setStatememtString(8, dataToUpdate.getPassword()) ;

		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(false) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		Logger.trace(sFctName + ": updated data for TraineeData " + dataToUpdate.getId(), _iUserId, Logger.TraceLevel.SUBSTEP) ;

		_dbConnector.closePreparedStatement() ;

		return true ;
	}

	/**
	 * Fill a structure with all the different {@link TraineeData} for a given coach
	 * 
	 * @param iUserId   ID of user
	 * @param aTrainees List of {@link TraineeData} to fill
	 * @param iCoachId  ID of the coach to get trainees for 
	 */
	public void fillTraineesForCoach(int iUserID, List<TraineeData> aTrainees, int iCoachId)
	{
		String sFctName = _sClassName + ".fillTraineesForCoach" ;

		if ((null == _dbConnector) || (-1 == iCoachId) || (null == aTrainees))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		String sQuery = "SELECT * FROM trainee WHERE coachID = ?" ;

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iCoachId) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no trainee found for coach " + iCoachId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		try
		{
			while (rs.next())
			{
				TraineeData foundData = new TraineeData() ;
				fillDataFromResultSet(rs, foundData) ;

				if (false == aTrainees.contains(foundData))
					aTrainees.add(foundData) ;
			}
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}

		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
	}

	/**
	 * Fill a structure with all the different {@link TraineeData} for a given region
	 * 
	 * @param aTrainees List of {@link TraineeData} to fill
	 * @param iRegionId Identifier of the region to get trainees for
	 */
	public void fillTraineesForRegion(List<TraineeData> aTrainees, int iRegionId)
	{
		String sFctName = _sClassName + ".fillTraineesForRegion" ;

		if ((null == _dbConnector) || (-1 == iRegionId) || (null == aTrainees))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		String sQuery = "SELECT * FROM trainee WHERE regionID = ?" ;

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iRegionId) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no trainee found for region " + iRegionId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		try
		{
			while (rs.next())
			{
				TraineeData foundData = new TraineeData() ;
				fillDataFromResultSet(rs, foundData) ;

				if (false == aTrainees.contains(foundData))
					aTrainees.add(foundData) ;
			}
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}

		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
	}

	/**
	 * Fill a structure with all the different {@link TraineeData} for a given zone (set of regions)
	 * 
	 * @param aTrainees List of {@link TraineeData} to fill
	 * @param iZoneId   Identifier of the zone to get trainees for
	 */
	public void fillTraineesForZone(List<TraineeData> aTrainees, int iZoneId)
	{
		String sFctName = _sClassName + ".fillTraineesForZone" ;

		if ((null == _dbConnector) || (-1 == iZoneId) || (null == aTrainees))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		RegionDataManager regionManager = new RegionDataManager(_iUserId, _dbConnector) ;

		List<RegionData> aRegionsForZone = new ArrayList<RegionData>() ;
		regionManager.fillRegionsForZone(aRegionsForZone, iZoneId) ;

		if (aRegionsForZone.isEmpty())
			return ;

		for (RegionData region : aRegionsForZone)
			fillTraineesForRegion(aTrainees, region.getId()) ;
	}

	/**
	 * Get all TraineeData in database
	 * 
	 * @return True if found, else false
	 * @param aUsers Array of TraineeData to fill with database content
	 * 
	 */
	public boolean getThemAll(List<TraineeData> atrainees)
	{
		String sFctName = _sClassName + ".getThemAll" ;

		if ((null == _dbConnector) || (null == atrainees))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM trainee" ;

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
			Logger.trace(sFctName + ": no TraineeData found", _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		try
		{
			while (rs.next())
			{
				TraineeData foundData = new TraineeData() ;
				fillDataFromResultSet(rs, foundData) ;
				atrainees.add(foundData) ;	    	
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
	 * Get the list of existing jobs
	 * 
	 * @return The list of jobs if everything went well, <code>null</code> if not
	 */
	public List<String> getJobsList()
	{
		String sFctName = _sClassName + ".getJobsList" ;
		
		if (null == _dbConnector)
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return null ;
		}

		String sQuery = "SELECT UNIQUE jobType FROM trainee" ;

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return null ;
		}

		List<String> aJobs = new ArrayList<>() ;
		
		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no TraineeData found", _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return aJobs ;
		}

		try
		{
			while (rs.next())
				aJobs.add(rs.getString("jobType")) ;
		} 
		catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}

		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
		
		return aJobs ;
	}
	
	/**
	 * Initialize a TraineeData from a query ResultSet 
	 * 
	 * @param rs        ResultSet of a query
	 * @param foundData TraineeData to fill
	 * 
	 */
	public void fillDataFromResultSet(final ResultSet rs, TraineeData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;

		try
		{
			foundData.setId(rs.getInt("id")) ;
			foundData.setLabel(getNeverNullString(rs, "traineeLabel")) ;
			foundData.setFirstName(getNeverNullString(rs, "traineeFirst")) ;
			foundData.setCoachId(rs.getInt("coachID")) ;
			foundData.setRegionId(rs.getInt("regionID")) ;
			foundData.setEMail(getNeverNullString(rs, "email")) ;
			foundData.setJobType(getNeverNullString(rs, "jobType")) ;
			foundData.setJobStartDate(getNeverNullString(rs, "jobStartDate")) ;
			foundData.setPassword(getNeverNullString(rs, "traineePass")) ;
		} 
		catch (SQLException e) {
			Logger.trace(_sClassName + ".fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}

	protected String getNeverNullString(final ResultSet rs, final String sColumnLabel) throws SQLException
	{
		if ((null == sColumnLabel) || (null == rs))
			return "" ;

		String sValue = rs.getString(sColumnLabel) ;

		if (null == sValue)
			return "" ;

		return sValue.trim() ;
	}
}
