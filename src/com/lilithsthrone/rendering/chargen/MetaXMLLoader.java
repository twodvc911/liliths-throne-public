package com.lilithsthrone.rendering.chargen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author twovgSP
 */
public class MetaXMLLoader {

	private static final List<String> DEFAULT_ATTRIBUTES_TO_GET = Arrays.asList(new String[]{"id"});

	public static String[] getDirMetaXML(String dir_path) {
		File dir_file = new File(dir_path);
		return dir_file.list((File current, String name) -> (new File(current, name).isFile()) && (name.endsWith(".meta.xml")));
	}
	
	public static String[] getSubDirs(String dir_path) {
		File dir_file = new File(dir_path);
		return dir_file.list((File current, String name) -> (new File(current, name).isDirectory()));
	}
	
	public static Document openXMLFile(String filename) {
		try {
			File xml_file = new File(filename);
			if (!xml_file.isFile()) return null;
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xml_document = builder.parse(xml_file);
			return xml_document;
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			return null;
		}
	}
	public static String getStringParam(Node parent_node, String param_name) {
		return getStringParam(parent_node, param_name, null);
	}
	public static String getStringParam(Node parent_node, String param_name, String default_value) {
		Node c_node = MetaXMLLoader.getChildFirstNodeOfType(parent_node, param_name);
		return c_node != null ? c_node.getTextContent() : default_value;
	}
	public static boolean getBoolParam(Node parent_node, String param_name) {
		Node c_node = MetaXMLLoader.getChildFirstNodeOfType(parent_node, param_name);
		return c_node != null && "1".equals(c_node.getTextContent());
	}
	public static double getDoubleParam(Node parent_node, String param_name, double default_value) {
		Node c_node = MetaXMLLoader.getChildFirstNodeOfType(parent_node, param_name);
		return c_node != null ? Double.parseDouble(c_node.getTextContent()) : default_value;
	}
	public static int getIntParam(Node parent_node, String param_name, int default_value) {
		Node c_node = MetaXMLLoader.getChildFirstNodeOfType(parent_node, param_name);
		return c_node != null ? Integer.valueOf(c_node.getTextContent()) : default_value;
	}
	public static Integer getIntegerParam(Node parent_node, String param_name, Integer default_value) {
		Node c_node = MetaXMLLoader.getChildFirstNodeOfType(parent_node, param_name);
		return c_node != null ? Integer.valueOf(c_node.getTextContent()) : default_value;
	}
	public static Color getColorParam(Node parent_node, String param_name, Color default_value) {
		Node c_node = MetaXMLLoader.getChildFirstNodeOfType(parent_node, param_name);
		return c_node != null ? Color.valueOf(c_node.getTextContent()) : default_value;
	}
	public static Node getChildFirstNodeOfType(Node parent_node, String type_name) {
		if (parent_node != null) {
			NodeList childs = parent_node.getChildNodes();
			for (int i=0; i<childs.getLength(); i++) {
				if (childs.item(i).getNodeName().equals(type_name)) {
					return childs.item(i);
				}
			}
		}
		return null;
	}
	public static String getNodeAttribute(Node node, String attr_name) {
		if (node != null && node.hasAttributes()) {
			NamedNodeMap attr_map = node.getAttributes();
			Node attr = attr_map.getNamedItem(attr_name);
			if (attr != null) {
				return attr.getTextContent().trim().toLowerCase();
			}
		}
		return null;
	}
	public static List<Node> getAllChildNodesOfType(Node parent_node, String type_name) {
		List<Node> node_list = new ArrayList<>();
		if (parent_node != null) {
			NodeList childs = parent_node.getChildNodes();
			for (int i=0; i<childs.getLength(); i++) {
				if (childs.item(i).getNodeName().equals(type_name)) {
					node_list.add(childs.item(i));
				}
			}
		}
		return node_list;
	}
	public static Map<String, String> getAllChildNodesAsMap(Node parent_node) {
		return MetaXMLLoader.getAllChildNodesAsMap(parent_node, DEFAULT_ATTRIBUTES_TO_GET);
	}
	public static Map<String, String> getAllChildNodesAsMap(Node parent_node, List<String> attributes_to_map) {
		Map<String, String> result = new HashMap<>();
		if (parent_node != null) {
			NodeList childs = parent_node.getChildNodes();
			for (int i=0; i<childs.getLength(); i++) {
				if (!childs.item(i).getNodeName().equals("#text")) {
					String base_node_name = childs.item(i).getNodeName();
					Map<String, String> this_node_map = getAllChildNodesAsMap(childs.item(i), attributes_to_map);
					if (!this_node_map.isEmpty()) {
						for (Map.Entry<String, String> sub_entry : this_node_map.entrySet()) {
							result.put(base_node_name + "_" + sub_entry.getKey(), sub_entry.getValue());
						}
					} else {
						result.put(base_node_name, childs.item(i).getTextContent().trim());
					}
				}
			}
			if (parent_node.hasAttributes()) {
				NamedNodeMap attr_map = parent_node.getAttributes();
				for(int j=0; j<attributes_to_map.size(); j++) {
					if (!result.containsKey(attributes_to_map.get(j))) {
						Node id_attr = attr_map.getNamedItem(attributes_to_map.get(j));
						if (id_attr != null) {
							result.put(attributes_to_map.get(j), id_attr.getTextContent().trim().toLowerCase());
						}
					}
				}
			}
		}
		return result;
	}
	public static List<Map<String, String>> getAllChildNodesMapList(Node parent_node, String node_type) {
		List<Map<String, String>> result = new ArrayList<>();
		List<Node> child_nodes = MetaXMLLoader.getAllChildNodesOfType(parent_node, node_type);
		for(Node child_node: child_nodes)  {
			Map<String, String> child_map = MetaXMLLoader.getAllChildNodesAsMap(child_node);
			result.add(child_map);
		}
		return result;
	}
	public static List<Map<String, String>> getAllChildNodesMapList(Node parent_node, String node_type, List<String> attributes_to_map) {
		List<Map<String, String>> result = new ArrayList<>();
		List<Node> child_nodes = MetaXMLLoader.getAllChildNodesOfType(parent_node, node_type);
		for(Node child_node: child_nodes)  {
			Map<String, String> child_map = MetaXMLLoader.getAllChildNodesAsMap(child_node, attributes_to_map);
			result.add(child_map);
		}
		return result;
	}
	public static Map<String, Map<String, String>> getAllChildNodesMapIDMap(Node parent_node, String node_type) {
		Map<String, Map<String, String>> result = new HashMap<>();
		List<Node> child_nodes = MetaXMLLoader.getAllChildNodesOfType(parent_node, node_type);
		for(Node child_node: child_nodes)  {
			Map<String, String> child_map = MetaXMLLoader.getAllChildNodesAsMap(child_node);
			if (child_map.containsKey("id")) {
				result.put(child_map.get("id"), child_map);
			}
		}
		return result;
	}
}
