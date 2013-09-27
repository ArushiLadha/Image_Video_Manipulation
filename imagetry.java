import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import ij.ImagePlus;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

public class imagetry{
	
	public static byte[] converts_to_bytearray(String FileName) throws MagickException{
		ImageInfo info = new ImageInfo(FileName);
		MagickImage im = new MagickImage(info);
		byte[] I = im.imageToBlob(info);
		return I;
	}
	
	public static void showImage(byte[] S){
		ImagePlus I = new ImagePlus();
		I = convertToImagePlusForJmagick(S);
		I.show();
	}
	
	public static ImagePlus convertToImagePlusForJmagick(byte[] Is){
		Image img = Toolkit.getDefaultToolkit().createImage(Is);
		ImagePlus I = new ImagePlus("title", img);
		return I;
	}
	
	public static void main(String args[]) throws Exception{
		ImageInfo I = new ImageInfo("index.jpeg");
		Map<String, String> M = new HashMap<String, String>();
		M.put("v","150" );
		M.put("x","100" );
		M.put("y","100" );
		M.put("w","100" );
		M.put("h","100" );
		M.put("s","10" );
		MagickImage IM = new MagickImage(I);
		byte[] S;
		byte[] b = IM.imageToBlob(I);
		showImage(b);
		//S = ImageEffects.autoContrast(b);
		//S = ImageEffects.adjustBrightness(b, M);
		//S = ImageEffects.adjustSaturation(b, M);
		//S = ImageEffects.autoBrightness(b);
		//S = ImageEffects.autoSharpen(b);
		//S = ImageEffects.binarize(b);
		//S = ImageEffects.blur(b, M);
		//S = ImageEffects.ConvertToGrayscale(b);
		//S = ImageEffects.pixelateImage(b, M);
		S = ImageEffects.sepia(b);
		showImage(S);
	}
}