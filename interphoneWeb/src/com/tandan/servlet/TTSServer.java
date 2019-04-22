package com.tandan.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tandan.interphone.server.TTS;

/**
 * Servlet implementation class TTSServer
 */
@WebServlet("/TTSServer")
public class TTSServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TTSServer() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		//ttsDemo.shutDown();
	}
	public String getText()  {
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH点mm分");// 设置日期格式
		String tts_text = "当前时间" + df.format(new Date()) + "";
		System.out.println(tts_text);
//		String city;
//		String result ="";
//		try {
//			city = java.net.URLEncoder.encode("长沙", "utf-8");
//			String apiUrl = String.format("https://www.sojson.com/open/api/weather/json.shtml?city=%s", city);
//			URL url = new URL(apiUrl);
//			URLConnection open = url.openConnection();
//			InputStream input = open.getInputStream();
//			result = org.apache.commons.io.IOUtils.toString(input, "utf-8");
//			input.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String pm25 = result.substring(result.indexOf("pm25") + 6, result.indexOf("pm25") + 8);
//		String wendu = result.substring(result.indexOf("wendu") + 8, result.indexOf("wendu") + 10);
//		String forecast = result.substring(result.indexOf("forecast"), result.indexOf("]"));
//		String high = forecast.substring(forecast.indexOf("high") + 10, forecast.indexOf("high") + 12);
//		String low = forecast.substring(forecast.indexOf("low") + 9, forecast.indexOf("low") + 11);
//		String type = forecast.substring(forecast.indexOf("type") + 7, forecast.indexOf("type") + 8);
//		String weather = "今日长沙天气" + type + ",最高温度" + high + "度，最低温度" + low + "度 ，实时温度" + wendu + "度,PM2.5指数" + pm25;
//		System.out.println(weather);
		//tts_text += weather;
		return tts_text;
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.setContentType("audio/wav; charset=utf-8");
		ServletOutputStream sos = response.getOutputStream();
		
		String akId = "LTAII6U8YIqqiA3r";
		String akSecret = "TMYaCXAmI1CXWQnBeQoDpVYHDZHLZo";
		TTS ttsDemo = new TTS(akId, akSecret);
//		File file = new File("tts1.wav");
//		if (!file.exists()) {
//			try {
//				file.createNewFile();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		OutputStream fileOutputStream = new FileOutputStream(file);
//		ttsDemo.os = fileOutputStream;
		ttsDemo.os = sos;
		ttsDemo.tts_text=getText();
		ttsDemo.startTTS();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
