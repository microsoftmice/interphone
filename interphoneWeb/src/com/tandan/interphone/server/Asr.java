package com.tandan.interphone.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import com.alibaba.idst.nls.NlsClient;
import com.alibaba.idst.nls.NlsFuture;
import com.alibaba.idst.nls.event.NlsEvent;
import com.alibaba.idst.nls.event.NlsListener;
import com.alibaba.idst.nls.protocol.NlsRequest;
import com.alibaba.idst.nls.protocol.NlsResponse;
import com.alibaba.nls.client.example.SpeechRecognizerDemo;

public class Asr implements NlsListener {
	private static NlsClient client = new NlsClient();
	private String akId;
	private String akSecret;
	public InputStream is;

	public Asr(String akId, String akSecret) {
		this.akId = akId;
		this.akSecret = akSecret;

	}

	public void init() {
		// 初始化NlsClient
		System.out.println("init Nls client...");
		client.init();
	}

	public void shutDown() {
		System.out.println("close NLS client");
		// 关闭客户端并释放资源
		client.close();
		System.out.println("demo done");
	}

	public void startAsr() throws Exception {
		// 开始发送语音
		

        String appKey = "itcsHfQc6V2Anz5p";
        String token = "5d3aa334b0474bb9821f402c74b80c7a";
       // SpeechRecognizerDemo demo = new SpeechRecognizerDemo(appKey, token);
		
		System.out.println("open audio file...");
		/*
		byte[] b = new byte[1024];
		int len = 0;
		Date d = new Date();
		String s = d.getTime();
		System.out.println("open audio file=============="+s);
		FileOutputStream out = new FileOutputStream("D:\\javaworkspace1\\interphone\\"+ +".pcm");
		while ((len = is.read(b)) > 0) {
			out.write(b);
		}
		out.close();
		*/
		//demo.process(is);
		
		if (is != null) {
			System.out.println("create NLS future");
			try {
				NlsRequest req = new NlsRequest();
				req.setAppKey("nls-service-multi-domain"); // appkey请从 "快速开始" 帮助页面的appkey列表中获取
				req.setAsrFormat("pcm"); // 设置语音文件格式为pcm,我们支持16k 16bit 的无头的pcm文件。
				req.authorize(akId, akSecret); // 请替换为用户申请到的Access Key ID和Access Key
				// Secret
				NlsFuture future = client.createNlsFuture(req, this); // 实例化请求,传入请求和监听器
				System.out.println("call NLS service........");
				byte[] b = new byte[16000];
				int len = 0;
				/////////////  save file
				Date d = new Date();
				//FileOutputStream out = new FileOutputStream("D:\\javaworkspace1\\interphone\\"+ d.getTime()+".pcm");
				while ((len = is.read(b)) > 0) {
					future.sendVoice(b, 0, len); // 发送语音数据
					//out.write(b,0,len);
					Thread.sleep(50);
				}
				//out.close();
				future.sendFinishSignal(); // 语音识别结束时，发送结束符
				//System.out.println("main thread enter waiting for less than 10s.");
				future.await(10000); // 设置服务端结果返回的超时时间
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("calling NLS service end");
			
		} else {
			System.out.println("fis is null");

		}
		
		 // demo.shutdown();
	}

	@Override
	public void onMessageReceived(NlsEvent e) {
		// 识别结果的回调
		NlsResponse response = e.getResponse();
		String result = "";
		int statusCode = response.getStatus_code();
		if (response.getAsr_ret() != null && statusCode == 200) {
			result += "\nget asr result: statusCode=[" + statusCode + "], " + response.getAsr_ret();
			System.out.println("---------------------"+response.jsonResults.getString("result")+"----------");
		}
		if (result != null) {
			System.out.println(result);
		} else {
			System.out.println(response.jsonResults.toString());
		}
	}

	@Override
	public void onOperationFailed(NlsEvent e) {
		// 识别失败的回调
		String result = "";
		result += "on operation failed: statusCode=[" + e.getResponse().getStatus_code() + "], " + e.getErrorMessage();
		System.out.println(result);
	}

	@Override
	public void onChannelClosed(NlsEvent e) {
		// socket 连接关闭的回调
		System.out.println("on websocket closed.");
	}

	public static void main(String[] args) throws Exception {
		String akId = "LTAII6U8YIqqiA3r";
		String akSecret = "TMYaCXAmI1CXWQnBeQoDpVYHDZHLZo";
		Asr asrDemo = new Asr(akId, akSecret);
		// File f=new File("D:\\interphone\\wav\\20180715T100918Z_16000_16_2.wav");
		File f = new File("D:\\javaworkspace1\\interphone\\1545187015206.pcm");
		InputStream fis = new FileInputStream(f);
		asrDemo.is = fis;
		asrDemo.init();
		asrDemo.startAsr();
		asrDemo.shutDown();
		fis.close();
	}
}
