package com.tandan.servlet;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tandan.interphone.server.ASR2;
import com.tandan.interphone.server.Asr;

/**
 * Servlet implementation class AsrServer
 */
@WebServlet("/AsrServer")
public class AsrServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ASR2 asr2 = new ASR2();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AsrServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
        asr2.shutdown();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		ServletInputStream ris = request.getInputStream();
		/*
		try {
		String akId = "LTAII6U8YIqqiA3r";
		String akSecret = "TMYaCXAmI1CXWQnBeQoDpVYHDZHLZo";
		Asr asr = new Asr(akId, akSecret);
		asr.is = ris;
		asr.init();
		asr.startAsr();
		asr.shutDown();
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		*/
		
		ServletContext application = this.getServletContext();// ªÒ»°application
		asr2.setApplication(application);
		asr2.process(ris);

		String rate = request.getHeader("x-audio-channel");
		response.getWriter().append("x-audio-channel==" + rate);
		response.setStatus(200);
		response.setHeader("Content-length", "18");
		response.flushBuffer();
		
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

	/**
	 * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse)
	 */
	protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
