package com.sharkhunter.channel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.encoders.FFMpegVideo;
import net.pms.encoders.MEncoderWebVideo;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class ChannelNullPlayer extends FFMpegVideo {

	private boolean transcode;
	private PmsConfiguration configuration;
	
	public ChannelNullPlayer(boolean transcode) {
		super();
		configuration=PMS.getConfiguration();
		this.transcode=transcode;
	}
	
	public String name() {
		return "Channel Null Player";
	}
	
	private void addMencoder(ArrayList<String> args,String in,boolean subs) {
		args.add(executable());
		args.add(in);
		args.add("-quiet");
		args.add("-prefer-ipv4");
		args.add("-cookies-file");
		args.add(Channels.cfg().getCookiePath());
		args.add("-ovc");
		if(!transcode&&!subs) 
			args.add("copy");
		else 
			args.add("lavc");
		args.add("-oac");
		if(!transcode)
			args.add("copy");
		else 
			args.add("lavc");
	}
	
	private void addTranscode(ArrayList<String> args) {
		int nThreads = configuration.getMencoderMaxThreads();
		String acodec = configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3";
		args.add("-of");
		args.add("lavf");
		args.add("-lavfopts");
		args.add("format=dvd");
		args.add("-lavcopts"); 
		args.add("vcodec=mpeg2video:vbitrate=4096:threads=" + nThreads + ":acodec=" + acodec + ":abitrate=128");
	}
	
	
	public ProcessWrapper launchTranscode(
			String fileName,
			DLNAResource dlna,
			DLNAMediaInfo media,
			OutputParams params) throws IOException {
		Channels.debug("ch_null_player launch "+fileName+" "+dlna);
			params.minBufferSize = params.minFileSize;
			params.secondread_minsize = 100000;
			//params.waitbeforestart = 1000;
			//boolean mencoder=false;
			boolean subs=(params.sid != null && params.sid.getId() != -1);

			PipeProcess pipe = new PipeProcess("channels" + System.currentTimeMillis());
			params.input_pipes[0] = pipe;
			
			ArrayList<String> args=new ArrayList<String>();
			String format=ChannelUtil.extension(dlna.getSystemName(),true);
			
			// FFmpeg try
			args.add(executable());
			if(fileName.startsWith("rtmpdump://channel?")) {
				fileName=fileName.substring(19);
				String ops="";
				String[] tmp=fileName.split("&");
				String url="";
				String swfUrl="";
				for(int i=0;i<tmp.length;i++) {
					String[] pair=tmp[i].split("=",2);
					if(pair[0].equals("-r"))
						url=ChannelUtil.unescape(pair[1]);
					else if(pair[0].equals("-y"))
						ops=ops+" playpath="+ChannelUtil.unescape(pair[1]);
					else if(pair[0].equals("-a"))
						ops=ops+" app="+ChannelUtil.unescape(pair[1]);
					else if(pair[0].equals("-v"))
						ops=ops+" live=1";
					else if(pair[0].equals("-p"))
						ops=ops+" pageurl="+ChannelUtil.unescape(pair[1]);
					else if(pair[0].equals("-s"))
						swfUrl=" swfUrl="+ChannelUtil.unescape(pair[1]);
					else if(pair[0].equals("-W")||pair[0].equals("--swfVfy")) {
						swfUrl=" swfUrl="+ChannelUtil.unescape(pair[1]);
						ops=ops+" swfVfy=1";
					}
				}
				args.add("-i");
				args.add(url+ops+swfUrl);
				format="flv";
			}
			else {
				args.add("-i");
				args.add(fileName);
				String cookie=ChannelCookie.getCookie(fileName);
				if(!ChannelUtil.empty(cookie)) {
					args.add("-headers");
					args.add("Cookie: "+cookie);
				}
			}
			
			if(subs) {
				args.add("-i");
				args.add(params.sid.getFile().getAbsolutePath());
				args.add("-scodec");
				args.add("copy");
			}
			
			// Generic options
			args.add("-threads");
			args.add(String.valueOf(configuration.getMencoderMaxThreads()));
			args.add("-y");
		/*	args.add("-v");
			args.add("quiet");*/
		
			if(!transcode&&!subs) {
				args.add("-vcodec");
				args.add("copy");
				args.add("-acodec");
				args.add("copy");
				args.add("-f");
				args.add(format);
			}
			else {
			/*	args.add("-vcodec");
				args.add("mpeg2video");
				args.add("-vb");
				args.add("6000k");
				args.add("-acodec");
				args.add("ac3");
				args.add("-ab");
				args.add("448k");
				args.add("-f");
				args.add("dvd");*/
				args.add("-target");
				args.add("ntsc-dvd");
			}
			
			args.add(pipe.getInputPipe());
			
			String[] cmdArray=new String[args.size()];
			args.toArray(cmdArray);

			ProcessWrapper mkfifo_process = pipe.getPipeProcess();

			cmdArray = finalizeTranscoderArgs(
				this,
				fileName,
				dlna,
				media,
				params,
				cmdArray);

			ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
			pw.attachProcess(mkfifo_process);
			mkfifo_process.runInNewThread();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			pipe.deleteLater();

			pw.runInNewThread();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			return pw;
		}
}

/*if(fileName.startsWith("rtmpdump://channel?")) {
args.add(Channels.cfg().getRtmpPath());
fileName=fileName.substring(19);
String[] tmp=fileName.split("&");
for(int i=0;i<tmp.length;i++) {
	String[] pair=tmp[i].split("=",2);
	args.add(pair[0]);
	args.add(ChannelUtil.unescape(pair[1]));
}
args.add("-q");
args.add("-o");
if(subs||transcode)
	args.add("-");
else
	args.add(pipe.getInputPipe());
}
else {
// simple download, use mencoder 
/*args.add(Channels.cfg().getCurlPath());
args.add("-s");
args.add("-S");
args.add("-b");
args.add(Channels.cfg().getCookiePath());
args.add("--location-trusted");
args.add("--output");
args.add(pipe.getInputPipe());
args.add(fileName);*/
/*mencoder=true;
addMencoder(args,fileName,subs);
}

/*if(subs) {
// we need subs!
DLNAMediaSubtitle sub=params.sid;
if(!mencoder) {
	mencoder=true;
	args.add("|");
	addMencoder(args,"-",subs);	
}
args.add("-sub");
args.add(sub.getFile().getAbsolutePath());
args.add("-subcp");
args.add(configuration.getMencoderSubCp());
args.add("-subpos");
args.add("90");
}

if(transcode) {
if(!mencoder) {
	mencoder=true;
	args.add("|");
	addMencoder(args,"-",subs);	
}
addTranscode(args);
}

if(mencoder) {
args.add("-o");
args.add(pipe.getInputPipe());
}*/
