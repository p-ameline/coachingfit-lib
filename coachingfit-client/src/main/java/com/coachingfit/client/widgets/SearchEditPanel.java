package com.coachingfit.client.widgets ;

import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox ;

/**
 * Model of search trait block
 */
public class SearchEditPanel extends FlowPanel implements HasKeyUpHandlers
{
	private final HTMLPanel _label ;
	private final TextBox   _textBox ;

	/**
	 * Default Constructor
	 */
	public SearchEditPanel(final String sStyle, final String sLabel)
	{
		super() ;
		setStyleName(sStyle);

		String _labelId = sLabel.toLowerCase().replace(' ', '_') ;
		//add(_label) ;

		_textBox = new TextBox() ;
		_textBox.setStyleName("form-control") ;
		_textBox.getElement().setAttribute("id", _labelId) ;
		_textBox.getElement().setAttribute("placeholder", sLabel) ;
		add(_textBox) ;

		_label = new HTMLPanel("label", sLabel) ;
		_label.setStyleName("form-label");
		_label.getElement().setAttribute("for", _labelId);
		add(_label) ;
	}

	/**
	 * To be implemented by derived classes
	 */
	public void reset() {
		_textBox.setText("") ;
	}

	/**
	 * Initialize the text box from a text
	 */
	public void setText(final String sText)
	{
		if (null == sText)
		{
			reset() ;
			return ;
		}
		_textBox.setText(sText) ;
	}

	/**
	 * Get a text from text box content
	 */
	public String getText() {
		return _textBox.getText() ;
	}
	
	@Override
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return addDomHandler(handler, KeyUpEvent.getType()) ;
	}
	
	public HTMLPanel getLabel() {
		return _label ;
	}
	
	public TextBox getTextBox() {
		return _textBox ;
	}
}
