package com.lilithsthrone.rendering.chargen;

import java.util.Map;

/**
 *
 * @author twovgSP
 */
public class ConnectionPoints {
	public Point p1 = null;
	public Point p2 = null;

	public static ConnectionPoints getConnectionPointsFromParams(Map<String, String> this_parent_position_params, double width, double height) {
		if (this_parent_position_params != null) {
			String point = this_parent_position_params.getOrDefault("point", null);
			String point_1 = this_parent_position_params.getOrDefault("point_1", null);
			String point_2 = this_parent_position_params.getOrDefault("point_2", null);
			if (point != null && point_1 == null) {
				point_1 = point;
			}
			if (point_1 != null && point_2 == null) {
				point_2 = point_1;
			}
			if (point_2 != null && point_1 == null) {
				point_1 = point_2;
			}
			if (point_1 == null || point_2 == null || !point_1.contains(";") || !point_2.contains(";")) {
				return null;
			}
			ConnectionPoints tp = new ConnectionPoints();
			tp.p1 = Point.getPointFromString(point_1, width, height, true);
			tp.p2 = Point.getPointFromString(point_2, width, height, true);
			return tp;
		}
		return null;
	}

	public void invertPoints(boolean invert_x, boolean invert_y, double width, double height) {
		if (invert_x) {
			p1.x = width - p1.x;
			p2.x = width - p2.x;
			if (p1.x > p2.x) {
				double tmp = p2.x;
				p2.x = p1.x;
				p1.x = tmp;
			}
		}
		if (invert_y) {
			p1.y = height - p1.y;
			p2.y = height - p2.y;
			if (p1.y > p2.y) {
				double tmp = p2.y;
				p2.y = p1.y;
				p1.y = tmp;
			}
		}
	}
	
	public void stretchPoints(double scale_x, double scale_y) {
		p1.stretchPoint(scale_x, scale_y);
		p2.stretchPoint(scale_x, scale_y);
	}
	public void stretchPointsCenter(double scale_x, double scale_y) {
		Point center = getCenter();
		double cp1_x = p1.x - center.x;
		double cp1_y = p1.y - center.y;
		p1.x = center.x + cp1_x * scale_x;
		p2.x = center.x - cp1_x * scale_x;
		p1.y = center.y + cp1_y * scale_y;
		p2.y = center.y - cp1_y * scale_y;
	}
	
	public Point getCenter() {
		return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
	}
	
	public Point getPoint1() {
		return p1;
	}
	
	public Point getPoint2() {
		return p2;
	}
}
