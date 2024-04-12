package com.coachingfit.client.mvp ;

import net.customware.gwt.dispatch.client.DispatchAsync ;
import net.customware.gwt.presenter.client.EventBus ;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import java.util.ArrayList ;
import java.util.List ;
import java.util.logging.Level ;
import java.util.logging.Logger;

import com.coachingfit.client.event.TraineesListEvent;
import com.coachingfit.client.event.TraineesListEventHandler;
import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.utilities.LocalFunctions;
import com.coachingfit.shared.database.RegionData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.GetCoachingFitCoachsListAction;
import com.coachingfit.shared.rpc.GetCoachingFitCoachsListResult;
import com.coachingfit.shared.rpc.GetCoachingFitJobsListAction;
import com.coachingfit.shared.rpc.GetCoachingFitJobsListResult;
import com.coachingfit.shared.rpc.GetCoachingFitRegionsListAction;
import com.coachingfit.shared.rpc.GetCoachingFitRegionsListResult;
import com.coachingfit.shared.rpc.GetTraineesListAction;
import com.coachingfit.shared.rpc.GetTraineesListResult;
import com.coachingfit.shared.rpc.RecordTraineeAction;
import com.coachingfit.shared.rpc.RecordTraineeResult;
import com.coachingfit.shared.rpc_util.TraineesSearchTrait;
import com.coachingfit.shared.util.CoachingFitFcts;
import com.google.gwt.event.dom.client.ClickEvent ;
import com.google.gwt.event.dom.client.ClickHandler ;
import com.google.gwt.event.dom.client.HasClickHandlers ;
import com.google.gwt.user.client.rpc.AsyncCallback ;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject ;
import com.primege.client.event.GoToLoginResponseEvent;
import com.primege.shared.database.UserData;

/**
 * Presenter part of the Patient list
 */
public class CoachingFitTraineesListPresenter extends WidgetPresenter<CoachingFitTraineesListPresenter.Display>
{
    /** Current sorted column (-1 means none) */
    private int                         _iSortedColumn ; 
    /** Naturally sorted means clicked once (or an odd number of times) */
    private boolean                     _bNaturallySorted ;

    private List<TraineeData>           _aTrainees = new ArrayList<>() ;
    private List<UserData>              _aCoachs   = new ArrayList<>() ;
    private List<RegionData>            _aRegions  = new ArrayList<>() ;

    private TraineeData                 _editedTrainee ;
    
    private final DispatchAsync         _dispatcher ;
    private final CoachingFitSupervisor _supervisor ;
    private       Logger                _logger = Logger.getLogger("") ;

    public interface Display extends WidgetDisplay
    {
        HasClickHandlers    getBackButton() ;
        HasClickHandlers    getSearchButton() ;
        HasClickHandlers    getNewButton() ;
        HasClickHandlers    getFlexTable() ;
        HasClickHandlers    getFlexTableClick() ;
        
        HasClickHandlers    getFillMailButton() ;
        HasClickHandlers    getFillPasswordButton() ;

        HasClickHandlers    getUnactiveButton() ;
        HasClickHandlers    getSaveButton() ;
        HasClickHandlers    getCancelButton() ;

        TraineesSearchTrait getTraits() ;
        void                setActiveTrait(boolean bValue) ;

        int                 getClickedColumnHeader(ClickEvent event) ;
        int                 getClickedRow(ClickEvent event) ;

        void                setSelectedRow(final int iRow) ;

        void                resetList() ;
        void                reloadTrainees(List<TraineeData> aTrainees) ;
        void                reloadCoaches(List<UserData> aCoaches) ;
        void                reloadRegions(List<RegionData> aRegions) ;
        void                reloadJobs(List<String> aJobs) ;

        void                showEditionPanel() ;
        void                hideEditionPanel() ;

        void                setLastName(final String sLastName) ;
        void                setFirstName(final String sFirstName) ;
        void                setJob(final String sJob) ;
        void                setMail(final String sMail) ;
        void                setCoach(final int iCoachId) ;
        void                setRegion(final int iRegionId) ;
        void                setPassword(final String sPassword) ;

        String              getLastName() ;
        String              getFirstName() ;
        String              getJob() ;
        String              getMail() ;
        int                 getCoach() ;
        int                 getRegion() ;
        String              getPassword() ;
    }

    @Inject
    public CoachingFitTraineesListPresenter(final Display display, final EventBus eventBus, final DispatchAsync dispatcher, final CoachingFitSupervisor supervisor)
    {
        super(display, eventBus) ;

        _dispatcher       = dispatcher ;
        _supervisor       = supervisor ;

        _iSortedColumn    = -1 ;
        _bNaturallySorted = true ;
        
        _editedTrainee    = null ;

        bind() ;

        getInformation() ;
    }

