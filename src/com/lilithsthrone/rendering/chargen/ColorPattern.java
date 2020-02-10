package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.rendering.CharacterImage;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author twovgSP
 */
public class ColorPattern {
	private String pattern_name = "";
	private String pattern_type = "";
	private List<String> bodypart_names_restriction = null;
	public double base_scale = 1.0;
	private Color primary_template_color = Color.WHITE;
	private Color secondary_template_color = Color.BLACK;

	private CharacterImage image = null;

	private final int max_cache_size = 10;
	private final Map<String, CharacterImage> cache = new HashMap<>();

	public ColorPattern(String xml_filename) throws IOException {
		initSettingsFromXML(xml_filename);
		File xml_file = new File(xml_filename);
		String file_dir = xml_file.getParent();
		String file_name = xml_file.getName();
		pattern_name = file_name.replaceAll(".meta.xml", "").trim().toLowerCase();
		String pattern_img_name = pattern_name + ".png";
		boolean image_main_required = (pattern_type.equals("2_colors_pattern"));

		image = new CharacterImage();
		if (!image.load(file_dir + File.separator + pattern_img_name)) image = null;
		if (image == null && image_main_required) throw new IOException("Can't load image "+pattern_img_name+" for pattern "+pattern_name+"!");
	}

	private void initSettingsFromXML(String pattern_xml_file) {
		Node root_node = null;
		Document xml_document = MetaXMLLoader.openXMLFile(pattern_xml_file);
		if (xml_document != null) {
			xml_document.normalizeDocument();
			NodeList root_nodes = xml_document.getElementsByTagName("pattern");
			if (root_nodes != null && root_nodes.getLength() > 0) {
				root_node = root_nodes.item(0);
			}
		}
		if (root_node == null) return;
		pattern_type = MetaXMLLoader.getStringParam(root_node, "type");
		base_scale = MetaXMLLoader.getDoubleParam(root_node, "scale", base_scale);
		primary_template_color = MetaXMLLoader.getColorParam(root_node, "color_primary", primary_template_color);
		secondary_template_color = MetaXMLLoader.getColorParam(root_node, "color_secondary", secondary_template_color);
		
		String bodyparts_only = MetaXMLLoader.getStringParam(root_node, "bodyparts_only");
		if (bodyparts_only != null) {
			bodypart_names_restriction = Arrays.asList(bodyparts_only.split(";"));
		}
	}
	
	public void clearCache() {
		cache.clear();
	}
	
	public void checkCache() {
		if (cache.size() > max_cache_size) clearCache();
	}

	public CharacterImage getTextureForBodypart(double dest_scale, String bodypart_name) {
		return getColorizedTextureForBodypart(dest_scale, null, bodypart_name);
	}
	
	public CharacterImage getColorizedTextureForBodypart(double dest_scale, BodyPartColoringInfo coloring, String bodypart_name) {
		if (bodypart_names_restriction != null && !bodypart_names_restriction.contains(bodypart_name)) {
			return null;
		}
		double result_scale = base_scale * dest_scale;

		String params_hash = Double.toString(result_scale) + "---" + (coloring != null ? coloring.toString() : "null");
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(params_hash.getBytes());
			params_hash = new String(messageDigest.digest());
		} catch (NoSuchAlgorithmException ex) {
			params_hash = Double.toString(result_scale) + "---" + Integer.toString(params_hash.hashCode());
		}

		if (!cache.containsKey(params_hash)) {
			CharacterImage new_image;
			if (coloring != null) {
				new_image = image.getColorizedImage(
					(int) Math.round(image.getWidth() * result_scale), 
					(int) Math.round(image.getHeight() * result_scale), 
					coloring, 
					primary_template_color, 
					secondary_template_color
				);
			} else {
				new_image = image.getResizedImage(
					(int) Math.round(image.getWidth() * result_scale), 
					(int) Math.round(image.getHeight() * result_scale),
					true
				);
			}
			cache.put(params_hash, new_image);
		}
		return cache.get(params_hash);
	}
	
	public static void loadPatterns(Map<String, ColorPattern> patterns_var, String from_dir) {
		patterns_var.clear();
		String[] pattern_meta_files = MetaXMLLoader.getDirMetaXML(from_dir);
		for (String file : pattern_meta_files) {
			try {
				ColorPattern new_pattern = new ColorPattern(from_dir + "/" + file);
				patterns_var.put(new_pattern.pattern_name, new_pattern);
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
	}
	
	@Override
	public String toString() {
		return "{"+
			"type: " + pattern_type + 
			" name:" + pattern_name + 
			" scale: " + base_scale + 
			" PC:" + primary_template_color + 
			" SC:" + secondary_template_color + 
		"}";
	}
}
