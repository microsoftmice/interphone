package com.tandan.interphone.server;

import com.alibaba.idst.nls.protocol.NlsResponse;
import com.alibaba.nls.client.AccessToken;
import com.aliyuncs.exceptions.ClientException;
import com.alibaba.idst.nls.NlsClient;
import com.alibaba.idst.nls.NlsFuture;
import com.alibaba.idst.nls.event.NlsEvent;
import com.alibaba.idst.nls.event.NlsListener;
import com.alibaba.idst.nls.protocol.NlsRequest;
import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class TTS implements NlsListener {
	private NlsClient client = new NlsClient();
	private String akId;
	private String akSecret;
	public String tts_text = "";
	public OutputStream os;
	
	public TTS(String akId, String akSecret) {
		System.out.println("init Nls client...");
		this.akId = akId;
		this.akSecret = akSecret;
		// 鍒濆鍖朜lsClient
		client.init();
	}

	public void shutDown() {
		System.out.println("close NLS client");
		// 鍏抽棴瀹㈡埛绔苟閲婃斁璧勬簮
		client.close();
		//System.out.println("demo done");
	}

	public void startTTS() throws Exception {
		AccessToken accessToken = AccessToken.apply("LTAIjTBoNOkZnbuT", "hyi1Ih3YhwV9Xk5fx4nFmyMgnn5F4Q");
		String token = accessToken.getToken();
		long expireTime = accessToken.getExpireTime();
		NlsRequest req = new NlsRequest();
		String appkey = "nls-service";
		req.setAppKey(appkey); // 璁剧疆璇煶鏂囦欢鏍煎紡
		req.setTtsRequest(tts_text); // 浼犲叆娴嬭瘯鏂囨湰锛岃繑鍥炶闊崇粨鏋�
		req.setTtsEncodeType("wav");// 杩斿洖璇煶鏁版嵁鏍煎紡锛屾敮鎸乸cm,wav.alaw
		req.setTtsVolume(50); // 闊抽噺澶у皬榛樿50锛岄槇鍊�0-100
		req.setTtsSpeechRate(-300); // 璇�燂紝闃堝��-500~500
		// req.setTtsBackgroundMusic(1, 0);// 鑳屾櫙闊充箰缂栧彿,鍋忕Щ閲�
		req.authorize("itcsHfQc6V2Anz5p", akSecret); // 璇锋浛鎹负鐢ㄦ埛鐢宠鍒扮殑Access Key ID鍜孉ccess Key Secret
		try {
			NlsFuture future = client.createNlsFuture(req, this); // 瀹炰緥鍖栬姹�,浼犲叆璇锋眰鍜岀洃鍚櫒
			int total_len = 0;
			byte[] data;
			while ((data = future.read()) != null) {
				os.write(data, 0, data.length);
				total_len += data.length;
				
			}
			os.flush();
			//fileOutputStream.close();
			//////////////////////////////////////////////////////////////////////////////////
			System.out.println("tts audio file size is :" + total_len);
			future.await(100000); // 璁剧疆鏈嶅姟绔粨鏋滆繑鍥炵殑瓒呮椂鏃堕棿
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReceived(NlsEvent e) {
		NlsResponse response = e.getResponse();
		String result = "";
		int statusCode = response.getStatus_code();
		if (response.getTts_ret() != null) {
			//result += "\nget tts result: statusCode=[" + statusCode + "], " + response.getTts_ret();
		}
		if (result != null) {
			//System.out.println(result);
		} else {
			//System.out.println(response.jsonResults.toString());
		}
	}

	@Override
	public void onOperationFailed(NlsEvent e) {
		// 璇嗗埆澶辫触鐨勫洖璋�
		System.out.print("on operation failed: ");
		System.out.println(e.getErrorMessage());
	}

	@Override
	public void onChannelClosed(NlsEvent e) {
		// socket 杩炴帴鍏抽棴鐨勫洖璋�
		System.out.println("on websocket closed.");
	}

	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		String akId = "LTAII6U8YIqqiA3r";
		String akSecret = "TMYaCXAmI1CXWQnBeQoDpVYHDZHLZo";
		TTS ttsDemo = new TTS(akId, akSecret);
		File file = new File("tts1.wav");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		OutputStream fileOutputStream = new FileOutputStream(file);
		ttsDemo.os = fileOutputStream;
		// ttsDemo.speak();
		ttsDemo.tts_text="123456789";
		ttsDemo.startTTS();
		ttsDemo.shutDown();
		
	}

	// public void speak() throws Exception {
	// AudioFormat audioFormat =
	//// new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100F,
	//// 16, 2, 1, 44100F, false);
	//// new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,16000F, 16, 2, 4,16000F,
	//// true);
	// new AudioFormat(16000, 8, 2, true, false);
	// DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
	//
	// info = new DataLine.Info(SourceDataLine.class, audioFormat);
	// sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
	// sourceDataLine.open(audioFormat);
	// sourceDataLine.start();
	// FloatControl fc = (FloatControl)
	// sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
	// double value = 2;
	// float dB = (float) (Math.log(value == 0.0 ? 0.0001 : value) / Math.log(10.0)
	// * 20.0);
	// fc.setValue(dB);
	// int nByte = 0;
	// final int bufSize = 4 * 100;
	// byte[] buffer = new byte[bufSize];
	//
	// }
}