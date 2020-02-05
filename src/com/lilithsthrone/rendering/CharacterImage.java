package com.lilithsthrone.rendering;

import com.lilithsthrone.rendering.chargen.BodyPartColoringInfo;
import com.lilithsthrone.rendering.chargen.GradientParams;
import com.lilithsthrone.rendering.chargen.ImageUtils;
import com.lilithsthrone.rendering.chargen.Point;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
	private static final String DEBUG_PATH = "res/images/chargen/debug";

	public static CharacterImage fromImage(BufferedImage image) {
		CharacterImage new_ci = new CharacterImage();
		new_ci.load(image);
		return new_ci;
	}

	public static CharacterImage fromFile(String file_name, int image_new_width, int image_new_height) throws IOException {
		BufferedImage original = ImageIO.read(new File(file_name));
		if (original.getWidth() != image_new_width || original.getHeight() != image_new_height) {
			return CharacterImage.fromImage(ImageUtils.getResizedImage(original, image_new_width, image_new_height, true));
		}
		return CharacterImage.fromImage(original);
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
			if (debug_mode) save(DEBUG_PATH + "/chargen-debug.png");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void initForSize(int width, int height) {
		updatePercentageWidth(ImageUtils.getNewEmptyBufferedImage(width, height));
	}

	public BufferedImage getImage() {
		return buff_image;
	}

	@Override
	public void updatePercentageWidth(BufferedImage image) {
		super.updatePercentageWidth(image);
		buff_image = image;
	}

	public CharacterImage getResizedImage(int image_new_width, int image_new_height, boolean stretch_image) {
		return CharacterImage.fromImage(ImageUtils.getResizedImage(buff_image, image_new_width, image_new_height, stretch_image));
	}

	public CharacterImage getColorizedImage(int image_new_width, int image_new_height, BodyPartColoringInfo coloring, Color primary_tmpl, Color secondary_tmpl) {
		return CharacterImage.fromImage(ImageUtils.getColorizedImage(buff_image, image_new_width, image_new_height, coloring, primary_tmpl, secondary_tmpl));
	}

	public void colorOverlay(Color color) {
		ImageUtils.colorOverlay(buff_image, color);
	}

	public CharacterImage generateMask(int image_new_width, int image_new_height) {
		return CharacterImage.fromImage(ImageUtils.generateMask(buff_image, image_new_width, image_new_height, Color.WHITE, 180, 5, true));
	}

	public void combineWithMask(CharacterImage second_mask, int mask_offset_x, int mask_offset_y) {
		ImageUtils.combineImageWithImageMask(buff_image, second_mask.getImage(), mask_offset_x, mask_offset_y);
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

	public void flipImage(boolean flip_x, boolean flip_y) {
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

	public void drawColorizedBodypartImage(
		CharacterImage bodypart_image, 
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
			if (debug_mode) parent_mask.save(DEBUG_PATH + "/part-chargen-parent-mask-debug"+index+".png");
			p_width = parent_mask.getWidth();
			p_height = parent_mask.getHeight();
			parent_mask_data = ImageUtils.getImageRGBArray(parent_mask.getImage());
			parent_draw_point_p = parent_draw_point.toIntPoint();
		}

		int[] coloring_mask_data = null;
		int coloring_texture_width = 0;
		int coloring_texture_height = 0;
		if (coloring_texture != null) {
			if (debug_mode) coloring_texture.save(DEBUG_PATH + "/part-chargen-coloring-debug"+index+".png");
			coloring_texture_width = coloring_texture.getWidth();
			coloring_texture_height = coloring_texture.getHeight();
			coloring_mask_data = ImageUtils.getImageRGBArray(coloring_texture.getImage());
		}

		int[] material_mask_data = null;
		int material_texture_width = 0;
		int material_texture_height = 0;
		if (material_texture != null) {
			if (debug_mode) material_texture.save(DEBUG_PATH + "/part-chargen-material-debug"+index+".png");
			material_texture_width = material_texture.getWidth();
			material_texture_height = material_texture.getHeight();
			material_mask_data = ImageUtils.getImageRGBArray(material_texture.getImage());
		}

		int[] parent_coloring_mask_data = null;
		int parent_texture_width = 0;
		int parent_texture_height = 0;
		if (parent_coloring_texture != null) {
			parent_texture_width = parent_coloring_texture.getWidth();
			parent_texture_height = parent_coloring_texture.getHeight();
			parent_coloring_mask_data = ImageUtils.getImageRGBArray(parent_coloring_texture.getImage());
		}
		
		int	p, a, r, g, b, pt, 
			material_r, material_g, material_b;
		double  gradient_k, dist, material_k_a,
			colorize_r, colorize_g, colorize_b, 
			gradient_r, gradient_g, gradient_b;
		boolean colorization_allowed;
		BufferedImage new_image = ImageUtils.getResizedImage(bodypart_image.getImage(), image_new_width, image_new_height, true);
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
				coloring_mask_new_image = ImageUtils.getResizedImage(coloring_mask.getImage(), image_new_width, image_new_height, true);
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
				ImageIO.write(new_image, "PNG", new File(DEBUG_PATH + "/part-chargen-debug"+index+".png"));
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
			if (debug_mode) {
				glow_mask.save(DEBUG_PATH + "/part-chargen-glow-blur-debug"+index+".png");
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
					ImageIO.write(glow_image, "PNG", new File(DEBUG_PATH + "/part-glow-mask-debug"+index+".png"));
				} catch (IOException ex) {}
			}
		}*/

		canvas.drawImage(new_image, pos_x, pos_y, null);
		canvas.dispose();

		if (debug_mode) save(DEBUG_PATH + "/chargen-debug"+index+".png");
	}
}
