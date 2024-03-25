package com.coachingfit.server.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.GetCoachingFitDashboardBlocksInBase;
import com.coachingfit.server.model.RegionDataManager;
import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.database.RegionData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachForDateBlock;
import com.coachingfit.shared.model.CoachingFitDashboardBlocks;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardTableAction;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardTableResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.model.UserManager;
import com.primege.shared.GlobalParameters;
import com.primege.shared.database.FormDataData;
import com.primege.shared.database.UserData;
import com.primege.shared.model.DashboardTable;
import com.primege.shared.model.DashboardTableCol;
import com.primege.shared.model.DashboardTableLine;
import com.primege.shared.model.DashboardTableLineModel;
import com.primege.shared.model.FormBlock;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/** 
 * Object in charge of getting all information to fill a given table 
 */
public class GetCoachingFitDashboardTableHandler implements ActionHandler<GetCoachingFitDashboardTableAction, GetCoachingFitDashboardTableResult> 
{	
	protected final Provider<ServletContext>     _servletContext ;
	protected final Provider<HttpServletRequest> _servletRequest ;
	
	protected List<DashboardTableLine>           _aLines ;
	
	protected DBConnector                        _dbConnector ;
	
	protected int                                _iUserId ;
	protected int                                _iPivotId ;
	protected String                             _sYear ;
	protected boolean                            _bOnlyAuthoredInfo ;
	protected DashboardTable                     _table ;
	
	/**
	 * Utility class that stores a seniority interval in the form <code>[min-max[</code>
	 */
	public class SeniorityInterval
	{
		protected int    _iMinSeniority ;  /** Minimum seniority in months, including this value */
		protected int    _iMaxSeniority ;  /** Maximum seniority in months, excluding this value */

		/**
		 * Void constructor
		 */
		public SeniorityInterval() {
			reinit() ;
		}
		
		/**
		 * Plain vanilla constructor
		 */
		public SeniorityInterval(final int iMinSeniority, final int iMaxSeniority)
		{
			_iMinSeniority = iMinSeniority ;
			_iMaxSeniority = iMaxSeniority ;
		}
		
		/**
		 * Copy constructor
		 */
		public SeniorityInterval(final SeniorityInterval model) {
			initFromOther(model) ;
		}
		
		public void reinit()
		{
			_iMinSeniority = 0 ;
			_iMaxSeniority = Integer.MAX_VALUE ;
		}
		
		public void initFromOther(final SeniorityInterval other)
		{
			reinit() ;
			
			if (null == other)
				return ;
			
			_iMinSeniority = other._iMinSeniority ;
			_iMaxSeniority = other._iMaxSeniority ;
		}

		/**
		 * Is the range [0 - Integer.MAX_VALUE]?
		 */
		public boolean isGlobal() {
			return ((0 == _iMinSeniority) && (Integer.MAX_VALUE == _iMaxSeniority)) ;
		}
		
		/**
		 * Is a seniority contained (or compatible with) this interval
		 * 
		 * @return <code>true</code> if interval is global or seniority within interval's <code>[min-max[</code>
		 */
		public boolean contains(final int iSeniority)
		{
			if (isGlobal())
				return true ;
			
			return ((iSeniority >= _iMinSeniority) && (iSeniority < _iMaxSeniority)) ;
		}
		
		public int getMinSeniority() {
			return _iMinSeniority ;
		}
		public void setMinSeniority(final int iMinSeniority) {
			_iMinSeniority = iMinSeniority ;
		}
		
		public int getMaxSeniority() {
			return _iMaxSeniority ;
		}
		public void setMaxSeniority(final int iMaxSeniority) {
			_iMaxSeniority = iMaxSeniority ;
		}
		
		/**
		 * Determine whether two PathAndSeniority are exactly similar
		 */
		public boolean equals(SeniorityInterval other)
		{
			if (this == other) {
				return true ;
			}
			if (null == other) {
				return false ;
			}
			
			return (_iMinSeniority == other._iMinSeniority) &&
						 (_iMaxSeniority == other._iMaxSeniority) ;
		}

		/**
		 * Determine whether this PathAndSeniority is exactly similar to another object
		 */
		public boolean equals(Object o) 
		{
			if (this == o) {
				return true ;
			}
			if (null == o || getClass() != o.getClass()) {
				return false;
			}

			final SeniorityInterval other = (SeniorityInterval) o ;

			return equals(other) ;
		}
	}
	
	/**
	 * Utility class that stores a local and a global counter for a given path and seniority interval
	 */
	public class ScoreCounter
	{
		protected String           _sPathRoot ;
		protected SeniorityInterval _seniorityInterval = new SeniorityInterval() ;
		
		protected double _dGlobalSum ;
		protected int    _iGlobalCount ;
		
		protected double _dCoachSum ;
		protected int    _iCoachCount ;
		
		protected double _dGlobalDeltaExpectedSum ;
		protected int    _iGlobalDeltaExpectedCount ;
		
		protected double _dCoachDeltaExpectedSum ;
		protected int    _iCoachDeltaExpectedCount ;
		
		public ScoreCounter(final String sPathRoot) {
			this(sPathRoot, 0, Integer.MAX_VALUE) ;
		}
		
		public ScoreCounter(final String sPathRoot, final SeniorityInterval seniorityInterval)
		{
			reinit() ;
			
			_sPathRoot = sPathRoot ;			
			_seniorityInterval.initFromOther(seniorityInterval) ;
		}
		
		public ScoreCounter(final String sPathRoot, final int iMinSeniority, final int iMaxSeniority)
		{
			reinit() ;
			
			_sPathRoot = sPathRoot ;
			
			_seniorityInterval.setMinSeniority(iMinSeniority) ;
			_seniorityInterval.setMaxSeniority(iMaxSeniority) ;
		}
		
		public void reinit()
		{
			_sPathRoot = "" ;
			_seniorityInterval.reinit() ;
			
			_dGlobalSum   = 0 ;
			_iGlobalCount = 0 ;
			
			_dCoachSum    = 0 ;
			_iCoachCount  = 0 ;
			
			_dGlobalDeltaExpectedSum   = 0 ;
			_iGlobalDeltaExpectedCount = 0 ; 
			
			_dCoachDeltaExpectedSum    = 0 ;
			_iCoachDeltaExpectedCount  = 0 ;
		}
		
		public void addToScore(final String sValue, final boolean wasDoneByCurrentCoach)
		{
			if ((null == sValue) || "".equals(sValue))
				return ;
			
			double dValue = 0 ;
			try {
				dValue = Double.parseDouble(sValue) ;
			} catch (NumberFormatException e) {
				return ;
			}
			
			_dGlobalSum += dValue ;
			_iGlobalCount++ ;
			
			if (wasDoneByCurrentCoach)
			{
				_dCoachSum += dValue ;
				_iCoachCount++ ;
			}
		}
		
