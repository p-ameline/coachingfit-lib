package com.coachingfit.server.handler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.coachingfit.server.DbParameters;
import com.coachingfit.server.model.CoachingFitFormDataManager;
import com.coachingfit.server.model.RegionDataManager;
import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.database.RegionData;
import com.coachingfit.shared.database.TraineeData;

import com.google.inject.Inject;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.csv.CsvInfoOption;
import com.primege.server.csv.CsvInformation;
import com.primege.server.csv.CsvRecord;
import com.primege.server.csv.CsvStructure;
import com.primege.server.model.FormInformationManager;
import com.primege.server.model.UserManager;
import com.primege.shared.database.FormDataData;
import com.primege.shared.database.UserData;
import com.primege.shared.model.FormBlock;

public class CoachingFitBuildCsvEngine
{	
	private FileOutputStream  _outputStream = null ;
	private String            _sErrorMsg ;

	private List<UserData>    _aCoachs ;
	private List<TraineeData> _aTrainees ;
	private List<RegionData>  _aRegions ;

	@Inject
	public CoachingFitBuildCsvEngine()
	{
		_sErrorMsg = "" ;
		_aCoachs   = null ;
		_aTrainees = null ;
		_aRegions  = null ;
	}

	public String execute(final int iUserId, final CsvStructure csvStructure)
	{
		String sFunctionName = "CoachingFitBuildCsvEngine.execute" ;

		if (null == csvStructure)
			return "Invalid parameters" ;

		Logger.trace(sFunctionName + " CSV file creation started to file \"" + DbParameters.getCSV() + "\".", iUserId, Logger.TraceLevel.DETAIL) ;

		try
		{
			_outputStream = new FileOutputStream(DbParameters.getCSV(), false) ;
		} 
		catch (FileNotFoundException e1)
		{
			Logger.trace(sFunctionName + " Cannot create file  \"" + DbParameters.getCSV() + "\". FileNotFoundException " + e1.getMessage(), iUserId, Logger.TraceLevel.ERROR) ;
			// e1.printStackTrace();
			return "Cannot create file " + DbParameters.getCSV() ;
		}

		if ("1".equals(csvStructure.getHeaderLine()))
			addHeader(csvStructure) ;

		DBConnector dbConnector = new DBConnector(false) ;

		// Get all valid forms in database (discard drafts and deleted)
		//
		List<CoachingFitFormData> aForms = new ArrayList<CoachingFitFormData>() ;

		CoachingFitFormDataManager formsManager = new CoachingFitFormDataManager(iUserId, dbConnector) ;
		if (false == formsManager.getThemAll(aForms, true))
		{
			Logger.trace(sFunctionName + " Error when getting forms in database.", iUserId, Logger.TraceLevel.ERROR) ;
			return "Error when getting forms in database" ;
		}

		if (aForms.isEmpty())
		{
			Logger.trace(sFunctionName + " No information in database.", iUserId, Logger.TraceLevel.ERROR) ;
			return "No information in database" ;
		}

		Logger.trace(sFunctionName + " " + aForms.size() + " forms are present in the database.", iUserId, Logger.TraceLevel.SUBDETAIL) ;



		// Prepare selection based on root
		//
		String sRoots = csvStructure.getRoots() ;

		Logger.trace(sFunctionName + " Roots list = \"" + sRoots + "\".", iUserId, Logger.TraceLevel.SUBDETAIL) ;

		List<String> aRoots = new ArrayList<String>() ;
		if (false == sRoots.isEmpty())
		{
			String sMsg = " CSV will be filled for roots = " ;

			// Of course, spilt is easier, but Regex are slow
			int iPrevious = 0 ;
			int iComaPos  = sRoots.indexOf(';') ;
			if (-1 == iComaPos)
			{
				aRoots.add(sRoots) ;
				sMsg += sRoots ;
			}
			else
			{
				while (-1 != iComaPos)
				{
					String sRoot = sRoots.substring(iPrevious, iComaPos) ;
					aRoots.add(sRoot) ;
					iPrevious = iComaPos + 1 ;
					iComaPos  = sRoots.indexOf(';', iPrevious) ;

					sMsg += sRoots + ", " ;
				}

				String sRoot = sRoots.substring(iPrevious) ;
				aRoots.add(sRoot) ;
				sMsg += sRoots ;
			}

			Logger.trace(sFunctionName + sMsg, iUserId, Logger.TraceLevel.SUBDETAIL) ;
		}

		// Get all forms' contents from database
		//
		List<FormBlock<FormDataData>> aFormBlocks = new ArrayList<FormBlock<FormDataData>>() ;

		FormInformationManager formInformationManager = new FormInformationManager(iUserId, dbConnector) ;

		int iValidFormsCount = 0 ;

		for (CoachingFitFormData formData : aForms)
		{
			boolean bIsValid = true ;

			if (false == aRoots.isEmpty())
			{
				String sFormRoot = formData.getRoot() ;

				bIsValid = false ;

				if ((null != sFormRoot) && (false == sFormRoot.isEmpty()))
				{
					for (String sRoot : aRoots)
						if (sFormRoot.equals(sRoot))
						{
							bIsValid = true ;
							break ;
						}
				}
			}

			if (bIsValid)
			{
				FormBlock<FormDataData> formBlock = new FormBlock<FormDataData>() ;
				formBlock.setDocumentLabel(formData) ;

				formInformationManager.loadFormData(formData.getFormId(), formBlock) ;

				aFormBlocks.add(formBlock) ;

				iValidFormsCount++ ;
			}
		}

		if (aFormBlocks.isEmpty())
		{
			Logger.trace(sFunctionName + " No valid form found for CSV.", iUserId, Logger.TraceLevel.DETAIL) ;
			return "No information in database" ;
		}

		Logger.trace(sFunctionName + " Found " + iValidFormsCount + " valid forms (from " + aForms.size() + " forms in database) for CSV.", iUserId, Logger.TraceLevel.SUBDETAIL) ;

		// Get the list of coaches (i.e. users)
		//
		_aCoachs = new ArrayList<UserData>() ;
		UserManager usersManager = new UserManager(iUserId, dbConnector) ;
		usersManager.getThemAll(_aCoachs) ;

		// Get the list of trainees
		//
		_aTrainees = new ArrayList<TraineeData>() ;
		TraineeDataManager traineesManager = new TraineeDataManager(iUserId, dbConnector) ;
		traineesManager.getThemAll(_aTrainees) ;

		// Get the list of regions
		//
		_aRegions = new ArrayList<RegionData>() ;
		RegionDataManager regionsManager = new RegionDataManager(iUserId, dbConnector) ;
		regionsManager.getThemAll(_aRegions) ;

		// Fill the CSV file, one formBlock at a time
		//
		for (FormBlock<FormDataData> formBlock : aFormBlocks)
			addFormBlockToCsv(formBlock, csvStructure) ;

		closeFile() ;

		Logger.trace(sFunctionName + " CSV file creation ended properly.", iUserId, Logger.TraceLevel.DETAIL) ;

		return _sErrorMsg ;
	}

