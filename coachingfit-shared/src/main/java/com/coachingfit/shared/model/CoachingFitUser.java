package com.coachingfit.shared.model ;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.database.CoachingFitUserRoleData;

import com.google.gwt.user.client.rpc.IsSerializable ;

import com.primege.shared.database.ArchetypeData;
import com.primege.shared.database.UserData;
import com.primege.shared.model.UserModel;

/**
 * A user, along with all relevant information (current event, roles and forms she is allowed to fill) 
 * 
 * Created: 20 may 2016
 * Author: PA
 * 
 */
public class CoachingFitUser extends UserModel implements IsSerializable 
{
	private List<CoachingFitUserRoleData> _aRoles ;
	
	/** List of trainees this user is the primary manager of */
	private List<TraineeData>             _aTrainees ;
	
	/** List of trainees this user is allowed to fill sets for */
	private List<TraineeData>             _aAllowedTrainees ;
	
	/** List of other users this user is the manager of */
	private List<UserData>                _aCoaches ;

	/**
	 * Default constructor (with zero information)
	 */
	public CoachingFitUser() 
	{
		super() ;
		
		_aRoles           = null ;
		_aTrainees        = null ;
		_aAllowedTrainees = null ;
		_aCoaches         = null ;
	}
	
	/**
	 * Plain vanilla constructor 
	 */
	public CoachingFitUser(final UserData userData, 
			                   final List<CoachingFitUserRoleData> aRoles,
			                   final List<ArchetypeData> aArchetypes,
			                   final List<TraineeData> aTrainees,
			                   final List<TraineeData> aAllowedTrainees,
			                   final List<UserData> aCoaches) 
	{
		super(userData, aArchetypes) ;
		
		setRoles(aRoles) ;
		setTrainees(aTrainees) ;
		setAllowedTrainees(aAllowedTrainees) ;
		setCoaches(aCoaches) ;
	}
		
	/**
	 * Copy constructor 
	 */
	public CoachingFitUser(final CoachingFitUser model) {
		initFromUser(model) ;
	}
	
	public void reset()
	{
		reset4Model() ;
		
		if (null != _aRoles)
			_aRoles.clear() ;
		if (null != _aTrainees)
			_aTrainees.clear() ;
		if (null != _aAllowedTrainees)
			_aAllowedTrainees.clear() ;
		if (null != _aCoaches)
			_aCoaches.clear() ;
	}
	
	public void initFromUser(final CoachingFitUser model)
	{
		reset() ;
		
		if (null == model)
			return ;
		
		init(model.getUserData(), model._aRoles, model.getArchetypes(), model._aTrainees, model._aAllowedTrainees, model._aCoaches) ;
	}
	
	public void init(final UserData userData, final List<CoachingFitUserRoleData> aRoles, final List<ArchetypeData> aArchetypes, final List<TraineeData> aTrainees, final List<TraineeData> aAllowedTrainees, final List<UserData> aCoaches)
	{
		reset() ;
		
		if ((null == userData) && (null == aRoles) && (null == aArchetypes))
			return ;
	
		initModel(userData, aArchetypes) ;
		
		if (null != aRoles)
			setRoles(aRoles) ;
		
		if (null != aTrainees)
			setTrainees(aTrainees) ;
		
		if (null != aAllowedTrainees)
			setAllowedTrainees(aAllowedTrainees) ;
		
		if (null != aCoaches)
			setCoaches(aCoaches) ;
	}
	
	public List<CoachingFitUserRoleData> getRoles() {
		return _aRoles ;
	}
	
	public List<TraineeData> getTrainees() {
		return _aTrainees ;
	}
	
	public List<TraineeData> getAllowedTrainees() {
		return _aAllowedTrainees ;
	}
	
	public List<UserData> getCoaches() {
		return _aCoaches ;
	}
	
