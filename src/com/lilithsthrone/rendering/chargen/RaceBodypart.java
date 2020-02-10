package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.valueEnums.CoveringPattern;
import com.lilithsthrone.game.character.body.valueEnums.PenetrationModifier;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.Colour;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javafx.scene.paint.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author twovgSP
 */
public class RaceBodypart {
	public final String race_name;		// human, demon, dog_morph...
	public final String bodypart_name;	// body, leg, face, vagina...
	public final String bodypart_code;	// file name of bodypart file (fox_ears, fat_1, core...)
	public BodyPartImages images = null;	// all images for this body part (main image, masks, etc...)

	public String derive_color_from_bodypart = null;
	public String mask_calculation_mode = "default";

	public boolean is_fallback = false;
	public boolean is_hidden = false;
	public boolean no_patterns = false;
	public boolean no_coverings = false;
	public boolean no_scale_by_parent = false;
	public boolean no_transitions = false;
	public boolean skip_colorization = false;
	public double pick_priority = 100;
	public double priority = 100;
	public double priority_offset = 0;
	public double pattern_scale = 1.0;
	public double scale = 1.0;
	public double scale_x = 1.0;
	public double scale_y = 1.0;
	public double min_scale = 0.00001;
	public double max_scale = 100;

	public Color color_to_mix = null;
	public String mix_type = null;
	public double mix_param = 50;

	public Integer variant_index = null;
	public Integer max_count = null;

	public List<Map<String, String>> parent_connections = new ArrayList<>();
	public Map<String, Map<String, String>> positions = new HashMap<>();
	public List<Map<String, String>> conditions = new ArrayList<>();
	public Map<String, Double> modifiers = new HashMap<>();
	public List<Map<String, String>> mod_conditions = new ArrayList<>();
	public Map<String, String> scale_params = new HashMap<>();
	public Set<String> hides_bodyparts_by_type = new HashSet<>();
	public Set<String> hides_bodyparts_by_id = new HashSet<>();

	public String derive_parent_connections = null;
	public String derive_positions = null;
	public String derive_conditions = null;
	public String derive_modifiers = null;
	public String derive_mod_conditions = null;
	public String derive_scale_params = null;
	public String derive_image = null;

	private static final List<String> ALL_MODIFIER_PARAMS = Arrays.asList(new String[]{
		"scale", "scale_x", "scale_y",
		"offset_x", "offset_y", 
		"add_priority", 
		"rotation",
		"inverse_x", "inverse_y", 
		"multiple_offset_scale", "multiple_offset_scale_x", "multiple_offset_scale_y",
		"multiple_offset_x", "multiple_offset_y"
	});
	private static final List<String> MODIFIER_PARAMS_ADD = Arrays.asList(new String[]{
		"offset_x", "offset_y", 
		"multiple_offset_x", "multiple_offset_y",
		"add_priority", "rotation",
	});

	public RaceBodypart(String bp_name, String bp_code, String bp_race_name, String bp_xml_file, String bp_img_file) throws IOException {
		this.bodypart_name = bp_name;
		this.bodypart_code = bp_code;
		this.race_name = bp_race_name;
		
		initSettingsFromXML(bp_xml_file);
		String img_file_base;
		if (derive_image != null) {
			File bp_xml_f = new File(bp_xml_file);
			img_file_base = bp_xml_f.getParent() + File.separator + derive_image;
		} else {
			img_file_base = bp_img_file.replace(".png", "");
		}
		images = new BodyPartImages(img_file_base);
		if (!is_hidden && !images.hasImage("main")) {
			throw new IOException("Can't find main image file for bodypart="+bp_name+"!");
		}
	}

