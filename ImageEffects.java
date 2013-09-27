package slashimages;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageEffects{

	private static ImagePlus convertToImagePlusForJmagick(byte[] Is){
		Image img = Toolkit.getDefaultToolkit().createImage(Is);
		ImagePlus I = new ImagePlus("title", img);
		return I;
	}

	private static byte[] convertToByteArrayFromImagePlusGeneral(ImagePlus I) throws IOException{
		BufferedImage buffer = I.getBufferedImage();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ImageIO.write(buffer, "jpg", baos );
		byte[] imageInByte=baos.toByteArray();
		return imageInByte;
	}

	public static byte[] binarize(byte[] Is) throws IOException{
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(Is); 
		ImageConverter C = new ImageConverter(I);
		C.convertToGray16();
		ImageProcessor ip = I.getProcessor();
		ip.autoThreshold();
		return convertToByteArrayFromImagePlusGeneral(I);
	}

	public static byte[] ConvertToGrayscale(byte[] Is) throws IOException{
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(Is); 
		ImageConverter C = new ImageConverter(I);
		C.convertToGray16();
		return convertToByteArrayFromImagePlusGeneral(I);
	}

	public static byte[] blur(byte[] Is, Map<String, String> M) throws Exception{
		int x, y, w, h;
		double s;
		x = Integer.parseInt(M.get("x"));
		y = Integer.parseInt(M.get("y"));
		w = Integer.parseInt(M.get("w"));
		h = Integer.parseInt(M.get("h"));
		s = Double.parseDouble(M.get("s"));
		return blurFinal(Is, x, y, w, h, s);
	}

	@SuppressWarnings("deprecation")
		private static byte[] blurFinal(byte[] Is, int x, int y, int w, int h, double s) throws Exception{
			ImagePlus I = new ImagePlus();
			I = convertToImagePlusForJmagick(Is);
			GaussianBlur G = new GaussianBlur();
			ImageProcessor ip = I.getProcessor();
			I.setRoi(x, y, w, h);
			G.blur(ip, s);
			return convertToByteArrayFromImagePlusGeneral(I);
		}

	public static byte[] autoContrast(byte[] Is) throws Exception{
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(Is);
		ContrastEnhancer CE = new ContrastEnhancer();
		//CE.equalize(I);
		CE.stretchHistogram(I, 10);
		return convertToByteArrayFromImagePlusGeneral(I);
	}

	public static byte[] sepia(byte[] Is) throws Exception{
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(Is);
		BufferedImage bI=I.getBufferedImage();
		BufferedImage fI = applySepiaFilter(bI, 30);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ImageIO.write(fI, "jpg", baos );
		byte[] imageInByte=baos.toByteArray();
		return imageInByte;
	}

	private static BufferedImage applySepiaFilter(BufferedImage img, int sepiaIntensity) {
		int sepiaDepth = 50;
		int w = img.getWidth();
		int h = img.getHeight();

		WritableRaster raster = img.getRaster();
		int[] pixels = new int[w*h*3];
		raster.getPixels(0, 0, w, h, pixels);

		for (int i=0;i<pixels.length; i+=3) {
			int r = pixels[i];
			int g = pixels[i+1];
			int b = pixels[i+2];

			int gry = (r + g + b) / 3;
			r = g = b = gry;
			r = r + (sepiaDepth * 2);
			g = g + sepiaDepth;

			if (r>255) r=255;
			if (g>255) g=255;
			if (b>255) b=255;

			b-= sepiaIntensity;

			if (b<0) b=0;
			if (b>255) b=255;

			pixels[i] = r;
			pixels[i+1]= g;
			pixels[i+2] = b;
		}
		raster.setPixels(0, 0, w, h, pixels);
		return img;
	}

	private static double meanValue(BufferedImage image) {
		Raster raster = image.getRaster();
		double sum = 0.0;

		for (int y = 0; y < image.getHeight(); ++y){
			for (int x = 0; x < image.getWidth(); ++x){
				sum += raster.getSample(x, y, 0);
			}
		}
		return sum / (image.getWidth() * image.getHeight());
	}

	private static BufferedImage applyBrightnessFilter(BufferedImage img, int Intensity) {
		int w = img.getWidth();
		int h = img.getHeight();

		WritableRaster raster = img.getRaster();
		int[] pixels = new int[w*h*3];
		raster.getPixels(0, 0, w, h, pixels);

		for (int i=0;i<pixels.length; i+=3) {
			int r = pixels[i] + Intensity;
			int g = pixels[i+1] + Intensity;
			int b = pixels[i+2] + Intensity;

			if (r>255) r=255;
			if (g>255) g=255;
			if (b>255) b=255;

			if (r<0) r=0;
			if (b<0) b=0;
			if (g<0) g=0;

			pixels[i] = r;
			pixels[i+1]= g;
			pixels[i+2] = b;
		}
		raster.setPixels(0, 0, w, h, pixels);
		return img;
	}

	public static byte[] adjustBrightness(byte[] Is, Map<String, String> M) throws NumberFormatException, Exception{
		return changeBrightness(Is, Integer.parseInt(M.get("v")));
	}

	private static byte[] changeBrightness(byte[] Is, int value) throws Exception{
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(Is);
		BufferedImage bI=I.getBufferedImage();
		BufferedImage fI = applyBrightnessFilter(bI, value);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ImageIO.write(fI, "jpg", baos );
		byte[] imageInByte=baos.toByteArray();
		return imageInByte;
	}

	public static byte[] autoBrightness(byte[] Is) throws Exception{
		int value = 0, m = 130, reqMean = 130;
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(Is);
		BufferedImage bI=I.getBufferedImage();
		m = (int) meanValue(bI);
		value = reqMean - m;
		System.out.println(m);
		BufferedImage fI = applyBrightnessFilter(bI, value);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ImageIO.write(fI, "jpg", baos );
		byte[] imageInByte=baos.toByteArray();
		return imageInByte;
	}

	public static byte[] adjustSaturation(byte[] Is, Map<String, String> M) throws NumberFormatException, MagickException{
		return changeSaturation(Is, Integer.parseInt(M.get("v")));
	}

	private static byte[] changeSaturation(byte[] Is, int val) throws MagickException{
		String str;
		str = "100, " + val +", 100";
		ImageInfo info = new ImageInfo();
		MagickImage im = new MagickImage(info, Is);
		im.modulateImage(str);
		byte[] I = im.imageToBlob(info);
		return I;
	}

	public static byte[] autoSharpen(byte[] Is) throws MagickException{
		double radius = 0.5, sigma = 0.5, threshold = 0.008, amount = 0.5;
		ImageInfo info = new ImageInfo();
		MagickImage im = new MagickImage(info, Is);
		MagickImage fm = im.unsharpMaskImage(radius, sigma, amount, threshold);
		byte[] I = fm.imageToBlob(info);
		return I;
	}

	public static byte[] pixelateImage(byte[] Is, Map<String, String> M) throws NumberFormatException, Exception{
		return pixelate(Is, Integer.parseInt(M.get("x")), Integer.parseInt(M.get("y")), Integer.parseInt(M.get("w")), Integer.parseInt(M.get("h")), Integer.parseInt(M.get("s")));
	}

	private static byte[] pixelate(byte[] Is, int gx, int gy, int gw, int gh, int val) throws Exception{
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(Is);
		BufferedImage img=I.getBufferedImage();
		BufferedImage fI = pixelateFinal(img, gx, gy, gw, gh, val);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ImageIO.write(fI, "jpg", baos );
		byte[] imageInByte=baos.toByteArray();
		return imageInByte;
	}

	private static BufferedImage pixelateFinal(BufferedImage img, int gx, int gy, int gw, int gh, int val){
		final int PIX_SIZE = val;
		Raster src = img.getData();
		WritableRaster dest = src.createCompatibleWritableRaster();		

		for(int y = 0; y < src.getHeight(); y++) {
			for(int x = 0; x < src.getWidth(); x++) {
				double[] pixel = null;
				pixel = src.getPixel(x, y, pixel);
				dest.setPixel(x, y, pixel);
			}
		}

		int maxvaly, maxvalx;
		if (gy + gh > src.getHeight()){
			maxvaly = src.getHeight();
		}else{
			maxvaly = gy + gh;
		}

		if (gx + gw > src.getWidth()){
			maxvalx = src.getWidth();
		}else{
			maxvalx = gx + gw;
		}

		for(int y = gy; y < maxvaly; y += PIX_SIZE) {
			for(int x = gx; x < maxvalx; x += PIX_SIZE) {
				double[] pixel = null;
				pixel = src.getPixel(x, y, pixel);
				for(int yd = y; (yd < y + PIX_SIZE) && (yd < maxvaly); yd++) {
					for(int xd = x; (xd < x + PIX_SIZE) && (xd < maxvalx); xd++) {
						dest.setPixel(xd, yd, pixel);
					}
				}
			}
		}
		img.setData(dest);
		return img;
	}

}