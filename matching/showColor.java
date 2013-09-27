package matching;
import static com.googlecode.javacv.cpp.opencv_core.CV_8UC3;

import java.util.List;

//import java.awt.List;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import com.googlecode.javacv.cpp.opencv_imgproc;

public class showColor{
	
	public static void plotColors(List<HueSat> top3, int count){
		String filename = "./plottedColors/"+count+".jpg";
		int s = top3.size();
		int k = 60;
		Size size = new Size(k*s, k); 
		Mat bgr = new Mat(size, CV_8UC3);
				
		Mat hsv = new Mat(size, CV_8UC3);
		org.opencv.imgproc.Imgproc.cvtColor(bgr, hsv, opencv_imgproc.CV_RGB2HSV);
		
		int i,j;
		int SatBins = 3, ValBins = 3;
		for(i = 0; i< k*s; i++){
			for(j = 0; j< k; j++){
				double[] f = new double[3];
				f[0] = (double) top3.get((int)(i/k)).Hue;
				f[0] = f[0]/2;
				f[1] = (double) top3.get((int)(i/k)).Sat;
				f[1] = f[1]*(255/SatBins) - (255/(2*SatBins));
				f[2] = (double) top3.get((int)(i/k)).Val;
				f[2] = 3 - f[2] + 1;
				f[2] = f[2]*(255/ValBins) - (255/(2*ValBins));
				hsv.put(j, i, f);				
			}
		}
		
		org.opencv.imgproc.Imgproc.cvtColor(hsv, bgr, opencv_imgproc.CV_HSV2RGB);
		org.opencv.highgui.Highgui.imwrite(filename, bgr);
	}
	
}