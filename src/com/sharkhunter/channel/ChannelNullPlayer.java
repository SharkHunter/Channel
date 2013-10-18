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
import net.pms.formats.v2.SubtitleUtils;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.util.CodecUtil;
import net.pms.util.FileUtil;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ChannelNullPlayer extends FFMpegVideo {

	private boolean transcode;
	private PmsConfiguration configuration;
	private boolean wmv; 	
	
	public ChannelNullPlayer(boolean transcode) {
		super();
		configuration=PMS.getConfiguration();
		this.transcode=transcode||Channels.cfg().mp2Force();
		wmv=false;
	}
	
	public String name() {
		return "Channel Null Player";
	}
	
	private void addMencoder(ArrayList<String> args,String in,File subFile) {
		int nThreads = configuration.getMencoderMaxThreads();
		String acodec = (configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3")+":abitrate=128" ;
		String vcodec = "mpeg2video";
		String format="dvd";
		args.add(configuration.getMencoderPath());
		args.add(in);
		args.add("-quiet");
		args.add("-prefer-ipv4");
		args.add("-cookies-file");
		args.add(Channels.cfg().getCookiePath());
		
		args.add("-oac");
		if(!transcode)
			args.add("pcm");
		else
			args.add("lavc");
		args.add("-ovc");
		if(!transcode)
			args.add("copy");
		else
			args.add("lavc");
		if(transcode) {
			args.add("-of");
			args.add("lavf");
			args.add("-lavfopts");
			args.add("format="+format);
			args.add("-lavcopts");
			args.add("vcodec="+vcodec+":vbitrate=4096:threads=" + nThreads + ":acodec=" + acodec);
			args.add("-vf");
			args.add("harddup");
			args.add("-ofps");
			args.add("25");
		}
		args.add("-cache");
		args.add("16384");
		if(subFile!=null) {
			args.add("-sub");
			args.add(subFile.getAbsolutePath());
			args.add("-subcp");
            // Append -subcp option for non UTF external subtitles
            String subcp = "UTF-8";
            args.add(subcp);
			args.add("-subpos");
			int subpos = 1;
			try {
				subpos = Integer.parseInt(configuration.getMencoderNoAssSubPos());
			} catch (NumberFormatException n) {
			}
			args.add(String.valueOf(100 - subpos));
			args.add("-subfont-text-scale");
			args.add(configuration.getMencoderNoAssScale());
			args.add("-subfont-outline");
			args.add(configuration.getMencoderNoAssOutline());
			args.add("-subfont-blur");
			args.add(configuration.getMencoderNoAssBlur());
			String font = CodecUtil.getDefaultFontPath();
			if(!ChannelUtil.empty(configuration.getFont()))
				font=configuration.getFont();
			if(!ChannelUtil.empty(font)) {
				args.add("-font");
				args.add(font);
			}

		}
	}
	
	public ProcessWrapper launchTranscode(
			DLNAResource dlna,
			DLNAMediaInfo media,
			OutputParams params) throws IOException {
        String fileName  = dlna.getSystemName();
		Channels.debug("ch_null_player launch "+fileName+" "+dlna);
			params.minBufferSize = params.minFileSize;
			params.secondread_minsize = 100000;
			params.waitbeforestart = 6000;
			//boolean mencoder=false;
			boolean subs=(params.sid != null && params.sid.getId() != -1);
			if (params.mediaRenderer.isTranscodeToWMV()) {
				wmv=true;
				transcode=true;
			}
			
			PipeProcess pipe = new PipeProcess("channels" + System.currentTimeMillis());
			params.input_pipes[0] = pipe;
				
			ArrayList<String> args=new ArrayList<String>();
			ChannelMediaStream cms=(ChannelMediaStream)dlna;
			String format=ChannelUtil.extension(cms.realFormat(),true);
			String effFile=fileName;
			
			if (Channels.cfg().fileBuffer()) {
				String fName=Channels.fileName(dlna.getName(),true);
				if(cms.saveName()!=null)
					fName=Channels.fileName(cms.saveName(), false);
		    	fName=ChannelUtil.guessExt(fName,fileName);
		    	final File m = new File(fName);
		    	if(!cms.isBgDownload()&&!fileName.startsWith("rtmpdump://channel?")) {
		    		if (m.exists() && !m.delete()) {
		    			ChannelUtil.sleep(3000);
		    		}
		    		ChannelUtil.cacheFile(m,"media");
		    		final String fileName1=fileName;
		    		Runnable r=new Runnable() {
		    			public void run() {
		    				ChannelUtil.downloadBin(fileName1, m);
		    			}
		    		};
		    		Thread t=new Thread(r);
		    		cms.bgThread(t);
		    		t.start();
		    	}
		    	else {
		    		cms.moreBg();
		    	}
		    	// delay until file is large enough
		    	while(m.length()<params.minBufferSize)
		    		ChannelUtil.sleep(200);
		    	effFile=m.getAbsolutePath();
			}
			
			
			/*if(subs) { // subtitles use menocder
				addMencoder(args,effFile,params.sid.getExternalFile());
				args.add("-o");
				args.add(params.input_pipes[0].getInputPipe());
				
			}
			else {*/
			if(subs) {
				addMencoder(args,fileName,params.sid.getExternalFile());
				args.add("-o");
				args.add("-");
			}	
				String src=fileName;
				args.add(configuration.getFfmpegPath());
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
					src=url+ops+swfUrl;
					format="flv";
				}
				args.add("-i");
				args.add(src);
				String cookie=ChannelCookie.getCookie(fileName);
				if(!ChannelUtil.empty(cookie)) {
					args.add("-headers");
					args.add("Cookie: "+cookie);
				}
				args.add("-threads");
				args.add(String.valueOf(configuration.getMencoderMaxThreads()));
				args.add("-y");
				args.add("-v");
				args.add("0");
				if(!transcode||wmv) {
					String acodec = "copy";
					String vcodec = "copy";
					if(wmv) {
						vcodec="wmv2";
						acodec="wmav2:abitrate=448";
						format="asf";
					}
					args.add("-vcodec");
					if(!wmv)
						args.add("copy");
					else {
						format="wav";
						args.add("wmv2");
					}
					args.add("-acodec");
					if(!wmv)
						args.add("copy");
					else
						args.add("wmav2");
					args.add("-f");
					args.add(format);
				}
				else {
					args.add("-target");
					args.add("ntsc-dvd");
				}
				args.add(params.input_pipes[0].getInputPipe());
				/*}
				else {
					addMencoder(args,fileName,null);
					args.add("-o");	
					args.add(params.input_pipes[0].getInputPipe());
				}
				}*/
			
			String[] cmdArray=new String[args.size()];
			args.toArray(cmdArray);
			
			ProcessWrapper mkfifo_process = null;
			mkfifo_process = params.input_pipes[0].getPipeProcess();

			cmdArray = finalizeTranscoderArgs(
				this,
				fileName,
				dlna,
				media,
				params,
				cmdArray);

			ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
			if(mkfifo_process!=null) {
				pw.attachProcess(mkfifo_process);
				mkfifo_process.runInNewThread();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			params.input_pipes[0].deleteLater();
			
			pw.runInNewThread();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			return pw;
		}
}
