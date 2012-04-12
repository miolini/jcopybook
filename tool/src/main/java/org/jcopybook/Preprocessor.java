package org.jcopybook;

import net.sf.cb2xml.CobolPreprocessor;
import net.sf.cb2xml.CopyBookAnalyzer;
import net.sf.cb2xml.DebugLexer;
import net.sf.cb2xml.sablecc.lexer.Lexer;
import net.sf.cb2xml.sablecc.node.Start;
import net.sf.cb2xml.sablecc.parser.Parser;
import net.sf.cb2xml.sablecc.parser.ParserException;
import net.sf.cb2xml.util.XmlUtils;
import org.w3c.dom.Document;

import java.io.*;
import java.util.Scanner;

public class Preprocessor {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Scanner scanner = new Scanner(new FileInputStream(args[0]));
		PrintWriter writer = new PrintWriter(args[0]+".converted");
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.trim().startsWith("*")) continue;
			line = line.replaceAll(":", "");
			line = line.replaceAll("^(.+?)([\\d]+)$", "$1");
			writer.println(line);
		}
		writer.close();
	}
	
	// public API methods	
	public static Document convertToXMLDOM(File file) {
		return convert(file, false);
	}
	
	public static String convertToXMLString(File file) {
		Document document = convert(file, false);
		return XmlUtils.domToString(document).toString();
	}

	// overloaded methods for debug mode
	public static Document convertToXMLDOM(File file, boolean debug) {
		return convert(file, debug);
	}
	
	public static String convertToXMLString(File file, boolean debug) {
		Document document = convert(file, debug);
		return XmlUtils.domToString(document).toString();
	}	
	
	private static Document convert(File file, boolean debug) {
		Document document = null;
		Lexer lexer = null;
		String preProcessed = null;
		try {
			preProcessed = CobolPreprocessor.preProcess(file);
			StringReader sr = new StringReader(preProcessed);
			PushbackReader pbr = new PushbackReader(sr, 1000);
			if (debug) {
				System.err.println("*** debug mode ***");
				lexer = new DebugLexer(pbr);
			} else {
				lexer = new Lexer(pbr);
			}
			Parser parser = new Parser(lexer);
			Start ast = parser.parse();
			CopyBookAnalyzer copyBookAnalyzer = new CopyBookAnalyzer(file.getName(), parser);
			ast.apply(copyBookAnalyzer);
			document = copyBookAnalyzer.getDocument();			
		} catch(ParserException pe) {
			System.err.println("*** fatal parse error ***");
			System.err.println(pe.getMessage());
			if (debug) {
				System.err.println("=== buffer dump start ===");
				System.err.println(((DebugLexer) lexer).getBuffer());
				System.err.println("=== buffer dump end ===");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return document;
	}

}
