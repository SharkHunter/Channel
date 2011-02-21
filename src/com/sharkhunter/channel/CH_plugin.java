package com.sharkhunter.channel;
import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalFolderAtRoot;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class CH_plugin implements AdditionalFolderAtRoot, ActionListener {

	private static final long DEFAULT_POLL_INTERVAL=20000;
	private Channels chRoot;
	private ChannelCfg cfg;

	public CH_plugin() {
		try {
			File chFolder=new File(PMS.getConfiguration().getTempFolder(),"channels");
			String confPath=(String)PMS.getConfiguration().getCustomProperty("channels.path");
			String path;
			if(confPath==null) {
				chFolder.mkdir();
				path=chFolder.toString();
			}
			else 
				path=confPath;
			String save=(String)PMS.getConfiguration().getCustomProperty("channels.save");
			chRoot=new Channels(path,getInterval());
			cfg=new ChannelCfg(chRoot);
			if(save!=null) {
				String ts=(String)PMS.getConfiguration().getCustomProperty("channels.save_ts");
				String savePath=save;
				if(ChannelUtil.empty(save.trim()))
					savePath=path+File.separator+"saved";
				chRoot.setSave(savePath,ts);
				cfg.ensureCreated(savePath);
			}
			cfg.init();
		}
		catch (Exception e) {
			PMS.debug("exp "+e)	;
		}
	}

	private long getInterval() {
		String interval=(String)PMS.getConfiguration().getCustomProperty("channels.poll");
		if(interval!=null) {
			try {
				Long l=Long.parseLong(interval);
				return l.longValue();
			}
			catch (Exception e) {
				PMS.minimal("Illegal interval value "+e.toString());
			}
		}
		return CH_plugin.DEFAULT_POLL_INTERVAL;
	}

	public DLNAResource getChild() {
		return chRoot;
	}

	public void shutdown() {
	}

	public String name() {
		return "Channels";
	}
	
	private JTextField chPath;
	private JTextField saPath;
	private JTextField rtmp;
	private JTextField script;
	
	private void update() {
		chPath.setText(cfg.getPath());
		saPath.setText(cfg.getSavePath());
		rtmp.setText(cfg.getRtmpPath());
		script.setText(cfg.getScriptPath());
	}
	
	private void pushPaths() {
		cfg.setPath(chPath.getText());
		cfg.setRtmpPath(rtmp.getText());
		cfg.setSavePath(saPath.getText());
		cfg.setScriptPath(script.getText());
	}

    //@Override
	public JComponent config() {
		//JFrame frame=new JFrame("Configure Channels");
		cfg.init();
		JPanel top=new JPanel(new BorderLayout(10,10));
		JPanel top1=new JPanel(new BorderLayout(10,10));
		JPanel pathPanel=new JPanel(new GridBagLayout());
		JPanel inst=new JPanel();
		JPanel pmsenc=new JPanel(new GridBagLayout());
		JButton channels=new JButton("Install/Update");
		//JButton install=new JButton("Install");
		JButton cBrowse=new JButton("Browse...");
		JButton sBrowse=new JButton("Browse...");
		JButton rBrowse=new JButton("Browse...");
		JButton scBrowse=new JButton("Browse...");
		JLabel l1=new JLabel("Channels Path: ");
		JLabel l2=new JLabel("Save Path: ");
		JLabel l3=new JLabel("RTMPDump path: ");
		JLabel l4=new JLabel("Scripts path: ");
		chPath=new JTextField(cfg.getPath(),20);
		saPath=new JTextField(cfg.getSavePath(),20);
		rtmp=new JTextField(cfg.getRtmpPath(),20);
		script=new JTextField(cfg.getScriptPath(),20);
		
		// Add some actions
		cBrowse.setActionCommand("cpath");
		cBrowse.addActionListener(this);
		sBrowse.setActionCommand("spath");
		sBrowse.addActionListener(this);
		rBrowse.setActionCommand("rpath");
		rBrowse.addActionListener(this);
		scBrowse.setActionCommand("scpath");
		scBrowse.addActionListener(this);
		channels.setActionCommand("channels");
		channels.addActionListener(this);
		
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
		top.add(top1,BorderLayout.SOUTH);
		return top;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String text = (String)e.getActionCommand();
		if(text.equals("cpath")||text.equals("spath")||text.equals("rpath")||
				text.equalsIgnoreCase("scpath")) {
			JFileChooser path=new JFileChooser();
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
						update();
						cfg.commit();
					} catch (IOException e1) {
					}
				}
			}
		}
		if(text.equals("channels")) {
			PMS.debug("update channels files");
			pushPaths();
			cfg.commit();
			cfg.fetchChannels();
			chRoot.fileChanged(new File(cfg.getPath()));
		}
		
	}
	
}
