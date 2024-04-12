package com.coachingfit.client.mvp;

//import com.google.gwt.core.client.GWT;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger ;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.client.widgets.SelectCoachControl;
import com.coachingfit.client.widgets.SelectTraineeControl;
import com.coachingfit.client.widgets.SessionDateControl;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.primege.client.loc.PrimegeViewConstants;
import com.primege.client.mvp.PrimegeBaseDisplay;
import com.primege.shared.database.UserData;

public class CoachingFitLoginResponseView extends PrimegeBaseDisplay implements CoachingFitLoginResponsePresenter.Display
{
    private final PrimegeViewConstants constants      = GWT.create(PrimegeViewConstants.class) ;
    private final CoachingFitConstants localConstants = GWT.create(CoachingFitConstants.class) ;

    private FlowPanel            _workspace ;
    private FlowPanel            _newFormsButtonsPanel ;

    private List<Button>         _aNewFormsButtons       = new ArrayList<Button>() ;
    private List<Button>         _aOpenDashboardButtons  = new ArrayList<Button>() ;
    private List<Button>         _aRequestCsvButtons     = new ArrayList<Button>() ;
    private List<Button>         _aOpenStaticHtmlButtons = new ArrayList<Button>() ;

    private Button               _EditFormButton ;
    private Button               _DeleteFormButton ;
    private Button               _EditUserInformationButton ;
    private Button               _GotoAdminPageButton ;

    private SessionDateControl   _MgtFromDate ;
    private SessionDateControl   _MgtToDate ;
    private Label                _CoachLabel ;
    private SelectCoachControl   _MgtCoach ;
    private SelectTraineeControl _MgtTrainee ;

    private ListBox              _FormsListBox ;

    private Button               _FormsSearchButton ;

    private DialogBox            _WarnindDialogBox ;
    private Label                _WarnindDialogBoxLabel ;
    private Button               _WarningDialogBoxOkButton ;

    private DialogBox            _DeleteDialogBox ;
    private Label                _DeleteDialogBoxLabel ;
    private Button               _DeleteDialogBoxOkButton ;
    private Button               _DeleteDialogBoxCancelButton ;

    private final CoachingFitSupervisor _supervisor ;
    private       Logger                _logger = Logger.getLogger("") ;

    @Inject
    public CoachingFitLoginResponseView(final CoachingFitSupervisor supervisor) 
    {
        //super();

        _logger.info("Constructor CoachingFitLoginResponseView") ;

        _supervisor = supervisor ;

        _logger.info("Constructor CoachingFitLoginResponseView, before initWorkspace") ;

        initWorkspace() ;
    }

    public void initWorkspace() 
    {					
        _EditUserInformationButton = null ;

        _GotoAdminPageButton = new Button(constants.administration()) ;
        _GotoAdminPageButton.getElement().setAttribute("id", "build_csv-id") ;
        _GotoAdminPageButton.addStyleName("button white buildCSVButton") ;

        _workspace = new FlowPanel() ;
        _workspace.addStyleName("mapworkspace") ;

        // Insert new forms buttons
        //
        _newFormsButtonsPanel = new FlowPanel() ;

        if (false == _aNewFormsButtons.isEmpty())
            for (Button button : _aNewFormsButtons)
                _newFormsButtonsPanel.add(button) ;

        _workspace.add(_newFormsButtonsPanel) ;

        installFormsManagementControls() ;

        // _workspace.add(_EditUserInformationButton) ;
        _workspace.add(_GotoAdminPageButton) ;
        _GotoAdminPageButton.setVisible(false) ;

        initWarningDialogBox() ;
        initDeleteDialogBox() ;

        initWidget(_workspace) ;
    }

