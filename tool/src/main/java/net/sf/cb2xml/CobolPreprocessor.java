/*************************************************************
 * This file is part of CB2XML.  
 * See the file "LICENSE" for copyright information and the
 * terms and conditions for copying, distribution and
 * modification of CB2XML.
 *************************************************************
 */

package net.sf.cb2xml;

import java.io.*;
import java.util.Properties;

/**
 * Very simple COBOL pre-processor that chops the left and right margins.
 * Column start and end positions are configurable using a properties file.
 * Linefeeds are retained as these are required by the main parser.
 * COBOL files typically contain some junk characters and
 * comment indicators in the "margins" and this routine removes those.
 *
 * @author Peter Thomas
 */
public class CobolPreprocessor {

	public static String preProcess(File file) {
		int columnStart = 6;
		int columnEnd = 72;
		try {
			FileInputStream propsStream = new FileInputStream("cb2xml.properties");
			Properties props = new Properties();
			props.load(propsStream);
			propsStream.close();
			String columnStartProperty = props.getProperty("column.start");
			String columnEndProperty = props.getProperty("column.end");
			if (columnStartProperty != null) {
				columnStart = Integer.parseInt(columnStartProperty);
			}
			if (columnEndProperty != null) {
				columnEnd = Integer.parseInt(columnEndProperty);
			}
		} catch (FileNotFoundException fe) {
			System.out.println("properties file for preprocessor not found");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("*** using start column = " + columnStart + ", end column = " + columnEnd);
		FileInputStream fis = null;
		BufferedReader buffer = null;
		StringBuffer sb = new StringBuffer();
		String s = null;
		int startPosition;
		try {
			fis = new FileInputStream(file);
			buffer = new BufferedReader(new InputStreamReader(fis));
			while ((s = buffer.readLine()) != null) {
				if (!s.trim().startsWith("*")) {
					s = s.replaceAll(":", "");
					s = s.replaceAll("^(.+?)([\\d]+)$", "$1");
				}
				if (s.length() > columnStart) {
					int thisColumnStart = columnStart;
					if (s.charAt(columnStart) == '/') {
						sb.append('*');
						thisColumnStart++;
					}
					if (s.length() < columnEnd) {
						sb.append(s.substring(thisColumnStart));
					} else {
						sb.append(s.substring(thisColumnStart, columnEnd));
					}
				}
				sb.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return sb.toString();
	}

}