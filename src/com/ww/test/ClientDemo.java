package com.ww.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端
 * 
 * @author 313921
 * 
 */
public class ClientDemo {

	private int port = 9999;

	private static Logger logger = LoggerFactory.getLogger(ClientDemo.class);

	private String host = "127.0.0.1";

	private Socket c = null;

	private BufferedReader consolein;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ClientDemo() {

	}

	public ClientDemo(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static void main(String[] args) {
		ClientDemo c = new ClientDemo();
		c.run();
	}

	public void run() {
		try {
			c = new Socket(host, port);

			logger.info("建立到主机：" + c.getRemoteSocketAddress().toString()
					+ " 的连接...");

			System.out.println("本地端口：" + c.getLocalPort());

			//
			// c.setSoTimeout(10000);
			// System.out.println("读取超时：" + c.getSoTimeout());
			//

			InputStream is = c.getInputStream();
			OutputStream os = c.getOutputStream();

			readThread rt = new readThread(is, this);
			rt.start();

			logger.debug("创建读取服务器信息的线程[" + rt.getId() + "]");

			consolein = new BufferedReader(new InputStreamReader(System.in));

			PrintStream ps = new PrintStream(os);

			String line = null;

			while ((line = consolein.readLine()) != null) {

				if (line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("Q")) {
					logger.debug("客户端主动退出程序：" + line);

					// logger.debug("将此套接字的输入流置于“流的末尾”。");
					// c.shutdownInput();

					logger.debug("禁用此套接字的输出流");
					c.shutdownOutput();

					// logger.debug("关闭此套接字");
					// c.close();

					break;

				} else if (line.equalsIgnoreCase("cmd1")) {
					// 测试
					logger.debug("shutdownInput");
					c.shutdownInput();

					// 再读取时会提示异常
					// int i = c.getInputStream().read();

				} else if (line.equalsIgnoreCase("cmd2")) {
					// 测试
					logger.debug("shutdownOutput");

					// 服务端会读到 EOF
					c.shutdownOutput();

				} else if (line.equalsIgnoreCase("cmd3")) {
					// 测试
					logger.debug("另外一个线程在inputstream上阻塞,直接关闭试试");

					// 看看另外一个线程是否会报错
					is.close();

				} else {
					ps.print(line);
					ps.flush();
				}

			}
		} catch (UnknownHostException e) {
			logger.error("无法确定主机的 IP 地址", e);
		} catch (IOException e) {
			logger.error("I/O 错误", e);
			exit();
		}

	}

	/**
	 * 退出
	 * 
	 */
	public void exit() {

		logger.debug("exit方法被调用 ");

		try {
			if (c != null && !c.isClosed())
				c.close();
		} catch (IOException e) {
			logger.error("关闭套接字异常!", e);
		}

	}
}

/**
 * 将输入输出到控制台，并且在读到eof时通知关闭socket
 * 
 */
class readThread extends Thread {

	private static Logger logger = LoggerFactory.getLogger(readThread.class);
	private ClientDemo cd;

	private InputStream is;

	private String headStr = "S：";

	public String getHeadStr() {
		return headStr;
	}

	public void setHeadStr(String headStr) {
		this.headStr = headStr;
	}

	public readThread(InputStream is, ClientDemo cd) {
		this.is = is;
		this.cd = cd;
	}

	public readThread(InputStream is, ClientDemo cd, String headStr) {
		this.is = is;
		this.cd = cd;
		this.headStr = headStr;
	}

	@Override
	public void run() {

		try {

			byte[] b = new byte[1024];
			int length = 0;
			while ((length = is.read(b)) > 0) {
				System.out.println("客户端收到" + length + "个字节\n    字符形式为："
						+ new String(b, 0, length) + "\n    字节形式为："
						+ Arrays.toString(Arrays.copyOf(b, length)));
			}
			logger.debug("socket输入流已到达流末尾，程序将结束");

			cd.exit();

			// java.net.SocketException: socket closed
		} catch (IOException e) {
			logger.error("socket输入流发生 I/O 异常", e);

			cd.exit();

			// 此时可能另外一个线程在阻塞

			System.exit(1);
		}

	}
}
