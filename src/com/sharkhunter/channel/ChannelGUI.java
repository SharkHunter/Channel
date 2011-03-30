package com.sharkhunter.channel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.pms.PMS;

public class ChannelGUI implements  ActionListener, ItemListener{
	
	private ChannelCfg cfg;
	private Channels root;
	private JTextField chPath;
	private JTextField saPath;
	private JTextField rtmp;
	private JTextField script;
	private JTextField sopcast;
	private JTextField pplive;
	private JTextField perl;
	private JTextField python;
	private JTextField get_flash;
	private JTextField yt;
	private JCheckBox dbg;
	private JCheckBox subs;
	
	public ChannelGUI(ChannelCfg cfg,Channels root) {
		this.cfg=cfg;
		this.root=root;
	}
	
	public JComponent draw() {
		JPanel top=new JPanel(new BorderLayout(10,10));
		JPanel top1=new JPanel(new BorderLayout(10,10));
		JPanel pathPanel=new JPanel(new GridBagLayout());
		JPanel inst=new JPanel();
		JPanel pmsenc=new JPanel(new GridBagLayout());
		JButton channels=new JButton("Install/Update");
		JButton debugPack=new JButton("Pack debug info");
		JButton cBrowse=new JButton("Browse...");
		JButton sBrowse=new JButton("Browse...");
		JButton rBrowse=new JButton("Browse...");
		JButton scBrowse=new JButton("Browse...");
		JButton sopBrowse=new JButton("Browse...");
		JButton ppBrowse=new JButton("Browse...");
		JButton perlBrowse=new JButton("Browse...");
		JButton pytBrowse=new JButton("Browse...");
		JButton get_flBrowse=new JButton("Browse...");
		JButton ytBrowse=new JButton("Browse...");
		JLabel l1=new JLabel("Channels Path: ");
		JLabel l2=new JLabel("Save Path: ");
		JLabel l3=new JLabel("RTMPDump path: ");
		JLabel l4=new JLabel("Scripts path: ");
		JLabel l5=new JLabel("Sopcast path: ");
		JLabel l6=new JLabel("PPLive path: ");
		JLabel l7=new JLabel("Perl path: ");
		JLabel l8=new JLabel("Python path: ");
		JLabel l9=new JLabel("get_flash_videos path: ");
		JLabel l10=new JLabel("YouTube-dl path: ");
		dbg=new JCheckBox("Enable debug",Channels.debugStatus());
		subs=new JCheckBox("Use subtiles",Channels.doSubs());
		chPath=new JTextField(cfg.getPath(),20);
		saPath=new JTextField(cfg.getSavePath(),20);
		rtmp=new JTextField(cfg.getRtmpPath(),20);
		script=new JTextField(cfg.getScriptPath(),20);
		sopcast=new JTextField(cfg.getSopPath(),20);
		pplive=new JTextField(cfg.getPPLivePath(),20);
		perl=new JTextField(cfg.getPerlPath(),20);
		python=new JTextField(cfg.getPythonPath(),20);
		get_flash=new JTextField(cfg.getFlashPath(),20);
		yt=new JTextField(cfg.getYouTubePath(),20);


		// Add some actions
		cBrowse.setActionCommand("cpath");
		cBrowse.addActionListener(this);
		sBrowse.setActionCommand("spath");
		sBrowse.addActionListener(this);
		rBrowse.setActionCommand("rpath");
		rBrowse.addActionListener(this);
		scBrowse.setActionCommand("scpath");
		scBrowse.addActionListener(this);
		sopBrowse.setActionCommand("soppath");
		sopBrowse.addActionListener(this);
		ppBrowse.setActionCommand("pppath");
		ppBrowse.addActionListener(this);
		perlBrowse.setActionCommand("perlpath");
		perlBrowse.addActionListener(this);
		pytBrowse.setActionCommand("pytpath");
		pytBrowse.addActionListener(this);
		get_flBrowse.setActionCommand("flpath");
		get_flBrowse.addActionListener(this);
		ytBrowse.setActionCommand("ytpath");
		ytBrowse.addActionListener(this);
		channels.setActionCommand("other_channels");
		channels.addActionListener(this);
		debugPack.setActionCommand("other_debug");
		debugPack.addActionListener(this);
		dbg.addItemListener(this);
		subs.addItemListener(this);

		GridBagConstraints c = new GridBagConstraints();
		// 1st the channels path
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx=1.0;
		pathPanel.add(l1,c);
		c.gridx++;
		c.weightx=2.0;
		pathPanel.add(chPath,c);
		c.gridx++;
		c.weightx=1.0;
		pathPanel.add(cBrowse,c);

		// 2nd the save path
		c.gridx = 0;
		c.gridy = 1;
		c.weightx=1.0;
		pathPanel.add(l2,c);
		c.gridx++;
		c.weightx=2.0;
		pathPanel.add(saPath,c);
		c.gridx++;
		c.weightx=1.0;
		pathPanel.add(sBrowse,c);
		// Debug
		c.gridx = 0;
		c.gridy = 2;
		c.weightx=1.0;
		pathPanel.add(dbg,c);
		// Subs
		c.gridx = 0;
		c.gridy = 3;
		c.weightx=1.0;
		pathPanel.add(subs,c);

		// 3rd the rtmp path
		c.gridx = 0;
		c.gridy = 2;
		c.weightx=1.0;
		pmsenc.add(l3,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(rtmp,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(rBrowse,c);

		// 4th the script path
		c.gridx = 0;
		c.gridy = 1;
		c.weightx=1.0;
		pmsenc.add(l4,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(script,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(scBrowse,c);
		
/*		// Sopcast
		c.gridx = 0;
		c.gridy = 3;
		c.weightx=1.0;
		pmsenc.add(l5,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(sopcast,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(sopBrowse,c);
		
		// PPLive
		c.gridx = 0;
		c.gridy = 4;
		c.weightx=1.0;
		pmsenc.add(l6,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(pplive,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(ppBrowse,c);
		
		// Perl
		c.gridx = 0;
		c.gridy = 5;
		c.weightx=1.0;
		pmsenc.add(l7,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(perl,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(perlBrowse,c);
		
		// Python
		c.gridx = 0;
		c.gridy = 6;
		c.weightx=1.0;
		pmsenc.add(l8,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(python,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(pytBrowse,c);
		
		// get_flash_videos
		c.gridx = 0;
		c.gridy = 7;
		c.weightx=1.0;
		pmsenc.add(l9,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(get_flash,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(get_flBrowse,c);
		
		// YouTube
		c.gridx = 0;
		c.gridy = 8;
		c.weightx=1.0;
		pmsenc.add(l10,c);
		c.gridx++;
		c.weightx=2.0;
		pmsenc.add(yt,c);
		c.gridx++;
		c.weightx=1.0;
		pmsenc.add(ytBrowse,c);*/

		// Add installation buttons
		// Channels
		c.fill = GridBagConstraints.BOTH;
		inst.add(channels);
		//inst.add(install);

		// Add all
		top.add(pathPanel,BorderLayout.NORTH);
		top.add(new JSeparator(), BorderLayout.CENTER);
		top1.add(pmsenc,BorderLayout.NORTH);
		top1.add(new JSeparator(), BorderLayout.CENTER);
		top1.add(inst,BorderLayout.SOUTH);
		top1.add(debugPack);
		top.add(top1,BorderLayout.SOUTH);
		return top;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String text = (String)e.getActionCommand();
		if(!text.startsWith("other_")) {
			JFileChooser path=new JFileChooser();
			if(!text.equals("rpath"))
				path.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int res=path.showOpenDialog(null);
			if(res==JFileChooser.APPROVE_OPTION) {
				if(path.getSelectedFile().exists()){
					try {
						if(text.equals("cpath"))
							cfg.setPath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("spath"))
							cfg.setSavePath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("rpath"))
							cfg.setRtmpPath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("scpath"))
							cfg.setScriptPath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("soppath"))
							cfg.setSopPath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("pppath"))
							cfg.setPPLivePath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("perlpath"))
							cfg.setPerlPath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("pytnpath"))
							cfg.setPythPath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("flpath"))
							cfg.setGetFlPath(path.getSelectedFile().getCanonicalPath().toString());
						else if(text.equals("ytpath"))
							cfg.setYouTubePath(path.getSelectedFile().getCanonicalPath().toString());
						update();
						cfg.commit();
					} catch (IOException e1) {
					}
				}
			}
		}
		else  {
			if(text.equals("other_channels")) {
				PMS.debug("update channels files");
				pushPaths();
				cfg.commit();
				cfg.fetchChannels();
				root.fileChanged(new File(cfg.getPath()));
			}
			else if(text.equals("other_debug")) {
				cfg.packDbg();
			}
		}

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		boolean val=true;
		Object source = e.getItemSelectable();
		if (e.getStateChange() == ItemEvent.DESELECTED) 
			val=false;
		if(source==dbg)
			Channels.debug(val);
		if(source==subs)
			Channels.setSubs(val);
		cfg.commit();
	}
	
	private void update() {
		chPath.setText(cfg.getPath());
		saPath.setText(cfg.getSavePath());
		rtmp.setText(cfg.getRtmpPath());
		script.setText(cfg.getScriptPath());
		sopcast.setText(cfg.getSopPath());
		pplive.setText(cfg.getPPLivePath());
		perl.setText(cfg.getPerlPath());
		python.setText(cfg.getPythonPath());
		get_flash.setText(cfg.getFlashPath());
		yt.setText(cfg.getYouTubePath());
	}
	
	private void pushPaths() {
		cfg.setPath(chPath.getText());
		cfg.setRtmpPath(rtmp.getText());
		cfg.setSavePath(saPath.getText());
		cfg.setScriptPath(script.getText());
		cfg.setSopPath(sopcast.getText());
		cfg.setPPLivePath(pplive.getText());
		cfg.setPerlPath(perl.getText());
		cfg.setPythPath(python.getText());
		cfg.setGetFlPath(get_flash.getText());
		cfg.setYouTubePath(yt.getText());
	}
	
}