package com.ww.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端测试类
 * 
 * @author 313921
 * 
 */
public class ServerDemo {

	public static int DEFAULT_PORT = 9999;

	private static Logger logger = LoggerFactory.getLogger(ServerDemo.class);

	private int port;

	public ServerDemo() {
		this.port = DEFAULT_PORT;
	}

	public ServerDemo(int port) {
		this.port = port;
	}

	class handleThread extends Thread {
		Socket conn;

		public handleThread(Socket s) {
			this.conn = s;
		}

		@Override
		public void run() {
			handleConnections();
		}

		public void handleConnections() {
			// System.out.println("设置服务端读取数据超时时间");
			// try {
			// conn.setSoTimeout(5000);
			// } catch (SocketException e2) {
			// e2.printStackTrace();
			// }
			
			InputStream in = null;
			try {
				in = conn.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
				try {
					conn.close();
				} catch (IOException e1) {
					logger.error(conn.toString() + "  获取输入流异常", e1);
				}
				return;
			}
			OutputStream out = null;
			try {
				out = conn.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
				try {
					conn.close();
				} catch (IOException e1) {
					logger.error(conn.toString() + "  获取输出流异常", e1);
				}
				return;
			}
			byte[] b = new byte[1024];
			try {
				int length = 0;
				while ((length = in.read(b)) != -1) {

					String content = new String(b, 0, length);
					System.out.println("服务器端收到" + length + "个字节\n    字符形式为："
							+ content + "\n    字节形式为："
							+ Arrays.toString(Arrays.copyOf(b, length)));

					out.write(("server : " + content).getBytes());

					// out.write(b, 0, length);

					out.flush();
				}
			} catch (IOException e) {
				logger.error("读取写入异常", e);
			}

			try {
				System.out.println("读取结束,关闭输出");
				conn.shutdownOutput();

				System.out.println("服务端Cocket关闭");
				conn.close();
			} catch (IOException e) {
				logger.error("服务端关闭异常", e);
			}

		}

	}

	public void startServer() throws IOException {

		ServerSocket server = new ServerSocket(port, 3);
		logger.info("服务端启动,端口:" + port);

		while (true) {

			Socket conn = server.accept();

			logger.info("###  收到新的连接     ###  来自："
					+ conn.getRemoteSocketAddress().toString());

			// 处理当前连接
			new handleThread(conn).start();
		}
	}

	public static void main(String[] args) {
		ServerDemo sd = new ServerDemo();

		try {
			sd.startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
