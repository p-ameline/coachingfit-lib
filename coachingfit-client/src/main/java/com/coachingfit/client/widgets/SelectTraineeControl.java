package com.coachingfit.client.widgets;

import java.util.Iterator;
import java.util.List;

import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.shared.database.TraineeData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;
import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.ControlBaseWithParams;
import com.primege.client.widgets.ControlModel;
import com.primege.shared.database.FormDataData;

/**
 * TextBox with a drop down list of trainees
 */
public class SelectTraineeControl extends ListBox implements ControlModel
{
    private final CoachingFitConstants constants = GWT.create(CoachingFitConstants.class) ;

    private ControlBaseWithParams _base ;
    private List<TraineeData>     _aTrainees ;

    private String                _sPreSelectedId ;   // In case of asynchronous initialization of trainees list
    
    private boolean               _bWideOpen ;

    /**
     * Default Constructor
     *
     */
    public SelectTraineeControl(List<TraineeData> aTrainees, final String sPath, boolean bAllowedMode, boolean bWideOpen)
    {
        super() ;

        _base           = new ControlBaseWithParams(sPath) ;
        _aTrainees      = aTrainees ;

        _sPreSelectedId = "" ;
        _bWideOpen      = bWideOpen ;

        init(bAllowedMode) ;
        
        setVisibleCount() ;
        setItemSelected(0, true) ;
    }

    private void setVisibleCount()
    {
        int iVisibleItemsCount = 1 ;
        
        if (_bWideOpen && _sPreSelectedId.isEmpty() && (null != _aTrainees) && (false == _aTrainees.isEmpty()))
        {
            iVisibleItemsCount = 20 ;
            if (_aTrainees.size() < 18)
                iVisibleItemsCount = _aTrainees.size() + 2 ;
        }
            
        setVisibleItemCount(iVisibleItemsCount) ;
    }
    
    /**
     * Initialize the list with trainees - the first being "undefined" 
     *
     */
    public void init(boolean bAllowedMode)
    {
        addItem(constants.Undefined()) ;

        if ((null == _aTrainees) || _aTrainees.isEmpty())
            return ;

        for (TraineeData trainee : _aTrainees)
            addItem(trainee.getLabel()) ;

        if (bAllowedMode)
            addItem(constants.Less()) ;
        else
            addItem(constants.More()) ;
        
        if (false == "".equals(_sPreSelectedId))
            initSelectedForId(_sPreSelectedId) ;
    }

    public boolean isSingleData() {
        return true ;
    }

    /**
     * Return a FormDataData which value is filled with content
     *
     */
    public FormDataData getContent() 
    {
        int iSelectedTraineeId = getSelectedTraineeId() ;
        if (-1 == iSelectedTraineeId)
            return null ;

        FormDataData formData = new FormDataData() ;
        formData.setPath(_base.getPath()) ;
        formData.setValue(Integer.toString(iSelectedTraineeId)) ;
        return formData ;
    }

    /**
     * Return the selected trainee Id is any, or <code>-1</code> if none, <code>-2</code> if "more" or <code>-3</code> if "less"
     */
    public int getSelectedTraineeId()
    {
        String sSelectedTrainee = getSelectedValue() ;

        if ("".equals(sSelectedTrainee) || sSelectedTrainee.equals(constants.Undefined()))
            return -1 ;
        
        if (sSelectedTrainee.equals(constants.More()))
            return -2 ;
        
        if (sSelectedTrainee.equals(constants.Less()))
            return -3 ;

        int iSelectedTraineeId = -1 ; 

        for (Iterator<TraineeData> it = _aTrainees.iterator() ; (it.hasNext()) && (-1 == iSelectedTraineeId) ; )
        {
            TraineeData trainee = it.next() ;
            if (sSelectedTrainee.equals(trainee.getLabel()))
                iSelectedTraineeId = trainee.getId() ;
        }

        // If a trainee is selected, contract the control to a single line
        //
        if (iSelectedTraineeId > 0)
            setVisibleItemCount(1) ;
        
        return iSelectedTraineeId ; 
    }

    /**
     * Initialize the selected trainee from a content and a default value
     * 
     * @param content       FormDataData used to initialize the control
     * @param sDefaultValue Configuration parameters, including default value in case there is no content
     */
    public void setContent(final FormDataData content, final String sDefaultValue)
    {
        _base.parseParams(sDefaultValue) ;

        setContent(content) ;
    }

    /**
     * Initialize the selected trainee from a content
     * 
     * @param content FormDataData used to initialize the control
     */
    public void setContent(final FormDataData content)
    {
        if (null == content)
        {
            setItemSelected(0, true) ;
            return ;
        }

        String sTraineeId = content.getValue() ;
        if ((null == sTraineeId) || "".equals(sTraineeId))
            sTraineeId = _base.getDefaultValue() ;

        _sPreSelectedId = sTraineeId ; 

        // If a trainee is already selected, no need to keep the list wide open
        //
        setVisibleItemCount(1) ;
        
        initSelectedForId(sTraineeId) ;
    }

    public void resetContent() {
        setItemSelected(0, true) ;
    }

    protected void initSelectedForId(final String sTraineeId)
    {
        if ((null == sTraineeId) || "".equals(sTraineeId))
        {
            setItemSelected(0, true) ;
            return ;
        }

        TraineeData trainee = getTraineeFromId(Integer.parseInt(sTraineeId)) ;
        if (null == trainee)
            return ;

        String sTraineeLabel = trainee.getLabel() ;		
        if ("".equals(sTraineeLabel))
            return ;

        int iSize = getItemCount() ;
        for (int i = 0 ; i < iSize ; i++)
            if (getItemText(i).equals(sTraineeLabel))
            {
                setItemSelected(i, true) ;
                return ;
            }
    }

    /**
     * Get trainee information from it's identifier 
     *
     */
    public TraineeData getTraineeFromId(final int iTraineeId)
    {
        if ((null == _aTrainees) || _aTrainees.isEmpty())
            return null ;

        for (TraineeData trainee : _aTrainees)
            if (trainee.getId() == iTraineeId)
                return trainee ;

        return null ;
    }

    public ControlBase getControlBase() {
        return _base ;
    }

    public void setInitFromPrev(final boolean bInitFromPrev) {
        _base.setInitFromPrev(bInitFromPrev) ;
    }

    public boolean getInitFromPrev() {
        return _base.getInitFromPrev() ;
    }

    /**
     * Update the list of trainees
     * 
     * @param aTrainees New list of trainees
     */
    public void updateTrainees(final List<TraineeData> aTrainees, boolean bAllowedMode)
    {
        clear() ;

        if (null == aTrainees)
        {
            _aTrainees.clear() ;
            return ;
        }

        _aTrainees = aTrainees ;
        
        setVisibleCount() ;

        init(bAllowedMode) ;
    }
}
