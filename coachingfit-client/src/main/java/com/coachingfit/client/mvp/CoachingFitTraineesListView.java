package com.coachingfit.client.mvp ;

import net.customware.gwt.presenter.client.widget.WidgetDisplay ;

import java.util.List ;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.client.widgets.CoachingFitFlexTable;
import com.coachingfit.client.widgets.SearchEditPanel;
import com.coachingfit.client.widgets.SelectCoachControl;
import com.coachingfit.client.widgets.SelectRegionControl;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc_util.TraineesSearchTrait;

import com.google.gwt.core.client.GWT ;
import com.google.gwt.event.dom.client.ClickEvent ;
import com.google.gwt.event.dom.client.HasClickHandlers ;
import com.google.gwt.user.client.ui.Button ;
import com.google.gwt.user.client.ui.Composite ;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel ;
import com.google.gwt.user.client.ui.HTML ;
import com.google.gwt.user.client.ui.HTMLPanel ;
import com.google.gwt.user.client.ui.HTMLTable.Cell ;
import com.google.gwt.user.client.ui.Label ;
import com.google.gwt.user.client.ui.Widget ;

import com.google.inject.Inject ;

/**
 * View component (MVP model) for the Patients list
 * 
 * @author Philippe
 *
 */
public class CoachingFitTraineesListView extends Composite implements CoachingFitTraineesListPresenter.Display
{
    private final CoachingFitConstants  constants      = GWT.create(CoachingFitConstants.class) ;
    private final FlowPanel             _workspace ;
    private       HTMLPanel             _h2 ;
    
    private       CoachingFitFlexTable  _listTable ;
    private       int                   _iTableColCount ;
    private       int                   _iSelectedRow ;
    
    // Search elements
    //
    private       FlowPanel             _TraitsPanel ;
    private final SearchEditPanel       _firstNameTrait ;
    private final SearchEditPanel       _lastNameTrait ;
    private       Button                _searchButton ;

    @Inject
    public CoachingFitTraineesListView(final CoachingFitSupervisor supervisor)
    {
        // Initialize panels structure
        //
        _workspace = new FlowPanel() ;
        {
            FlowPanel workspaceContainer = new FlowPanel() ;
            {
                _h2 = new HTMLPanel(constants.traineesListTitle()) ;
                _h2.setStyleName("h2");
                workspaceContainer.add(_h2) ;

                _TraitsPanel = new FlowPanel() ;
                _TraitsPanel.addStyleName("formsManagementPanel") ;
                {
                    _firstNameTrait = new SearchEditPanel("", constants.generalFirstName()) ;
                    _lastNameTrait  = new SearchEditPanel("", constants.generalLastName()) ;
                    
                    // Search controls table
                    //
                    FlexTable queryControls = new FlexTable() ;
                    queryControls.setWidget(0, 0, _firstNameTrait.getLabel()) ;
                    queryControls.setWidget(0, 1, _firstNameTrait.getTextBox()) ;
                    queryControls.setWidget(0, 2, _lastNameTrait.getLabel()) ;
                    queryControls.setWidget(0, 3, _lastNameTrait.getTextBox()) ;
                    
                    _TraitsPanel.add(queryControls) ;
                    
                    _searchButton = new Button(constants.generalSearch()) ;
                    _searchButton.addStyleName("button white") ;
                    
                    _TraitsPanel.add(_searchButton) ;
                    
                    workspaceContainer.add(_TraitsPanel) ;
                }
                
                FlexTable operationsTable = new FlexTable() ;
                {
                    _listTable = new CoachingFitFlexTable() ;
                    operationsTable.setWidget(0, 0, _listTable) ;
                    
                    FlowPanel buttonsView = new FlowPanel() ;
                    
                    operationsTable.setWidget(0, 1, buttonsView) ;
                    
                    workspaceContainer.add(operationsTable) ;
                }
            }
            _workspace.add(workspaceContainer) ;
        }

        initWidget(_workspace) ;
    }

    /**
     * Clear the table and install headers
     */
    @Override
    public void resetList()
    {
        _listTable.clear() ;
        _listTable.removeAllRows() ;

        _iTableColCount = 0 ;

        _listTable.setWidget(0, _iTableColCount++, new HTML(constants.generalLastName())) ;
        _listTable.setWidget(0, _iTableColCount++, new HTML(constants.generalJob())) ;
        _listTable.setWidget(0, _iTableColCount++, new HTML(constants.generalCoach())) ;
        _listTable.setWidget(0, _iTableColCount++, new HTML(constants.generalRegion())) ;
    }