	private void initSettingsFromXML(String bp_xml_file) {
		Node bodypart_node = null;
		Document xml_document = MetaXMLLoader.openXMLFile(bp_xml_file);
		if (xml_document != null) {
			xml_document.normalizeDocument();
			NodeList bodypart_nodes = xml_document.getElementsByTagName("bodypart");
			if (bodypart_nodes != null && bodypart_nodes.getLength() > 0) {
				bodypart_node = bodypart_nodes.item(0);
			}
		}
		if (bodypart_node == null) return;
		
		derive_image = MetaXMLLoader.getNodeAttribute(bodypart_node, "derive_image_from");
		
		is_hidden = MetaXMLLoader.getBoolParam(bodypart_node, "hidden");
		is_fallback = MetaXMLLoader.getBoolParam(bodypart_node, "fallback");
		no_patterns = MetaXMLLoader.getBoolParam(bodypart_node, "no_patterns");
		no_coverings = MetaXMLLoader.getBoolParam(bodypart_node, "no_coverings");
		no_transitions = MetaXMLLoader.getBoolParam(bodypart_node, "no_transitions");
		no_scale_by_parent = MetaXMLLoader.getBoolParam(bodypart_node, "no_scale_by_parent");
		skip_colorization = MetaXMLLoader.getBoolParam(bodypart_node, "skip_colorization");

		derive_color_from_bodypart = MetaXMLLoader.getStringParam(bodypart_node, "derive_color_from_bodypart");
		mask_calculation_mode = MetaXMLLoader.getStringParam(bodypart_node, "mask_calculation_mode");
		
		scale = MetaXMLLoader.getDoubleParam(bodypart_node, "scale", scale);
		scale_x = MetaXMLLoader.getDoubleParam(bodypart_node, "scale_x", scale_x);
		scale_y = MetaXMLLoader.getDoubleParam(bodypart_node, "scale_y", scale_y);
		min_scale = MetaXMLLoader.getDoubleParam(bodypart_node, "min_scale", min_scale);
		max_scale = MetaXMLLoader.getDoubleParam(bodypart_node, "max_scale", max_scale);
		pattern_scale = MetaXMLLoader.getDoubleParam(bodypart_node, "pattern_scale", pattern_scale);
		priority = MetaXMLLoader.getDoubleParam(bodypart_node, "priority", priority);
		pick_priority = MetaXMLLoader.getDoubleParam(bodypart_node, "pick_priority", pick_priority);
		priority_offset = MetaXMLLoader.getDoubleParam(bodypart_node, "priority_offset", priority_offset);

		variant_index = MetaXMLLoader.getIntegerParam(bodypart_node, "variant_index", null);
		max_count = MetaXMLLoader.getIntegerParam(bodypart_node, "max_count", null);

		Node color_mix_node = MetaXMLLoader.getChildFirstNodeOfType(bodypart_node, "color_mix");
		color_to_mix = MetaXMLLoader.getColorParam(color_mix_node, "color", null);
		mix_type = MetaXMLLoader.getStringParam(color_mix_node, "type", null);
		mix_param = MetaXMLLoader.getDoubleParam(color_mix_node, "value", 50);

		Node conditions_node = MetaXMLLoader.getChildFirstNodeOfType(bodypart_node, "conditions");
		derive_conditions = MetaXMLLoader.getNodeAttribute(conditions_node, "derive_from");
		conditions = MetaXMLLoader.getAllChildNodesMapList(conditions_node, "condition");

		Node modifiers_node = MetaXMLLoader.getChildFirstNodeOfType(bodypart_node, "modifiers");
		derive_modifiers = MetaXMLLoader.getNodeAttribute(modifiers_node, "derive_from");
		Map<String, String> str_modifiers = MetaXMLLoader.getAllChildNodesAsMap(modifiers_node);
		for(Map.Entry<String, String> entry: str_modifiers.entrySet()) {
			Double mod_val = getModifierFromParamString(entry.getKey(), entry.getValue());
			if (mod_val != null) modifiers.put(entry.getKey(), mod_val);
		}

		Node mod_conditions_node = MetaXMLLoader.getChildFirstNodeOfType(bodypart_node, "mod_conditions");
		derive_mod_conditions = MetaXMLLoader.getNodeAttribute(mod_conditions_node, "derive_from");
		mod_conditions = MetaXMLLoader.getAllChildNodesMapList(mod_conditions_node, "condition", ALL_MODIFIER_PARAMS);
		
		Node parent_connections_node = MetaXMLLoader.getChildFirstNodeOfType(bodypart_node, "connections_parent");
		derive_parent_connections = MetaXMLLoader.getNodeAttribute(parent_connections_node, "derive_from");
		parent_connections = MetaXMLLoader.getAllChildNodesMapList(parent_connections_node, "connection");

		Node positions_node = MetaXMLLoader.getChildFirstNodeOfType(bodypart_node, "positions");
		derive_positions = MetaXMLLoader.getNodeAttribute(positions_node, "derive_from");
		positions = MetaXMLLoader.getAllChildNodesMapIDMap(positions_node, "position");

		Node scale_params_node = MetaXMLLoader.getChildFirstNodeOfType(bodypart_node, "scale_params");
		derive_scale_params = MetaXMLLoader.getNodeAttribute(scale_params_node, "derive_from");
		scale_params = MetaXMLLoader.getAllChildNodesAsMap(scale_params_node);

		String hides_bodyparts_type_str = MetaXMLLoader.getStringParam(bodypart_node, "hides_bodyparts_by_type", "");
		hides_bodyparts_by_type = new HashSet<>(Arrays.asList(hides_bodyparts_type_str.split(";")));

		String hides_bodyparts_id_str = MetaXMLLoader.getStringParam(bodypart_node, "hides_bodyparts_by_id", "");
		hides_bodyparts_by_id = new HashSet<>(Arrays.asList(hides_bodyparts_id_str.split(";")));
	}

