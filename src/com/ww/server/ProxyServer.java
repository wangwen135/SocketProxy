package com.ww.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ww.handler.ConnectionHandler;
import com.ww.handler.TransportHandler;

/**
 * 代理服务类
 * 
 * 
 */
public class ProxyServer {

	private static Logger logger = LoggerFactory.getLogger(ProxyServer.class);

	private ServerProperties properties;

	/**
	 * 只需要一个比较简单的池即可 任务都比较简单,只是创建线程
	 */
	private ThreadPoolExecutor threadPool;

	private TransportThreadPool transportThreadPool;

	/**
	 * 运行标志
	 */
	private boolean runFlag = true;

	public ServerProperties getProperties() {
		return properties;
	}

	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	public TransportThreadPool getTransportThreadPool() {
		return transportThreadPool;
	}

	public ProxyServer(ServerProperties properties) {
		this.properties = properties;

		int maxConn = properties.getMaxConnections();

		// 创建一个固定容量的线程池

		// 最大线程数据
		// 最大队列深度
		// 当任务被拒绝时等待
		threadPool = new ThreadPoolExecutor(maxConn, maxConn, 0L,
				TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(maxConn),
				new MyRejectePolicy());

		transportThreadPool = new TransportThreadPool(this);

		// 开始监控

		if (properties.isMonitor()) {
			new MonitorThread(this).start();
		}

	}

	/**
	 * 创建服务器套接字，接受请求
	 */
	public void acceptConnections() {

		try {
			// 创建服务器套接字
			ServerSocket server;

			String listenAddr = properties.getListenAddr();

			int port = properties.getListenPort();

			int maxCon = properties.getMaxConnections();

			if (listenAddr == null) {
				server = new ServerSocket(port, maxCon);
				logger.info("服务已经创建,在端口:" + port);
			} else {
				server = new ServerSocket(port, maxCon,
						InetAddress.getByName(listenAddr));
				logger.info("服务已经创建,在:" + listenAddr + " " + port);
			}

			Socket inSocket = null;
			while (runFlag) {
				// 侦听并接受到此套接字的连接
				try {
					inSocket = server.accept();
				} catch (IOException e) {
					logger.error("等待连接时发生 I/O 错误", e);
					runFlag = false;
					continue;
				}
				logger.info("###  收到新的连接     ###  来自："
						+ inSocket.getRemoteSocketAddress().toString());
				// 处理当前连接
				handleConnection(inSocket);
			}
		} catch (Exception e) {
			runFlag = false;
			logger.error("服务端错误", e);
		}
	}

	/**
	 * 处理当前连接
	 * 
	 * @param inSocket
	 *            当前进入的连接
	 */
	private void handleConnection(Socket inSocket) {
		threadPool.execute(new ConnectionHandler(inSocket, this));
	}

	/**
	 * 执行传输任务
	 * 
	 * @param handler
	 */
	public void executeTransportTask(TransportHandler handler) {
		transportThreadPool.execute(handler);
	}

	public static void main(String[] args) {
		logger.info("启动程序......");

		ServerProperties p;
		try {
			p = ServerProperties.get4properties();
		} catch (Exception e) {
			logger.error("程序启动后，读取配置文件出错！", e);
			return;
		}

		ProxyServer server = new ProxyServer(p);

		logger.info("创建服务器套接字，开始接受请求 ...");

		server.acceptConnections();

	}

	public boolean isRunFlag() {
		return runFlag;
	}

	public void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

}
