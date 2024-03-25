package com.coachingfit.client.event ;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.FlowPanel ;

/**
 * Event sent to the ApnolabSingleDayManagerPresenter object to bootstrap it
 */
public class TraineesListEvent extends GwtEvent<TraineesListEventHandler>
{
    public static Type<TraineesListEventHandler> TYPE = new Type<TraineesListEventHandler>() ;

    private FlowPanel _workspace ;

    public static Type<TraineesListEventHandler> getType()
    {
        if (null == TYPE)
            TYPE = new Type<TraineesListEventHandler>() ;
        return TYPE ;
    }

    public TraineesListEvent(FlowPanel flowPanel)
    {
        _workspace = flowPanel ;
    }

    public FlowPanel getWorkspace()
    {
        return _workspace ;
    }

    @Override
    protected void dispatch(TraineesListEventHandler handler) {
        handler.onTraineesList(this) ;
    }

    @Override
    public Type<TraineesListEventHandler> getAssociatedType() {
        return TYPE ;
    }
}
