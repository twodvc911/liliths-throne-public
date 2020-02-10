package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.rendering.CharacterImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author twovgSP
 */
public class CharacterBodypart {
	public final String id;
	public final String xml_id;
	public final String id_path;
	public final String type_path;

	public final RaceBodypart bodypart;
	public final CharacterBodypart parent;
	public Map<String, CharacterBodypart> child_parts = null;
	public CharacterImage image = null;
	public CharacterImage image_mask = null;
	public CharacterImage image_color_mask = null;
	public CharacterImage coloring_texture = null;
	public CharacterImage covering_texture = null;

	public CharacterBodypart mask_target = null;
	public boolean draw_inverse_x = false;
	public boolean draw_inverse_y = false;
	public String transition_bodypart_code = null;
	public int transition_area_width_percent = 0;
	public boolean transition_is_inverted = false;

	public boolean is_hidden = false;

	public double draw_x_point = 0;
	public double draw_y_point = 0;
	public double draw_width = 0;
	public double draw_height = 0;
	
	private int image_initial_width = 0;
	private int image_initial_height = 0;

	public double priority = 0.0;
	public double this_stretch_x = 1.0;
	public double this_stretch_y = 1.0;

	public int row_index = 0;
	public int rows_count = 0;
	public int col_index = 0;
	public int cols_count = 0;

	public Point point_1_border = null;
	public Point point_2_border = null;

	public Point rotation_center = null;
	public double rotation_angle = 0.0;

	private final double base_height_for_scale_cm = 180;
	private final int max_bodypart_depth = 40;

	public CharacterBodypart(String bp_id, String bp_xml_id, RaceBodypart bp, CharacterBodypart bp_parent) throws IOException {
		this.id = bp_id;
		this.xml_id = bp_xml_id;
		this.bodypart = bp;
		this.id_path = (bp_parent != null ? bp_parent.id_path : "") + "." + bp_xml_id;
		this.type_path = (bp_parent != null ? bp_parent.type_path : "") + "." + bp.bodypart_name;
		this.parent = bp_parent;
		this.is_hidden = bp.is_hidden;
		this.priority = (bp.priority_offset != 0) && (bp_parent != null) ? (bp_parent.priority + bp.priority_offset) : bp.priority;
		if (!bodypart.is_hidden) initImage();
	}

	private RaceBodypart pickBodypart(Map<String, Map<String, RaceBodypart>> bodyparts_for_use, String bodypart_code, String parent_type, String position_id, Integer parent_variant, GameCharacter character) {
		if (!bodyparts_for_use.containsKey(bodypart_code) || bodyparts_for_use.get(bodypart_code).isEmpty()) {
			return null;
		}
		double max_bodypart_priority = 0;
		List<RaceBodypart> picked = new ArrayList<>();
		for (Map.Entry<String, RaceBodypart> part_entry : bodyparts_for_use.get(bodypart_code).entrySet()) {
			if (part_entry.getValue().connectionIsForTypeAndPosition(parent_type, position_id)) {
				if ((picked.isEmpty() || (part_entry.getValue().pick_priority >= max_bodypart_priority)) && part_entry.getValue() != this.bodypart) {
					if (part_entry.getValue().pick_priority > max_bodypart_priority) picked.clear();
					picked.add(part_entry.getValue());
					max_bodypart_priority = part_entry.getValue().pick_priority;
				}
			}
		}
		if (picked.size() > 1) {
			if (parent_variant != null) {
				List<RaceBodypart> picked_for_parent_variant = new ArrayList<>();
				for(RaceBodypart picked_i: picked) {
					if (parent_variant.equals(picked_i.variant_index)) {
						picked_for_parent_variant.add(picked_i);
					}
				}
				if (picked_for_parent_variant.size() == 1) {
					return picked_for_parent_variant.get(0);
				} else if (picked_for_parent_variant.size() > 1) {
					picked = picked_for_parent_variant;
				}
			}
			picked.sort((RaceBodypart rb1, RaceBodypart rb2) -> {
				int compare_var = 0;
				if (rb1.variant_index != null && rb2.variant_index != null) {
					compare_var = Integer.compare(rb1.variant_index, rb2.variant_index);
				} else if (rb1.variant_index != null && rb2.variant_index == null) {
					compare_var = -1;
				} else if (rb1.variant_index == null && rb2.variant_index != null) {
					compare_var = 1;
				}
				if (compare_var == 0) compare_var = rb1.bodypart_code.compareTo(rb2.bodypart_code);
				return compare_var;
			});
			String hash_str = character.getNameIgnoresPlayerKnowledge() + "_" + character.getId() + "_" + parent_type;
			int pick_index = Math.abs((int)(hash_str.hashCode()/10)) % picked.size();
			return picked.get(pick_index);
		} else if (picked.size() == 1) {
			return picked.get(0);
		}
		return null;
	}

