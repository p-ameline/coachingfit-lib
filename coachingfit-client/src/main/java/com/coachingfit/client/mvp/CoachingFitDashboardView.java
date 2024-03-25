package com.coachingfit.client.mvp;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.quadrifolium.shared.model.QfTime;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.widgets.SelectCoachControl;
import com.coachingfit.client.widgets.SelectTraineeControl;
import com.coachingfit.client.widgets.SelectYearControl;
import com.coachingfit.client.widgets.SelectZoneControl;
import com.coachingfit.client.widgets.SelectZoneControl.ZoneData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;
import com.googlecode.gwt.charts.client.ChartType;
import com.googlecode.gwt.charts.client.ChartWrapper;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.ComboChartOptions;
import com.googlecode.gwt.charts.client.corechart.ComboChartSeries;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.SeriesType;
import com.googlecode.gwt.charts.client.options.VAxis;

import com.primege.client.loc.PrimegeViewConstants;
import com.primege.client.mvp.DashboardViewModel;
import com.primege.client.util.DashboardCol;
import com.primege.client.util.FormControl;
import com.primege.client.widgets.EventDateControl;
import com.primege.client.widgets.FormCheckBox;
import com.primege.shared.GlobalParameters;
import com.primege.shared.database.FormDataData;
import com.primege.shared.database.UserData;
import com.primege.shared.model.DashboardChart;
import com.primege.shared.model.DashboardTableCol;
import com.primege.shared.model.DashboardTableLine;

public class CoachingFitDashboardView extends DashboardViewModel implements CoachingFitDashboardPresenter.Display
{	
	private final PrimegeViewConstants constants = GWT.create(PrimegeViewConstants.class) ;

	@Inject
	public CoachingFitDashboardView(final CoachingFitSupervisor supervisor)
	{
		super(supervisor) ;

		CoachingFitUser user = supervisor.getCoachingFitUser() ;
		// if (user.hasRole(0, "CV"))
		//	_submitButton.setVisible(false) ;
	}

	/** 
	 * Insert a new control to the pivot pannel
	 * 
	 * @param sControlPath    control's path (arborescent identifier)
	 * @param sControlCaption control's caption
	 * @param sControlType    control's type (Edit, Buttons...)
	 */
	@Override
	public void insertNewPivotControl(final String sControlPath, final String sControlCaption, final String sControlType)
	{
		// Get the panel to add the control to
		// 
		if (null == _selectionPanel)
			return ;

		CoachingFitUser user = ((CoachingFitSupervisor) _supervisor).getCoachingFitUser() ;
		// if (user.hasRole(0, "CV"))
		//	return ;

		if ("SessionDate".equalsIgnoreCase(sControlType))
		{
			EventDateControl dateControl = new EventDateControl(null, sControlPath) ;
			dateControl.addStyleName("dashboardPivotControl") ;

			// Initialize with "now" or "yesterday" 
			//
			FormDataData fakeContent = new FormDataData() ;

			Date tNow = new Date() ;

			String sContentToInitialize = "" ;

			// Before 10 AM, we suppose that the event occurred the day before
			//
			if (tNow.getHours() < 10)
			{
				long lTime = tNow.getTime() ;
				long lM24H = 1000 * 60 * 60 * 24 ;
				long lYest = lTime - lM24H ;

				Date tYesterday = new Date(lYest) ;
				sContentToInitialize = GlobalParameters.getDateAsString(tYesterday) ;
			}
			else
				sContentToInitialize = GlobalParameters.getDateAsString(tNow) ;

			fakeContent.setValue(sContentToInitialize) ;
			dateControl.setContent(fakeContent, "") ;

			_aPivotControls.add(new FormControl(dateControl, null, sControlPath, "")) ;

			addPivotLabel(sControlCaption) ;
			_selectionPanel.add(dateControl) ;
		}
		else if ("user".equalsIgnoreCase(sControlType))
		{
			if (user.hasRole(0, "CV"))  // option not accessible to coaches
				return ;

			// Create an array of coaches sorted by last name
			//
			List<UserData> aCoaches = ((CoachingFitSupervisor) _supervisor).getCoachesArray() ;

			SelectCoachControl coachControl = new SelectCoachControl(aCoaches, null, ((CoachingFitSupervisor) _supervisor).getUser().getUserData(), sControlPath) ;
			coachControl.addStyleName("dashboardPivotControl") ;

			_aPivotControls.add(new FormControl(coachControl, null, sControlPath, "")) ;

			addPivotLabel(sControlCaption) ;
			_selectionPanel.add(coachControl) ;
		}
		else if ("zone".equalsIgnoreCase(sControlType))
		{
			if (user.hasRole(0, "CV"))  // option not accessible to coaches
				return ;

			List<ZoneData> aZones = new ArrayList<ZoneData>() ; 

			// If role is "A", then get all zones
			//
			if (user.hasRole(0, "A"))
			{
				aZones.add(new ZoneData("1")) ;
				aZones.add(new ZoneData("2")) ;
				aZones.add(new ZoneData("12")) ;
				aZones.add(new ZoneData("9")) ;
				aZones.add(new ZoneData("129")) ;
			}	
			else if (user.hasRole(0, "Z1"))
				aZones.add(new ZoneData("1")) ;
			else if (user.hasRole(0, "Z2"))
				aZones.add(new ZoneData("2")) ;

			SelectZoneControl zoneControl = new SelectZoneControl(aZones, sControlPath) ;
			zoneControl.addStyleName("dashboardPivotControl") ;

			_aPivotControls.add(new FormControl(zoneControl, null, sControlPath, "")) ;

			addPivotLabel(sControlCaption) ;
			_selectionPanel.add(zoneControl) ;
		}
		else if ("year".equalsIgnoreCase(sControlType))
		{
			SelectYearControl yearControl = new SelectYearControl(2017, sControlPath) ;
			yearControl.addStyleName("dashboardPivotControl") ;

			_aPivotControls.add(new FormControl(yearControl, null, sControlPath, "")) ;

			addPivotLabel(sControlCaption) ;
			_selectionPanel.add(yearControl) ;
		}
		else if ("checkBox".equalsIgnoreCase(sControlType))
		{
			FormCheckBox checkBox = new FormCheckBox(sControlCaption, sControlPath) ;
			checkBox.addStyleName("dashboardPivotControl") ;

			_aPivotControls.add(new FormControl(checkBox, null, sControlPath, "")) ;

			_selectionPanel.add(checkBox) ;
		}
	}

