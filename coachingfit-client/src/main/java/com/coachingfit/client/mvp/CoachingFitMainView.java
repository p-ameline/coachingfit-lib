package com.coachingfit.client.mvp;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class CoachingFitMainView extends Composite implements CoachingFitMainPresenter.Display 
{
	//private final TextBox name;
	private FlowPanel body ;
	private FlowPanel header ;
	private FlowPanel workspace ;
	private FlowPanel footer ;

	public CoachingFitMainView() 
	{
		body = new FlowPanel() ;
		body.addStyleName("ecogenBody") ;
		initHeader() ; 		
		initWorkspace() ;
		initMainFooter() ; 
		initWidget(body) ;
	}	
	
	public void initHeader()
	{
		header = new FlowPanel() ;
		header.add(new Label("header")) ;
		header.addStyleName("header") ;
		body.add(header) ;
	}
		 	 
	public void initWorkspace()
	{
		workspace = new FlowPanel();
		workspace.addStyleName("workspace");
		workspace.add(new Label("workspace"));
		body.add(workspace) ;
	}
		
	public void initMainFooter()
	{
		HTML footerContent = new HTML("") ;
		footer = new FlowPanel();
		footer.addStyleName("footer");
		footer.add(footerContent);
		body.add(footer); 
		body.add(footer) ;
	}
	
	public FlowPanel getHeader() {
		return header ;
	}
	public FlowPanel getWorkspace() {
		return workspace ;
	}
	public FlowPanel getFooter() {
		return footer ;
	}

	public void reset() {
		// Focus the cursor on the name field when the app loads
	}
	
	/**
	 * Returns this widget as the {@link WidgetDisplay#asWidget()} value.
	 */
	public Widget asWidget() {
		return this;
	}
}
