package slashimages;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
//import java.lang.Math;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public class contentaware{
	
	
/*	private static Mat saliency(Mat I){
		Mat F = new Mat();
		double[] m = mean(I);
		//System.out.println(m[0]+","+ m[1]+","+ m[2]);
		F = compute_dist(m, I);
		Core.convertScaleAbs(F, F);
		return F;
	}
	
	private static Mat compute_dist(double[] mean, Mat I){
		int i, j;
		double d;
		Mat F = new Mat(I.height(), I.width(), 0);
		for (i = 0; i < I.height(); i++){
			for (j = 0; j < I.width(); j++){
				d = (I.get(i, j)[0]-mean[0])*(I.get(i, j)[0]-mean[0]) + (I.get(i, j)[1]-mean[1])*(I.get(i, j)[1]-mean[1]) + (I.get(i, j)[2]-mean[2])*(I.get(i, j)[2]- mean[2]);
				d = Math.sqrt(d);
				F.put(i, j, d);
			}
		}
		return F;
	} 
	
	
	private static double[] mean(Mat I){
		int sumB= 0, sumG = 0, sumR = 0;
		double mean[] = new double[3];
		int j, k;
		for (j = 0; j < I.height(); j++){
			for (k = 0; k < I.width(); k++){
					sumB = (int) (I.get(j,k)[0] + sumB);
					sumG = (int) (I.get(j,k)[1] + sumG);
					sumR = (int) (I.get(j,k)[2] + sumR);
			}
		}
		mean[0] = sumB/(I.height()*I.width());
		mean[1] = sumG/(I.height()*I.width());
		mean[2] = sumR/(I.height()*I.width());
		return mean;
	}
	*/
	
	private static Mat gradient(Mat I){		
		Mat F = new Mat();
		Size ksize = new Size(3, 3);
		double sigmaX = 0, sigmaY = 0;
		Imgproc.GaussianBlur(I, F, ksize, sigmaX, sigmaY);
		Imgproc.cvtColor(F, F, Imgproc.COLOR_RGB2GRAY);
		Mat Fx = new Mat();
		Mat Fy = new Mat();
		Imgproc.Sobel(F, Fx, -1, 0, 1);
		Imgproc.Sobel(F, Fy, -1, 1, 0);

		Core.convertScaleAbs(Fx, Fx);
		Core.convertScaleAbs(Fy, Fy);
		
		Core.addWeighted(Fx, 0.5, Fy, 0.5, 0, F);
		return F;
	}
	
	private static int check_increment(Mat I, double scalefactor, int s1, int s2, int olds1, int olds2, double cord1, double cord2){
		double sums1 = 0, sums2 = 0;
		int news1 = (int)(olds1*scalefactor);
		if (cord1+s1+news1 < I.height()){
			Mat fors1 = I.submat((int)cord1+s1, (int)cord1+s1+news1, (int)cord2, (int)cord2+s2);
			sums1 = Core.sumElems(fors1).val[0];
		}
		int news2 = (int)(olds2*scalefactor);
		if (cord2+s2+news2 < I.width()){
			Mat fors2 = I.submat((int)cord1, (int)cord1+s1, (int)cord2+s2, (int)cord2+s2+news2);
			sums2 = Core.sumElems(fors2).val[0];
		}
		if (sums1 > sums2)
			return 1;
		else if (sums1 < sums2)
			return 2;
		else if (sums1 == 0 && sums2 == 0)
			return 0;
		return 0;
	}
	
	private static double[] findCheck(Mat I, int s1, int s2, double scalefactor, int threshold, int preserveAspectRatio){
		double[] cord = new double[5];
		int olds1 = s1;
		int olds2 = s2;
		cord[0] = 0;
		cord[1] = 0;
		cord[2] = 0;
		cord[3] = 0;
		cord[4] = 0;
		while(cord[2] < threshold && s1 <= I.height() && s2 <= I.width()){
			cord = check(I, s1, s2);
			cord[3] = s1;
			cord[4] = s2;
			//------Preserving aspect ratio---------//
			if (preserveAspectRatio == 1){
				s1 = (int) (s1 + (scalefactor*olds1));
				s2 = (int) (s2 + (scalefactor*olds2));
				if (cord[2] < threshold && (s2 > I.width() || s1 > I.height())){
					cord[2] = -1;
				}
			}else{
			//------ Not perserving aspect ratio-----//
				if (check_increment(I, scalefactor, s1, s2, olds1, olds2, cord[0], cord[1]) == 1)
					s1 = (int) (s1 + (scalefactor*olds1));
				else if (check_increment(I, scalefactor, s1, s2, olds1, olds2, cord[0], cord[1]) == 2)
					s2 = (int) (s2 + (scalefactor*olds2));
			}
		}
		
		return cord;
	}
	
	private static double[] check(Mat F,int s1, int s2){
		double totalSum = 0, tmpSum = 0, max = 0;
		double cord[] = new double[5];
		Scalar sum = Core.sumElems(F);
		totalSum = sum.val[0];
		int i, j, locx = 0, locy = 0;
		for (i = 0; i < F.height()-s1; i++){
			for (j = 0; j < F.width()-s2; j++){
				Mat tmp = F.submat(i, i+s1, j, j+s2);
				tmpSum = Core.sumElems(tmp).val[0];
				if (tmpSum > max){
					max = tmpSum;
					locx = i;
					locy = j;
				}
			}
		}
		//System.out.println("Sum: "+totalSum+", Max: "+max+", locx: "+locx+", locy: "+locy);
		double percentageMatch = max*100/totalSum;
		//System.out.println("Percentage Coverage: "+percentageMatch+" %");
		cord[0] = locx;
		cord[1] = locy;
		cord[2] = percentageMatch;
		return cord;
	}
	
	private static Mat bytearrayToMat(byte[] b) throws IOException{
		File file = new File("try.jpeg");
		InputStream in = new ByteArrayInputStream(b);
		BufferedImage bI = ImageIO.read(in);
		ImageIO.write(bI, "jpg", file);
		Mat I = Highgui.imread("try.jpeg");
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
	
	private static byte[] matToBytearray(Mat I, int s1, int s2) throws IOException{
		BufferedImage bI = matToBufferedImage(I);
		bI = Scalr.resize(bI, Scalr.Mode.FIT_EXACT, s1, s2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bI, "jpg", baos);
		byte[] b = baos.toByteArray();
		return b;
	}
	
	public static byte[] contentAware(byte[] b, Map<String, String> M) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		int s1 = Integer.parseInt(M.get("w"));
		int s2 = Integer.parseInt(M.get("h"));
		double scalefactor = 0.1;
		int threshold = 90;
		int preserveAspectRatio = 1;
		if (M.containsKey("p"))
			preserveAspectRatio = Integer.parseInt(M.get("p"));
		Mat I = bytearrayToMat(b);
		Mat F = contentaware.gradient(I);
		double[] cord = findCheck(F, s2, s1, scalefactor, threshold, preserveAspectRatio);
		if (cord[2] == -1){
			//.............send to normal resize function...........//
			//System.out.println("Send To Normal Resize");
			byte[] a = null;
			return a;
		}else{
			Mat finalImage = I.submat((int)cord[0], (int)(cord[0]+cord[3]), (int)cord[1], (int)(cord[1]+cord[4]));
			//System.out.println(finalImage.size());
			return matToBytearray(finalImage, s1, s2);
		}
	}
}