	/** 
	 * Insert a new control to the pivot pannel
	 * 
	 * @param sControlPath    control's path (arborescent identifier)
	 * @param sControlCaption control's caption
	 * @param sControlType    control's type (Edit, Buttons...)
	 */
	public void createLocalPivotControl(final String sControlPath, final String sControlType)
	{
		if ("trainee".equalsIgnoreCase(sControlType))
		{
			SelectTraineeControl traineeControl = new SelectTraineeControl(new ArrayList<TraineeData>(), sControlPath) ;
			traineeControl.addStyleName("dashboardLocalPivotControl") ;

			_aLocalPivotCtr.add(new FormControl(traineeControl, null, sControlPath, "")) ;
		}
	}

	/**
	 * Initialize local trainee selection controls when needed
	 */
	@Override
	public void updateLocalTraineePivotControl(final List<TraineeData> aTrainees)
	{
		// Sort the array
		//
		List<TraineeData> aSortedTrainees = CoachingFitSupervisor.getSortedTraineesArray(aTrainees) ;

		for (FormControl pivot : _aLocalPivotCtr)
		{
			SelectTraineeControl traineeControl = (SelectTraineeControl) pivot.getWidget() ;
			if (null != traineeControl)
				traineeControl.updateTrainees(aSortedTrainees) ;
		}
	}

	/** 
	 * Reset everything to display a new dashboard
	 * 
	 */
	@Override
	public void resetAll()
	{
		resetAll4Model() ;
	}

	/** 
	 * Fill a table from a set of lines
	 */
	@Override
	public void fillTable(final int iTableIndex, final List<DashboardTableLine> aLines, final DashboardTableLine titleLine)
	{
		if ((iTableIndex < 0) || (iTableIndex >= _aTables.size()))
			return ;

		FlexTable table = _aTables.get(iTableIndex) ;

		int iLineIndex = 0 ;

		// First, insert the columns titles line
		//
		if ((null != titleLine) && (false == titleLine.isEmpty()))
		{
			int iColIndex = 0 ;
			for (Iterator<String> itContent = titleLine.getContent().iterator() ; itContent.hasNext() ; )
			{
				table.setText(iLineIndex, iColIndex, itContent.next()) ;
				iColIndex++ ;
			}

			iLineIndex++ ;
		}

		// Then fill information lines
		//
		for (Iterator<DashboardTableLine> itLine = aLines.iterator() ; itLine.hasNext() ; )
		{
			DashboardTableLine line = itLine.next() ;

			int iColIndex = 0 ;
			for (Iterator<String> itContent = line.getContent().iterator() ; itContent.hasNext() ; )
			{
				table.setText(iLineIndex, iColIndex, itContent.next()) ;
				iColIndex++ ;
			}

			iLineIndex++ ;
		}
	}

	protected void displayColumHeader(DashboardCol newCol)
	{
		if (null == newCol)
			return ;

		String sToDisplay = "" ;

		// What must be displayed is a date
		//
		if ("$date$".equals(_colDesc.getDisplayedData()))
		{
			String sDate   = newCol.getDate() ;
			String sFormat = _colDesc.getHeadFormat() ;

			if ((null == sFormat) || "".equals(sFormat))
				sFormat = "DD/MM/YYYY" ;

			sFormat = sFormat.replace("YYYY", sDate.substring(0, 4)) ;
			sFormat = sFormat.replace("MM", sDate.substring(4, 6)) ;
			sFormat = sFormat.replace("DD", sDate.substring(6, 8)) ;

			sToDisplay = sFormat ;
		}

		_dashboardPannel.setWidget(0, newCol.getCol() + _iFirstDataCol - 1, new Label(sToDisplay)) ;
	}

