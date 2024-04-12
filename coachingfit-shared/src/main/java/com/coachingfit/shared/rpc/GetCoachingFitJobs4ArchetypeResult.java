package com.coachingfit.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import com.coachingfit.shared.database.ArchetypeForJobData;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Object that return information from a query for a trainee's information<br>
 * <br>
 * Created: 16 May 2016<br>
 * Author: PA<br>
 * 
 */
public class GetCoachingFitJobs4ArchetypeResult implements Result 
{
    private List<ArchetypeForJobData> _aJobsTypes = new ArrayList<>() ;
    private String                    _sMessage ;

    public GetCoachingFitJobs4ArchetypeResult()
    {
        super() ;
        _sMessage = "" ;
    }

    public List<ArchetypeForJobData> getJobsTypes() {
        return _aJobsTypes  ;
    }

    /**
     * Add a new form to the list
     * 
     * @param formData FormData to add to the list of returned objects
     */
    public void addJobType(ArchetypeForJobData jobData)
    {
        if (null == jobData)
            return ;

        _aJobsTypes.add(jobData) ;
    }

    /**
     * Initialize the array from a model array
     * 
     * @param aJobsTypes Model of ArchetypeForJobData array
     */
    public void initFromJobsTypes(final List<ArchetypeForJobData> aJobsTypes)
    {
        if ((null == aJobsTypes) || aJobsTypes.isEmpty())
        {
            _aJobsTypes.clear() ;
            return ;
        }

        for (ArchetypeForJobData job : aJobsTypes)
            addJobType(job) ;
    }

    public String getMessage() {
        return _sMessage ;
    }
    public void setMessage(final String sMessage) {
        _sMessage = sMessage ;
    }
}