	public void initChildPositionParts(Map<String, Map<String, RaceBodypart>> bodyparts_for_use, GameCharacter character, int depth) throws NoSuchFieldException, IOException {
		child_parts = new HashMap<>();
		if (depth > max_bodypart_depth) {
			return;
		}
		for (Map.Entry<String, Map<String, String>> entry : bodypart.positions.entrySet()) {
			Map<String, String> position_params = entry.getValue();
			String position_id = entry.getKey();
			String type = position_params.getOrDefault("type", null);
			String required = position_params.getOrDefault("required", null);
			if (position_id != null && !position_id.isEmpty() && type != null && !type.isEmpty()) {
				int bp_rows_count = 1;
				int bp_cols_count = 1;
				String multiple_rows_count = RaceBodypart.getCharacterParamByName(position_params.getOrDefault("multiple_rows_count_param", ""), character);
				if (multiple_rows_count != null && !multiple_rows_count.isEmpty()) {
					int tmp_rows_count = Integer.valueOf(multiple_rows_count);
					if (tmp_rows_count > 1) {
						bp_rows_count = tmp_rows_count;
					}
				}
				String multiple_cols_count = RaceBodypart.getCharacterParamByName(position_params.getOrDefault("multiple_cols_count_param", ""), character);
				if (multiple_cols_count != null && !multiple_cols_count.isEmpty()) {
					int tmp_cols_count = Integer.valueOf(multiple_cols_count);
					if (tmp_cols_count > 1) {
						bp_cols_count = tmp_cols_count;
					}
				}
				
				for(int i=0; i<bp_rows_count; i++) {
					for(int j=0; j<bp_cols_count; j++) {
						RaceBodypart picked_bp = pickBodypart(bodyparts_for_use, type, bodypart.bodypart_name, position_id, bodypart.variant_index, character);
						if (picked_bp != null) {
							String position_cl_id = position_id;
							if (i > 0 || j > 0) {
								if (bp_rows_count > 1) position_cl_id += "[" + i + "]";
								if (bp_cols_count > 1) position_cl_id += "[" + j + "]";
							}
							CharacterBodypart new_part = new CharacterBodypart(position_cl_id, position_id, picked_bp, this);
							new_part.row_index = i;
							new_part.rows_count = bp_rows_count;
							new_part.col_index = j;
							new_part.cols_count = bp_cols_count;
							child_parts.put(position_cl_id, new_part);
							new_part.initChildPositionParts(bodyparts_for_use, character, depth+1);
						} else if ("1".equals(required)) {
							throw new NoSuchFieldException("No available required bodypart for id="+position_id+", type="+type+" was found!");
						}
					}
				}
			}
		}
	}

	private void initImage() throws IOException {
		image = bodypart.images.getImage("main");
		if (image == null) throw new IOException("Can't load image for id="+id+"!");
		image_initial_width = image.getWidth();
		image_initial_height = image.getHeight();
	}

	public BodyPartDrawOrderList getBodypartsList() {
		BodyPartDrawOrderList result = new BodyPartDrawOrderList();
		result.add(this);
		for (Map.Entry<String, CharacterBodypart> entry : child_parts.entrySet()) {
			result.addAll(entry.getValue().getBodypartsList());
		}
		return result;
	}

