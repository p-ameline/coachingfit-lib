package com.coachingfit.client.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.shared.database.TraineeData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.ControlBaseWithParams;
import com.primege.client.widgets.ControlModel;
import com.primege.shared.database.FormDataData;
import com.primege.shared.database.UserData;

/**
 * TextBox with a drop down list that displays coaches and coaching trainees 
 * 
 * Inspired from http://sites.google.com/site/gwtcomponents/auto-completiontextbox
 */
public class SelectCoachControl extends ListBox implements ControlModel
{
    private final CoachingFitConstants constants = GWT.create(CoachingFitConstants.class) ;

    private ControlBaseWithParams _base ;

    private UserData              _user ;

    private List<UserData>        _aCoachs ;
    private List<TraineeData>     _aCoachingTrainees ;

    private List<Person>          _aPersons = new ArrayList<Person>() ;

    public static class Person
    {
        public enum PersonType { coach, trainee } ;

        protected String     _sLabel ;
        protected PersonType _iPersonType ;
        protected int        _iID ;

        protected String     _sLastSortableName ;

        public Person(final String sLabel, PersonType iPersonType, int iID)
        {
            _sLabel      = sLabel ;
            _iPersonType = iPersonType ;
            _iID         = iID ;

            _sLastSortableName = processLastSortableName() ;
        }

        public String getLabel() {
            return _sLabel ;
        }
        public String getLastName() {
            return CoachingFitSupervisor.getLastName(_sLabel) ;
        }
        public String processLastSortableName()
        {
            String sLastName = getLastName() ;
            return CoachingFitSupervisor.getSortableLastName(sLastName) ;
        }
        public String getLastSortableName() {
            return _sLastSortableName ;
        }
        public PersonType getPersonType() {
            return _iPersonType ;
        }
        public int getID() {
            return _iID ;
        }
    }

    public SelectCoachControl()
    {
        super() ;

        _base              = null ;
        _aCoachs           = null ;
        _aCoachingTrainees = null ;
        _user              = null ;

        setVisibleItemCount(1) ;
    }

    /**
     * Default Constructor
     *
     */
    public SelectCoachControl(final List<UserData> aCoachs, final List<TraineeData> aCoachingTrainees, final UserData user, final String sPath)
    {
        super() ;

        setParameters(aCoachs, aCoachingTrainees, user, sPath) ;
    }

    public boolean isSingleData() {
        return true ;
    }

    public void setParameters(final List<UserData> aCoachs, final List<TraineeData> aCoachingTrainees, final UserData user, final String sPath)
    {
        _base              = new ControlBaseWithParams(sPath) ;
        _aCoachs           = aCoachs ;
        _aCoachingTrainees = aCoachingTrainees ;
        _user              = user ;

        init() ;

        setVisibleItemCount(1) ;
        setItemSelected(0, true) ;
    }

    /**
     * Initialize the list with trainees - the first being "undefined" 
     *
     */
    public void init()
    {
        addItem(constants.Undefined()) ;

        if (null == _aPersons)
            return ;

        _aPersons.clear() ;

        if ((null != _aCoachs) && (false == _aCoachs.isEmpty()))
            for (UserData user : _aCoachs)
                _aPersons.add(new Person(user.getLabel(), Person.PersonType.coach, user.getId())) ;

        if (null != _user)
            _aPersons.add(new Person(_user.getLabel(), Person.PersonType.coach, _user.getId())) ;

        if ((null != _aCoachingTrainees) && (false == _aCoachingTrainees.isEmpty()))
            for (TraineeData trainee : _aCoachingTrainees)
                _aPersons.add(new Person("*" + trainee.getLabel(), Person.PersonType.trainee, trainee.getId())) ;

        if (_aPersons.isEmpty())
            return ;

        Comparator<Person> lastTraineeNameComparator = new Comparator<Person>()
        {
            @Override
            public int compare(Person left, Person right) {
                return left.getLastSortableName().compareTo(right.getLastSortableName()) ;
            }
        };

        Collections.sort(_aPersons, lastTraineeNameComparator) ;

        for (Person person : _aPersons)
            addItem(person.getLabel()) ;
    }

