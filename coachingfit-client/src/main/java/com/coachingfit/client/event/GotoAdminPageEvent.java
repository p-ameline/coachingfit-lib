package com.coachingfit.client.event ;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event sent to the ApnolabSingleDayManagerPresenter object to bootstrap it
 */
public class GotoAdminPageEvent extends GwtEvent<GotoAdminPageEventHandler>
{
    public static Type<GotoAdminPageEventHandler> TYPE = new Type<GotoAdminPageEventHandler>() ;

    public static Type<GotoAdminPageEventHandler> getType()
    {
        if (null == TYPE)
            TYPE = new Type<GotoAdminPageEventHandler>() ;
        return TYPE ;
    }

    @Override
    protected void dispatch(GotoAdminPageEventHandler handler) {
        handler.onGotoAdminPage(this) ;
    }

    @Override
    public Type<GotoAdminPageEventHandler> getAssociatedType() {
        return TYPE ;
    }
}
