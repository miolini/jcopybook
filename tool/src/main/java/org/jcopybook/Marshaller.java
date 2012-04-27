package org.jcopybook;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class Marshaller {
	private Document copybook;
	private HashMap<String, Map<String, String>> meta;

	public Marshaller() {
	}

	public Marshaller(Document layout) {
		setLayout(layout);
	}

	public void setLayout(Document copybook) {
		this.copybook = copybook;
		meta = new HashMap<String, Map<String, String>>();
		analyze(copybook, meta);
	}

	public String process(Document data) {
		StringBuilder out = new StringBuilder();
		iterate(data.getElementsByTagName("copybook").item(0).getFirstChild(), meta, out);
		return out.toString();
	}

	private void analyze(Node copybookNode, Map<String, Map<String, String>> meta) {
		if (copybookNode.getAttributes() != null && copybookNode.getAttributes().getNamedItem("display-length") != null) {
			Map<String, String> attrs = new HashMap<String, String>();
			NamedNodeMap attrNodes = copybookNode.getAttributes();
			for (int i = 0; i < attrNodes.getLength(); i++) {
				Node attr = attrNodes.item(i);
				attrs.put(attr.getNodeName(), attr.getNodeValue());
			}
			String path = getPathLayout(copybookNode);
			meta.put(path, attrs);
		}
		NodeList childs = copybookNode.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			analyze(childs.item(i), meta);
		}
	}

	private void iterate(Node node, Map<String, Map<String, String>> meta, StringBuilder out) {
		if (node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.TEXT_NODE) {
			String value = node.getFirstChild().getNodeValue();
			value = value.replaceAll("[\\r\\n]+", "");
			String path = getPath(node);
			Map<String, String> nodeMeta = meta.get(path);
			if (nodeMeta != null) {
				int len = Integer.parseInt(nodeMeta.get("display-length"));
				if ("true".equals(nodeMeta.get("numeric"))) {
					if (nodeMeta.containsKey("scale")) {

					} else {
						String format = String.format("%%0%dd", len);
						value = String.format(format, Integer.parseInt(value));
					}
				}
			}
			out.append(value);
			return;
		}
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			iterate(childs.item(i), meta, out);
		}
	}

	private String getPathLayout(Node node) {
		if (node.getNodeName().equals("copybook")) return "";
		StringBuilder path = new StringBuilder();
		if (node.getParentNode().getNodeName().equals("item"))
			path.append(node.getParentNode().getAttributes().getNamedItem("name").getNodeValue());
		path.append("/");
		path.append(node.getAttributes().getNamedItem("name").getNodeValue());
		return path.toString();
	}

	private String getPath(Node node) {
		if (node.getNodeName().equals("copybook")) return "";
		StringBuilder path = new StringBuilder();
		path.append(node.getParentNode().getNodeName());
		path.append("/");
		path.append(node.getNodeName());
		return path.toString();
	}
}
