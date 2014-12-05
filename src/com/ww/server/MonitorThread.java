package com.ww.server;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监控线程
 * 
 * @author 313921
 * 
 */
public class MonitorThread extends Thread {

	private static Logger logger = LoggerFactory.getLogger(MonitorThread.class);

	private ProxyServer server;

	public MonitorThread(ProxyServer server) {
		this.server = server;
	}

	private String getLog() {
		ThreadPoolExecutor threadPool = server.getThreadPool();

		ThreadPoolExecutor tranThreadPool = server.getTransportThreadPool()
				.getThreadPool();

		StringBuffer log = new StringBuffer();

		log.append("\n####接收连接线程池####");
		log.append("\n\t执行任务线程数：" + threadPool.getActiveCount());
		log.append("\n\t任务数：" + threadPool.getTaskCount());
		log.append("\n\t已完成执行任务总数：" + threadPool.getCompletedTaskCount());

		log.append("\n\n####传输处理线程池####");
		log.append("\n\t核心线程数：" + tranThreadPool.getCorePoolSize());
		log.append("\n\t允许的最大线程数："+ tranThreadPool.getMaximumPoolSize());
		log.append("\n\t池中当前线程数：" + tranThreadPool.getPoolSize());
		log.append("\n\t曾经同时位于池中的最大线程数:" + tranThreadPool.getLargestPoolSize());
		log.append("\n\t执行任务线程数：" + tranThreadPool.getActiveCount());
		log.append("\n\t任务数：" + tranThreadPool.getTaskCount());
		log.append("\n\t已完成执行任务总数：" + tranThreadPool.getCompletedTaskCount());

		return log.toString();
	}

	@Override
	public void run() {

		while (server.isRunFlag()) {
			try {
				sleep(server.getProperties().getInterval() * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 为了显示记录
			logger.error(getLog());

		}

	}
}
