package com.ww.server;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * 用于被拒绝任务的处理程序
 * 
 * </pre>
 * 
 * @author 313921
 * 
 */
public class MyRejectePolicy implements RejectedExecutionHandler {

	private static Logger logger = LoggerFactory
			.getLogger(MyRejectePolicy.class);

	private long sleepTime = 100;

	public MyRejectePolicy() {

	}

	public MyRejectePolicy(Long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		if (!e.isShutdown()) {

			logger.error("！！！！！出现任务被拒绝的情况，如果频繁出现，请调整参数！！！！！");

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			e.execute(r);// 重新执行任务
		}
	}
}