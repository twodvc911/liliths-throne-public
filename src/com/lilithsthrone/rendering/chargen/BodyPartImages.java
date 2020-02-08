package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.rendering.CharacterImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author twovgSP
 */
public class BodyPartImages {
	private String base_img_name = null;
	private final Map<String, String> items = new HashMap<>();

	public BodyPartImages(String base_img) {
		base_img_name = base_img;
		addImage("main", null);
		addImage("mask", "mask");
		addImage("color", "color");
	}

	public boolean hasImage(String code) {
		return items.containsKey(code);
	}

	public String getImagePath(String code) {
		return items.getOrDefault(code, null);
	}

	public CharacterImage getImage(String code) {
		CharacterImage image = new CharacterImage();
		String image_path = getImagePath(code);
		if (!image.load(image_path)) {
			return null;
		}
		return image;
	}

	public CharacterImage getImage(String code, int width, int height) {
		CharacterImage image = null;
		String image_path = getImagePath(code);
		try {
			image = CharacterImage.fromFile(image_path, width, height);
		} catch(IOException ex) {
			System.out.println("Can't open file "+image_path+"!");
		}
		return image;
	}

	private boolean addImage(String img_code, String img_file_postfix) {
		String img_file_path = base_img_name + (img_file_postfix != null ? "_" + img_file_postfix : "") + ".png";
		File img_file_f = new File(img_file_path);
		if (img_file_f.exists() && img_file_f.isFile()) {
			items.put(img_code, img_file_path);
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return items.toString();
	}
}
