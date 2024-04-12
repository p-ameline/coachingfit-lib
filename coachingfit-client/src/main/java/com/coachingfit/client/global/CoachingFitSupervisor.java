package com.coachingfit.client.global;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.coachingfit.client.gin.CoachingFitGinjector;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;

import com.google.inject.Inject;

import com.primege.client.global.PrimegeSupervisorModel;
import com.primege.shared.database.UserData;

public class CoachingFitSupervisor extends PrimegeSupervisorModel
{
    private CoachingFitGinjector _injector ;
    private final String         _sClientVersion ;
    private       String         _sServerVersion ;

    protected     TraineeData    _traineeAsUser ;

    @Inject
    public CoachingFitSupervisor(final DispatchAsync dispatcher, final EventBus eventBus)
    {
        super(dispatcher, eventBus) ;

        _user           = new CoachingFitUser() ;
        _traineeAsUser  = null ;
        _injector       = null ;
        _sClientVersion = "2.6" ;
        _sServerVersion = "" ;
    }

    /**
     * Set the new user (since there is either a genuine user or a trainee as user, the trainee is set to <code>null</code>)
     */
    public void setCoachingFitUser(CoachingFitUser otherUser)
    {
        if (null == otherUser)
        {
            ((CoachingFitUser) _user).reset() ;
            return ;
        }

        ((CoachingFitUser) _user).initFromUser(otherUser) ;

        setTraineeUser(null) ;
    }
    public CoachingFitUser getCoachingFitUser() {
        return (CoachingFitUser) _user ;
    }

    /**
     * Set the trainee as user (since there is either a genuine user or a trainee as user, the user is reseted)
     */
    public void setTraineeUser(TraineeData trainee) 
    {
        if (null == trainee)
        {
            _traineeAsUser = null ;
            return ;
        }

        // Resets the user
        //

        // Don't do that since archetypes and trainees are part of the user structure
        // ((CoachingFitUser) _user).reset() ;

        // Just set userData to blank
        //
        _user.setUserData(new UserData()) ; 

        // Update the trainee
        //
        if (null == _traineeAsUser)
            _traineeAsUser = new TraineeData(trainee) ;
        else
            _traineeAsUser.initFromOther(trainee) ;
    }
    public TraineeData getTraineeUser() {
        return _traineeAsUser ;
    }

    /**
     * Is current user a trainee?
     */
    public boolean isUserATrainee() {
        return (null != _traineeAsUser) ;
    }

    /**
     * Get trainee as user's id
     * 
     * @return <code>-1</code> if user is not a trainee, its identifier if she is
     */
    public int getTraineeAsUserId()
    {
        if (false == isUserATrainee())
            return -1 ;

        return _traineeAsUser.getId() ;
    }

    public void setInjector(CoachingFitGinjector injector) {
        _injector = injector ;
    }
    public CoachingFitGinjector getInjector() {
        return _injector ;
    }

    public String getClientVersion() {
        return _sClientVersion ;
    }

    public void setServerVersion(String sVersion) {
        _sServerVersion = sVersion ;
    }
    public String getServerVersion() {
        return _sServerVersion ;
    }

    public String getUserLanguage() {
        return "fr" ;
    }

    /**
     * Get trainees for current user
     * 
     * @return The List of trainees, of <code>null</code> if no current user
     */
    public List<TraineeData> getTraineesForUser() 
    {
        CoachingFitUser user = getCoachingFitUser() ;
        if (null == user)
            return null ;

        return user.getTrainees() ;
    }
    
    /**
     * Get allowed trainees for current user
     * 
     * @return The List of allowed trainees, of <code>null</code> if no current user
     */
    public List<TraineeData> getAllowedTraineesForUser() 
    {
        CoachingFitUser user = getCoachingFitUser() ;
        if (null == user)
            return null ;

        return user.getAllowedTrainees() ;
    }

    /**
     * Get coaching trainees for current user
     * 
     * @return The List of trainees, of <code>null</code> if no current user
     */
    public List<TraineeData> getCoachingTraineesForUser() 
    {
        CoachingFitUser user = getCoachingFitUser() ;
        if (null == user)
            return null ;

        List<TraineeData> aResult = new ArrayList<TraineeData>() ;

        for (TraineeData trainee : user.getTrainees())
            if (false == trainee.getPassword().isEmpty())
                aResult.add(trainee) ;

        return aResult ;
    }

    /**
     * Get the pseudo for the "Hello X" welcome invite
     */
    public String getUserPseudo() 
    {
        if (null == _traineeAsUser)
            return super.getUserPseudo() ;

        return _traineeAsUser.getFirstName() ;
    }

