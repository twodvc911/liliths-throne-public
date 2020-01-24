package com.lilithsthrone.rendering.chargen;

import java.awt.geom.AffineTransform;

/**
 *
 * @author twovgSP
 */
public class Point {
	public double x;
	public double y;
	
	public Point(double px, double py) {
		x = px;
		y = py;
	}

	public void invertPoint(boolean invert_x, boolean invert_y, double width, double height) {
		if (invert_x) {
			x = width - x;
		}
		if (invert_y) {
			y = height - y;
		}
	}

	public void stretchPoint(double scale_x, double scale_y) {
		x = x * scale_x;
		y = y * scale_y;
	}

	public java.awt.Point toIntPoint() {
		return new java.awt.Point((int) Math.round(x), (int) Math.round(y));
	}
	
	public static Point getPointFromString(String str_point) {
		return getPointFromString(str_point, 0);
	}
	public static Point getPointFromString(String str_point, double bodypart_size) {
		return getPointFromString(str_point, bodypart_size, bodypart_size, false);
	}
	public static Point getPointFromString(String str_point, double bodypart_width, double bodypart_height, boolean allow_one_value) {
		return getPointFromString(str_point, bodypart_width, bodypart_height, allow_one_value, 1.0, 1.0);
	}
	public static Point getPointFromString(String str_point, double bodypart_width, double bodypart_height, boolean allow_one_value, double scale_x, double scale_y) {
		if (str_point != null) {
			String[] str_point_parts = str_point.split(";");
			if (str_point_parts.length > 1) {
				double dx, dy;
				if (str_point_parts[0].contains("%")) {
					dx = 0.01 * Double.parseDouble(str_point_parts[0].replace("%", "")) * bodypart_width;
				} else {
					dx = scale_x * Double.parseDouble(str_point_parts[0]);
				}
				if (str_point_parts[1].contains("%")) {
					dy = 0.01 * Double.parseDouble(str_point_parts[1].replace("%", "")) * bodypart_height;
				} else {
					dy = scale_y * Double.parseDouble(str_point_parts[1]);
				}
				return new Point(dx, dy);
			} else if (allow_one_value && (str_point_parts.length == 1)) {
				double dx;
				if (str_point_parts[0].contains("%")) {
					dx = 0.01 * Double.parseDouble(str_point_parts[0].replace("%", "")) * Math.max(bodypart_width, bodypart_height);
					return new Point(dx, dx);
				} else {
					dx = Double.parseDouble(str_point_parts[0]);
				}
				return new Point(scale_x * dx, scale_y * dx);
			}
		}
		return null;
	}

	public static void pointsSortCoord(Point p_min, Point p_mid, Point p_max) {
		double tmp;
		if (p_min.x > p_mid.x) {tmp = p_min.x; p_min.x = p_mid.x; p_mid.x = tmp;}
		if (p_min.x > p_max.x) {tmp = p_min.x; p_min.x = p_max.x; p_max.x = tmp;}
		if (p_mid.x > p_max.x) {tmp = p_mid.x; p_mid.x = p_max.x; p_max.x = tmp;}
		if (p_min.y > p_mid.y) {tmp = p_min.y; p_min.y = p_mid.y; p_mid.y = tmp;}
		if (p_min.y > p_max.y) {tmp = p_min.y; p_min.y = p_max.y; p_max.y = tmp;}
		if (p_mid.y > p_max.y) {tmp = p_mid.y; p_mid.y = p_max.y; p_max.y = tmp;}
	}

	public void rotatePoint(Point center, double angle) {
		double[] pt = {x, y};
		AffineTransform.getRotateInstance(Math.toRadians(angle), center.x, center.y).transform(pt, 0, pt, 0, 1);
		x = pt[0];
		y = pt[1];
	}
	
	@Override
	public String toString() {
		return "Point{x: "+x+"; y:"+y+"}";
	}
}