    private void installFormsManagementControls()
    {
        FlowPanel formsMgtPannel = new FlowPanel() ;		
        formsMgtPannel.addStyleName("formsManagementPanel") ;

        // Date from and date to
        //
        _MgtFromDate = new SessionDateControl("") ;
        _MgtFromDate.addStyleName("formsManagementDate") ;
        _MgtToDate   = new SessionDateControl("") ;
        _MgtToDate.addStyleName("formsManagementDate") ;

        Label formsFromLabel = new Label(constants.formsFromDay()) ;
        formsFromLabel.addStyleName("formsManagementLabel") ;
        Label formsToLabel   = new Label(constants.formsToDay()) ;
        formsToLabel.addStyleName("formsManagementLabel") ;

        // Trainees
        //
        Label traineeLabel = new Label(localConstants.formsTrainees()) ;
        traineeLabel.addStyleName("formsManagementLabel") ;

        // Sort the array
        //
        List<TraineeData> aSortedTrainees = CoachingFitSupervisor.getSortedTraineesArray(((CoachingFitUser) _supervisor.getUser()).getTrainees()) ;

        _MgtTrainee = new SelectTraineeControl(aSortedTrainees, "", false, false, false) ;

        // Coaches
        //
        _CoachLabel = new Label(localConstants.formsAuthor()) ;
        _CoachLabel.addStyleName("formsManagementLabel") ;

        // Create an array of coaches sorted by last name
        //
        List<UserData>    aCoaches          = ((CoachingFitSupervisor) _supervisor).getCoachesArray() ;
        List<TraineeData> aCoachingTrainees = ((CoachingFitSupervisor) _supervisor).getCoachingTraineesForUser() ;

        _MgtCoach = new SelectCoachControl(aCoaches, aCoachingTrainees, ((CoachingFitSupervisor) _supervisor).getUser().getUserData(), "") ;

        // Controls table
        //
        FlexTable queryControls = new FlexTable() ;
        queryControls.setWidget(0, 0, formsFromLabel) ;
        queryControls.setWidget(0, 1, _MgtFromDate) ;
        queryControls.setWidget(0, 2, _CoachLabel) ;
        queryControls.setWidget(0, 3, _MgtCoach) ;
        queryControls.setWidget(1, 0, formsToLabel) ;
        queryControls.setWidget(1, 1, _MgtToDate) ;
        queryControls.setWidget(1, 2, traineeLabel) ;
        queryControls.setWidget(1, 3, _MgtTrainee) ;

        formsMgtPannel.add(queryControls) ;

        _FormsSearchButton = new Button(constants.formsSearch()) ;
        _FormsSearchButton.getElement().setAttribute("id", "form_search-id") ;
        _FormsSearchButton.addStyleName("button red formsSearchButton") ;
        formsMgtPannel.add(_FormsSearchButton) ;

        _FormsListBox = new ListBox() ;
        _FormsListBox.setMultipleSelect(true) ;
        _FormsListBox.addStyleName("formsManagementList") ;
        _FormsListBox.setVisibleItemCount(10) ;

        formsMgtPannel.add(_FormsListBox) ;

        _EditFormButton = new Button(constants.formEdit()) ;
        _EditFormButton.getElement().setAttribute("id", "form_edit-id") ;
        _EditFormButton.addStyleName("button red formEditButton") ;
        formsMgtPannel.add(_EditFormButton) ;

        _DeleteFormButton = new Button(constants.formDelete()) ;
        _DeleteFormButton.getElement().setAttribute("id", "form_delete-id") ;
        _DeleteFormButton.addStyleName("button red formEditButton") ;
        formsMgtPannel.add(_DeleteFormButton) ;

        _workspace.add(formsMgtPannel) ; 
    }

    /** 
     * initWarningDialogBox - Initialize warning dialog box
     * 
     * @param    nothing
     * @return   nothing  
     */
    private void initWarningDialogBox()
    {
        _WarnindDialogBox = new DialogBox() ;
        _WarnindDialogBox.setPopupPosition(100, 200) ;
        _WarnindDialogBox.setText(constants.warning()) ;
        _WarnindDialogBox.setAnimationEnabled(true) ;

        _WarnindDialogBoxLabel = new Label("") ;
        _WarnindDialogBoxLabel.addStyleName("warningDialogLabel") ;

        _WarningDialogBoxOkButton = new Button(constants.generalOk()) ;
        _WarningDialogBoxOkButton.setSize("70px", "30px") ;
        _WarningDialogBoxOkButton.getElement().setId("okbutton") ;

        FlowPanel warningPannel = new FlowPanel() ;
        warningPannel.add(_WarnindDialogBoxLabel) ;
        warningPannel.add(_WarningDialogBoxOkButton) ;

        _WarnindDialogBox.add(warningPannel) ;
    }

