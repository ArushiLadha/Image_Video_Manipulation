package matching;

import ij.ImagePlus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
public class compareImages{
	
	public static void main(String[] args) throws IOException{
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		List<List<HueSat>> L = readFile.readfile("./myntra_data.txt");
		
		String InputFile = "./myntra_images/648.jpg";
		Mat I = Highgui.imread(InputFile);
		//............resizing required..............//
		//Size dsize = new Size(300, 400);
		//double fx = dsize.width/I.cols();
		//Imgproc.resize(I, I, dsize, fx, fx, 0);
		ImagePlus Image = new ImagePlus(InputFile);
		Image.show();
		List<HueSat> top3 = new ArrayList<HueSat>();
		top3 = CustomHistogram.ComputeHistogram(I);
		
		int i;
		double[] dist = new double[L.size()];
		int[] index = new int[L.size()];
		double val;
		for (i = 0; i < L.size(); i++){
			val = calcDist(top3, L.get(i));
			dist[i] = val;
			index[i] = i;
		}
		
		int j;
		double temp;
		int temp2;
		for (i = 0; i < dist.length; i++ ){
			for (j = 0; j < dist.length-1; j++){
				if (dist[j] > dist[j+1]){
					temp = dist[j];
					dist[j] = dist[j+1];
					dist[j+1] = temp;
					temp2 = index[j];
					index[j] = index[j+1];
					index[j+1] = temp2;
				}
			}
		}
		
		int DisplayTopN = 5;
		for (i = 0; i < DisplayTopN; i++){
				String filename = index[i]+".jpg";
				System.out.println(filename+" "+dist[i]);
				//Image = new ImagePlus("./image.orig/"+filename);
				Image = new ImagePlus("./myntra_images/"+filename);
				Image.show();
				Image = new ImagePlus("./plottedColors/"+filename);
				Image.show();
				
		}
		
	}
	
	private static double calcDist(List<HueSat> o1, List<HueSat> o2){
		double d = 0, min, d1;
		int i, j;
		int compelems = o1.size();
		double sum = 0, sum1 = 0;
		for (i = 0; i < compelems; i++){
			min = 10000;
			d1 = distance.getDistance_cyl(o1.get(i), o2.get(i)) + 0.0001;
			d1 = d1*o1.get(i).coverage*o2.get(i).coverage;
//			d1 = d1/(i+1);
			for (j = 0; j < compelems; j++){
				d = distance.getDistance_cyl(o1.get(i), o2.get(j)) + 0.0001;
				d = d*o1.get(i).coverage*o2.get(j).coverage;
				if (d < min)
					min = d;
			}
			sum = sum + min;

			sum1 = sum1 + d1;
			
		}
		return sum1;
		//return (sum1<sum)?sum1:sum;
			
		}
	
}