	public void initDrawingParams(GameCharacter character) throws NoSuchFieldException, NumberFormatException, IOException {
		initDrawingParams(this, character);
	}

	private void initDrawingParams(CharacterBodypart root, GameCharacter character) throws NoSuchFieldException, NumberFormatException, IOException {
		if (parent == null) {
			// root is body core
			draw_width = this_stretch_x * image_initial_width;
			draw_height = this_stretch_y * image_initial_height;
		} else if (!bodypart.is_hidden) {
			// init drawing params not root bodyparts for character

			// calc priority based on parent
			priority = bodypart.priority_offset != 0 ? parent.priority + bodypart.priority_offset : bodypart.priority;

			// getting connection points for this and parent
			Map<String, String> this_parent_position_params = parent.bodypart.positions.getOrDefault(xml_id, null);
			ConnectionPoints parent_c = ConnectionPoints.getConnectionPointsFromParams(this_parent_position_params, parent.image_initial_width, parent.image_initial_height);
			int parent_conn_id = bodypart.getParentConnectionByIdAndType(parent.bodypart.bodypart_name, xml_id);
			if (parent_conn_id < 0) {
				throw new NoSuchFieldException("Cant find parent connection for id="+id+"!");
			}
			Map<String, String> this_connection_params = bodypart.parent_connections.get(parent_conn_id);
			ConnectionPoints this_c = ConnectionPoints.getConnectionPointsFromParams(this_connection_params, image_initial_width, image_initial_height);
			if (parent_c == null || this_c == null) {
				throw new NoSuchFieldException("Points error for bodypart with id="+id+"!");
			}

			// getting transition params
			transition_bodypart_code = this_parent_position_params.getOrDefault("transition_bodypart_type", null);
			transition_area_width_percent = Integer.valueOf(this_parent_position_params.getOrDefault("transition_area_size_percent", "10"));
			transition_is_inverted = Integer.valueOf(this_parent_position_params.getOrDefault("transition_inverted", "0")) == 1;

			// parent scale results
			double parent_stretch_x = (double) parent.draw_width / (double) parent.image_initial_width;
			double parent_stretch_y = (double) parent.draw_height / (double) parent.image_initial_height;

			// conditional modifiers
			Map<String, Double> modifiers = bodypart.getAllModifiersForCharacter(character, this_parent_position_params);

			// inversion flags calculation
			draw_inverse_x = false;
			draw_inverse_y = false;
			if (parent.draw_inverse_x) draw_inverse_x = !draw_inverse_x;
			if (parent.draw_inverse_y) draw_inverse_y = !draw_inverse_y;
			if (modifiers.getOrDefault("inverse_x", Double.valueOf(1)) < 0) draw_inverse_x = !draw_inverse_x;
			if (modifiers.getOrDefault("inverse_y", Double.valueOf(1)) < 0) draw_inverse_y = !draw_inverse_y;
			if ((cols_count > 1) && "1".equals(this_parent_position_params.getOrDefault("multiple_inverse_by_cols", ""))) {
				if (col_index >= (cols_count/2)) draw_inverse_x = !draw_inverse_x;
			}
			if ((rows_count > 1) && "1".equals(this_parent_position_params.getOrDefault("multiple_inverse_by_rows", ""))) {
				if (row_index >= (rows_count/2)) draw_inverse_y = !draw_inverse_y;
			}

			// inverting points
			this_c.invertPoints(draw_inverse_x, draw_inverse_y, image_initial_width, image_initial_height);
			parent_c.invertPoints(parent.draw_inverse_x, parent.draw_inverse_y, parent.image_initial_width, parent.image_initial_height);

			// scale params calculation for current item
			this_stretch_x = bodypart.scale_x;
			this_stretch_y = bodypart.scale_y;

			if (this_c.p1.x != this_c.p2.x || this_c.p1.y != this_c.p2.y) {
				if (parent_c.p1.x != parent_c.p2.x || parent_c.p1.y != parent_c.p2.y) {
					if (this_c.p1.x != this_c.p2.x && parent_c.p1.x != parent_c.p2.x) {
						this_stretch_x = Math.abs((double) parent_c.p2.x - parent_c.p1.x) / Math.abs((double) this_c.p2.x - this_c.p1.x);
					}
					if (this_c.p1.y != this_c.p2.y && parent_c.p1.y != parent_c.p2.y) {
						this_stretch_y = Math.abs((double) parent_c.p2.y - parent_c.p1.y) / Math.abs((double) this_c.p2.y - this_c.p1.y);
					}
				}
			}

			double this_stretch_xy = bodypart.scale;

			if (bodypart.scale_params.containsKey("size_param")) {
				String real_bodypart_size = RaceBodypart.getCharacterParamByName(bodypart.scale_params.get("size_param"), character);
				if (real_bodypart_size != null && !real_bodypart_size.isEmpty()) {
					double real_bodypart_size_d = Double.parseDouble(real_bodypart_size);
					double calc_scale;
					if (bodypart.scale_params.containsKey("scale_of_zero") && bodypart.scale_params.containsKey("scale_of_hundred")) {
						double zero_scale = Math.max(Double.parseDouble(bodypart.scale_params.getOrDefault("scale_of_zero", "0")), 0.0001);
						double hundred_scale = Math.max(Double.parseDouble(bodypart.scale_params.getOrDefault("scale_of_hundred", "1")), 0.0001);
						calc_scale = zero_scale + (hundred_scale - zero_scale) * real_bodypart_size_d / 100.0;
					} else {
						real_bodypart_size_d *= base_height_for_scale_cm / character.getHeightValue();
						double base_scale_real_size_d = Math.max(Double.parseDouble(bodypart.scale_params.getOrDefault("real_size_base", "10")), 0.0001);
						calc_scale = Math.max(real_bodypart_size_d / base_scale_real_size_d, 0.0001);
					}
					switch(bodypart.scale_params.getOrDefault("scale_type", "scale")) {
						case "scale_x":
							this_stretch_x *= calc_scale;
							break;
						case "scale_y":
							this_stretch_y *= calc_scale;
							break;
						case "scale":
							this_stretch_xy *= calc_scale;
							break;
					}
				}
			}
			this_stretch_x *= this_stretch_xy * modifiers.getOrDefault("scale_x", Double.valueOf(1)) * modifiers.getOrDefault("scale", Double.valueOf(1));
			this_stretch_y *= this_stretch_xy * modifiers.getOrDefault("scale_y", Double.valueOf(1)) * modifiers.getOrDefault("scale", Double.valueOf(1));

			if (this_stretch_x < bodypart.min_scale) this_stretch_x = bodypart.min_scale;
			if (this_stretch_y < bodypart.min_scale) this_stretch_y = bodypart.min_scale;
			if (this_stretch_x > bodypart.max_scale) this_stretch_x = bodypart.max_scale;
			if (this_stretch_y > bodypart.max_scale) this_stretch_y = bodypart.max_scale;
			
			if ("y".equals(this_parent_position_params.getOrDefault("scale_alignment", ""))) {
				this_stretch_y = this_stretch_x;
			} else if ("x".equals(this_parent_position_params.getOrDefault("scale_alignment", ""))) {
				this_stretch_x = this_stretch_y;
			}
			
			if (bodypart.no_scale_by_parent) {
				this_stretch_x *= 1/parent_stretch_x;
				this_stretch_y *= 1/parent_stretch_y;
				if (parent.parent != null) {
					double parent2_stretch_x = (double) parent.parent.draw_width / (double) parent.parent.image_initial_width;
					double parent2_stretch_y = (double) parent.parent.draw_height / (double) parent.parent.image_initial_height;
					this_stretch_x *= parent2_stretch_x;
					this_stretch_y *= parent2_stretch_y;
				}
			}

			// scale calculation is now finished. now stretching points
			parent_c.stretchPoints(parent_stretch_x, parent_stretch_y);
			this_c.stretchPoints(this_stretch_x * parent_stretch_x, this_stretch_y * parent_stretch_y);

			// centers for result position calculation
			Point parent_c_center = parent_c.getCenter();
			Point this_c_center = this_c.getCenter();

			// calculating main results
			draw_x_point = parent.draw_x_point + parent_c_center.x - this_c_center.x;
			draw_y_point = parent.draw_y_point + parent_c_center.y - this_c_center.y;
			draw_width = image_initial_width * this_stretch_x * parent_stretch_x;
			draw_height = image_initial_height * this_stretch_y * parent_stretch_y;
			point_1_border = this_c.getPoint1();
			point_2_border = this_c.getPoint2();

			// calculationg transition points
			if (this_connection_params.containsKey("transition_point_1")) {
				Point tr_point_1 = Point.getPointFromString(this_connection_params.get("transition_point_1"), draw_width, draw_height, true, root.this_stretch_x * parent_stretch_x, root.this_stretch_y * parent_stretch_y);
				if (tr_point_1 != null) {
					tr_point_1.invertPoint(draw_inverse_x, draw_inverse_y, draw_width, draw_height);
					point_1_border = tr_point_1;
				}
			}
			if (this_connection_params.containsKey("transition_point_2")) {
				Point tr_point_2 = Point.getPointFromString(this_connection_params.get("transition_point_2"), draw_width, draw_height, true, root.this_stretch_x * parent_stretch_x, root.this_stretch_y * parent_stretch_y);
				if (tr_point_2 != null) {
					tr_point_2.invertPoint(draw_inverse_x, draw_inverse_y, draw_width, draw_height);
					point_2_border = tr_point_2;
				}
			}
			
			// final modifiers
			if (modifiers.containsKey("offset_x")) draw_x_point += modifiers.get("offset_x") * root.this_stretch_x;
			if (modifiers.containsKey("offset_y")) draw_y_point += modifiers.get("offset_y") * root.this_stretch_y;
			if (modifiers.containsKey("add_priority")) priority += modifiers.get("add_priority");

			rotation_angle = modifiers.getOrDefault("rotation", 0.0);

			// if this bodypart is part of dynamic set
			// we need to correct positions and other params according to output mode
			if (cols_count > 1 || rows_count > 1) {
				boolean is_secondary_item = (col_index > 0 || row_index > 0);
				String multiple_mode = this_parent_position_params.getOrDefault("multiple_output_mode", "");
				Point max_offset_point = Point.getPointFromString(this_parent_position_params.getOrDefault("multiple_max_distance", "0"), draw_width, draw_height, true, root.this_stretch_x, root.this_stretch_y);
				Point mid_offset_point = Point.getPointFromString(this_parent_position_params.getOrDefault("multiple_min_distance", null), draw_width, draw_height, true, root.this_stretch_x, root.this_stretch_y);
				Point min_offset_point = Point.getPointFromString(this_parent_position_params.getOrDefault("multiple_mid_distance", null), draw_width, draw_height, true, root.this_stretch_x, root.this_stretch_y);

				if (modifiers.containsKey("multiple_offset_scale_x")) {
					if(max_offset_point != null) max_offset_point.x *= modifiers.get("multiple_offset_scale_x");
					if(mid_offset_point != null) mid_offset_point.x *= modifiers.get("multiple_offset_scale_x");
					if(min_offset_point != null) min_offset_point.x *= modifiers.get("multiple_offset_scale_x");
				}
				if (modifiers.containsKey("multiple_offset_scale_y")) {
					if(max_offset_point != null) max_offset_point.y *= modifiers.get("multiple_offset_scale_y");
					if(mid_offset_point != null) mid_offset_point.y *= modifiers.get("multiple_offset_scale_y");
					if(min_offset_point != null) min_offset_point.y *= modifiers.get("multiple_offset_scale_y");
				}
				if (modifiers.containsKey("multiple_offset_scale")) {
					if(max_offset_point != null) max_offset_point.x *= modifiers.get("multiple_offset_scale");
					if(mid_offset_point != null) mid_offset_point.x *= modifiers.get("multiple_offset_scale");
					if(min_offset_point != null) min_offset_point.x *= modifiers.get("multiple_offset_scale");
					if(max_offset_point != null) max_offset_point.y *= modifiers.get("multiple_offset_scale");
					if(mid_offset_point != null) mid_offset_point.y *= modifiers.get("multiple_offset_scale");
					if(min_offset_point != null) min_offset_point.y *= modifiers.get("multiple_offset_scale");
				}
				if (modifiers.containsKey("multiple_offset_x")) {
					if(max_offset_point != null) max_offset_point.x += modifiers.get("multiple_offset_x");
					if(mid_offset_point != null) mid_offset_point.x += modifiers.get("multiple_offset_x");
					if(min_offset_point != null) min_offset_point.x += modifiers.get("multiple_offset_x");
				}
				if (modifiers.containsKey("multiple_offset_y")) {
					if(max_offset_point != null) max_offset_point.y += modifiers.get("multiple_offset_y");
					if(mid_offset_point != null) mid_offset_point.y += modifiers.get("multiple_offset_y");
					if(min_offset_point != null) min_offset_point.y += modifiers.get("multiple_offset_y");
				}
				if (mid_offset_point != null && min_offset_point != null) {
					Point.pointsSortCoord(min_offset_point, mid_offset_point, max_offset_point);
				}

				double multiple_base_offset_d, multiple_base_offset_d2;
				if (mid_offset_point != null) {
					multiple_base_offset_d = mid_offset_point.x;
					multiple_base_offset_d2 = mid_offset_point.y;
				} else {
					multiple_base_offset_d = max_offset_point.x;
					multiple_base_offset_d2 = max_offset_point.y;
				}

				int i_index = col_index*cols_count + row_index;
				int items_count = cols_count * rows_count;

				double multiple_mod_scale_x = 1.0, multiple_mod_scale_y = 1.0;
				double multiple_mod_dx = 0.0, multiple_mod_dy = 0.0;

				switch(multiple_mode) {
					case "polygon":
					case "polygon_center":
						double start_angle = 0.25 * Math.PI;
						if (items_count == 2) start_angle = -start_angle * 4;
						if (multiple_mode.equals("polygon_center")) {
							if (items_count == 3) start_angle = -0.5 * Math.PI;
							else if (items_count == 4) start_angle = -0.33 * Math.PI;
						}
						if (max_offset_point != null && mid_offset_point != null && min_offset_point != null) {
							double rx0 = draw_width/2;
							double midx = mid_offset_point.x;
							double minx = min_offset_point.x;
							double maxx = max_offset_point.x;
							double rcx;
							if (rx0 <= (midx - minx)/2) {
								rcx = midx;
							} else if (rx0 <= (maxx - minx)/2) {
								rcx = minx + rx0;
							} else {
								rcx = (maxx - minx)/2;
								multiple_mod_scale_x = Math.max(maxx - minx, 1.0) / (2 * rx0);
							}
							double ry0 = draw_height/2;
							double midy = mid_offset_point.y;
							double miny = min_offset_point.y;
							double maxy = max_offset_point.y;
							double rcy;
							if (ry0 <= (midy - miny)/2) {
								rcy = midy;
							} else if (ry0 <= (maxy - miny)/2) {
								rcy = miny + ry0;
							} else {
								rcy = (maxy - miny)/2;
								multiple_mod_scale_y = Math.max(maxy - miny, 1.0) / (2 * ry0);
							}
							multiple_base_offset_d = rcx;
							multiple_base_offset_d2 = rcy;
						}
						if (multiple_mode.equals("polygon")) {
							multiple_mod_dx = multiple_base_offset_d * Math.sin(start_angle + 2 * Math.PI * i_index / items_count);
							multiple_mod_dy = multiple_base_offset_d2 * Math.cos(start_angle + 2 * Math.PI * i_index / items_count);
						} else if (is_secondary_item) {
							multiple_mod_dx = multiple_base_offset_d * Math.sin(start_angle + 2 * Math.PI * (i_index - 1) / (items_count - 1));
							multiple_mod_dy = multiple_base_offset_d2 * Math.cos(start_angle + 2 * Math.PI * (i_index - 1) / (items_count - 1));
						}
						break;
					case "rotation":
						max_offset_point = Point.getPointFromString(this_parent_position_params.getOrDefault("multiple_max_distance", "0"), 180, 180, true);
						if (modifiers.containsKey("multiple_offset_x")) max_offset_point.x += modifiers.get("multiple_offset_x");
						if (modifiers.containsKey("multiple_offset_y")) max_offset_point.y += modifiers.get("multiple_offset_y");
						multiple_base_offset_d = max_offset_point.x;
						multiple_base_offset_d2 = max_offset_point.y;
						rotation_angle += multiple_base_offset_d2 + multiple_base_offset_d * (-1.0 + 2.0 * i_index / (items_count - 1));
						break;
					case "rows_and_cols":
						if(cols_count>1) multiple_mod_dx = (multiple_base_offset_d * col_index / (cols_count-1)) - multiple_base_offset_d*0.5;
						if(rows_count>1) multiple_mod_dy += (multiple_base_offset_d2 * row_index / (rows_count-1)) - multiple_base_offset_d2*0.5;
						priority += (double) (0.1 * row_index / rows_count) + (0.01 * col_index / cols_count);
						break;
					case "line":
						multiple_mod_dx = (multiple_base_offset_d * i_index / (items_count-1)) - multiple_base_offset_d*0.5;
						multiple_mod_dy = (multiple_base_offset_d2 * i_index / (items_count-1)) - multiple_base_offset_d2*0.5;
						priority += (double) (0.1 * i_index);
						break;
					case "y_offset_down":
						if (is_secondary_item) multiple_mod_dy = ((double) i_index) * multiple_base_offset_d / items_count;
						break;
					case "y_offset_up":
						if (is_secondary_item) multiple_mod_dy = -((double) i_index) * multiple_base_offset_d / items_count;
						break;
					case "y_offset_updown":
						if (is_secondary_item) multiple_mod_dy = (0.5 * multiple_base_offset_d * i_index / (items_count - 1)) * ((i_index % 2) == 0 ? -1 : 1);
						break;
				}
				Point offset_point = Point.getPointFromString(this_parent_position_params.getOrDefault("multiple_offset", null), draw_width, draw_height, true, root.this_stretch_x, root.this_stretch_y);
				if (offset_point != null) {
					multiple_mod_dx += offset_point.x;
					multiple_mod_dy += offset_point.y;
				}
				if (is_secondary_item) {
					Point sec_offset_point = Point.getPointFromString(this_parent_position_params.getOrDefault("multiple_secondary_offset", null), draw_width, draw_height, true, root.this_stretch_x, root.this_stretch_y);
					if (sec_offset_point != null) {
						multiple_mod_dx += sec_offset_point.x;
						multiple_mod_dy += sec_offset_point.y;
					}
					priority += Double.parseDouble(this_parent_position_params.getOrDefault("multiple_secondary_priority_change", "0"));
					priority += Double.parseDouble(this_parent_position_params.getOrDefault("multiple_secondary_priority_inc", "0")) * i_index;
					
					double scale_mul = Double.parseDouble(this_parent_position_params.getOrDefault("multiple_secondary_scale_mul", "1"));
					if (scale_mul != 1) {
						if (i_index>1) scale_mul = Math.pow(scale_mul, i_index);
						multiple_mod_scale_x *= scale_mul;
						multiple_mod_scale_y *= scale_mul;
					}
				}
				draw_x_point += multiple_mod_dx;
				draw_y_point += multiple_mod_dy;
				if (multiple_mod_scale_x != 1.0 || multiple_mod_scale_y != 1.0) {
					scaleCalculatedParams(multiple_mod_scale_x, multiple_mod_scale_y);
				}
			}
			
			if (rotation_angle != 0) {
				rotation_center = new Point((point_1_border.x + point_2_border.x)/2, (point_1_border.y + point_2_border.y)/2);
			}
		}

		if (!bodypart.is_hidden) {
			for (Map.Entry<String, CharacterBodypart> entry : child_parts.entrySet()) {
				entry.getValue().initDrawingParams(root, character);
			}
		}
	}

