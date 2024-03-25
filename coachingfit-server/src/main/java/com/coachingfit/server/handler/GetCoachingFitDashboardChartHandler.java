package com.coachingfit.server.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.GetCoachingFitDashboardBlocksInBase;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.model.CoachForDateBlock;
import com.coachingfit.shared.model.CoachingFitDashboardBlocks;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardChartAction;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardChartResult;
import com.coachingfit.shared.util.CoachingFitDelay;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.shared.database.FormDataData;
import com.primege.shared.model.DashboardChart;
import com.primege.shared.model.DashboardTable;
import com.primege.shared.model.DashboardTableCol;
import com.primege.shared.model.DashboardTableLine;
import com.primege.shared.model.FormBlock;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/** 
 * Object in charge of getting all information to fill a given chart 
 */
public class GetCoachingFitDashboardChartHandler implements ActionHandler<GetCoachingFitDashboardChartAction, GetCoachingFitDashboardChartResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	protected ArrayList<DashboardTableLine>      _aLines ;
	
	protected DBConnector                        _dbConnector ;
	
	protected int                                _iUserId ;
	protected int                                _iCoachId ;
	protected int                                _iPivotId ;
	protected String                             _sYear ;
	protected boolean                            _bOnlyAuthoredInfo ;
	protected DashboardChart                     _chart ;
	
	@Inject
	public GetCoachingFitDashboardChartHandler(final Provider<ServletContext>     servletContext,
			                                       final Provider<HttpServletRequest> servletRequest)
	{
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
		
		_aLines = null ;
		
		_dbConnector = null ;
		
		_iUserId  = -1 ;
		_iCoachId = -1 ;
		_iPivotId = -1 ;
		_sYear    = "" ;
		_bOnlyAuthoredInfo = false ;
		_chart    = null ;
	}

	@Override
	public GetCoachingFitDashboardChartResult execute(GetCoachingFitDashboardChartAction action, ExecutionContext context) throws ActionException 
	{
		// Get context
		//
		_iUserId           = action.getUserId() ;
		_iCoachId          = action.getCoachId() ;
		_iPivotId          = action.getPivotId() ;
		_sYear             = action.getYear() ;
		_bOnlyAuthoredInfo = action.onlyAuthoredInfo() ;
		
		_chart             = action.getChart() ;
	
		GetCoachingFitDashboardChartResult result = new GetCoachingFitDashboardChartResult() ;
		result.setDashboardChart(_chart) ;
		
		String sPivot = _chart.getPivot() ;
		if ("$trainee$".equalsIgnoreCase(sPivot))
			getTraineeChartInformation(result) ;
		
		return result ;
	}
	
	/**
	 * Pivot is the trainee 
	 */
	protected void getTraineeChartInformation(GetCoachingFitDashboardChartResult result)
	{
		// Prepare result
		//
		_aLines = result.getLines() ;
		_aLines.clear() ;
		
		if (-1 == _iPivotId)
			return ;
		
		_dbConnector = new DBConnector(false) ;
		
		GetCoachingFitDashboardBlocksInBase blocksLoader = new GetCoachingFitDashboardBlocksInBase(_iUserId, _dbConnector) ;
		CoachingFitDashboardBlocks          dashBlocks   = new CoachingFitDashboardBlocks() ; 
		
		String[] aRoots = DashboardTable.getRoots(_chart.getRoots()) ;
		
		// Get all forms for the trainee
		//
		blocksLoader.GetDashboardBlocks(-1, aRoots, _iPivotId, "", "", -1, dashBlocks) ;
		
		// Lines are defined by existing information for the X (abscissa) information, get its pivot
		//
		DashboardTableCol abscissaCol = _chart.getPivotForAbscissa() ;
		if (null == abscissaCol)
		{
			result.setMessage("Cannot find chart column for abscissa.") ;
			Logger.trace("GetCoachingFitDashboardChartHandler: Cannot find chart column for abscissa.", _iUserId, Logger.TraceLevel.ERROR) ;
			return ;
		}
		
		// Abscissa is sessions dates
		//
		if ("$user$.$trainee$.$session$.$date$".equals(abscissaCol.getColPath()))
		{
			fillChartForSessionsDates(dashBlocks, blocksLoader, result) ;
		}
	}
	
	/**
	 * Fill the table with all information from a block that contains all sessions for a given trainee, with dates as abscissa 
	 * 
	 * @param dashBlocks All sessions for the trainee
	 * 
	 * */
	protected void fillChartForSessionsDates(final CoachingFitDashboardBlocks dashBlocks, GetCoachingFitDashboardBlocksInBase blocksLoader, GetCoachingFitDashboardChartResult result)
	{
		if ((null == dashBlocks) || dashBlocks.isEmpty() || (null == result))
			return ;
		
		ArrayList<CoachForDateBlock> aBlocks = dashBlocks.getInformation() ;
		if ((null == aBlocks) || aBlocks.isEmpty())
			return ;
		
		// Are global information needed?
		//
		boolean bGlobalNeeded  = false ;
		boolean bAlwaysCompare = true ;
		for (DashboardTableCol col : _chart.getCols())
		{
			String sPath = col.getColPath() ;
			if (sPath.startsWith("$global$"))
			{
				bGlobalNeeded = true ;
				if (false == sPath.contains("$sameSeniority$"))
					bAlwaysCompare = false ;
			}
		}
		
		CoachingFitDashboardBlocks globalDashBlocks = null ;
		if (bGlobalNeeded)
		{
			String[] aRoots = DashboardTable.getRoots(_chart.getRoots()) ;
			
			if (bAlwaysCompare)
			{
				// Since all global informations are comparative, we get min and max dates for current trainee sessions to adjust the global query
				//
				String sMinDate = "99999999999999" ;
				String sMaxDate = "00000000000000" ;
				
				for (CoachForDateBlock block : aBlocks)
				{
					String sSessionDate = block.getSessionDate() ;
					if (sSessionDate.compareTo(sMaxDate) > 0)
						sMaxDate = sSessionDate ;
					if (sSessionDate.compareTo(sMinDate) < 0)
						sMinDate = sSessionDate ;
				}
				
				// Getting an interval of 6 months before and after this session to get mean score of trainees with same seniority
				//
				sMinDate = substractMonths(sMinDate, 6) ;
				sMaxDate = addMonths(sMaxDate, 6) ;
				
				// Get all forms for this interval of time
				//
				globalDashBlocks = new CoachingFitDashboardBlocks(-1, sMinDate, sMaxDate, null) ;
				
				Logger.trace("GetCoachingFitDashboardChartHandler: Getting all sessions in database from " + sMinDate + " to " + sMaxDate , _iUserId, Logger.TraceLevel.DETAIL) ;
				blocksLoader.GetDashboardBlocks(-1, aRoots, -1, sMinDate, sMaxDate, -1, globalDashBlocks) ;
			}
			else
			{
				// Get all forms in database
				//
				Logger.trace("GetCoachingFitDashboardChartHandler: Getting all sessions in database.", _iUserId, Logger.TraceLevel.DETAIL) ;
				blocksLoader.GetDashboardBlocks(-1, aRoots, -1, "", "", -1, globalDashBlocks) ;
			}
		}
		
		// Iterate on each block (all sessions at a given date)
		//
		for (CoachForDateBlock block : aBlocks)
		{
			String sSessionDate = block.getSessionDate() ;
		
			// Iterate on all sessions
			//
			ArrayList<FormBlock<FormDataData>> aSessions = block.getInformation() ;
			if ((null != aSessions) && (false == aSessions.isEmpty()) && ((false == _bOnlyAuthoredInfo) || (block.getCoachId() == _iCoachId)))
			{
				for (FormBlock<FormDataData> formBlock : aSessions)
				{
					// Create a line for this session
					//
					DashboardTableLine tableLine = createNewLine(sSessionDate) ;
					
					// Cell to fill is 1 since the rule here is that dates are in abscissa, hence column zero has already been set
					//
					int iCellIndex = 1 ;
					int iColIndex  = 0 ;
					
					// Set all columns
					//
					for (DashboardTableCol column : _chart.getCols())
					{
						// String sPath = column.getColPath() ;
						
						String sType = _chart.getTypeForIndex(iColIndex) ;
						iColIndex++ ;
						
						if ((null != sType) && (false == "X".equalsIgnoreCase(sType)))
						{
							String sCellValue = getCellContent(column, formBlock, globalDashBlocks) ;
							if (null != sCellValue)
								tableLine.setContent(iCellIndex, sCellValue) ;
							else
								tableLine.setContent(iCellIndex, "") ;
								
							iCellIndex++ ;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Get information, defined by the column, from session data and global database data
	 * 
	 * @return <code>null</code> if something went wrong (consider <code>""</code> as a valid answer)
	 */
	protected String getCellContent(final DashboardTableCol column, final FormBlock<FormDataData> formBlock, final CoachingFitDashboardBlocks globalDashBlocks)
	{
		if ((null == column) || (null == formBlock))
			return null ;
		
		// What are we looking for?
		// 
		String sPath = column.getColPath() ;
		
		// Is it a session data? 
		//
		String sSessionDataSpecifier = "$user$.$trainee$.$session$." ; 
		
		if (sPath.startsWith(sSessionDataSpecifier))
		{
			int iSpecifierLen = sSessionDataSpecifier.length() ;
			int iPathLen      = sPath.length() ; 
			
			if (iPathLen == iSpecifierLen)
				return null ;
			
			// Splitting the string located after "$user$.$trainee$.$session$."
			//
			String[] sParams = sPath.substring(iSpecifierLen, iPathLen).split("\\.") ;
			
			// First parameter is the path to required information, the second, if any, is the modifier
			//
			String sLocalPath = sParams[0] ;
			String sModifier  = (sParams.length > 1) ? sParams[1] : "" ;
			
			return getCellContentFromSession(sLocalPath, sModifier, formBlock) ;
		}
		
		// Is it a global data built to compare same seniority trainees? 
		//
		String sGlobalSameSenioritySpecifier = "$global$.$sameSeniority$.$session$." ; 
			
		if (sPath.startsWith(sGlobalSameSenioritySpecifier))
		{
			int iSpecifierLen = sGlobalSameSenioritySpecifier.length() ;
			int iPathLen      = sPath.length() ; 
			
			if (iPathLen == iSpecifierLen)
				return null ;
			
			// Splitting the string located after "$user$.$trainee$.$session$."
			//
			String[] sParams = sPath.substring(iSpecifierLen, iPathLen).split("\\.") ;
			
			// First parameter is the path to required information, the second, if any, is the modifier
			//
			String sLocalPath = sParams[0] ;
			String sModifier  = (sParams.length > 1) ? sParams[1] : "" ;
			
			return getGlobalCellContent(sLocalPath, sModifier, formBlock, globalDashBlocks, true) ;
		}
		
		return "" ;
	}
	
	/**
	 * Get information, defined by the column, from session data and global database data
	 * 
	 * @return <code>null</code> if something went wrong (consider <code>""</code> as a valid answer)
	 */
	protected String getCellContentFromSession(final String sLocalPath, final String sModifier, final FormBlock<FormDataData> formBlock)
	{
		if ((null == sLocalPath) || "".equals(sLocalPath) || (null == formBlock))
			return null ;
		
		// No modifier, simply return the data from its path
		//
		if ((null == sModifier) || "".equals(sModifier))
		{
			FormDataData data = formBlock.getDataForPath(sLocalPath) ;
			if (null == data)
				return null ;
			
			return data.getValue() ;
		}
		
		// Percent of expected for chapters
		//
		if ("$%expected$".equalsIgnoreCase(sModifier))
		{
			// Get total and expected scores to get expected ratio 
			//
			FormDataData chapterScore  = null ;
			FormDataData expectedScore = null ;
			
			String sRealPathEnder = "/SCOR" ;
			String sExpcPathEnder = "/SCOE" ;
			
			// First case, the score is explicitly pointed (usually because it is the global score) 
			//
			if (sLocalPath.endsWith(sRealPathEnder))
			{
				chapterScore  = formBlock.getDataForPath(sLocalPath) ;
				
				String sLocalPathForExpected = sLocalPath.substring(0, sLocalPath.length() - sRealPathEnder.length()) + sExpcPathEnder ;
				expectedScore = formBlock.getDataForPath(sLocalPathForExpected) ;
			}
			else
			{
				chapterScore  = formBlock.getDataForPath(sLocalPath + sRealPathEnder) ;
				expectedScore = formBlock.getDataForPath(sLocalPath + sExpcPathEnder) ;
			}
			
			if ((null == chapterScore) || (null == expectedScore))
				return null ;
			
			try
			{
				double dChapterScore  = Double.parseDouble(chapterScore.getValue()) ;
				double dExpectedScore = Double.parseDouble(expectedScore.getValue()) ;
			
				return getTwoDigitPercent(dChapterScore, dExpectedScore) ;
			}
			catch (NumberFormatException e) {
				return null ;
			}
		}
		
		// Unknown modifier
		//
		return null ;
	}
	
	/**
	 * Get information, defined by the column, from global database data
	 * 
	 * @return <code>null</code> if something went wrong (consider <code>""</code> as a valid answer)
	 */
	protected String getGlobalCellContent(final String sLocalPath, final String sModifier, final FormBlock<FormDataData> formBlock, final CoachingFitDashboardBlocks globalDashBlocks, boolean bSameSeniority)
	{
		if ((null == sLocalPath) || "".equals(sLocalPath) || (null == formBlock) || (null == globalDashBlocks))
			return null ;
		
		String sSessionDate = "" ;
		CoachingFitFormData form = (CoachingFitFormData) formBlock.getDocumentLabel() ;
		if (null != form)
			sSessionDate = form.getCoachingDate() ;
		
		String sScanFrom    = substractMonths(sSessionDate, 6) ;
		String sScanTo      = addMonths(sSessionDate, 6) ;
		
		int iSeniorMonthsFrom = -1 ;
		int iSeniorMonthsTo   = Integer.MAX_VALUE ;
		
		if (bSameSeniority)
		{
			int iGlobalMonths = getSeniorityInMonths(formBlock) ;
			
			if ((iGlobalMonths >= 0) && (iGlobalMonths < 2))
			{
				iSeniorMonthsTo = 2 ;
			}
			else if (iGlobalMonths < 6)
			{
				iSeniorMonthsFrom = 2 ;
				iSeniorMonthsTo   = 6 ;
			}
			else if (iGlobalMonths < 12)
			{
				iSeniorMonthsFrom = 6 ;
				iSeniorMonthsTo   = 12 ;
			}
			else if (iGlobalMonths < 18)
			{
				iSeniorMonthsFrom = 12 ;
				iSeniorMonthsTo   = 18 ;
			}
			else if (iGlobalMonths < 24)
			{
				iSeniorMonthsFrom = 18 ;
				iSeniorMonthsTo   = 24 ;
			}
			else
				iSeniorMonthsFrom = 24 ;
		}
		
		return getGlobalInformation(sLocalPath, sModifier, sScanFrom, sScanTo, globalDashBlocks, iSeniorMonthsFrom, iSeniorMonthsTo) ;
	}
	
	/**
	 * Get information, defined by the column, from global database data
	 * 
	 * @return <code>null</code> if something went wrong (consider <code>""</code> as a valid answer)
	 */
	protected String getGlobalInformation(final String sLocalPath, final String sModifier, final String sDateFrom, final String sDateTo, final CoachingFitDashboardBlocks globalDashBlocks, int iSeniorMonthsFrom, int iSeniorMonthsTo)
	{
		if ((null == sLocalPath) || "".equals(sLocalPath) || (null == sDateFrom) || "".equals(sDateFrom) || (null == globalDashBlocks))
			return null ;
	
		ArrayList<CoachForDateBlock> aBlocks = globalDashBlocks.getInformation() ;
		if ((null == aBlocks) || aBlocks.isEmpty())
			return "" ;
		
		// Sort by date in order to know when to stop browsing.
		// We have to provide a specific comparator because the standard comparator for CoachForDateBlock sort by coach then by date  
		//
		Collections.sort(aBlocks, new Comparator<CoachForDateBlock>() {
      @Override
      public int compare(CoachForDateBlock o1, CoachForDateBlock o2) {
      	return o1.getSessionDate().compareTo(o2.getSessionDate()) ;
      }
		}) ;
		
		int iInstances    = 0 ;
		double dValuesSum = 0 ;
		
		for (CoachForDateBlock coachDateBloc : aBlocks)
		{
			String sBlockDate = coachDateBloc.getSessionDate() ;
			if (sBlockDate.compareTo(sDateTo) > 0)
				break ;
			
			for (FormBlock<FormDataData> session : coachDateBloc.getInformation())
			{
				boolean bSeniorityOk = true ;
				if ((iSeniorMonthsFrom >= 0) || (iSeniorMonthsTo < Integer.MAX_VALUE))
				{
					int iGlobalMonths = getSeniorityInMonths(session) ;
					if ((iGlobalMonths <= iSeniorMonthsFrom) || (iGlobalMonths > iSeniorMonthsTo))
						bSeniorityOk = false ;
				}
				
				if (bSeniorityOk)
				{
					String sContent = getCellContentFromSession(sLocalPath, sModifier, session) ;
					if ((null != sContent) && (false == "".equals(sContent)))
					{
						try 
						{
							double dValue = Double.parseDouble(sContent) ;
						
							dValuesSum += dValue ;
							iInstances++ ;	
						} 
						catch (NumberFormatException e) {
						}
					}
				}
			}
		}
		
		return getDoubleAsXdotXX(dValuesSum / iInstances) ;
	}
		
	/**
	 * Create a new line for a given date
	 **/
	protected DashboardTableLine createNewLine(final String sSessionDate)
	{
		int iColCount = _chart.getCols().size() ;  
		
		DashboardTableLine tableLine = new DashboardTableLine(iColCount) ;
		tableLine.setContent(0, sSessionDate) ;
		
		_aLines.add(tableLine) ;
		
		return tableLine ;
	}
		
	/**
	 * Get "now" as a String in the yyyyMMddHHmmss format
	 * 
	 * */
	protected String getCurrentDateAndTime()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss") ;
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime()) ;
	}
	
	protected String getDoubleAsString(final double dValue)
	{
		String sValue = Double.toString(dValue) ;
		if ("".equals(sValue))
			return "" ;
		
		if (sValue.endsWith(".0"))
			sValue = sValue.substring(0, sValue.length() - 2) ;
		
		return sValue ;
	}

	/**
	 * Get a string à la X.XX that represents iNumerator / iDenominator as a percentage
	 * 
	 * @return <code>null</code> if something went wrong.
	 */
	protected String getTwoDigitPercent(double dNumerator, double dDenominator)
	{
		if (0 == dNumerator)
			return "0.00" ;
		
		if (0 == dDenominator)
			return null ;
		
		// In order to get a value in the form X,XX %
		//
		return getDoubleAsXdotXX(100 * dNumerator / dDenominator) ;
	}
	
	/**
	 * Get a double as a X.XX string
	 * 
	 * @return <code>null</code> if something went wrong, a N.2 string if not
	 */
	protected String getDoubleAsXdotXX(final double dValue)
	{
		if (0 == dValue)
			return "0.00" ;
		
		String sRoundedValue = "" + (int) Math.floor(100 * dValue) ;
		
		int iResLen = sRoundedValue.length() ;
		if (0 == iResLen)
			return null ;
		
		if (1 == iResLen)
			return "0.0" + sRoundedValue ;
		if (2 == iResLen)
			return "0." + sRoundedValue ;
		
		return sRoundedValue.substring(0, iResLen - 2) + "." + sRoundedValue.substring(iResLen - 2, iResLen) ;
	}
	
	/**
	 * Return a string that represent a given date minus a count of months
	 */
	protected static String substractMonths(final String sDate, final int iMonthsCount)
	{
		if (null == sDate)
			return "" ;
		
		if (0 == iMonthsCount)
			return sDate ;
		
		int iDateLength = sDate.length() ;
		if (iDateLength < 6)
			return "" ;
		
		if (iMonthsCount < 0)
			return addMonths(sDate, -iMonthsCount) ;
		
		String sTrailing = "" ;
		if (iDateLength > 6)
			sTrailing = sDate.substring(6) ;

		// Remaining months (can be negative if it leads to some previous year)
		//
		int iFinalMonths = Integer.parseInt(sDate.substring(4, 6)) - iMonthsCount ;
				
		// Still positive, means same year
		//
		if (iFinalMonths > 0)
			return sDate.substring(0, 4) + getMonthAsString(iFinalMonths) + sTrailing ;
		
		// Negative, means that if points to some previous year
		//
		int iRemainsToSubstract = -iFinalMonths ;
		
		// Count how many full years are to be removed (at least 1 anyway)
		//
		int iYearsCount = 0 ;
		if (iRemainsToSubstract > 11)
			iYearsCount = Math.floorDiv(iRemainsToSubstract, 12) ;
		
		int iRemainingYears  = Integer.parseInt(sDate.substring(0, 4)) - iYearsCount - 1 ;
		int iRemainingMonths = 12 - (iRemainsToSubstract - (12 * iYearsCount)) ; 
				
		return "" + iRemainingYears + getMonthAsString(iRemainingMonths) + sTrailing ;
	}
	
	/**
	 * Return a string that represent a given date plus a count of months
	 */
	protected static String addMonths(final String sDate, final int iMonthsCount)
	{
		if (null == sDate)
			return "" ;
		
		if (0 == iMonthsCount)
			return sDate ;
		
		int iDateLength = sDate.length() ;
		if (iDateLength < 6)
			return "" ;
		
		if (iMonthsCount < 0)
			return substractMonths(sDate, -iMonthsCount) ;
		
		String sTrailing = "" ;
		if (iDateLength > 6)
			sTrailing = sDate.substring(6) ;

		// Remaining months (can be negative if it leads to some previous year)
		//
		int iFinalMonths = Integer.parseInt(sDate.substring(4, 6)) + iMonthsCount ;
				
		// Less than 13, means same year
		//
		if (iFinalMonths < 13)
			return sDate.substring(0, 4) + getMonthAsString(iFinalMonths) + sTrailing ;
		
		// More than 13, means that if points to some next year
		//
		int iRemainsToAdd = iFinalMonths - 12 ;
		
		// Count how many full years are to be added (at least 1 anyway)
		//
		int iYearsCount = 0 ;
		if (iRemainsToAdd > 12)
			iYearsCount = Math.floorDiv(iRemainsToAdd, 12) ;
		
		int iRemainingYears  = Integer.parseInt(sDate.substring(0, 4)) + iYearsCount + 1 ;
		int iRemainingMonths = iRemainsToAdd - (12 * iYearsCount) ; 
				
		return "" + iRemainingYears + getMonthAsString(iRemainingMonths) + sTrailing ;
	}
	
	/**
	 * Return a month as a two chars value ("00" if the parameter is not valid)
	 */
	protected static String getMonthAsString(int iMonth)
	{
		if ((iMonth < 0) || (iMonth > 12))
			return "00" ;
		
		if (iMonth < 10)
			return "0" + iMonth ;
		
		return "" + iMonth ;
	}

	/**
	 * Get the seniority expressed in months
	 * 
	 * @param formBlock Record the seniority is to be extracted
	 * 
	 * @return <code>-1</code> is something went wrong, else the computed seniority
	 */
	protected int getSeniorityInMonths(final FormBlock<FormDataData> formBlock)
	{
		if (null == formBlock)
			return -1 ;
		
		FormDataData seniority = formBlock.getDataForPath("SENI/VALU") ;
		if (null == seniority)
			return -1 ;
		
		CoachingFitDelay seniorityAsDelay = new CoachingFitDelay(seniority.getValue()) ;
		
		return 12 * seniorityAsDelay.getYears() + seniorityAsDelay.getMonths() ;
	}
	
	@Override
	public Class<GetCoachingFitDashboardChartAction> getActionType() {
		return GetCoachingFitDashboardChartAction.class;
	}

	@Override
	public void rollback(GetCoachingFitDashboardChartAction action, GetCoachingFitDashboardChartResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