		/**
		 * Add an (actual / expected) ratio to the counter 
		 */
		public void addToDeltaExpectedScore(final String sValue, final String sExpectedValue, final boolean wasDoneByCurrentCoach)
		{
			if ((null == sValue) || "".equals(sValue) || (null == sExpectedValue) || "".equals(sExpectedValue))
				return ;
			
			double dValue = 0 ;
			try {
				dValue = Double.parseDouble(sValue) ;
			} catch (NumberFormatException e) {
				return ;
			}
			
			double dExpectedValue = 0 ;
			try {
				dExpectedValue = Double.parseDouble(sExpectedValue) ;
			} catch (NumberFormatException e) {
				return ;
			}
			
			if (0 == dExpectedValue)
				return ;
			
			double dRatio = dValue / dExpectedValue ;  
			
			_dGlobalDeltaExpectedSum += dRatio ;
			_iGlobalDeltaExpectedCount++ ;
			
			if (wasDoneByCurrentCoach)
			{
				_dCoachDeltaExpectedSum += dRatio ;
				_iCoachDeltaExpectedCount++ ;
			}
		}
		
		/**
		 * Is this seniority contained (or compatible with) the seniority interval for this counter
		 */
		public boolean isValidForSeniority(int iSeniority) {
			return _seniorityInterval.contains(iSeniority) ;
		}
		
		public double getGlobalAverageScore() 
		{
			if (_iGlobalCount > 0)
				return _dGlobalSum / _iGlobalCount ;
			return 0 ;
		}
		
		public double getCoachAverageScore() 
		{
			if (_iCoachCount > 0)
				return _dCoachSum / _iCoachCount ;
			return 0 ;
		}
		
		public double getGlobalDeltaExpectedAverageScore() 
		{
			if (_iGlobalDeltaExpectedCount > 0)
				return _dGlobalDeltaExpectedSum / _iGlobalDeltaExpectedCount ;
			return 0 ;
		}
		
		public double getCoachDeltaExpectedAverageScore() 
		{
			if (_iCoachDeltaExpectedCount > 0)
				return _dCoachDeltaExpectedSum / _iCoachDeltaExpectedCount ;
			return 0 ;
		}
		
		public String getPath() {
			return _sPathRoot ;
		}
		public SeniorityInterval getSeniorityInterval() {
			return _seniorityInterval ;
		}
		public int getMinSeniority() {
			return _seniorityInterval.getMinSeniority() ;
		}
		public int getMaxSeniority() {
			return _seniorityInterval.getMaxSeniority() ;
		}
		
		/**
		 * Determine whether two ScoreCounter are exactly similar
		 */
		public boolean equals(ScoreCounter other)
		{
			if (this == other) {
				return true ;
			}
			if (null == other) {
				return false ;
			}
			
			return _seniorityInterval.equals(other._seniorityInterval) && 
					   GlobalParameters.areStringsEqual(_sPathRoot, other._sPathRoot) ;
		}

		/**
		 * Determine whether this ScoreCounter is exactly similar to another object
		 */
		public boolean equals(Object o) 
		{
			if (this == o) {
				return true ;
			}
			if (null == o || getClass() != o.getClass()) {
				return false;
			}

			final ScoreCounter other = (ScoreCounter) o ;

			return equals(other) ;
		}
	}
	
	protected List<ScoreCounter> _aScores ;
	
	protected String _sH1StartingDate ;
	protected String _sH1EndingDate ;
	protected String _sH2StartingDate ;
	protected String _sH2EndingDate ;
	
	protected String _sQ1StartingDate ;
	protected String _sQ1EndingDate ;
	protected String _sQ2StartingDate ;
	protected String _sQ2EndingDate ;
	protected String _sQ3StartingDate ;
	protected String _sQ3EndingDate ;
	
	protected String _sT1StartingDate ;
	protected String _sT1EndingDate ;
	protected String _sT2StartingDate ;
	protected String _sT2EndingDate ;
	protected String _sT3StartingDate ;
	protected String _sT3EndingDate ;
	protected String _sT4StartingDate ;
	protected String _sT4EndingDate ;
	
	@Inject
	public GetCoachingFitDashboardTableHandler(final Provider<ServletContext>     servletContext,
			                                       final Provider<HttpServletRequest> servletRequest)
	{
		_servletContext = servletContext ;
		_servletRequest = servletRequest ;
	}

