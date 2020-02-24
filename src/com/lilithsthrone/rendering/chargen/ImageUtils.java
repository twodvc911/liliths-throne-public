package com.lilithsthrone.rendering.chargen;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;
import javafx.scene.paint.Color;

/**
 *
 * @author twovgSP
 */
public class ImageUtils {

	public static BufferedImage getNewEmptyBufferedImage(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[image.getWidth()*image.getHeight()];
		Arrays.fill(pixels, 0);
		image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		return image;
	}
	public static BufferedImage getResizedImage(BufferedImage image, int image_new_width, int image_new_height, boolean stretch_image) {
		return getResizedImage(image, image_new_width, image_new_height, stretch_image, true);
	}
	public static BufferedImage getResizedImage(BufferedImage image, int image_new_width, int image_new_height, boolean stretch_image, boolean center_image) {
		BufferedImage new_image = getNewEmptyBufferedImage(image_new_width, image_new_height);
		Graphics2D canvas = new_image.createGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (stretch_image) {
			canvas.drawImage(image, 0, 0, image_new_width, image_new_height, null);
		} else {
			if (center_image) {
				canvas.drawImage(image, (image_new_width - image.getWidth()) / 2, (image_new_height - image.getHeight()) / 2, image.getWidth(), image.getHeight(), null);
			} else {
				canvas.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
			}
		}
		canvas.dispose();
		return new_image;
	}

	public static int[] getImageRGBArray(BufferedImage buff_image) {
		int w = buff_image.getWidth();
		int h = buff_image.getHeight();
		int[] rgbaArray = new int[ w*h ];
		buff_image.getRGB(0, 0, w, h, rgbaArray, 0, w);
		return rgbaArray;
	}

