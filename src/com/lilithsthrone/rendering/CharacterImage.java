package com.lilithsthrone.rendering;

import com.lilithsthrone.rendering.chargen.BodyPartColoringInfo;
import com.lilithsthrone.rendering.chargen.GradientParams;
import com.lilithsthrone.rendering.chargen.Point;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;

/**
 *
 * @author twovgSP
 */
public class CharacterImage extends CachedImage {
	
	private final boolean debug_mode = false;
	private BufferedImage buff_image = null;
	//private BufferedImage glow_image = null;

	public static CharacterImage fromImage(BufferedImage image) {
		CharacterImage new_ci = new CharacterImage();
		new_ci.load(image);
		return new_ci;
	}

	public boolean load(String filename) {
		return load(new File(filename));
	}

	@Override
	public boolean load(File f) {
		try {
			return load(ImageIO.read(f));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean load(BufferedImage image) {
		updatePercentageWidth(image);
		width = image.getWidth();
		height = image.getHeight();
		return true;
        }

	public boolean save(String filename) {
		try {
			ImageIO.write(buff_image, "PNG", new File(filename));
		} catch (IOException ex) {
			return false;
		}
		return true;
	}
	
	public boolean updateImageString() {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
			ImageIO.setUseCache(false);
			ImageIO.write(buff_image, "PNG", byteStream);
			imageString = "data:image/png;base64," + Base64.getEncoder().encodeToString(byteStream.toByteArray());
			if (debug_mode) save("res/images/chargen/debug/chargen-debug.png");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static BufferedImage getBufferedImageForSize(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[image.getWidth()*image.getHeight()];
		Arrays.fill(pixels, 0);
		image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		return image;
	}

	public static BufferedImage getResizedImage(BufferedImage image, int image_new_width, int image_new_height, boolean stretch_image) {
		BufferedImage new_image = getBufferedImageForSize(image_new_width, image_new_height);
		Graphics2D canvas = new_image.createGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (stretch_image) {
			canvas.drawImage(image, 0, 0, image_new_width, image_new_height, null);
		} else {
			canvas.drawImage(image, (image_new_width - image.getWidth()) / 2, (image_new_height - image.getHeight()) / 2, image.getWidth(), image.getHeight(), null);
		}
		canvas.dispose();
		return new_image;
	}
	
	public void initForSize(int width, int height) {
		updatePercentageWidth(getBufferedImageForSize(width, height));
	}
	
	public BufferedImage getImage() {
		return buff_image;
	}
	
	@Override
	public void updatePercentageWidth(BufferedImage image) {
		super.updatePercentageWidth(image);
		buff_image = image;
	}

	public CharacterImage getResizedCharacterImage(int image_new_width, int image_new_height, boolean stretch_image) {
		return CharacterImage.fromImage(getResizedImage(buff_image, image_new_width, image_new_height, stretch_image));
	}

	public CharacterImage getColorizedImage(int image_new_width, int image_new_height, BodyPartColoringInfo coloring, Color primary_tmpl, Color secondary_tmpl) {
		BufferedImage new_image = getResizedImage(buff_image, image_new_width, image_new_height, true);
		
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

		return CharacterImage.fromImage(new_image);
	}

	public int[] getImageRGBArray() {
		int w = buff_image.getWidth();
		int h = buff_image.getHeight();
		int[] rgbaArray = new int[ w*h ];
		buff_image.getRGB(0, 0, w, h, rgbaArray, 0, w);
		return rgbaArray;
	}

	private void applyBlurFilterToImage(int count) {
		int w = buff_image.getWidth();
		int h = buff_image.getHeight();
		int[] rgbaArray = new int[ w*h ];
		buff_image.getRGB(0, 0, w, h, rgbaArray, 0, w);
		double v = 1.0/9.0;
		double[] filterArray = new double[] { v,v,v, v,v,v, v,v,v };
		for (int c = 0; c < count; c++) {
			for (int x = 1; x < w-1; x++) {
				for (int y = 1; y < h-1; y++) {
					double rNew = 0, gNew = 0, bNew = 0, aNew = 0;
					int k = 0;
					int p, r, g, b, a;
					for (int j = y-1; j <= y+1; j++) {
						for (int i = x-1; i <= x+1; i++) {
							p = rgbaArray[i + j*w];
							r = (p>>16) & 0xff;
							g = (p>>8) & 0xff;
							b = p & 0xff;
							a = (p>>24) & 0xff;
							if (a == 0) r = b = g = 255;
							rNew += r*filterArray[k];
							gNew += g*filterArray[k];
							bNew += b*filterArray[k];
							aNew += a*filterArray[k];
							k++;
						}
					}
					r = (int)Math.round(Math.min(255, Math.abs(rNew)));
					g = (int)Math.round(Math.min(255, Math.abs(gNew)));
					b = (int)Math.round(Math.min(255, Math.abs(bNew)));
					a = (int)Math.round(Math.min(255, Math.abs(aNew)));
					p = (a<<24) | (r<<16) | (g<<8) | b;
					rgbaArray[x + y*w] = p;
				}
			}
		}
		buff_image.setRGB(0, 0, w, h, rgbaArray, 0, w);
	}

	public void color_set(Color color) {
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

	public static CharacterImage loadMask(String file_name, int image_new_width, int image_new_height) throws IOException {
		BufferedImage original = ImageIO.read(new File(file_name));
		if (original.getWidth() != image_new_width || original.getHeight() != image_new_height) {
			BufferedImage new_image = getResizedImage(original, image_new_width, image_new_height, true);
			return CharacterImage.fromImage(new_image);
		}
		return CharacterImage.fromImage(original);
	}
	
	public static CharacterImage generateMask(BufferedImage image, int image_new_width, int image_new_height) {
		return CharacterImage.generateMask(image, image_new_width, image_new_height, Color.WHITE, 180, 5, true);
	}
	
	public static CharacterImage generateMask(
		BufferedImage image, 
		int image_new_width, 
		int image_new_height, 
		Color mask_color, 
		int brightness_threshold, 
		int brightness_grad_pow,
		boolean with_resize
	) {
		BufferedImage new_image = getResizedImage(image, image_new_width, image_new_height, with_resize);

		int mr = (int) Math.floor(mask_color.getRed() * 255);
		int mg = (int) Math.floor(mask_color.getGreen() * 255);
		int mb = (int) Math.floor(mask_color.getBlue() * 255);

		int[] rgbaArray = new int[ image_new_width ];
		for(int y = 0; y < image_new_height; y++) {
			new_image.getRGB(0, y, image_new_width, 1, rgbaArray, 0, image_new_width);
			for(int x = 0; x < image_new_width; x++) {
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
			new_image.setRGB(0, y, image_new_width, 1, rgbaArray, 0, image_new_width);
		}
		return CharacterImage.fromImage(new_image);
	}
	public void combineWithMask(CharacterImage second_mask, int mask_offset_x, int mask_offset_y) {
		int w = buff_image.getWidth();
		int h = buff_image.getHeight();
		int w_src = second_mask.getWidth();
		int h_src = second_mask.getHeight();
		int[] rgbaArray = new int[ w ];
		int[] rgbaArraySrc = new int[ w_src ];
		boolean src_array_filled = true;
		for(int y = 0; y < h; y++) {
			buff_image.getRGB(0, y, w, 1, rgbaArray, 0, w);
			int y_src = y + mask_offset_y;
			if (y_src >= 0 && y_src < h_src) {
				second_mask.buff_image.getRGB(0, y_src,w_src, 1, rgbaArraySrc, 0, w_src);
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
						if (a_src < a) a = a_src;
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
	
	public void scaleDown() {
		scaleDown(600, 600);
	}
	public void scaleDown(int to_width, int to_height) {
		int[] targetSize = getAdjustedSize(to_width, to_height);
		buff_image = scaleDown(buff_image, targetSize[0], targetSize[1]);
		width = buff_image.getWidth();
		height = buff_image.getHeight();
		super.updatePercentageWidth(buff_image);
	}
	
	public void flip_image(boolean flip_x, boolean flip_y) {
		AffineTransform tx = null;
		if (flip_x && flip_y) {
			tx = AffineTransform.getScaleInstance(-1, -1);
			tx.translate(-buff_image.getWidth(), -buff_image.getHeight());
		} else if (flip_x) {
			tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-buff_image.getWidth(), 0);
		} else if (flip_y) {
			tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -buff_image.getHeight());
		}
		if (tx != null) {
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			buff_image = op.filter(buff_image, null);
		}
	}

	public void drawColorizedImage(
		CharacterImage image, 
		int pos_x,
		int pos_y,
		int image_new_width,
		int image_new_height,
		BodyPartColoringInfo coloring,
		CharacterImage coloring_texture,
		CharacterImage material_texture,
		CharacterImage coloring_mask,
		GradientParams transition_params,
		CharacterImage parent_coloring_texture,
		Point parent_coloring_draw_point,
		CharacterImage parent_mask,
		Point parent_draw_point,
		Point rotate_center,
		double rotate_angle_deg,
		int index
	) {
		if (image_new_width < 1 || image_new_height < 1) {
			return;
		}
		double diag_size = Math.hypot(image_new_width, image_new_height);
		double part_transition_pixels = diag_size * (transition_params != null ? transition_params.part_transition_percent : 0) * 0.01;

		Color image_color = coloring.primary_color;
		int mr = (int) Math.floor(image_color.getRed() * 255);
		int mg = (int) Math.floor(image_color.getGreen() * 255);
		int mb = (int) Math.floor(image_color.getBlue() * 255);

		Color border_color = transition_params!=null ? transition_params.gradient_color : Color.WHITE;
		int pr = (int) Math.floor(border_color.getRed() * 255);
		int pg = (int) Math.floor(border_color.getGreen() * 255);
		int pb = (int) Math.floor(border_color.getBlue() * 255);

		int[] parent_mask_data = null;
		int p_width = 0;
		int p_height = 0;
		java.awt.Point parent_draw_point_p = null;
		if (parent_draw_point != null && parent_mask != null) {
			if (debug_mode) parent_mask.save("res/images/chargen/debug/part-chargen-parent-mask-debug"+index+".png");
			p_width = parent_mask.getWidth();
			p_height = parent_mask.getHeight();
			parent_mask_data = parent_mask.getImageRGBArray();
			parent_draw_point_p = parent_draw_point.toIntPoint();
		}

		int[] coloring_mask_data = null;
		int coloring_texture_width = 0;
		int coloring_texture_height = 0;
		if (coloring_texture != null) {
			if (debug_mode) coloring_texture.save("res/images/chargen/debug/part-chargen-coloring-debug"+index+".png");
			coloring_texture_width = coloring_texture.getWidth();
			coloring_texture_height = coloring_texture.getHeight();
			coloring_mask_data = coloring_texture.getImageRGBArray();
		}

		int[] material_mask_data = null;
		int material_texture_width = 0;
		int material_texture_height = 0;
		if (material_texture != null) {
			if (debug_mode) material_texture.save("res/images/chargen/debug/part-chargen-material-debug"+index+".png");
			material_texture_width = material_texture.getWidth();
			material_texture_height = material_texture.getHeight();
			material_mask_data = material_texture.getImageRGBArray();
		}

		int[] parent_coloring_mask_data = null;
		int parent_texture_width = 0;
		int parent_texture_height = 0;
		if (parent_coloring_texture != null) {
			parent_texture_width = parent_coloring_texture.getWidth();
			parent_texture_height = parent_coloring_texture.getHeight();
			parent_coloring_mask_data = parent_coloring_texture.getImageRGBArray();
		}
		
		int	p, a, r, g, b, pt, 
			material_r, material_g, material_b;
		double  gradient_k, dist, material_k_a,
			colorize_r, colorize_g, colorize_b, 
			gradient_r, gradient_g, gradient_b;
		boolean colorization_allowed;
		BufferedImage new_image = getResizedImage(image.getImage(), image_new_width, image_new_height, true);
		if (
			(parent_mask_data != null && parent_draw_point != null) || 
			coloring_mask_data != null || 
			parent_coloring_mask_data != null || 
			material_mask_data != null || 
			(transition_params!=null && (pr!=mr || pg!=mg || pb!=mb)) || 
			(mr!=255 || mg!=255 || mb!=255)
		) {
			int[] rgbaArrayColorMask = null;
			BufferedImage coloring_mask_new_image = null;
			if (coloring_mask != null) {
				rgbaArrayColorMask = new int[ image_new_width ];
				coloring_mask_new_image = getResizedImage(coloring_mask.getImage(), image_new_width, image_new_height, true);
			}
			int[] rgbaArray = new int[ image_new_width ];
			for(int y = 0; y < image_new_height; y++) {
				new_image.getRGB(0, y, image_new_width, 1, rgbaArray, 0, image_new_width);
				if (coloring_mask_new_image != null) coloring_mask_new_image.getRGB(0, y, image_new_width, 1, rgbaArrayColorMask, 0, image_new_width);
				for(int x = 0; x < image_new_width; x++) {
					p = rgbaArray[x];
					a = (p>>24) & 0xff;
					if (a > 0) {
						r = (p>>16) & 0xff;
						g = (p>>8) & 0xff;
						b = p & 0xff;
						
						colorize_r = mr;
						colorize_g = mg;
						colorize_b = mb;
						
						gradient_r = pr;
						gradient_g = pg;
						gradient_b = pb;
						gradient_k = 1;

						colorization_allowed = (rgbaArrayColorMask == null || (rgbaArrayColorMask[x] & 0xffffff) > 0);
						if (!colorization_allowed) {
							colorize_r = colorize_g = colorize_b = 255;
							gradient_r = gradient_g = gradient_b = 255;
						}

						int parent_a = 255;
						if (parent_mask_data != null) {
							int x_p = x + pos_x - parent_draw_point_p.x;
							int y_p = y + pos_y - parent_draw_point_p.y;
							if (x_p >= 0 && x_p < p_width && y_p >= 0 && y_p < p_height) {
								parent_a = (parent_mask_data[x_p + y_p*p_width] >> 24) & 0xff;
							} else {
								parent_a = 0;
							}
						}
						if (transition_params!=null && colorization_allowed) {
							dist = transition_params.getTransitionDistance(x, y);
							if (dist < part_transition_pixels) gradient_k = (dist / part_transition_pixels);
							if (transition_params.is_inverted) gradient_k = 1 - gradient_k;
						}
						if (coloring_mask_data != null && colorization_allowed) {
							pt = coloring_mask_data[(pos_x+x) % coloring_texture_width + ((pos_y+y) % coloring_texture_height)*coloring_texture_width];
							colorize_r = (pt>>16) & 0xff;
							colorize_g = (pt>>8) & 0xff;
							colorize_b = pt & 0xff;
						}
						if (parent_coloring_mask_data != null && colorization_allowed) {
							pt = parent_coloring_mask_data[(pos_x+x) % parent_texture_width + ((pos_y+y) % parent_texture_height)*parent_texture_width];
							gradient_r = (pt>>16) & 0xff;
							gradient_g = (pt>>8) & 0xff;
							gradient_b = pt & 0xff;
						}
						if (material_mask_data != null) {
							pt = material_mask_data[(pos_x+x) % material_texture_width + ((pos_y+y) % material_texture_height)*material_texture_width];
							material_r = (pt>>16) & 0xff;
							material_g = (pt>>8) & 0xff;
							material_b = pt & 0xff;
							material_k_a = ((double) ((pt>>24) & 0xff)) / 255.0;
							
							colorize_r = colorize_r * (1 - material_k_a) + material_r * material_k_a;
							colorize_g = colorize_g * (1 - material_k_a) + material_g * material_k_a;
							colorize_b = colorize_b * (1 - material_k_a) + material_b * material_k_a;
							
							gradient_r = gradient_r * (1 - material_k_a) + material_r * material_k_a;
							gradient_g = gradient_g * (1 - material_k_a) + material_g * material_k_a;
							gradient_b = gradient_b * (1 - material_k_a) + material_b * material_k_a;
						}
						r = (int) (((colorize_r * gradient_k + gradient_r * (1 - gradient_k)) * r) / 255);
						g = (int) (((colorize_g * gradient_k + gradient_g * (1 - gradient_k)) * g) / 255);
						b = (int) (((colorize_b * gradient_k + gradient_b * (1 - gradient_k)) * b) / 255);
						if (r>255) r=255;
						if (g>255) g=255;
						if (b>255) b=255;
						if (parent_a < a) a = parent_a;

						rgbaArray[x] = (a<<24) | (r<<16) | (g<<8) | b;
					}
				}
				new_image.setRGB(0, y, image_new_width, 1, rgbaArray, 0, image_new_width);
			}
		}

		if (debug_mode) {
			try {
				ImageIO.write(new_image, "PNG", new File("res/images/chargen/debug/part-chargen-debug"+index+".png"));
			} catch (IOException ex) {}
		}
		
		Graphics2D canvas = buff_image.createGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (rotate_center != null && Math.abs(rotate_angle_deg) > 0.005) {
			AffineTransform at = new AffineTransform();
			at.rotate(Math.toRadians(rotate_angle_deg), rotate_center.x + pos_x, rotate_center.y + pos_y);
			canvas.setTransform(at);
		}
		
		/*if (coloring.primary_glowing) {
			CharacterImage glow_mask = generateMask(new_image, new_image.getWidth()*2, new_image.getHeight()*2, image_color, 30, 5, false);
			MotionBlurOp filter = new MotionBlurOp((float) 0.25, 0, 0, (float) 0.5);
			glow_mask.buff_image = filter.filter(glow_mask.buff_image, null);
			
			//CharacterImage glow_mask = generateMask(new_image, new_image.getWidth() + 10, new_image.getHeight() + 10, image_color, 0, 0, false);
			//glow_mask.applyBlurFilterToImage(3);
			if (debug_mode) {
				glow_mask.save("res/images/chargen/debug/part-chargen-glow-blur-debug"+index+".png");
			}
			
			if (glow_image == null) {
				glow_image = getBufferedImageForSize(buff_image.getWidth() * 2, buff_image.getHeight() * 2);
			}
			
			Graphics2D glow_canvas = glow_image.createGraphics();
			glow_canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			glow_canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			glow_canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			glow_canvas.drawImage(glow_mask.getImage(), pos_x + buff_image.getWidth()/2 - new_image.getWidth()/2, pos_y + buff_image.getHeight()/2 - new_image.getHeight()/2, null);
			glow_canvas.dispose();
			
			if (debug_mode) {
				try {
					ImageIO.write(glow_image, "PNG", new File("res/images/chargen/debug/part-glow-mask-debug"+index+".png"));
				} catch (IOException ex) {}
			}
		}*/

		canvas.drawImage(new_image, pos_x, pos_y, null);
		canvas.dispose();
		
		if (debug_mode) save("res/images/chargen/debug/chargen-debug"+index+".png");
	}
}
