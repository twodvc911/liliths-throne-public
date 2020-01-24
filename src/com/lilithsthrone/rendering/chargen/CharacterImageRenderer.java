package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.game.PropertyValue;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.types.BodyCoveringType;
import com.lilithsthrone.game.character.body.valueEnums.CoveringPattern;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.race.Race;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.rendering.CachedImage;
import com.lilithsthrone.rendering.CharacterImage;
import com.lilithsthrone.utils.Colour;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author twovgSP
 */
public class CharacterImageRenderer {
	
	public static final CharacterImageRenderer INSTANCE = new CharacterImageRenderer();
	private final String chargen_root_dir = "res/images/chargen/races";
	private final String pattern_dir = "res/images/chargen/patterns";
	private final String material_dir = "res/images/chargen/materials";
	private final String common_race = "common";
	private final String default_fallback_race = "human";
	private final String[] root_bodyparts = new String[]{"body_taur", "body"};

	private final boolean do_pre_scale = true;
	private final double base_image_scale = 1.0;
	private final int max_image_width = 600;
	private final int max_image_height = 600;
	
	private final boolean debug_mode = false;
	private final boolean reveal_everybody = false;
	private final boolean save_characters = false;
	
	private boolean initialized = false;
	
	private final Map<String, ColorPattern> materials = new HashMap<>();
	private final Map<String, ColorPattern> patterns = new HashMap<>();
	private Map<String, List<String>> raceFallbacks = null;
	private Map<String, Map<String, List<String>>> raceBodypartFallbacks = null;
	private Map<String, Map<String, Map<String, RaceBodypart>>> raceBodyparts = null;
	
	public CharacterImageRenderer() {
		if (generationIsAllowed()) {
			initParams();
		}
	}

	private boolean generationIsAllowed() {
		return Main.getProperties().hasValue(PropertyValue.generatedImages);
	}
	
