package com.lilithsthrone.rendering.chargen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author twovgSP
 */
public class BodyPartDrawOrderList {
	private final List<CharacterBodypart> items = new ArrayList<>();
	public double full_image_width;
	public double full_image_height;

	private class MinMaxResults {
		public double min_x = 0;
		public double min_y = 0;
		public double max_x = 0;
		public double max_y = 0;
	}

	private MinMaxResults getMinMax() {
		MinMaxResults results = new MinMaxResults();
		boolean minmax_init = false;

		for(CharacterBodypart item : items) {
			if (minmax_init) {
				if (results.min_x > item.draw_x_point) results.min_x = item.draw_x_point;
				if (results.min_y > item.draw_y_point) results.min_y = item.draw_y_point;
				if (results.max_x < (item.draw_x_point + item.draw_width)) results.max_x = item.draw_x_point + item.draw_width;
				if (results.max_y < (item.draw_y_point + item.draw_height)) results.max_y = item.draw_y_point + item.draw_height;
			} else {
				results.min_x = item.draw_x_point;
				results.min_y = item.draw_y_point;
				results.max_x = item.draw_x_point + item.draw_width;
				results.max_y = item.draw_y_point + item.draw_height;
				minmax_init = true;
			}
		}
		items.forEach((item) -> {
			if (item.rotation_center != null) {
				double this_draw_x_point = item.draw_x_point;
				double this_draw_y_point = item.draw_y_point;
				double this_draw_width = item.draw_width;
				double this_draw_height = item.draw_height;
				Point[] points = {
					new Point(this_draw_x_point, this_draw_y_point),
					new Point(this_draw_x_point + this_draw_width, this_draw_y_point),
					new Point(this_draw_x_point + this_draw_width, this_draw_y_point + this_draw_height),
					new Point(this_draw_x_point, this_draw_y_point + this_draw_height)
				};
				Point this_center = new Point(item.rotation_center.x + this_draw_x_point, item.rotation_center.y + this_draw_y_point);
				double pt_min_x = 0;
				double pt_min_y = 0;
				double pt_max_x = 0;
				double pt_max_y = 0;
				boolean pt_minmax_init = false;
				for(int i=0; i<points.length; i++) {
					points[i].rotatePoint(this_center, item.rotation_angle);
					if (pt_minmax_init) {
						if (pt_min_x > points[i].x) pt_min_x = points[i].x;
						if (pt_min_y > points[i].y) pt_min_y = points[i].y;
						if (pt_max_x < points[i].x) pt_max_x = points[i].x;
						if (pt_max_y < points[i].y) pt_max_y = points[i].y;
					} else {
						pt_min_x = pt_max_x = points[i].x;
						pt_min_y = pt_max_y = points[i].y;
						pt_minmax_init = true;
					}
				}
				this_draw_x_point = pt_min_x;
				this_draw_y_point = pt_min_y;
				this_draw_width = pt_max_x - pt_min_x;
				this_draw_height = pt_max_y - pt_min_y;
				if (results.min_x > this_draw_x_point) results.min_x = this_draw_x_point;
				if (results.min_y > this_draw_y_point) results.min_y = this_draw_y_point;
				if (results.max_x < (this_draw_x_point + this_draw_width)) results.max_x = this_draw_x_point + this_draw_width;
				if (results.max_y < (this_draw_y_point + this_draw_height)) results.max_y = this_draw_y_point + this_draw_height;
			}
		});
		return results;
	}

	public void normalizePositions() {
		MinMaxResults minmax = getMinMax();
		full_image_width = minmax.max_x - minmax.min_x;
		full_image_height = minmax.max_y - minmax.min_y;
		items.forEach((item) -> {
			item.draw_x_point -= minmax.min_x;
			item.draw_y_point -= minmax.min_y;
		});
		sortItems();
	}

	private static boolean patternMatches(String pattern, String str) {
		if (pattern.length() == 0 && str.length() == 0) 
			return true; 
		if (pattern.length() > 1 && pattern.charAt(0) == '*' && str.length() == 0) 
			return false; 
		if (
			(pattern.length() > 1 && pattern.charAt(0) == '?') || 
			(pattern.length() != 0 && str.length() != 0 && pattern.charAt(0) == str.charAt(0))
		) 
			return patternMatches(pattern.substring(1), str.substring(1)); 
		if (pattern.length() > 0 && pattern.charAt(0) == '*') 
			return patternMatches(pattern.substring(1), str) || patternMatches(pattern, str.substring(1)); 
		return false;
	}

	public void prepareForDrawing() {
		Set<String> set_of_hidden_types = new HashSet<>();
		Set<String> set_of_hidden_ids = new HashSet<>();
		Map<RaceBodypart, Integer> max_cnt_map = new HashMap<>();

		// init sets of hidden ids and types, init max counts
		items.forEach((item) -> {
			if (item.bodypart.is_hidden) return;
			set_of_hidden_types.addAll(item.bodypart.hides_bodyparts_by_type);
			set_of_hidden_ids.addAll(item.bodypart.hides_bodyparts_by_id);
			if (item.bodypart.max_count != null && !max_cnt_map.containsKey(item.bodypart)) {
				max_cnt_map.put(item.bodypart, item.bodypart.max_count);
			}
		});
		// hide by types
		set_of_hidden_types.forEach((String hidden_type) -> {
			for(CharacterBodypart item : items) {
				if (!item.is_hidden && (item.bodypart.bodypart_name.equals(hidden_type) || patternMatches(hidden_type, item.type_path))) {
					item.is_hidden = true;
				}
			}
		});
		// hide by ids
		set_of_hidden_ids.forEach((String hidden_id) -> {
			for(CharacterBodypart item : items) {
				if (!item.is_hidden && (item.id.equals(hidden_id) || patternMatches(hidden_id, item.id_path))) {
					item.is_hidden = true;
				}
			}
		});
		// hide by max counts, init masks
		items.stream().filter((item) -> (!item.is_hidden)).forEachOrdered((CharacterBodypart item) -> {
			if (!item.is_hidden && max_cnt_map.containsKey(item.bodypart)) {
				if (max_cnt_map.get(item.bodypart) > 0) {
					max_cnt_map.put(item.bodypart, max_cnt_map.get(item.bodypart) - 1);
				} else {
					item.is_hidden = true;
				}
			}
			item.initRequiredMasks();
		});
		// image & mask flips
		items.forEach((item) -> {
			if (item.is_hidden) return;
			item.image.flipImage(item.draw_inverse_x, item.draw_inverse_y);
			if (item.image_mask != null) item.image_mask.flipImage(item.draw_inverse_x, item.draw_inverse_y);
			if (item.image_color_mask != null) item.image_color_mask.flipImage(item.draw_inverse_x, item.draw_inverse_y);
		});
	}

	public boolean add(CharacterBodypart new_bp) {
		return items.add(new_bp);
	}

	public void addAll(BodyPartDrawOrderList other_list) {
		other_list.getItems().forEach((child_item) -> {
			items.add(child_item);
		});
	}

	private void sortItems() {
		Collections.sort(items, (d1, d2) -> {
			return Double.compare(d1.priority, d2.priority);
		});
	}

	public List<CharacterBodypart> getItems() {
		return items;
	}
}