	public void doDerives(Map<String, Map<String, Map<String, RaceBodypart>>> raceBodyparts) {
		if (derive_mod_conditions != null) {
			if (raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name) && raceBodyparts.get(race_name).get(bodypart_name).containsKey(derive_mod_conditions)) {
				mod_conditions = raceBodyparts.get(race_name).get(bodypart_name).get(derive_mod_conditions).mod_conditions;
			}
		}
		if (derive_parent_connections != null) {
			if (raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name) && raceBodyparts.get(race_name).get(bodypart_name).containsKey(derive_parent_connections)) {
				parent_connections = raceBodyparts.get(race_name).get(bodypart_name).get(derive_parent_connections).parent_connections;
			}
		}
		if (derive_positions != null) {
			if (raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name) && raceBodyparts.get(race_name).get(bodypart_name).containsKey(derive_positions)) {
				positions = raceBodyparts.get(race_name).get(bodypart_name).get(derive_positions).positions;
			}
		}
		if (derive_conditions != null) {
			if (raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name) && raceBodyparts.get(race_name).get(bodypart_name).containsKey(derive_conditions)) {
				conditions = raceBodyparts.get(race_name).get(bodypart_name).get(derive_conditions).conditions;
			}
		}
		if (derive_modifiers != null) {
			if (raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name) && raceBodyparts.get(race_name).get(bodypart_name).containsKey(derive_modifiers)) {
				modifiers = raceBodyparts.get(race_name).get(bodypart_name).get(derive_modifiers).modifiers;
			}
		}
		if (derive_scale_params != null) {
			if (raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name) && raceBodyparts.get(race_name).get(bodypart_name).containsKey(derive_scale_params)) {
				scale_params = raceBodyparts.get(race_name).get(bodypart_name).get(derive_scale_params).scale_params;
			}
		}
	}

	public static String getCharacterParamByName(String condition_param, GameCharacter character) {
		try {
			String condition_val = "0";
			if (condition_param.startsWith("random")) {
				condition_val = condition_param.replace("random", "");
				condition_param = "random";
			} else if (condition_param.startsWith("charnamehash")) {
				condition_val = condition_param.replace("charnamehash", "");
				condition_param = "charnamehash";
			} else if (condition_param.startsWith("clothes_")) {
				condition_val = condition_param.replace("clothes_", "");
				condition_param = "clothes";
			}
			switch(condition_param) {
				case "true":
					return "1";
				case "name":
					return character.getNameIgnoresPlayerKnowledge().toLowerCase();
				case "random":
					return String.valueOf(ThreadLocalRandom.current().nextInt(0, Integer.valueOf(condition_val)));
				case "charnamehash":
					String hash_str = character.getNameIgnoresPlayerKnowledge() + "_" + character.getId();
					return String.valueOf(Math.abs((int)(hash_str.hashCode()/10)) % Integer.valueOf(condition_val));

				case "affection_level_to_player":
					return character.getAffectionLevel(Main.game.getPlayer()).getName().toLowerCase();

				// body
				case "pregnancy":
					int preg_level = 0;
					if(character.hasStatusEffect(StatusEffect.PREGNANT_1)){
						preg_level = 1;
					}else if(character.hasStatusEffect(StatusEffect.PREGNANT_2)){
						preg_level = 2;
					}else if(character.hasStatusEffect(StatusEffect.PREGNANT_3)){
						preg_level = 3;
					}
					return String.valueOf(preg_level);
				case "bodytypes":
					return character.getBodySize().getName(false).toLowerCase();
				case "muscles":
					return character.getMuscle().getName(false).toLowerCase();
				case "height_cm":
					int h_size = character.getHeightValue();
					if (h_size < 1) h_size = 1;
					return String.valueOf(h_size);

				// face
				case "feminity":
					return character.getFemininity().getName(false).toLowerCase();
				case "face_is_bestial":
					return String.valueOf(character.getFaceRace().isAffectedByFurryPreference() ? 1 : 0);
				case "ear_is_bestial":
					return String.valueOf(character.getEarRace().isAffectedByFurryPreference() ? 1 : 0);
				case "ear_type":
					return character.getEarType().getTransformName().toLowerCase();
				case "lips_size":
					return character.getLipSize().getName();

				// eyes
				case "eye_pairs":
					return String.valueOf(character.getEyePairs());
				case "eye_iris_shape":
					return character.getIrisShape().getName().toLowerCase();
				case "eye_pupil_shape":
					return character.getPupilShape().getName().toLowerCase();

				// hairs
				case "hair_length":
					return character.getHairLength().getDescriptor().toLowerCase();
				case "hair_type":
					return character.getHairType().getNameSingular(character).toLowerCase();
				case "hair_style":
					return character.getHairStyle().getName().toLowerCase();
				case "pubic_hair":
					return character.getPubicHair().getName().toLowerCase();

				// arms
				case "arm_rows":
					return String.valueOf(character.getArmRows());
				case "arm_is_bestial":
					return String.valueOf(character.getArmRace().isAffectedByFurryPreference() ? 1 : 0);
				case "is_hand_nails_polished":
					return String.valueOf((character.getHandNailPolish() != null && character.getHandNailPolish().getPrimaryColour() != Colour.COVERING_NONE) ? 1 : 0);

				// legs
				case "leg_is_bestial":
					return String.valueOf(character.getLegRace().isAffectedByFurryPreference() ? 1 : 0);
				case "leg_configuration":
					return character.getLegConfiguration().toString().toLowerCase();
				case "foot_structure":
					return character.getFootStructure().getName().toLowerCase();
				case "is_foot_nails_polished":
					return String.valueOf((character.getFootNailPolish() != null && character.getFootNailPolish().getPrimaryColour() != Colour.COVERING_NONE) ? 1 : 0);

				// breast
				case "breast_size":
					return character.getBreastSize().getDescriptor().toLowerCase();
				case "breast_rows":
					return String.valueOf(character.getBreastRows());

				// nipples
				case "areola_size":
					return character.getAreolaeSize().getName().toLowerCase();
				case "areola_shape":
					return character.getAreolaeShape().getName().toLowerCase();
				case "nipple_size":
					return character.getNippleSize().getName().toLowerCase();
				case "nipple_shape":
					return character.getNippleShape().getName().toLowerCase();
				case "nipple_count_per_breast":
					return String.valueOf(character.getNippleCountPerBreast());
				case "nipple_count_per_breast_crotch":
					return String.valueOf(character.getBody().getBreastCrotch().getNippleCountPerBreast());

				// udders
				case "breast_crotch_size":
					return character.getBreastCrotchSize().getDescriptor().toLowerCase();
				case "breast_crotch_rows":
					return String.valueOf(character.getBreastCrotchRows());

				// vagina
				case "vagina_labia_size":
					return character.getVaginaLabiaSize().getName().toLowerCase();
				case "vagina_clitoris_size":
					return character.getVaginaClitorisSize().getDescriptor().toLowerCase();
				case "genital_arrangement":
					return character.getGenitalArrangement().getName().toLowerCase();

				// penis
				case "penis_size_cm":
					int pp_size = character.getPenisRawSizeValue();
					if (pp_size < 1) pp_size = 1;
					return String.valueOf(pp_size);
				case "penis_girth":
					return character.getPenisGirth().getName().toLowerCase();
				case "penises_count":
					return String.valueOf((character.hasSecondPenis() ? 1 : 0) + (character.hasPenis() ? 1 : 0));
				case "has_penis":
					return String.valueOf(character.hasPenis() ? 1 : 0);
				case "penis_is_veiny":
					return String.valueOf(character.getPenisModifiers().contains(PenetrationModifier.VEINY) ? 1 : 0);
				case "penis_is_sheathed":
					return String.valueOf(character.getPenisModifiers().contains(PenetrationModifier.SHEATHED) ? 1 : 0);
				case "penis_is_barbed":
					return String.valueOf(character.getPenisModifiers().contains(PenetrationModifier.BARBED) ? 1 : 0);
				case "has_penis_erection":
					return String.valueOf(character.hasPenis() && character.hasErection() ? 1 : 0);

				// testicles
				case "testicles_count":
					return String.valueOf(character.getPenisNumberOfTesticles());
				case "testicle_size":
					return character.getTesticleSize().getDescriptor().toLowerCase();
				case "testicle_pairs_count":
					return String.valueOf(character.getPenisNumberOfTesticles() / 2);
				case "internal_testicles":
					return String.valueOf(character.isInternalTesticles() ? 1 : 0);

				// horns
				case "horn_rows":
					return String.valueOf(character.getHornRows());
				case "horn_type":
					return character.getHornType().getTransformName().toLowerCase();
				case "horns_count_per_row":
					return String.valueOf(character.getHornsPerRow());
				case "horn_length":
					return character.getBody().getHorn().getHornLength().getDescriptor().toLowerCase();
				case "horn_length_cm":
					return String.valueOf(character.getHornLength());

				// tails
				case "tail_count":
					return String.valueOf(character.getTailCount());
				case "tail_type":
					return character.getTailType().toString().toLowerCase();
				case "tail_girth":
					return character.getTailGirth().toString().toLowerCase();
				case "tail_pattern":
					CoveringPattern tp = BodyColorsMap.getBodypartCoveringPattern(character.getBody().getTail(), character);
					return tp!=null ? tp.toString().toLowerCase() : "none";

				// wings
				case "wing_type":
					return character.getWingType().toString().toLowerCase();
				case "wing_able_to_fly":
					return String.valueOf(character.getBody().isAbleToFlyFromWings() ? 1 : 0);
				case "wing_size":
					return character.getWingSize().getName().toLowerCase();

				// clothes
				case "clothes":
					InventorySlot c_slot = null;
					switch(condition_val) {
						case "torso_under":
							c_slot = InventorySlot.TORSO_UNDER;
							break;
						case "torso_over":
							c_slot = InventorySlot.TORSO_OVER;
							break;
						case "groin":
							c_slot = InventorySlot.GROIN;
							break;
						case "penis":
							c_slot = InventorySlot.PENIS;
							break;
						case "leg":
							c_slot = InventorySlot.LEG;
							break;
						case "foot":
							c_slot = InventorySlot.FOOT;
							break;
						case "head":
							c_slot = InventorySlot.HEAD;
							break;
						case "neck":
							c_slot = InventorySlot.NECK;
							break;
						case "finger":
							c_slot = InventorySlot.FINGER;
							break;
					}
					AbstractClothing c_clothing = c_slot != null ? character.getClothingInSlot(c_slot) : null;
					return c_clothing != null ? c_clothing.getName().toLowerCase(): "";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private boolean checkParamStatement(String param, String value, GameCharacter character) {
		if (value == null) value = "";
		boolean is_not = value.contains("!");
		if (is_not) value = value.replace("!", "");
		List<String> condition_values = Arrays.asList(value.split(";"));
		String check_value = getCharacterParamByName(param, character);
		boolean statement_result = true;
		if (check_value != null && !param.equals("true")) {
			statement_result = condition_values.contains(check_value);
		}
		if (is_not) statement_result = !statement_result;
		return statement_result;
	}
	
	public boolean checkBodypartConditions(GameCharacter character) {
		int checked_conditions = 0;
		for(Map<String, String> condition: conditions) {
			checked_conditions++;
			boolean condition_is_not = false;
			boolean condition_result = true;
			for (Map.Entry<String, String> entry : condition.entrySet()) {
				String condition_param = entry.getKey();
				String condition_value = entry.getValue();
				condition_result = condition_result && checkParamStatement(condition_param, condition_value, character);
			}
			if (condition_is_not) condition_result = !condition_result;
			if (condition_result) return true;
		}
		return checked_conditions <= 0;
	}

	private Double getModifierFromParamValue(String param_name, Double value) {
		switch (param_name) {
			case "inverse_x":
			case "inverse_y":
				if (value > 0) 
					return -1.0;
				else
					return null;
		}
		return value;
	}
	private Double getModifierFromParamString(String param_name, String value) {
		try {
			Double d_val = Double.parseDouble(value);
			return getModifierFromParamValue(param_name, d_val);
		} catch (NumberFormatException e) {}
		return null;
	}

	public Map<String, Double> getAllModifiersForCharacter(GameCharacter character) {
		return getAllModifiersForCharacter(character, null);
	}
	public Map<String, Double> getAllModifiersForCharacter(GameCharacter character, Map<String, String> parent_position_params) {
		Map<String, Double> result_modifiers = new HashMap<>();
		result_modifiers.putAll(modifiers);
		for(Map<String, String> condition: mod_conditions) {
			Map<String, Double> st_modifiers = new HashMap<>();
			boolean statement_result = true;
			for (Map.Entry<String, String> entry : condition.entrySet()) {
				String condition_param = entry.getKey();
				String condition_value = entry.getValue();
				if (ALL_MODIFIER_PARAMS.contains(condition_param)) {
					Double d_val = getModifierFromParamString(condition_param, condition_value);
					if (d_val != null) st_modifiers.put(condition_param, d_val);
				} else {
					statement_result = statement_result && checkParamStatement(condition_param, condition_value, character);
				}
			}
			if (statement_result) {
				for (Map.Entry<String, Double> entry : st_modifiers.entrySet()) {
					if (result_modifiers.containsKey(entry.getKey())) {
						if (MODIFIER_PARAMS_ADD.contains(entry.getKey())) {
							result_modifiers.put(entry.getKey(), result_modifiers.get(entry.getKey()) + entry.getValue());
						} else {
							result_modifiers.put(entry.getKey(), result_modifiers.get(entry.getKey()) * entry.getValue());
						}
					} else {
						result_modifiers.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		if (parent_position_params != null) {
			for (Map.Entry<String, String> entry : parent_position_params.entrySet()) {
				if (entry.getKey().startsWith("modifiers_")) {
					String mod_prop_name = entry.getKey().replace("modifiers_", "");
					if (ALL_MODIFIER_PARAMS.contains(mod_prop_name)) {
						Double d_val = getModifierFromParamString(mod_prop_name, entry.getValue());
						if (d_val != null) {
							if (result_modifiers.containsKey(mod_prop_name)) {
								if (MODIFIER_PARAMS_ADD.contains(mod_prop_name)) {
									result_modifiers.put(mod_prop_name, result_modifiers.get(mod_prop_name) + d_val);
								} else {
									result_modifiers.put(mod_prop_name, result_modifiers.get(mod_prop_name) * d_val);
								}
							} else {
								result_modifiers.put(mod_prop_name, d_val);
							}
						}
					}
				}
			}
		}
		return result_modifiers;
	}

	public boolean connectionIsForPosition(int parent_connection_index, String position) {
		if (parent_connection_index >=0 && parent_connection_index < parent_connections.size()) {
			String position_str = parent_connections.get(parent_connection_index).getOrDefault("position", null);
			if (position_str != null) {
				List<String> position_values = Arrays.asList(position_str.split(";"));
				return position_values.contains(position);
			}
			return true;
		}
		return false;
	}
	public boolean connectionIsForTypeAndPosition(String parent_type, String position) {
		return getParentConnectionByIdAndType(parent_type, position) >= 0;
	}
	public int getParentConnectionByIdAndType(String parent_type, String position) {
		for(int i=0; i<parent_connections.size(); i++) {
			String type_str = parent_connections.get(i).getOrDefault("type", null);
			if (type_str == null || Arrays.asList(type_str.split(";")).contains(parent_type)) {
				if (connectionIsForPosition(i, position)) return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		return "{code:" + bodypart_code + 
			" bt:" + bodypart_name + 
			" race:" + race_name + 
			" images:" + images + 
			" POS:" + positions.size() + (derive_positions != null ? "("+derive_positions+")" : "") +
			" PCN:" + parent_connections.size() + (derive_parent_connections != null ? "("+derive_parent_connections+")" : "") +
			" CND:" + conditions.size() + (derive_conditions != null ? "("+derive_conditions+")" : "") +
			" MOC:" + mod_conditions.size() + (derive_mod_conditions != null ? "("+derive_mod_conditions+")" : "") +
			"}";
	}
}
