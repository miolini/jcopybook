package org.jcopybook;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Marshaller {
	public String process(Document data, Document copybook) {
		StringBuilder out = new StringBuilder();
		iterate(data.getElementsByTagName("copybook").item(0).getFirstChild(), out);
		return out.toString();
	}

	private void iterate(Node node, StringBuilder out) {
		if (node.getFirstChild() != null)
			System.out.println(node.getNodeName() + " '" + node.getFirstChild().getNodeValue()+"'");
		if (node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.TEXT_NODE) {
			String value = node.getFirstChild().getNodeValue();
			value = value.replaceAll("[\\r\\n]+", "");
			out.append(value);
			return;
		} else {
			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				iterate(childs.item(i), out);
			}
		}
	}
}