	@Override
	public GetCoachingFitDashboardTableResult execute(GetCoachingFitDashboardTableAction action, ExecutionContext context) throws ActionException 
	{
		// Get context
		//
		_iUserId           = action.getUserId() ;
		_iPivotId          = action.getPivotId() ;
		_sYear             = action.getYear() ;
		_bOnlyAuthoredInfo = action.onlyAuthoredInfo() ;
		
		_table             = action.getTable() ;
		
		// Prepare result
		//
		GetCoachingFitDashboardTableResult result = new GetCoachingFitDashboardTableResult() ;
		result.setDashboardTable(_table) ; 
		
		_aLines = result.getLines() ;
		_aLines.clear() ; 
		
		// Get current year
		//
		if ("".equals(_sYear))
		{
			// No year and no pivot, return a void result
			//
			if (-1 == _iPivotId)
				return result ;
			
			String sNow  = getCurrentDateAndTime() ;
			_sYear = sNow.substring(0, 4) ;
		}
			
		String sStartingDate = _sYear + "0101" ;  // Starting 01/01/YYYY
		String sEndingDate   = _sYear + "1231" ;  // Ending   31/12/YYYY
		
		_sH1StartingDate = _sYear + "0101" ;  // S1 starting 01/01/YYYY
		_sH1EndingDate   = _sYear + "0630" ;  // S1 ending   30/06/YYYY
		_sH2StartingDate = _sYear + "0701" ;  // S2 starting 01/07/YYYY
		_sH2EndingDate   = _sYear + "1231" ;  // S2 ending   31/12/YYYY
		
		_sQ1StartingDate = _sYear + "0101" ;  // Q1 starting 01/01/YYYY
		_sQ1EndingDate   = _sYear + "0430" ;  // Q1 ending   30/04/YYYY
		_sQ2StartingDate = _sYear + "0501" ;  // Q2 starting 01/05/YYYY
		_sQ2EndingDate   = _sYear + "0831" ;  // Q2 ending   31/08/YYYY
		_sQ3StartingDate = _sYear + "0901" ;  // Q3 starting 01/09/YYYY
		_sQ3EndingDate   = _sYear + "1231" ;  // Q3 ending   31/12/YYYY
		
		_sT1StartingDate = _sYear + "0101" ;  // T1 starting 01/01/YYYY
		_sT1EndingDate   = _sYear + "0331" ;  // T1 ending   31/03/YYYY
		_sT2StartingDate = _sYear + "0401" ;  // T2 starting 01/04/YYYY
		_sT2EndingDate   = _sYear + "0630" ;  // T2 ending   30/06/YYYY
		_sT3StartingDate = _sYear + "0701" ;  // T3 starting 01/07/YYYY
		_sT3EndingDate   = _sYear + "0930" ;  // T3 ending   30/09/YYYY
		_sT4StartingDate = _sYear + "1001" ;  // T4 starting 01/10/YYYY
		_sT4EndingDate   = _sYear + "1231" ;  // T4 ending   31/12/YYYY
		
		_dbConnector = new DBConnector(false) ;
		
		GetCoachingFitDashboardBlocksInBase blocksLoader = new GetCoachingFitDashboardBlocksInBase(_iUserId, _dbConnector) ;
		CoachingFitDashboardBlocks          dashBlocks   = new CoachingFitDashboardBlocks() ; 
		
		// Are roots specified?
		//
		String[] aRoots = DashboardTable.getRoots(_table.getRoots()) ;
		
		// This means one trainee for each line
		//
		// In this case, the first column will contain trainees' Ids (in order to fill other columns more easily) then
		// it will be replaced by trainees' names
		//
		if ("$user$.$trainee$".equals(_table.getPivot()))
		{
			initTitleLine(result, 1) ;
			
			// Get all information in database for this coach during the time period
			//
			if (_bOnlyAuthoredInfo)
				blocksLoader.GetDashboardBlocks(_iPivotId, aRoots, -1, sStartingDate, sEndingDate, -1, dashBlocks) ;
			
			// Get all information in database for this coach's current trainees during the time period
			//
			else
			{
				TraineeDataManager traineesManager = new TraineeDataManager(_iUserId, _dbConnector) ;
				
				List<TraineeData> aTrainees = new ArrayList<TraineeData>() ; 
				traineesManager.fillTraineesForCoach(_iUserId, aTrainees, _iPivotId) ;
				
				if (aTrainees.isEmpty())
					return result ;
				
				int[] aTraineesIds = new int[aTrainees.size()] ;
				int iIndex = 0 ;
				for (TraineeData data : aTrainees)
				{
					aTraineesIds[iIndex] = data.getId() ;
					iIndex++ ;
				}
				
				blocksLoader.GetDashboardBlocks(-1, aRoots, aTraineesIds, sStartingDate, sEndingDate, -1, dashBlocks) ;
			}
			
			if (dashBlocks.isEmpty())
				return result ;

			createLinesForTrainees() ;
			fillTableForCoachBlock(dashBlocks) ;
		}
		// This means one region for each line
		//
		// In this case, the first column will contain regions' Ids (in order to fill other columns more easily) then
		// it will be replaced by regions' labels
		//
		else if ("$zone$.$region$".equals(_table.getPivot()))
		{
			initTitleLine(result, 1) ;
				
			// Get all regions (ie region + coach) for a given zone
			//
			List<RegionData> aRegions = new ArrayList<RegionData>() ;
			getRegionsForZone(aRegions, _iPivotId) ;
			
			if (aRegions.isEmpty())
				return result ;
			
			for (RegionData region : aRegions)
			{
				int iRegionId = region.getId() ;
			
				// Get all information in database for this region
				//
				blocksLoader.GetDashboardBlocks(-1, aRoots, -1, sStartingDate, sEndingDate, iRegionId, dashBlocks) ;
				
				fillTableForRegionBlock(dashBlocks, iRegionId) ;
				
				dashBlocks.reset() ;
			}
					
			// Replace regions' Ids by their names on column 0
			//
			replaceRegionsIdsByNames() ;
				
			if (_table.getTotalLine() != DashboardTable.TOTALTYPE.totalNone)
				addTotalLine(1) ;
		}
		//
		// No pivot, it means that all cells are described in the archetype
		//
		// In this case, the first column will contain the path (in order to fill other columns more easily) then
		// it will be replaced by lines labels
		//
		else if ((null == _table.getPivot()) || "".equals(_table.getPivot()))
		{
			initTitleLine(result, 1) ;
			
			// Create the lines
			//
			if (_table.getLines().isEmpty())
				return result ;
			
			List<DashboardTableCol> aCols = _table.getCols() ;
			
			int iColCount = aCols.size() + 1 ;  // remember that col 0 is for labels
			
			// Initialize counters and lines
			//
			_aScores = new ArrayList<ScoreCounter>() ;

			for (DashboardTableLineModel tableLineModel : _table.getLines())
			{
				DashboardTableLine tableLine = new DashboardTableLine(iColCount) ;
				
				String sLinePath = tableLineModel.getPath() ;
				
				tableLine.setContent(0, sLinePath) ;
				_aLines.add(tableLine) ;
				
				// A counter holds both local and global information for a given path and a given seniority interval.
				// Hence, when encountering "$user$.$mean100$" then "$global$.$mean100$" columns, a single counter gets created 
				//
				for (DashboardTableCol col : aCols)
				{
					SeniorityInterval interval = getSeniorityIntervalForCol(col.getColPath()) ;
					
					if (null != interval)
					{
						if (false == containsCounter(_aScores, sLinePath, interval))
							_aScores.add(new ScoreCounter(sLinePath, interval)) ;
					}
				}
			}
			
			// Get all information in database during current year
			//
			blocksLoader.GetDashboardBlocks(-1, aRoots, -1, sStartingDate, sEndingDate, -1, dashBlocks) ;
			
			if (dashBlocks.isEmpty())
			{
				replacePathsByLabels() ;
				return result ;
			}
			
			List<CoachForDateBlock> aBlocks = dashBlocks.getInformation() ;
			
			// Iterate on each block (all sessions for a given coach at a given date)
			//
			for (CoachForDateBlock block : aBlocks)
			{
				// Iterate on all sessions (for a single trainee)
				//
				List<FormBlock<FormDataData>> aSessions = block.getInformation() ;
				if ((null != aSessions) && (false == aSessions.isEmpty()))
				{
					for (FormBlock<FormDataData> formBlock : aSessions)
					{
						// Get information for a session
						//
						List<FormDataData> blockData = formBlock.getInformation() ;
						
						if (false == blockData.isEmpty())
						{
							int iSeniority = getSeniorityInForm(blockData) ;
							
							// Count individual 
							//
							for (FormDataData data : blockData)
							{
								String sDataPath = data.getPath() ;
								
								// When finding a count, look for expected count in order to compute the ratio (effective / expected)
								//
								if      (sDataPath.endsWith("SCOR"))
								{
									String sRootPath = sDataPath.substring(0, sDataPath.length() - 5) ;
									String sExpcPath = sRootPath + "/SCOE" ;
									
									// Looking for expected score for the same chapter
									//
									for (FormDataData dataBis : blockData)
									{
										if (sExpcPath.equals(dataBis.getPath()))
											addToDeltaExpectedScore(sRootPath, data.getValue(), dataBis.getValue(), iSeniority, _iPivotId == block.getCoachId()) ;
									}
								}
								else if (false == sDataPath.endsWith("SCOE"))
									addToScore(data, iSeniority, _iPivotId == block.getCoachId()) ;
							}
						}
					}
				}
			}
			
			fillLinesWithScores() ;
			
			// Replace trainees'Ids by their names on column 0
			//
			replacePathsByLabels() ;
			
			if (_table.getTotalLine() != DashboardTable.TOTALTYPE.totalNone)
				addTotalLine(1) ;
		}
		
		return result ;
	}
	
