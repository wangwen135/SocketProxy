package com.ww.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ww.utils.PropertiesUtils;

/**
 * 配置对象
 * 
 * @author 313921
 * 
 */
public class ServerProperties {

	private static Logger logger = LoggerFactory
			.getLogger(ServerProperties.class);

	private ServerProperties() {
	}

	/**
	 * 最大连接数
	 */
	private int maxConnections;

	/**
	 * 监听端口
	 */
	private int listenPort;

	/**
	 * 监听地址
	 */
	private String listenAddr;

	/**
	 * 转发地址
	 */
	private String forwardAddr;

	/**
	 * 转发端口
	 */
	private int forwardPort;

	/**
	 * 连接超时
	 */
	private int connTimeOut;

	/**
	 * inSocket读取数据超时时间
	 */
	private int inSocketSoTimeOut = 0;

	/**
	 * outSocket读取数据超时时间
	 */
	private int outSocketSoTimeOut = 0;

	/**
	 * 缓存大小
	 */
	private int bufferSize = 10240;

	/**
	 * 池中所保存的线程数，包括空闲线程
	 */
	private int corePoolSize;

	/**
	 * 池中允许的最大线程数
	 */
	private int maxPoolSize;

	/**
	 * 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间
	 */
	private int keepAliveTime;

	/**
	 * 队列深度
	 */
	private int queueSize;

	/**
	 * 0 不记录日志<br>
	 * 1 字节16进制编码+字符串<br>
	 * 2 字节16进制编码<br>
	 * 3 字符串（默认编码）<br>
	 * 4 记录在tmp文件中
	 */
	private String logMode = "0";

	/**
	 * 延时时间 ms 测试用的
	 */
	private int delayTime = 0;

	/**
	 * 是否启用监控
	 */
	private boolean monitor = false;

	/**
	 * 监控时间间隔
	 */
	private int interval;

