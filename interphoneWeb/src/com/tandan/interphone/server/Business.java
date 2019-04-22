package com.tandan.interphone.server;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Business {

	public String run(String content) throws Exception {

		String reply = "";
		if (content.contains("主控台")) {
			reply = checkin(content);
		}
		if (content.contains("汨罗天气")) {
			reply = weather("101251004");
		}
		if (content.contains("长沙天气")) {
			reply = weather("101250101");
		}
		if (content.contains("五一大道")) {
			reply = trafficinfo("五一大道");
		}
		if (content.contains("南二环")) {
			reply = trafficinfo("南二环");
		}
		if (content.contains("三一大道")) {
			reply = trafficinfo("三一大道");
		}
		if (content.contains("远大路")) {
			reply = trafficinfo("远大一路");
		}
		if (content.contains("芙蓉")) {
			reply = trafficinfo("芙蓉大道");
		}		
		if (content.contains("三叉")) {
			reply = trafficinfo("北二环");
		}				
		return reply;
	}

	public String checkin(String c) {
		String reply = "";
		c = c.replaceAll("主控台", "ABC");
		int no = c.indexOf("呼号");
		if (no > 0) {
			c = c.substring(no);
		}
		c += ",主控抄收是否正确.";
		return reply;
	}

	public String weather(String city) throws Exception {
		String result = "";
		// city = java.net.URLEncoder.encode(city, "utf-8");
		// String apiUrl =
		// String.format("https://www.sojson.com/open/api/weather/json.shtml?city=%s",
		// city);
		URL url = new URL("http://t.weather.sojson.com/api/weather/city/" + city);
		URLConnection open = url.openConnection();
		InputStream input = open.getInputStream();
		result = org.apache.commons.io.IOUtils.toString(input, "utf-8");
		input.close();
		System.out.println("weather result: " + result);
		JSONObject forecast = JSON.parseObject(result).getJSONObject("data").getJSONArray("forecast").getJSONObject(0);
		String fx = forecast.getString("fx");
		String high = forecast.getString("high");
		String low = forecast.getString("low");
		String type = forecast.getString("type");
		String notice = forecast.getString("notice");
		String weather = "今日天气" + type + "," + fx + ",最高温度" + high + "度，最低温度" + low + "度 ," + notice;
		System.out.println(weather);
		return weather;
	}

	public String trafficinfo(String way) throws Exception {
		String city = java.net.URLEncoder.encode(way, "utf-8");
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
		return description;
	}

	public static void main(String[] args) throws Exception {
		Business business = new Business();
		String reply = business.run("汨罗天气");
		System.out.println(reply);
	}
}
