package com.tandan.servlet;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tandan.interphone.server.PlayTime;

/**
 * Servlet implementation class PlayTimeServer
 */
@WebServlet("/PlayTimeServer")
public class PlayTimeServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	PlayTime pt;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!启动线程");
		pt = new PlayTime();
		ServletContext application = this.getServletContext();// 获取application
		pt.setApplication(application);
		pt.start();
	}

	public void destroy() {
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!中断线程");
		pt.is_work = false;
		pt.interrupt();
		super.destroy();
	}

}