    @Override
    public TraineesSearchTrait getTraits()
    {
        TraineesSearchTrait searchTraits = new TraineesSearchTrait() ;
        searchTraits.setFirstName(_firstNameTrait.getText()) ;
        searchTraits.setLastName(_lastNameTrait.getText()) ;
        
        return searchTraits ;
    }
    
    @Override
    public HasClickHandlers getSearchButton() {
        return _searchButton ;
    }

    @Override
    public HasClickHandlers getFlexTable() {
        return _listTable ;
    }

    @Override
    public HasClickHandlers getFlexTableClick() {
        return _listTable ;
    }

    /**
     * Get the column which header was clicked
     */
    @Override
    public int getClickedColumnHeader(ClickEvent event)
    {
        if (null == event)
            return -1 ;

        Cell clickedCell = _listTable.getCellForEvent(event) ;
        if (null == clickedCell)
            return -1 ;

        if (0 != clickedCell.getRowIndex())
            return -1 ;

        return clickedCell.getCellIndex() ;
    }

    /**
     * Get clicked row index (can be 0)
     */
    @Override
    public int getClickedRow(ClickEvent event)
    {
        if (null == event)
            return -1 ;

        Cell clickedCell = _listTable.getCellForEvent(event) ;
        if (null == clickedCell)
            return -1 ;

        int iIndex = clickedCell.getRowIndex() ;

        return iIndex ;
    }

    /**
     * Get double clicked row index (can be 0)
     */
    /*
    @Override
    public int getDoubleClickedRow(DoubleClickEvent event)
    {
        if (null == event)
            return -1 ;
    
        DoubleClickCell clickedCell = _ListTable.getCellForDoubleClickEvent(event) ;
        if (null == clickedCell)
            return -1 ;
    
        int iIndex = clickedCell.getRowIndex() ;
    
        return iIndex ;
    }
    */
    /**
     * Establish a row as newly selected row
     * 
     * @param iRow Newly selected row, or <code>-1</code> if there is no longer any selected row
     */
    @Override
    public void setSelectedRow(final int iRow)
    {
        int iPreviousSelectedRow = _iSelectedRow ;

        _iSelectedRow = iRow ;

        if (-1 != iPreviousSelectedRow)
            setStyleForRow(iPreviousSelectedRow) ;
        if (-1 != _iSelectedRow)
            setStyleForRow(_iSelectedRow) ;
    }

    /**
     * Set style for a given row (selected rows and even rows have specific style
     * 
     * @param iRow
     */
    protected void setStyleForRow(final int iRow)
    {
        // Paint selected row specifically
        //
        if (iRow == _iSelectedRow)
        {
            _listTable.getRowFormatter().addStyleName(iRow, "selected-row") ;
            return ;
        }

        _listTable.getRowFormatter().removeStyleName(iRow, "selected-row") ;

        // Paint even rows differently
        //
        // if (0 == iRow % 2)
        // _ListTable.getRowFormatter().addStyleName(iRow, "plEvenRow") ;

        if (iRow > 0)
            _listTable.getRowFormatter().setStyleName(iRow, "body-row") ;
    }

    @Override
    public void reloadTrainees(List<TraineeData> aTrainees)
    {
        resetList() ;
        
        if ((null == aTrainees) || aTrainees.isEmpty())
            return ;

        int iRow = 1;

        for (TraineeData trainee : aTrainees)
        {
            fillCell(iRow, 0, trainee.getLabel()) ;
            fillCell(iRow, 1, trainee.getJobType()) ;
            fillCell(iRow, 2, "" + trainee.getCoachId()) ;
            fillCell(iRow, 3, "" + trainee.getRegionId()) ;

            iRow++;
        }
    }

    private void fillCell(int iRow, int iCol, final String sInfo)
    {
        if ((null == sInfo) || sInfo.isEmpty())
            _listTable.setWidget(iRow, iCol, new HTML("&nbsp;"));
        else
            _listTable.setWidget(iRow, iCol, new Label(sInfo));
    }
    
    
    /**
     * Returns this widget as the {@link WidgetDisplay#asWidget()} value.
     */
    public Widget asWidget() {
        return this ;
    }
}
