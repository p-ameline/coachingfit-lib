package com.coachingfit.server.handler;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.GetTraineesListAction;
import com.coachingfit.shared.rpc.GetTraineesListResult;
import com.coachingfit.shared.rpc_util.TraineesSearchTrait;
import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Get an array of trainees information from a set of traits
 * 
 * @author Philippe
 */
public class GetTraineesListHandler extends GetFormsHandlerBase implements ActionHandler<GetTraineesListAction, GetTraineesListResult> 
{	
    protected final Provider<ServletContext>     _servletContext ;
    protected final Provider<HttpServletRequest> _servletRequest ;

    @Inject
    public GetTraineesListHandler(final Provider<ServletContext>     servletContext,
                                  final Provider<HttpServletRequest> servletRequest)
    {
        super() ;

        _servletContext = servletContext ;
        _servletRequest = servletRequest ;
    }

    @Override
    public GetTraineesListResult execute(GetTraineesListAction action, ExecutionContext context) throws ActionException 
    {
        // String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;

        int                 iUserId = action.getUserId() ;
        TraineesSearchTrait traits  = action.getSearchTraits() ;

        // Logger.trace("GetTraineesListHandler: looking for " + aTraineesIds.size() + " trainee(s)", iUserId, Logger.TraceLevel.STEP) ;

        if ((null == traits) || traits.isEmpty())
            return new GetTraineesListResult("server error: empty query", null) ;

        GetTraineesListResult result = new GetTraineesListResult() ;

        List<TraineeData> aTrainees = new ArrayList<>() ;

        if (getTraineesFromTraits(traits, aTrainees, iUserId))
        {
            result.setTrainees(aTrainees) ;
            return result ;
        }

        result.setMessage("server error when getting trainees") ;

        return result ;
    }

    /**
     * Fill a list of {@link TraineeData} from a set of traits
     * 
     * @param aTraineesIds List of identifiers
     * @param aTrainees    List of trainees to be filled
     * @param iUserId      User identifier, for tracing purposes
     * 
     * @return <code>true</code> if all went well, <code>false</code> if not
     */
    private boolean getTraineesFromTraits(final TraineesSearchTrait traits, List<TraineeData> aTrainees, int iUserId)
    {
        if ((null == aTrainees) || (null == traits))
            return false ;
        if (traits.isEmpty())
            return true ;

        DBConnector dbConnector = new DBConnector(false) ;
        TraineeDataManager traineesManager = new TraineeDataManager(iUserId, dbConnector) ;

        // Get initial list
        //
        List<TraineeData> aDbTrainees = new ArrayList<>() ;

        if (traits.getRegionId() > 0)
            traineesManager.fillTraineesForRegion(aDbTrainees, traits.getRegionId()) ;
        else if (traits.getCoachId() > 0)
            traineesManager.fillTraineesForCoach(iUserId, aDbTrainees, traits.getCoachId()) ;
        else
            traineesManager.getThemAll(aDbTrainees) ;

        traits.setFirstName(traits.getFirstName().toUpperCase()) ;
        traits.setLastName(traits.getLastName().toUpperCase()) ;
        traits.setCategory(traits.getCategory().toUpperCase()) ;

        for (TraineeData trainee : aDbTrainees)
        {
            if (validTraits(trainee, traits))
                aTrainees.add(trainee) ;
        }

        return true ;
    }

    private boolean validTraits(final TraineeData trainee, final TraineesSearchTrait traits)
    {
        if ((null == trainee) || trainee.isEmpty() || (null == traits))
            return false ;

        if (traits.isEmpty())
            return true ;

        Collator insenstiveStringComparator = Collator.getInstance();
        insenstiveStringComparator.setStrength(Collator.PRIMARY);

        if ((traits.getRegionId() > 0) && (trainee.getRegionId() != traits.getRegionId()))
            return false ;
        if ((traits.getCoachId() > 0) && (trainee.getCoachId() != traits.getCoachId()))
            return false ;

        if ((traits.isToBeActive()) && (trainee.getCoachId() < 1))
            return false ;
        
        if (false == traits.getFirstName().isEmpty())
        {
            String sTraineeFirstName = trainee.getFirstName().toUpperCase() ;
            if (false == startsWith(sTraineeFirstName, traits.getFirstName(), insenstiveStringComparator))
                return false ;
        }

        if (false == traits.getLastName().isEmpty())
        {
            String sLastName = trainee.getLastName().toUpperCase() ;

            if (false == startsWith(sLastName, traits.getLastName(), insenstiveStringComparator))
                return false ;
        }

        if (false == traits.getCategory().isEmpty())
        {
            if (false == trainee.getJobType().toUpperCase().startsWith(traits.getCategory()))
                return false ;
        }

        return true ;
    }

    /**
     * Does a start with b (comparing accentuated and non-accentuated letters as identical)
     */
    public boolean startsWith(String a, String b, Collator comparator)
    {
        if ((null == a) || (null == b))
            return false ;

        if ((b.isEmpty()) || (a.length() < b.length()))
            return false ;

        return isSame(a.substring(0, b.length()), b, comparator) ;
    }

    /**
     * Compare accentuated and non-accentuated letters as identical
     */
    public boolean isSame(String a, String b, Collator comparator) {
        return comparator.compare(a, b) == 0 ;
    }

    @Override
    public Class<GetTraineesListAction> getActionType() {
        return GetTraineesListAction.class;
    }

    @Override
    public void rollback(GetTraineesListAction action, GetTraineesListResult result,
            ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub
    }
}
