package com.coachingfit.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface ScoreModifiedEventHandler extends EventHandler 
{
	void onEditEncounter(ScoreModifiedEvent event) ;
}