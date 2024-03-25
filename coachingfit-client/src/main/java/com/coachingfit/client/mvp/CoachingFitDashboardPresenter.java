package com.coachingfit.client.mvp;

import java.util.List;
import java.util.logging.Level ;
import java.util.logging.Logger ;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.widgets.SelectTraineeControl;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardChartAction;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardChartResult;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardTableAction;
import com.coachingfit.shared.rpc.GetCoachingFitDashboardTableResult;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesForCoachAction;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesForCoachResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import com.primege.client.mvp.DashboardInterfaceModel;
import com.primege.client.mvp.DashboardPresenterModel;
import com.primege.client.mvp.DashboardViewModel;
import com.primege.client.util.FormControl;
import com.primege.shared.model.DashboardChart;
import com.primege.shared.model.DashboardTable;
import com.primege.shared.model.DashboardTableLine;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

/**
 * Presenter from the presenter/view model for dashboards
 *
 */
public class CoachingFitDashboardPresenter extends DashboardPresenterModel<CoachingFitDashboardPresenter.Display>
{
	private Logger _logger = Logger.getLogger("") ;

	public interface Display extends DashboardInterfaceModel
	{
		public void resetAll() ;

		public void fillTable(final int iTableIndex, final List<DashboardTableLine> aLines, final DashboardTableLine titleLine) ;
		public void fillChart(final int iChartIndex, final DashboardChart dashboardChart, final List<DashboardTableLine> aLines, final DashboardTableLine titleLine) ;

		public void updateLocalTraineePivotControl(final List<TraineeData> aTrainees) ;

		// public void setEvent(EventData event) ;
		// public void setCities(List<CityData> aCities) ;

		// public void insertColumn(final CityForDateBlock blockForCol) ;
	}

	private int     _iTableIndex ;
	private int     _iChartIndex ;
	private int     _iPivotId ;
	private String  _sYear ;
	private boolean _bOnlyAuthoredInfo ;   // Only display information authored by selected author, or all sets for her trainees

	@Inject
	public CoachingFitDashboardPresenter(final Display               display, 
			                             final EventBus              eventBus,
			                             final DispatchAsync         dispatcher,
			                             final CoachingFitSupervisor supervisor) 
	{
		super(display, eventBus, dispatcher, supervisor) ;
	}

	protected void resetAll()
	{
		display.resetAll() ;

		resetAll4Model() ;

		// display.setEvent(_supervisor.getUser().getEventData()) ;
		// display.setCities(_supervisor.getUser().getCities()) ;

		_iTableIndex       = 0 ;
		_iPivotId          = -1 ;
		_sYear             = "" ;
		_bOnlyAuthoredInfo = true ;
	}

	/**
	 * Fill the dashboard with information from the database<br>
	 * Function called from DashboardPresenterModel when the "display" button is pressed
	 */
	protected void fillDashboard()
	{
		if ("".equals(getPivot()))
			return ;

		// Get pivots paths as an array
		//
		String[] aPivots = getPivot().split("\\|") ;

		for (int i = 0 ; i < aPivots.length ; i++)
		{
			String sPivot = aPivots[i] ; 

			// Pivot is the user
			//
			if ("$user$".equals(sPivot))
			{
				String sCoach = "" ;

				// Coach can either be static (for example user is the coach displaying her own information) or selected in a list
				//
				if ("".equals(getStaticPivotValue()))
					sCoach = display.getPivotInformation(sPivot) ;
				else
					sCoach = getStaticPivotValue() ;

				// If pivot is not yet set, it is attributed to the current user 
				//
				if (false == "".equals(sCoach))
					_iPivotId = Integer.parseInt(sCoach) ;
				else
				{
					CoachingFitUser user = ((CoachingFitSupervisor) _supervisor).getCoachingFitUser() ;
					_iPivotId = user.getUserData().getId() ;
				}
			}

			// Pivot is the year
			//
			if ("$year$".equals(sPivot))
			{
				_sYear = "" ;

				if ("".equals(getStaticPivotValue()))
					_sYear = display.getPivotInformation(sPivot) ;
				else
					_sYear = getStaticPivotValue() ;
			}

			// Pivot is the city
			//
			if ("$zone$".equals(sPivot))
			{
				String sZone = "" ;

				if ("".equals(getStaticPivotValue()))
					sZone = display.getPivotInformation(sPivot) ;
				else
					sZone = getStaticPivotValue() ;

				if (false == "".equals(sZone))
					_iPivotId = Integer.parseInt(sZone) ;
				else
					_iPivotId = 0 ;
			}

			// Pivot is the author status
			//
			if ("$authored$".equals(sPivot))
			{
				String sAuthored = "" ;

				if ("".equals(getStaticPivotValue()))
					sAuthored = display.getPivotInformation(sPivot) ;
				else
					sAuthored = getStaticPivotValue() ;

				// FormCheckBox' getContent() returns null when unchecked 
				//
				if (null == sAuthored)
					_bOnlyAuthoredInfo = false ;
				else
					_bOnlyAuthoredInfo = true ;
			}
		}

		askServerForTablesContent() ;
	}

	/**
	 * Ask the server for tables information
	 */
	protected void askServerForTablesContent()
	{
		if ((null == getTables()) || getTables().isEmpty())
		{
			askServerForChartsContent() ;
			return ;
		}

		_iTableIndex = 0 ;

		callNextTable() ;
	}

	/**
	 * Ask the server for information to fill a given table<br>
	 * <br>
	 * this function is recursive, next table information will be queried when the server provides information for current table 
	 */
	protected void callNextTable()
	{
		if (_iTableIndex >= getTables().size())
		{
			askServerForChartsContent() ;
			return ;
		}

		DashboardTable dashboardTable = getTables().get(_iTableIndex++) ;
		_dispatcher.execute(new GetCoachingFitDashboardTableAction(_supervisor.getUserId(), _iPivotId, _sYear, _bOnlyAuthoredInfo, dashboardTable), new getDashboardTableCallback()) ;
	}

