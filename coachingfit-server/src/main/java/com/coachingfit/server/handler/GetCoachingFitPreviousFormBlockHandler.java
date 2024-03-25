package com.coachingfit.server.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.CoachingFitFormDataManager;
import com.coachingfit.server.model.GetCoachingFitFormInBase;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.rpc.GetCoachingFitPreviousFormBlockAction;
import com.coachingfit.shared.rpc.GetCoachingFitPreviousFormBlockResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;
import com.primege.server.model.ArchetypeDataManager;
import com.primege.shared.database.ArchetypeData;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetCoachingFitPreviousFormBlockHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitPreviousFormBlockAction, GetCoachingFitPreviousFormBlockResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;

	protected       DBConnector _dbConnector ;
	protected       int         _iUserId ;

	@Inject
	public GetCoachingFitPreviousFormBlockHandler(final Provider<ServletContext> servletContext,
			final Provider<HttpServletRequest> servletRequest)
	{
		super() ;

		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitPreviousFormBlockResult execute(GetCoachingFitPreviousFormBlockAction action, ExecutionContext context) throws ActionException 
	{
		String sFctName = "GetCoachingFitPreviousFormBlockHandler.execute" ;
		
		GetCoachingFitPreviousFormBlockResult result = new GetCoachingFitPreviousFormBlockResult() ;
		
		_iUserId     = action.getUserId() ;
		_dbConnector = new DBConnector(false) ;
		
		int iTraineeId   = action.getTraineeId() ;
		
		String sRootConcept = getRootForArchetype(action.getArchetypeId()) ;

		// First, get the most recent session date for this trainee
		//
		String sMostRecentSessionDate = getMostRecentSessionDate(iTraineeId, sRootConcept) ;
		if ((null == sMostRecentSessionDate) || "".equals(sMostRecentSessionDate))
		{
			Logger.trace(sFctName + ": query gave no answer", _iUserId, Logger.TraceLevel.WARNING) ;
			result.setMessage("Query gave no answer.") ;
			return result ;
		}

		// Next get the form record for this session
		//
		CoachingFitFormData foundData = new CoachingFitFormData() ;
		if (false == existDataForDate(iTraineeId, sRootConcept, sMostRecentSessionDate, foundData))
		{
			Logger.trace(sFctName + ": cannot get form record for trainee " + iTraineeId + " and session date " + sMostRecentSessionDate, _iUserId, Logger.TraceLevel.ERROR) ;
			result.setMessage("Database error, cannot get form information.") ;
			return result ;
		}

		result.getFormBlock().setDocumentLabel(foundData) ;

		GetCoachingFitFormInBase formGetter = new GetCoachingFitFormInBase(_iUserId, _dbConnector) ;
		formGetter.loadFormData(foundData.getFormId(), result.getFormBlock()) ;

		return result ;
	}

	/**
	 * Get the most recent session date for a trainee
	 * 
	 * @return The date as stored in database if found, <code>""</code> if not
	 * @param iTraineeId ID of trainee to check the latest session date for
	 * 
	 */
	protected String getMostRecentSessionDate(final int iTraineeId, final String sRootConcept)
	{
		if ((null == _dbConnector) || (-1 == iTraineeId))
			return "" ;

		String sFctName = "GetCoachingFitPreviousFormBlockHandler.getMostRecentSessionDate" ;

		String sQuery = "SELECT MAX(formDate) AS MAX_DATE FROM form WHERE traineeID = ?" ;
		if (null != sRootConcept)
			sQuery += " AND root = ?" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.RETURN_GENERATED_KEYS) ;

		if (null == _dbConnector.getPreparedStatement())
		{
			Logger.trace(sFctName + ": cannot get Statement", _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closeAll() ;
			return "" ;
		}

		_dbConnector.setStatememtInt(1, iTraineeId) ;
		if (null != sRootConcept)
			_dbConnector.setStatememtString(2, sRootConcept) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return "" ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no information found for trainee = " + iTraineeId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return "" ;
		}

		String sMaxDate = "" ;

		try
		{
			if (rs.next())
				sMaxDate = rs.getString("MAX_DATE") ;
			else
				Logger.trace(sFctName + ": cannot get MAX(formDate) after query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
		} 
		catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}

		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;

		Logger.trace(sFctName + ": found that most recent session date for trainee " + iTraineeId + " was " + sMaxDate, _iUserId, Logger.TraceLevel.STEP) ;

		return sMaxDate ;
	}

	/**
	 * Check if there is any FormData for a trainee and a session date in database and, if true get its content
	 * 
	 * @param iDataId ID of FormData to check
	 * @param foundData FormData to get existing information
	 *
	 * @return <code>true</code> if found, else <code>false</code>
	 */
	public boolean existDataForDate(final int iTraineeId, final String sRootConcept, final String sSessionDate, CoachingFitFormData foundData)
	{
		String sFctName = "GetCoachingFitPreviousFormBlockHandler.existDataForDate" ;

		if ((null == _dbConnector) || (null == sSessionDate) || "".equals(sSessionDate) || (null == foundData))
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		String sQuery = "SELECT * FROM form WHERE traineeID = ? AND formDate = ?" ;
		if (null != sRootConcept)
			sQuery += " AND root = ?" ;

		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtInt(1, iTraineeId) ;
		_dbConnector.setStatememtString(2, sSessionDate) ;
		
		if (null != sRootConcept)
			_dbConnector.setStatememtString(3, sRootConcept) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		if (null == rs)
		{
			Logger.trace(sFctName + ": no FormData found for traineeID = " + iTraineeId + " and session date " + sSessionDate, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}

		try
		{
			if (rs.next())
			{
				CoachingFitFormDataManager manager = new CoachingFitFormDataManager(_iUserId, _dbConnector) ;

				manager.fillDataFromResultSet(rs, foundData) ;

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
	 * Get archetype's root concept
	 * 
	 * @param iArchetypeId Archetype's identifier
	 * 
	 * @return The root concept if found (may be <code>""</code>), <code>null</code> if not
	 */
	private String getRootForArchetype(int iArchetypeId)
	{
		// Get the corresponding ArchetypeData 
		//
		ArchetypeData archetypeData = new ArchetypeData() ;
		
		ArchetypeDataManager archetypeDataManager = new ArchetypeDataManager(_iUserId, _dbConnector) ;
		if (false == archetypeDataManager.existData(iArchetypeId, archetypeData))
			return null ;
		
		return archetypeData.getRoot() ;
	}
	
	@Override
	public Class<GetCoachingFitPreviousFormBlockAction> getActionType() {
		return GetCoachingFitPreviousFormBlockAction.class;
	}

	@Override
	public void rollback(GetCoachingFitPreviousFormBlockAction action, GetCoachingFitPreviousFormBlockResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
