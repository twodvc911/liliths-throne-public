package com.lilithsthrone.rendering.chargen;

import javafx.scene.paint.Color;

/**
 *
 * @author twovgSP
 */
public class GradientParams {
	public Color gradient_color;
	public Point transition_border1;
	public Point transition_border2;
	public double part_transition_percent;
	public boolean is_inverted;
	
	public static double getColorsAvgSquareDistance(Color col1, Color col2) {
		int rt1 = (int) Math.floor(col1.getRed() * 255);
		int gt1 = (int) Math.floor(col1.getGreen() * 255);
		int bt1 = (int) Math.floor(col1.getBlue() * 255);
		int rt2 = (int) Math.floor(col2.getRed() * 255);
		int gt2 = (int) Math.floor(col2.getGreen() * 255);
		int bt2 = (int) Math.floor(col2.getBlue() * 255);
		return ((rt2 - rt1)*(rt2 - rt1) + (gt2 - gt1)*(gt2 - gt1) + (bt2 - bt1)*(bt2 - bt1)) / 3;
	}
	
	public static Color mixColors(Color col1, Color col2, double k) {
		return Color.color(
			col1.getRed() * (1-k) + col2.getRed() * k, 
			col1.getGreen() * (1-k) + col2.getGreen() * k, 
			col1.getBlue() * (1-k) + col2.getBlue() * k
		);
	}
	
	public static double pDistance(double x, double y, double x1, double y1, double x2, double y2) {
		double A = x - x1;
		double B = y - y1;
		double C = x2 - x1;
		double D = y2 - y1;
		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = -1;
		if (len_sq != 0) param = dot / len_sq;
		double xx, yy;
		if (param < 0) {
			xx = x1;
			yy = y1;
		} else if (param > 1) {
			xx = x2;
			yy = y2;
		} else {
			xx = x1 + param * C;
			yy = y1 + param * D;
		}
		double dx = x - xx;
		double dy = y - yy;
		return Math.hypot(dx, dy);
	}
	
	public double getTransitionDistance(double x, double y) {
		return pDistance(x, y, transition_border1.x, transition_border1.y, transition_border2.x, transition_border2.y);
	}
}
