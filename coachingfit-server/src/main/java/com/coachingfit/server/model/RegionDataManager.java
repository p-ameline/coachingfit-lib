package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.coachingfit.shared.database.RegionData;

import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>region</code> table 
 *   
 */
public class RegionDataManager  
{	
	protected final DBConnector _dbConnector ;
	protected final int         _iUserId ;

	/**
	 * Constructor 
	 */
	public RegionDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}

	/**
	 * Insert a RegionData object in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToInsert RegionData to be inserted
	 * 
	 */
	public boolean insertData(RegionData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;

		String sFctName = "RegionDataManager.insertData" ;

		String sQuery = "INSERT INTO region (regionLabel, coachID, zoneID) VALUES (?, ?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		_dbConnector.setStatememtString(1, dataToInsert.getLabel()) ;
		_dbConnector.setStatememtInt(2, dataToInsert.getCoachId()) ;
		_dbConnector.setStatememtInt(3, dataToInsert.getZoneId()) ;

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
	 * Update a SiteData in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToUpdate SiteData to be updated
	 * 
	 */
	public boolean updateData(RegionData dataToUpdate)
	{
		String sFctName = "RegionDataManager.updateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		RegionData foundData = new RegionData() ;
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
	 * Check if there is any RegionData with this Id in database and, if true get its content
	 * 
	 * @return True if found, else false
	 * @param iDataId ID of RegionData to check
	 * @param foundData RegionData to get existing information
	 * 
	 */
	public boolean existData(int iDataId, RegionData foundData)
	{
		String sFctName = "RegionDataManager.existData" ;

		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM region WHERE id = ?" ;

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
	 * Update a SiteData in database
	 * 
	 * @return <code>true</code> if creation succeeded, <code>false</code> if not
	 * @param  dataToUpdate RegionData to update
	 * 
	 */
	private boolean forceUpdateData(RegionData dataToUpdate)
	{
		String sFctName = "RegionDataManager.forceUpdateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		// Prepare SQL query
		//
		String sQuery = "UPDATE region SET regionLabel = ?, coachID = ?, zoneID = ?" +
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
		_dbConnector.setStatememtInt(2, dataToUpdate.getCoachId()) ;
		_dbConnector.setStatememtInt(3, dataToUpdate.getZoneId()) ;

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
	 * Fill a structure with all the different regions for a given coach 
	 * 
	 * @param aRegions  RegionData array to fill
	 * @param iCoachId  ID of the coach to get Regions for 
	 * 
	 */
	public void fillRegionsForCoach(List<RegionData> aRegions, int iCoachId)
	{
		String sFctName = "RegionDataManager.fillRegionsForCoach" ;

		if ((null == _dbConnector) || (-1 == iCoachId) || (null == aRegions))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		String sQuery = "SELECT * FROM region WHERE coachID = ?" ;

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
			Logger.trace(sFctName + ": no Region found for coach " + iCoachId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		try
		{
			while (rs.next())
			{
				RegionData foundData = new RegionData() ;
				fillDataFromResultSet(rs, foundData) ;
				aRegions.add(foundData) ;
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
	 * Fill a structure with all the different regions for a given zone 
	 * 
	 * @param aRegions  RegionData array to fill
	 * @param iCoachId  ID of the coach to get Regions for 
	 * 
	 */
	public void fillRegionsForZone(List<RegionData> aRegions, int iZoneId)
	{
		String sFctName = "RegionDataManager.fillRegionsForZone" ;

		if ((null == _dbConnector) || (-1 == iZoneId) || (null == aRegions))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		String sQuery = "SELECT * FROM region WHERE zoneID = ?" ;

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iZoneId) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no Region found for zone " + iZoneId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		try
		{
			while (rs.next())
			{
				RegionData foundData = new RegionData() ;
				fillDataFromResultSet(rs, foundData) ;
				aRegions.add(foundData) ;
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
	 * Get all RegionData in database
	 * 
	 * @return True if found, else false
	 * @param aUsers Array of RegionData to fill with database content
	 * 
	 */
	public boolean getThemAll(List<RegionData> aRegions)
	{
		String sFctName = "RegionDataManager.getThemAll" ;

		if ((null == _dbConnector) || (null == aRegions))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM region" ;

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
				RegionData foundData = new RegionData() ;
				fillDataFromResultSet(rs, foundData) ;
				aRegions.add(foundData) ;	    	
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
	 * Initialize a RegionData from a query ResultSet 
	 * 
	 * @param rs        ResultSet of a query
	 * @param foundData RegionData to fill
	 * 
	 */
	protected void fillDataFromResultSet(ResultSet rs, RegionData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;

		try
		{
			foundData.setId(rs.getInt("id")) ;
			foundData.setLabel(rs.getString("RegionLabel")) ;
			foundData.setCoachId(rs.getInt("coachID")) ;
			foundData.setZoneId(rs.getInt("zoneID")) ;
		} 
		catch (SQLException e) {
			Logger.trace("RegionDataManager.fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}
}