	private void initParams() {
		try {
			initBodyparts();
			ColorPattern.loadPatterns(patterns, pattern_dir);
			ColorPattern.loadPatterns(materials, material_dir);
			initialized = true;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Map<String, String> getCharacterBodypartRaces(GameCharacter character) {
		Race body_race = character.getBody().getRace();
		Race head_race = character.getFaceRace();
		Race arm_race = character.getArmRace();
		Race leg_race = character.getLegRace();
		
		Race penis_race = character.getPenisRace();
		Race vagina_race = character.getVaginaRace();
		Race breasts_race = character.getBreastRace();
		Race breast_crotch_race = character.getBreastCrotchRace();
		
		Race hair_race = character.getHairRace();
		Race ear_race = character.getEarRace();
		Race eye_race = character.getEyeRace();
		Race wing_race = character.getWingRace();
		Race horn_race = character.getHornRace();
		Race antenna_race = character.getAntennaRace();
		
		Race ass_race = character.getAssRace();
		Race tongue_race = character.getTongueRace();
		Race tail_race = character.getTailRace();

		Race cloaca_race = character.getGenitalArrangement().toString().equals("CLOACA") ? (vagina_race != null && vagina_race != Race.NONE ? vagina_race : penis_race) : null;

		Map<String, String> real_parts_races = new HashMap<>();

		if (body_race != null && body_race != Race.NONE) {
			real_parts_races.put("body", body_race.name().toLowerCase());
			real_parts_races.put("belly", body_race.name().toLowerCase());
			if (character.isTaur()) real_parts_races.put("body_taur", body_race.name().toLowerCase());
		} else {
			real_parts_races.put("body", getRaceBodypartFallback(null, "body"));
			real_parts_races.put("belly", getRaceBodypartFallback(null, "belly"));
			if (character.isTaur()) real_parts_races.put("body_taur", getRaceBodypartFallback(null, "body_taur"));
		}
		
		if (head_race != null && head_race != Race.NONE) {
			real_parts_races.put("head", head_race.name().toLowerCase());
			real_parts_races.put("snout", head_race.name().toLowerCase());
			real_parts_races.put("lips", head_race.name().toLowerCase());
		} else {
			real_parts_races.put("head", getRaceBodypartFallback(null, "head"));
			real_parts_races.put("snout", getRaceBodypartFallback(null, "snout"));
			real_parts_races.put("lips", getRaceBodypartFallback(null, "lips"));
		}
		
		if (arm_race != null && arm_race != Race.NONE) {
			real_parts_races.put("arm", arm_race.name().toLowerCase());
		} else {
			real_parts_races.put("arm", getRaceBodypartFallback(null, "arm"));
		}
		
		if (leg_race != null && leg_race != Race.NONE) {
			real_parts_races.put("leg", leg_race.name().toLowerCase());
		} else {
			real_parts_races.put("leg", getRaceBodypartFallback(null, "leg"));
		}
		
		if (character.getBlusher() != null && character.getBlusher().getPrimaryColour()!=Colour.COVERING_NONE) {
			if (head_race != null && head_race != Race.NONE) {
				real_parts_races.put("blusher", head_race.name().toLowerCase());
			} else {
				real_parts_races.put("blusher", getRaceBodypartFallback(null, "blusher"));
			}
		}
		
		if (character.getEyeShadow() != null && character.getEyeShadow().getPrimaryColour()!=Colour.COVERING_NONE) {
			if (head_race != null && head_race != Race.NONE) {
				real_parts_races.put("eye_shadow", head_race.name().toLowerCase());
			} else {
				real_parts_races.put("eye_shadow", getRaceBodypartFallback(null, "eye_shadow"));
			}
		}
		
		if (character.getEyeLiner() != null && character.getEyeLiner().getPrimaryColour()!=Colour.COVERING_NONE) {
			if (head_race != null && head_race != Race.NONE) {
				real_parts_races.put("eye_liner", head_race.name().toLowerCase());
			} else {
				real_parts_races.put("eye_liner", getRaceBodypartFallback(null, "eye_liner"));
			}
		}

		if (penis_race != null && penis_race != Race.NONE) real_parts_races.put("penis", penis_race.name().toLowerCase());
		if (penis_race != null && penis_race != Race.NONE) real_parts_races.put("balls", penis_race.name().toLowerCase());
		if (vagina_race != null && vagina_race != Race.NONE) real_parts_races.put("vagina", vagina_race.name().toLowerCase());
		if (breasts_race != null && breasts_race != Race.NONE) real_parts_races.put("breasts", breasts_race.name().toLowerCase());
		if (breasts_race != null && breasts_race != Race.NONE) real_parts_races.put("nipple", breasts_race.name().toLowerCase());
		if (breast_crotch_race != null && breast_crotch_race != Race.NONE) real_parts_races.put("breasts-crotch", breast_crotch_race.name().toLowerCase());
		if (breast_crotch_race != null && breast_crotch_race != Race.NONE) real_parts_races.put("breasts-crotch-nipple", breast_crotch_race.name().toLowerCase());
		if (hair_race != null && hair_race != Race.NONE) real_parts_races.put("hair", hair_race.name().toLowerCase());
		if (ear_race != null && ear_race != Race.NONE) real_parts_races.put("ear", ear_race.name().toLowerCase());
		if (eye_race != null && eye_race != Race.NONE) real_parts_races.put("eye", eye_race.name().toLowerCase());
		if (eye_race != null && eye_race != Race.NONE) real_parts_races.put("eye_sclera", eye_race.name().toLowerCase());
		if (eye_race != null && eye_race != Race.NONE) real_parts_races.put("eye_pupil", eye_race.name().toLowerCase());
		if (eye_race != null && eye_race != Race.NONE) real_parts_races.put("eye_iris", eye_race.name().toLowerCase());
		if (wing_race != null && wing_race != Race.NONE) real_parts_races.put("wing", wing_race.name().toLowerCase());
		if (horn_race != null && horn_race != Race.NONE) real_parts_races.put("horn", horn_race.name().toLowerCase());
		if (antenna_race != null && antenna_race != Race.NONE) real_parts_races.put("antenna", antenna_race.name().toLowerCase());
		if (ass_race != null && ass_race != Race.NONE) real_parts_races.put("ass", ass_race.name().toLowerCase());
		if (cloaca_race != null && cloaca_race != Race.NONE) real_parts_races.put("cloaca", cloaca_race.name().toLowerCase());
		if (tongue_race != null && tongue_race != Race.NONE) real_parts_races.put("tongue", tongue_race.name().toLowerCase());
		if (tail_race != null && tail_race != Race.NONE) real_parts_races.put("tail", tail_race.name().toLowerCase());
		
		//System.out.println(real_parts_races);
		
		return real_parts_races;
	}
	
	private BodyColorsMap getCharacterColors(GameCharacter character) {
		BodyColorsMap body_colors = new BodyColorsMap();
		body_colors.add_body_color("body", character.getBody().getSkin(), character);
		body_colors.add_body_color("body_taur", character.getBody().getLeg(), character);
		body_colors.add_body_color("belly", character.getBody().getSkin(), character);
		body_colors.add_body_color("leg", character.getBody().getLeg(), character);
		body_colors.add_body_color("arm", character.getBody().getArm(), character);
		body_colors.add_body_color("head", character.getBody().getFace(), character);
		body_colors.add_body_color("blusher", character.getBlusher(), character);
		body_colors.add_body_color("eye_shadow", character.getEyeShadow(), character);
		body_colors.add_body_color("eye_liner", character.getEyeLiner(), character);
		body_colors.add_body_color("snout", character.getBody().getFace(), character);
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
		body_colors.add_body_color("eye", Color.WHITE);
		body_colors.add_body_color("eye_sclera", BodyCoveringType.EYE_SCLERA, character);
		body_colors.add_body_color("eye_pupil", BodyCoveringType.EYE_PUPILS, character);
		body_colors.add_body_color("eye_iris", character.getBody().getEye(), character);
		body_colors.add_body_color("wing", character.getBody().getWing(), character);
		body_colors.add_body_color("ass", character.getBody().getAss(), character);
		body_colors.add_body_color("tongue", character.getBody().getFace()!= null ? character.getBody().getFace().getTongue() : null, character);
		body_colors.add_body_color("tail", character.getBody().getTail(), character);
		
		body_colors.mix_main_body_colors("nipple", "breasts", 0.5);

		if (BodyColorsMap.getBodypartCoveringPattern(character.getBody().getEye(), character) == CoveringPattern.EYE_IRISES_HETEROCHROMATIC) body_colors.add_body_secondary_to_main_color("eye_iris_inversed", "eye_iris");
		if (BodyColorsMap.getBodypartCoveringPattern(BodyCoveringType.EYE_SCLERA, character) == CoveringPattern.EYE_SCLERA_HETEROCHROMATIC) body_colors.add_body_secondary_to_main_color("eye_sclera_inversed", "eye_sclera");
		if (BodyColorsMap.getBodypartCoveringPattern(BodyCoveringType.EYE_PUPILS, character) == CoveringPattern.EYE_PUPILS_HETEROCHROMATIC) body_colors.add_body_secondary_to_main_color("eye_pupil_inversed", "eye_pupil");

		return body_colors;
	}
	
	private void initBodyparts() {
		raceBodyparts = new HashMap<>();
		raceFallbacks = new HashMap<>();
		raceBodypartFallbacks = new HashMap<>();
		String[] races_meta_files = ChargenMetaXMLLoader.getDirMetaXML(chargen_root_dir);
		for (String file : races_meta_files) {
			String race_name = file.replaceAll(".meta.xml", "").trim().toLowerCase();
			Document xml_document = ChargenMetaXMLLoader.openXMLFile(chargen_root_dir + "/" + file);
			if (xml_document != null) {
				NodeList fb_race = xml_document.getElementsByTagName("fallback_race");
				if (fb_race != null && fb_race.getLength() > 0) {
					for(int i=0; i<fb_race.getLength(); i++) {
						String fallback_race_name = fb_race.item(i).getTextContent();
						fallback_race_name = fallback_race_name.trim().toLowerCase();
						if (!raceFallbacks.containsKey(race_name)) {
							raceFallbacks.put(race_name, new ArrayList<>());
						}
						raceFallbacks.get(race_name).add(fallback_race_name);
					}
				}
			}
		}
		String[] races_dirs = ChargenMetaXMLLoader.getSubDirs(chargen_root_dir);
		for (String race_dir : races_dirs) {
			String race_name = race_dir.trim().toLowerCase();
			String[] race_meta_files = ChargenMetaXMLLoader.getDirMetaXML(chargen_root_dir + "/" + race_name);
			for (String file : race_meta_files) {
				String bodypart_name = file.replaceAll(".meta.xml", "").trim().toLowerCase();
				Document xml_document = ChargenMetaXMLLoader.openXMLFile(chargen_root_dir + "/" + race_name + "/" + file);
				if (xml_document != null) {
					NodeList fb_race = xml_document.getElementsByTagName("fallback_race");
					if (fb_race != null && fb_race.getLength() > 0) {
						for(int i=0; i<fb_race.getLength(); i++) {
							String fallback_race_name = fb_race.item(i).getTextContent();
							fallback_race_name = fallback_race_name.trim().toLowerCase();
							if (!raceBodypartFallbacks.containsKey(race_name)) {
								raceBodypartFallbacks.put(race_name, new HashMap<>());
							}
							if (!raceBodypartFallbacks.get(race_name).containsKey(bodypart_name)) {
								raceBodypartFallbacks.get(race_name).put(bodypart_name, new ArrayList<>());
							}
							raceBodypartFallbacks.get(race_name).get(bodypart_name).add(fallback_race_name);
						}
					}
				}
			}
			String[] bodypart_dirs = ChargenMetaXMLLoader.getSubDirs(chargen_root_dir + "/" + race_name);
			for (String bodypart_dir : bodypart_dirs) {
				String bodypart_name = bodypart_dir.trim().toLowerCase();
				String[] part_meta_files = ChargenMetaXMLLoader.getDirMetaXML(chargen_root_dir + "/" + race_name + "/" + bodypart_name);
				for (String file : part_meta_files) {
					String part_code = file.replaceAll(".meta.xml", "").trim().toLowerCase();
					String part_xml_name = chargen_root_dir + "/" + race_name + "/" + bodypart_name + "/" + file;
					String img_name = chargen_root_dir + "/" + race_name + "/" + bodypart_name + "/" + part_code + ".png";
					RaceBodypart new_part;
					try {
						new_part = new RaceBodypart(bodypart_name, part_code, race_name, part_xml_name, img_name);
						if (!raceBodyparts.containsKey(race_name)) {
							raceBodyparts.put(race_name, new HashMap<>());
						}
						if (!raceBodyparts.get(race_name).containsKey(bodypart_name)) {
							raceBodyparts.get(race_name).put(bodypart_name, new HashMap<>());
						}
						raceBodyparts.get(race_name).get(bodypart_name).put(part_code, new_part);
					} catch (IOException ex) {
						System.out.println(ex.toString());
					}
				}
			}
		}
		raceBodyparts.entrySet().forEach((entry) -> {
			entry.getValue().entrySet().forEach((entry2) -> {
				entry2.getValue().entrySet().forEach((entry3) -> {
					entry3.getValue().doDerives(raceBodyparts);
				});
			});
		});
	}

	private String pickFallbackRaceFromListFor(List<String> fallback_race_names, String bodypart_name) {
		for(String fallback_race: fallback_race_names) {
			if (raceBodyparts.containsKey(fallback_race) && raceBodyparts.get(fallback_race).containsKey(bodypart_name)) {
				return fallback_race;
			}
		}
		return null;
	}
	
	private String getRaceBodypartFallback(String race_name, String bodypart_name) {
		String fallback_race = null;
		boolean has_fallback_race_config = raceFallbacks.containsKey(race_name);
		if (race_name != null && bodypart_name != null) {
			if (raceBodypartFallbacks.containsKey(race_name) && raceBodypartFallbacks.get(race_name).containsKey(bodypart_name)) {
				fallback_race = pickFallbackRaceFromListFor(raceBodypartFallbacks.get(race_name).get(bodypart_name), bodypart_name);
			}
			if (fallback_race == null && raceFallbacks.containsKey(race_name)) {
				fallback_race = pickFallbackRaceFromListFor(raceFallbacks.get(race_name), bodypart_name);
			}
		}
		if (fallback_race != null) return fallback_race;
		if (!has_fallback_race_config && (!raceBodyparts.containsKey(common_race) || !raceBodyparts.get(common_race).containsKey(bodypart_name))) {
			return default_fallback_race;
		}
		return common_race;
	}
	
	private RaceBodypart pickBodypartByPriority(Map<String, RaceBodypart> bodyparts) {
		double max_bodypart_priority = 0;
		RaceBodypart picked = null;
		for (Map.Entry<String, RaceBodypart> entry : bodyparts.entrySet()) {
			if (picked == null || (entry.getValue().pick_priority > max_bodypart_priority)) {
				picked = entry.getValue();
				max_bodypart_priority = picked.pick_priority;
			}
		}
		return picked;
	}
	
	private CharacterBodypart getCharacterBodyPartTree(GameCharacter character) {
		Map<String, String> real_parts_races = getCharacterBodypartRaces(character);
		Map<String, Map<String, RaceBodypart>> bodyparts_for_use = new HashMap<>();
		for (Map.Entry<String, String> entry : real_parts_races.entrySet()) {
			String race_name = entry.getValue();
			String bodypart_name = entry.getKey();
			String fallback_race = getRaceBodypartFallback(race_name, bodypart_name);
			if (!raceBodyparts.containsKey(race_name) || !raceBodyparts.get(race_name).containsKey(bodypart_name)) {
				race_name = fallback_race;
			}
			if (raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name)) {
				RaceBodypart fallback_bodypart = null;
				for (Map.Entry<String, RaceBodypart> part_entry : raceBodyparts.get(race_name).get(bodypart_name).entrySet()) {
					if (part_entry.getValue().checkBodypartConditions(character)) {
						if (!bodyparts_for_use.containsKey(bodypart_name)) {
							bodyparts_for_use.put(bodypart_name, new HashMap<>());
						}
						bodyparts_for_use.get(bodypart_name).put(part_entry.getKey(), part_entry.getValue());
					} else if (part_entry.getValue().is_fallback) {
						fallback_bodypart = part_entry.getValue();
					}
				}
				if (fallback_bodypart != null) {
					if (!bodyparts_for_use.containsKey(bodypart_name)) {
						bodyparts_for_use.put(bodypart_name, new HashMap<>());
					}
					if (bodyparts_for_use.get(bodypart_name).isEmpty()) {
						bodyparts_for_use.get(bodypart_name).put(fallback_bodypart.bodypart_code, fallback_bodypart);
					}
				}
			}
		}

		RaceBodypart main_body = null;
		for(int i=0; (i<root_bodyparts.length) && (main_body == null); i++) {
			String root_name = root_bodyparts[i];
			if (bodyparts_for_use.containsKey(root_name) && !bodyparts_for_use.get(root_name).isEmpty()) {
				main_body = pickBodypartByPriority(bodyparts_for_use.get(root_name));
			}
		}

		if (main_body == null) return null;

		CharacterBodypart root = null;
		try {
			root = new CharacterBodypart(main_body.bodypart_name, main_body.bodypart_name, main_body, null);
			root.initChildPositionParts(bodyparts_for_use, character);
		} catch (NoSuchFieldException | IOException ex) {
			root = null;
			ex.printStackTrace();
		}

		return root;
	}

	public CachedImage renderCharacter(GameCharacter character) {
		
		if (!generationIsAllowed()) return null;
		if (debug_mode || !initialized) initParams();

		CharacterImage image = null;
		try{
			long startTime = System.nanoTime();

			if (debug_mode) System.out.println(character.getName());

			List<CharacterBodypart> items = null;
			CharacterBodypart root = getCharacterBodyPartTree(character);

			double root_scale = base_image_scale;
			if (root != null) {
				try {
					root.this_stretch_x = root_scale;
					root.this_stretch_y = root_scale;
					root.initDrawingParams(character);
					items = root.getSortedBodypartList();
					if (do_pre_scale) {
						double c_width = root.full_image_width;
						double c_height = root.full_image_height;
						if (c_width > max_image_width || c_height > max_image_height) {
							root_scale = Math.min(max_image_width/c_width, max_image_height/c_height);
							if (debug_mode) System.out.println("Full image scale: " + root_scale);
							root.this_stretch_x = root_scale;
							root.this_stretch_y = root_scale;
							root.initDrawingParams(character);
							items = root.getSortedBodypartList();
						}
					}
				} catch (IOException | NoSuchFieldException | NumberFormatException ex) {
					root = null;
					ex.printStackTrace();
				}
			}

			if (root == null || items == null) return null;

			boolean is_revealed = reveal_everybody || character.isPlayer() || ((Main.game.getPlayer().hasTraitActivated(Perk.OBSERVANT)) && !character.isRaceConcealed()) || (character.getTotalTimesHadSex(Main.game.getPlayer()) > 0);
			root.initMasks();

			int image_width = (int) Math.round(root.full_image_width);
			int image_height = (int) Math.round(root.full_image_height);

			image = new CharacterImage();
			image.initForSize(image_width, image_height);

			BodyColorsMap body_colors = getCharacterColors(character);

			//System.out.println("Image size: " + image_width + " " + image_height);
			if (debug_mode) System.out.println(body_colors);

			int index = 0;
			BodyPartColoringInfo default_coloring = new BodyPartColoringInfo();

			for(CharacterBodypart item : items) {
				if (item.bodypart.is_hidden) continue;
				item.image.flip_image(item.draw_inverse_x, item.draw_inverse_y);
				if (item.image_mask != null) {
					item.image_mask.flip_image(item.draw_inverse_x, item.draw_inverse_y);
				}
			}

			String material_name = character.getBodyMaterial().toString().toLowerCase();
			ColorPattern material = materials.getOrDefault(material_name, null);

			for(CharacterBodypart item : items) {
				if (item.bodypart.is_hidden) continue;

				GradientParams transition_params = null;
				BodyPartColoringInfo coloring = null;

				if (!item.bodypart.skip_colorization) {
					if (item.draw_inverse_x || item.draw_inverse_y) coloring = body_colors.getOrDefault(item.bodypart.bodypart_name + "_inversed", null);
					if (coloring == null && item.bodypart.derive_color_from_bodypart != null) coloring = body_colors.getOrDefault(item.bodypart.derive_color_from_bodypart, null);
					if (coloring == null) coloring = body_colors.getOrDefault(item.bodypart.bodypart_name, default_coloring);
				} else {
					coloring = default_coloring;
				}
				if (item.bodypart.color_to_mix != null) coloring = coloring.getMixWithColor(item.bodypart.color_to_mix, item.bodypart.mix_type, item.bodypart.mix_param);

				if (item.transition_bodypart_code != null && body_colors.containsKey(item.transition_bodypart_code) && !item.bodypart.no_transitions) {
					transition_params = new GradientParams();
					transition_params.gradient_color = body_colors.getPrimaryColor(item.transition_bodypart_code);
					transition_params.transition_border1 = item.point_1_border;
					transition_params.transition_border2 = item.point_2_border;
					transition_params.part_transition_percent = item.transition_area_width_percent;
					transition_params.is_inverted = item.transition_is_inverted;
				}

				if (!item.bodypart.skip_colorization && item.coloring_texture == null && coloring.covering_pattern != null && !item.bodypart.no_patterns && patterns.containsKey(coloring.covering_pattern)) {
					item.coloring_texture = patterns.get(coloring.covering_pattern).getColorizedTextureForBodypart(item.bodypart.pattern_scale * root_scale, coloring, item.bodypart.bodypart_name);
				}

				if (debug_mode) {
					System.out.println(index + ": " + item.bodypart.bodypart_name + " -> " + item.id + " -> " + item.bodypart.bodypart_code + " (" + item.priority +")");
					/*System.out.println("x: " + item.draw_x_point + " y:" + item.draw_y_point + " w:" + item.draw_width + " h:" + item.draw_height);
					System.out.println(coloring);*/
				}

				image.drawColorizedImage(
					item.image,		// image of bodypart we need to add
					(int) Math.round(item.draw_x_point),	// x coord
					(int) Math.round(item.draw_y_point),	// y coord
					(int) Math.round(item.draw_width),	// stretched width
					(int) Math.round(item.draw_height), 	// stretched height
					coloring,		// coloring of bodypart
					item.coloring_texture,	// texture to use instead of plan color
					material != null ? material.getColorizedTextureForBodypart(item.bodypart.pattern_scale * root_scale, null, item.bodypart.bodypart_name) : null,
					item.bodypart.use_mask_for_colorization ? item.image_mask : null,
					transition_params,	// gradient transition params
					item.parent != null ? item.parent.coloring_texture : null,									// parent coloring texture
					item.parent != null ? new Point(item.parent.draw_x_point, item.parent.draw_y_point) : null,					// parent offset
					item.parent_with_mask != null ? item.parent_with_mask.image_mask : null,							// parent mask
					item.parent_with_mask != null ? new Point(item.parent_with_mask.draw_x_point, item.parent_with_mask.draw_y_point) : null,	// parent offset
					item.rotation_center,	// center of rotation
					item.rotation_angle,	// rotation angle in degrees
					index++			// index of layer for debug
				);
			}
			if (save_characters) image.save("res/images/chargen/results/"+character.getName()+"_" + String.format("%.2f", root_scale) +".png");
			image.scaleDown(max_image_width, max_image_height);
			if (!is_revealed) image.color_set(Color.GREY);
			image.updateImageString();

			for(Map.Entry<String, ColorPattern> pattern : patterns.entrySet()) {
				pattern.getValue().checkCache();
			}

			if (debug_mode) System.out.println("---\nGeneration time: " + ((System.nanoTime() - startTime)/1000000) + " msec");
		} catch(Exception ex) {
			ex.printStackTrace();
			image = null;
		}

		return image;
	}
}
