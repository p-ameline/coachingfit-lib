package com.coachingfit.shared.database ;

import com.google.gwt.user.client.rpc.IsSerializable ;

import com.primege.shared.database.UserRoleDataModel;

/**
 * A UserRoleData object establish the role of a user for a given site, city, event (connected or not)
 * 
 * Created: 17 May 2016
 * Author: PA
 * 
 */
public class CoachingFitUserRoleData extends UserRoleDataModel implements IsSerializable 
{
	/**
	 * Default constructor (with zero information)
	 */
	public CoachingFitUserRoleData() {
		reset() ;
	}

	/**
	 * Plain vanilla constructor 
	 */
	public CoachingFitUserRoleData(int iId, int iUserId, int iArcheId, String sRole) 
	{
		super(iId, iUserId, iArcheId, sRole) ;
	}

	/**
	 * Copy constructor
	 * 
	 * @param model UserRoleData to initialize from 
	 */
	public CoachingFitUserRoleData(CoachingFitUserRoleData model) {
		reset() ;
		initFromUserRole(model) ;
	}

	/**
	 * Initialize all information from another UserRoleData
	 * 
	 * @param model UserRoleData to initialize from 
	 */
	public void initFromUserRole(CoachingFitUserRoleData model)
	{
		if (null == model)
			return ;
		
		initFromUserRoleModel(model) ;
	}

	/**
	 * Zeros all information
	 */
	public void reset() 
	{
		reset4Model() ;
	}
	
	/**
	 * Check if this object has no initialized data
	 * 
	 * @return true if all data are zeros, false if not
	 */
	public boolean isEmpty()
	{
		if (isEmptyModel())
			return true ;
		
		return false ;
	}
		
	/**
	  * Determine whether two UserRoleData are exactly similar
	  * 
	  * @return true if all data are the same, false if not
	  * @param  user4cityData UserRoleData to compare with
	  * 
	  */
	public boolean equals(CoachingFitUserRoleData userRoleData)
	{
		if (this == userRoleData) {
			return true ;
		}
		if (null == userRoleData) {
			return false ;
		}
		
		UserRoleDataModel model     = (UserRoleDataModel) userRoleData ;
		UserRoleDataModel modelThis = (UserRoleDataModel) this ;
		
		return (modelThis.equals(model))  ;
	}

	/**
	  * Determine whether this UserRoleData is exactly similar to another object
	  * 
	  * @return true if all data are the same, false if not
	  * @param o Object to compare with
	  * 
	  */
	public boolean equals(Object o) 
	{
		if (this == o) {
			return true ;
		}
		if ((null == o) || (getClass() != o.getClass())) {
			return false ;
		}

		final CoachingFitUserRoleData userRoleData = (CoachingFitUserRoleData) o ;

		return equals(userRoleData) ;
	}
}