    @Override
    protected void onBind()
    {
        // Install Event Bus messages handlers
        //
        eventBus.addHandler(TraineesListEvent.TYPE, new TraineesListEventHandler()
        {
            public void onTraineesList(final TraineesListEvent event)
            {
                FlowPanel workSpace = event.getWorkspace() ;
                workSpace.clear() ;
                workSpace.add(getDisplay().asWidget()) ;
            }
        }) ;

        // Click back button
        //
        display.getBackButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                eventBus.fireEvent(new GoToLoginResponseEvent()) ;
            }
        }) ;

        // Click search according to entered traits
        //
        display.getSearchButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                reloadList() ;
            }
        }) ;
        
        // Click on trainees list
        //
        display.getFlexTableClick().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                // Was the click on a header?
                //
                int iClickedColHeader = display.getClickedColumnHeader(event) ;
                if (iClickedColHeader >= 0)
                    sortForHeader(iClickedColHeader) ;
                else
                {
                    int iClickedRow = display.getClickedRow(event) ;
                    if (iClickedRow < 1)
                        return ;

                    display.setSelectedRow(iClickedRow) ;

                    _editedTrainee = getTraineeAtRow(iClickedRow) ;
                    if (null == _editedTrainee)
                        return ;

                    editTrainee() ;
                }
            }
        }) ;

        // Click to create a new trainee
        //
        display.getNewButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                createNew() ;
            }
        }) ;

        // Click to save changes
        //
        display.getSaveButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                saveChanges() ;
            }
        }) ;

        // Click to unactivate trainee
        //
        display.getUnactiveButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                unactivateTrainee() ;
            }
        }) ;

        // Click to unactivate trainee
        //
        display.getCancelButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                cancelEdition() ;
            }
        }) ;
        
        // Click to fill email from name
        //
        display.getFillMailButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                fillMail() ;
            }
        }) ;
        
        // Click to fill password
        //
        display.getFillPasswordButton().addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(final ClickEvent event)
            {
                fillPassword() ;
            }
        }) ;
        
        // Initialize columns and search interface elements
        //
        initDisplay() ;
    }

    protected void initDisplay()
    {
        display.resetList() ;
        display.setActiveTrait(true) ;
    }

    /**
     * Edit/create a trainee
     * 
     * @param trainee trainee information to edit
     */
    private void editTrainee()
    {
        display.showEditionPanel() ;

        if (null == _editedTrainee)
            resetEditionControls() ;
        else
            setEditionControls();
    }

    /**
     * Set all information in the edition panel to their void values
     */
    private void resetEditionControls()
    {
        display.setLastName("") ;
        display.setFirstName("") ;
        display.setJob("") ;
        display.setMail("") ;
        display.setCoach(0) ;
        display.setRegion(0) ;
        display.setPassword("") ;
    }

    /**
     * Set all information in the edition panel to a trainee's values
     */
    private void setEditionControls()
    {
        if (null == _editedTrainee)
        {
            resetEditionControls() ;
            return ;
        }

        display.setLastName(_editedTrainee.getLastName()) ;
        display.setFirstName(_editedTrainee.getFirstName()) ;
        display.setJob(_editedTrainee.getJobType()) ;
        display.setMail(_editedTrainee.getEMail()) ;
        display.setCoach(_editedTrainee.getCoachId()) ;
        display.setRegion(_editedTrainee.getRegionId()) ;
        display.setPassword(_editedTrainee.getPassword()) ;
    }

    /**
     * Create a new trainee
     */
    private void createNew()
    {
        resetEditionControls() ;

        display.showEditionPanel() ;
    }

    /**
     * Click to record changes on server
     */
    private void saveChanges()
    {
        TraineeData trainee = new TraineeData(_editedTrainee) ;

        fillTraineeFromForm(trainee) ;

        if (false == trainee.isValid())
            return ;

        // Ask the server for corresponding information
        //
        _dispatcher.execute(new RecordTraineeAction(_supervisor.getUserId(), trainee), new recordTraineeCallback()) ;
    }
    
    /**
     * Unactivate currently edited trainee (set coach identifier and region identifier to <code>0</code>)
     */
    private void unactivateTrainee()
    {
        TraineeData trainee = new TraineeData(_editedTrainee) ;
        
        fillTraineeFromForm(trainee) ;

        trainee.setCoachId(0) ;
        trainee.setRegionId(0) ;
        
        // Ask the server for corresponding information
        //
        _dispatcher.execute(new RecordTraineeAction(_supervisor.getUserId(), trainee), new recordTraineeCallback()) ;
    }

    /**
     * Cancel the edition process (close the edition screen with no changes)
     */
    private void cancelEdition()
    {
        closeEditionPanel() ;
    }
    
    /**
     * Callback function called when the server returns information from files processing
     */
    public class recordTraineeCallback implements AsyncCallback<RecordTraineeResult>
    {
        public recordTraineeCallback() {
            super() ;
        }

        @Override
        public void onFailure(Throwable cause)
        {
            _logger.log(Level.SEVERE, "Unhandled error", cause) ;
            _logger.info("error from recordTraineeCallback!!") ;
        }

        @Override
        public void onSuccess(RecordTraineeResult value)
        {
            String sMessage = value.getMessage() ;
            if (false == "".equals(sMessage))
            {
                _logger.info("Recording edited trainee information failed (" + sMessage + ").") ;
                return ;
            }

            _logger.info("Trainees information have been recorded successfully") ;

            closeEditionPanel() ;
            
            reloadList() ;
        }
    }
    
    /**
     * Close the edition panel, reset its controls and nullify edited trainee
     */
    private void closeEditionPanel()
    {
        _editedTrainee = null ;
        
        resetEditionControls() ;

        display.hideEditionPanel() ;
    }
    
    /**
     * Fill a {@link TraineeData} from the entry form
     * 
     * @param trainee The {@link TraineeData} to fill
     */
    private void fillTraineeFromForm(TraineeData trainee)
    {
        if (null == trainee)
            return ;

        String sLastName  = display.getLastName() ;
        String sFirstName = display.getFirstName() ;

        trainee.setFirstName(sFirstName) ;
        trainee.setLabel(sFirstName, sLastName) ;

        trainee.setJobType(display.getJob()) ;
        trainee.setEMail(display.getMail()) ;
        trainee.setCoachId(display.getCoach()) ;
        trainee.setRegionId(display.getRegion()) ;
        trainee.setPassword(display.getPassword()) ;
    }

    /**
     * Refresh trainees list with all the trainees affected to current user
     */
    private void loadAllTraineesForUser()
    {
        // Get current search traits' value
        //
        TraineesSearchTrait traits = new TraineesSearchTrait() ;

        // Ask the server for corresponding information
        //
        _dispatcher.execute(new GetTraineesListAction(_supervisor.getUserId(), traits), new reloadListCallback()) ;
    }

    /**
     * Refresh patients list from a new selection of traits
     */
    protected void reloadList()
    {
        // Get current search traits' value
        //
        TraineesSearchTrait traits = display.getTraits() ;

        // Ask the server for corresponding information
        //
        _dispatcher.execute(new GetTraineesListAction(_supervisor.getUserId(), traits), new reloadListCallback()) ;
    }

    /**
     * Callback function called when the server returns information from files processing
     */
    public class reloadListCallback implements AsyncCallback<GetTraineesListResult>
    {
        public reloadListCallback() {
            super() ;
        }

        @Override
        public void onFailure(Throwable cause)
        {
            _logger.log(Level.SEVERE, "Unhandled error", cause) ;
            _logger.info("error from reloadListCallback!!") ;
        }

        @Override
        public void onSuccess(GetTraineesListResult value)
        {
            String sMessage = value.getMessage() ;
            if (false == "".equals(sMessage))
            {
                _logger.info("Fetching a new list of patients failed (" + sMessage + ").") ;
                return ;
            }

            _logger.info("Server returned processed a new list of trainees.") ;

            _aTrainees.clear() ;
            _aTrainees.addAll(value.getTrainees()) ;

            display.reloadTrainees(_aTrainees) ;
        }
    }

    protected void sortForHeader(int iClickedCol)
    {
        if (iClickedCol == _iSortedColumn)
            _bNaturallySorted = !_bNaturallySorted ;
        else
        {
            _iSortedColumn = iClickedCol ;
            _bNaturallySorted = true ;
        }

        sortData() ;

        display.reloadTrainees(_aTrainees) ;
    }

    /**
     * Sort data for selected column
     */
    protected void sortData()
    {
        // if (_iSortedColumn == 0)
        // _aTraitsForPatient.sort(new TraitsForPatientComparator(iSortedTrait, _bNaturallySorted)) ;
    }

    /**
     * Get the trainee located at a given row inside the list
     * 
     * @param iRow Row to get trainee information from
     * @return The {@link TraineeData} if row is valid, <code>null</code> if not
     */
    protected TraineeData getTraineeAtRow(final int iRow)
    {
        if ((iRow < 1) || (iRow > _aTrainees.size()))
            return null ;

        return _aTrainees.get(iRow - 1) ;
    }

    /**
     * Get a {@link TraineeData} from its identifier
     * 
     * @param iTraineeId Identifier of the trainee to look for
     * 
     * @return The {@link TraineeData} if found, <code>null</code> if not
     */
    private TraineeData getTraineeFromId(int iTraineeId)
    {
        if ((null == _aTrainees) || _aTrainees.isEmpty())
            return null ;

        for (TraineeData trainee : _aTrainees)
            if (trainee.getId() == iTraineeId)
                return trainee ;

        return null ;
    }

    /**
     * Get all needed information (users, regions, etc)
     */
    private void getInformation()
    {
        loadCoaches() ;
    }

    private void loadCoaches() {
        _dispatcher.execute(new GetCoachingFitCoachsListAction(_supervisor.getUserId(), true), new getCoachsCallback()) ;
    }

    /**
     * Callback function called when the server returns the list of active coaches
     */
    public class getCoachsCallback implements AsyncCallback<GetCoachingFitCoachsListResult>
    {
        public getCoachsCallback() {
            super() ;
        }

        @Override
        public void onFailure(Throwable cause)
        {
            _logger.log(Level.SEVERE, "Unhandled error", cause) ;
            _logger.info("error when getting list of coaches!!") ;
        }

        @Override
        public void onSuccess(GetCoachingFitCoachsListResult value)
        {
            String sMessage = value.getMessage() ;
            if (false == "".equals(sMessage))
            {
                _logger.info("Fetching the list of coaches failed (" + sMessage + ").") ;
                return ;
            }

            _logger.info("Server returned the list of coaches.") ;

            _aCoachs.clear() ;
            _aCoachs.addAll(value.getCoachsData()) ;

            display.reloadCoaches(_aCoachs) ;

            loadRegions() ;
        }
    }

    private void loadRegions() {
        _dispatcher.execute(new GetCoachingFitRegionsListAction(_supervisor.getUserId(), true), new getRegionsCallback()) ;
    }

    /**
     * Callback function called when the server returns the list of active coaches
     */
    public class getRegionsCallback implements AsyncCallback<GetCoachingFitRegionsListResult>
    {
        public getRegionsCallback() {
            super() ;
        }

        @Override
        public void onFailure(Throwable cause)
        {
            _logger.log(Level.SEVERE, "Unhandled error", cause) ;
            _logger.info("error when getting list of regions!!") ;
        }

        @Override
        public void onSuccess(GetCoachingFitRegionsListResult value)
        {
            String sMessage = value.getMessage() ;
            if (false == "".equals(sMessage))
            {
                _logger.info("Fetching the list of coaches failed (" + sMessage + ").") ;
                return ;
            }

            _logger.info("Server returned the list of coaches.") ;

            _aRegions.clear() ;
            _aRegions.addAll(value.getRegionsData()) ;

            display.reloadRegions(_aRegions) ;

            loadJobs() ;
        }
    }

    private void loadJobs() {
        _dispatcher.execute(new GetCoachingFitJobsListAction(_supervisor.getUserId()), new getJobsCallback()) ;
    }

    /**
     * Callback function called when the server returns the list of active coaches
     */
    public class getJobsCallback implements AsyncCallback<GetCoachingFitJobsListResult>
    {
        public getJobsCallback() {
            super() ;
        }

        @Override
        public void onFailure(Throwable cause)
        {
            _logger.log(Level.SEVERE, "Unhandled error", cause) ;
            _logger.info("error when getting list of jobs!!") ;
        }

        @Override
        public void onSuccess(GetCoachingFitJobsListResult value)
        {
            String sMessage = value.getMessage() ;
            if (false == "".equals(sMessage))
            {
                _logger.info("Fetching the list of jobs failed (" + sMessage + ").") ;
                return ;
            }

            _logger.info("Server returned the list of jobs.") ;

            display.reloadJobs(value.getJobs()) ;
        }
    }

    /**
     * Fill the "e-mail" text box from first name and last name
     */
    private void fillMail()
    {
        String sLastName  = display.getLastName() ;
        String sFirstName = display.getFirstName() ;
        
        if (sLastName.isEmpty() && sFirstName.isEmpty())
            return ;
        
        String sMail = LocalFunctions.getMailFromName(sFirstName, sLastName, "savencia-plf.com") ;
        
        if (null != sMail)
            display.setMail(sMail) ;
    }
    
    private void fillPassword()
    {
        String sPassword = CoachingFitFcts.buildPassword(10) ;
        display.setPassword(sPassword) ;
    }

    @Override
    protected void onUnbind()
    {
        // Add unbind functionality here for more complex presenters.
    }

    public void refreshDisplay()
    {
        // This is called when the presenter should pull the latest data
        // from the server, etc. In this case, there is nothing to do.
    }

    public void revealDisplay()
    {
        // Nothing to do. This is more useful in UI which may be buried
        // in a tab bar, tree, etc.
    }

    @Override
    protected void onRevealDisplay()
    {
        // Nothing to do
    }
}
