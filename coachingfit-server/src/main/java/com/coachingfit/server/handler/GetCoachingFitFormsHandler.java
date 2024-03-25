package com.coachingfit.server.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.CoachingFitFormDataManager;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.rpc.GetCoachingFormsAction;
import com.coachingfit.shared.rpc.GetCoachingFormsResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Generic forms server<br><br>
 * Forms can be queried by archetype, trainee, author, entry date or event date  
 * 
 * @author Philippe
 *
 */
public class GetCoachingFitFormsHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFormsAction, GetCoachingFormsResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitFormsHandler(final Provider<ServletContext>     servletContext,
			                              final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFormsResult execute(GetCoachingFormsAction action, ExecutionContext context) throws ActionException 
	{
		String sFctName = "GetCoachingFitFormsHandler.execute" ;
		
		GetCoachingFormsResult result = new GetCoachingFormsResult() ;
		
		int iUserId = action.getUserId() ;
		
		// Build query
		//
		String sQuery = "SELECT * FROM form WHERE" ;
		setQueryWhere("") ;
		
		int iArchetypeId = action.getArchetypeId() ;
		if (-1 != iArchetypeId)
			addToQueryWhere("archetypeID") ;
		
		int iTraineeId = action.getTraineeId() ;
		if (-1 != iTraineeId)
			addToQueryWhere("traineeID") ;
		
		int iAuthorId = action.getAuthorId() ;
		if (-1 != iAuthorId)
			addToQueryWhere("userID") ;
		
		int iSeniorTraineeId = action.getSeniorTraineeId() ;
		if (-1 != iSeniorTraineeId)
			addToQueryWhere("seniorTraineeID") ;
		
		String sEventDateFrom = action.getSessionDateFrom() ;
		String sEventDateTo   = action.getSessionDateTo() ;
		addToQueryWhereForDateInterval("formDate", sEventDateFrom, sEventDateTo) ;
		
		String sEntryDateFrom = action.getEntryDateFrom() ;
		String sEntryDateTo   = action.getEntryDateTo() ;
		addToQueryWhereForDateInterval("formEntryDate", sEntryDateFrom, sEntryDateTo) ;
		
		if ("".equals(getQueryWhere()))
		{
			Logger.trace(sFctName + ": empty query " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			result.setMessage("Empty query") ;
			return result ;
		}
		
		DBConnector dbConnector = new DBConnector(false) ;

		dbConnector.prepareStatememt(sQuery + getQueryWhere(), Statement.NO_GENERATED_KEYS) ;
		
		int iPos = 1 ;
		
		if (-1 != iArchetypeId)
			dbConnector.setStatememtInt(iPos++, iArchetypeId) ;
		if (-1 != iTraineeId)
			dbConnector.setStatememtInt(iPos++, iTraineeId) ;
		if (-1 != iAuthorId)
			dbConnector.setStatememtInt(iPos++, iAuthorId) ;
		if (-1 != iSeniorTraineeId)
			dbConnector.setStatememtInt(iPos++, iSeniorTraineeId) ;
		if (false == "".equals(sEventDateFrom))
			dbConnector.setStatememtString(iPos++, sEventDateFrom) ;
		if (false == "".equals(sEventDateTo))
			dbConnector.setStatememtString(iPos++, sEventDateTo) ;
		if (false == "".equals(sEntryDateFrom))
			dbConnector.setStatememtString(iPos++, sEntryDateFrom) ;
		if (false == "".equals(sEntryDateTo))
			dbConnector.setStatememtString(iPos++, sEntryDateTo) ;
		
		if (false == dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			result.setMessage("Database error") ;
			return result ;
		}

		CoachingFitFormDataManager formManager = new CoachingFitFormDataManager(iUserId, dbConnector) ;
		
		int iNbCode = 0 ;

		ResultSet rs = dbConnector.getResultSet() ;
		try
		{        
			while (rs.next())
			{
				CoachingFitFormData formData = new CoachingFitFormData() ;
				formManager.fillDataFromResultSet(rs, formData) ;
				
				if (false == formData.isReallyDeleted())
					result.addFormData(formData) ;
				
				iNbCode++ ;
			}
			
			if (0 == iNbCode)
			{
				Logger.trace(sFctName + ": query gave no answer", iUserId, Logger.TraceLevel.WARNING) ;
				dbConnector.closePreparedStatement() ;
				result.setMessage("Query gave no answer.") ;
				return result ;
			}
		}
		catch(SQLException ex)
		{
			Logger.trace(sFctName + ": DBConnector.dbSelectPreparedStatement: executeQuery failed for preparedStatement " + sQuery, action.getUserId(), Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLException: " + ex.getMessage(), action.getUserId(), Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLState: " + ex.getSQLState(), action.getUserId(), Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": VendorError: " +ex.getErrorCode(), action.getUserId(), Logger.TraceLevel.ERROR) ;        
		}

		dbConnector.closeResultSet() ;
		dbConnector.closePreparedStatement() ;
		
		Logger.trace(sFctName + ": query gave " + iNbCode + " answers", iUserId, Logger.TraceLevel.DETAIL) ;
		
		return result ;
	}
			
	@Override
	public Class<GetCoachingFormsAction> getActionType() {
		return GetCoachingFormsAction.class;
	}

	@Override
	public void rollback(GetCoachingFormsAction action, GetCoachingFormsResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