	/** 
	 * Fill a chart from a set of lines
	 */
	@Override
	public void fillChart(final int iChartIndex, final DashboardChart dashboardChart, final List<DashboardTableLine> aLines, final DashboardTableLine titleLine)
	{
		if ((iChartIndex < 0) || (iChartIndex >= _aCharts.size()))
			return ;

		if ((null == dashboardChart) || (null == aLines) || aLines.isEmpty())
			return ;

		// Get cols
		//
		List<DashboardTableCol> aColumns = dashboardChart.getCols() ;

		ChartWrapper<ComboChartOptions> chartWrapper = _aCharts.get(iChartIndex) ;
		if (null == chartWrapper)
			return ;

		chartWrapper.setVisible(true) ;

		// Set options
		// 
		ComboChartOptions options = ComboChartOptions.create() ;
		options.setTitle("") ;
		options.setHAxis(HAxis.create("Date")) ;
		options.setVAxis(VAxis.create(dashboardChart.getYCaption())) ;

		// Series
		//
		int iTypesLen = dashboardChart.getTypes().size() ;

		int iIndex = 0 ;
		for (int i = 0 ; i < iTypesLen ; i++)
		{
			String sColType = dashboardChart.getTypeForIndex(i) ;

			if (false == "X".equalsIgnoreCase(sColType))
			{
				DashboardTableCol column = aColumns.get(i) ;

				if      ("bar".equalsIgnoreCase(sColType))
				{
					ComboChartSeries bars = ComboChartSeries.create() ;
					bars.setType(SeriesType.BARS) ;

					String sColor = column.getColBgColor() ;
					if (false == "".equals(sColor))
						bars.setColor(sColor) ;

					options.setSeries(iIndex++, bars) ;
				}
				else if ("line".equalsIgnoreCase(sColType))
				{
					ComboChartSeries line = ComboChartSeries.create() ;
					line.setType(SeriesType.LINE) ;
					options.setSeries(iIndex++, line) ;
				}
			}
		}

		if (iTypesLen > 2)
			chartWrapper.setChartType(ChartType.COMBO) ;

		chartWrapper.setOptions(options) ;

		// Fill columns
		//
		DataTable dataTable = DataTable.create() ;

		// Create columns
		//
		for (DashboardTableCol col : aColumns)
		{
			String sColType = col.getColType() ;

			ColumnType iType = ColumnType.NUMBER ;

			if      ("date".equalsIgnoreCase(sColType))
				iType = ColumnType.DATE ;
			else if ("datetime".equalsIgnoreCase(sColType))
				iType = ColumnType.DATETIME ;
			else if ("int".equalsIgnoreCase(sColType))
				iType = ColumnType.NUMBER ;
			else if ("double".equalsIgnoreCase(sColType))
				iType = ColumnType.NUMBER ;

			dataTable.addColumn(iType, col.getColCaption()) ;
		}

		// Fill chart
		//
		for (DashboardTableLine line : aLines)
		{
			String sFirstCol = line.get(0) ;

			DashboardTableCol col = aColumns.get(0) ;
			String sColType = col.getColType() ;

			Object[] rows = new Object[aColumns.size()] ;

			if      ("date".equalsIgnoreCase(sColType))
			{
				QfTime tValue = new QfTime(0) ;
				tValue.initFromUTCDate(sFirstCol) ;
				rows[0] = tValue.toJavaDate() ;
			}
			else
				rows[0] = sFirstCol ;

			for (int i = 1 ; i < line.getCellsCount() ; i++)
				rows[i] = getObject(line.getContent().get(i), aColumns.get(i).getColType()) ;

			dataTable.addRow(rows) ;
		}

		// Draw
		//
		// chartWrapper.setWidth("1000px") ;
		// chartWrapper.setHeight("200px") ;
		chartWrapper.setDataTable(dataTable) ;
		chartWrapper.draw() ;
	}

	/**
	 * Get a value as an Object depending on its type
	 */
	protected Object getObject(final String sValue, final String sType)
	{
		if ((null == sValue) || (null == sType) || "".equals(sType))
			return null ;

		Object YValue ;

		if      ("double".equalsIgnoreCase(sType))
		{
			try {
				YValue = Double.parseDouble(sValue) ;
			} catch (NumberFormatException e) {
				return (double) 0 ;
			}
		}
		else if ("int".equalsIgnoreCase(sType))
		{
			try {
				YValue = Integer.parseInt(sValue) ;
			} catch (NumberFormatException e) {
				return (int) 0 ;
			}
		}
		else if ("date".equalsIgnoreCase(sType))
		{
			QfTime tValue = new QfTime(0) ;
			tValue.initFromLocalDateTime(sValue) ;
			YValue = tValue.toJavaDate() ;
		}
		else
			YValue = sValue ;

		return YValue ;
	}

	@Override
	public Widget asWidget() {
		return this;
	}
}
