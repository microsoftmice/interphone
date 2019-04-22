package com.tandan.interphone.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizer;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerResponse;
import com.aliyuncs.exceptions.ClientException;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletContext;

/**
 * SpeechRecognizerDemo class
 *
 * 一句话识别Demo
 * 
 * @author siwei
 * @date 2018/5/29
 */
public class ASR2 {
	private String appKey;
	NlsClient client;
	String token;

	static ServletContext application;

	public ServletContext getApplication() {
		return application;
	}

	public void setApplication(ServletContext application) {
		this.application = application;
	}

	/**
	 * @param appKey
	 * @param token
	 */
	public ASR2() {
		AccessToken accessToken;
		try {
			accessToken = AccessToken.apply("LTAIjTBoNOkZnbuT", "hyi1Ih3YhwV9Xk5fx4nFmyMgnn5F4Q");
			token = accessToken.getToken();
		} catch (ClientException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		this.appKey = "itcsHfQc6V2Anz5p";
		// Step0 创建NlsClient实例,应用全局创建一个即可,默认服务地址为阿里云线上服务地址
		client = new NlsClient(token);
	}

	private static SpeechRecognizerListener getRecognizerListener() {
		SpeechRecognizerListener listener = new SpeechRecognizerListener() {
			Business business = new Business();
			// 识别出中间结果.服务端识别出一个字或词时会返回此消息.仅当setEnableIntermediateResult(true)时,才会有此类消息返回
			@Override
			public void onRecognitionResultChanged(SpeechRecognizerResponse response) {
				// 事件名称 RecognitionResultChanged
				System.out.println("name: " + response.getName() +
				// 状态码 20000000 表示识别成功
				", status: " + response.getStatus() +
				// 一句话识别的中间结果
				", result: " + response.getRecognizedText());
			}

			// 识别完毕
			@Override
			public void onRecognitionCompleted(SpeechRecognizerResponse response) {
				// 事件名称 RecognitionCompleted
				System.out.println("name: " + response.getName() +
				// 状态码 20000000 表示识别成功
				", status: " + response.getStatus() +
				// 一句话识别的完整结果
				", result: " + response.getRecognizedText());
				if (response.getStatus() == 20000000) {
					
					Business business = new Business();
					String reply = "";
					try {
						reply = business.run(response.getRecognizedText());
					} catch (Exception e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					if (!reply.equals("")) {
						TTS2 demo = new TTS2();
						demo.filepath = application.getRealPath(".") + File.separator + "reply.wav";
						demo.ttsText = reply;
						demo.process();
						System.out.println("set the wav to list!");
						String content = "http://47.100.196.114:8080/interphone/reply.wav";
						application.setAttribute("list", content);
						demo.shutdown();
					}
					

				}
			}
		};
		return listener;
	}

	public void process(InputStream ins) {
		SpeechRecognizer recognizer = null;
		try {
			AccessToken accessToken = AccessToken.apply("LTAIjTBoNOkZnbuT", "hyi1Ih3YhwV9Xk5fx4nFmyMgnn5F4Q");
			String token = accessToken.getToken();
			client.setToken(token);
			// Step1 创建实例,建立连接
			recognizer = new SpeechRecognizer(client, getRecognizerListener());
			recognizer.setAppKey(appKey);
			// 设置音频编码格式
			recognizer.setFormat(InputFormatEnum.PCM);
			// 设置音频采样率
			recognizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);

			// 设置是否返回中间识别结果
			recognizer.setEnableIntermediateResult(false);

			// Step2 此方法将以上参数设置序列化为json发送给服务端,并等待服务端确认
			recognizer.start();
			// Step3 语音数据来自声音文件用此方法,控制发送速率;若语音来自实时录音,不需控制发送速率直接调用 recognizer.sent(ins)即可
			recognizer.send(ins);
			// Step4 通知服务端语音数据发送完毕,等待服务端处理完成
			recognizer.stop();
			System.out.println("asr over!!!!!!!!!!");
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			// Step5 关闭连接
			if (null != recognizer) {
				recognizer.close();
			}
		}
	}

	public void shutdown() {
		client.shutdown();
	}

	public static void main(String[] args) throws Exception {
		/*
		 * ASR2 asr2 = new ASR2(); File f = new
		 * File("D:\\javaworkspace\\RealtimeDemo\\tts1.wav"); InputStream ins = new
		 * FileInputStream(f); if (null == ins) {
		 * System.err.println("open the audio file failed!"); System.exit(-1); }
		 * asr2.process(ins); asr2.shutdown();
		 * 
		 */
		try {
			String city = java.net.URLEncoder.encode("岳麓大道", "utf-8");
			String apiUrl = String.format(
					"https://restapi.amap.com/v3/traffic/status/road?key=e3ffb60aa1fdccbae25c8ce840fb99ef&adcode=430100&name=%s",
					city);
			URL url = new URL(apiUrl);
			URLConnection open = url.openConnection();
			InputStream input = open.getInputStream();
			String result = org.apache.commons.io.IOUtils.toString(input, "utf-8");
			System.out.println("name: " + result);
			String description = JSON.parseObject(result).getJSONObject("trafficinfo").getString("description");
			System.out.println("description: " + description);
			input.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
