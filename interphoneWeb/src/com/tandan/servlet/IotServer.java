package com.tandan.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class IotServer
 */
@WebServlet("/IotServer")
public class IotServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public IotServer() {
		super();

		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		ServletContext application = this.getServletContext();// ªÒ»°application
		String content = (String)application.getAttribute("list");
		
		String mp3url = request.getParameter("mp3url");
		if(mp3url != null) {
			System.out.println("mp3url = "+mp3url);
			application.setAttribute("list", mp3url);
			response.getWriter().write("OK,seting in list," + mp3url);
			return;
		}

		//String content = "http://192.168.1.100:8080/interphone/adf_music.mp3";
		//String content = "";
		if (content != null) {
			response.setContentLength(content.length());
			response.getWriter().append(content);
			response.flushBuffer();
			System.out.println("list = "+content);
			application.removeAttribute("list");
		} else {
			//System.out.println("list is null");
		}
		
		/*
		 * ServletOutputStream sos = response.getOutputStream(); String b =
		 * "12345678901"; sos.write(b.getBytes()); sos.flush(); sos.close();
		 */
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