	void addStringToFile(String sContent)
	{
		if (null == sContent)
			return ;

		String s = sContent + "\n" ;
		byte data[] = s.getBytes() ;
		try
		{
			_outputStream.write(data, 0, data.length) ;
		} 
		catch (IOException x)
		{
			System.err.println(x);
		}
	}

	void closeFile()
	{
		try
		{
			_outputStream.flush() ;
		} 
		catch (IOException e)
		{ e.printStackTrace() ;
		}

		try
		{
			_outputStream.close() ;
		} 
		catch (IOException e)
		{ e.printStackTrace() ;
		}
	}

	void addHeader(final CsvStructure csvStructure)
	{
		String s = "" ;

		if ((null == csvStructure.getRecords()) || csvStructure.getRecords().isEmpty())
		{
			addStringToFile(s) ;
			return ;
		}

		for (CsvRecord csvRecord : csvStructure.getRecords())
		{
			if ((null != csvRecord.getInformations()) || (false == csvRecord.getInformations().isEmpty()))
			{
				for (CsvInformation csvInformation : csvRecord.getInformations())
				{
					if (false == "".equals(s))
						s += ";" ;
					s += csvInformation.getCaption() ;
				}
			}
		}

		addStringToFile(s) ;
	}

	/**
	 * Add a line to the CSV file. This line will be structured according to a CsvStructure
	 * and will publish information from a single form  
	 * 
	 * @param formBlock    content to be published 
	 * @param csvStructure structure of the line
	 * 
	 * @return <code>true</code> if everything went well, <code>false</code> if not
	 * 
	 **/
	boolean addFormBlockToCsv(final FormBlock<FormDataData> formBlock, final CsvStructure csvStructure)
	{
		if ((null == formBlock) || (null == csvStructure))
		{
			_sErrorMsg = "Invalid parameters when processing a CoachingFitFormBlock" ;
			return false ;
		}

		if ((null == csvStructure.getRecords()) || csvStructure.getRecords().isEmpty())
		{
			_sErrorMsg = "Invalid CSV description when processing a CityForDateBlock" ;
			return false ;
		}

		String s = "" ;

		// Processing records. There is probably only one Record and it is global
		//
		for (CsvRecord csvRecord : csvStructure.getRecords())
		{
			if ((null != csvRecord.getInformations()) || (false == csvRecord.getInformations().isEmpty()))
			{
				for (CsvInformation csvInformation : csvRecord.getInformations())
				{
					String sNewInfo = getInfo(csvInformation, formBlock) ;

					if (false == "".equals(s))
						s += ";" ;
					s += escapeString(sNewInfo) ;
				}
			}
		}

		addStringToFile(s) ;

		return true ;
	}

