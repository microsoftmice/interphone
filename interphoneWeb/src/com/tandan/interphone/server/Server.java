package com.tandan.interphone.server;

import java.io.*;
import java.net.*;

public class Server {
	/**
	 * �����Ķ˿�
	 */
	public static final int PORT = 8000;

	public static void main(String[] args) {
		System.out.println("����������������������");
		Server server = new Server();
		server.init();
	}

	public void init() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			while (true) {
				// һ���ж�������ʾ��������ͻ��˻��������
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
				// ��ȡ�ͻ�������
				InputStream input = socket.getInputStream();
				byte b[] = new byte[1024000] ;
				int len = 0;
				int temp = 0; // ����ÿһ����ȡ����������
				while ((temp = input.read()) != -1) {
					// ��ʾ�������ݣ��ļ�û�ж���
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
				System.out.println("�������쳣��" + e.getMessage());
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {
						socket = null;
						System.out.println("����� finally �쳣:" + e.getMessage());
					}
				}
			}
		}
	}

}