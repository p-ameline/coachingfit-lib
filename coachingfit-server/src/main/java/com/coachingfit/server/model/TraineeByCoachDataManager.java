package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.coachingfit.shared.database.TraineeByCoachData;
import com.coachingfit.shared.database.TraineeData;
import com.primege.server.DBConnector;
import com.primege.server.Logger;

/** 
 * Object in charge of Read/Write operations in the <code>traineeByCoach</code> table 
 *   
 */
public class TraineeByCoachDataManager
{
	private final static String _sClassName = "TraineeByCoachDataManager" ;

	private final DBConnector _dbConnector ;
	private final int         _iUserId ;

	/**
	 * Constructor 
	 */
	public TraineeByCoachDataManager(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}

	/**
	 * Insert a {@link TraineeByCoachData} object in database
	 * 
	 * @return true if successful, false if not
	 * 
	 * @param dataToInsert TraineeByCoachData to be inserted
	 */
	public boolean insertData(TraineeByCoachData dataToInsert)
	{
		if ((null == _dbConnector) || (null == dataToInsert))
			return false ;

		String sFctName = _sClassName + ".insertData" ;

		String sQuery = "INSERT INTO traineesByCoach (coachID, traineeID) VALUES (?, ?)" ;
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return false ;
		}

		_dbConnector.setStatememtInt(1, dataToInsert.getCoachId()) ;
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

		int itraineesByCoachDataId = 0 ;

		ResultSet rs = _dbConnector.getResultSet() ;
		try
		{
			if (rs.next())
			{
				itraineesByCoachDataId = rs.getInt(1) ;
				dataToInsert.setId(itraineesByCoachDataId) ;
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

		Logger.trace(sFctName + ": user " + _iUserId + " successfuly recorded traineesByCoach " + itraineesByCoachDataId, _iUserId, Logger.TraceLevel.STEP) ;

		return true ;
	}

	/**
	 * Update a TraineeByCoachData in database
	 * 
	 * @return true if successful, false if not
	 * @param dataToUpdate {@link TraineeByCoachData} to be updated
	 * 
	 */
	public boolean updateData(TraineeByCoachData dataToUpdate)
	{
		String sFctName = _sClassName + ".updateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		TraineeByCoachData foundData = new TraineeByCoachData() ;
		if (false == existData(dataToUpdate.getId(), foundData))
			return false ;

		if (foundData.equals(dataToUpdate))
		{
			Logger.trace(sFctName + ": TraineeByCoachData to update (id = " + dataToUpdate.getId() + ") unchanged; nothing to do", _iUserId, Logger.TraceLevel.SUBSTEP) ;
			return true ;
		}

		return forceUpdateData(dataToUpdate) ;
	}

	/**
	 * Check if there is any TraineeByCoachData with this Id in database and, if true get its content
	 * 
	 * @return <code>true</code> if found, else <code>false</code>
	 * 
	 * @param iDataId ID of RegionData to check
	 * @param foundData {@link TraineeByCoachData} to get existing information
	 * 
	 */
	public boolean existData(int iDataId, TraineeByCoachData foundData)
	{
		String sFctName = _sClassName + ".existData" ;

		if ((null == _dbConnector) || (-1 == iDataId) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM traineesByCoach WHERE id = ?" ;

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
			Logger.trace(sFctName + ": no TraineeByCoachData found for id = " + iDataId, _iUserId, Logger.TraceLevel.WARNING) ;
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
	 * Update a {@link TraineeByCoachData} in database
	 * 
	 * @return <code>true</code> if creation succeeded, <code>false</code> if not
	 * 
	 * @param  dataToUpdate RegionData to update
	 */
	private boolean forceUpdateData(TraineeByCoachData dataToUpdate)
	{
		String sFctName = _sClassName + ".forceUpdateData" ;

		if ((null == _dbConnector) || (null == dataToUpdate))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		// Prepare SQL query
		//
		String sQuery = "UPDATE traineesByCoach SET coachID = ?, traineeID = ?" +
				" WHERE " +
				"id = '" + dataToUpdate.getId() + "'" ; 

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		_dbConnector.setStatememtInt(1, dataToUpdate.getCoachId()) ;
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

		Logger.trace(sFctName + ": updated data for TraineeByCoachData " + dataToUpdate.getId(), _iUserId, Logger.TraceLevel.SUBSTEP) ;

		_dbConnector.closePreparedStatement() ;

		return true ;
	}

	/**
	 * Fill a structure with all the trainees attributed to a given coach
	 * 
	 * @param aTrainees       List of {@link TraineeByCoachData} to fill
	 * @param iCoachId        ID of the coach to get traineesByCoach records for
	 * @param traineesManager Manager of the trainee table
	 */
	public void fillTraineesForCoach(List<TraineeData> aTrainees, int iCoachId, TraineeDataManager traineesManager)
	{
		String sFctName = _sClassName + ".fillTraineesForCoach" ;

		if ((null == _dbConnector) || (-1 == iCoachId) || (null == aTrainees))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}

		List<TraineeByCoachData> aTraineeByCoach = getTraineesbyCoachForCoach(iCoachId) ;
		
		if ((null == aTraineeByCoach) || aTraineeByCoach.isEmpty())
			return ;
		
		TraineeDataManager traineeManager = traineesManager ;
		if (null == traineeManager)
			traineeManager = new TraineeDataManager(_iUserId, _dbConnector) ;
		
		for (TraineeByCoachData tbc : aTraineeByCoach)
		{
			TraineeData foundData = new TraineeData() ;
			if (traineeManager.existData(tbc.getTraineeId(), foundData) && (false == aTrainees.contains(foundData)))
				aTrainees.add(foundData) ;
		}
	}

	/**
	 * Get the list of all the {@link TraineeByCoachData} attributed to a given coach
	 * 
	 * @param iCoachId   ID of the coach to get traineesByCoach records for
	 * 
	 * @return The list of {@link TraineeByCoachData} if all went well (may be empty), <code>null</code> if not
	 * 
	 */
	public List<TraineeByCoachData> getTraineesbyCoachForCoach(int iCoachId)
	{
		String sFctName = _sClassName + ".getTraineesbyCoachForCoach" ;

		if ((null == _dbConnector) || (-1 == iCoachId))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return null ;
		}

		String sQuery = "SELECT * FROM traineesByCoach WHERE coachID = ?" ;

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iCoachId) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return null ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no traineesByCoach found for coach " + iCoachId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return null ;
		}

		List<TraineeByCoachData> aTraineeByCoach = new ArrayList<TraineeByCoachData>() ;

		try
		{
			while (rs.next())
			{
				TraineeByCoachData foundData = new TraineeByCoachData() ;
				fillDataFromResultSet(rs, foundData) ;
				aTraineeByCoach.add(foundData) ;
			}
		} catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}

		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;

		return aTraineeByCoach ;
	}

	/**
	 * Initialize a {@link TraineeByCoachData} from a query ResultSet
	 * 
	 * @param rs        ResultSet of a query
	 * @param foundData TraineeByCoachData to fill
	 * 
	 */
	protected void fillDataFromResultSet(ResultSet rs, TraineeByCoachData foundData)
	{
		if ((null == rs) || (null == foundData))
			return ;

		try
		{
			foundData.setId(rs.getInt("id")) ;
			foundData.setCoachId(rs.getInt("coachID")) ;
			foundData.setTraineeId(rs.getInt("traineeID")) ;
		} 
		catch (SQLException e) {
			Logger.trace(_sClassName + ".fillDataFromResultSet: exception when processing results set: " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
	}
}
