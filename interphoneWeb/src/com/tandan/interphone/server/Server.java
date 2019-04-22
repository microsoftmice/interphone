package com.tandan.interphone.server;

import java.io.*;
import java.net.*;

public class Server {
	/**
	 * 监听的端口
	 */
	public static final int PORT = 8000;

	public static void main(String[] args) {
		System.out.println("服务器启动》》》》》》");
		Server server = new Server();
		server.init();
	}

	public void init() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			while (true) {
				// 一旦有堵塞，表示服务器与客户端获得了连接
				Socket client = serverSocket.accept();
				new HandlerThread(client);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class HandlerThread implements Runnable {
		private Socket socket;

		public HandlerThread(Socket socket) {
			this.socket = socket;
			new Thread(this).start();
		}

		@Override
		public void run() {
			try {
				// 读取客户端数据
				InputStream input = socket.getInputStream();
				byte b[] = new byte[1024000] ;
				int len = 0;
				int temp = 0; // 接收每一个读取进来的数据
				while ((temp = input.read()) != -1) {
					// 表示还有内容，文件没有读完
					b[len] = (byte) temp;
					len++;
				}
				System.out.println(new String(b,0,len));
				String akId = "LTAII6U8YIqqiA3r";
				String akSecret = "TMYaCXAmI1CXWQnBeQoDpVYHDZHLZo";

				// AsrDemo asrDemo = new AsrDemo(akId, akSecret);
				// asrDemo.startAsr(input);
				// asrDemo.shutDown();

			} catch (IOException e) {
				System.out.println("服务器异常：" + e.getMessage());
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {
						socket = null;
						System.out.println("服务端 finally 异常:" + e.getMessage());
					}
				}
			}
		}
	}

}