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
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.GetTraineesListAction;
import com.coachingfit.shared.rpc.GetTraineesListResult;
import com.coachingfit.shared.rpc_util.TraineesSearchTrait;

import com.google.gwt.event.dom.client.ClickEvent ;
import com.google.gwt.event.dom.client.ClickHandler ;
import com.google.gwt.event.dom.client.HasClickHandlers ;
import com.google.gwt.user.client.rpc.AsyncCallback ;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject ;

/**
 * Presenter part of the Patient list
 */
public class CoachingFitTraineesListPresenter extends WidgetPresenter<CoachingFitTraineesListPresenter.Display>
{
    /** Current sorted column (-1 means none) */
    private int                         _iSortedColumn ; 
    /** Naturally sorted means clicked once (or an odd number of times) */
    private boolean                     _bNaturallySorted ;
    
    private List<TraineeData>           _aTrainees = new ArrayList<TraineeData>() ;
    
    private final DispatchAsync         _dispatcher ;
	private final CoachingFitSupervisor _supervisor ;
	private       Logger                _logger = Logger.getLogger("") ;
    
    public interface Display extends WidgetDisplay
    {
        HasClickHandlers    getSearchButton() ;
        HasClickHandlers    getFlexTable() ;
        HasClickHandlers    getFlexTableClick() ;

        TraineesSearchTrait getTraits() ;
        
        int                 getClickedColumnHeader(ClickEvent event) ;
        int                 getClickedRow(ClickEvent event) ;

        void                setSelectedRow(final int iRow) ;

        void                resetList() ;
        void                reloadTrainees(List<TraineeData> aTrainees) ;
    }

    @Inject
    public CoachingFitTraineesListPresenter(final Display display, final EventBus eventBus, final DispatchAsync dispatcher, final CoachingFitSupervisor supervisor)
    {
        super(display, eventBus) ;
        
        _dispatcher          = dispatcher ;
		_supervisor          = supervisor ;

        _iSortedColumn       = -1 ;
        _bNaturallySorted    = true ;

        bind() ;
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

                    TraineeData trainee = getTraineeAtRow(iClickedRow) ;
                    if (null == trainee)
                        return ;
                }
            }
        }) ;

        // Initialize columns and search interface elements
        //
        initDisplay() ;
    }

    protected void initDisplay() {
        display.resetList() ;
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
