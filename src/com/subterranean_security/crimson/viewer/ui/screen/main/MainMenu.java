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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.MenuSelectionManager;
import javax.swing.UIManager;

import com.subterranean_security.crimson.viewer.ui.UICommon;
import com.subterranean_security.crimson.viewer.ui.screen.about.AboutDialog;
import com.subterranean_security.crimson.viewer.ui.screen.files.FMFrame;
import com.subterranean_security.crimson.viewer.ui.screen.files.FMFrame.Type;
import com.subterranean_security.crimson.viewer.ui.screen.generator.GenDialog;
import com.subterranean_security.crimson.viewer.ui.screen.netman.NetMan;
import com.subterranean_security.crimson.viewer.ui.screen.settings.SettingsDialog;
import com.subterranean_security.crimson.viewer.ui.utility.UUtil;

public class MainMenu extends JPanel {

	private static final long serialVersionUID = 1L;
	public ProgressArea progressArea;

	public MainMenu() {
		setForeground(UICommon.bg);
		setBackground(UICommon.bg);

		setLayout(new BorderLayout(0, 0));

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(UIManager.getColor("Button.background"));
		add(menuBar, BorderLayout.CENTER);

		JMenu mnControls = new JMenu("Controls");
		menuBar.add(mnControls);

		MenuControls mc = new MenuControls();
		mnControls.add(mc);

		JMenu mnManagement = new JMenu("Management");
		menuBar.add(mnManagement);

		final WideMenuItem wmFiles = new WideMenuItem(UUtil.getIcon("icons16/general/folder.png"), "Files",
				"Manage Filesystems");
		wmFiles.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				FMFrame fmf = new FMFrame(Type.SV);
				fmf.setVisible(true);

				wmFiles.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnManagement.add(wmFiles);

		WideMenuItem wmNetwork = new WideMenuItem(UUtil.getIcon("icons16/general/computer.png"), "Network",
				"Manage Networking");
		wmNetwork.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				NetMan nm = new NetMan();
				nm.setVisible(true);

				wmNetwork.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnManagement.add(wmNetwork);

		final WideMenuItem wmGen = new WideMenuItem(UUtil.getIcon("icons16/general/linechart.png"), "Generator",
				"Create an Installer");
		wmGen.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				GenDialog gd = new GenDialog();
				gd.setVisible(true);

				wmGen.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnManagement.add(wmGen);

		WideMenuItem wmGlobalControl = new WideMenuItem(UUtil.getIcon("icons16/general/cog.png"), "Control Panel",
				"Global Control Panel");
		wmGlobalControl.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
			}
		});

		mnManagement.add(wmGlobalControl);

		WideMenuItem wmLogs = new WideMenuItem(UUtil.getIcon("icons16/general/error_log.png"), "Logs", "View Crimson Logs");
		wmLogs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
			}
		});

		mnManagement.add(wmLogs);

		WideMenuItem wmUsers = new WideMenuItem(UUtil.getIcon("icons16/general/clients.png"), "Users",
				"Manage Users/Groups");
		wmUsers.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
			}
		});

		mnManagement.add(wmUsers);

		final WideMenuItem wmSettings = new WideMenuItem(UUtil.getIcon("icons16/general/setting_tools.png"), "Settings",
				"Edit Preferences/Settings");
		wmSettings.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO DONT allow multiple instances
				SettingsDialog sd = new SettingsDialog();
				sd.setVisible(true);

				wmSettings.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnManagement.add(wmSettings);

		JMenu mnAbout = new JMenu("About");
		menuBar.add(mnAbout);

		final WideMenuItem wmAbout = new WideMenuItem(UUtil.getIcon("c-16.png"), "About Crimson", "Info on Crimson");
		wmAbout.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO DONT allow multiple instances
				AboutDialog ad = new AboutDialog();
				ad.setVisible(true);

				wmAbout.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnAbout.add(wmAbout);

		final WideMenuItem wmUpgrade = new WideMenuItem(UUtil.getIcon("icons16/general/barcode_2d.png"), "Serial Codes",
				"Add/Remove Keys");
		wmUpgrade.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				wmUpgrade.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnAbout.add(wmUpgrade);

		final WideMenuItem wmHelp = new WideMenuItem(UUtil.getIcon("icons16/general/health.png"), "Help",
				"Open help center");
		wmHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				MainFrame.main.np.addNote("info:Testing the notification area");
				wmHelp.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnAbout.add(wmHelp);

		final WideMenuItem wmTour = new WideMenuItem(UUtil.getIcon("icons16/general/steering_wheel.png"), "Tour",
				"Explore the interface");
		wmTour.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				MainFrame.main.np.addNote("info:Testing the notification area");
				wmTour.resetBG();
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});

		mnAbout.add(wmTour);

		progressArea = new ProgressArea();
		add(progressArea, BorderLayout.EAST);

	}
}