	/**
	 * Create a line for each trainee that belongs to this coach's team
	 */
	protected void createLinesForTrainees()
	{
		TraineeDataManager traineesManager = new TraineeDataManager(_iUserId, _dbConnector) ;
		
		List<TraineeData> aTrainees = new ArrayList<TraineeData>() ; 
		traineesManager.fillTraineesForCoach(_iUserId, aTrainees, _iPivotId) ;
		
		if (aTrainees.isEmpty())
			return ;
		
		for (TraineeData trainee : aTrainees)
			createNewLine(trainee.getId()) ;
	}
	
	/**
	 * Fill the table with all information from a block that contains sessions from a given coach
	 * 
	 * */
	protected void fillTableForCoachBlock(final CoachingFitDashboardBlocks dashBlocks)
	{
		if ((null == dashBlocks) || dashBlocks.isEmpty())
			return ;
		
		int iIndexForT1 = getIndexForPath("$NbSessionT1$") ;
		int iIndexForT2 = getIndexForPath("$NbSessionT2$") ;
		int iIndexForT3 = getIndexForPath("$NbSessionT3$") ;
		int iIndexForT4 = getIndexForPath("$NbSessionT4$") ;
		
		int iIndexForQ1 = getIndexForPath("$NbSessionQ1$") ;
		int iIndexForQ2 = getIndexForPath("$NbSessionQ2$") ;
		int iIndexForQ3 = getIndexForPath("$NbSessionQ3$") ;
		
		int iIndexForH1 = getIndexForPath("$NbSessionS1$") ;
		int iIndexForH2 = getIndexForPath("$NbSessionS2$") ;
		
		int iIndexForY  = getIndexForPath("$NbSessionYear$") ;
		
		int iIndexForS1 = getIndexForPath("$ScoreFirstSession$") ;
		int iIndexForSS = getIndexForPath("$ScoresOtherSessions$") ;
		int iIndexForSL = getIndexForPath("$ScoreLastSession$") ;
		int iIndexForDT = getIndexForPath("$DeltaScore$") ;
		
		boolean bScoreNeeded = (iIndexForS1 >= 0) || (iIndexForSS >= 0) || (iIndexForSL >= 0) || (iIndexForDT >= 0) ;
		
		List<CoachForDateBlock> aBlocks = dashBlocks.getInformation() ;
		
		// Iterate on each block (all sessions for a given coach at a given date)
		//
		for (CoachForDateBlock block : aBlocks)
		{
			// int    iCoachId     = block.getCoachId() ;
			String sSessionDate = block.getSessionDate() ;
			
			// Iterate on all sessions
			//
			List<FormBlock<FormDataData>> aSessions = block.getInformation() ;
			if ((null != aSessions) && (false == aSessions.isEmpty()))
			{
				for (FormBlock<FormDataData> formBlock : aSessions)
				{
					// Get the score, if needed
					//
					String sScore = "" ;
					
					if (bScoreNeeded)
					{
						double dScore = getScore(formBlock) ;
						if (dScore >= 0)
							sScore = getDoubleAsString(dScore) ;
					}
					
					// Get the table line for this trainee, or create a new one
					//
					CoachingFitFormData document = (CoachingFitFormData) formBlock.getDocumentLabel() ;
					
					int iTraineeId = document.getTraineeId() ;
					DashboardTableLine tableLine = getLineForPivot(iTraineeId) ;
					if (null == tableLine)
					{
						tableLine = createNewLine(iTraineeId) ;
						
						// The first score is the score that comes at line's creation (except if all lines were previously created)
						//
						addFirstScoreToLine(tableLine, sScore, iIndexForS1, iIndexForSS, iIndexForSL, iIndexForDT) ;
					}
					//
					// Here, since the line already exists, we know that the current score for this trainee is not the first one
					// (except if all lines were previously created, so better check)
					//
					else if (false == "".equals(sScore))
					{
						boolean bFirstAlreadyExists = true ;
					
						if (iIndexForS1 >= 0)
						{
							String sExistingFirst = tableLine.get(iIndexForS1 + 1) ;
							if ("".equals(sExistingFirst))
								bFirstAlreadyExists = false ;
						}
						
						if (bFirstAlreadyExists)
							addNthScoreToLine(tableLine, sScore, iIndexForS1, iIndexForSS, iIndexForSL, iIndexForDT) ;
						else
							addFirstScoreToLine(tableLine, sScore, iIndexForS1, iIndexForSS, iIndexForSL, iIndexForDT) ;
					}
					
					// Increment the proper column for time period (trimester, quarter or semester)  
					//
					if ((iIndexForH1 > 0) || (iIndexForH2 > 0))
						incrementSemesterCell(tableLine, sSessionDate, 1, iIndexForH1, iIndexForH2) ;
					if ((iIndexForQ1 > 0) || (iIndexForQ2 > 0) || (iIndexForQ3 > 0))
						incrementQuarterCell(tableLine, sSessionDate, 1, iIndexForQ1, iIndexForQ2, iIndexForQ3) ;
					if ((iIndexForT1 > 0) || (iIndexForT2 > 0) || (iIndexForT3 > 0) || (iIndexForT4 > 0))
						incrementTrimesterCell(tableLine, sSessionDate, 1, iIndexForT1, iIndexForT2, iIndexForT3, iIndexForT4) ;
					
					// Increment the yearly count column
					//
					if (iIndexForY >= 0)
						incrementCell(tableLine, iIndexForY + 1, 1) ;
				}
			}
		}
		
		// Replace trainees'Ids by their names on column 0
		//
		replaceTraineesIdsByNames() ;
		
		if (_table.getTotalLine() != DashboardTable.TOTALTYPE.totalNone)
			addTotalLine(1) ;
	}

