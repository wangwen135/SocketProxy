package com.ww.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ww.server.ServerProperties;
import com.ww.utils.StringTool;

/**
 * 数据传输处理
 * 
 * <pre>
 * 从 InputStream 中读取字节，写入 OutputStream 中
 * </pre>
 * 
 * @author 313921
 * 
 */
public class TransportHandler implements Runnable {

	private static Logger logger = LoggerFactory
			.getLogger(TransportHandler.class);

	/**
	 * 名称
	 */
	private String tranName;

	/**
	 * 缓存大小
	 */
	private int bufferSize = 2048;

	/**
	 * 延时时间 ms
	 */
	private int delayTime = 0;

	/**
	 * 代理连接处理对象
	 */
	private ConnectionHandler proxy;

	/**
	 * 传入流
	 */
	private InputStream in;

	/**
	 * 传出流
	 */
	private OutputStream out;

	/**
	 * 0 不记录日志<br>
	 * 1 字节16进制编码+字符串<br>
	 * 2 字节16进制编码<br>
	 * 3 字符串（默认编码）<br>
	 * 4 记录在tmp文件中
	 */
	private String logMode = "1";

	/**
	 * 记录数据的目录
	 */
	private String transDataFolder = "transData";

	/**
	 * 传输数据记录文件
	 */
	private FileOutputStream fos_tmpFile;

	private FileOutputStream getFos() throws IOException {
		if (fos_tmpFile != null) {
			return this.fos_tmpFile;
		} else {
			File tmp = new File(this.transDataFolder, this.getTranName()
					.replace("/", "").replace(":", "_").replace(">", "上")
					.replace("<", "下"));
			tmp.getParentFile().mkdirs();
			tmp.createNewFile();
			fos_tmpFile = new FileOutputStream(tmp);
			return fos_tmpFile;
		}

	}

	/**
	 * 构造方法
	 * 
	 * @param proxy
	 *            代理连接处理类
	 * @param in
	 *            输入流
	 * @param out
	 *            输出流
	 * 
	 * 
	 * @param threadName
	 *            本线程的名字
	 * @param prop
	 *            配置参数
	 */
	public TransportHandler(ConnectionHandler proxy, InputStream in,
			OutputStream out, String threadName, ServerProperties prop) {
		this.tranName = threadName;
		this.proxy = proxy;
		this.in = in;
		this.out = out;
		this.bufferSize = prop.getBufferSize();
		this.delayTime = prop.getDelayTime();
		this.logMode = prop.getLogMode();
	}

	@Override
	public void run() {
		byte[] b = new byte[bufferSize];

		try {
			int length = 0;
			while ((length = in.read(b)) > 0) {

				// 网络延时控制
				// 线程休眠一段时间后再发送数据 ms
				if (delayTime > 0) {
					try {
						Thread.sleep(delayTime);
					} catch (Exception ex) {

					}
				}

				// 记录日志
				recordLog(logMode, b, length);

				out.write(b, 0, length);

				out.flush();
			}

			logger.info(getTranName() + "方向的传输结束了！通知上级线程");

			proxy.transportEnd(this);

		} catch (SocketTimeoutException ex) {
			logger.warn(getTranName() + " :读取数据超时");
			//
			proxy.transportEnd(this);

		} catch (Exception e) {

			logger.error(getTranName() + " :传输过程中出错！", e);

			// 关闭双方的socket
			proxy.closeSocket();

		} finally {
			if (fos_tmpFile != null) {
				try {
					fos_tmpFile.close();
				} catch (IOException e) {
					logger.error("关闭记录文件数据文件异常！", e);
				}
			}
		}

	}

	/**
	 * @param logMode
	 *            0 不记录日志<br>
	 *            1 字节16进制编码+字符串<br>
	 *            2 字节16进制编码<br>
	 *            3 字符串（默认编码）<br>
	 *            4 记录在tmp文件中
	 * @param buffer
	 *            字节缓冲区
	 * @param length
	 *            有效数据长度
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void recordLog(String logMode, byte[] buffer, int length)
			throws FileNotFoundException, IOException {
		for (int i = 0; i < logMode.length(); i++) {
			recordLog(logMode.charAt(i), buffer, length);
		}
	}

	/**
	 * @param logMode
	 *            0 不记录日志<br>
	 *            1 字节16进制编码+字符串<br>
	 *            2 字节16进制编码<br>
	 *            3 字符串（默认编码）<br>
	 *            4 记录在tmp文件中
	 * @param buffer
	 *            字节缓冲区
	 * @param length
	 *            有效数据长度
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void recordLog(char logMode, byte[] buffer, int length)
			throws FileNotFoundException, IOException {
		if ('0' == logMode) {
			// 不记录日志

		} else if ('1' == logMode) {
			// 字节16进制编码+字符串
			byte[] logByte = new byte[length];
			System.arraycopy(buffer, 0, logByte, 0, length);
			logger.debug(getTranName() + "#\n"
					+ StringTool.toHexTable(logByte, 25));

		} else if ('2' == logMode) {
			// 字节16进制编码
			StringBuilder sb = new StringBuilder(getTranName() + "#\n");
			for (int i = 0; i < length; i++) {
				sb.append("0x");
				sb.append(Integer.toHexString(buffer[i] & 0xff).toUpperCase());
				sb.append(" ");
				// 换行
				if ((i + 1) % 20 == 0)
					sb.append("\n");
			}
			sb.append("\n");
			logger.debug(sb.toString());
		} else if ('3' == logMode) {
			// 字符串（默认编码）
			logger.debug(getTranName() + "#\n" + new String(buffer, 0, length)
					+ "\n");
		} else if ('4' == logMode) {
			// 记录在tmp文件中
			try {
				getFos().write(buffer, 0, length);
				getFos().flush();
			} catch (FileNotFoundException e) {
				logger.error("日志记录失败！找不到文件：" + this.transDataFolder + "/"
						+ getTranName(), e);
				throw e;
			} catch (IOException e) {
				logger.error("日志记录失败！写入文件异常：" + this.transDataFolder + "/"
						+ getTranName(), e);
				throw e;
			}

		}
	}

	public String getTranName() {
		return tranName;
	}

	public void setTranName(String tranName) {
		this.tranName = tranName;
	}

}
