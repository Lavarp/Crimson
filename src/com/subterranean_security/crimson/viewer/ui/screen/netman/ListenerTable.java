/******************************************************************************
 *                                                                            *
 *                    Copyright 2016 Subterranean Security                    *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *      http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 *                                                                            *
 *****************************************************************************/
package com.subterranean_security.crimson.viewer.ui.screen.netman;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.subterranean_security.crimson.sv.Listener;
import com.subterranean_security.crimson.viewer.ViewerStore;

public class ListenerTable extends JScrollPane {

	private static final long serialVersionUID = 1L;
	private JTable table = new JTable();
	private TM tm = new TM();

	public ListenerTable() {
		table.setModel(tm);
		table.setFillsViewportHeight(true);
		table.setShowVerticalLines(false);
		setViewportView(table);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// get source of click
				JTable source = (JTable) e.getSource();
				final int sourceRow = source.rowAtPoint(e.getPoint());
				if (sourceRow == -1) {
					source.clearSelection();
					return;
				}
				// select row
				if (!source.isRowSelected(sourceRow)) {
					source.changeSelection(sourceRow, 0, false, false);
				}

				if (e.getButton() == MouseEvent.BUTTON3) {
					Listener selected = tm.getAt(sourceRow);
					// right click

				}
			}
		});
	}

}

class TM extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private final String[] headers = new String[] { "Name", "Port", "UPnP", "Owner" };

	@Override
	public int getColumnCount() {
		return headers.length;
	}

	@Override
	public int getRowCount() {
		return ViewerStore.Listeners.listeners.size();
	}

	@Override
	public String getColumnName(int column) {
		return headers[column];
	};

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		switch (headers[columnIndex]) {
		case "Name": {
			return ViewerStore.Listeners.listeners.get(rowIndex).name;
		}
		case "Port": {
			return "" + ViewerStore.Listeners.listeners.get(rowIndex).port;
		}
		case "UPnP": {
			return ViewerStore.Listeners.listeners.get(rowIndex).upnp ? "true" : "false";
		}

		}
		return null;
	}

	public Listener getAt(int row) {
		return ViewerStore.Listeners.listeners.get(row);
	}

	public void removeAt(int row) {
		ViewerStore.Listeners.listeners.remove(row);
	}

}