	/**
	 * Callback called when the server provides information to fill current table 
	 */
	protected class getDashboardTableCallback implements AsyncCallback<GetCoachingFitDashboardTableResult> 
	{
		public getDashboardTableCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "getDashboardTableCallback: Unhandled error", cause) ;
		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitDashboardTableResult value) 
		{
			int iTableId = value.getDashboardTable().getId() ;

			List<DashboardTableLine> aLines = value.getLines() ;

			display.fillTable(iTableId, aLines, value.getTitleLine()) ;

			callNextTable() ;
		}
	}

	/**
	 * Ask the server for charts information
	 */
	protected void askServerForChartsContent()
	{
		if ((null == getCharts()) || getCharts().isEmpty())
		{
			finalizeDisplay() ;
			return ;
		}

		_iChartIndex = 0 ;

		callNextChart() ;
	}

	/**
	 * Ask the server for information to fill a given chart<br>
	 * <br>
	 * this function is recursive, next chart information will be queried when the server provides information for current chart 
	 */
	protected void callNextChart()
	{
		if (_iChartIndex >= getCharts().size())
		{
			finalizeDisplay() ;
			return ;
		}

		DashboardChart dashboardChart = getCharts().get(_iChartIndex++) ;

		// If chart's pivot is one of the global pivots, the chart can be drawn
		//
		String sChartPivot = dashboardChart.getPivot() ;
		if (belongsToPivots(sChartPivot))
			_dispatcher.execute(new GetCoachingFitDashboardChartAction(_supervisor.getUserId(), _iPivotId, -1, _sYear, _bOnlyAuthoredInfo, dashboardChart), new getDashboardChartCallback()) ;
		else
			callNextChart() ;
	}

	/**
	 * Callback called when the server provides information to fill current chart 
	 */
	protected class getDashboardChartCallback implements AsyncCallback<GetCoachingFitDashboardChartResult> 
	{
		public getDashboardChartCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "getDashboardChartCallback: Unhandled error", cause) ;
		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitDashboardChartResult value) 
		{
			int iTableId = value.getDashboardChart().getId() ;

			List<DashboardTableLine> aLines = value.getLines() ;

			display.fillChart(iTableId, getCharts().get(iTableId), aLines, value.getTitleLine()) ;

			callNextChart() ;
		}
	}

	/**
	 * Final step when tables and charts have been refreshed
	 */
	protected void finalizeDisplay()
	{
		if ("".equals(getPivot()))
			return ;

		// Get pivots paths as an array
		//
		String[] aPivots = getPivot().split("\\|") ;

		for (int i = 0 ; i < aPivots.length ; i++)
		{
			String sPivot = aPivots[i] ; 

			// Pivot is the user, means _iPivotId is the ID of user whose data are displayed
			//
			if ("$user$".equals(sPivot))
			{
				// Get trainees for this coach
				//
				_dispatcher.execute(new GetCoachingFitTraineesForCoachAction(_supervisor.getUserId(), _iPivotId), new getTraineesForCoachCallback()) ;
				return ;
			}
		}
	}

	/**
	 * Callback called when the server provides information about trainees for a coach 
	 */
	protected class getTraineesForCoachCallback implements AsyncCallback<GetCoachingFitTraineesForCoachResult> 
	{
		public getTraineesForCoachCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "getTraineesForCoachCallback: Unhandled error", cause) ;
		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitTraineesForCoachResult value) 
		{
			display.updateLocalTraineePivotControl(value.getTraineesData()) ;
		}
	}

	/**
	 * Chart with a given ID has to be refreshed because it's pivot information changed
	 */
	protected void refreshChart(int iChartId)
	{
		DashboardChart dashboardChart = getCharts().get(iChartId) ;

		String sLocalPivotPath = DashboardViewModel.getLocalPivotPath(dashboardChart.getPivot(), iChartId, false) ;
		FormControl localPivot = display.getLocalPivotControl(sLocalPivotPath) ;
		if (null == localPivot)
			return ;

		int iLocalPivotId = -1 ;

		SelectTraineeControl traineesControl = (SelectTraineeControl) localPivot.getWidget() ;
		if (null != traineesControl)
			iLocalPivotId = traineesControl.getSelectedTraineeId() ;

		if (-1 == iLocalPivotId)
			return ;

		_dispatcher.execute(new GetCoachingFitDashboardChartAction(_supervisor.getUserId(), iLocalPivotId, _iPivotId, _sYear, _bOnlyAuthoredInfo, dashboardChart), new updateChartCallback()) ;
	}

	/**
	 * Callback called when the server provides information to fill current chart 
	 */
	protected class updateChartCallback implements AsyncCallback<GetCoachingFitDashboardChartResult> 
	{
		public updateChartCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "getDashboardChartCallback: Unhandled error", cause) ;
		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitDashboardChartResult value) 
		{
			int iTableId = value.getDashboardChart().getId() ;

			List<DashboardTableLine> aLines = value.getLines() ;

			display.fillChart(iTableId, getCharts().get(iTableId), aLines, value.getTitleLine()) ;
		}
	}

	/**
	 * Is the candidate string one of the pivots?
	 */
	protected boolean belongsToPivots(final String sCandidate)
	{
		if ((null == sCandidate) || "".equals(sCandidate) || "".equals(getPivot()))
			return false ;

		String[] aPivots = getPivot().split("\\|") ;

		for (int i = 0 ; i < aPivots.length ; i++)
			if (sCandidate.equalsIgnoreCase(aPivots[i]))
				return true ;

		return false ;
	}
}
