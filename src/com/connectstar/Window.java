/*  Copyright (C) 2014 by Oduah Tobi
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *	
 *	The above copyright notice and this permission notice shall be included in
 *	all copies or substantial portions of the Software.
 *	
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *	THE SOFTWARE.
 */
package com.connectstar;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;

public class Window implements ActionListener, ItemListener{

	private JFrame frame;
	private JTextField timeText;
	private String[] connections;
	private String[] timeString = {"Seconds", "Minutes", "Hours"};
	private JComboBox profileCombo;
	private JRadioButton autoRadio;
	private JRadioButton manualRadio;
	private JComboBox timeCombo;
	private JButton startButton;
	private JLabel timeLabel;
	private JLabel timerLabel;
	private JTextField connectionText;
	private String connectionName, timeValue;
	private JButton disconnectButton;
	private JButton connectButton;
	private Timer connectionTimer;
	private int timerHour, timerMinute, timerSecond;
	private int origHour, origMinute, origSecond;
	private Preferences prefs;
	private JLabel lblOduahtgmailcom;

	public void itemStateChanged(ItemEvent ev){
		if (ev.getSource().equals(timeCombo)){
			switch(timeCombo.getSelectedIndex()){
			case 0:
				timeLabel.setText("Seconds:");
				break;
			case 1:
				timeLabel.setText("Minutes:");
				break;
			case 2:
				timeLabel.setText("Hours:");
				break;
			}
		}else if (ev.getSource() == autoRadio){
			if (autoRadio.isSelected()){
				timeCombo.setEnabled(true);
				timeText.setEnabled(true);
				startButton.setEnabled(true);
			}else{
				timeCombo.setEnabled(false);
				timeText.setEnabled(false);
				startButton.setEnabled(false);
			}
		}else if (ev.getSource() == manualRadio){
			if (manualRadio.isSelected()){
				disconnectButton.setEnabled(true);
				connectButton.setEnabled(true);
				if (connectionTimer != null){
					if (connectionTimer.isRunning())
						connectionTimer.stop();
				}
				timerLabel.setText("00:00:00");
			}else{
				disconnectButton.setEnabled(false);
				connectButton.setEnabled(false);
			}
		}
	}
	public void actionPerformed(ActionEvent ev){
		if (ev.getSource().equals(startButton)){
			if (connections.length == 0){
				connectionName = connectionText.getText().trim();
			}else{
				connectionName = connections[profileCombo.getSelectedIndex()];
			}
			String timeField = timeText.getText().trim();
			if (!verifyInput(connectionName, timeField))
				return;
			int timeInt = Integer.parseInt(timeField);
			if (timeInt < 0){
				JOptionPane.showMessageDialog(frame, "Only positive values allowed", "Integer error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			startButton.setEnabled(false);
			saveSettings(connectionName, timeCombo.getSelectedIndex(), timeField);
			timerHour = 0; timerMinute = 0; timerSecond = 0;
			switch(timeCombo.getSelectedIndex()){
				case 0:
					if (timeInt >= 60){
						if (timeInt >= 3600){
							timerHour = timeInt / 3600;
							int remnant = timeInt % 3600;
							if (remnant >= 60){
								origMinute = timerMinute = remnant / 60;
								origSecond = timerSecond = remnant % 60;
							}else{
								timerSecond = remnant;
							}
						}else{
							timerMinute = timeInt / 60;
							timerSecond = timeInt % 60;
						}
					}else{
						timerSecond = timeInt;
					}
					break;
				case 1:
					timeInt = 60 * timeInt;
					if (timeInt >= 3600){
						timerHour = timeInt / 3600;
						int remnant = timeInt % 3600;
						if (remnant >= 60){
							timerMinute = remnant / 60;
							timerSecond = remnant % 60;
						}else{
							timerSecond = remnant;
						}
					}else{
						timerMinute = timeInt / 60;
						timerSecond = timeInt % 60;
					}
					break;
				case 2:
					timeInt = 60 * 60 * timeInt;
					timerHour = timeInt / 3600;
					int remnant = timeInt % 3600;
					if (remnant >= 60){
						timerMinute = remnant / 60;
						timerSecond = remnant % 60;
					}else{
						timerSecond = remnant;
					}
					break;
			}
			origHour = timerHour; origMinute = timerMinute; origSecond = timerSecond;
			timerLabel.setText(String.format("%02d:%02d:%02d", timerHour, timerMinute, timerSecond));
			if (!Operation.modemDial(2, connectionName)){
				Operation.modemDial(0, connectionName);
			}
			connectionTimer = new Timer(1000, this);
			connectionTimer.start();
		}else if (ev.getSource().equals(connectButton) | ev.getSource().equals(disconnectButton)){
			if (connections.length == 0){
				connectionName = connectionText.getText().trim();
			}else{
				connectionName = connections[profileCombo.getSelectedIndex()];
			}
			if (!verifyInput(connectionName, "0"))
				return;
			if (ev.getSource().equals(connectButton)){
				connectButton.setEnabled(false);
				disconnectButton.setEnabled(false);
				Thread t = new Thread(new Runnable(){
					public void run(){
						Operation.modemDial(0, connectionName);
						disconnectButton.setEnabled(true);
					}
				});
				t.start();
			}
			else{
				connectButton.setEnabled(false);
				disconnectButton.setEnabled(false);
				Thread t = new Thread(new Runnable(){
					public void run(){
						Operation.modemDial(1, connectionName);
						connectButton.setEnabled(true);
					}
				});
				t.start();
			}
		}else if (ev.getSource() == connectionTimer){
			int newSec = timerSecond - 1;
			if (newSec < 0){
				timerSecond = 59;
				timerMinute -= 1;
			}else
				timerSecond -= 1;
			if (timerMinute < 0){
				timerMinute = 59;
				timerHour -= 1;
			}
			if (timerSecond == 0 & timerMinute == 0 & timerHour == 0){
				connectionTimer.stop();
				if (Operation.modemDial(2, connectionName)){
					if (Operation.modemDial(1, connectionName)){   // disconnect modem if connected
						Operation.modemDial(0, connectionName);
					}else{
						Operation.modemDial(0, connectionName);
					}
				}else{
					Operation.modemDial(0, connectionName);
				}
				timerLabel.setText(String.format("%02d:%02d:%02d", timerHour, timerMinute, timerSecond));
				timerHour = origHour; timerMinute = origMinute; timerSecond = origSecond;
				connectionTimer = new Timer(1000, this);
				connectionTimer.start();
				return;
			}
			timerLabel.setText(String.format("%02d:%02d:%02d", timerHour, timerMinute, timerSecond));
		}
	}
	/**
	 * Create the application.
	 */
	public Window() {
		windowsSet();
		connections = getConnections();
		prefs = Preferences.userNodeForPackage(Run.class);
		initialize();
		loadSettings();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	
	public void windowsSet()
	{
		try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }
    }
	
	private void initialize() {
		frame = new JFrame("ConnectStar                                 :::_where");
		frame.getContentPane().setBackground(new Color(60, 179, 113));
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblModemProfile = new JLabel("Modem Profile:");
		lblModemProfile.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblModemProfile.setBounds(54, 37, 97, 25);
		frame.getContentPane().add(lblModemProfile);
		
		autoRadio = new JRadioButton("Auto");
		autoRadio.setOpaque(false);
		autoRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		autoRadio.setBounds(56, 71, 109, 23);
		autoRadio.setSelected(true);
		autoRadio.addItemListener(this);
		frame.getContentPane().add(autoRadio);
		
		manualRadio = new JRadioButton("Manual");
		manualRadio.setOpaque(false);
		manualRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		manualRadio.setBounds(54, 178, 109, 23);
		manualRadio.addItemListener(this);
		frame.getContentPane().add(manualRadio);
		
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(autoRadio);
		radioGroup.add(manualRadio);
		
		timeCombo = new JComboBox(timeString);
		timeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		timeCombo.setBounds(89, 103, 109, 25);
		timeCombo.addItemListener(this);
		frame.getContentPane().add(timeCombo);
		
		timeText = new JTextField();
		timeText.setBounds(304, 104, 63, 25);
		frame.getContentPane().add(timeText);
		timeText.setColumns(10);
		
		timeLabel = new JLabel("Seconds:");
		timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		timeLabel.setBounds(215, 103, 79, 25);
		frame.getContentPane().add(timeLabel);
		
		startButton = new JButton("Start Operation");
		startButton.setOpaque(false);
		startButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		startButton.setBounds(139, 143, 134, 32);
		startButton.addActionListener(this);
		frame.getContentPane().add(startButton);
		
		disconnectButton = new JButton("Disconnect");
		disconnectButton.setOpaque(false);
		disconnectButton.setEnabled(false);
		disconnectButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		disconnectButton.setBounds(112, 208, 109, 32);
		disconnectButton.addActionListener(this);
		frame.getContentPane().add(disconnectButton);
		
		connectButton = new JButton("Connect");
		connectButton.setOpaque(false);
		connectButton.setEnabled(false);
		connectButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		connectButton.setBounds(247, 208, 113, 32);
		connectButton.addActionListener(this);
		frame.getContentPane().add(connectButton);
		
		timerLabel = new JLabel("00:00:00");
		timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
		timerLabel.setBounds(304, 145, 120, 25);
		frame.getContentPane().add(timerLabel);
		
		connectionText = new JTextField();
		connectionText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		connectionText.setColumns(10);
		connectionText.setBounds(168, 38, 188, 25);
		connectionText.setVisible(false);
		connectionText.setToolTipText("Enter the name of your connection profile");
		frame.getContentPane().add(connectionText);
		
		profileCombo = new JComboBox(connections);
		profileCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		profileCombo.setBounds(167, 38, 189, 25);
		if (connections.length != 0){
			profileCombo.setVisible(true);
			connectionText.setVisible(false);
		}
		else{
			profileCombo.setVisible(false);
			connectionText.setVisible(true);
		}
		frame.getContentPane().add(profileCombo);
		
		lblOduahtgmailcom = new JLabel("(C) oduaht@gmail.com");
		lblOduahtgmailcom.setHorizontalAlignment(SwingConstants.CENTER);
		lblOduahtgmailcom.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		lblOduahtgmailcom.setBounds(0, 239, 424, 25);
		frame.getContentPane().add(lblOduahtgmailcom);
	}
	
	public String[] getConnections(){
		String[] directories = new String[2];
		String rasPath = String.format("%sMicrosoft%sNetwork%sConnections%sPbk%srasphone.pbk", 
				File.separator, File.separator, File.separator, File.separator, File.separator, File.separator);
		directories[0] = String.format("%s%s",
				System.getenv("APPDATA"), rasPath);
		directories[1] = String.format("C:%sUsers%sAll Users%s", 
				File.separator, File.separator, rasPath);
		ArrayList<String> connections = new ArrayList<String>();
		for (String string : directories){
			FileInputStream inStream;
			try {
				inStream = new FileInputStream(new File(string));
			}catch (FileNotFoundException e) {
				continue;
			}
			StringBuilder fullConnect = new StringBuilder(10000);
			byte[] buffer = new byte[1024];
			int k;
			try{
				while ((k = inStream.read(buffer)) != -1){
					fullConnect.append(new String(buffer, 0, k));
				}
				inStream.close();
			}catch (IOException e){
			}
			int lastPosit = 0;
			while (true){
				lastPosit = fullConnect.indexOf("[", lastPosit);
				if (lastPosit == -1)
					break;
				int end = fullConnect.indexOf("]", lastPosit);
				connections.add(fullConnect.substring(lastPosit+1, end));
				lastPosit = end;
			}
		}
		String[] connectReturn;
		if (connections.size() > 0){
			connectReturn = new String[connections.size()];
			connections.toArray(connectReturn);
		}else
			connectReturn = new String[0];
		return connectReturn;
	}
	
	public boolean verifyInput(String name, String time){
		if (name.equals("")){
			JOptionPane.showMessageDialog(frame, "Blank connection name", "Invalid name", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		int timeInt = 0;
		try{
			timeInt = Integer.parseInt(time);
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(frame, "Invalid time value entered", "Integer Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	public void saveSettings(String name, int index, String value){
		prefs.put("connection", name);
		prefs.putInt("time", index);
		prefs.put("value", value);
	}
	
	public void loadSettings(){
		String connection = prefs.get("connection", "");
		int index = prefs.getInt("time", 0);
		String value = prefs.get("value", "");
		
		if (connections.length > 0){
			int position = Arrays.binarySearch(connections, connection);
			if (position >= 0)
				profileCombo.setSelectedIndex(position);
			else
				connectionText.setText(connection);
		}else
			connectionText.setText(connection);
		timeCombo.setSelectedIndex(index);
		timeText.setText(value);
	}
}
