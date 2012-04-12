package org.jcopybook;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Utils {

	public static Element getFirstElement(Element documentElement) {
		Object node = null;
		NodeList childs = documentElement.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			node = childs.item(i);
			if (node instanceof Element) return (Element) node;
		}
		throw new RuntimeException("Can't find any Element");
	}
}