	/**
	 * Fill the table with all information from a block that contains sessions from a given region
	 * 
	 * */
	protected void fillTableForRegionBlock(final CoachingFitDashboardBlocks dashBlocks, final int iRegionId)
	{
		if (null == dashBlocks)
			return ;
		
		// We create a line even if it remains empty
		//
		DashboardTableLine tableLine = getLineForPivot(iRegionId) ;
		if (null == tableLine)
			tableLine = createNewLine(iRegionId) ;
		
		if (dashBlocks.isEmpty())
			return ;
		
		int iIndexForT1 = getIndexForPath("$NbSessionT1$") ;
		int iIndexForT2 = getIndexForPath("$NbSessionT2$") ;
		int iIndexForT3 = getIndexForPath("$NbSessionT3$") ;
		int iIndexForT4 = getIndexForPath("$NbSessionT4$") ;
		
		int iIndexForQ1 = getIndexForPath("$NbSessionQ1$") ;
		int iIndexForQ2 = getIndexForPath("$NbSessionQ2$") ;
		int iIndexForQ3 = getIndexForPath("$NbSessionQ3$") ;
		
		int iIndexForH1 = getIndexForPath("$NbSessionS1$") ;
		int iIndexForH2 = getIndexForPath("$NbSessionS2$") ;
		
		int iIndexForY  = getIndexForPath("$NbSessionYear$") ;
		
		List<CoachForDateBlock> aBlocks = dashBlocks.getInformation() ;
		
		// Iterate on each block (all session at a given date)
		//
		for (CoachForDateBlock block : aBlocks)
		{
			// int    iCoachId     = block.getCoachId() ;
			String sSessionDate = block.getSessionDate() ;
			
			// Iterate on all sessions
			//
			List<FormBlock<FormDataData>> aSessions = block.getInformation() ;
			if ((null != aSessions) && (false == aSessions.isEmpty()))
			{
				for (FormBlock<FormDataData> formBlock : aSessions)
				{
					// Increment the proper quarter column 
					//
					if ((iIndexForH1 > 0) || (iIndexForH2 > 0))
						incrementSemesterCell(tableLine, sSessionDate, 1, iIndexForH1, iIndexForH2) ;
					if ((iIndexForQ1 > 0) || (iIndexForQ2 > 0) || (iIndexForQ3 > 0))
						incrementQuarterCell(tableLine, sSessionDate, 1, iIndexForQ1, iIndexForQ2, iIndexForQ3) ;
					if ((iIndexForT1 > 0) || (iIndexForT2 > 0) || (iIndexForT3 > 0) || (iIndexForT4 > 0))
						incrementTrimesterCell(tableLine, sSessionDate, 1, iIndexForT1, iIndexForT2, iIndexForT3, iIndexForT4) ;
					
					// Increment the yearly count column
					//
					if (iIndexForY >= 0)
						incrementCell(tableLine, iIndexForY + 1, 1) ;
				}
			}
		}
	}
	
	/**
	 * On column zero, replace trainees'Ids by the corresponding names
	 * 
	 * */
	protected void replaceTraineesIdsByNames()
	{
		if ((null == _aLines) || _aLines.isEmpty())
			return ;
		
		TraineeDataManager traineeManager = new TraineeDataManager(_iUserId, _dbConnector) ;
		
		for (DashboardTableLine line : _aLines)
		{
			try
			{
				int iTraineeId = Integer.parseInt(line.get(0)) ;
			
				TraineeData foundData = new TraineeData() ;
				if (traineeManager.existData(Integer.parseInt(line.get(0)), foundData))
					line.setContent(0, foundData.getLabel()) ;
			}
			catch (NumberFormatException e) {
				line.setContent(0, line.get(0) + " (?)") ;
			}
		}
	}
	
	/**
	 * On column zero, replace coaches'Ids by the corresponding names
	 * 
	 * */
	protected void replaceCoachesIdsByNames()
	{
		if ((null == _aLines) || _aLines.isEmpty())
			return ;
		
		UserManager userManager = new UserManager(_iUserId, _dbConnector) ;
		
		for (DashboardTableLine line : _aLines)
		{
			UserData foundData = new UserData() ;
			if (userManager.existUser(Integer.parseInt(line.get(0)), foundData))
				line.setContent(0, foundData.getLabel()) ;
		}
	}
	
	/**
	 * On column zero, replace regions'Ids by the corresponding names
	 * 
	 * */
	protected void replaceRegionsIdsByNames()
	{
		if ((null == _aLines) || _aLines.isEmpty())
			return ;
		
		RegionDataManager regionManager = new RegionDataManager(_iUserId, _dbConnector) ;
		
		for (DashboardTableLine line : _aLines)
		{
			RegionData foundData = new RegionData() ;
			if (regionManager.existData(Integer.parseInt(line.get(0)), foundData))
				line.setContent(0, foundData.getLabel()) ;
		}
	}
	
	/**
	 * Get the coaches that belong to a given abstract zone (abstract since zone 3 = zone 1 + zone 2)
	 * 
	 * @param aRegions List of regions to fill
	 * @param iZoneID  Zones identifier as an integer, ex 19 means zones 1 and 9
	 * 
	 * */
	protected void getRegionsForZone(List<RegionData> aRegions, int iZoneID)
	{
		if ((null == aRegions) || (iZoneID < 0))
			return ;
		
		String sZones = Integer.toString(iZoneID) ;
		
		RegionDataManager regionsManager = new RegionDataManager(_iUserId, _dbConnector) ;
		
		// Load regions for each zone
		//
		for (int i = 0 ; i < sZones.length() ; i++)
		{
			int iZoneId = Integer.parseInt(sZones.substring(i, i + 1)) ;
			regionsManager.fillRegionsForZone(aRegions, iZoneId) ;
		}
	}
	
	/**
	 * Find, if any, the line that belongs to a given pivot (i.e. whose first column contains this Id)
	 * 
	 * @param iPivotId Pivot id whose line is to be found
	 * @return the line if found, <code>null</code> if not
	 * 
	 * */
	protected DashboardTableLine getLineForPivot(final int iPivotId)
	{
		if ((null == _aLines) || _aLines.isEmpty())
			return null ;
		
		String sPivotId = Integer.toString(iPivotId) ;
		
		for (DashboardTableLine line : _aLines)
		{
			String sLeftContent = line.get(0) ;
			if (sPivotId.equals(sLeftContent))
				return line ;
		}
		
		return null ;
	}
	
	/**
	 * Increment the value of a semester for a given line
	 * 
	 */
	protected void incrementSemesterCell(DashboardTableLine tableLine, final String sSessionDate, int iValueToAdd, int iIndexForH1, int iIndexForH2)
	{
		if (null == tableLine)
			return ;
		
		// Find the proper quarter column 
		//
		int iColToIncrease = 0 ;
		if      ((sSessionDate.compareTo(_sH1StartingDate) >= 0) && (sSessionDate.compareTo(_sH1EndingDate) <= 0))
			iColToIncrease = iIndexForH1 + 1 ;
		else if ((sSessionDate.compareTo(_sH2StartingDate) >= 0) && (sSessionDate.compareTo(_sH2EndingDate) <= 0))
			iColToIncrease = iIndexForH2 + 1 ;
		
		if (iColToIncrease > 0)
			incrementCell(tableLine, iColToIncrease, iValueToAdd) ;
	}
	