    /** 
     * initDeleteDialogBox - Initialize delete dialog box
     * 
     * @param    nothing
     * @return   nothing  
     */
    private void initDeleteDialogBox()
    {
        _DeleteDialogBox = new DialogBox() ;
        _DeleteDialogBox.setPopupPosition(100, 200) ;
        _DeleteDialogBox.setText(constants.warning()) ;
        _DeleteDialogBox.setAnimationEnabled(true) ;

        _DeleteDialogBoxLabel = new Label(constants.confirmDeleteForm()) ;
        _DeleteDialogBoxLabel.addStyleName("warningDialogLabel") ;

        _DeleteDialogBoxOkButton = new Button(constants.generalOk()) ;
        _DeleteDialogBoxOkButton.setSize("70px", "30px") ;
        _DeleteDialogBoxOkButton.getElement().setId("deleteokbutton") ;

        _DeleteDialogBoxCancelButton = new Button(constants.generalCancel()) ;
        _DeleteDialogBoxCancelButton.setSize("70px", "30px") ;
        _DeleteDialogBoxCancelButton.getElement().setId("deletecancelbutton") ;

        FlowPanel deletePannel = new FlowPanel() ;
        deletePannel.add(_DeleteDialogBoxLabel) ;
        deletePannel.add(_DeleteDialogBoxOkButton) ;
        deletePannel.add(_DeleteDialogBoxCancelButton) ;

        _DeleteDialogBox.add(deletePannel) ;
    }

    @Override
    public void setFormDates(Date tDate)
    {
        _MgtFromDate.initFromDate(tDate, true) ;
        include2017inFrom() ;

        _MgtToDate.initFromDate(tDate, true) ;
        include2017inTo() ;
    }

    @Override
    public void setFormDateFrom(Date tDate)
    {
        _MgtFromDate.initFromDate(tDate, true) ;
        include2017inFrom() ;
    }

    /**
     * Include the starting date in the "from" year list
     */
    @SuppressWarnings("deprecation")
    protected void include2017inFrom()
    {
        // Make certain that 2017, date of start, is in the list
        //
        Date tNow = new Date() ;
        int iYearNow = tNow.getYear() + 1900 ;

        _MgtFromDate.initYearListBox(2017, iYearNow + 3, iYearNow) ;
    }

    /**
     * Include the starting date in the "from" year list
     */
    @SuppressWarnings("deprecation")
    protected void include2017inTo()
    {
        // Make certain that 2017, date of start, is in the list
        //
        Date tNow = new Date() ;
        int iYearNow = tNow.getYear() + 1900 ;

        _MgtToDate.initYearListBox(2017, iYearNow + 3, iYearNow) ;
    }

    @Override
    public void setFormDateTo(Date tDate)
    {
        _MgtToDate.initFromDate(tDate, true) ;
        include2017inTo() ;
    }

    /** 
     * Get the form Id of the selected form in forms list
     * 
     * @return The selected form's Id if a form is selected, -1 if not  
     */
    @Override
    public int getSelectedForm()
    {
        int iSelectedItem = _FormsListBox.getSelectedIndex() ;
        if (-1 == iSelectedItem)
            return -1 ;

        String sIndexValue = _FormsListBox.getValue(iSelectedItem) ;

        return Integer.parseInt(sIndexValue) ;
    }

    /** 
     * popupWarningMessage - Display warning dialog box
     * 
     * @param    nothing
     * @return   nothing  
     */
    @Override
    public void popupWarningMessage(String sMessage)
    {
        if      (sMessage.equals("ERROR_MUST_SELECT_ENCOUNTER"))
            _WarnindDialogBoxLabel.setText(constants.warningAlreadyExist()) ;
        else if (sMessage.equals("ERROR_MUST_ENTER_EVERY_INFORMATION"))
            _WarnindDialogBoxLabel.setText(constants.mandatoryEnterAll()) ;

        _WarnindDialogBox.show() ;
    }

    @Override
    public void popupMessage(String sMessage)
    {
        _WarnindDialogBoxLabel.setText(sMessage) ;		
        _WarnindDialogBox.show() ;
    }

    @Override
    public void closeWarningDialog() {
        _WarnindDialogBox.hide() ;
    }

    @Override
    public void popupDeleteMessage() {
        _DeleteDialogBox.show() ;
    }

    @Override
    public void closeDeleteDialog() {
        _DeleteDialogBox.hide() ;
    }

    @Override
    public HasClickHandlers getDeleteOk() {
        return _DeleteDialogBoxOkButton ;
    }

    @Override
    public HasClickHandlers getDeleteCancel() {
        return _DeleteDialogBoxCancelButton ;
    }

    public void reset() {
    }

    public Widget asWidget() {
        return this;
    }

    public FlowPanel getWorkspace() {
        return _workspace ;
    }

    @Override
    public HasClickHandlers getEditUserData() {
        return _EditUserInformationButton ;
    }

    /*
	@Override
	public HasClickHandlers getBuildCsv() {
		return _BuidCSVButton ;
	}

	@Override
	public void hideBuildCsvButton() {
		_BuidCSVButton.setVisible(false) ;
	}
     */

