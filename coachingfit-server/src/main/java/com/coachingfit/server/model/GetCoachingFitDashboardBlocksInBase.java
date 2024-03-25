package com.coachingfit.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.model.CoachForDateBlock;
import com.coachingfit.shared.model.CoachingFitDashboardBlocks;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.shared.database.FormDataData;
import com.primege.shared.model.FormBlock;

public class GetCoachingFitDashboardBlocksInBase  
{	
	protected final DBConnector _dbConnector ;
	protected final int         _iUserId ;
	
	public GetCoachingFitDashboardBlocksInBase(int iUserId, final DBConnector dbConnector)
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}
	
	/** 
	 * Fill a DashboardBlocks list with all forms for a given coach ID and/or trainee ID and/or a period of time 
	 * 
	 * @param  iCoachId      coach identifier
	 * @param  iTraineeId    trainee identifier, or -1
	 * @param  sStartingDate starting date, or ""
	 * @param  sEndingDate   ending date, or ""
	 * @param  iRegionId     region identifier, or -1
	 * @param  dashBlocks    the list of DashboardBlocks to be completed by information from database
	 * 
	 * @return <code>true</code> if all went well, <code>false</code> if not   
	 */
	public boolean GetDashboardBlocks(final int iCoachId, final String[] aRoots, final int iTraineeId, final String sStartingDate, final String sEndingDate, final int iRegionId, CoachingFitDashboardBlocks dashBlocks) 
	{
		if (null == dashBlocks)
			return false ;
		
		int[] aTrainees = null ;
		if (iTraineeId > 0)
		{
			aTrainees = new int[1] ;
			aTrainees[0] = iTraineeId ; 
		}
		
		return GetDashboardBlocks(iCoachId, aRoots, aTrainees, sStartingDate, sEndingDate, iRegionId, dashBlocks) ;
	}
	
	/** 
	 * Fill a DashboardBlocks list with all forms for a given coach ID and/or trainee ID and/or a period of time 
	 * 
	 * @param  iCoachId      coach identifier
	 * @param  aTrainees     trainees identifiers, or null
	 * @param  sStartingDate starting date, or ""
	 * @param  sEndingDate   ending date, or ""
	 * @param  iRegionId     region identifier, or -1
	 * @param  dashBlocks    the list of DashboardBlocks to be completed by information from database
	 * 
	 * @return <code>true</code> if all went well, <code>false</code> if not   
	 */
	public boolean GetDashboardBlocks(final int iCoachId, final String[] aRoots, final int[] aTrainees, final String sStartingDate, final String sEndingDate, final int iRegionId, CoachingFitDashboardBlocks dashBlocks) 
	{
		if (null == dashBlocks)
			return false ;
		
		String sFctName = "GetCoachingFitDashboardBlocksInBase.GetDashboardBlocks" ;
		
		// The first step is to get all information from the "form" table and create objects in dashBlocks
		//
		// Database query is build depending on variables states
		//
		String sQuery = "SELECT * FROM form" ;
		
		boolean bExistClause = false ;
		
		if (iCoachId > 0)
		{
			sQuery += " WHERE userID = ?" ;
			
			bExistClause = true ;
		}
			
		if ((null != aRoots) && (aRoots.length > 0))
		{
			if (false == bExistClause)
				sQuery += " WHERE" ;
			else
				sQuery += " AND" ;
			
			if (1 == aRoots.length)
				sQuery += " root = ?" ;
			else
			{
				sQuery += " (root = ?" ;
				for (int i = 1 ; i < aRoots.length ; i++)
					sQuery += " OR root = ?" ;
				sQuery += ")" ;
			}
			
			bExistClause = true ;
		}
		
		if ((null != aTrainees) && (aTrainees.length > 0))
		{
			if (false == bExistClause)
				sQuery += " WHERE " ;
			else
				sQuery += " AND " ;
			
			String sTraineeQuery = "traineeID = ?" ;
			for (int i = 1 ; i < aTrainees.length ; i++)
				sTraineeQuery += " OR traineeID = ?" ;
			if (aTrainees.length > 1)
				sTraineeQuery = "(" + sTraineeQuery + ")" ;
			
			sQuery += sTraineeQuery ;
			
			bExistClause = true ;
		}
		
		if (iRegionId > 0)
		{
			if (false == bExistClause)
				sQuery += " WHERE" ;
			else
				sQuery += " AND" ;
			
			sQuery += " regionID = ?" ;
			
			bExistClause = true ;
		}
		
		if ((null != sStartingDate) && (false == "".equals(sStartingDate)))
		{
			if (false == bExistClause)
				sQuery += " WHERE" ;
			else
				sQuery += " AND" ;
			
			sQuery += " formDate >= ?" ;
			
			bExistClause = true ;
		}
		
		if ((null != sEndingDate) && (false == "".equals(sEndingDate)))
		{
			if (false == bExistClause)
				sQuery += " WHERE" ;
			else
				sQuery += " AND" ;
			
			sQuery += " formDate <= ?" ;
			
			bExistClause = true ;
		}
		
		_dbConnector.prepareStatememt(sQuery, Statement.NO_GENERATED_KEYS) ;
		
		// Fill statement's variables
		//
		int iNextIndex = 1 ;
		
		if (iCoachId > 0)
			_dbConnector.setStatememtInt(iNextIndex++, iCoachId) ;
		if ((null != aRoots) && (aRoots.length > 0))
			for (int i = 0 ; i < aRoots.length ; i++)
				_dbConnector.setStatememtString(iNextIndex++, aRoots[i]) ;
		if ((null != aTrainees) && (aTrainees.length > 0))
			for (int i = 0 ; i < aTrainees.length ; i++)
				_dbConnector.setStatememtInt(iNextIndex++, aTrainees[i]) ;
		if (iRegionId > 0)
			_dbConnector.setStatememtInt(iNextIndex++, iRegionId) ;
		if ((null != sStartingDate) && (false == "".equals(sStartingDate)))
			_dbConnector.setStatememtString(iNextIndex++, sStartingDate) ;
		if ((null != sEndingDate) && (false == "".equals(sEndingDate)))
			_dbConnector.setStatememtString(iNextIndex++, sEndingDate) ;

		if (false == _dbConnector.executePreparedStatement())
		{
			Logger.trace(sFctName + ": failed query " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			_dbConnector.closePreparedStatement() ;
			return false ;
		}
		
		// Processing resulting forms
		//
		CoachingFitFormDataManager formManager = new CoachingFitFormDataManager(_iUserId, _dbConnector) ;

		int iNbCode = 0 ;

		ResultSet rs = _dbConnector.getResultSet() ;
		try
		{
			while (rs.next())
			{
				// Get form's meta information
				//
				CoachingFitFormData document = new CoachingFitFormData() ;
				formManager.fillDataFromResultSet(rs, document) ;
					
				if (document.isValid())
					addFormDataToDashboardBlocks(document, dashBlocks) ;
					
				iNbCode++ ;
			}

			if (0 == iNbCode)
			{
				Logger.trace(sFctName + ": query gave no answer", _iUserId, Logger.TraceLevel.WARNING) ;
				_dbConnector.closeResultSet() ;
				_dbConnector.closePreparedStatement() ;
				return false ;
			}
		}
		catch(SQLException ex)
		{
			Logger.trace(sFctName + ": DBConnector.dbSelectPreparedStatement: executeQuery failed for preparedStatement " + sQuery, _iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLException: " + ex.getMessage(), _iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": SQLState: " + ex.getSQLState(), _iUserId, Logger.TraceLevel.ERROR) ;
			Logger.trace(sFctName + ": VendorError: " +ex.getErrorCode(), _iUserId, Logger.TraceLevel.ERROR) ;        
		}

		// The second step is to complete dashBlocks with all information from the formData table
		//
		addFormsInformation(dashBlocks) ;
			
		_dbConnector.closeResultSet() ;
		_dbConnector.closePreparedStatement() ;
			
		return true ;
	}
	
	/**
	  * Complete all forms inside the DashboardBlocks with their data    
	  * 
	  */
	protected void addFormsInformation(CoachingFitDashboardBlocks dashBlocks)
	{
		if ((null == dashBlocks) || dashBlocks.isEmpty())
			return ;
		
		// Object that get a form, or parts of a form, in database
		//
		GetCoachingFitFormInBase getFormInBase = new GetCoachingFitFormInBase(_iUserId, _dbConnector) ;
		
		for (CoachForDateBlock coach4date : dashBlocks.getInformation())
		{
			if (false == coach4date.isEmpty())
			{
				for (FormBlock<FormDataData> formBlock : coach4date.getInformation())
		  	{
					// Fill the formBlock with its data
					//
					CoachingFitFormData document = (CoachingFitFormData) formBlock.getDocumentLabel() ;
					if (null != document)
						getFormInBase.loadFormData(document.getFormId(), formBlock) ;
		  	}
			}
		}
	}
	
	/**
	 * Add a FormData into the proper CoachForDatesBlock into the CoachingFitDashboardBlocks  
	 * 
	 */
	protected void addFormDataToDashboardBlocks(final CoachingFitFormData document, CoachingFitDashboardBlocks dashBlocks)
	{
		if ((null == document) || (null == dashBlocks))
			return ;
		
		FormBlock<FormDataData> formBlock = new FormBlock<FormDataData>("", document, null) ;
		
		// Look for an existing block for the document's date
		//
		CoachForDateBlock coach4dates = getCoach4DatesInBlocks(document.getAuthorId(), document.getCoachingDate(), dashBlocks) ;
		
		if (null != coach4dates)
		{
			coach4dates.addData(formBlock) ;
			return ;
		}
			
		CoachForDateBlock newBlock4Date = new CoachForDateBlock(document.getAuthorId(), document.getCoachingDate(), new ArrayList<FormBlock<FormDataData>>()) ;
		newBlock4Date.addData(formBlock) ;
			
		dashBlocks.addData(newBlock4Date) ;
	}
	
	/**
	  * Get the CoachForDatesBlock attached to a given coach for a given date inside the DashboardBlocks  
	  * 
	  * @param iCoachId   ID of coach to look for
	  * @param sEventDate date to look for
	  * @param dashBlocks DashboardBlocks to look into
	  * 
	  * @return The corresponding CityForDateBlock if found, <code>null</code> if not
	  * 
	  */
	protected CoachForDateBlock getCoach4DatesInBlocks(final int iCoachId, final String sDate, final CoachingFitDashboardBlocks dashBlocks)
	{
		if ((null == dashBlocks) || (null == dashBlocks.getInformation()) || (null == sDate))
			return null ;
		
		if (dashBlocks.getInformation().isEmpty())
			return null ;
		
		for (CoachForDateBlock coach4date : dashBlocks.getInformation())
			if ((coach4date.getCoachId() == iCoachId) && sDate.equals(coach4date.getSessionDate()))
				return coach4date ;
		
		return null ;
	}
}
