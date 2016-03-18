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
package com.subterranean_security.crimson.viewer.ui.screen.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.subterranean_security.crimson.core.storage.Headers;
import com.subterranean_security.crimson.sv.ClientProfile;
import com.subterranean_security.crimson.viewer.ViewerStore;

public class HostList extends JPanel {

	private static final long serialVersionUID = 1L;

	private static JTable table = new JTable();
	private TM tm = new TM();
	private TR tr = new TR();

	public HostList() {
		setLayout(new BorderLayout());
		table.setModel(tm);
		table.setDefaultRenderer(Object.class, tr);
		table.setFillsViewportHeight(true);
		table.setShowVerticalLines(false);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// get source of click
				JTable source = (JTable) e.getSource();
				final int sourceRow = source.rowAtPoint(e.getPoint());
				if (sourceRow == -1) {
					source.clearSelection();
					MainFrame.main.dp.closeDetail();
					return;
				}
				// select row
				if (!source.isRowSelected(sourceRow)) {
					source.changeSelection(sourceRow, 0, false, false);
				}
				final ClientProfile sp = ViewerStore.Profiles.profiles.get(sourceRow);

				if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {

					// right click on table
					JPopupMenu popup = new JPopupMenu();
					JMenuItem control = new JMenuItem();
					control.setText("Control Panel");
					control.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {

							new Thread() {
								public void run() {

								}
							}.start();

						}

					});
					popup.add(control);

					JMenu quick = new JMenu();
					quick.setText("Quick Commands");
					popup.add(quick);

					JMenuItem poweroff = new JMenuItem();
					poweroff.setText("Shutdown");
					poweroff.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {

							new Thread() {
								public void run() {

								}
							}.start();

						}

					});
					quick.add(poweroff);

					JMenuItem restart = new JMenuItem();
					restart.setText("Restart");
					restart.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {

							new Thread() {
								public void run() {

								}
							}.start();

						}

					});
					quick.add(restart);

					JMenuItem refresh = new JMenuItem();
					refresh.setText("Refresh Information");
					refresh.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {

							new Thread() {
								public void run() {

								}
							}.start();

						}

					});
					quick.add(refresh);

					popup.show(table, e.getX(), e.getY());

				} else {
					// open up the detail
					MainFrame.main.dp.showDetail(sp);
				}
			}
		});

		JScrollPane jsp = new JScrollPane(table);

		add(jsp, BorderLayout.CENTER);
	}

	public void refreshTM() {
		tm.refreshHeaders();
		tm.fireTableStructureChanged();
	}
}

class TM extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	Headers[] headers = new Headers[] {};

	public TM() {
		refreshHeaders();
	}

	public void refreshHeaders() {
		try {
			headers = (Headers[]) ViewerStore.Databases.local.getObject("hostlist.headers");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int getColumnCount() {
		return headers.length;
	}

	@Override
	public int getRowCount() {
		return ViewerStore.Profiles.profiles.size();
	}

	@Override
	public String getColumnName(int column) {
		return headers[column].toString();
	};

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (headers[columnIndex]) {
		case USERNAME: {
			return ViewerStore.Profiles.profiles.get(rowIndex).getUsername();
		}
		case USER_STATUS: {

		}
		case HOSTNAME: {
			return ViewerStore.Profiles.profiles.get(rowIndex).getHostname();
		}
		case INTERNAL_IP: {

		}
		case EXTERNAL_IP: {

		}
		case LANGUAGE: {
			return ViewerStore.Profiles.profiles.get(rowIndex).getLanguage();
		}
		case ACTIVE_WINDOW: {

		}
		case COUNTRY: {

		}
		case CPU_MODEL: {

		}
		case CPU_USAGE: {

		}
		case CPU_TEMP: {

		}
		case RAM_CAPACITY: {

		}
		case RAM_USAGE: {

		}
		case CRIMSON_VERSION: {

		}
		case OS_FAMILY: {

		}
		case OS_ARCH: {

		}
		case JAVA_VERSION: {

		}
		case MONITOR_COUNT: {

		}
		case VIRTUALIZATION: {

		}
		case TIMEZONE: {
			return ViewerStore.Profiles.profiles.get(rowIndex).getTimezone();
		}

		}
		return null;
	}

}

class TR extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setBorder(noFocusBorder);
		return this;
	}

	protected void setValue(Object value) {
		if (value instanceof ImageIcon) {
			ImageIcon ico = (ImageIcon) value;
			setIcon(ico);
			setText(ico.getDescription());
		} else {
			setIcon(null);
			super.setValue(value);
		}
	}

}
