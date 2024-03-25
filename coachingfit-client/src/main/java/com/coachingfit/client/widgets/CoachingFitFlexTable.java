package com.coachingfit.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * A specifically tuned FlexTable
 */
public class CoachingFitFlexTable extends FlexTable
{
	/**
	 * Default Constructor
	 *
	 */
	public CoachingFitFlexTable() {
		super() ;
	}

	/**
	 * Return value for {@link CoachingFitFlexTable#getCellForDoubleClickEvent}.
	 */
	public class DoubleClickCell
	{
		private final int rowIndex;
		private final int cellIndex;

		/**
		 * Creates a cell.
		 * 
		 * @param rowIndex the cell's row
		 * @param cellIndex the cell's index
		 */
		protected DoubleClickCell(int rowIndex, int cellIndex) {
			this.cellIndex = cellIndex;
			this.rowIndex = rowIndex;
		}

		/**
		 * Gets the cell index.
		 * 
		 * @return the cell index
		 */
		public int getCellIndex() {
			return cellIndex;
		}

		/**
		 * Gets the cell's element.
		 * 
		 * @return the cell's element.
		 */
		public com.google.gwt.user.client.Element getElement() {
			return DOM.asOld(getCellFormatter().getElement(rowIndex, cellIndex));
		}

		/**
		 * Get row index.
		 * 
		 * @return the row index
		 */
		public int getRowIndex() {
			return rowIndex;
		}
	}

	/**
	 * Given a double click event, return the DoubleClickCell that was clicked, or null if the event
	 * did not hit this table.  The cell can also be null if the click event does
	 * not occur on a specific cell.
	 * 
	 * @param event A double click event of indeterminate origin
	 * @return The appropriate cell, or null
	 */
	public DoubleClickCell getCellForDoubleClickEvent(DoubleClickEvent event)
	{
		Element td = getEventTargetCell(Event.as(event.getNativeEvent())) ;
		if (td == null) {
			return null ;
		}

		int row = TableRowElement.as(td.getParentElement()).getSectionRowIndex() ;
		int column = TableCellElement.as(td).getCellIndex() ;
		return new DoubleClickCell(row, column) ;
	}
}
