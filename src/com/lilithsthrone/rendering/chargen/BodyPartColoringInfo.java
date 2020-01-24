package com.lilithsthrone.rendering.chargen;

import javafx.scene.paint.Color;

/**
 *
 * @author twovgSP
 */
public class BodyPartColoringInfo {
	public Color primary_color = Color.WHITE;
	public boolean primary_glowing = false;
	public Color secondary_color= Color.WHITE;
	public boolean secondary_glowing = false;
	public String covering_pattern = "plain";

	public BodyPartColoringInfo getMixWithColor(Color mix_color, String mix_type, double mix_param) {
		BodyPartColoringInfo new_info = new BodyPartColoringInfo();
		if ("multiply".equals(mix_type)) {
			new_info.primary_color = new Color(
				primary_color.getRed() * mix_color.getRed(), 
				primary_color.getGreen() * mix_color.getGreen(), 
				primary_color.getBlue() * mix_color.getBlue(),
				1
			);
			new_info.secondary_color = new Color(
				secondary_color.getRed() * mix_color.getRed(), 
				secondary_color.getGreen() * mix_color.getGreen(), 
				secondary_color.getBlue() * mix_color.getBlue(),
				1
			);
		} else if ("transition".equals(mix_type)) {
			double k = 1 - 0.01 * mix_param;
			if (k > 1) k = 1;
			else if (k < 0) k = 0;
			new_info.primary_color = new Color(
				primary_color.getRed() * k + mix_color.getRed() * (1-k), 
				primary_color.getGreen() * k + mix_color.getGreen() * (1-k), 
				primary_color.getBlue() * k + mix_color.getBlue() * (1-k),
				1
			);
			new_info.secondary_color = new Color(
				secondary_color.getRed() * k + mix_color.getRed() * (1-k), 
				secondary_color.getGreen() * k + mix_color.getGreen() * (1-k), 
				secondary_color.getBlue() * k + mix_color.getBlue() * (1-k),
				1
			);
		}
		new_info.primary_glowing = primary_glowing;
		new_info.secondary_glowing = secondary_glowing;
		new_info.covering_pattern = covering_pattern;
		return new_info;
	}
	
	@Override
	public String toString() {
		return covering_pattern + 
			" col1: " + primary_color + (primary_glowing ? " glow" : "") +
			" col2: " + secondary_color + (secondary_glowing ? " glow" : "");
	}
}
