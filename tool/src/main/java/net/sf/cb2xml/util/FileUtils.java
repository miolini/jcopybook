/*************************************************************
 * This file is part of CB2XML.  
 * See the file "LICENSE" for copyright information and the
 * terms and conditions for copying, distribution and
 * modification of CB2XML.
 *************************************************************
 */

package net.sf.cb2xml.util;

import java.io.*;

/**
 * quick and easy file utilities useful for debugging / logging
 *
 * @author Peter Thomas
 */
public class FileUtils {

	public static InputStream openFile(String fileName) throws FileNotFoundException {
		InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
		if (stream == null) stream = new FileInputStream(fileName);
		if (stream == null) throw new FileNotFoundException("resources not found: " + fileName);
		return stream;
	}

	public static StringBuffer readFile(String fileName) throws FileNotFoundException {
		InputStream fis = openFile(fileName);
		BufferedReader buffer = null;
		StringBuffer sb = new StringBuffer();
		String s = null;
		try {
			buffer = new BufferedReader(new InputStreamReader(fis));
			while ((s = buffer.readLine()) != null) {
				sb.append(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					;
				}
			}
		}
		return sb;
	}

	public static void writeFile(String content, String fileName, boolean append) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(fileName, append);
			writer.write(content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
				}
				;
			}
		}
	}

}