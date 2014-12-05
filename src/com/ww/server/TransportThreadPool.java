package com.ww.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 传输数据线程池
 * 
 * @author 313921
 * 
 */
public class TransportThreadPool {

	@SuppressWarnings("unused")
	private ProxyServer server;

	private ThreadPoolExecutor threadPool;

	public TransportThreadPool(ProxyServer server) {

		this.server = server;
		ServerProperties properties = server.getProperties();

		// 核心值大小
		int coreSize = properties.getCorePoolSize();

		// 最大值大小
		int maxSize = properties.getMaxPoolSize();

		// 终止多余的空闲线程等待新任务的最长时间
		// 默认单位秒
		int keepAliveTime = properties.getKeepAliveTime();

		// 最大队列深度
		int queueSize = properties.getQueueSize();

		threadPool = new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize),
				new MyRejectePolicy());
	}

	/**
	 * 执行任务
	 * 
	 * <pre>
	 *  在将来某个时间执行给定任务。
	 *  可以在新线程中或者在现有池线程中执行该任务。 
	 *  如果无法将任务提交执行，或者因为此执行程序已关闭，或者因为已达到其容量
	 * 则该任务由当前 MyRejectePolicy 处理
	 * </pre>
	 * 
	 * @param command
	 */
	public void execute(Runnable command) {
		threadPool.execute(command);
	}

	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

}