	/**
	 * Find the information (site and path) in the CoachForDateBlock and, if found, return it as a formatted string 
	 * 
	 * @param csvInformation information to look for 
	 * @param iTraineeId     trainee to look information for, or -1 if information is global (date or coach for example)
	 * @param coach4date     CoachForDateBlock to look information into
	 * 
	 * @return A formated string, or ""
	 * 
	 **/
	protected String getInfo(final CsvInformation csvInformation, final FormBlock<FormDataData> formBlock)
	{
		if ((null == csvInformation) || (null == formBlock))
			return "" ;

		String sPath = csvInformation.getPath() ;

		CoachingFitFormData formData = (CoachingFitFormData) formBlock.getDocumentLabel() ;
		if (null == formData)
			return "" ;

		if ("$date$".equals(sPath))
			return getFormatedStringValue(csvInformation.getType(), formData.getCoachingDate(), csvInformation.getFormat()) ;

		if ("$coach$".equals(sPath))
			return getFormatedStringValue(csvInformation.getType(), getCoachLabelFromId(formData.getAuthorId()), csvInformation.getFormat()) ;

		if ("$senior$".equals(sPath))
			return getFormatedStringValue(csvInformation.getType(), getTraineeLabelFromId(formData.getSeniorTraineeId()), csvInformation.getFormat()) ;

		if ("$trainee$".equals(sPath))
			return getFormatedStringValue(csvInformation.getType(), getTraineeLabelFromId(formData.getTraineeId()), csvInformation.getFormat()) ;

		if ("$region$".equals(sPath))
			return getFormatedStringValue(csvInformation.getType(), getRegionLabelFromId(formData.getRegionId()), csvInformation.getFormat()) ;

		String sValue = getInformationInForm(csvInformation, formBlock) ;
		if ("".equals(sValue))
			return "" ;

		return getFormatedStringValue(csvInformation.getType(), sValue, csvInformation.getFormat()) ;
	}

	/**
	 * Return a formated string 
	 * 
	 * @param sType   information type, for example "date"
	 * @param sValue  value to format
	 * @param sFormat format pattern, for example "DD/MM" for dates 
	 * 
	 * @return The formated value
	 * 
	 **/
	protected String getFormatedStringValue(final String sType, final String sValue, final String sFormat)
	{
		if ("".equals(sFormat))
			return sValue ;

		// Date
		//
		if ("date".equalsIgnoreCase(sType))
		{
			if (sValue.length() != 8)
				return sValue ;

			String sToReturn = sFormat ;

			sToReturn = sToReturn.replace("YYYY", sValue.substring(0, 4)) ;
			sToReturn = sToReturn.replace("MM", sValue.substring(4, 6)) ;
			sToReturn = sToReturn.replace("DD", sValue.substring(6, 8)) ;

			return sToReturn ;
		}

		// Seniority expressed as a count of months
		//
		if ("seniorityasmonths".equalsIgnoreCase(sType))
		{
			if (sValue.length() != 6)
				return sValue ;

			String sYears  = sValue.substring(0, 2) ;
			String sMonths = sValue.substring(2, 4) ;

			int iMonthsCount = Integer.parseInt(sYears) * 12 + Integer.parseInt(sMonths) ;

			return "" + iMonthsCount ;
		}

		return sValue ;
	}

