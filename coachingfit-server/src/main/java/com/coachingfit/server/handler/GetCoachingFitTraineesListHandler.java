package com.coachingfit.server.handler;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.coachingfit.server.model.TraineeDataManager;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesListAction;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesListResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.primege.server.DBConnector;
import com.primege.server.Logger;
import com.primege.server.handler.GetFormsHandlerBase;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Get an array of trainees information from a list of trainees IDs 
 * 
 * @author Philippe
 */
public class GetCoachingFitTraineesListHandler extends GetFormsHandlerBase implements ActionHandler<GetCoachingFitTraineesListAction, GetCoachingFitTraineesListResult> 
{	
    protected final Provider<ServletContext>     _servletContext ;
    protected final Provider<HttpServletRequest> _servletRequest ;

    @Inject
    public GetCoachingFitTraineesListHandler(final Provider<ServletContext>     servletContext,
            final Provider<HttpServletRequest> servletRequest)
    {
        super() ;

        _servletContext = servletContext ;
        _servletRequest = servletRequest ;
    }

    @Override
    public GetCoachingFitTraineesListResult execute(GetCoachingFitTraineesListAction action, ExecutionContext context) throws ActionException 
    {
        // String sFctName = "GetCoachingFitTraineeInformationHandler.execute" ;

        int iUserId                = action.getUserId() ;
        List<Integer> aTraineesIds = action.getTraineesIds() ;

        Logger.trace("GetCoachingFitTraineesListHandler: looking for " + aTraineesIds.size() + " trainee(s)", iUserId, Logger.TraceLevel.STEP) ;

        if ((null == aTraineesIds) || aTraineesIds.isEmpty())
            return new GetCoachingFitTraineesListResult("server error: empty query", null) ;

        GetCoachingFitTraineesListResult result = new GetCoachingFitTraineesListResult() ;

        if (getTraineesFromList(aTraineesIds, result.getTraineesData(), iUserId))
            return result ;

        result.setMessage("server error when getting trainees") ;

        return result ;
    }

    /**
     * Fill a list of {@link TraineeData} from a list of identifiers
     * 
     * @param aTraineesIds List of identifiers
     * @param aTrainees    List of trainees to be filled
     * @param iUserId      User identifier, for tracing purposes
     * 
     * @return <code>true</code> if all went well, <code>false</code> if not
     */
    public static boolean getTraineesFromList(List<Integer> aTraineesIds, List<TraineeData> aTrainees, int iUserId)
    {
        if ((null == aTraineesIds) || (null == aTrainees))
            return false ;
        if (aTraineesIds.isEmpty())
            return true ;

        DBConnector dbConnector = new DBConnector(false) ;
        TraineeDataManager traineesManager = new TraineeDataManager(iUserId, dbConnector) ;

        for (Integer iTraineeId : aTraineesIds)
        {
            TraineeData traineeData = new TraineeData() ;

            if (traineesManager.existData(iTraineeId, traineeData) && (false == aTrainees.contains(traineeData)))
                aTrainees.add(traineeData) ;
        }

        return true ;
    }

    @Override
    public Class<GetCoachingFitTraineesListAction> getActionType() {
        return GetCoachingFitTraineesListAction.class;
    }

    @Override
    public void rollback(GetCoachingFitTraineesListAction action, GetCoachingFitTraineesListResult result,
            ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub
    }
}
