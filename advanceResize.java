package slashimages;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public class advanceResize {
	
	private static Mat bytearrayToMat(byte[] b) throws IOException{
		File file = new File("try.jpeg");
		InputStream in = new ByteArrayInputStream(b);
		BufferedImage bI = ImageIO.read(in);
		ImageIO.write(bI, "jpg", file);
		Mat I = Highgui.imread("try.jpeg");
		Runtime r = Runtime.getRuntime();
		r.exec("rm -r try.jpeg");
		return I;
	}
	
	private static BufferedImage matToBufferedImage(Mat bgr) {
	    int width = bgr.width();
	    int height = bgr.height();
	    BufferedImage image;
	    WritableRaster raster;

	    if (bgr.channels()==1) {
	        image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
	        raster = image.getRaster();

	        byte[] px = new byte[1];

	        for (int y=0; y<height; y++) {
	            for (int x=0; x<width; x++) {
	                bgr.get(y,x,px);
	                raster.setSample(x, y, 0, px[0]);
	            }
	        }
	    } else {
	        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	        raster = image.getRaster();

	        byte[] px = new byte[3];
	        int[] rgb = new int[3];

	        for (int y=0; y<height; y++) {
	            for (int x=0; x<width; x++) {
	                bgr.get(y,x,px);
	                rgb[0] = px[2];
	                rgb[1] = px[1];
	                rgb[2] = px[0];
	                raster.setPixel(x,y,rgb);
	            }
	        }
	    }

	    return image;
	}
	
	private static byte[] matToBytearray(Mat I) throws IOException{
		BufferedImage bI = matToBufferedImage(I);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bI, "jpg", baos);
		byte[] b = baos.toByteArray();
		return b;
	}
	
	public static byte[] resize(byte[] b, int width, int height, String interpolation) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat I = bytearrayToMat(b);
		Mat J = new Mat();
		Size dsize = new Size(width, height);
		if (interpolation == "Bicubic")
			Imgproc.resize(I, J, dsize, 0, 0, Imgproc.INTER_CUBIC);
		else if (interpolation == "NearestNeighbor")
			Imgproc.resize(I, J, dsize, 0, 0, Imgproc.INTER_NEAREST);
		else
			Imgproc.resize(I, J, dsize, 0, 0, Imgproc.INTER_LINEAR);
		return matToBytearray(J);
		
	}
}