	/**
	 * Fill the array of roles from a model
	 * 
	 * @param aRoles array of roles to copy in order to initialize the local array
	 */
	public void setRoles(final List<CoachingFitUserRoleData> aRoles) 
	{
		if (null == aRoles)
		{
			_aRoles = null ;
			return ;
		}
		
		if (null == _aRoles)
			_aRoles = new ArrayList<CoachingFitUserRoleData>() ;
		else
			_aRoles.clear() ;
			
		if (aRoles.isEmpty())
			return ;
		
		for (Iterator<CoachingFitUserRoleData> it = aRoles.iterator() ; it.hasNext() ; )
			_aRoles.add(new CoachingFitUserRoleData(it.next())) ;
	}
	
	/**
	 * Add a role into the array of roles from copying a model
	 * 
	 * @param role UserRoleData to add a copy from in the local array
	 */
	public void addRole(final CoachingFitUserRoleData role)
	{
		if (null == role)
			return ;
		
		if (null == _aRoles)
			_aRoles = new ArrayList<CoachingFitUserRoleData>() ;
			
		_aRoles.add(new CoachingFitUserRoleData(role)) ;
	}
	
	/**
	 * Check if a given role for a given archetype (or independently from an archetype) exists among this user's roles
	 * 
	 * @param iArchetypeId Archetype which role is to be checked, or <code>0</code> is the request is archetype independent
	 * @param sRole        Role to check for
	 * 
	 * @return <code>true</code> if a role exists with same archetype ID and same role type, <code>false</code> if not
	 * 
	 */
	public boolean hasRole(final int iArchetypeId, final String sRole)
	{
		if ((null == _aRoles) || _aRoles.isEmpty())
			return false ;
		
		if ((null == sRole) || "".equals(sRole))
			return false ;
		
		for (CoachingFitUserRoleData role : _aRoles)
			if ((role.getArchetypeId() == iArchetypeId) && role.getUserRole().equals(sRole))
				return true ;
		
		return false ;
	}
	
	/**
	 * Check if this user is an administrator (means has a role for archetype 0 that starts with 'A')
	 */
	public boolean isAdministrator()
	{
		if ((null == _aRoles) || _aRoles.isEmpty())
			return false ;

		for (CoachingFitUserRoleData role : _aRoles)
			if (role.getArchetypeId() == 0)
			{
				String sRole = role.getUserRole() ;
				if ((null != sRole) && (false == sRole.isEmpty()) && ('A' == sRole.charAt(0)))
					return true ;
			}
		
		return false ;
	}
	
	/**
	 * Fill the array of trainees from a model
	 * 
	 * @param aTrainees array of trainees to copy in order to initialize the local array
	 */
	public void setTrainees(final List<TraineeData> aTrainees) 
	{
		if (null == aTrainees)
		{
			_aTrainees = null ;
			return ;
		}
		
		if (null == _aTrainees)
			_aTrainees = new ArrayList<TraineeData>() ;
		else
			_aTrainees.clear() ;
			
		if (aTrainees.isEmpty())
			return ;
		
		for (TraineeData trainee : aTrainees)
			_aTrainees.add(new TraineeData(trainee)) ;
	}
	
	/**
	 * Fill the array of allowed trainees from a model
	 * 
	 * @param aAllowedTrainees array of allowed trainees to copy in order to initialize the local array
	 */
	public void setAllowedTrainees(final List<TraineeData> aAllowedTrainees) 
	{
		if (null == aAllowedTrainees)
		{
			_aAllowedTrainees = null ;
			return ;
		}
		
		if (null == _aAllowedTrainees)
			_aAllowedTrainees = new ArrayList<TraineeData>() ;
		else
			_aAllowedTrainees.clear() ;
			
		if (aAllowedTrainees.isEmpty())
			return ;
		
		for (TraineeData trainee : aAllowedTrainees)
			_aAllowedTrainees.add(new TraineeData(trainee)) ;
	}
	