	/**
	 * Increment the value of a quarter for a given line
	 * 
	 */
	protected void incrementQuarterCell(DashboardTableLine tableLine, final String sSessionDate, int iValueToAdd, int iIndexForQ1, int iIndexForQ2, int iIndexForQ3)
	{
		if (null == tableLine)
			return ;
		
		// Find the proper quarter column 
		//
		int iColToIncrease = 0 ;
		if      ((sSessionDate.compareTo(_sQ1StartingDate) >= 0) && (sSessionDate.compareTo(_sQ1EndingDate) <= 0))
			iColToIncrease = iIndexForQ1 + 1 ;
		else if ((sSessionDate.compareTo(_sQ2StartingDate) >= 0) && (sSessionDate.compareTo(_sQ2EndingDate) <= 0))
			iColToIncrease = iIndexForQ2 + 1 ;
		else if ((sSessionDate.compareTo(_sQ3StartingDate) >= 0) && (sSessionDate.compareTo(_sQ3EndingDate) <= 0))
			iColToIncrease = iIndexForQ3 + 1 ;
		
		if (iColToIncrease > 0)
			incrementCell(tableLine, iColToIncrease, iValueToAdd) ;
	}
	
	/**
	 * Increment the value of a trimester for a given line
	 * 
	 */
	protected void incrementTrimesterCell(DashboardTableLine tableLine, final String sSessionDate, int iValueToAdd, int iIndexForT1, int iIndexForT2, int iIndexForT3, int iIndexForT4)
	{
		if (null == tableLine)
			return ;
		
		// Find the proper quarter column 
		//
		int iColToIncrease = 0 ;
		if      ((sSessionDate.compareTo(_sT1StartingDate) >= 0) && (sSessionDate.compareTo(_sT1EndingDate) <= 0))
			iColToIncrease = iIndexForT1 + 1 ;
		else if ((sSessionDate.compareTo(_sT2StartingDate) >= 0) && (sSessionDate.compareTo(_sT2EndingDate) <= 0))
			iColToIncrease = iIndexForT2 + 1 ;
		else if ((sSessionDate.compareTo(_sT3StartingDate) >= 0) && (sSessionDate.compareTo(_sT3EndingDate) <= 0))
			iColToIncrease = iIndexForT3 + 1 ;
		else if ((sSessionDate.compareTo(_sT4StartingDate) >= 0) && (sSessionDate.compareTo(_sT4EndingDate) <= 0))
			iColToIncrease = iIndexForT4 + 1 ;
		
		if (iColToIncrease > 0)
			incrementCell(tableLine, iColToIncrease, iValueToAdd) ;
	}
	
	/**
	 * Increment the value of a cell inside a given line
	 * 
	 * */
	protected void incrementCell(DashboardTableLine tableLine, int iColToIncrease, int iValueToAdd)
	{
		if (null == tableLine)
			return ;
		
		String sPreviousValue = tableLine.get(iColToIncrease) ;
		
		if (null != tableLine)
			tableLine.setContent(iColToIncrease, addToContent(sPreviousValue, iValueToAdd)) ;
	}
	
	/**
	 * Get the String that contains sPreviousContent + iValueToAdd (for example "5" + 2 returns "7")
	 * 
	 * */
	protected String addToContent(String sPreviousContent, int iValueToAdd)
	{
		int iPreviousValue = 0 ;
		if ((null != sPreviousContent) && (false == "".equals(sPreviousContent)))
			iPreviousValue = Integer.parseInt(sPreviousContent) ;
		
		int iNewValue = iPreviousValue + iValueToAdd ;
		
		return Integer.toString(iNewValue) ;
	}
	
	/**
	 * Get column index for a given path
	 * 
	 * @return -1 if the path doesn't exist in the table, its column index if it exists (0 for left column)
	 * 
	 * */
	protected int getIndexForPath(final String sPath)
	{
		if ((null == _table) || (null == sPath) || "".equals(sPath))
			return -1 ;
		
		List<DashboardTableCol> cols = _table.getCols() ;
		if ((null == cols) || cols.isEmpty())
			return -1 ;
		
		int iIndex = 0 ;
		for (DashboardTableCol col : cols)
		{
			if (sPath.equalsIgnoreCase(col.getColPath()))
				return iIndex ;
			
			iIndex++ ;
		}
		
		return -1 ;
	}

	/**
	 * Initialize the title line as columns' captions (possibly shifted since the first column may contain trainees names)
	 * 
	 * @param result   result object which title line is to be initialized
	 * @param iColShif count of columns to be left empty to hold other information (for example trainees names)
	 * 
	 * */
	protected void initTitleLine(GetCoachingFitDashboardTableResult result, final int iColShift)
	{
		if (null == result)
			return ;
		
		// Create a line
		//
		int iColCount = _table.getCols().size() + iColShift ;		
		DashboardTableLine titleLine = new DashboardTableLine(iColCount) ;
		
		// Fill it
		//
		int iCol = 0 ;
		for (DashboardTableCol col : _table.getCols())
		{
			titleLine.setContent(iCol + iColShift, col.getColCaption()) ;
			iCol++ ;
		}
		
		// Insert it inside the result
		//
		result.setTitleLine(titleLine) ;
	}
	
	/**
	 * Add a line that contains the total of all columns
	 * 
	 * @param iColShif count of columns to be left empty to hold other information (for example trainees names)
	 * 
	 * */
	protected void addTotalLine(final int iColShift)
	{
		if ((null == _aLines) || _aLines.isEmpty())
			return ;
		
		int iColCount = _table.getCols().size() + iColShift ;
		DashboardTableLine totalLine = new DashboardTableLine(iColCount) ;
		
		if (iColShift > 0)
			totalLine.setContent(0, _table.getTotalCaption()) ;
		
		for (int iCol = iColShift ; iCol < iColCount ; iCol++)
		{
			int iSum   = 0 ;
			int iCount = 0 ;
			
			for (DashboardTableLine line : _aLines)
			{
				String sContent = line.get(iCol) ;
				if (false == "".equals(sContent))
					iSum += Integer.parseInt(sContent) ;
				iCount++ ;
			}
			
			if      (_table.getTotalLine() == DashboardTable.TOTALTYPE.totalSum)
				totalLine.setContent(iCol, Integer.toString(iSum)) ;
			else if (_table.getTotalLine() == DashboardTable.TOTALTYPE.totalMean)
				totalLine.setContent(iCol, Integer.toString(iSum / iCount)) ;
		}
		
		_aLines.add(totalLine) ;
	}
	
