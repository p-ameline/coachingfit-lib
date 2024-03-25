package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.coachingfit.shared.database.TraineeByCoachGenericData;
import com.coachingfit.shared.database.TraineeData;
import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>traineeByCoach</code> table 
 *   
 */
public class TraineeByCoachGenericDataManager
{
	private final static String _sClassName = "TraineeByCoachGenericDataManager" ;
	private final static String _sTableName = "traineesByCoachGeneric" ;

	private final DBConnector _dbConnector ;
	private final int         _iUserId ;

	/**
	 * Constructor 
	 */
	public TraineeByCoachGenericDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}

	/**
	 * Insert a {@link TraineeByCoachGenericData} object in database
	 * 
	 * @return true if successful, false if not
	 * 
	 * @param dataToInsert {@link TraineeByCoachGenericData} to be inserted
	 */
	public boolean insertData(TraineeByCoachGenericData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;

		String sFctName = _sClassName + ".insertData" ;

		String sQuery = "INSERT INTO " + _sTableName + " (coachID, parameterType, parameterID) VALUES (?, ?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		_dbConnector.setStatememtInt(1,    dataToInsert.getCoachId()) ;
		_dbConnector.setStatememtString(2, dataToInsert.getParameterType()) ;
		_dbConnector.setStatememtInt(3,    dataToInsert.getParameterId()) ;

		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(true) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		int itraineesByCoachGenericDataId = 0 ;

		ResultSet rs = _dbConnector.getResultSet() ;
		try
		{
			if (rs.next())
			{
				itraineesByCoachGenericDataId = rs.getInt(1) ;
				dataToInsert.setId(itraineesByCoachGenericDataId) ;
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

		Logger.trace(sFctName + ": user " + _iUserId + " successfuly recorded traineesByCoachGeneric " + itraineesByCoachGenericDataId, _iUserId, Logger.TraceLevel.STEP) ;

		return true ;
	}

	/**
	 * Update a TraineeByCoachGenericData in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToUpdate {@link TraineeByCoachGenericData} to be updated
	 * 
	 */
	public boolean updateData(TraineeByCoachGenericData dataToUpdate)
	{
		String sFctName = _sClassName + ".updateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		TraineeByCoachGenericData foundData = new TraineeByCoachGenericData() ;
		if (false == existData(dataToUpdate.getId(), foundData))
			return false ;

		if (foundData.equals(dataToUpdate))
		{
			Logger.trace(sFctName + ": TraineeByCoachGenericData to update (id = " + dataToUpdate.getId() + ") unchanged; nothing to do", _iUserId, Logger.TraceLevel.SUBSTEP) ;
			return true ;
		}

		return forceUpdateData(dataToUpdate) ;
	}

	/**
	 * Check if there is any TraineeByCoachGenericData with this Id in database and, if true get its content
	 * 
	 * @return <code>true</code> if found, else <code>false</code>
	 * 
	 * @param iDataId ID of RegionData to check
	 * @param foundData {@link TraineeByCoachGenericData} to get existing information
	 * 
	 */
	public boolean existData(int iDataId, TraineeByCoachGenericData foundData)
	{
		String sFctName = _sClassName + ".existData" ;

		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM " + _sTableName + " WHERE id = ?" ;

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
			Logger.trace(sFctName + ": no TraineeByCoachGenericData found for id = " + iDataId, _iUserId, Logger.TraceLevel.WARNING) ;
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
	 * Update a {@link TraineeByCoachGenericData} in database
	 * 
	 * @return <code>true</code> if creation succeeded, <code>false</code> if not
	 * 
	 * @param  dataToUpdate RegionData to update
	 */
	private boolean forceUpdateData(TraineeByCoachGenericData dataToUpdate)
	{
		String sFctName = _sClassName + ".forceUpdateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		// Prepare SQL query
		//
		String sQuery = "UPDATE " + _sTableName + " SET coachID = ?, parameterType = ?, parameterID = ?" +
				" WHERE " +
				"id = '" + dataToUpdate.getId() + "'" ; 

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		_dbConnector.setStatememtInt(1,    dataToUpdate.getCoachId()) ;
		_dbConnector.setStatememtString(2, dataToUpdate.getParameterType()) ;
		_dbConnector.setStatememtInt(3,    dataToUpdate.getParameterId()) ;

		// Execute query 
		//
		int iNbAffectedRows = _dbConnector.executeUpdatePreparedStatement(false) ;
		if (-1 == iNbAffectedRows)
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		Logger.trace(sFctName + ": updated data for TraineeByCoachGenericData " + dataToUpdate.getId(), _iUserId, Logger.TraceLevel.SUBSTEP) ;

		_dbConnector.closePreparedStatement() ;

		return true ;
	}

	/**
	 * Fill a structure with all the trainees attributed to a given coach
	 * 
	 * @param aTrainees  List of {@link TraineeByCoachGenericData} to fill
	 * @param iCoachId   ID of the coach to get traineesByCoach records for
	 * 
	 */
	public void fillTraineesForCoach(List<TraineeData> aTrainees, int iCoachId)
	{
		String sFctName = _sClassName + ".fillTraineesForCoach" ;

		if ((null == _dbConnector) || (-1 == iCoachId) || (null == aTrainees))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		String sQuery = "SELECT * FROM " + _sTableName + " WHERE coachID = ?" ;

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
			Logger.trace(sFctName + ": no traineesByCoach found for coach " + iCoachId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}

		try
		{
			while (rs.next())
			{
				TraineeByCoachGenericData foundData = new TraineeByCoachGenericData() ;
				fillDataFromResultSet(rs, foundData) ;
				
				fillTraineesFromGenericRule(foundData, aTrainees) ;
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
	 * Get the list of trainees that fit a generic rule
	 * 
	 * @param rule      Rule to comply with
	 * @param aTrainees List of {@link TraineeData} to fill
	 */
	public void fillTraineesFromGenericRule(final TraineeByCoachGenericData rule, List<TraineeData> aTrainees)
	{
		if ((null == aTrainees) || (null == rule) || rule.isEmpty())
			return ;
		
		String sRule = rule.getParameterType().trim() ;
		
		if (sRule.isEmpty())
			return ;
		
		// Get all trainees for a given region
		//
		if (sRule.equalsIgnoreCase("R"))
		{
			TraineeDataManager manager = new TraineeDataManager(_iUserId, _dbConnector) ;
			manager.fillTraineesForRegion(aTrainees, rule.getParameterId()) ;
			return ;
		}
		
		// Get all trainees for a given zone
		//
		if (sRule.equalsIgnoreCase("Z"))
		{
			TraineeDataManager manager = new TraineeDataManager(_iUserId, _dbConnector) ;
			manager.fillTraineesForZone(aTrainees, rule.getParameterId()) ;
			return ;
		}
		
		// Get all trainees for a given coach
		//
		if (sRule.equalsIgnoreCase("C"))
		{
			TraineeDataManager manager = new TraineeDataManager(_iUserId, _dbConnector) ;
			manager.fillTraineesForCoach(_iUserId, aTrainees, rule.getParameterId()) ;
			return ;
		}
	}
	
	/**
	 * Initialize a {@link TraineeByCoachGenericData} from a query ResultSet
	 * 
	 * @param rs        ResultSet of a query
	 * @param foundData TraineeByCoachGenericData to fill
	 * 
	 */
	protected void fillDataFromResultSet(ResultSet rs, TraineeByCoachGenericData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;

		try
		{
			foundData.setId(rs.getInt("id")) ;
			foundData.setCoachId(rs.getInt("coachID")) ;
			foundData.setParameterType(rs.getString("parameterType")) ;
			foundData.setParameterId(rs.getInt("parameterID")) ;
		} 
		catch (SQLException e) {
			Logger.trace(_sClassName + ".fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}
}
