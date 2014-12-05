package com.ww.utils;

import java.io.UnsupportedEncodingException;

public class StringTool {

	/**
	 * 转换为16进行字符串
	 * 
	 * @param bySrc
	 * @param offset
	 * @param len
	 * @return
	 */
	public static String convertToHex(byte bySrc[], int offset, int len) {
		byte byNew[] = new byte[len];
		String sTmp = "";
		String sResult = "";
		int i;
		for (i = 0; i < len; i++)
			byNew[i] = bySrc[offset + i];

		i = 0;
		for (int n = byNew.length; i < n; i++) {
			sTmp = Integer.toHexString(byNew[i] & 0xff);
			sTmp = fillChar(sTmp, '0', 2, true) + " ";
			sResult = sResult + sTmp;
		}

		return sResult;
	}

	/**
	 * 填充字符
	 * 
	 * @param sSource
	 *            源字符串
	 * @param ch
	 *            填充字符
	 * @param nLen
	 *            长度
	 * @param bLeft
	 *            true:从左边填充
	 * @return
	 */
	public static String fillChar(String sSource, char ch, int nLen,
			boolean bLeft) {
		int nSrcLen = sSource.length();
		if (nSrcLen <= nLen) {
			StringBuffer buffer = new StringBuffer();
			if (bLeft) {
				for (int i = 0; i < nLen - nSrcLen; i++)
					buffer.append(ch);

				buffer.append(sSource);
			} else {
				buffer.append(sSource);
				for (int i = 0; i < nLen - nSrcLen; i++)
					buffer.append(ch);

			}
			return buffer.toString();
		} else {
			return sSource;
		}
	}

	public static void main(String args[]) throws UnsupportedEncodingException {

		byte test[] = "12345678ab中文ef12345678ab中文ef12345678ab中文ef12345678ab中文ef12345678ab中文ef12345678ab中文ef12345678ab中文ef12345678ab中文ef"
				.getBytes();
		String str = null;

		str = toHexTable(test, 16);

		System.out.println(str);
	}

	public static String toHexTable(byte byteSrc[]) {
		return toHexTable(byteSrc, 16, 7);
	}

	public static String toHexTable(byte byteSrc[], int lengthOfLine) {
		return toHexTable(byteSrc, lengthOfLine, 7);
	}

	public static String toHexTable(byte byteSrc[], int lengthOfLine, int column) {
		StringBuffer hexTableBuffer = new StringBuffer(256);
		int totalLen = byteSrc.length;
		int lineCount = byteSrc.length / lengthOfLine;
		if (byteSrc.length % lengthOfLine != 0)
			lineCount++;

		for (int lineNumber = 0; lineNumber < lineCount; lineNumber++) {
			int startPos = lineNumber * lengthOfLine;
			byte lineByte[] = new byte[Math.min(lengthOfLine, totalLen
					- startPos)];
			System.arraycopy(byteSrc, startPos, lineByte, 0, lineByte.length);
			int columnA = column & 4;
			if (4 == columnA) {
				int count = 10 * lineNumber;
				String addrStr = Integer.toString(count);
				int len = addrStr.length();
				for (int i = 0; i < 8 - len; i++)
					hexTableBuffer.append('0');

				hexTableBuffer.append(addrStr);
				hexTableBuffer.append("h: ");
			}
			int columnB = column & 2;
			if (2 == columnB) {
				StringBuffer byteStrBuf = new StringBuffer();
				for (int i = 0; i < lineByte.length; i++) {
					String num = Integer.toHexString(lineByte[i] & 0xff);
					if (num.length() < 2)
						byteStrBuf.append('0');
					byteStrBuf.append(num);
					byteStrBuf.append(' ');
				}

				hexTableBuffer.append(fillChar(byteStrBuf.toString(), ' ', 48,
						false));
				hexTableBuffer.append("; ");
			}
			int columnC = column & 1;
			if (1 == columnC) {
				for (int i = 0; i < lineByte.length; i++) {
					char c = (char) lineByte[i];
					if (c < '!')
						c = '.';
					try {
						if (c >= '\240' && i < lineByte.length - 1) {// 这娃只能搞双字节的
							char c2 = (char) lineByte[i + 1];
							if (c2 >= '\240') {
								String str = new String(lineByte, i, 2);
								hexTableBuffer.append(str);
								i++;
								continue;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					hexTableBuffer.append("");
					hexTableBuffer.append(c);
				}

			}
			if (lineNumber >= lineCount - 1)
				break;
			hexTableBuffer.append('\n');
		}

		return hexTableBuffer.toString();
	}

}
