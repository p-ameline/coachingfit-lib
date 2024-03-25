package com.coachingfit.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.NotStrict;

/**
 * An object that implements this interface may have validation components.
 *
 * @author lowec
 *
 */
public interface CoachingFitResources extends ClientBundle 
{
	public static final CoachingFitResources INSTANCE =  GWT.create(CoachingFitResources.class) ;

	@NotStrict
  @Source("CoachingFit.css")
  public CssResource css();
	
	@Source("logo_Carlsberg.png")
	public ImageResource welcomeImg() ;

/*
  @Source("config.xml")
  public TextResource initialConfiguration();

  @Source("manual.pdf")
  public DataResource ownersManual();
*/
}
