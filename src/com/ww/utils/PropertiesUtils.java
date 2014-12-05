package com.ww.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述： properties文件工具<br>
 * 内部处理异常--读取和写入时如发生错误，返回null
 * 
 * <pre>
 * HISTORY
 * ****************************************************************************
 *  ID   DATE            PERSON         REASON
 *  1    2013-11-11      313921         Create
 * ****************************************************************************
 * </pre>
 * 
 * @author 313921
 * @since 1.0
 */
public class PropertiesUtils {

	private static Logger logger = LoggerFactory
			.getLogger(PropertiesUtils.class);

	public static final String fileName = "./proxy.properties";

	private static Properties properties;

	/**
	 * <pre>
	 * 取Properties对象，单例
	 * </pre>
	 * 
	 * @return
	 */
	public static synchronized Properties getProperties() {

		if (properties != null) {
			return properties;
		}

		InputStream ins = null;
		try {
			ins = PropertiesUtils.class.getClassLoader().getResourceAsStream(
					fileName);

			if (ins == null) {
				logger.error("未找到配置文件");
				return null;
			}
			properties = new Properties();
			properties.load(ins);

			return properties;
		} catch (IOException e1) {
			logger.error("加载配置文件失败", e1);
		} catch (Exception e) {
			logger.error("获取properties异常", e);
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;

	}

	/**
	 * <pre>
	 * 获取配置
	 * </pre>
	 * 
	 * @param key
	 * @return
	 */
	public static String getPropertiesValue(String key) {
		Properties p = getProperties();
		if (p != null) {
			return p.getProperty(key);
		}
		return null;
	}

	/**
	 * 获取配置
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		return getPropertiesValue(key);
	}

	/**
	 * 获取Integer值<br>
	 * 如果未配置将返回null
	 * 
	 * @param key
	 * @return
	 */
	public static Integer getIntValue(String key) {
		String var = getValue(key);
		if (var == null) {
			return null;
		} else {
			return Integer.valueOf(var);
		}
	}

	public static void main(String[] args) {
		String aaa = getPropertiesValue("aaa1");
		System.out.println("aaa:" + aaa);
		String ReceWeChatImagePath = getPropertiesValue("ReceWeChatImagePath");
		System.out.println("ReceWeChatImagePath:" + ReceWeChatImagePath);

	}
}