	/**
	 * Find the information (site and path) in the CityForDateBlock 
	 * 
	 * @param iSiteId        site to look information for, or -1 if information is global (date or city for example)
	 * @param csvInformation CsvInformation to look information for
	 * @param city4date      CityForDateBlock to look information into
	 * 
	 * @return The value if site and path exist in CityForDateBlock, "" if not
	 * 
	 **/
	protected String getInformationInForm(final CsvInformation csvInformation, final FormBlock<FormDataData> formBlock)
	{
		if ((null == csvInformation) || (null == formBlock))
			return "" ;

		String sPath = csvInformation.getPath() ;
		if ("".equals(sPath))
			return "" ;

		// Get the set of data for this form
		//
		List<FormDataData> aData = formBlock.getInformation() ;
		if ((null == aData) || aData.isEmpty())
			return "" ;

		List<CsvInfoOption> aOptions = csvInformation.getOptions() ;

		for (FormDataData data : aData)
		{
			if (sPath.equals(data.getPath()))
				return data.getValue() ;

			// In case this element contains options, check global path: path + "/" + option path
			//
			if ((null != aOptions) && (false == aOptions.isEmpty()))
			{
				for (CsvInfoOption currentOption : aOptions)
				{
					String sOptionPath = sPath + "/" + currentOption.getPath() ; 
					if (sOptionPath.equals(data.getPath()))
						return currentOption.getCaption() ;
				}
			}
		}

		return "" ;
	}

	/**
	 * Get the coach label from her identifier 
	 * 
	 * @param iCoachId coach identifier to get label from
	 * 
	 * @return The label if a coach with this identifier is found, <code>""</code> if not
	 * 
	 **/
	protected String getCoachLabelFromId(final int iCoachId)
	{
		if ((null == _aCoachs) || _aCoachs.isEmpty())
			return "" ;

		for (UserData coach : _aCoachs)
			if (coach.getId() == iCoachId)
				return coach.getLabel() ;

		return "" ;
	}

	/**
	 * Get the trainee label from her identifier 
	 * 
	 * @param iTraineeId trainee identifier to get label from
	 * 
	 * @return The label if a trainee with this identifier is found, <code>""</code> if not
	 * 
	 **/
	protected String getTraineeLabelFromId(final int iTraineeId)
	{
		if ((null == _aTrainees) || _aTrainees.isEmpty())
			return "" ;

		for (TraineeData trainee : _aTrainees)
			if (trainee.getId() == iTraineeId)
				return trainee.getLabel() ;

		return "" ;
	}

	/**
	 * Get the region label from her identifier 
	 * 
	 * @param iRegionId region identifier to get label from
	 * 
	 * @return The label if a region with this identifier is found, <code>""</code> if not
	 * 
	 **/
	protected String getRegionLabelFromId(final int iRegionId)
	{
		if ((null == _aRegions) || _aRegions.isEmpty())
			return "" ;

		for (RegionData region : _aRegions)
			if (region.getId() == iRegionId)
				return region.getLabel() ;

		return "" ;
	}

	/**
	 * Escape a text so that it becomes "Excel CSV compatible". Typically:<br>
	 * - embed the field inside a set of double quotes if it contains the separator character (i.e. ';')<br>
	 * - in fields containing a double quote, replace the single double quote with two double quotes. 
	 * 
	 * @param sText the text to be escaped
	 * 
	 * @return The escaped text
	 * 
	 **/
	protected String escapeString(final String sText)
	{
		String sReturn = sText ;

		// We will embed the text inside a set of double quotes, so we will just replace double quotes by 2 double quotes
		// 
		int iDoubleQuote = sReturn.indexOf("\"") ;

		//
		//  abcd"efg
		//  01234567
		//
		while (iDoubleQuote > 0)
		{
			int iTextLen = sReturn.length() ;

			if (iDoubleQuote == iTextLen - 1) // The double quote is the last char
			{
				sReturn += "\"" ;
				iDoubleQuote = -1 ;
			}
			else
			{
				sReturn = sReturn.substring(0, iDoubleQuote + 1) + "\"" + sReturn.substring(iDoubleQuote + 1, iTextLen) ;
				iDoubleQuote = sReturn.indexOf("\"", iDoubleQuote + 2) ;
			}
		}

		return "\"" + sReturn + "\"" ;
	}
}
