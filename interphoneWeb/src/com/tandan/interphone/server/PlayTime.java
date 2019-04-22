package com.tandan.interphone.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletContext;

import com.alibaba.nls.client.AccessToken;

public class PlayTime extends Thread {
	ServletContext application;
	GregorianCalendar gc = new GregorianCalendar();
	public boolean is_work = true;
	 TTS2 demo = new TTS2();  
    
	public ServletContext getApplication() {
		return application;
	}

	public void setApplication(ServletContext application) {
		this.application = application;
	}

	public void run() {
		System.out.println("this is a playtime thread,path="+application.getRealPath("."));
		demo.filepath=application.getRealPath(".")+File.separator+"tts1.wav";
		try {
			while (is_work) {
				Date d = new Date();
				gc.setTime(d);
				//if ((gc.get(gc.SECOND) == 0)) {
				if ((gc.get(gc.SECOND) == 0) && gc.get(gc.MINUTE)  == 0 ) {
					//System.out.println("整点:"+d);
				    demo.ttsText=getText();
			        demo.process();
					System.out.println("set the wav to list!");
					String content = "http://47.100.196.114:8080/interphone/tts1.wav";
					application.setAttribute("list", content);
				}
				this.sleep(1000);
			}
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	
	
	public static String getText()  {
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH点mm分");// 设置日期格式
		String tts_text = "您当前收听到的信号为汨罗业余无线电中继电台信号，电台呼号BR7DZK,上行频率为431.525兆赫,下行频率为439.525兆赫,哑音88.5,当前时间" + df.format(new Date()) + "";
		//System.out.println(tts_text);
		return tts_text;
	}
	
	public static void main(String[] args) throws Exception {
		
		String akId = "LTAII6U8YIqqiA3r";
		String akSecret = "TMYaCXAmI1CXWQnBeQoDpVYHDZHLZo";
		TTS ttsDemo = new TTS(akId, akSecret);
		File file = new File("sys-ok2.wav");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		OutputStream fileOutputStream = new FileOutputStream(file);
		ttsDemo.os = fileOutputStream;
		ttsDemo.tts_text=getText();
		ttsDemo.startTTS();
		ttsDemo.shutDown();
		
		//AccessToken accessToken = AccessToken.apply("LTAIjTBoNOkZnbuT", "hyi1Ih3YhwV9Xk5fx4nFmyMgnn5F4Q");
		//String token = accessToken.getToken();
		//long expireTime = accessToken.getExpireTime();
		//System.out.print(token);
	}

}
