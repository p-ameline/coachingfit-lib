package com.coachingfit.server.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.DbParameters;
import com.coachingfit.server.model.CoachingFitUserRoleDataManager;
import com.coachingfit.server.model.RegionDataManager;
import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.CoachingFitUserRoleData;
import com.coachingfit.shared.database.RegionData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.SendFormByMailAction;
import com.coachingfit.shared.rpc.SendFormByMailResult;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.primege.server.DBConnector;
import com.primege.server.EMailer;
import com.primege.server.Logger;
import com.primege.server.model.UserManager;
import com.primege.shared.database.UserData;
import com.primege.shared.util.MailTo;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class SendMailsHandler implements ActionHandler<SendFormByMailAction, SendFormByMailResult> 
{	
	protected final Provider<HttpServletRequest> _servletRequest ;
	protected final Provider<ServletContext>     _servletContext ;
	
	protected       int                          _iUserId ;
	protected       String                       _sModelFileName ;
	
	@Inject
	public SendMailsHandler(final Provider<ServletContext>     servletContext,       
                          final Provider<HttpServletRequest> servletRequest)
	{
		_servletRequest = servletRequest ;
		_servletContext = servletContext ;
	}
	
	@Override
	public SendFormByMailResult execute(SendFormByMailAction action, ExecutionContext context) throws ActionException 
	{
		_iUserId = -1 ;
		if (null != action)
			_iUserId = action.getUserId() ;
		
		Logger.trace("Entering SendMailsHandler.", _iUserId, Logger.TraceLevel.STEP) ;
		
		// Build the list of addresses to send the mail to from the list of addresses and variables
	 	//
	 	List<MailTo> aMailAddresses = getRecipients(action.getMails(), action.getTraineeId()) ;
		
	 	if (aMailAddresses.isEmpty())
	 	{
	 		Logger.trace("SendMailsHandler: empty recipients list.", _iUserId, Logger.TraceLevel.ERROR) ;
	 		return new SendFormByMailResult("Server error (empty recipients list)") ;
	 	}
	 	
 		// Get the next available file name
 		//
		String sRootName = getNotInUseHtmlName(DbParameters.getArchetypeDir()) ;
		if ("".equals(sRootName))
		{
			Logger.trace("SendMailsHandler: temporary files counter is full.", _iUserId, Logger.TraceLevel.ERROR) ;
			return new SendFormByMailResult("Server error (temporary files counter is full)") ;
		}
		
		String sHtmlFileName = DbParameters.getArchetypeDir() + sRootName + ".html" ; 
		String sPdfFileName  = DbParameters.getArchetypeDir() + sRootName + ".pdf" ;
		
		// Create the Html file
		//
		String sErrMsg = createHtmlFile(sHtmlFileName, action.getHtmlContent()) ;
 		if (false == "".equals(sErrMsg))
 		{
 			Logger.trace("SendMailsHandler: problem creating HTML file \"" + sHtmlFileName + "\" (" + sErrMsg + ").", _iUserId, Logger.TraceLevel.ERROR) ;
 			return new SendFormByMailResult(sErrMsg) ;
 		}
 		
 		if (false == existFile(sHtmlFileName))
 		{
 			Logger.trace("Model HTML file creation failed.", _iUserId, Logger.TraceLevel.ERROR) ;
 			return new SendFormByMailResult("Model HTML file creation failed.") ;
 		}
 		
 		// Convert the Html to pdf
 		//
 		sErrMsg = Convert(sHtmlFileName, sPdfFileName) ;
 		if (false == "".equals(sErrMsg))
 		{
 			Logger.trace("Failed converting the HTML file \"" + sHtmlFileName + "\" to PDF file \"" + sPdfFileName + "\" (" + sErrMsg + ").", _iUserId, Logger.TraceLevel.ERROR) ;
 			deleteFile(sHtmlFileName) ;
 			return new SendFormByMailResult(sErrMsg) ;
 		}
 		
 		deleteFile(sHtmlFileName) ;
 		
 		if (false == existFile(sPdfFileName))
 		{
 			Logger.trace("PDF file creation failed.", _iUserId, Logger.TraceLevel.ERROR) ;
 			return new SendFormByMailResult("PDF file creation failed.") ;
 		}

 		// Send the mail
 		//
 		boolean bMailSent = sendPdfFileByMail(sPdfFileName, action.getHtmlBodyContent(), aMailAddresses, action.getMailFrom(), action.getMailCaption()) ;
 	
 		// Delete the Html file
  	//
 		deleteFile(sPdfFileName) ;
 		
 		if (false == bMailSent)
 		{
 			Logger.trace("L'envoi du mail a échoué.", _iUserId, Logger.TraceLevel.ERROR) ;
 			return new SendFormByMailResult("L'envoi du mail a échoué.") ;
 		}
		
 		Logger.trace("Leaving SendMailsHandler.", _iUserId, Logger.TraceLevel.STEP) ;
		return new SendFormByMailResult("") ;
	}
	
	/**
	 * Send the PDF file by mail
	 */
	public boolean sendPdfFileByMail(final String sPdfFileName, final String sHtmlMailBody, final List<MailTo> aMailAddresses, final String sFromEmailAddr, final String sMailCaption)
	{
		if ((null == sPdfFileName) || "".equals(sPdfFileName))
			return false ;
		
		String sFctName = "SendMailsHandler.sendPdfFileByMail" ;
		
		if (aMailAddresses.isEmpty())
		{
			Logger.trace(sFctName + ": Empty list of mail addresses, cannot send Pdf file.", _iUserId, Logger.TraceLevel.ERROR) ;
			return false ;
		}
		
		Logger.trace(sFctName + ": Sending Pdf file", _iUserId, Logger.TraceLevel.DETAIL) ;
		
		String sRealPath  = _servletContext.get().getRealPath("") ;
		
		String sMailTitle = sMailCaption ;
		if (sMailTitle.isEmpty())
			sMailTitle = "[Coaching Fit] Compte rendu d'accompagnement" ;
		
		String sBody = "" ; 
		if ((null == sHtmlMailBody) || "".equals(sHtmlMailBody))
			sBody  = "Vous trouverez ci-joint le compte rendu de la dernière session d'accompagnement." ;
		
		List<String> aAttachedFiles = new ArrayList<String>() ;
		aAttachedFiles.add(sPdfFileName) ;
		
		Logger.trace(sFctName + ": Constructing EMailer with path = " + sRealPath, _iUserId, Logger.TraceLevel.SUBDETAIL) ;
		
		EMailer mailer = new EMailer(sRealPath) ;
		
		Logger.trace(sFctName + ": calling EMailer.sendEmail.", _iUserId, Logger.TraceLevel.SUBDETAIL) ;
		
		boolean bMailSent = mailer.sendEmail(sFromEmailAddr, aMailAddresses, sMailTitle, sBody, sHtmlMailBody, aAttachedFiles) ;
		
		if (false == bMailSent)
			Logger.trace(sFctName + ": EMailer returned \"false\".", _iUserId, Logger.TraceLevel.ERROR) ;
		else
			Logger.trace(sFctName + ": EMailer returned \"true\".", _iUserId, Logger.TraceLevel.SUBDETAIL) ;
		
		return bMailSent ;
	}
	
	/**
	 * Write HTML content as a file
	 * 
	 * @param sHtmlFileName Complete file name for file to be created
	 * @param sHtmlContent  HTML content
	 * 
	 * @return <code>""</code> if all went well, an error message if not
	 */
	protected String createHtmlFile(final String sHtmlFileName, final String sHtmlContent) 
	{
		String sFctName = "SendMailsHandler.createHtmlFile" ;
		
		if (isNullOrEmpty(sHtmlFileName) || isNullOrEmpty(sHtmlContent))
			return "Invalid information" ;
		
		// Open output file
		//
		FileOutputStream out = null ;
		try
	  {
			out = new FileOutputStream(sHtmlFileName, false) ;
	  } 
		catch (FileNotFoundException eOpen)
	  {
			Logger.trace(sFctName + ": cannot create file " + sHtmlFileName + " ; stackTrace:" + eOpen.getStackTrace(), _iUserId, Logger.TraceLevel.ERROR) ;
		  return "Server Error: Cannot create model Html file." ;
	  }
			
		// Write string to disk
		//
		String sErrorMsg = "" ;
		
		String sCompleteFileContent = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n    <link type=\"text/css\" rel=\"stylesheet\" href=\"CoachingFit.css\">\n  </head>\n  <body>\n" + sHtmlContent + "\n  </body>\n</html>" ;
			
		byte data[] = sCompleteFileContent.getBytes() ;
		try
	  {
			out.write(data, 0, data.length) ;
	  } 
		catch (IOException eWrite)
	  {
			Logger.trace(sFctName + ": error writing file " + sHtmlFileName + " ; stackTrace:" + eWrite.getStackTrace(), -1, Logger.TraceLevel.ERROR) ;
			sErrorMsg = "Server Error: Cannot write model Html file." ;
	  }
		finally
		{		
			try
	    {
		    out.flush() ;
	    } 
			catch (IOException eFlush)
	    {
				Logger.trace(sFctName + ": error flushing file " + sHtmlFileName + " ; stackTrace:" + eFlush.getStackTrace(), -1, Logger.TraceLevel.ERROR) ;
				sErrorMsg = "Server Error: Cannot flush model Html file." ;
	    }
			try
	    {
		    out.close() ;
	    } 
			catch (IOException eClose)
	    {
				Logger.trace(sFctName + ": error closing file " + sHtmlFileName + " ; stackTrace:" + eClose.getStackTrace(), -1, Logger.TraceLevel.ERROR) ;
				sErrorMsg = "Server Error: Cannot close model Html file." ;
	    }
		}		
		return sErrorMsg ;
	}
	
	/**
	 * Convert the HTML file into a PDF file
	 * 
	 * @param sHtmlFileName Full file name for the input HTML file
	 * @param sPdfFileName  Full file name for the output PDF file
	 * 
	 * @return <code>""</code> if all went well, an error message if not
	 */
	protected String Convert(final String sHtmlFileName, final String sPdfFileName)
	{
		String sFctName = "SendMailsHandler.Convert" ;
		
		if ((null == sHtmlFileName) || "".equals(sHtmlFileName) || (null == sPdfFileName) || "".equals(sPdfFileName))
			return "Server Error: wrong parameter when converting html file to pdf." ;
		
		ProcessBuilder pb = new ProcessBuilder("./wkhtmltopdf", sHtmlFileName, sPdfFileName) ;
		
		String sShortDirName = DbParameters.getArchetypeDir() ;
		sShortDirName = removeFinalSlashes(sShortDirName) ;
		pb.directory(new File(sShortDirName)) ;
		
		File log = new File(DbParameters.getArchetypeDir() + "converterError.log") ;
		pb.redirectErrorStream(true) ;
		pb.redirectOutput(Redirect.appendTo(log)) ;
		
		try 
		{
			Process p = pb.start() ;
			
			p.waitFor() ;
		} catch (IOException e) {
			Logger.trace(sFctName + ": error executing converter ; stackTrace:" + e.getMessage(), -1, Logger.TraceLevel.ERROR) ;
			return "Server Error: cannot execute the html to pdf converter." ;
		} catch (InterruptedException e) {
			Logger.trace(sFctName + ": error waiting fot converter to terminate ; stackTrace:" + e.getMessage(), -1, Logger.TraceLevel.ERROR) ;
		}
		
		return "" ;
	}
	
	protected String getNotInUseHtmlName(final String sCompletePath)
	{
		if ((null == sCompletePath) || "".equals(sCompletePath))
			return "" ;
		
		String sExtension = "0000" ;
		
		while (true)
		{
			String sCandidateFileName = "temp_" + sExtension ;
		
			if (false == existFile(sCompletePath + sCandidateFileName + ".html"))
				return sCandidateFileName ;
			
			sExtension = getNextId(sExtension) ;
			if ("".equals(sExtension))
				return "" ;
		}
	}
	
	/**
	 * Check if a file exists
	 *     
	 * @return true if file exists 
	 **/
	public boolean existFile(String sCompleteFileName)
	{
		File f = new File(sCompleteFileName) ;		
		return f.exists() ;
	}
	
	protected void deleteFile(String sCompleteFileName)
	{
		if ((null == sCompleteFileName) || "".equals(sCompleteFileName))
			return ;
		 
		String sFctName = "SendMailsHandler.deleteFile" ;
		
		Path path = FileSystems.getDefault().getPath(sCompleteFileName) ;
		
		try 
		{
 	    Files.delete(path) ;
		} 
		catch (NoSuchFileException x) {
			Logger.trace(sFctName + ": no such file or directory (\"" + sCompleteFileName + "\")", -1, Logger.TraceLevel.ERROR) ;
		} 
		catch (DirectoryNotEmptyException x) {
			Logger.trace(sFctName + ": directory not empty (\"" + sCompleteFileName + "\")", -1, Logger.TraceLevel.ERROR) ;
		} 
		catch (IOException e) {
			Logger.trace(sFctName + ": error deleting (\"" + sCompleteFileName + "\") ; stackTrace:" + e.getMessage(), -1, Logger.TraceLevel.ERROR) ;
		}
	}
	
	protected List<MailTo> getRecipients(final List<MailTo> aMails, int iTraineeId)
	{
		if ((null == aMails) || aMails.isEmpty())
			return aMails ;
		
		List<MailTo> aMailAddresses = new ArrayList<MailTo>() ; 
		
		DBConnector dbConnector = null ;
		
		for (MailTo mailTo : aMails)
		{
			String sMailAddress = mailTo.getAddress() ;
			if (false == "".equals(sMailAddress))
			{
				// It is a variable, usually in the form $role=A$ or $role=Z?$
				//
				if ('$' == sMailAddress.charAt(0))
				{
					String sVar = sMailAddress.substring(1, sMailAddress.length() - 1) ;
					String sVarParts[] = sVar.split("=") ;
					
					if (null == dbConnector)
						dbConnector = new DBConnector(false) ;
					
					// Mail(s) for role
					//
					if ((2 == sVarParts.length) && "role".equalsIgnoreCase(sVarParts[0]))
						addMailAddressForVar(sVarParts[1], aMailAddresses, iTraineeId, mailTo.getRecipientType(), dbConnector) ;
				}
				else if (false == isInAddressesList(sMailAddress, aMailAddresses))
					aMailAddresses.add(new MailTo(mailTo)) ;
				
			}
		}
		
		return aMailAddresses ;
	}
	
	protected void addMailAddressForVar(final String sVar, List<MailTo> aMailAddresses, int iTraineeId, MailTo.RecipientType iRecipientType, DBConnector dbConnector)
	{
		if ((null == sVar) || "".equals(sVar) || (null == aMailAddresses))
			return ;
		
		String sMailAddress = "" ;
		
		// Sales director
		//
		if ("CV".equalsIgnoreCase(sVar))
		{
			sMailAddress = getMailAddressForSalesDirector(iTraineeId, dbConnector) ;
		}
		// Does the variable end with a '!' (meaning trainee specific information, like "Z!" for region director
		//
		else if ('!' == sVar.charAt(sVar.length() - 1))
		{
			// Region director
			//
			if ("Z!".equals(sVar))
				sMailAddress = getMailAddressForRegionalDirector(iTraineeId, dbConnector) ;
		}
		// '*' means any role starting with the pattern before it
		//
		else if ('*' == sVar.charAt(sVar.length() - 1))
		{
			String sPattern = sVar.substring(0, sVar.length() - 1) ;
			addMailAddressForPattern(sPattern, false, aMailAddresses, iRecipientType, dbConnector) ;
		}
		// add all users for this exact role
		//
		else
			addMailAddressForPattern(sVar, true, aMailAddresses, iRecipientType, dbConnector) ;
		
		if ("".equals(sMailAddress) || isInAddressesList(sMailAddress, aMailAddresses))
			return ;
		
		aMailAddresses.add(new MailTo(sMailAddress, iRecipientType)) ;
	}
	
	/**
	 * Get a trainee's sales director's mail address 
	 * 
	 * @param iTraineeId Trainee's identifier
	 * 
	 * @return The mail address if found, <code>""</code> if not
	 */
	protected String getMailAddressForSalesDirector(int iTraineeId, DBConnector dbConnector)
	{
		String sFctName = "SendMailsHandler.getMailAddressForSalesDirector" ;
		
		if ((iTraineeId < 0) || (null == dbConnector))
		{
			if (iTraineeId < 0)
				Logger.trace(sFctName + ": wrong trainee ID (" + iTraineeId + ").", _iUserId, Logger.TraceLevel.ERROR) ;
			if (null == dbConnector)
				Logger.trace(sFctName + ": null database connector.", _iUserId, Logger.TraceLevel.ERROR) ;
			
			return "" ;
		}
		
		// Find trainee data in order to get its sales director
		//
		TraineeDataManager traineeManager = new TraineeDataManager(_iUserId, dbConnector) ;
		
		TraineeData traineeData = new TraineeData() ;
		if (false == traineeManager.existData(iTraineeId, traineeData))
		{
			Logger.trace(sFctName + ": cannot find trainee information for ID = " + iTraineeId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		int iSalesDirectorId = traineeData.getCoachId() ;
		if (iSalesDirectorId <= 0)
		{
			Logger.trace(sFctName + ": wrong coach ID (" + iSalesDirectorId + ") for trainee ID = " + iTraineeId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		// Finally, get corresponding user
		//
		UserManager userManager = new UserManager(_iUserId, dbConnector, true) ;
		
		UserData userData = new UserData() ; 
		if (false == userManager.existUser(iSalesDirectorId, userData))
		{
			Logger.trace(sFctName + ": cannot find user for ID = " + iSalesDirectorId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		return userData.getEMail() ;
	}
	
	/**
	 * Get a trainee's regional director's mail address 
	 * 
	 * @param iTraineeId Trainee's identifier
	 * 
	 * @return The mail address if found, <code>""</code> if not
	 */
	protected String getMailAddressForRegionalDirector(int iTraineeId, DBConnector dbConnector)
	{
		String sFctName = "SendMailsHandler.getMailAddressForRegionalDirector" ;
		
		if ((iTraineeId < 0) || (null == dbConnector))
		{
			if (iTraineeId < 0)
				Logger.trace(sFctName + ": wrong trainee ID (" + iTraineeId + ").", _iUserId, Logger.TraceLevel.ERROR) ;
			if (null == dbConnector)
				Logger.trace(sFctName + ": null database connector.", _iUserId, Logger.TraceLevel.ERROR) ;
			
			return "" ;
		}
		
		// Find trainee data in order to get its region
		//
		TraineeDataManager traineeManager = new TraineeDataManager(_iUserId, dbConnector) ;
		
		TraineeData traineeData = new TraineeData() ;
		if (false == traineeManager.existData(iTraineeId, traineeData))
		{
			Logger.trace(sFctName + ": cannot find trainee information for ID = " + iTraineeId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		int iRegionId = traineeData.getRegionId() ;
		if (iRegionId <= 0)
		{
			Logger.trace(sFctName + ": wrong region ID (" + iRegionId + ") for trainee ID = " + iTraineeId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		// Find region information
		//
		RegionDataManager regionManager = new RegionDataManager(_iUserId, dbConnector) ;
		
		RegionData regionData = new RegionData() ; 
		if (false == regionManager.existData(iRegionId, regionData))
		{
			Logger.trace(sFctName + ": cannot find region information for ID = " + iRegionId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		int iZoneId = regionData.getZoneId() ;
		if (iZoneId <= 0)
		{
			Logger.trace(sFctName + ": wrong zone ID (" + iZoneId + ") for region ID = " + iRegionId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		// Find user role of the kind "Z" + zoneID
		//
		CoachingFitUserRoleDataManager userRoleManager = new CoachingFitUserRoleDataManager(_iUserId, dbConnector) ;
		
		List<CoachingFitUserRoleData> aRoles = new ArrayList<CoachingFitUserRoleData>() ;
		userRoleManager.fillRolesForRolePattern("Z" + iZoneId, true, aRoles) ;
		
		if (aRoles.isEmpty())
		{
			Logger.trace(sFctName + ": cannot find any user role for pattern = Z" + iZoneId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		CoachingFitUserRoleData userRole = aRoles.get(0) ;
		
		int iRegionDirectorUserId = userRole.getUserId() ;
		if (iRegionDirectorUserId <= 0)
		{
			Logger.trace(sFctName + ": wrong user ID (" + iRegionDirectorUserId + ") for user role ID = " + userRole.getId() + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		// Finally, get corresponding user
		//
		UserManager userManager = new UserManager(_iUserId, dbConnector, true) ;
		
		UserData userData = new UserData() ; 
		if (false == userManager.existUser(iRegionDirectorUserId, userData))
		{
			Logger.trace(sFctName + ": cannot find user for ID = " + iRegionDirectorUserId + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return "" ;
		}
		
		return userData.getEMail() ;
	}
	
	protected void addMailAddressForPattern(final String sPattern, boolean bExact, List<MailTo> aMailAddresses, MailTo.RecipientType iRecipientType, DBConnector dbConnector)
	{
		String sFctName = "SendMailsHandler.addMailAddressForPattern" ;
		
		if ((null == sPattern) || (null == aMailAddresses) || (null == dbConnector))
		{
			if (null == sPattern)
				Logger.trace(sFctName + ": null pattern.", _iUserId, Logger.TraceLevel.ERROR) ;
			if (null == dbConnector)
				Logger.trace(sFctName + ": null database connector.", _iUserId, Logger.TraceLevel.ERROR) ;
			if (null == aMailAddresses)
				Logger.trace(sFctName + ": null mail addresses list.", _iUserId, Logger.TraceLevel.ERROR) ;
			
			return ;
		}

		if ("".equals(sPattern) && (true == bExact))
		{
			Logger.trace(sFctName + ": empty pattern is not compatible with exact search.", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		// Find proper user roles
		//
		CoachingFitUserRoleDataManager userRoleManager = new CoachingFitUserRoleDataManager(_iUserId, dbConnector) ;
			
		List<CoachingFitUserRoleData> aRoles = new ArrayList<CoachingFitUserRoleData>() ;
		userRoleManager.fillRolesForRolePattern(sPattern, bExact, aRoles) ;
			
		if (aRoles.isEmpty())
		{
			if (bExact)
				Logger.trace(sFctName + ": cannot find any user role for exact pattern = " + sPattern + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			else
				Logger.trace(sFctName + ": cannot find any user role for pattern = " + sPattern + ".", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		UserManager userManager = new UserManager(_iUserId, dbConnector, true) ;
		
		// Only get the roles with no archetype 
		//
		for (CoachingFitUserRoleData userRole : aRoles)
		{
			int iArchetypeId = userRole.getArchetypeId() ;
			if (iArchetypeId <= 0)
			{
				UserData userData = new UserData() ; 
				if (false == userManager.existUser(userRole.getUserId(), userData))
					Logger.trace(sFctName + ": cannot find user for ID = " + userRole.getUserId() + ".", _iUserId, Logger.TraceLevel.ERROR) ;
				else
				{
					String sUserMail = userData.getEMail() ;
					if ("".equals(sUserMail))
						Logger.trace(sFctName + ": empty mail for user ID = " + userRole.getUserId() + ".", _iUserId, Logger.TraceLevel.ERROR) ;
					else
					{
						if (false == isInAddressesList(sUserMail, aMailAddresses))
							aMailAddresses.add(new MailTo(sUserMail, iRecipientType)) ;
					}
				}
			}
		}
	}
	
	/**
	 * Is a mail address already present in the list?
	 * 
	 * @param sMailAddress   Mail address to look for in the list
	 * @param aMailAddresses List of mail addresses to look into
	 * 
	 * @return <code>true</code> if the mail address is found, <code>false</code> if not, or if a variable is null or empty
	 */
	protected boolean isInAddressesList(final String sMailAddress, final List<MailTo> aMailAddresses)
	{
		if ((null == sMailAddress) || "".equals(sMailAddress) || aMailAddresses.isEmpty())
			return false ;
		
		for (MailTo mailTo : aMailAddresses)
			if (sMailAddress.equalsIgnoreCase(mailTo.getAddress()))
				return true ;
		
		return false ;
	}
	
	/**
	 * Computes an increment in base 36.
	 * @param id the String id in base 36.
	 * @return the following id or <code>""</code> if the counter is full
	 */
	static public String getNextId(final String id)
	//=============================================
	{
		if (null == id)
		  return null ;
		
		int len = id.length() ;
		if (0 == len)
		  return null ;
  
		StringBuffer nextId = new StringBuffer(id) ;	//make a copy of the id
		int i = len - 1 ;
		while (true)
		{
			char j = id.charAt(i) ;
			j++ ;
			if ((j >= '0' && j <= '9') || (j >= 'A' && j <= 'Z'))
			{
				nextId.setCharAt(i, j) ;
				break ;
			}
			else if (j > '9' && j < 'A')
			{
				nextId.setCharAt(i, 'A') ;
				break ;
			}
			else
			{
				nextId.setCharAt(i, '0') ;
				if (0 == i)
				  return "" ;
				i-- ;
			}
		}
		return nextId.toString() ;    
	}
	
	/**
	 * Check if a String is <code>null</code> or <code>""</code>
	 *     
	 * @return true if it is the case, false if not
	 **/
	protected boolean isNullOrEmpty(final String sToCheck) {
		return (null == sToCheck) || ("".equals(sToCheck)) ;
	}
	
	/**
	 * Returns a String with trailing '/' removed
	 *     
	 **/
	protected String removeFinalSlashes(final String sModel)
	{
		if ((null == sModel) || "".equals(sModel))
			return "" ;
		
		String sResult = sModel ;
		
		int iLen = sResult.length() ;
		while ((iLen > 0) && ('/' == sResult.charAt(iLen - 1)))
		{
			sResult = sResult.substring(0, iLen - 1) ;
			iLen = sResult.length() ;
		}
		
		return sResult ;
	}
	
	@Override
	public Class<SendFormByMailAction> getActionType() {
		return SendFormByMailAction.class;
	}

	@Override
	public void rollback(SendFormByMailAction action, SendFormByMailResult result, ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