    @Override
    public HasClickHandlers getFormsSearchButton() {
        return _FormsSearchButton ;
    }

    @Override
    public HasClickHandlers getEditFormButton() {
        return _EditFormButton ;
    }

    @Override
    public HasClickHandlers getDeleteFormButton() {
        return _DeleteFormButton ;
    }

    @Override
    public void activateEditFormButton(boolean bActivate) {
        _EditFormButton.setEnabled(bActivate) ;
    }

    @Override
    public void activateDeleteFormButton(boolean bActivate) {
        _DeleteFormButton.setEnabled(bActivate) ;
    }

    @Override
    public String getFormDateFrom() {
        return _MgtFromDate.getContentAsString() ;
    }

    @Override
    public String getFormDateTo() {
        return _MgtToDate.getContentAsString() ;
    }

    @Override
    public void clearFormsList() {
        _FormsListBox.clear() ;
    }

    @Override
    public void addForm(String sFormName, int iMessageId) {
        _FormsListBox.addItem(sFormName, Integer.toString(iMessageId)) ;
    }

    /**
     * Add a button to open a new form
     */
    @Override
    public void addNewFormButton(String sCaption, ClickHandler handler, int iArchetypeId) 
    {
        Button newFormButton = new Button(sCaption, handler) ;
        newFormButton.getElement().setAttribute("id", "new_form-id" + iArchetypeId) ;
        newFormButton.addStyleName("button red newFormButton") ;

        _aNewFormsButtons.add(newFormButton) ;
        // _newFormsButtonsPanel.add(newFormButton) ;
    }

    /**
     * Add a button to open a dashboard
     */
    @Override
    public void addOpenDashboardButton(String sCaption, ClickHandler handler, int iArchetypeId) 
    {
        Button newFormButton = new Button(sCaption, handler) ;
        newFormButton.getElement().setAttribute("id", "open_dash-id" + iArchetypeId) ;
        newFormButton.addStyleName("button orange newFormButton") ;

        _aOpenDashboardButtons.add(newFormButton) ;
        // _newFormsButtonsPanel.add(newFormButton) ;
    }

    /**
     * Add a button to request a CSV file
     */
    @Override
    public void addRequestCsvButton(String sCaption, ClickHandler handler, int iArchetypeId) 
    {
        Button newFormButton = new Button(sCaption, handler) ;
        newFormButton.getElement().setAttribute("id", "request_csv-id" + iArchetypeId) ;
        newFormButton.addStyleName("button white newFormButton") ;

        _aRequestCsvButtons.add(newFormButton) ;
        // _newFormsButtonsPanel.add(newFormButton) ;
    }

    /**
     * Add a static Html button
     */
    @Override
    public void addOpenStaticHtmlButton(String sCaption, ClickHandler handler, int iArchetypeId) 
    {
        Button newFormButton = new Button(sCaption, handler) ;
        newFormButton.getElement().setAttribute("id", "static_html-id" + iArchetypeId) ;
        newFormButton.addStyleName("button blue newFormButton") ;

        _aOpenStaticHtmlButtons.add(newFormButton) ;
        // _newFormsButtonsPanel.add(newFormButton) ;
    }

    /**
     * Show all buttons, category by category
     */
    @Override
    public void showButtons()
    {
        if (false == _aNewFormsButtons.isEmpty())
            for (Button formButton : _aNewFormsButtons)
                _newFormsButtonsPanel.add(formButton) ;

        if (false == _aOpenDashboardButtons.isEmpty())
            for (Button dashButton : _aOpenDashboardButtons)
                _newFormsButtonsPanel.add(dashButton) ;

        if (false == _aRequestCsvButtons.isEmpty())
            for (Button csvButton : _aRequestCsvButtons)
                _newFormsButtonsPanel.add(csvButton) ;

        if (false == _aOpenStaticHtmlButtons.isEmpty())
            for (Button staticButton : _aOpenStaticHtmlButtons)
                _newFormsButtonsPanel.add(staticButton) ;
    }

