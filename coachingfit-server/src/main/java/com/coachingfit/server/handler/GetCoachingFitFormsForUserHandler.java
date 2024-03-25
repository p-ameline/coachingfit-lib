package com.coachingfit.server.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.CoachingFitFormDataManager;
import com.coachingfit.server.model.CoachingFitUserRoleDataManager;
import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.database.CoachingFitUserRoleData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;
import com.coachingfit.shared.rpc.GetCoachingFormsForUserAction;
import com.coachingfit.shared.rpc.GetCoachingFormsForUserResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;
import com.primege.server.model.UserManager;
import com.primege.shared.database.UserData;

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
public class GetCoachingFitFormsForUserHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFormsForUserAction, GetCoachingFormsForUserResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public GetCoachingFitFormsForUserHandler(final Provider<ServletContext>     servletContext,
			                                     final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFormsForUserResult execute(GetCoachingFormsForUserAction action, ExecutionContext context) throws ActionException 
	{
		String sFctName = "GetCoachingFitFormsForUserHandler.execute" ;
		
		GetCoachingFormsForUserResult result = new GetCoachingFormsForUserResult() ;
		
		int iUserId        = action.getUserId() ;
		int iAuthorUserId  = action.getAuthorId() ;
		int iTraineeUserId = action.getSeniorTraineeId() ;
		
		Logger.trace(sFctName + "GetCoachingFitFormsForUserHandler: get forms for userID=" + iUserId + " authorUserID=" + iAuthorUserId + " traineeUserID=" + iTraineeUserId, iUserId, Logger.TraceLevel.STEP) ;
		
		DBConnector dbConnector = new DBConnector(false) ;
		
		// Get user authored forms
		//
		getUserAuthoredForms(dbConnector, iUserId, iAuthorUserId, iTraineeUserId, result) ;
		
		// Authored forms are all that senior trainees can see
		//
		if (iTraineeUserId > 0)
			return result ;
		
		// Get user information to know her role
		//
		UserData user = new UserData() ;
		
		UserManager userManager = new UserManager(iUserId, dbConnector, false) ;
		if (false == userManager.existUser(iUserId, user))
			return result ;
		
		// Get user's roles
		//
		ArrayList<CoachingFitUserRoleData> roles = new ArrayList<CoachingFitUserRoleData>() ;
		
		CoachingFitUserRoleDataManager userRolesManager = new CoachingFitUserRoleDataManager(iUserId, dbConnector) ;
		userRolesManager.fillRolesForUser(iUserId, roles) ;
		
		CoachingFitUser fitUser = new CoachingFitUser(user, roles, null, null, null, null) ;
		
		// Get other forms depending on user's role
		//
		
		// Administrator, get all forms 
		//
		if ((fitUser.hasRole(0, "A")) || (fitUser.hasRole(0, "A2")))
		{
			getFormsForRegion(dbConnector, iUserId, -1, result) ;
			return result ;
		}
		
		//
		// Region director, get all forms for the region 
		//
		for (int i = 1 ; i < 10 ; i++)
			if (fitUser.hasRole(0, "Z" + i))
			{
				getFormsForRegion(dbConnector, iUserId, i, result) ;
				return result ;
			}
				
		if (false == fitUser.hasRole(0, "CV"))
			return result ;
		
		// Sales director, get all forms for current trainees
		//
		
		// First step, get all trainees for this coach
		//
		TraineeDataManager traineesManager = new TraineeDataManager(iUserId, dbConnector) ;
		fitUser.setTrainees(new ArrayList<TraineeData>()) ; 
		traineesManager.fillTraineesForCoach(iUserId, fitUser.getTrainees(), iUserId) ;
		
		if (fitUser.getTrainees().isEmpty())
			return result ;
		
		// Second step, get all forms for these trainees
		//
		for (TraineeData trainee : fitUser.getTrainees())
			getFormsForTrainee(dbConnector, iUserId, trainee.getId(), result) ;
		
		return result ;
	}
	
	/**
	 * Get all user authored forms 
	 */
	protected void getUserAuthoredForms(DBConnector dbConnector, int iUserId, int iAuthorUserId, int iTraineeUserId, GetCoachingFormsForUserResult result) 
	{
		String sFctName = "GetCoachingFitFormsForUserHandler.getUserAuthoredForms" ;
		
		// Build query
		//
		String sQuery = "SELECT * FROM form WHERE" ;
		setQueryWhere("") ;
		
		String sTrace = "" ;
		
		if (-1 != iAuthorUserId)
		{
			addToQueryWhere("userID") ;
			sTrace += " for user = " + iAuthorUserId ;
		}
		
		if (-1 != iTraineeUserId)
		{
			addToQueryWhere("seniorTraineeID") ;
			sTrace += " for senior trainee = " + iTraineeUserId ;
		}
				
		if ("".equals(getQueryWhere()))
		{
			Logger.trace(sFctName + ": empty query " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			result.setMessage("Empty query") ;
			return ;
		}
		
		dbConnector.prepareStatememt(sQuery + getQueryWhere(), Statement.NO_GENERATED_KEYS) ;
		
		int iPos = 1 ;
		
		if (-1 != iAuthorUserId)
			dbConnector.setStatememtInt(iPos++, iAuthorUserId) ;
		if (-1 != iTraineeUserId)
			dbConnector.setStatememtInt(iPos++, iTraineeUserId) ;
		
		if (false == dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			result.setMessage("Database error") ;
			return ;
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
				Logger.trace(sFctName + ": query gave no answer" + sTrace, iUserId, Logger.TraceLevel.WARNING) ;
				dbConnector.closePreparedStatement() ;
				result.setMessage("Query gave no answer.") ;
				return ;
			}
		}
		catch(SQLException ex)
		{
			Logger.trace(sFctName + ": DBConnector.dbSelectPreparedStatement: executeQuery failed for preparedStatement " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLException: " + ex.getMessage(), iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLState: " + ex.getSQLState(), iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": VendorError: " +ex.getErrorCode(), iUserId, Logger.TraceLevel.ERROR) ;        
		}

		dbConnector.closeResultSet() ;
		dbConnector.closePreparedStatement() ;
		
		Logger.trace(sFctName + ": query gave " + iNbCode + " answers" + sTrace, iUserId, Logger.TraceLevel.DETAIL) ;
	}
		
	/**
	 * Get all user authored forms 
	 */
	protected void getFormsForRegion(DBConnector dbConnector, int iUserId, int iRegionId, GetCoachingFormsForUserResult result) 
	{
		String sFctName = "GetCoachingFitFormsForUserHandler.getFormsForRegion" ;
		
		// Build query
		//
		String sQuery = "SELECT * FROM form" ;
		
		if (-1 != iRegionId)
			sQuery += " WHERE regionID = ?" ;
		
		dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		
		if (-1 != iRegionId)
			dbConnector.setStatememtInt(1, iRegionId) ;
		
		if (false == dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			result.setMessage("Database error") ;
			return ;
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
				
				if (formData.isValid())
					result.addFormData(formData) ;
				
				iNbCode++ ;
			}
			
			if (0 == iNbCode)
			{
				Logger.trace(sFctName + ": query gave no answer for region = " + iRegionId, iUserId, Logger.TraceLevel.WARNING) ;
				dbConnector.closePreparedStatement() ;
				result.setMessage("Query gave no answer.") ;
				return ;
			}
		}
		catch(SQLException ex)
		{
			Logger.trace(sFctName + ": DBConnector.dbSelectPreparedStatement: executeQuery failed for preparedStatement " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLException: " + ex.getMessage(), iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLState: " + ex.getSQLState(), iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": VendorError: " +ex.getErrorCode(), iUserId, Logger.TraceLevel.ERROR) ;        
		}

		dbConnector.closeResultSet() ;
		dbConnector.closePreparedStatement() ;
		
		Logger.trace(sFctName + ": query gave " + iNbCode + " answers for region = " + iRegionId, iUserId, Logger.TraceLevel.DETAIL) ;
	}
	
	/**
	 * Get all user authored forms 
	 */
	protected void getFormsForTrainee(DBConnector dbConnector, int iUserId, int iTraineeId, GetCoachingFormsForUserResult result) 
	{
		String sFctName = "GetCoachingFitFormsForUserHandler.getFormsForTrainee" ;
		
		// Build query
		//
		String sQuery = "SELECT * FROM form WHERE traineeID = ?" ;
		
		dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		dbConnector.setStatememtInt(1, iTraineeId) ;
		
		if (false == dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			result.setMessage("Database error") ;
			return ;
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
				
				if (formData.isValid())
					result.addFormData(formData) ;
				
				iNbCode++ ;
			}
			
			if (0 == iNbCode)
			{
				Logger.trace(sFctName + ": query gave no answer for trainee = " + iTraineeId, iUserId, Logger.TraceLevel.WARNING) ;
				dbConnector.closePreparedStatement() ;
				result.setMessage("Query gave no answer.") ;
				return ;
			}
		}
		catch(SQLException ex)
		{
			Logger.trace(sFctName + ": DBConnector.dbSelectPreparedStatement: executeQuery failed for preparedStatement " + sQuery, iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLException: " + ex.getMessage(), iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLState: " + ex.getSQLState(), iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": VendorError: " +ex.getErrorCode(), iUserId, Logger.TraceLevel.ERROR) ;        
		}

		dbConnector.closeResultSet() ;
		dbConnector.closePreparedStatement() ;
		
		Logger.trace(sFctName + ": query gave " + iNbCode + " answers for trainee = " + iTraineeId, iUserId, Logger.TraceLevel.DETAIL) ;
	}
	
	@Override
	public Class<GetCoachingFormsForUserAction> getActionType() {
		return GetCoachingFormsForUserAction.class ;
	}

	@Override
	public void rollback(GetCoachingFormsForUserAction action, GetCoachingFormsForUserResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