    /**
     * Return the selected person, or <code>null</code> if none
     */
    public Person getSelectedPerson()
    {
        String sSelectedCoach = getSelectedValue() ;

        if ("".equals(sSelectedCoach) || sSelectedCoach.equals(constants.Undefined()))
            return null ;

        if ((null == _aPersons) || _aPersons.isEmpty())
            return null ;

        for (Person person : _aPersons)
            if (sSelectedCoach.equals(person.getLabel()))
                return person ;

        return null ; 
    }

    /**
     * Return the selected coach Id is any, or <code>-1</code> if none
     */
    public int getSelectedCoachId()
    {
        Person selectedPerson = getSelectedPerson() ;

        if (null == selectedPerson)
            return -1 ;

        if (Person.PersonType.coach == selectedPerson.getPersonType())
            return selectedPerson.getID() ;

        return -1 ; 
    }

    public void setSelectedCoach(int iCoachId)
    {
        if (iCoachId <= 0)
        {
            setItemSelected(0, true) ;
            return ;
        }

        UserData coach = getCoachFromId(iCoachId) ;

        if (null == coach)
            return ;

        int iIndex = 1 ;

        for (Person person : _aPersons)
        {
            if ((Person.PersonType.coach == person.getPersonType()) && (person.getID() == iCoachId))
            {
                setItemSelected(iIndex, true) ;
                break ;
            }

            iIndex++ ;
        }
    }

    /**
     * Return the selected coaching trainee Id is any, or <code>-1</code> if none
     */
    public int getSelectedCoachingTraineeId()
    {
        Person selectedPerson = getSelectedPerson() ;

        if (null == selectedPerson)
            return -1 ;

        if (Person.PersonType.trainee == selectedPerson.getPersonType())
            return selectedPerson.getID() ;

        return -1 ; 
    }

    /**
     * Get user information from it's identifier 
     */
    public UserData getCoachFromId(final int iCoachId)
    {
        if ((null == _aCoachs) || _aCoachs.isEmpty())
            return null ;

        for (UserData coach : _aCoachs)
            if (coach.getId() == iCoachId)
                return coach ;

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
     * Initialize state from a content and a default value
     *
     * @param content       FormDataData used to initialize the control
     * @param sDefaultValue Configuration parameters, including default value in case there is no content 
     */
    @Override
    public void setContent(final FormDataData content, final String sDefaultValue) 
    {
        _base.parseParams(sDefaultValue) ;

        setContent(content) ;
    }

    /**
     * Initialize state from a content
     *
     * @param content FormDataData used to initialize the control
     */
    @Override
    public void setContent(final FormDataData content)
    {
        if (null == content)
        {
            setItemSelected(0, true) ;
            return ;
        }

        String sCoachId = content.getValue() ;

        if ((null == sCoachId) || "".equals(sCoachId))
            sCoachId = _base.getDefaultValue() ;

        if ((null == sCoachId) || "".equals(sCoachId))
        {
            setItemSelected(0, true) ;
            return ;
        }

        UserData coach = getCoachFromId(Integer.parseInt(sCoachId)) ;
        if (null == coach)
            return ;

        String sCoachLabel = coach.getLabel() ;		
        if ("".equals(sCoachLabel))
            return ;

        int iSize = getItemCount() ;
        for (int i = 0 ; i < iSize ; i++)
            if (getItemText(i).equals(sCoachLabel))
            {
                setItemSelected(i, true) ;
                return ;
            }
    }

    public void resetContent() {
        setItemSelected(0, true) ;
    }

    @Override
    public FormDataData getContent() 
    {
        int iSelectedCoachId = getSelectedCoachId() ;
        if (-1 == iSelectedCoachId)
            return null ;

        FormDataData formData = new FormDataData() ;
        formData.setPath(_base.getPath()) ;
        formData.setValue(Integer.toString(iSelectedCoachId)) ;
        return formData ;
    }
}
