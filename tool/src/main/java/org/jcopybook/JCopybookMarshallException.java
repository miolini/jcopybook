package org.jcopybook;

import org.w3c.dom.Document;

/**
 * User: miolini
 * Date: 22.05.12 4:15
 */
public class JCopybookMarshallException extends JCopybookException {
	public JCopybookMarshallException(String message, Throwable throwable, String contextIn, String contextOut, String elementName, String elementValue, Document layout) {
		super(message, throwable, contextIn, contextOut, elementName, elementValue, layout);
	}
}
