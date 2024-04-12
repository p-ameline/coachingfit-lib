package com.coachingfit.client.widgets ;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Model of search trait block
 */
public class SearchCheckboxPanel extends FlowPanel implements HasClickHandlers
{
    // private final HTMLPanel _label ;
    private final CheckBox  _checkBox ;

    /**
     * Default Constructor
     */
    public SearchCheckboxPanel(final String sStyle, final String sLabel)
    {
        super() ;
        setStyleName(sStyle);

        String _labelId = sLabel.toLowerCase().replace(' ', '_') ;
        //add(_label) ;

        _checkBox = new CheckBox(sLabel) ;
        _checkBox.setStyleName("form-control") ;
        _checkBox.getElement().setAttribute("id", _labelId) ;
        _checkBox.getElement().setAttribute("placeholder", sLabel) ;
        add(_checkBox) ;

        // _label = new HTMLPanel("label", sLabel) ;
        // _label.setStyleName("form-label");
        // _label.getElement().setAttribute("for", _labelId);
        // add(_label) ;
    }

    /**
     * To be implemented by derived classes
     */
    public void reset() {
        _checkBox.setValue(false) ;
    }

    /**
     * Initialize the text box from a text
     */
    public void setValue(final boolean bChecked) {
        _checkBox.setValue(bChecked) ;
    }

    /**
     * Get a text from text box content
     */
    public boolean getValue() {
        return _checkBox.getValue() ;
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType()) ;
    }

    // public HTMLPanel getLabel() {
    //     return _label ;
    // }

    public CheckBox getCheckBox() {
        return _checkBox ;
    }
}