	/**
	 * <pre>
	 * 从配置文件中获取配置对象
	 * 如果必须的配置为空则抛出 IllegalArgumentException
	 * </pre>
	 * 
	 * @return
	 */
	public static ServerProperties get4properties() {
		ServerProperties p = new ServerProperties();

		logger.info("##### 开始读取配置文件 #####");

		String plport = PropertiesUtils.getValue("listenPort");
		if (plport == null) {
			throw new IllegalArgumentException("监听端口： listenPort 未配置");
		}

		p.setListenPort(Integer.parseInt(plport));
		logger.info("监听端口：" + plport);

		String pladdr = PropertiesUtils.getValue("listenAddr");
		if (pladdr == null) {
			logger.info("未配置监听地址:listenAddr，默认在所有地址上进行监听");
		} else {
			p.setListenAddr(pladdr);
			logger.info("监听地址：" + pladdr);
		}

		String pmcon = PropertiesUtils.getValue("maxConnections");
		if (pmcon == null) {
			throw new IllegalArgumentException("最大连接数： maxConnections 未配置");
		}

		p.setMaxConnections(Integer.parseInt(pmcon));
		logger.info("最大连接数：" + pmcon);

		String pfaddr = PropertiesUtils.getValue("forwardAddr");

		if (pfaddr == null) {
			throw new IllegalArgumentException("转发地址： forwardAddr 未配置");
		}

		p.setForwardAddr(pfaddr);
		logger.info("转发地址：" + pfaddr);

		String pfport = PropertiesUtils.getValue("forwardPort");

		if (pfport == null) {
			throw new IllegalArgumentException("转发端口： forwardPort 未配置");
		}

		p.setForwardPort(Integer.parseInt(pfport));
		logger.info("转发端口：" + pfport);

		String pctout = PropertiesUtils.getValue("connTimeOut");
		if (pctout != null) {
			p.setConnTimeOut(Integer.parseInt(pctout));
			logger.info("连接超时时间:" + pctout);
		}

		Integer inSocketSoTimeOut = PropertiesUtils
				.getIntValue("inSocketSoTimeOut");
		if (inSocketSoTimeOut == null) {
			logger.info("inSocket读取数据超时时间未配置，默认：inSocketSoTimeOut="
					+ p.getInSocketSoTimeOut());
		} else {
			p.setInSocketSoTimeOut(inSocketSoTimeOut);
			logger.info("inSocket读取数据超时时间：" + inSocketSoTimeOut);
		}

		Integer outSocketSoTimeOut = PropertiesUtils
				.getIntValue("outSocketSoTimeOut");
		if (outSocketSoTimeOut == null) {
			logger.info("outSocket读取数据超时时间未配置，默认：outSocketSoTimeOut="
					+ p.getOutSocketSoTimeOut());
		} else {
			p.setOutSocketSoTimeOut(outSocketSoTimeOut);
			logger.info("outSocket读取数据超时时间：" + outSocketSoTimeOut);
		}

		String pbsize = PropertiesUtils.getValue("bufferSize");
		if (pbsize == null) {
			logger.info("未配置缓冲区大小，使用默认配置：    bufferSize=" + p.getBufferSize());
		} else {
			p.setBufferSize(Integer.parseInt(pbsize));
			logger.info("缓冲区大小：" + pbsize);
		}

		// @@@@@@@@@@线程池配置
		String corePoolSize = PropertiesUtils
				.getValue("ThreadPool.corePoolSize");
		if (corePoolSize == null) {
			throw new IllegalArgumentException("线程池.核心线程数量： corePoolSize 未配置");
		}
		p.setCorePoolSize(Integer.parseInt(corePoolSize));
		logger.info("线程池.核心线程数量：" + corePoolSize);

		String maxPoolSize = PropertiesUtils.getValue("ThreadPool.maxPoolSize");
		if (maxPoolSize == null) {
			throw new IllegalArgumentException("线程池.最大线程数量： maxPoolSize 未配置");
		}
		p.setMaxPoolSize(Integer.parseInt(maxPoolSize));
		logger.info("线程池.最大线程数量：" + maxPoolSize);

		String keepAliveTime = PropertiesUtils
				.getValue("ThreadPool.keepAliveTime");
		if (keepAliveTime == null) {
			throw new IllegalArgumentException("线程池.保存活动时间： keepAliveTime 未配置");
		}
		p.setKeepAliveTime(Integer.parseInt(keepAliveTime));
		logger.info("线程池.保存活动时间：" + keepAliveTime);

		String queueSize = PropertiesUtils.getValue("ThreadPool.queueSize");
		if (queueSize == null) {
			throw new IllegalArgumentException("线程池.队列深度： queueSize 未配置");
		}
		p.setQueueSize(Integer.parseInt(queueSize));
		logger.info("线程池.队列深度：" + queueSize);

		// @@@@@@@@@@线程池配置

		String plmode = PropertiesUtils.getValue("logMode");
		if (plmode == null) {
			logger.info("未配置日志记录模式，使用默认配置：    logMode=" + p.getLogMode());
		} else {
			p.setLogMode(plmode);
			logger.info("日志记录模式：" + plmode);
		}

		String pdtime = PropertiesUtils.getValue("delayTime");
		if (pdtime == null) {
			logger.info("未配置延时时间，使用默认配置：    delayTime=" + p.getDelayTime());
		} else {
			p.setDelayTime(Integer.parseInt(pdtime));
			logger.info("延时时间：" + pdtime);
		}

		// 是否启用监控
		String monitor = PropertiesUtils.getValue("monitor");
		if (monitor != null && "TRUE".equalsIgnoreCase(monitor)) {
			
			Integer interval = PropertiesUtils.getIntValue("interval");
			if(interval == null){
				throw new IllegalArgumentException("启用了监控，但监控时间间隔： interval 未配置");
			}
			p.setMonitor(true);
			p.setInterval(interval);
			
		}

		logger.info("##### 读取配置文件完成 #####");

		return p;

	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	public String getListenAddr() {
		return listenAddr;
	}

	public void setListenAddr(String listenAddr) {
		this.listenAddr = listenAddr;
	}

	public String getForwardAddr() {
		return forwardAddr;
	}

	public void setForwardAddr(String forwardAddr) {
		this.forwardAddr = forwardAddr;
	}

	public int getForwardPort() {
		return forwardPort;
	}

	public void setForwardPort(int forwardPort) {
		this.forwardPort = forwardPort;
	}

	public int getConnTimeOut() {
		return connTimeOut;
	}

	public void setConnTimeOut(int connTimeOut) {
		this.connTimeOut = connTimeOut;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public String getLogMode() {
		return logMode;
	}

	public void setLogMode(String logMode) {
		this.logMode = logMode;
	}

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public int getInSocketSoTimeOut() {
		return inSocketSoTimeOut;
	}

	public void setInSocketSoTimeOut(int inSocketSoTimeOut) {
		this.inSocketSoTimeOut = inSocketSoTimeOut;
	}

	public int getOutSocketSoTimeOut() {
		return outSocketSoTimeOut;
	}

	public void setOutSocketSoTimeOut(int outSocketSoTimeOut) {
		this.outSocketSoTimeOut = outSocketSoTimeOut;
	}

	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	
}
