package com.coachingfit.shared.rpc;

import java.util.ArrayList;

import com.coachingfit.shared.database.CoachingFitFormData;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Object that return information from a query for a set of forms
 * 
 * Created: 16 May 2016
 * Author: PA
 * 
 */
public class GetCoachingFormsForUserResult implements Result 
{
	private ArrayList<CoachingFitFormData> _aForms = new ArrayList<CoachingFitFormData>() ;
	private String                         _sMessage ;
	
	public GetCoachingFormsForUserResult()
	{
		super() ;
	}
	
	public GetCoachingFormsForUserResult(String sMessage) 
	{
		_sMessage = sMessage ;
	}

	public ArrayList<CoachingFitFormData> getForms() {
  	return _aForms ;
  }
	/**
	 * Add a new form to the list (if not already there)
	 * 
	 * @param formData FormData to add to the list of returned objects
	 */
	public void addFormData(CoachingFitFormData formData)
  {
		if (null == formData)
			return ;
		
		if (false == _aForms.contains(formData))
			_aForms.add(new CoachingFitFormData(formData)) ;
  }

	public String getMessage()
  {
  	return _sMessage ;
  }
	public void setMessage(String sMessage)
  {
  	_sMessage = sMessage ;
  }
}