	/**
	 * Get the Coaching Fit score inside data
	 * 
	 * @param  formBlock block of information
	 * @return The score if found (as data which path is <code>"SFIT/SCOR"</code>) or <code>-1</code> if not 
	 * 
	 * */
	protected double getScore(final FormBlock<FormDataData> formBlock)
	{
		if (null == formBlock)
			return -1 ;
		
		List<FormDataData> information = formBlock.getInformation() ;
		if ((null == information) || information.isEmpty())
			return -1 ;
		
		for (FormDataData data : information)
			if (data.getPath().equals("SFIT/SCOR"))
				return Double.parseDouble(data.getValue()) ;
		
		return -1 ;
	}
	
	/**
	 * Increment all counters that correspond to a FormDataData for a given seniority 
	 * 
	 * @param data              FormDataData to process
	 * @param iSeniority        Trainee's seniority, <code>-1</code> if none
	 * @param bFromCurrentCoach <code>true</code> if the current coach is author of this information
	 */
	protected void addToDeltaExpectedScore(final String sRootPath, final String sValue, final String sExpectedValue, final int iSeniority, final boolean bFromCurrentCoach)
	{
		if ((null == sRootPath) || (null == sValue) || (null == sExpectedValue) || "".equals(sRootPath)) 
			return ;
		
		List<ScoreCounter> aCounters = getScoresForPath(sRootPath) ;
		if ((null == aCounters) || aCounters.isEmpty())
			return ;
		
		for (ScoreCounter counter : aCounters)
			if (counter.isValidForSeniority(iSeniority))
				counter.addToDeltaExpectedScore(sValue, sExpectedValue, bFromCurrentCoach) ;
	}
	
	/**
	 * Increment all counters that correspond to a FormDataData for a given seniority 
	 * 
	 * @param data              FormDataData to process
	 * @param iSeniority        Trainee's seniority, <code>-1</code> if none
	 * @param bFromCurrentCoach <code>true</code> if the current coach is author of this information
	 */
	protected void addToScore(final FormDataData data, final int iSeniority, final boolean bFromCurrentCoach)
	{
		if (null == data)
			return ;
		
		String sPathRoot = data.getPath().substring(0, 9) ;
		
		List<ScoreCounter> aCounters = getScoresForPath(sPathRoot) ;
		if ((null == aCounters) || aCounters.isEmpty())
			return ;
		
		for (ScoreCounter counter : aCounters)
			if (counter.isValidForSeniority(iSeniority))
				counter.addToScore(data.getValue(), bFromCurrentCoach) ;
	}
	
	/**
	 * Get all score counters attached to a sub-path
	 * 
	 * @param sPath path we are looking for
	 * 
	 * @return an array of ScoreCounter if found, <code>null</code> if something went wrong
	 * 
	 * */
	protected List<ScoreCounter> getScoresForPath(final String sPath)
	{
		if ((null == sPath) || "".equals(sPath) || (null == _aScores) || _aScores.isEmpty())
			return null ;
		
		List<ScoreCounter> aCounters = new ArrayList<ScoreCounter>() ;
		
		for (ScoreCounter counter : _aScores)
			if (sPath.equals(counter.getPath()))
				aCounters.add(counter) ;
		
		return aCounters ;
	}
	
	/**
	 * Fill table cells with scores
	 * 
	 * */
	protected void fillLinesWithScores()
	{
		if ((null == _aLines) || _aLines.isEmpty())
			return ;
		
		List<DashboardTableCol> aCols = _table.getCols() ;
		if (aCols.isEmpty())
			return ;
		
		// Fill the lines
		//
		for (DashboardTableLine line : _aLines)
		{
			String sLinePath = line.get(0) ;

			List<ScoreCounter> aCounters = getScoresForPath(sLinePath) ;
			if ((null != aCounters) && (false == aCounters.isEmpty()))
			{
				int iCol = 1 ;
				for (DashboardTableCol col : aCols)
				{
					SeniorityInterval seniorityInterval = getSeniorityIntervalForCol(col.getColPath()) ;
					
					ScoreCounter counter = getCounter(aCounters, sLinePath, seniorityInterval) ;
					if (null != counter)
					{
						int iScorePercent = -1 ;
					
						if      (col.getColPath().startsWith("$user$.$mean100$"))
							iScorePercent = (int) (counter.getCoachAverageScore() * 100) ;
						else if (col.getColPath().startsWith("$global$.$mean100$"))
							iScorePercent = (int) (counter.getGlobalAverageScore() * 100) ;
						else if (col.getColPath().startsWith("$user$.$delta100$"))
							iScorePercent = (int) (counter.getCoachDeltaExpectedAverageScore() * 100) ;
						else if (col.getColPath().startsWith("$global$.$delta100$"))
							iScorePercent = (int) (counter.getGlobalDeltaExpectedAverageScore() * 100) ;
						
						line.setContent(iCol, Integer.toString(iScorePercent)) ;
					}
					
					iCol++ ;
				}
			}
		}
	}
	
	/**
	 * Replace paths with labels on column zero
	 * 
	 * */
	protected void replacePathsByLabels()
	{
		if ((null == _aLines) || _aLines.isEmpty())
			return ;
		
		// Fill lines
		//
		for (DashboardTableLine line : _aLines)
		{
			String sLinePath = line.get(0) ;
			line.setContent(0, _table.getLineLabelForPath(sLinePath)) ;
		}
	}

	/**
	 * Create a new line dedicated to a pivot's information inside a table
	 * 
	 * */
	protected DashboardTableLine createNewLine(int iPivotID)
	{
		int iColCount = _table.getCols().size() + 1 ;  // remember that col 0 is for pivots' labels
		
		DashboardTableLine tableLine = new DashboardTableLine(iColCount) ;
		tableLine.setContent(0, Integer.toString(iPivotID)) ;
		
		_aLines.add(tableLine) ;
		
		return tableLine ;
	}
	
	/**
	 * Add a score to a line, when it is the first score in the line
	 * 
	 * @param tableLine the DashboardTableLine to put the score into
	 * @param sScore    the score
	 * @param iIndexForS1 index of column to store the initial score
	 * @param iIndexForSS index of column to store the other scores
	 * @param iIndexForSL index of column to store the last score
	 * @param iIndexForDT index of column to store the evolution last - initial
	 * 
	 * */
	protected void addFirstScoreToLine(final DashboardTableLine tableLine, final String sScore, int iIndexForS1, int iIndexForSS, int iIndexForSL, int iIndexForDT)
	{
		if ((null == tableLine) || (null == sScore) || "".equals(sScore))
			return ;
		
		// If there is something like a "first score" then we are finished
		//
		if (iIndexForS1 >= 0)
		{
			tableLine.setContent(iIndexForS1 + 1, sScore) ;
			return ;
		}
		
		addNthScoreToLine(tableLine, sScore, iIndexForS1, iIndexForSS, iIndexForSL, iIndexForDT) ;
	}
	