	public static void replaceColors(BufferedImage buff_image, Map<Color, Color> replaces) {
		int replaces_cnt = replaces.size();
		int[] from_replaces = new int[replaces_cnt];
		int[] to_replaces = new int[replaces_cnt];
		int k = 0;
		for (Map.Entry<Color, Color> replace : replaces.entrySet()) {
			int color_from	= (((int) Math.floor(replace.getKey().getRed() * 255))<<16) | (((int) Math.floor(replace.getKey().getGreen() * 255))<<8) | ((int) Math.floor(replace.getKey().getBlue() * 255));
			int color_to	= (((int) Math.floor(replace.getValue().getRed() * 255))<<16) | (((int) Math.floor(replace.getValue().getGreen() * 255))<<8) | ((int) Math.floor(replace.getValue().getBlue() * 255));
			from_replaces[k] = color_from;
			to_replaces[k] = color_to;
			k++;
		}
		int w = buff_image.getWidth();
		int h = buff_image.getHeight();
		int[] rgbaArray = new int[ w*h ];
		buff_image.getRGB(0, 0, w, h, rgbaArray, 0, w);
		int p, a;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				p = rgbaArray[x + y*w];
				a = (p>>24) & 0xff;
				if (a > 0) {
					p = p & 0xffffff;
					for(int i=0; i<replaces_cnt; i++) {
						if (p == from_replaces[i]) {
							rgbaArray[x + y*w] = (a<<24) | to_replaces[i];
							break;
						}
					}
				}
			}
		}
		buff_image.setRGB(0, 0, w, h, rgbaArray, 0, w);
	}
	
	public static void colorOverlay(BufferedImage buff_image, Color color) {
		int mr = (int) Math.floor(color.getRed() * 255);
		int mg = (int) Math.floor(color.getGreen() * 255);
		int mb = (int) Math.floor(color.getBlue() * 255);
		int w = buff_image.getWidth();
		int h = buff_image.getHeight();
		int[] rgbaArray = new int[ w*h ];
		buff_image.getRGB(0, 0, w, h, rgbaArray, 0, w);
		int p, a;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				p = rgbaArray[x + y*w];
				a = (p>>24) & 0xff;
				p = (a<<24) | (mr<<16) | (mg<<8) | mb;
				rgbaArray[x + y*w] = p;
			}
		}
		buff_image.setRGB(0, 0, w, h, rgbaArray, 0, w);
	}

	public static BufferedImage pixelate(BufferedImage imageToPixelate, int pixelSize) {
		BufferedImage pixelateImage = getNewEmptyBufferedImage(imageToPixelate.getWidth(), imageToPixelate.getHeight());
		for (int y = 0; y < imageToPixelate.getHeight(); y += pixelSize) {
			for (int x = 0; x < imageToPixelate.getWidth(); x += pixelSize) {
				BufferedImage croppedImage = getCroppedImage(imageToPixelate, x, y, pixelSize, pixelSize);
				int dominantColor = getDominantColor(croppedImage);
				for (int yd = y; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()); yd++) {
					for (int xd = x; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()); xd++) {
						pixelateImage.setRGB(xd, yd, dominantColor);
					}
				}
			}
		}
		return pixelateImage;
	}
	private static BufferedImage getCroppedImage(BufferedImage image, int startx, int starty, int width, int height) {
	    if (startx < 0) startx = 0;
	    if (starty < 0) starty = 0;
	    if (startx > image.getWidth()) startx = image.getWidth();
	    if (starty > image.getHeight()) starty = image.getHeight();
	    if (startx + width > image.getWidth()) width = image.getWidth() - startx;
	    if (starty + height > image.getHeight()) height = image.getHeight() - starty;
	    return image.getSubimage(startx, starty, width, height);
	}
	public static int getDominantColor(BufferedImage bitmap) {
		if (null == bitmap) return 0;
		int redBucket = 0;
		int greenBucket = 0;
		int blueBucket = 0;
		int alphaBucket = 0;
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int pixelCount = w * h;
		int[] pixels = new int[pixelCount];
		bitmap.getRGB(0, 0, w, h, pixels, 0, w);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int color = pixels[x + y * w];
				redBucket += (color >> 16) & 0xFF;
				greenBucket += (color >> 8) & 0xFF;
				blueBucket += (color & 0xFF);
				alphaBucket += (color >>> 24);
			}
		}
		return intColor(
			alphaBucket / pixelCount,
			redBucket / pixelCount,
			greenBucket / pixelCount,
			blueBucket / pixelCount
		);
	}

	public static int intColor(int a, int r, int g, int b) {
		return (a<<24) | (r<<16) | (g<<8) | b;
	}

	public static BufferedImage getColorizedImage(BufferedImage buff_image, int image_new_width, int image_new_height, BodyPartColoringInfo coloring, Color primary_tmpl, Color secondary_tmpl) {
		BufferedImage new_image = ImageUtils.getResizedImage(buff_image, image_new_width, image_new_height, true);
		
		double max_color_distance = Math.max(GradientParams.getColorsAvgSquareDistance(primary_tmpl, secondary_tmpl), GradientParams.getColorsAvgSquareDistance(primary_tmpl, Color.WHITE));
		max_color_distance = Math.max(GradientParams.getColorsAvgSquareDistance(primary_tmpl, Color.BLACK), max_color_distance);
		max_color_distance = Math.max(GradientParams.getColorsAvgSquareDistance(secondary_tmpl, Color.WHITE), max_color_distance);
		max_color_distance = Math.max(GradientParams.getColorsAvgSquareDistance(secondary_tmpl, Color.BLACK), max_color_distance);
		
		int rt1 = (int) Math.floor(primary_tmpl.getRed() * 255);
		int gt1 = (int) Math.floor(primary_tmpl.getGreen() * 255);
		int bt1 = (int) Math.floor(primary_tmpl.getBlue() * 255);

		int rt2 = (int) Math.floor(secondary_tmpl.getRed() * 255);
		int gt2 = (int) Math.floor(secondary_tmpl.getGreen() * 255);
		int bt2 = (int) Math.floor(secondary_tmpl.getBlue() * 255);
		
		int rc1 = (int) Math.floor(coloring.primary_color.getRed() * 255);
		int gc1 = (int) Math.floor(coloring.primary_color.getGreen() * 255);
		int bc1 = (int) Math.floor(coloring.primary_color.getBlue() * 255);

		int rc2 = (int) Math.floor(coloring.secondary_color.getRed() * 255);
		int gc2 = (int) Math.floor(coloring.secondary_color.getGreen() * 255);
		int bc2 = (int) Math.floor(coloring.secondary_color.getBlue() * 255);
		
		int w = new_image.getWidth();
		int h = new_image.getHeight();
		int[] rgbaArray = new int[ w*h ];
		new_image.getRGB(0, 0, w, h, rgbaArray, 0, w);
		for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					double rNew = 0, gNew = 0, bNew = 0, aNew = 0;
					int p, r, g, b, a;
					p = rgbaArray[x + y*w];
					r = (p>>16) & 0xff;
					g = (p>>8) & 0xff;
					b = p & 0xff;
					a = (p>>24) & 0xff;
					
					double k1 = ((r - rt1)*(r - rt1) + (g - gt1)*(g - gt1) + (b - bt1)*(b - bt1)) / 3;
					if (k1 < max_color_distance) k1 = 1.0 - (k1 / max_color_distance);
					else k1 = 0;
					
					double k2 = ((r - rt2)*(r - rt2) + (g - gt2)*(g - gt2) + (b - bt2)*(b - bt2)) / 3;
					if (k2 < max_color_distance) k2 = 1.0 - (k2 / max_color_distance);
					else k2 = 0;
					
					if (k1 > 0 || k2 > 0) {
						aNew = a;
						rNew = (rc1 * k1 + rc2 * k2) / (k1 + k2);
						gNew = (gc1 * k1 + gc2 * k2) / (k1 + k2);
						bNew = (bc1 * k1 + bc2 * k2) / (k1 + k2);
					}
					
					r = (int)Math.round(Math.min(255, Math.abs(rNew)));
					g = (int)Math.round(Math.min(255, Math.abs(gNew)));
					b = (int)Math.round(Math.min(255, Math.abs(bNew)));
					a = (int)Math.round(Math.min(255, Math.abs(aNew)));
					p = (a<<24) | (r<<16) | (g<<8) | b;
					new_image.setRGB(x, y, p);
				}
		}

		return new_image;
	}

	public static BufferedImage generateMask(
		BufferedImage image, 
		int mask_width, 
		int mask_height, 
		Color mask_color, 
		int brightness_threshold, 
		int brightness_grad_pow,
		boolean with_resize
	) {
		BufferedImage new_image = ImageUtils.getResizedImage(image, mask_width, mask_height, with_resize);

		int mr = (int) Math.floor(mask_color.getRed() * 255);
		int mg = (int) Math.floor(mask_color.getGreen() * 255);
		int mb = (int) Math.floor(mask_color.getBlue() * 255);

		int[] rgbaArray = new int[ mask_width ];
		for(int y = 0; y < mask_height; y++) {
			new_image.getRGB(0, y, mask_width, 1, rgbaArray, 0, mask_width);
			for(int x = 0; x < mask_width; x++) {
				int p = rgbaArray[x];
				int a = (p>>24) & 0xff;
				if (a > 0) {
					int brightness = (((p>>16) & 0xff) + ((p>>8) & 0xff) + (p & 0xff)) / 3;
					if (brightness < brightness_threshold) a = a - (brightness_threshold - brightness)*brightness_grad_pow;
					if (a < 0) a = 0; else if (a > 255) a = 255;
					rgbaArray[x] = (a<<24) | (mr << 16) | (mg << 8) | mb;
				} else {
					rgbaArray[x] = 0;
				}
			}
			new_image.setRGB(0, y, mask_width, 1, rgbaArray, 0, mask_width);
		}
		return new_image;
	}
	public static void combineImageWithImageMask(BufferedImage buff_image, BufferedImage image_mask, boolean combine_min, int mask_offset_x, int mask_offset_y) {
		int w = buff_image.getWidth();
		int h = buff_image.getHeight();
		int w_src = image_mask.getWidth();
		int h_src = image_mask.getHeight();
		int[] rgbaArray = new int[ w ];
		int[] rgbaArraySrc = new int[ w_src ];
		boolean src_array_filled = true;
		for(int y = 0; y < h; y++) {
			buff_image.getRGB(0, y, w, 1, rgbaArray, 0, w);
			int y_src = y + mask_offset_y;
			if (y_src >= 0 && y_src < h_src) {
				image_mask.getRGB(0, y_src, w_src, 1, rgbaArraySrc, 0, w_src);
				src_array_filled = true;
			} else {
				src_array_filled = false;
			}
			for(int x = 0; x < w; x++) {
				int p = rgbaArray[x];
				int a = (p>>24) & 0xff;
				if (src_array_filled) {
					int x_src = x + mask_offset_x;
					if (x_src >= 0 && x_src < w_src) {
						int p_src = rgbaArraySrc[x_src];
						int a_src = (p_src>>24) & 0xff;
						if (combine_min) {
							if (a_src < a) a = a_src;
						} else {
							if (a_src > a) a = a_src;
						}
					} else {
						a = 0;
					}
				} else {
					a = 0;
				}
				rgbaArray[x] = (a<<24);
			}
			buff_image.setRGB(0, y, w, 1, rgbaArray, 0, w);
		}
	}
}