	/**
	 * Add a trainee into the array of trainees from copying a model
	 * 
	 * @param trainee TraineeData to add a copy from in the local array
	 */
	public void addTrainee(final TraineeData trainee)
	{
		if (null == trainee)
			return ;
		
		if (null == _aTrainees)
			_aTrainees = new ArrayList<TraineeData>() ;
		
		if (null == getTraineeFromId(trainee.getId()))			
			_aTrainees.add(new TraineeData(trainee)) ;
	}
	
	/**
	 * Add an allowed trainee into the list of trainees from copying a model
	 * 
	 * @param allowedTrainee TraineeData to add a copy from in the local list
	 */
	public void addAllowedTrainee(final TraineeData allowedTrainee)
	{
		if (null == allowedTrainee)
			return ;
		
		if (null == _aAllowedTrainees)
			_aAllowedTrainees = new ArrayList<TraineeData>() ;
		
		if (null == getAllowedTraineeFromId(allowedTrainee.getId()))			
			_aAllowedTrainees.add(new TraineeData(allowedTrainee)) ;
	}
	
	/**
	 * Get a trainee from her ID from a given list
	 * 
	 * @param iTraineeId Identifier of the trainee to look for
	 * @param aTrainees  List of trainee to look into
	 * 
	 * @return a {@link TraineeData} if found, <code>null</code> if not 
	 */
	public TraineeData getTraineeFromId(final int iTraineeId, final List<TraineeData> aTrainees) 
	{
		if ((null == aTrainees) || aTrainees.isEmpty())
			return null ;
		
		for (TraineeData trainee : aTrainees)
			if (trainee.getId() == iTraineeId)
				return trainee ;
		
		return null ;
	}
	
	/**
	 * Get a trainee from her ID
	 * 
	 * @param iTraineeId Identifier of the trainee to look for
	 * 
	 * @return a {@link TraineeData} if found, <code>null</code> if not 
	 */
	public TraineeData getTraineeFromId(final int iTraineeId) {
		return getTraineeFromId(iTraineeId, _aTrainees) ;
	}
	
	/**
	 * Get an allowed trainee from her ID
	 * 
	 * @param iTraineeId Identifier of the trainee to look for
	 * 
	 * @return a {@link TraineeData} if found, <code>null</code> if not 
	 */
	public TraineeData getAllowedTraineeFromId(final int iTraineeId) {
		return getTraineeFromId(iTraineeId, _aAllowedTrainees) ;
	}
	
	/**
	 * Fill the array of coaches from a model
	 * 
	 * @param aCoachs array of coaches to copy in order to initialize the local array
	 */
	public void setCoaches(final List<UserData> aCoaches) 
	{
		if (null == aCoaches)
		{
			_aCoaches = null ;
			return ;
		}
		
		if (null == _aCoaches)
			_aCoaches = new ArrayList<UserData>() ;
		else
			_aCoaches.clear() ;
			
		if (aCoaches.isEmpty())
			return ;
		
		for (Iterator<UserData> it = aCoaches.iterator() ; it.hasNext() ; )
			_aCoaches.add(new UserData(it.next())) ;
	}
	
	/**
	 * Add a coach into the array of coaches from copying a model
	 * 
	 * @param coach UserData to add a copy from in the local array
	 */
	public void addCoach(final UserData coach)
	{
		if (null == coach)
			return ;
		
		if (null == _aCoaches)
			_aCoaches = new ArrayList<UserData>() ;
		
		if (null == getCoachFromId(coach.getId()))			
			_aCoaches.add(new UserData(coach)) ;
	}
	
	/**
	 * Get a coach from her ID
	 * 
	 * @param iCoachId Identifier of the coach to look for
	 * @return a UserData if found, <code>null</code> if not 
	 */
	public UserData getCoachFromId(final int iCoachId) 
	{
		if ((null == _aCoaches) || _aCoaches.isEmpty())
			return null ;
		
		for (UserData coach : _aCoaches)
			if (coach.getId() == iCoachId)
				return coach ;
		
		return null ;
	}
}
