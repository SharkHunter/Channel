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
import net.pms.util.CodecUtil;

public class ChannelNullPlayer extends FFMpegVideo {

	private boolean transcode;
	private PmsConfiguration configuration;
	
	public ChannelNullPlayer(boolean transcode) {
		super();
		configuration=PMS.getConfiguration();
		this.transcode=transcode||Channels.cfg().mp2Force();
	}
	
	public String name() {
		return "Channel Null Player";
	}
	
	private void addMencoder(ArrayList<String> args,String in,File subFile) {
		int nThreads = configuration.getMencoderMaxThreads();
		String acodec = configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3";
		args.add(configuration.getMencoderPath());
		args.add(in);
		args.add("-quiet");
		args.add("-prefer-ipv4");
		args.add("-cookies-file");
		args.add(Channels.cfg().getCookiePath());
		args.add("-ovc");
		if(!transcode)
			args.add("copy");
		else
			args.add("lavc");
		args.add("-oac");
		if(!transcode)
			args.add("pcm");
		else
			args.add("lavc");
		if(transcode) {
			args.add("-lavcopts");
			args.add("vcodec=mpeg2video:vbitrate=4096:threads=" + nThreads + ":acodec=" + acodec + ":abitrate=128");
			args.add("-lavfopts");
			args.add("format=dvd");
		}
		if(subFile!=null) {
			args.add("-sub");
			args.add(subFile.getAbsolutePath());
			args.add("-subcp");
			args.add(configuration.getMencoderSubCp());
			args.add("-subpos");
			int subpos = 1;
			try {
				subpos = Integer.parseInt(configuration.getMencoderAssMargin());
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
			if(!ChannelUtil.empty(configuration.getMencoderFont()))
				font=configuration.getMencoderFont();
			if(!ChannelUtil.empty(font)) {
				args.add("-font");
				args.add(font);
			}

		}
	}
	
	public ProcessWrapper launchTranscode(
			String fileName,
			DLNAResource dlna,
			DLNAMediaInfo media,
			OutputParams params) throws IOException {
		Channels.debug("ch_null_player launch "+fileName+" "+dlna);
			params.minBufferSize = params.minFileSize;
			params.secondread_minsize = 100000;
			params.waitbeforestart = 1000;
			//boolean mencoder=false;
			boolean subs=(params.sid != null && params.sid.getId() != -1);

			PipeProcess pipe = new PipeProcess("channels" + System.currentTimeMillis());
			params.input_pipes[0] = pipe;
			
			ArrayList<String> args=new ArrayList<String>();
			ChannelMediaStream cms=(ChannelMediaStream)dlna;
			String format=ChannelUtil.extension(cms.realFormat(),true);
			
			
			if(subs) { // subtitles use menocder
				addMencoder(args,fileName,params.sid.getExternalFile());
				args.add("-o");
				args.add(pipe.getInputPipe());
				
			}
			else {
				String src=fileName;
				if(fileName.startsWith("rtmpdump://channel?")) {
					args.add(configuration.getFfmpegPath());
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
					if(!transcode) {
						args.add("-vcodec");
						args.add("copy");
						args.add("-acodec");
						args.add("copy");
						args.add("-f");
						args.add(format);
					}
					else {
						args.add("-target");
						args.add("ntsc-dvd");
					}		
					args.add(pipe.getInputPipe());
				}
				else {
					addMencoder(args,fileName,null);
					args.add("-o");
					args.add(pipe.getInputPipe());
				}
			}
			
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
