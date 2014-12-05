package com.ww.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ww.server.ProxyServer;
import com.ww.server.ServerProperties;

/**
 * 代理连接处理类<br>
 * 
 * 每次创建两个新对象,由线程池执行
 * 
 * 
 */
public class ConnectionHandler implements Runnable {

	private static Logger logger = LoggerFactory
			.getLogger(ConnectionHandler.class);

	/**
	 * 传入连接
	 */
	private Socket inSocket;
	/**
	 * 传出连接
	 */
	private Socket outSocket;
	/**
	 * 传入的输入流
	 */
	private InputStream srcIn;
	/**
	 * 传入的输出流
	 */
	private OutputStream srcOut;
	/**
	 * 传出的输入流
	 */
	private InputStream descIn;
	/**
	 * 传出的输出流
	 */
	private OutputStream descOut;
	/**
	 * 向上传输线程
	 */
	private TransportHandler upTrans;
	/**
	 * 向下传输线程
	 */
	private TransportHandler downTrans;

	/**
	 * 配置信息
	 */
	private ServerProperties properties;

	private ProxyServer server;

	/**
	 * 关闭状态
	 */
	private int closeState = 0;

	/**
	 * 构造方法
	 * 
	 * @param inSocket
	 *            传入Socket
	 * @param server
	 */
	public ConnectionHandler(Socket inSocket, ProxyServer server) {
		this.inSocket = inSocket;
		this.server = server;
		this.properties = server.getProperties();
	}

	/**
	 * 关闭socket
	 */
	public void closeSocket() {

		if (inSocket != null && !inSocket.isClosed()) {
			try {
				inSocket.close();
			} catch (Exception e) {
				logger.error("关闭源连接异常", e);
			}
		}

		if (outSocket != null && !outSocket.isClosed()) {
			try {
				outSocket.close();
			} catch (Exception e) {
				logger.error("关闭目标连接异常", e);
			}
		}

	}

	/**
	 * 传输结束时调用
	 * 
	 * @param tranThread
	 */
	public void transportEnd(TransportHandler tranThread) {

		// 如果是向上的则
		closeState++;
		if (tranThread == upTrans) {
			try {
				outSocket.shutdownOutput();
			} catch (IOException e) {
				logger.error("关闭向上的输出流", e);
			}
		}

		if (tranThread == downTrans) {
			try {
				inSocket.shutdownOutput();
			} catch (IOException e) {
				logger.error("关闭向下的输出流", e);
			}
		}

		if (closeState == 2) {
			if (logger.isInfoEnabled()) {
				logger.info(inSocket.getRemoteSocketAddress().toString()
						+ " --> "
						+ outSocket.getRemoteSocketAddress().toString()
						+ "  双方都已经传输完毕，关闭双方的socket");
			}
			closeSocket();
		}

	}

	/**
	 * 处理连接
	 */
	public void handleConnection() {

		int intmout = properties.getInSocketSoTimeOut();

		if (intmout >= 0) {
			try {
				inSocket.setSoTimeout(intmout);
			} catch (SocketException e2) {
				logger.error("设置inSocket读取超时时间异常", e2);
				closeSocket();
				return;
			}
		}

		// 从请求中获取输入输出流
		try {
			srcIn = inSocket.getInputStream();
			srcOut = inSocket.getOutputStream();
		} catch (Exception e) {
			logger.error("获取源流出错！关闭连接", e);
			closeSocket();
			return;
		}
		// 连接到远端地址
		try {

			outSocket = new Socket();

			int timeout = properties.getConnTimeOut();

			if (timeout <= 0) {
				outSocket.connect(new InetSocketAddress(properties
						.getForwardAddr(), properties.getForwardPort()));
			} else {
				outSocket.connect(
						new InetSocketAddress(properties.getForwardAddr(),
								properties.getForwardPort()), timeout);
			}

			// remoteConnection = new Socket(remoteAddr, remotePort);
		} catch (Exception e) {
			logger.error("建立到远端的连接出错！", e);
			closeSocket();
			return;
		}

		int outtmout = properties.getOutSocketSoTimeOut();

		if (outtmout >= 0) {
			try {
				outSocket.setSoTimeout(outtmout);
			} catch (SocketException e2) {
				logger.error("设置outSocket读取超时时间异常", e2);
				closeSocket();
				return;
			}
		}

		// 从远端连接中获取输入输出流
		try {
			descIn = outSocket.getInputStream();
			descOut = outSocket.getOutputStream();
		} catch (Exception e) {
			logger.error("获取目标流出错！关闭连接", e);
			closeSocket();
			return;
		}

		// 构建向上传输的线程
		upTrans = new TransportHandler(this, srcIn, descOut, this.inSocket
				.getRemoteSocketAddress().toString()
				+ " --> "
				+ this.outSocket.getRemoteSocketAddress().toString(),
				properties);

		// 构建向下传输的线程
		downTrans = new TransportHandler(this, descIn, srcOut, this.inSocket
				.getRemoteSocketAddress().toString()
				+ " <-- "
				+ this.outSocket.getRemoteSocketAddress().toString(),
				properties);

		server.executeTransportTask(upTrans);
		server.executeTransportTask(downTrans);

	}

	@Override
	public void run() {
		handleConnection();
	}

}
