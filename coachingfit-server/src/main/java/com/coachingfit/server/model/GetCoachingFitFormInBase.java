package com.coachingfit.server.model;

import com.coachingfit.shared.database.CoachingFitFormData;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.model.FormInformationManager;
import com.primege.shared.database.FormDataData;
import com.primege.shared.model.FormBlock;

public class GetCoachingFitFormInBase
{	
	protected final DBConnector _dbConnector ;
	protected final int         _iUserId ;
	
	public GetCoachingFitFormInBase(int iUserId, final DBConnector dbConnector) 
	{
		_dbConnector = dbConnector ;
		_iUserId     = iUserId ;
	}
	
	/** 
	 * Fill a FormBlock with a form and its information from a given form ID 
	 * 
	 * @param  iFormId form's unique identifier
	 * @param  form    FormBlock to be completed by information from database
	 * @return <code>true</code> if all went well, <code>false</code> if not   
	 */
	public boolean GetForm(int iFormId, FormBlock<FormDataData> form) 
	{
		String sFctName = "GetCoachingFitFormInBase:GetForm" ;
		
		if (null == form)
		{
			Logger.trace(sFctName + ": query for form " + iFormId + " aborted due to null parameter", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		Logger.trace(sFctName + ": starting to query information for form " + iFormId + ".", _iUserId, Logger.TraceLevel.STEP) ;
		
		if ((null == form.getDocumentLabel()) || (form.getDocumentLabel().isEmpty()))
			loadDocument(iFormId, form) ;

		if ((null == form.getInformation()) || (form.getInformation().isEmpty()))
			loadFormData(iFormId, form) ;

		buildDocumentLabel(form) ;
		
		Logger.trace(sFctName + ": finished to query information for form " + iFormId + ".", _iUserId, Logger.TraceLevel.STEP) ;
		
		return true ;
	}		
	
	/** 
	 * Get information from table form for a given form 
	 * 
	 * @param  iFormId form's unique identifier
	 * @param  form    FormBlock to be completed by information from database
	 * @return <code>true</code> if all went well, <code>false</code> if not   
	 */
	public boolean loadDocument(int iFormId, FormBlock<FormDataData> form) 
	{
		String sFctName = "GetCoachingFitFormInBase.loadDocument" ;
		
		CoachingFitFormDataManager formDataManager = new CoachingFitFormDataManager(_iUserId, _dbConnector) ;
		CoachingFitFormData document = new CoachingFitFormData() ;
		
		if (formDataManager.existData(iFormId, document))
			form.setDocumentLabel(document) ;
		else
		{
			Logger.trace(sFctName + ": query for form " + iFormId + " gave no answer", _iUserId, Logger.TraceLevel.WARNING) ;
			return false ;
		}
		
		return true ;
	}
	
	/** 
	 * Get information from table formData for a given form
	 * 
	 * @param  iFormId form's unique identifier
	 * @param  form    FormBlock to be completed by information from database
	 * @return <code>true</code> if all went well, <code>false</code> if not
	 */
	public boolean loadFormData(int iFormId, FormBlock<FormDataData> form) 
	{
		if (null == form)
			return false ;
		
		FormInformationManager formInformationManager = new FormInformationManager(_iUserId, _dbConnector) ;
		
		return formInformationManager.loadFormData(iFormId, form) ;
	}
	
	/** 
	 * Get information from table contact for a given message 
	 * 
	 * @param    iMessageId message unique identifier
	 * @param    encounter EncounterBlock to be completed by contact information
	 * @return   void  
	 */
	public boolean buildDocumentLabel(FormBlock<FormDataData> form) 
	{		
		return true ;
	}	
}
