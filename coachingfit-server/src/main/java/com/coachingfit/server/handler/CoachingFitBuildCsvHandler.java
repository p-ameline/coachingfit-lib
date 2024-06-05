package com.coachingfit.server.handler;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.DbParameters;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.primege.server.DBConnector;
import com.primege.server.EMailer;
import com.primege.server.Logger;
import com.primege.server.csv.CsvStructure;
import com.primege.server.handler.BuildCsvHandlerBase;
import com.primege.server.model.ArchetypeDataManager;
import com.primege.server.model.UserManager;
import com.primege.shared.database.ArchetypeData;
import com.primege.shared.database.UserData;
import com.primege.shared.rpc.GetCsvAction;
import com.primege.shared.rpc.GetCsvResult;
import com.primege.shared.util.MailTo;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class CoachingFitBuildCsvHandler extends BuildCsvHandlerBase implements ActionHandler<GetCsvAction, GetCsvResult> 
{	
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	@Inject
	public CoachingFitBuildCsvHandler(final DbParameters                 dbParameters,
                                      final Provider<ServletContext>     servletContext,
                                      final Provider<HttpServletRequest> servletRequest)
	{
		super(servletContext) ;
		
		_servletRequest = servletRequest ;
	}
	
	@Override
	public GetCsvResult execute(GetCsvAction action, ExecutionContext context) throws ActionException 
	{
		if (null == action)
			return new GetCsvResult("", "Server error (invalid action request)") ;
		
		int iUserId      = action.getUserId() ;
		int iArchetypeId = action.getEventId() ;
		
		ArchetypeData archetype = getArchetype(iUserId, iArchetypeId) ;
		if (null == archetype)
		    return new GetCsvResult("", "Server error (archetype not found)") ;
		
		String sArchetypeFileName = archetype.getFile() ;
		if ("".equals(sArchetypeFileName))
			return new GetCsvResult("", "Server error (archetype not found)") ;
		
 		// Get full file name
 		//
		_sModelFileName = DbParameters.getArchetypeDir() + sArchetypeFileName ; 
 		
 		// Parse the xml file
 		//
 		CsvStructure csvStructure = new CsvStructure() ;
 		if (false == parseArchetype(csvStructure, iUserId))
 			return new GetCsvResult("", "Server error (cannot parse the Archetype file)") ;
 		
 		// Prepare the csv file name
 		//
 		String sFileName = getFileName(archetype.getLabel()) + ".csv" ;
 		
 		// Build the CSV file
 		//
		CoachingFitBuildCsvEngine cvsEngine = new CoachingFitBuildCsvEngine() ;
		String sError = cvsEngine.execute(iUserId, csvStructure, sFileName) ;
		if (false == "".equals(sError))
			return new GetCsvResult("", sError) ;
		
		// Send the mail
		//
		boolean bMailSuccess = sendCsvMail(iUserId, DbParameters.getCSV()) ;
		if (false == bMailSuccess)
			return new GetCsvResult("", "Server error (cannot send file by mail)") ;
		
		return new GetCsvResult("", sError) ;
	}
	
	/**
	  * Send the CSV file by mail
	  */
	public boolean sendCsvMail(final int iUserId, final String sFileName)
	{
		if ((null == sFileName) || "".equals(sFileName))
			return false ;
		
		Logger.trace("Sending CSV file", iUserId, Logger.TraceLevel.DETAIL) ;
		
		UserData user = getUserData(iUserId) ;
		if (null == user)
		{
			Logger.trace("Sending CSV file aborted, can't find user information in database", iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		String sRealPath  = _servletContext.get().getRealPath("") ;
		
		String sMailTitle = "[Coaching Fit] Fichier CSV" ;
		String sMailBody  = "Bonjour " + user.getLabel() + ", vous trouverez ci-joint le fichier CSV." ;
		
		ArrayList<MailTo> aToEmailAddr  = new ArrayList<MailTo>() ;
		aToEmailAddr.add(new MailTo(user.getEMail(), MailTo.RecipientType.To)) ;
		
		ArrayList<String> aAttachedFiles = new ArrayList<String>() ;
		aAttachedFiles.add(sFileName) ;
		
		EMailer mailer = new EMailer(sRealPath) ;
		boolean bMailSent = mailer.sendEmail("", aToEmailAddr, sMailTitle, sMailBody, aAttachedFiles) ;
		
		return bMailSent ;
	}
	
	protected ArchetypeData getArchetype(final int iUserId, final int iArchetypeId)
	{
		DBConnector dbConnector = new DBConnector(false) ;
	
		ArchetypeDataManager archetypeManager = new ArchetypeDataManager(iUserId, dbConnector) ;
		
		ArchetypeData archetype = new ArchetypeData() ;
		if (false == archetypeManager.existData(iArchetypeId, archetype))
			return null ;
			
		return archetype ;
	}
	
	protected UserData getUserData(final int iUserId)
	{
		DBConnector dbConnector = new DBConnector(false) ;
	
		UserManager userManager = new UserManager(iUserId, dbConnector, true) ;
		
		UserData user = new UserData() ;
		if (false == userManager.existUser(iUserId, user))
			return null ;
			
		return user ;
	}
	
	/**
	 * Get a file name by replacing all white spaces with underscores
	 */
	public static String getFileName(final String sArchetypeLabel)
	{
	    if ((null == sArchetypeLabel) || sArchetypeLabel.isEmpty())
	        return "" ;
	    
	    String sAtWork = sArchetypeLabel.trim() ;
	    
	    int iLabelLength = sAtWork.length() ;
	    
	    String sResult = "" ;
	    for (int i = 0 ; i < iLabelLength ; i++)
	    {
	        char c = sAtWork.charAt(i) ;
	        if (' ' != c)
	            sResult += c ;
	        else
	        {
	            sResult += "_" ;
	            while ((i < iLabelLength - 1) && (sAtWork.charAt(i + 1) == ' '))
	                i++ ;
	        }
	    }
	    
	    return sResult ;
	}
	
	@Override
	public Class<GetCsvAction> getActionType() {
		return GetCsvAction.class;
	}

	@Override
	public void rollback(GetCsvAction action, GetCsvResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