	private void scaleCalculatedParams(double sx, double sy) {
		double center_x = 0.5 * draw_width + draw_x_point;
		double center_y = 0.5 * draw_height + draw_y_point;
		draw_width = sx * draw_width;
		draw_height = sy * draw_height;
		draw_x_point = center_x - 0.5*draw_width;
		draw_y_point = center_y - 0.5*draw_height;

		point_1_border.x = point_1_border.x * sx;
		point_1_border.y = point_1_border.y * sy;
		point_2_border.x = point_2_border.x * sx;
		point_2_border.y = point_2_border.y * sy;
	}

	public void initRequiredMasks() {
		if (bodypart.is_hidden || is_hidden) return;
		if (parent != null) {
			Map<String, String> this_parent_position_params = parent.bodypart.positions.getOrDefault(xml_id, null);
			if (this_parent_position_params != null) {
				// "use_parent_alpha" mode requires parent mask
				if (this_parent_position_params.getOrDefault("draw_mode", "").equals("use_parent_alpha")) mask_target = parent;
				else if (this_parent_position_params.getOrDefault("draw_mode", "").equals("use_parent_parent_alpha") && parent != null) mask_target = parent.parent;
				else if (this_parent_position_params.getOrDefault("draw_mode", "").equals("use_parent_parent_parent_alpha") && parent != null && parent.parent != null) mask_target = parent.parent.parent;
				else if (this_parent_position_params.getOrDefault("draw_mode", "").equals("use_sibling_alpha") && parent != null) mask_target = parent.child_parts.getOrDefault(this_parent_position_params.getOrDefault("draw_mode_target_id", ""), null);
				if ((mask_target != null) && !mask_target.initCurrentMask()) mask_target = null;
			}
		}
		if (image_color_mask == null && !bodypart.is_hidden && bodypart.images.hasImage("color")) {
			initColorMask();
		}
	}

