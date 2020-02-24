package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.game.PropertyValue;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.rendering.CachedImage;
import com.lilithsthrone.rendering.CharacterImage;
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

	private final String chargen_root_dir = "res/images/chargen";
	private final String races_root_dir = chargen_root_dir + "/races";
	private final String clothes_root_dir = chargen_root_dir + "/clothes";
	private final String covering_types_dir = chargen_root_dir + "/covering_types";
	private final String pattern_dir = chargen_root_dir + "/patterns";
	private final String material_dir = chargen_root_dir + "/materials";
	private final String results_dir = chargen_root_dir + "/results";

	private final String common_race = "common";
	private final String default_fallback_race = "human";
	private final String[] root_bodyparts = new String[]{"body_taur", "body"};

	private final boolean skip_unknown_races = true;
	private final boolean show_clothes = true;

	private final boolean do_pre_scale = true;
	private final double base_image_scale = 1.0;
	private final int max_image_width = 600;
	private final int max_image_height = 600;

	private final boolean do_post_pixelization = false;
	private final int pixel_size = 12;
	private final int pixelated_image_width = 250;
	private final int pixelated_image_height = 250;

	private final boolean debug_mode = false;
	private final boolean reveal_everybody = false;
	private final boolean save_characters = false;

	private boolean initialized = false;

	private final Map<String, ColorPattern> covering_types = new HashMap<>();
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
			ColorPattern.loadPatterns(covering_types, covering_types_dir);
			ColorPattern.loadPatterns(patterns, pattern_dir);
			ColorPattern.loadPatterns(materials, material_dir);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		initialized = true;
	}

	private void initBodyparts() {
		raceBodyparts = new HashMap<>();
		raceFallbacks = new HashMap<>();
		raceBodypartFallbacks = new HashMap<>();
		List<String> dirs_to_scan = new ArrayList<>();
		dirs_to_scan.add(races_root_dir);
		dirs_to_scan.add(clothes_root_dir);
		for(String dir_to_scan: dirs_to_scan) {
			String[] races_meta_files = MetaXMLHelper.getDirMetaXML(dir_to_scan);
			for (String file : races_meta_files) {
				String race_name = file.replaceAll(".meta.xml", "").trim().toLowerCase();
				Document xml_document = MetaXMLHelper.openXMLFile(dir_to_scan + "/" + file);
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
			String[] races_dirs = MetaXMLHelper.getSubDirs(dir_to_scan);
			for (String race_dir : races_dirs) {
				String race_name = race_dir.trim().toLowerCase();
				String[] race_meta_files = MetaXMLHelper.getDirMetaXML(dir_to_scan + "/" + race_name);
				for (String file : race_meta_files) {
					String bodypart_name = file.replaceAll(".meta.xml", "").trim().toLowerCase();
					Document xml_document = MetaXMLHelper.openXMLFile(dir_to_scan + "/" + race_name + "/" + file);
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
				String[] bodypart_dirs = MetaXMLHelper.getSubDirs(dir_to_scan + "/" + race_name);
				for (String bodypart_dir : bodypart_dirs) {
					String bodypart_name = bodypart_dir.trim().toLowerCase();
					String[] part_meta_files = MetaXMLHelper.getDirMetaXML(dir_to_scan + "/" + race_name + "/" + bodypart_name);
					for (String file : part_meta_files) {
						String part_code = file.replaceAll(".meta.xml", "").trim().toLowerCase();
						String part_xml_name = dir_to_scan + "/" + race_name + "/" + bodypart_name + "/" + file;
						String img_name = dir_to_scan + "/" + race_name + "/" + bodypart_name + "/" + part_code + ".png";
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
		}
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
		if (skip_unknown_races && !raceBodyparts.containsKey(race_name) && !raceFallbacks.containsKey(race_name) && !raceBodypartFallbacks.containsKey(race_name)) {
			return null;
		}
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
		BodyPartRacesMap character_body_races_map = BodyPartRacesMap.fromCharacter(character, show_clothes);
		if (debug_mode) System.out.println(character_body_races_map);
		Map<String, Map<String, RaceBodypart>> bodyparts_for_use = new HashMap<>();
		for (Map.Entry<String, String> entry : character_body_races_map.entrySet()) {
			String race_name = entry.getValue();
			String bodypart_name = entry.getKey();
			if (race_name == null || race_name.equals("fallback")) race_name = getRaceBodypartFallback(null, bodypart_name);
			String fallback_race = getRaceBodypartFallback(race_name, bodypart_name);
			if (!raceBodyparts.containsKey(race_name) || !raceBodyparts.get(race_name).containsKey(bodypart_name)) {
				race_name = fallback_race;
			}
			if (race_name!= null && raceBodyparts.containsKey(race_name) && raceBodyparts.get(race_name).containsKey(bodypart_name)) {
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
			root.initChildPositionParts(bodyparts_for_use, character, 0);
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

			BodyPartDrawOrderList draw_list = null;
			CharacterBodypart root = getCharacterBodyPartTree(character);

			double root_scale = base_image_scale;
			if (root != null) {
				try {
					root.this_stretch_x = root_scale;
					root.this_stretch_y = root_scale;
					root.initDrawingParams(character);
					draw_list = root.getBodypartsList();
					draw_list.normalizePositions();
					if (do_pre_scale) {
						double c_width = draw_list.full_image_width;
						double c_height = draw_list.full_image_height;
						if (c_width > max_image_width || c_height > max_image_height) {
							root_scale = Math.min(max_image_width/c_width, max_image_height/c_height);
							if (debug_mode) System.out.println("Full image scale: " + root_scale);
							root.this_stretch_x = root_scale;
							root.this_stretch_y = root_scale;
							root.initDrawingParams(character);
							draw_list = root.getBodypartsList();
							draw_list.normalizePositions();
						}
					}
				} catch (IOException | NoSuchFieldException | NumberFormatException ex) {
					draw_list = null;
					ex.printStackTrace();
				}
			}
			if (draw_list == null) return null;

			boolean is_revealed = reveal_everybody || character.isPlayer() || ((Main.game.getPlayer().hasTraitActivated(Perk.OBSERVANT)) && !character.isRaceConcealed()) || (character.getTotalTimesHadSex(Main.game.getPlayer()) > 0);

			image = new CharacterImage();
			image.initForSize((int) Math.round(draw_list.full_image_width), (int) Math.round(draw_list.full_image_height));

			BodyPartColoringInfo default_coloring = new BodyPartColoringInfo();
			BodyPartColorsMap body_colors = BodyPartColorsMap.fromCharacter(character);
			if (debug_mode) System.out.println(body_colors);

			int index = 0;
			draw_list.prepareForDrawing();
			for(CharacterBodypart item : draw_list.getItems()) {
				if (item.is_hidden) continue;

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

				if (!item.bodypart.skip_colorization && item.coloring_texture == null && coloring.color_pattern != null && !item.bodypart.no_patterns && patterns.containsKey(coloring.color_pattern)) {
					item.coloring_texture = patterns.get(coloring.color_pattern).getColorizedTextureForBodypart(item.bodypart.pattern_scale * root_scale, coloring, item.bodypart.bodypart_name);
				}
				if (!item.bodypart.skip_colorization && item.covering_texture == null && coloring.covering_type != null && !item.bodypart.no_patterns && !item.bodypart.no_coverings && covering_types.containsKey(coloring.covering_type)) {
					item.covering_texture = covering_types.get(coloring.covering_type).getColorizedTextureForBodypart(item.bodypart.pattern_scale * root_scale, null, item.bodypart.bodypart_name);
				}
				CharacterImage material_texture = null;
				if (materials.containsKey(coloring.material)) {
					material_texture = materials.get(coloring.material).getColorizedTextureForBodypart(item.bodypart.pattern_scale * root_scale, null, item.bodypart.bodypart_name);
				}

				if (debug_mode) {
					System.out.println(index + ": " + item.bodypart.bodypart_name + " -> " + item.id_path + " ("+item.type_path+")" + " -> " + item.bodypart.bodypart_code + " (" + item.priority +")");
					/*System.out.println("x: " + item.draw_x_point + " y:" + item.draw_y_point + " w:" + item.draw_width + " h:" + item.draw_height);
					System.out.println(coloring);*/
				}

				image.drawColorizedBodypartImage(
					item.image,				// image of bodypart we need to add
					(int) Math.round(item.draw_x_point),	// x coord
					(int) Math.round(item.draw_y_point),	// y coord
					(int) Math.round(item.draw_width),	// stretched width
					(int) Math.round(item.draw_height), 	// stretched height
					coloring,				// coloring of bodypart
					item.coloring_texture,			// texture to use instead of plan color
					item.covering_texture,			// texture to use as covering before colorizing
					material_texture,			// material texture
					item.image_color_mask,			// mask for restriction of colorized area
					transition_params,			// gradient transition params
					item.parent != null ? item.parent.coloring_texture : null,									// parent coloring texture
					item.parent != null ? item.parent.covering_texture : null,									// parent covering texture
					item.parent != null ? new Point(item.parent.draw_x_point, item.parent.draw_y_point) : null,					// parent offset
					item.mask_target != null ? item.mask_target.image_mask : null,							// parent mask
					item.mask_target != null ? new Point(item.mask_target.draw_x_point, item.mask_target.draw_y_point) : null,	// parent mask offset
					item.rotation_center,			// center of rotation
					item.rotation_angle,			// rotation angle in degrees
					index++					// index of layer for debug
				);
			}
			if (save_characters) image.save(results_dir + "/"+character.getName()+"_" + String.format("%.2f", root_scale) +".png");
			if (do_post_pixelization) {
				image.pixelate(pixel_size);
				image.scaleDown(pixelated_image_width, pixelated_image_height);
			} else {
				image.scaleDown(max_image_width, max_image_height);
			}
			if (!is_revealed) image.colorOverlay(Color.GREY);
			image.updateImageString();

			patterns.entrySet().forEach((item) -> {
				item.getValue().checkCache();
			});
			materials.entrySet().forEach((item) -> {
				item.getValue().checkCache();
			});
			covering_types.entrySet().forEach((item) -> {
				item.getValue().checkCache();
			});

			if (debug_mode) System.out.println("---\nGeneration time: " + ((System.nanoTime() - startTime)/1000000) + " msec");
		} catch(Exception ex) {
			ex.printStackTrace();
			image = null;
		}

		return image;
	}
}