	/**
	 * Add a score to a line, when it is not the first score in the line
	 * 
	 * @param tableLine the DashboardTableLine to put the score into
	 * @param sScore    the score
	 * @param iIndexForS1 index of column to store the initial score
	 * @param iIndexForSS index of column to store the other scores
	 * @param iIndexForSL index of column to store the last score
	 * @param iIndexForDT index of column to store the evolution last - initial
	 */
	protected void addNthScoreToLine(final DashboardTableLine tableLine, final String sScore, int iIndexForS1, int iIndexForSS, int iIndexForSL, int iIndexForDT)
	{
		if ((null == tableLine) || (null == sScore) || "".equals(sScore))
			return ;
		
		// If there is something like a "last score"
		//
		if (iIndexForSL >= 0)
		{
			String sPreviousLast = tableLine.get(iIndexForSL + 1) ;
			
			// If last score was not empty, it is added to "others"
			//
			if ((false == "".equals(sPreviousLast)) && (iIndexForSS >= 0)) 
			{
				String sPreviousOther = tableLine.get(iIndexForSS + 1) ;
				if (false == "".equals(sPreviousOther))
					sPreviousOther += " - "  ;
				sPreviousOther += sScore ;
				
				tableLine.setContent(iIndexForSS + 1, sPreviousOther) ;
			}
				
			// Set current score as last score
			//
			tableLine.setContent(iIndexForSL + 1, sScore) ;
		}
		else if (iIndexForSS >= 0)
		{
			String sPreviousOther = tableLine.get(iIndexForSS + 1) ;
			if (false == "".equals(sPreviousOther))
				sPreviousOther += " - " ;
			sPreviousOther += sScore ;
		}
		
		if ((iIndexForDT >= 0) && (iIndexForS1 >= 0))
		{
			String sFirst = tableLine.get(iIndexForS1 + 1) ;
			
			double dFirstScore = 0 ;
			try {
				dFirstScore = Double.parseDouble(sFirst) ;
			} catch (NumberFormatException e) {
				dFirstScore = 0 ;
			} 
			
			double dLastScore = 0 ;
			try {
				dLastScore = Double.parseDouble(sScore) ;
			} catch (NumberFormatException e) {
				dLastScore = 0 ;
			}
			
			double dDelta = dLastScore - dFirstScore ;
			tableLine.setContent(iIndexForDT + 1, getDoubleAsString(dDelta)) ;
		}
	}
	
	/**
	 * Get a seniority interval (there 12 - 18) from a string of the form "$tag1$.$tagN$.$minSeniority12$.$maxSeniority18$"
	 */
	protected SeniorityInterval getSeniorityIntervalForCol(String sPath)
	{
		if ((null == sPath) || "".equals(sPath))
			return null ;
		
		int iMinSeniority = getValueinTag(sPath, "minSeniority") ;
		if (-1 == iMinSeniority)
			iMinSeniority = 0 ;
		
		int iMaxSeniority = getValueinTag(sPath, "maxSeniority") ;
		if (-1 == iMaxSeniority)
			iMaxSeniority = Integer.MAX_VALUE ;

		return new SeniorityInterval(iMinSeniority, iMaxSeniority) ;
	}
	
	/**
	 * Get an int value (here 18) for a tag (here "maxSeniority") from a string of the form "$tag1$.$tagN$.$maxSeniority18$"
	 * 
	 * @param sPath String to find tag and value into
	 * @param sTag  Tag the value is attached to
	 * @return The value or <code>-1</code> if something went wrong or the tag was not found
	 */
	protected int getValueinTag(final String sPath, final String sTag)
	{
		if ((null == sPath) || (null == sTag) || "".equals(sPath) || "".equals(sTag))
			return -1 ;
		
		// Can we find the tag?
		//
		int iStartIndex = sPath.indexOf("$" + sTag) ;
		if (-1 == iStartIndex)
			return -1 ;
		
		// Can we find the end of tag char
		//
		int iEndIndex = sPath.indexOf("$", iStartIndex + 1) ;
		if (-1 == iEndIndex)
			return -1 ;

		if (iStartIndex + sTag.length() + 1 >= iEndIndex)
			return -1 ;
		
		String sValue = sPath.substring(iStartIndex + sTag.length() + 1, iEndIndex) ;
		
		return Integer.parseInt(sValue) ;
	}
	
	/**
	 * Get seniority information in months from form's data
	 * 
	 * @return The seniority if found, <code>-1</code> if not
	 */
	protected int getSeniorityInForm(final List<FormDataData> blockData)
	{
		if ((null == blockData) || blockData.isEmpty())
			return -1 ;
		
		// Get seniority information
		//
		String sSeniority = "" ;
		
		for (FormDataData data : blockData)
			if ("SENI/VALU".equals(data.getPath()))
			{
				sSeniority = data.getValue() ;
				break ;
			}
		
		if (sSeniority.length() != 6)
			return -1 ;
				
		// Parse seniority information (YYMMDD)
		//
		return 12 * Integer.parseInt(sSeniority.substring(0, 2)) + Integer.parseInt(sSeniority.substring(2, 4)) ;
	}
	
	/**
	 * Does the array of counters contain a counter for a given seniority interval?
	 */
	protected boolean containsCounter(final List<ScoreCounter> aScores, final String sPath, final SeniorityInterval interval)
	{
		if ((null == aScores) || aScores.isEmpty() || (null == sPath) || (null == interval))
			return false ;
		
		for (ScoreCounter counter : aScores)
			if (interval.equals(counter.getSeniorityInterval()) && sPath.equals(counter.getPath()))
				return true ;
		
		return false ;
	}
	
	/**
	 * Get counter for path and seniority interval in array of counters
	 */
	protected ScoreCounter getCounter(final List<ScoreCounter> aScores, final String sPath, final SeniorityInterval interval)
	{
		if ((null == aScores) || aScores.isEmpty() || (null == sPath) || (null == interval))
			return null ;
		
		for (ScoreCounter counter : aScores)
			if (interval.equals(counter.getSeniorityInterval()) && sPath.equals(counter.getPath()))
				return counter ;
		
		return null ;
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
	
	@Override
	public Class<GetCoachingFitDashboardTableAction> getActionType() {
		return GetCoachingFitDashboardTableAction.class;
	}

	@Override
	public void rollback(GetCoachingFitDashboardTableAction action, GetCoachingFitDashboardTableResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
	}
}