	private boolean initColorMask() {
		if (image_color_mask == null && !bodypart.is_hidden && !is_hidden) {
			if (bodypart.images.hasImage("color")) {
				image_color_mask = bodypart.images.getImage("color", (int) Math.round(draw_width), (int) Math.round(draw_height));
			}
		}
		return image_color_mask != null;
	}

	private boolean initCurrentMask() {
		if (image_mask == null && !bodypart.is_hidden && !is_hidden) {
			if (bodypart.images.hasImage("mask")) {
				image_mask = bodypart.images.getImage("mask", (int) Math.round(draw_width), (int) Math.round(draw_height));
			} else {
				image_mask = image.generateMask((int) Math.round(draw_width), (int) Math.round(draw_height));
			}
			if (image_mask != null && "combine_with_parent_mask".equals(bodypart.mask_calculation_mode)) {
				if (parent.image_mask == null) {
					parent.initCurrentMask();
				}
				if (parent.image_mask != null) {
					image_mask.combineWithMask(parent.image_mask, (int) Math.round(draw_x_point - parent.draw_x_point), (int) Math.round(draw_y_point - parent.draw_y_point));
				}
			}
		}
		return image_mask != null;
	}

	@Override
	public String toString() {
		return "{"+
			"id:" + id + 
			" xml_id:" + xml_id + 
			" type_path:" + type_path + 
			" id_path:" + id_path + 
			" bodypart:" + bodypart.toString() + 
			" x:" + draw_x_point + 
			" y:" + draw_y_point + 
			" w:" + draw_width + 
			" h:" + draw_height + 
			" inv_x:" + (draw_inverse_x?"1":"0") + 
			" inv_y:" + (draw_inverse_y?"1":"0") + 
			" priority:" + priority + 
			" child:" + (child_parts!=null ? child_parts.toString() : "null") + 
			"}";
	}
}
