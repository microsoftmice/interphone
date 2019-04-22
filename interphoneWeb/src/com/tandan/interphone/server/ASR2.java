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
 * һ�仰ʶ��Demo
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
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		this.appKey = "itcsHfQc6V2Anz5p";
		// Step0 ����NlsClientʵ��,Ӧ��ȫ�ִ���һ������,Ĭ�Ϸ����ַΪ���������Ϸ����ַ
		client = new NlsClient(token);
	}

	private static SpeechRecognizerListener getRecognizerListener() {
		SpeechRecognizerListener listener = new SpeechRecognizerListener() {
			Business business = new Business();
			// ʶ����м���.�����ʶ���һ���ֻ��ʱ�᷵�ش���Ϣ.����setEnableIntermediateResult(true)ʱ,�Ż��д�����Ϣ����
			@Override
			public void onRecognitionResultChanged(SpeechRecognizerResponse response) {
				// �¼����� RecognitionResultChanged
				System.out.println("name: " + response.getName() +
				// ״̬�� 20000000 ��ʾʶ��ɹ�
				", status: " + response.getStatus() +
				// һ�仰ʶ����м���
				", result: " + response.getRecognizedText());
			}

			// ʶ�����
			@Override
			public void onRecognitionCompleted(SpeechRecognizerResponse response) {
				// �¼����� RecognitionCompleted
				System.out.println("name: " + response.getName() +
				// ״̬�� 20000000 ��ʾʶ��ɹ�
				", status: " + response.getStatus() +
				// һ�仰ʶ����������
				", result: " + response.getRecognizedText());
				if (response.getStatus() == 20000000) {
					
					Business business = new Business();
					String reply = "";
					try {
						reply = business.run(response.getRecognizedText());
					} catch (Exception e) {
						// TODO �Զ����ɵ� catch ��
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
			// Step1 ����ʵ��,��������
			recognizer = new SpeechRecognizer(client, getRecognizerListener());
			recognizer.setAppKey(appKey);
			// ������Ƶ�����ʽ
			recognizer.setFormat(InputFormatEnum.PCM);
			// ������Ƶ������
			recognizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);

			// �����Ƿ񷵻��м�ʶ����
			recognizer.setEnableIntermediateResult(false);

			// Step2 �˷��������ϲ����������л�Ϊjson���͸������,���ȴ������ȷ��
			recognizer.start();
			// Step3 �����������������ļ��ô˷���,���Ʒ�������;����������ʵʱ¼��,������Ʒ�������ֱ�ӵ��� recognizer.sent(ins)����
			recognizer.send(ins);
			// Step4 ֪ͨ������������ݷ������,�ȴ�����˴������
			recognizer.stop();
			System.out.println("asr over!!!!!!!!!!");
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			// Step5 �ر�����
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
			String city = java.net.URLEncoder.encode("��´���", "utf-8");
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
