package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.BodyPartInterface;
import com.lilithsthrone.game.character.body.Covering;
import com.lilithsthrone.game.character.body.types.BodyCoveringType;
import com.lilithsthrone.game.character.body.valueEnums.CoveringPattern;
import com.lilithsthrone.utils.Colour;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;

/**
 *
 * @author twovgSP
 */
public class BodyColorsMap {
	
	private final Map<String, BodyPartColoringInfo> body_colors = new HashMap<>();

	public boolean containsKey(String key) {
		return body_colors.containsKey(key);
	}

	public BodyPartColoringInfo get(String key) {
		return body_colors.get(key);
	}
	public BodyPartColoringInfo getOrDefault(String bodypart_type, BodyPartColoringInfo default_val) {
		return body_colors.getOrDefault(bodypart_type, default_val);
	}
	public BodyPartColoringInfo getOrDefault(String bodypart_type, String bodypart_id, BodyPartColoringInfo default_val) {
		if (body_colors.containsKey(bodypart_id)) {
			return body_colors.get(bodypart_id);
		}
		return body_colors.getOrDefault(bodypart_type, default_val);
	}
	public Color getPrimaryColor(String key) {
		return body_colors.get(key).primary_color;
	}
	public Color getSecondaryColor(String key) {
		return body_colors.get(key).secondary_color;
	}

	public void mix_main_body_colors(String bodypart_target, String bodypart_mix, double koef) {
		if (body_colors.containsKey(bodypart_target) && body_colors.containsKey(bodypart_mix)) {
			Color mix = GradientParams.mixColors(getPrimaryColor(bodypart_target), getPrimaryColor(bodypart_mix), koef);
			body_colors.get(bodypart_target).primary_color = mix;
		}
	}

	public void add_body_secondary_to_main_color(String bodypart_main_code, String bodypart_secondary_code) {
		if (body_colors.containsKey(bodypart_secondary_code)) {
			BodyPartColoringInfo old_info = body_colors.get(bodypart_secondary_code);
			BodyPartColoringInfo new_info = new BodyPartColoringInfo();
			new_info.primary_color = new_info.secondary_color = old_info.secondary_color;
			new_info.primary_glowing = new_info.secondary_glowing = old_info.secondary_glowing;
			new_info.covering_pattern = old_info.covering_pattern;
			body_colors.put(bodypart_main_code, new_info);
		}
	}

	public void add_body_main_to_main_color(String bodypart_main_code_to, String bodypart_main_code_from) {
		if (body_colors.containsKey(bodypart_main_code_from)) {
			BodyPartColoringInfo old_info = body_colors.get(bodypart_main_code_from);
			BodyPartColoringInfo new_info = new BodyPartColoringInfo();
			new_info.primary_color = new_info.secondary_color = old_info.primary_color;
			new_info.primary_glowing = new_info.secondary_glowing = old_info.primary_glowing;
			new_info.covering_pattern = old_info.covering_pattern;
			body_colors.put(bodypart_main_code_to, new_info);
		}
	}

	public static Covering getBodypartCovering(BodyCoveringType bc_type, GameCharacter character) {
		if (bc_type != null) {
			return character.getCovering(bc_type);
		}
		return null;
	}
	public static Covering getBodypartCovering(BodyPartInterface bodypart, GameCharacter character) {
		if (bodypart != null) {
			return getBodypartCovering(bodypart.getBodyCoveringType(character), character);
		}
		return null;
	}
	public static CoveringPattern getBodypartCoveringPattern(BodyPartInterface bodypart, GameCharacter character) {
		Covering bc = getBodypartCovering(bodypart, character);
		if (bc != null) {
			return bc.getPattern();
		}
		return null;
	}
	public static CoveringPattern getBodypartCoveringPattern(BodyCoveringType bc_type, GameCharacter character) {
		Covering bc = getBodypartCovering(bc_type, character);
		if (bc != null) {
			return bc.getPattern();
		}
		return null;
	}
	
	public void add_body_color(String bodypart_code, BodyPartInterface bodypart, GameCharacter character) {
		if (bodypart != null) {
			add_body_color(bodypart_code, bodypart.getBodyCoveringType(character), character);
		}
	}
	public void add_body_color(String bodypart_code, BodyCoveringType bc_type, GameCharacter character) {
		if (bc_type != null) {
			add_body_color(bodypart_code, character.getCovering(bc_type), character);
		}
	}
	public void add_body_color(String bodypart_code, Covering ch_bt_covering, GameCharacter character) {
		if (ch_bt_covering != null && ch_bt_covering.getPrimaryColour() != Colour.COVERING_NONE) {
			BodyPartColoringInfo new_info = new BodyPartColoringInfo();
			new_info.primary_color = convertColourToColor(ch_bt_covering.getPrimaryColour());
			new_info.primary_glowing = ch_bt_covering.isPrimaryGlowing();
			new_info.covering_pattern = ch_bt_covering.getPattern().toString().toLowerCase();
			new_info.secondary_color = convertColourToColor(ch_bt_covering.getSecondaryColour());
			new_info.secondary_glowing = ch_bt_covering.isPrimaryGlowing();
			body_colors.put(bodypart_code, new_info);
		}
	}
	public void add_body_color(String bodypart_code, Color color) {
		BodyPartColoringInfo new_info = new BodyPartColoringInfo();
		new_info.primary_color = color;
		new_info.secondary_color = color;
		body_colors.put(bodypart_code, new_info);
	}
	
	public Color convertColourToColor(Colour col) {
		//System.out.println(col.getName() + " " + col.toString());
		switch(col.toString()) {
			case "EYE_PITCH_BLACK":
				return Color.web("#111");
			case "COVERING_BLACK":
				return Color.web("#555");
			case "COVERING_JET_BLACK":
				return Color.web("#333");
			case "SKIN_JET_BLACK":
				return Color.web("#444");
			case "COVERING_WHITE":
			case "EYE_WHITE":
				return Color.web("#fff");
		}
		return col.getColor();
	}
	
	@Override
	public String toString() {
		return body_colors.toString();
	}
}
