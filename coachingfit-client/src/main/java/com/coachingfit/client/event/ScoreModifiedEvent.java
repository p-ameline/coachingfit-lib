package com.coachingfit.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ScoreModifiedEvent extends GwtEvent<ScoreModifiedEventHandler> 
{	
	public static Type<ScoreModifiedEventHandler> TYPE = new Type<ScoreModifiedEventHandler>();
	
	public static Type<ScoreModifiedEventHandler> getType() 
	{
		if (null == TYPE)
			TYPE = new Type<ScoreModifiedEventHandler>() ;
		return TYPE ;
	}
	
	public ScoreModifiedEvent()
	{
	}
		
	@Override
	protected void dispatch(ScoreModifiedEventHandler handler) {
		handler.onEditEncounter(this) ;
	}

	@Override
	public Type<ScoreModifiedEventHandler> getAssociatedType() {
		return TYPE ;
	}
}