    /**
     * Get an array of coaches (non sorted)
     * 
     * @return <code>null</code> if something went wrong, the sorted array if not
     */
    public List<UserData> getCoachesArray()
    {
        if (null == _user)
            return null ;

        return ((CoachingFitUser) _user).getCoaches() ;
    }

    /**
     * Get an array of coaches sorted on last name
     * 
     * @return <code>null</code> if something went wrong, the sorted array if not
     */
    public List<UserData> getSortedCoachesArray()
    {
        // Create the array early so we never return the original one, but always a copy
        //
        List<UserData> aSortedCoaches = new ArrayList<UserData>() ;

        // Get the unsorted array of coaches
        //
        List<UserData> aCoaches = getCoachesArray() ;
        if ((null == aCoaches) || aCoaches.isEmpty())
            return aSortedCoaches ;

        // Fill and sort
        //
        aSortedCoaches.addAll(aCoaches) ;

        Comparator<UserData> lastNameComparator = new Comparator<UserData>()
        {
            @Override
            public int compare(UserData left, UserData right) {
                return getSortableLastName(CoachingFitSupervisor.getLastName(left)).compareTo(getSortableLastName(CoachingFitSupervisor.getLastName(right))) ;
            }
        };

        Collections.sort(aSortedCoaches, lastNameComparator) ;

        return aSortedCoaches ;
    }

    /**
     * Get an array of trainees sorted on last name
     * 
     * @return <code>null</code> if something went wrong, the sorted array if not
     */
    public static List<TraineeData> getSortedTraineesArray(List<TraineeData> aUnsorted)
    {
        if (null == aUnsorted)
            return null ;

        // Create the array early so we never return the original one, but always a copy
        //
        List<TraineeData> aSortedTrainees = new ArrayList<TraineeData>() ;

        // Get the unsorted array of coaches
        //
        if (aUnsorted.isEmpty())
            return aSortedTrainees ;

        // Fill and sort
        //
        aSortedTrainees.addAll(aUnsorted) ;

        Comparator<TraineeData> lastTraineeNameComparator = new Comparator<TraineeData>()
        {
            @Override
            public int compare(TraineeData left, TraineeData right) {
                return getSortableLastName(CoachingFitSupervisor.getLastTraineeName(left)).compareTo(getSortableLastName(CoachingFitSupervisor.getLastTraineeName(right))) ;
            }
        };

        Collections.sort(aSortedTrainees, lastTraineeNameComparator) ;

        return aSortedTrainees ;
    }

    /**
     * Get user's last name. For example "Pierre DUPONT" returns "DUPONT"
     * 
     * @param userData
     * @return
     */
    public static String getLastName(UserData userData)
    {
        if (null == userData)
            return "" ;

        return getLastName(userData.getLabel()) ;
    }

    /**
     * Get user's last name. For example "Pierre DUPONT" returns "DUPONT"
     * 
     * @param userData
     * @return
     */
    public static String getLastName(final String sPseudo)
    {
        if ((null == sPseudo) || sPseudo.isEmpty())
            return "" ;

        int iFirstBlanck = sPseudo.indexOf(' ') ;
        if (-1 == iFirstBlanck)
            return sPseudo ;

        return sPseudo.substring(iFirstBlanck, sPseudo.length()).trim() ;
    }

    /**
     * Get trainee's last name. For example "Pierre DUPONT" returns "DUPONT"
     * 
     * @param userData
     * @return
     */
    public static String getLastTraineeName(TraineeData traineeData)
    {
        String sPseudo = traineeData.getLabel() ;
        String sFirstName = traineeData.getFirstName() ;

        int iFirstNameLen = sFirstName.length() ;
        if ((iFirstNameLen > 0) && (iFirstNameLen < sPseudo.length()))
            return sPseudo.substring(iFirstNameLen, sPseudo.length()).trim() ;

        int iFirstBlanck = sPseudo.indexOf(' ') ;
        if (-1 == iFirstBlanck)
            return sPseudo ;

        return sPseudo.substring(iFirstBlanck, sPseudo.length()).trim() ;
    }

    /**
     * Get user's sortable last name part. For example "du TRAIN" returns "TRAIN"
     * 
     * @param userData
     * @return
     */
    public static String getSortableLastName(final String sLastName)
    {
        if ((null == sLastName) || "".equals(sLastName))
            return "" ;

        int iLen = sLastName.length() ;

        int i = 0 ;
        for ( ; i < iLen ; i++)
        {
            char c = sLastName.charAt(i) ;
            if ((' ' != c) && Character.isUpperCase(c))
                break ;
        }

        if ((0 == i) || (iLen == i))
            return sLastName ;

        return sLastName.substring(i).trim() ;
    }
}
