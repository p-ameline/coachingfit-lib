package com.coachingfit.client.mvp ;

import net.customware.gwt.presenter.client.widget.WidgetDisplay ;

import java.util.ArrayList;
import java.util.List ;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.client.widgets.CoachingFitFlexTable;
import com.coachingfit.client.widgets.SearchEditPanel;
import com.coachingfit.client.widgets.SelectCoachControl;
import com.coachingfit.client.widgets.SelectRegionControl;
import com.coachingfit.shared.database.RegionData;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget ;

import com.google.inject.Inject ;
import com.primege.shared.database.UserData;

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

    private       List<UserData>        _aCoaches = new ArrayList<>() ;
    private       List<RegionData>      _aRegions = new ArrayList<>() ;
    
    private       CoachingFitFlexTable  _listTable ;
    private       int                   _iTableColCount ;
    private       int                   _iSelectedRow ;
    
    // Search elements
    //
    private       FlowPanel             _TraitsPanel ;
    private final SearchEditPanel       _firstNameTrait ;
    private final SearchEditPanel       _lastNameTrait ;
    private       Button                _searchButton ;

    // Edition elements
    //
    private       FlowPanel             _editionPanel ;
    private       TextBox               _nameBox ;
    private       TextBox               _firstNameBox ;
    private       TextBox               _emailBox ;
    private       SelectCoachControl    _coachList ;
    private       SelectRegionControl   _regionList ;
    private       ListBox               _jobsList ;
    
    private       Button                _cancelButton ;
    private       Button                _unactiveButton ;
    private       Button                _saveButton ;
    private       Button                _newButton ;
    
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
                _TraitsPanel.addStyleName("searchTraitsPanel") ;
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
                    
                    _searchButton = new Button(constants.generalSearch()) ;
                    _searchButton.addStyleName("button green") ;
                    
                    _newButton    = new Button(constants.editButtonNew()) ;
                    _newButton.addStyleName("button white") ;
                    
                    queryControls.setWidget(0, 4, _searchButton) ;
                    
                    _TraitsPanel.add(queryControls) ;
                    
                    workspaceContainer.add(_TraitsPanel) ;
                }
                
                FlexTable operationsTable = new FlexTable() ;
                operationsTable.addStyleName("searchResultPanel") ;
                {
                    _listTable = new CoachingFitFlexTable() ;
                    operationsTable.setWidget(0, 0, _listTable) ;
                    
                    FlowPanel buttonsView = new FlowPanel() ;
                    
                    operationsTable.setWidget(0, 1, buttonsView) ;
                    
                    workspaceContainer.add(operationsTable) ;
                }
                
                _editionPanel = new FlowPanel() ;
                _editionPanel.addStyleName("searchResultPanel") ;
                {
                    _nameBox      = new TextBox() ;
                    _firstNameBox = new TextBox() ;
                    _emailBox     = new TextBox() ;
                    _jobsList     = new ListBox() ;
                    _coachList    = new SelectCoachControl() ;
                    _regionList   = new SelectRegionControl() ;
                    
                    // Edition controls table
                    //
                    FlexTable editionControls = new FlexTable() ;
                    editionControls.setWidget(0, 0, new Label(constants.generalFirstName())) ;
                    editionControls.setWidget(0, 1, _firstNameBox) ;
                    editionControls.setWidget(1, 0, new Label(constants.generalLastName())) ;
                    editionControls.setWidget(1, 1, _nameBox) ;
                    editionControls.setWidget(2, 0, new Label(constants.generalMail())) ;
                    editionControls.setWidget(2, 1, _emailBox) ;
                    editionControls.setWidget(3, 0, new Label(constants.generalJob())) ;
                    editionControls.setWidget(3, 1, _jobsList) ;
                    editionControls.setWidget(4, 0, new Label(constants.generalCoach())) ;
                    editionControls.setWidget(4, 1, _coachList) ;
                    editionControls.setWidget(5, 0, new Label(constants.generalRegion())) ;
                    editionControls.setWidget(5, 1, _regionList) ;
                    
                    _editionPanel.add(editionControls) ;
                    
                    FlowPanel editButtonsPanel = new FlowPanel() ;
                    
                    _unactiveButton = new Button(constants.editButtonUnactive()) ;
                    _unactiveButton.addStyleName("button red") ;
                    
                    _saveButton     = new Button(constants.editButtonSave()) ;
                    _saveButton.addStyleName("button green") ;
                    
                    _cancelButton   = new Button(constants.editButtonCancel()) ;
                    _cancelButton.addStyleName("button orange") ;
                    
                    workspaceContainer.add(_editionPanel) ;
                    
                    _editionPanel.setVisible(false) ;
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
            
            if (trainee.getCoachId() == 0)
            	fillCell(iRow, 2, "inactif") ;
            else
            {
            	UserData coach = getCoachFromId(trainee.getCoachId()) ;
            	if (null == coach)
            		fillCell(iRow, 2, "?") ;
            	else
            		fillCell(iRow, 2, coach.getLabel()) ;
            }
            
            if (trainee.getRegionId() == 0)
            	fillCell(iRow, 3, "non attribué") ;
            else
            {
            	RegionData region = getRegionFromId(trainee.getRegionId()) ;
            	if (null == region)
            		fillCell(iRow, 3, "?") ;
            	else
            		fillCell(iRow, 3, region.getLabel()) ;
            }
            
            iRow++;
        }
    }

    public void reloadCoaches(List<UserData> aCoaches)
    {
        _aCoaches.clear() ;
        _aCoaches.addAll(aCoaches) ;
        
        _coachList.setParameters(aCoaches, null, null, "") ;
    }
    
    private UserData getCoachFromId(int iCoachId)
    {
        for (UserData coach : _aCoaches)
            if (coach.getId() == iCoachId)
            	return coach ;
        
        return null ;
    }
    
    public void reloadRegions(List<RegionData> aRegions)
    {
        _aRegions.clear() ;
        _aRegions.addAll(aRegions) ;
        
        _regionList.setParameters(aRegions, null, "") ;
    }
    
    private RegionData getRegionFromId(int iRegionId)
    {
        for (RegionData region : _aRegions)
            if (region.getId() == iRegionId)
            	return region ;
        
        return null ;
    }
    
    public void reloadJobs(List<String> aJobs)
    {
    	_jobsList.clear() ;
    	
    	if ((null == aJobs) || aJobs.isEmpty())
    		return ;
    	
    	for (String sJob : aJobs)
    		_jobsList.addItem(sJob) ;
    }
    
    private void fillCell(int iRow, int iCol, final String sInfo)
    {
        if ((null == sInfo) || sInfo.isEmpty())
            _listTable.setWidget(iRow, iCol, new HTML("&nbsp;"));
        else
            _listTable.setWidget(iRow, iCol, new Label(sInfo));
    }

    @Override
    public void showEditionPanel() {
    	_editionPanel.setVisible(true) ;
    }
    
    @Override
    public void hideEditionPanel() {
    	_editionPanel.setVisible(false) ;
    }
    
    @Override
    public void setLastName(final String sLastName) {
    	_nameBox.setText(sLastName) ;
    }
    
    @Override
    public void setFirstName(final String sFirstName) {
    	_firstNameBox.setText(sFirstName) ;
    }
    
    @Override
    public void setJob(final String sJob)
    {
        int iSize = _jobsList.getItemCount() ;
         ;
        for (int i = 0 ; i < iSize ; i++)
            if (_jobsList.getItemText(i).equals(sJob))
            	_jobsList.setItemSelected(i, true) ;
    }
    
    @Override
    public void setMail(final String sMail) {
    	_emailBox.setText(sMail) ;
    }
    
    @Override
    public void setCoach(final int iCoachId) {
    	_coachList.setSelectedCoach(iCoachId) ;
    }
    
    @Override
    public void setRegion(final int iRegionId) {
    	_regionList.setSelectedRegion(iRegionId) ;
    }
    
    @Override
    public String getLastName() {
    	return _nameBox.getText() ;
    }
    
    @Override
    public String getFirstName() {
    	return _firstNameBox.getText() ;
    }
    
    @Override
    public String getJob() {
    	return "" ;
    }
    
    @Override
    public String getMail() {
    	return _emailBox.getText() ;
    }
    
    @Override
    public int getCoach() {
    	return _coachList.getSelectedCoachId() ;
    }
    
    @Override
    public int getRegion() {
    	return _regionList.getSelectedRegionId() ;
    }

    public HasClickHandlers getUnactiveButton() {
    	return _unactiveButton ;
    }
    
    public HasClickHandlers getSaveButton() {
    	return _saveButton ;
    }
    
    public HasClickHandlers getCancelButton() {
    	return _cancelButton ;
    }
    
    public HasClickHandlers getNewButton() {
    	return _newButton ;
    }
    
    /**
     * Returns this widget as the {@link WidgetDisplay#asWidget()} value.
     */
    public Widget asWidget() {
        return this ;
    }
}
