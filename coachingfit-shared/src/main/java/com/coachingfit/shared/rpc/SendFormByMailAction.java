package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.primege.shared.util.MailTo;

import net.customware.gwt.dispatch.shared.Action;

public class SendFormByMailAction implements Action<SendFormByMailResult> 
{	
	private int          _iUserId ;
	private int          _iSeniorTraineeId ;
	
	private int          _iTraineeId ;
	
	private String       _sHtmlContent ;     // Form in HTML format, in order to build a PDF
	private String       _sHtmlBodyContent ; // Mail body in HTML format
	
	private List<MailTo> _aMailAddresses = new ArrayList<MailTo>() ;
	
	private String       _sMailFrom ;
	
	private String       _sMailCaption ;
	
	public SendFormByMailAction() 
	{
		super() ;
		
		_iUserId          = -1 ;
		_iSeniorTraineeId = -1 ;
		_iTraineeId       = -1 ;
		_sHtmlContent     = "" ;
		_sHtmlBodyContent = "" ;
		_sMailFrom        = "" ;
		_sMailCaption     = "" ;
	}
	
	public SendFormByMailAction(int iUserId, int iSeniorTraineeId, int iTraineeId, final String sHtmlContent, final String sHtmlBodyContent, final List<MailTo> aMailAddresses, final String sMailFrom, final String sCaption) 
	{
		_iUserId          = iUserId ;
		_iSeniorTraineeId = iSeniorTraineeId ;
		_iTraineeId       = iTraineeId ;
		_sHtmlContent     = sHtmlContent ;
		_sHtmlBodyContent = sHtmlBodyContent ;
		_sMailFrom        = sMailFrom ;
		_sMailCaption     = sCaption ;
		
		initFromMailAddresses(aMailAddresses) ;
	}

	public int getUserId() {
		return _iUserId ;
	}
	public void setUserId(int iUserId) {
		_iUserId = iUserId ;
	}

	public int getSeniorTraineeId() {
		return _iSeniorTraineeId ;
	}
	public void setSeniorTraineeId(int iSeniorTraineeId) {
		_iSeniorTraineeId = iSeniorTraineeId ;
	}
	
	public int getTraineeId() {
		return _iTraineeId ;
	}
	public void setTraineeId(int iTraineeId) {
		_iTraineeId = iTraineeId ;
	}
	
	public String getHtmlContent() {
		return _sHtmlContent ;
	}
	public void setHtmlContent(final String sHtmlContent) {
		_sHtmlContent = sHtmlContent ;
	}
	
	public String getHtmlBodyContent() {
		return _sHtmlBodyContent ;
	}
	public void setHtmlBodyContent(final String sHtmlBodyContent) {
		_sHtmlBodyContent = sHtmlBodyContent ;
	}
	
	public String getMailFrom() {
		return _sMailFrom ;
	}
	public void setMailFrom(final String sMailFrom) {
		_sMailFrom = sMailFrom ;
	}
	
	public String getMailCaption() {
		return _sMailCaption ;
	}
	
	public List<MailTo> getMails() {
		return _aMailAddresses ;
	}
	public void addMailAddress(final String sMailAddress) {
		_aMailAddresses.add(new MailTo(sMailAddress, MailTo.RecipientType.To)) ;
	}
	public void addMailAddress(final MailTo mailAddress) {
		_aMailAddresses.add(new MailTo(mailAddress)) ;
	}
	public void initFromMailAddresses(final List<MailTo> aMailAddresses)
	{
		_aMailAddresses.clear() ;
		
		if ((null == aMailAddresses) || aMailAddresses.isEmpty())
			return ;
		
		for (Iterator<MailTo> it = aMailAddresses.iterator() ; it.hasNext() ; )
			addMailAddress(it.next()) ;
	}
}