    /** 
     * Find the button that correspond to this widget and return its ID
     * 
     * @param  sender the Widget to be resolved as a Button
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    @Override
    public int getArchetypeIdForNewForm(Widget sender) 
    {
        if ((null == sender) || _aNewFormsButtons.isEmpty())
            return -1 ;

        for (Button button : _aNewFormsButtons)
            if (button == sender)
                return getArchetypeIdForNewFormButton(button) ;

        return -1 ;
    }

    /** 
     * Find the button that correspond to this widget and return its ID
     * 
     * @param  sender the Widget to be resolved as a Button
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    @Override
    public int getArchetypeIdForOpenDashboard(Widget sender)
    {
        if (null == sender)
            return -1 ;

        for (Button button : _aOpenDashboardButtons)
            if (button == sender)
                return getArchetypeIdForOpenDashboardButton(button) ;

        return -1 ;
    }

    /** 
     * Find the button that correspond to this widget and return its ID
     * 
     * @param  sender the Widget to be resolved as a Button
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    @Override
    public int getArchetypeIdForRequestCsv(Widget sender)
    {
        if (null == sender)
            return -1 ;

        for (Button button : _aRequestCsvButtons)
            if (button == sender)
                return getArchetypeIdForRequestCsvButton(button) ;

        return -1 ;
    }

    /** 
     * Find the button that correspond to this widget and return its ID
     * 
     * @param  sender the Widget to be resolved as a Button
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    @Override
    public int getArchetypeIdForOpenStaticHtml(Widget sender)
    {
        if (null == sender)
            return -1 ;

        for (Button button : _aOpenStaticHtmlButtons)
            if (button == sender)
                return getArchetypeIdForOpenStaticHtmlButton(button) ;

        return -1 ;
    }

    /** 
     * The button Id is in the form "new_form-id" + iArchetypeId; we must parse it to return the 
     * archetype ID as an int
     * 
     * @param  newFormButton the Button which ID is to be returned as an int
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    public int getArchetypeIdForNewFormButton(Button newFormButton) {
        return getArchetypeIdFromPattern(newFormButton, "new_form-id") ;
    }

    /** 
     * The button Id is in the form "open_dash-id" + iArchetypeId; we must parse it to return the 
     * archetype ID as an int
     * 
     * @param  openDashboardButton the Button which ID is to be returned as an int
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    public int getArchetypeIdForOpenDashboardButton(Button openDashboardButton) {
        return getArchetypeIdFromPattern(openDashboardButton, "open_dash-id") ;
    }

    /** 
     * The button Id is in the form "open_csv-id" + iArchetypeId; we must parse it to return the 
     * archetype ID as an int
     * 
     * @param  requestCsvButton the Button which ID is to be returned as an int
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    public int getArchetypeIdForRequestCsvButton(Button requestCsvButton) {
        return getArchetypeIdFromPattern(requestCsvButton, "request_csv-id") ;
    }

    /** 
     * The button Id is in the form "static_html-id" + iArchetypeId; we must parse it to return the 
     * archetype ID as an int
     * 
     * @param  openStaticHtmlButton the Button which ID is to be returned as an int
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    public int getArchetypeIdForOpenStaticHtmlButton(Button openStaticHtmlButton) {
        return getArchetypeIdFromPattern(openStaticHtmlButton, "static_html-id") ;
    }

    /** 
     * The button Id is in the form "new_form-id" + iArchetypeId; we must parse it to return the 
     * archetype ID as an int
     * 
     * @param  newFormButton the Button which ID is to be returned as an int
     *  
     * @return The archetype ID attached to this button if found; -1 if not  
     */
    protected int getArchetypeIdFromPattern(Widget button, String sPattern) 
    {
        if ((null == button) || (null == sPattern) || "".equals(sPattern))
            return -1 ;

        String sButtonId = button.getElement().getAttribute("id") ;

        int iPatternLen  = sPattern.length() ;
        int iButtonIdLen = sButtonId.length() ;

        if (iButtonIdLen <= iPatternLen)
            return -1 ;

        if (false == sPattern.equals(sButtonId.substring(0, iPatternLen)))
            return -1 ;

        String sArchetypeId = sButtonId.substring(iPatternLen, iButtonIdLen) ;

        try
        {
            return Integer.parseInt(sArchetypeId) ;
        } 
        catch (NumberFormatException e) 
        {
            return -1 ;
        }
    }

    @Override
    public int getSelectedCoach() {
        return _MgtCoach.getSelectedCoachId() ;
    }

    @Override
    public int getSelectedCoachingTrainee() {
        return _MgtCoach.getSelectedCoachingTraineeId() ;
    }

    @Override
    public int getSelectedTrainee() {
        return _MgtTrainee.getSelectedTraineeId() ;
    }

    @Override
    public HasClickHandlers getWarningOk() {
        return _WarningDialogBoxOkButton ;
    }

    @Override
    public HasClickHandlers getAdminPageButton() {
        return _GotoAdminPageButton ;
    }

    @Override
    public void showAdminPageButton() {
        _GotoAdminPageButton.setVisible(true) ;
    }
}
