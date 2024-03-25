package com.coachingfit.shared.rpc;

import com.primege.shared.database.FormDataData;
import com.primege.shared.model.FormBlock;

import net.customware.gwt.dispatch.shared.Result;

public class GetCoachingFitPreviousFormBlockResult implements Result 
{
	private FormBlock<FormDataData> _formBlock = new FormBlock<FormDataData>() ;
	private String                  _sMessage ;
	
	public GetCoachingFitPreviousFormBlockResult()
	{
		super() ;
		
		_sMessage = "" ;
	}
	
	public GetCoachingFitPreviousFormBlockResult(final String sMessage) 
	{
		super() ;
		
		_sMessage = sMessage ;
	}

	public FormBlock<FormDataData> getFormBlock() {
  	return _formBlock ;
  }
	public void setFormBlock(final FormBlock<FormDataData> formBlock) {
		_formBlock.initFromFormBlock(formBlock) ;
  }

	public String getMessage() {
  	return _sMessage ;
  }
	public void setMessage(final String sMessage) {
  	_sMessage = sMessage ;
  }
}
