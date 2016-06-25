package com.subterranean_security.crimson.viewer.ui.screen.users.ep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.subterranean_security.crimson.core.proto.Misc.Outcome;
import com.subterranean_security.crimson.core.proto.Users.ViewerPermissions;
import com.subterranean_security.crimson.core.ui.StatusLabel;
import com.subterranean_security.crimson.sv.profile.ViewerProfile;
import com.subterranean_security.crimson.viewer.net.ViewerCommands;
import com.subterranean_security.crimson.viewer.ui.UIUtil;
import com.subterranean_security.crimson.viewer.ui.common.panels.epanel.EPanel;

public class EditUser extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private StatusLabel sl;
	private JCheckBox chckbxGenerator;
	private JCheckBox chckbxListenerCreation;
	private JCheckBox chckbxServerPower;
	private JCheckBox chckbxServerSettings;
	private JCheckBox chckbxServerFilesystemRead;
	private JCheckBox chckbxServerFilesystemWrite;
	private JCheckBox chckbxSuperuser;
	private JPasswordField fld_old;
	private JPasswordField fld_new;
	private JPasswordField fld_retype;

	private ViewerProfile original;
	private EPanel ep;

	public EditUser(EPanel ep, ViewerProfile original) {
		this.ep = ep;
		this.original = original;
		init();
	}

	public void init() {

		setBounds(100, 100, 658, 245);
		setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setPreferredSize(new Dimension(430, 185));
		contentPanel.add(panel_1);
		panel_1.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(8, 6, 211, 113);
		panel_1.add(panel);
		panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "User Information",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel.setLayout(null);

		fld_old = new JPasswordField();
		fld_old.setBounds(100, 24, 99, 19);
		panel.add(fld_old);

		fld_new = new JPasswordField();
		fld_new.setBounds(100, 48, 99, 19);
		panel.add(fld_new);

		fld_retype = new JPasswordField();
		fld_retype.setBounds(100, 72, 99, 19);
		panel.add(fld_retype);

		JLabel lblOldPassword = new JLabel("Old Password:");
		lblOldPassword.setFont(new Font("Dialog", Font.BOLD, 10));
		lblOldPassword.setBounds(8, 26, 87, 15);
		panel.add(lblOldPassword);

		JLabel lblNewPassword = new JLabel("New Password:");
		lblNewPassword.setFont(new Font("Dialog", Font.BOLD, 10));
		lblNewPassword.setBounds(8, 50, 87, 15);
		panel.add(lblNewPassword);

		JLabel lblRetype = new JLabel("Retype:");
		lblRetype.setFont(new Font("Dialog", Font.BOLD, 10));
		lblRetype.setBounds(8, 74, 87, 15);
		panel.add(lblRetype);
		{
			JPanel panel_1_1 = new JPanel();
			panel_1_1.setBounds(225, 6, 197, 170);
			panel_1.add(panel_1_1);
			panel_1_1.setBorder(
					new TitledBorder(null, "Permissions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel_1_1.setLayout(null);

			chckbxGenerator = new JCheckBox("Generator");
			chckbxGenerator.setSelected(original.getPermissions().getGenerate());
			chckbxGenerator.setFont(new Font("Dialog", Font.BOLD, 10));
			chckbxGenerator.setBounds(8, 40, 181, 20);
			panel_1_1.add(chckbxGenerator);

			chckbxListenerCreation = new JCheckBox("Listener Creation");
			chckbxListenerCreation.setSelected(original.getPermissions().getCreateListener());
			chckbxListenerCreation.setFont(new Font("Dialog", Font.BOLD, 10));
			chckbxListenerCreation.setBounds(8, 60, 181, 20);
			panel_1_1.add(chckbxListenerCreation);

			chckbxServerPower = new JCheckBox("Server Power");
			chckbxServerPower.setSelected(original.getPermissions().getServerPower());
			chckbxServerPower.setFont(new Font("Dialog", Font.BOLD, 10));
			chckbxServerPower.setBounds(8, 80, 181, 20);
			panel_1_1.add(chckbxServerPower);

			chckbxServerSettings = new JCheckBox("Server Settings");
			chckbxServerSettings.setSelected(original.getPermissions().getServerSettings());
			chckbxServerSettings.setFont(new Font("Dialog", Font.BOLD, 10));
			chckbxServerSettings.setBounds(8, 100, 181, 20);
			panel_1_1.add(chckbxServerSettings);

			chckbxServerFilesystemRead = new JCheckBox("Server Filesystem Read");
			chckbxServerFilesystemRead.setSelected(original.getPermissions().getServerFsRead());
			chckbxServerFilesystemRead.setFont(new Font("Dialog", Font.BOLD, 10));
			chckbxServerFilesystemRead.setBounds(8, 120, 181, 20);
			panel_1_1.add(chckbxServerFilesystemRead);

			chckbxServerFilesystemWrite = new JCheckBox("Server Filesystem Write");
			chckbxServerFilesystemWrite.setSelected(original.getPermissions().getServerFsWrite());
			chckbxServerFilesystemWrite.setFont(new Font("Dialog", Font.BOLD, 10));
			chckbxServerFilesystemWrite.setBounds(8, 140, 181, 20);
			panel_1_1.add(chckbxServerFilesystemWrite);

			chckbxSuperuser = new JCheckBox("Superuser");
			chckbxSuperuser.setBounds(8, 20, 181, 20);
			panel_1_1.add(chckbxSuperuser);
			chckbxSuperuser.setSelected(original.getPermissions().getSuper());
			chckbxSuperuser.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent arg0) {
					sl.setInfo("Grant all privileges on server and clients");
				}

				@Override
				public void mouseExited(MouseEvent e) {
					sl.setDefault();
				}
			});
			chckbxSuperuser.setFont(new Font("Dialog", Font.BOLD, 10));
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setMargin(new Insets(2, 4, 2, 4));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ep.drop();
					}
				});
				cancelButton.setFont(new Font("Dialog", Font.BOLD, 10));
				buttonPane.add(cancelButton);
			}
			{
				JButton okButton = new JButton("Edit");
				okButton.setMargin(new Insets(2, 8, 2, 8));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if (!verify()) {
							return;
						}
						sl.setInfo("Applying changes...");
						new SwingWorker<Outcome, Void>() {

							@Override
							protected Outcome doInBackground() throws Exception {
								ViewerPermissions vp = ViewerPermissions.newBuilder()
										.setSuper(chckbxSuperuser.isSelected())
										.setGenerate(chckbxGenerator.isSelected())
										.setCreateListener(chckbxListenerCreation.isSelected())
										.setServerPower(chckbxServerPower.isSelected())
										.setServerSettings(chckbxServerSettings.isSelected())
										.setServerFsRead(chckbxServerFilesystemRead.isSelected())
										.setServerFsWrite(chckbxServerFilesystemWrite.isSelected()).build();
								String oldPass = UIUtil.getPassword(fld_old);
								String newPass = UIUtil.getPassword(fld_new);
								return ViewerCommands.editUser(original.getUser(), oldPass.isEmpty() ? null : oldPass,
										oldPass.isEmpty() ? null : newPass, vp);
							}

							protected void done() {
								try {
									Outcome outcome = get();
									if (outcome.getResult()) {
										sl.setGood("Success!");
										new SwingWorker<Void, Void>() {
											@Override
											protected Void doInBackground() throws Exception {
												Thread.sleep(700);
												return null;
											}

											protected void done() {
												ep.drop();
											};

										}.execute();

									} else {
										if (outcome.hasComment()) {
											sl.setBad("Failed: " + outcome.getComment());
										} else {
											sl.setBad("Failed to edit user!");
										}

									}
								} catch (InterruptedException | ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							};
						}.execute();

					}
				});
				okButton.setFont(new Font("Dialog", Font.BOLD, 10));
				buttonPane.add(okButton);
			}
		}

		sl = new StatusLabel("Editing user: " + original.getUser());
		add(sl, BorderLayout.NORTH);
	}

	private boolean verify() {

		return true;

	}
}
