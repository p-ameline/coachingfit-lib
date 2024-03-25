package com.coachingfit.server.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.DbParameters;
import com.coachingfit.server.model.CoachingFitUserRoleDataManager;
import com.coachingfit.server.model.TraineeByCoachDataManager;
import com.coachingfit.server.model.TraineeByCoachGenericDataManager;
import com.coachingfit.server.model.TraineeByTraineeDataManager;
import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.CoachingFitUserRoleData;
import com.coachingfit.shared.database.TraineeByTraineeData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.CoachingFitLoginUserInfo;
import com.coachingfit.shared.rpc.CoachingFitLoginUserResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.model.ArchetypeDataManager;
import com.primege.shared.database.ArchetypeData;
import com.primege.shared.database.UserData;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/** 
 * Object in charge of user login  
 *   
 */
public class SendCoachingFitLoginHandler implements ActionHandler<CoachingFitLoginUserInfo, CoachingFitLoginUserResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	protected       DBConnector                  _dbConnector ;
	protected       int                          _iUserId ;
	
	@Inject
	public SendCoachingFitLoginHandler(final Provider<ServletContext>     servletContext,
			                               final Provider<HttpServletRequest> servletRequest)
	{
		super() ;
		
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}
	
	/**
	  * Check if login information is valid, then return a User
	  * 
	  * @return The LoginUserResult object that answers client request
	  * 
	  * @param action  The LoginUserInfo object that conveys client's request
	  * @param context Technical context
	  * 
	  */
	@Override
	public CoachingFitLoginUserResult execute(CoachingFitLoginUserInfo action, ExecutionContext context) throws ActionException 
	{
		String sFctName = "SendCoachingFitLoginHandler.execute" ;
		
		CoachingFitLoginUserResult result = new CoachingFitLoginUserResult() ;
		result.setVersion(DbParameters.getVersion()) ;
		
		String sLogin    = action.getUserName() ;
		String sPassword = action.getPassWord() ;
		
		if (sLogin.equals("") || sPassword.equals(""))
		{
			Logger.trace(sFctName + ": empty parameter", -1, Logger.TraceLevel.ERROR) ;
			return result ;
		}
		
		_dbConnector = new DBConnector(false) ;
		
		// Look for a real user
		//
		boolean bGotUser = getUser(result, sLogin, sPassword) ;
		
		if (bGotUser)
			return result ;
		
		getTrainee(result, sLogin, sPassword) ;
		
		return result ;
	}

	/**
	 * Look for a real user from login and password
	 * 
	 * @param result    Return structure to be filled 
	 * @param sLogin    Login to look for
	 * @param sPassword Password to look for
	 * 
	 * @return <code>true</code> if a user was found from login and password, <code>false</code> if not 
	 */
	public boolean getUser(CoachingFitLoginUserResult result, final String sLogin, final String sPassword) 
	{
		String sFctName = "SendCoachingFitLoginHandler.getUser" ;
			
		if (sLogin.equals("") || sPassword.equals(""))
		{
			Logger.trace(sFctName + ": empty parameter", -1, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		// Database query to find a user from login and password
		//
		String sqlText = "SELECT * FROM user " +
		                         "WHERE userLogn = ? " +
		                           "AND userPass = ?" ;
		
		_dbConnector.prepareStatememt(sqlText, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtString(1, sLogin) ;
		_dbConnector.setStatememtString(2, sPassword) ;
		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + _dbConnector.getPreparedStatement().toString(), -1, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		try
		{        
			if (rs.next())
			{
				// User found, fill the UserData part of the User object 
				//
				UserData userData = new UserData() ;
				
				userData.setId(rs.getInt("id")) ;
				userData.setLogin(rs.getString("userLogn")) ;
				userData.setPassword(rs.getString("userPass")) ;
				userData.setLabel(rs.getString("userLabel")) ;
				userData.setEMail(rs.getString("email")) ;

				result.setUserData(userData) ;
			}
			else
			{
				// User not found, return empty answer object  
				//
				Logger.trace(sFctName + ": no user found for pseudo " + sLogin + " and pass " + sPassword, -1, Logger.TraceLevel.WARNING) ;
				_dbConnector.closePreparedStatement() ;
				return false ;
			}

		}
		catch(SQLException ex)
		{
			Logger.trace(sFctName + ": DBConnector.dbSelectPreparedStatement: executeQuery failed for preparedStatement " + sqlText, -1, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLException: " + ex.getMessage(), -1, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLState: " + ex.getSQLState(), -1, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": VendorError: " +ex.getErrorCode(), -1, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
			
		UserData userData = result.getUserData() ;
		if (null == userData)
			return false ;
		
		_iUserId = userData.getId() ;
		if (-1 == _iUserId)
			return false ;
				
		// User found, get her roles 
		//
		getUserRoles(result) ;
		
		// Get allowed archetypes, since it depends from roles and event
		//
		getUserArchetypes(result) ;
		
		// Get the list of coaches managed by this user
		//
		getCoaches(result) ;
		
		// Get the list of trainees for this user
		//
		getTrainees(result) ;
		
		// Get the list of allowed trainees for this user
		//
		getAllowedTrainees(result) ;
		
		Logger.trace(sFctName + ": user " + _iUserId + " found for pseudo " + sLogin + " and password " + sPassword, -1, Logger.TraceLevel.WARNING) ;
		
		return true ;
	}
	
	/**
	 * Look for a trainee from login (for trainees, the e-mail address) and password
	 * 
	 * @param result    Return structure to be filled 
	 * @param sLogin    Login to look for
	 * @param sPassword Password to look for
	 * 
	 * @return <code>true</code> if a trainee was found from login and password, <code>false</code> if not 
	 */
	public boolean getTrainee(CoachingFitLoginUserResult result, final String sLogin, final String sPassword) 
	{
		String sFctName = "SendCoachingFitLoginHandler.getTrainee" ;
			
		if (sLogin.equals("") || sPassword.equals(""))
		{
			Logger.trace(sFctName + ": empty parameter", -1, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		// Database query to find a user from login and password
		//
		String sqlText = "SELECT * FROM trainee " +
		                         "WHERE email = ? " +
		                           "AND traineePass = ?" ;
		
		_dbConnector.prepareStatememt(sqlText, Statement.NO_GENERATED_KEYS) ;
		_dbConnector.setStatememtString(1, sLogin) ;
		_dbConnector.setStatememtString(2, sPassword) ;
		
		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + _dbConnector.getPreparedStatement().toString(), -1, Logger.TraceLevel.ERROR) ;
			return false ;
		}

		ResultSet rs = _dbConnector.getResultSet() ;
		try
		{        
			if (rs.next())
			{
				// Trainee found, fill the TraineeData part of the User object 
				//
				TraineeData traineeData = new TraineeData() ;
				
				TraineeDataManager manager = new TraineeDataManager(_iUserId, _dbConnector) ;
				manager.fillDataFromResultSet(rs, traineeData) ;

				result.setTrainee(traineeData) ;
			}
			else
			{
				// Trainee not found, return empty answer object  
				//
				Logger.trace(sFctName + ": no trainee found for mail " + sLogin + " and pass " + sPassword, -1, Logger.TraceLevel.WARNING) ;
				_dbConnector.closePreparedStatement() ;
				return false ;
			}

		}
		catch(SQLException ex)
		{
			Logger.trace(sFctName + ": DBConnector.dbSelectPreparedStatement: executeQuery failed for preparedStatement " + sqlText, -1, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLException: " + ex.getMessage(), -1, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLState: " + ex.getSQLState(), -1, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": VendorError: " +ex.getErrorCode(), -1, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
		
		// Trainee not found, leaving
		//
		if (null == result.getTrainee())
			return false ;
		
		// Trainee found, set her role (as her job) 
		//
		setTraineeRole(result) ;
		
		// Get allowed archetypes, since it depends from roles and event
		//
		getTraineeArchetypes(result) ;
		
		// Get the list of trainees for this (senior) trainee
		//
		getTraineesForSenior(result) ;
		
		Logger.trace(sFctName + ": user " + _iUserId + " found for pseudo " + sLogin + " and password " + sPassword, -1, Logger.TraceLevel.WARNING) ;
		
		return true ;
	}
	
	/**
	  * Fills the result object with user's roles 
	  * 
	  * @param result      Object to get completed before being sent back to the requesting client
	  */
	protected void getUserRoles(CoachingFitLoginUserResult result)
	{
		// Create the array of roles
		//
		result.setRoles(new ArrayList<CoachingFitUserRoleData>()) ;
		
		// Get user's roles
		//
		CoachingFitUserRoleDataManager userRolesManager = new CoachingFitUserRoleDataManager(_iUserId, _dbConnector) ;
		userRolesManager.fillRolesForUser(_iUserId, result.getRoles()) ;
	}
	
	/**
	  * Fills the result object with user's allowed archetypes (by default, all)
	  * 
	  * @param result Object to get completed before being sent back to the requesting client
	  */
	protected void getUserArchetypes(CoachingFitLoginUserResult result)
	{
		// First, get all archetypes
		//
		ArrayList<ArchetypeData> candidateArchetypes = new ArrayList<ArchetypeData>() ;
		
		ArchetypeDataManager archetypeManager = new ArchetypeDataManager(_iUserId, _dbConnector) ;
		archetypeManager.getThemAll(candidateArchetypes) ;
		
		if (candidateArchetypes.isEmpty())
		{
			result.setArchetypes(new ArrayList<ArchetypeData>()) ;
			return ;
		}
		
		// Then check which ones are valid for this user
		//
		CoachingFitUserRoleDataManager roleManager = new CoachingFitUserRoleDataManager(_iUserId, _dbConnector) ;
		
		ArrayList<ArchetypeData> userArchetypes = new ArrayList<ArchetypeData>() ;
		
		for (ArchetypeData archetype : candidateArchetypes)
		{
			// If there is a role for this user and this archetype, it provides the information (if not "DEN" then ok)
			//
			CoachingFitUserRoleData roleData = roleManager.getRoleForUserAndArchetype(_iUserId, archetype.getId()) ;
			if (null != roleData)
			{
				if (false == "DEN".equals(roleData.getUserRole()))
					userArchetypes.add(archetype) ;
			}
			else
			{
				// If there is no role for this user and this archetype, get a general information for this archetype
				//
				CoachingFitUserRoleData genericRoleData = roleManager.getRoleForUserAndArchetype(0, archetype.getId()) ;
				if ((null == genericRoleData) || (false == "DEN".equals(genericRoleData.getUserRole())))
					userArchetypes.add(archetype) ;
			}
		}
		
		result.setArchetypes(userArchetypes) ;
	}
		
	/**
	  * Fills the result object with the coaches managed by this user 
	  * 
	  * @param result      Object to get completed before being sent back to the requesting client
	  */
	protected void getCoaches(CoachingFitLoginUserResult result)
	{
		// If role is "CV", then there is no managed coach
		//
		if (result.getUser().hasRole(0, "CV"))
			return ;
		
		// If role is "A", then get all coaches
		//
		if (result.getUser().hasRole(0, "A"))
		{
			getUsersForZone(result, -1) ;
			return ;
		}
		
		for (int i = 0 ; i < 99 ; i++)
		{
			if (result.getUser().hasRole(0, "Z" + i))
			{
				getUsersForZone(result, i) ;
				return ;
			}
		}
	}
	
	/**
	  * Fills the result object with user's trainees 
	  * 
	  * @param result Object to get completed before being sent back to the requesting client
	  */
	protected void getTrainees(CoachingFitLoginUserResult result)
	{
		// Get the list of coaches managed by this user
		//
		List<UserData> aCoaches = result.getUser().getCoaches() ;
		
		// Create the array of trainees
		//
		result.setTrainees(new ArrayList<TraineeData>()) ;
		
		// Step one, get all trainees that are directly attributed to this user or coaches she manages
		//
		TraineeDataManager traineesManager = new TraineeDataManager(_iUserId, _dbConnector) ;
		
		// If coaches list is empty, then only get trainees for this user as a coach
		//
		if ((null == aCoaches) || aCoaches.isEmpty())
			traineesManager.fillTraineesForCoach(_iUserId, result.getTrainees(), _iUserId) ;
		else
			for (UserData coach : aCoaches)
				traineesManager.fillTraineesForCoach(_iUserId, result.getTrainees(), coach.getId()) ;
		
		// Step 2, get all trainees that are attributed as secondary coach
		//
		TraineeByCoachDataManager traineesByCoachManager = new TraineeByCoachDataManager(_iUserId, _dbConnector) ;
		
		// If coaches list is empty, then only get trainees for this user as a coach
		//
		if ((null == aCoaches) || aCoaches.isEmpty())
			traineesByCoachManager.fillTraineesForCoach(result.getTrainees(), _iUserId, traineesManager) ;
		else
			for (UserData coach : aCoaches)
				traineesByCoachManager.fillTraineesForCoach(result.getTrainees(), coach.getId(), traineesManager) ;
	}
	
	/**
	  * Fills the result object with user's allowed trainees 
	  * 
	  * @param result Object to get completed before being sent back to the requesting client
	  */
	protected void getAllowedTrainees(CoachingFitLoginUserResult result)
	{
		// Get all trainees that comply to generic rules 
		//
		TraineeByCoachGenericDataManager genericTraineesManager = new TraineeByCoachGenericDataManager(_iUserId, _dbConnector) ;
		
		List<TraineeData> aGenericTrainees = new ArrayList<TraineeData>() ;
		genericTraineesManager.fillTraineesForCoach(aGenericTrainees, _iUserId) ;
		
		if (aGenericTrainees.isEmpty())
			return ;
		
		// Create the array of trainees
		//
		result.setAllowedTrainees(new ArrayList<TraineeData>()) ;
		
		// Fill the list of allowed trainees from those who comply to generic rules and don't belong to the team
		//
		List<TraineeData> aTrainees = result.getTrainees() ;
		
		if (null == aTrainees)
		{
			result.getAllowedTrainees().addAll(aGenericTrainees) ;
			return ;
		}
		
		for (TraineeData genericTrainee : aGenericTrainees)
			if (false == aTrainees.contains(genericTrainee))
				result.getAllowedTrainees().add(genericTrainee) ;
	}
	
	/**
	  * Fills the result object with coaches (users inside a zone)  
	  * 
	  * @param result  Object to get completed before being sent back to the requesting client
	  * @param iZoneId Zone identifier or all zones if -1
	  * 
	  */
	protected void getUsersForZone(CoachingFitLoginUserResult result, final int iZoneId)
	{
		String sFctName = "SendCoachingFitLoginHandler.getUsersForZone" ;
		
		if (null == _dbConnector)
		{
			Logger.trace(sFctName + ": bad parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		String sQuery = "" ;
		if (-1 == iZoneId)
			sQuery = "SELECT * FROM user WHERE id IN (SELECT coachID FROM region)" ;
		else
			sQuery = "SELECT * FROM user WHERE id IN (SELECT coachID FROM region WHERE zoneID = ?)" ;
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		
		if (iZoneId >= 0)
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
			Logger.trace(sFctName + ": no coach found for zone = " + iZoneId, _iUserId, Logger.TraceLevel.WARNING) ;
			_dbConnector.closePreparedStatement() ;
			return ;
		}
		
		try
		{
	    while (rs.next())
	    {
	    	UserData foundData = new UserData() ; 
	    	
	    	foundData.setId(rs.getInt("id")) ;
				foundData.setLogin(rs.getString("userLogn")) ;
				foundData.setPassword(rs.getString("userPass")) ;
				foundData.setLabel(rs.getString("userLabel")) ;
				foundData.setEMail(rs.getString("email")) ;
	    	
				result.getUser().addCoach(foundData) ;
	    }
		} 
		catch (SQLException e)
		{
			Logger.trace(sFctName + ": exception when iterating results " + e.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
		}
		
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
	}
	
	@Override
	public Class<CoachingFitLoginUserInfo> getActionType() {
		return CoachingFitLoginUserInfo.class;
	}

	@Override
	public void rollback(CoachingFitLoginUserInfo action, CoachingFitLoginUserResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
	
	/**
	  * Fills the result object with user's role from trainee's job type 
	  * 
	  * @param result Object to get completed before being sent back to the requesting client
	  */
	protected void setTraineeRole(CoachingFitLoginUserResult result)
	{
		// 
		//
		if ((null == result) || (null == result.getTrainee()))
			return ;
		
		TraineeData trainee = result.getTrainee() ;
		
		if (null == trainee)
			return ;
		
		// Create the array of roles and add trainee's job as a role
		//
		List<CoachingFitUserRoleData> aRoles = new ArrayList<CoachingFitUserRoleData>() ;
		aRoles.add(new CoachingFitUserRoleData(-1, -1, -1, trainee.getJobType())) ;
		
		result.setRoles(aRoles) ;
	}
	
	/**
	  * Fills the result object with trainee's allowed archetypes (by default, none) 
	  * 
	  * @param result Object to get completed before being sent back to the requesting client
	  */
	protected void getTraineeArchetypes(CoachingFitLoginUserResult result)
	{
		// Get the trainee (to get her job)
		//
		TraineeData trainee = result.getTrainee() ;
		
		if (null == trainee)
			return ;
		
		String sTraineeJob = trainee.getJobType() ;
		
		// First, get all archetypes
		//
		List<ArchetypeData> candidateArchetypes = new ArrayList<ArchetypeData>() ;
		
		ArchetypeDataManager archetypeManager = new ArchetypeDataManager(_iUserId, _dbConnector) ;
		archetypeManager.getThemAll(candidateArchetypes) ;
		
		if (candidateArchetypes.isEmpty())
		{
			result.setArchetypes(new ArrayList<ArchetypeData>()) ;
			return ;
		}
		
		// Then check which ones are valid for this senior trainee's job
		//
		CoachingFitUserRoleDataManager roleManager = new CoachingFitUserRoleDataManager(_iUserId, _dbConnector) ;
		
		List<ArchetypeData> userArchetypes = new ArrayList<ArchetypeData>() ;
		
		for (ArchetypeData archetype : candidateArchetypes)
		{
			// If there is a role for this senior trainee's job and this archetype, add it to the list
			//
			CoachingFitUserRoleData roleData = roleManager.getRoleForTraineeJobAndArchetype(sTraineeJob, archetype.getId()) ;
			if (null != roleData)
				userArchetypes.add(archetype) ;
		}
		
		result.setArchetypes(userArchetypes) ;
	}
	
	/**
	  * Fills the result object with senior's trainees 
	  * 
	  * @param result Object to get completed before being sent back to the requesting client
	  */
	protected void getTraineesForSenior(CoachingFitLoginUserResult result)
	{
		// Get the senior trainee
		//
		TraineeData senior = result.getTrainee() ;
			
		if (null == senior)
			return ;
		
		// Create the array of trainees
		//
		result.setTrainees(new ArrayList<TraineeData>()) ;
		
		// Get the id of all trainees that can get coached by this senior
		//
		List<TraineeByTraineeData> aTraineesByTrainee = new ArrayList<TraineeByTraineeData>() ;
		
		TraineeByTraineeDataManager traineesByTraineeManager = new TraineeByTraineeDataManager(_iUserId, _dbConnector) ;
		traineesByTraineeManager.fillTraineesForSenior(_iUserId, aTraineesByTrainee, senior.getId()) ;
		
		if (aTraineesByTrainee.isEmpty())
			return ;
		
		// Get all trainees from their identifiers
		//
		TraineeDataManager traineesManager = new TraineeDataManager(_iUserId, _dbConnector) ;
		
		for (TraineeByTraineeData TBT : aTraineesByTrainee)
		{
			TraineeData trainee = new TraineeData() ; 
			if (traineesManager.existData(TBT.getTraineeId(), trainee))
				result.getTrainees().add(trainee) ;
		}
	}
}
