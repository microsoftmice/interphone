package com.tandan.interphone.server;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.aliyuncs.exceptions.ClientException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * SpeechSynthesizerDemo class
 *
 * �����ϳɣ�TTS��Demo
 * 
 * @author siwei
 * @date 2018/6/25
 */
public class TTS2 {
	private String appKey;
	NlsClient client;
	static String filepath = "tts1.wav";
	String ttsText = "û���κ�����";
	String token;

	public TTS2() {
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

	private static SpeechSynthesizerListener getSynthesizerListener() {
		SpeechSynthesizerListener listener = null;

		try {
			listener = new SpeechSynthesizerListener() {
				File f = new File(filepath);
				FileOutputStream fout = new FileOutputStream(f);

				// �����ϳɽ���
				@Override
				public void onComplete(SpeechSynthesizerResponse response) {
					// �¼����� SynthesisCompleted
					System.out.println("name: " + response.getName() +
					// ״̬�� 20000000 ��ʾʶ��ɹ�
					", status: " + response.getStatus() +
					// �����ϳ��ļ�·��
					", output file :" + f.getAbsolutePath());
				}

				// �����ϳɵ���������������
				@Override
				public void onMessage(ByteBuffer message) {
					try {
						byte[] bytesArray = new byte[message.remaining()];
						message.get(bytesArray, 0, bytesArray.length);
						// System.out.println("write array:" + bytesArray.length);
						fout.write(bytesArray);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listener;
	}

	public void process() {
		SpeechSynthesizer synthesizer = null;
		try {
			AccessToken accessToken = AccessToken.apply("LTAIjTBoNOkZnbuT", "hyi1Ih3YhwV9Xk5fx4nFmyMgnn5F4Q");
			String token = accessToken.getToken();
			client.setToken(token);
			// Step1 ����ʵ��,��������
			synthesizer = new SpeechSynthesizer(client, getSynthesizerListener());
			synthesizer.setAppKey(appKey);
			// ���÷�����Ƶ�ı����ʽ
			synthesizer.setFormat(OutputFormatEnum.WAV);
			// ���÷�����Ƶ�Ĳ�����
			synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
			// �������������ϳɵ��ı�
			synthesizer.setText(ttsText);

			// Step2 �˷��������ϲ����������л�Ϊjson���͸������,���ȴ������ȷ��
			synthesizer.start();
			// Step3 �ȴ������ϳɽ���
			synthesizer.waitForComplete();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			// Step4 �ر�����
			if (null != synthesizer) {
				synthesizer.close();
			}
		}
	}

	public void shutdown() {
		client.shutdown();
	}

	public static void main(String[] args) throws Exception {
		TTS2 demo = new TTS2();
		// demo.ttsText="";
		// demo.filepath="";
		demo.process();
		demo.shutdown();
	}
}
