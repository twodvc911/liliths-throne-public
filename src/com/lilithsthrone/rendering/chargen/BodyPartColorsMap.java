package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.BodyPartInterface;
import com.lilithsthrone.game.character.body.Covering;
import com.lilithsthrone.game.character.body.types.BodyCoveringType;
import com.lilithsthrone.game.character.body.valueEnums.CoveringPattern;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.utils.colours.Colour;
import com.lilithsthrone.utils.colours.PresetColour;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;

/**
 *
 * @author twovgSP
 */
public class BodyPartColorsMap {

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
			new_info.color_pattern = old_info.color_pattern;
			new_info.material = old_info.material;
			new_info.covering_type = old_info.covering_type;
			body_colors.put(bodypart_main_code, new_info);
		}
	}

	public void add_body_main_to_main_color(String bodypart_main_code_to, String bodypart_main_code_from) {
		if (body_colors.containsKey(bodypart_main_code_from)) {
			BodyPartColoringInfo old_info = body_colors.get(bodypart_main_code_from);
			BodyPartColoringInfo new_info = new BodyPartColoringInfo();
			new_info.primary_color = new_info.secondary_color = old_info.primary_color;
			new_info.primary_glowing = new_info.secondary_glowing = old_info.primary_glowing;
			new_info.color_pattern = old_info.color_pattern;
			new_info.material = old_info.material;
			new_info.covering_type = old_info.covering_type;
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
	public void add_body_color(String bodypart_code, AbstractClothing clothing) {
		if (clothing != null && clothing.getPatternColour() != null) {
			add_body_color(bodypart_code, clothing.getColour().getColor(), "cloth");
			add_body_color(bodypart_code + ".primary", clothing.getColour().getColor(), "cloth");
			add_body_color(bodypart_code + ".secondary", clothing.getSecondaryColour().getColor(), "cloth");
			add_body_color(bodypart_code + ".tertiary", clothing.getTertiaryColour().getColor(), "cloth");
		}
	}
	public void add_body_color(String bodypart_code, BodyCoveringType bc_type, GameCharacter character) {
		if (bc_type != null) {
			add_body_color(bodypart_code, character.getCovering(bc_type), character);
		}
	}
	public void add_body_color(String bodypart_code, Covering ch_bt_covering, GameCharacter character) {
		if (ch_bt_covering != null && ch_bt_covering.getPrimaryColour() != PresetColour.COVERING_NONE) {
			BodyPartColoringInfo new_info = new BodyPartColoringInfo();
			new_info.primary_color = convertColourToColor(ch_bt_covering.getPrimaryColour());
			new_info.primary_glowing = ch_bt_covering.isPrimaryGlowing();
			new_info.color_pattern = ch_bt_covering.getPattern().toString().toLowerCase();
			new_info.secondary_color = convertColourToColor(ch_bt_covering.getSecondaryColour());
			new_info.secondary_glowing = ch_bt_covering.isPrimaryGlowing();
			new_info.material = character.getBodyMaterial().toString().toLowerCase();
			new_info.covering_type = ch_bt_covering.getType().getNameSingular(character).toLowerCase();
			body_colors.put(bodypart_code, new_info);
		}
	}
	public void add_body_color(String bodypart_code, Color color, String material) {
		BodyPartColoringInfo new_info = new BodyPartColoringInfo();
		new_info.primary_color = color;
		new_info.secondary_color = color;
		new_info.material = material;
		body_colors.put(bodypart_code, new_info);
	}

	public Color convertColourToColor(Colour col) {
		//System.out.println(col.getName() + " " + col.getId());
		switch(col.getId()) {
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

	public static BodyPartColorsMap fromCharacter(GameCharacter character) {
		BodyPartColorsMap body_colors = new BodyPartColorsMap();

		body_colors.add_body_color("body", character.getBody().getSkin(), character);
		body_colors.add_body_color("body_taur", character.getBody().getLeg(), character);
		body_colors.add_body_color("belly", character.getBody().getSkin(), character);
		body_colors.add_body_color("leg", character.getBody().getLeg(), character);
		body_colors.add_body_color("foot", character.getBody().getLeg(), character);
		body_colors.add_body_color("arm", character.getBody().getArm(), character);
		body_colors.add_body_color("hand", character.getBody().getArm(), character);
		body_colors.add_body_color("head", character.getBody().getFace(), character);
		body_colors.add_body_color("blusher", character.getBlusher(), character);
		body_colors.add_body_color("eye_shadow", character.getEyeShadow(), character);
		body_colors.add_body_color("eye_liner", character.getEyeLiner(), character);
		body_colors.add_body_color("muzzle", character.getBody().getFace(), character);
		body_colors.add_body_color("lips", character.getBody().getFace().getMouth(), character);
		body_colors.add_body_color("lips", character.getLipstick(), character);
		body_colors.add_body_color("hand_nails", character.getHandNailPolish(), character);
		body_colors.add_body_color("foot_nails", character.getFootNailPolish(), character);
		body_colors.add_body_color("hair", character.getBody().getHair(), character);
		body_colors.add_body_color("penis", character.getBody().getPenis(), character);
		body_colors.add_body_color("balls", character.getBody().getPenis() != null ? character.getBody().getPenis().getTesticle() : null, character);
		body_colors.add_body_color("balls", character.getBody().getLeg(), character);
		body_colors.add_body_color("vagina", character.getBody().getVagina(), character);
		body_colors.add_body_color("cloaca", character.getBody().getPenis(), character);
		body_colors.add_body_color("cloaca", character.getBody().getVagina(), character);
		body_colors.add_body_color("breasts", character.getBody().getBreast(), character);
		body_colors.add_body_color("breasts-crotch", character.getBody().getBreastCrotch(), character);
		body_colors.add_body_color("breasts-crotch-nipple", character.getBody().getBreastCrotch() != null ? character.getBody().getBreastCrotch().getNipples() : null, character);
		body_colors.add_body_color("nipple", character.getBody().getBreast() != null ? character.getBody().getBreast().getNipples() : null, character);
		body_colors.add_body_color("horn", character.getBody().getHorn(), character);
		body_colors.add_body_color("antenna", character.getBody().getAntenna(), character);
		body_colors.add_body_color("ear", character.getBody().getEar(), character);
		body_colors.add_body_color("eye", Color.WHITE, character.getBodyMaterial().toString().toLowerCase());
		body_colors.add_body_color("eye_sclera", BodyCoveringType.EYE_SCLERA, character);
		body_colors.add_body_color("eye_pupil", BodyCoveringType.EYE_PUPILS, character);
		body_colors.add_body_color("eye_iris", character.getBody().getEye(), character);
		body_colors.add_body_color("wing", character.getBody().getWing(), character);
		body_colors.add_body_color("ass", character.getBody().getAss(), character);
		body_colors.add_body_color("tongue", character.getBody().getFace()!= null ? character.getBody().getFace().getTongue() : null, character);
		body_colors.add_body_color("tail", character.getBody().getTail(), character);

		body_colors.add_body_color("neck", character.getBody().getFace(), character);
		body_colors.add_body_color("eyes", Color.WHITE, character.getBodyMaterial().toString().toLowerCase());
		body_colors.add_body_color("finger", character.getBody().getArm(), character);

		body_colors.mix_main_body_colors("nipple", "breasts", 0.5);

		if (BodyPartColorsMap.getBodypartCoveringPattern(character.getBody().getEye(), character) == CoveringPattern.EYE_IRISES_HETEROCHROMATIC) body_colors.add_body_secondary_to_main_color("eye_iris_inversed", "eye_iris");
		if (BodyPartColorsMap.getBodypartCoveringPattern(BodyCoveringType.EYE_SCLERA, character) == CoveringPattern.EYE_SCLERA_HETEROCHROMATIC) body_colors.add_body_secondary_to_main_color("eye_sclera_inversed", "eye_sclera");
		if (BodyPartColorsMap.getBodypartCoveringPattern(BodyCoveringType.EYE_PUPILS, character) == CoveringPattern.EYE_PUPILS_HETEROCHROMATIC) body_colors.add_body_secondary_to_main_color("eye_pupil_inversed", "eye_pupil");

		List<AbstractClothing> equiped_items = character.getClothingCurrentlyEquipped();
		equiped_items.forEach((equiped_item) -> {
			body_colors.add_body_color("clothes_" + equiped_item.getSlotEquippedTo().name().toLowerCase(), equiped_item);
		});

		return body_colors;
	}

	@Override
	public String toString() {
		return body_colors.toString();
	}
}
