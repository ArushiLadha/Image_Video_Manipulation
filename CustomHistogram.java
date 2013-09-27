import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public class CustomHistogram {
	
	public static List<HueSat> ComputeHistogram(Mat I, List<HueSat> top3){
		Mat J = new Mat();
		Map<HueSat, Integer> M = new HashMap<HueSat, Integer>();
		HueSat E = new HueSat();
		E.setValues(10, 25, 127);
		int k;
		M.put(E, 0);
		for (k = 0; k < 3; k++)
			top3.add(k, E);
		Imgproc.cvtColor(I, J, Imgproc.COLOR_RGB2HSV);
		int i, j;
		for (i = 0; i < J.height(); i ++){
			for (j = 0; j < J.width(); j++){
				int normalizedHue;
				double[] a = J.get(i, j);
				int tempHue = (int)(a[0]*2);
				int bin_num = tempHue/20;
				normalizedHue = bin_num*20 + 10;
				if (normalizedHue >= 360)
					normalizedHue = 350;
				int normalizedSat = (int)(a[1]/51)*51 + 25;
				if (normalizedSat >= 255)
					normalizedSat = 230;
				int normalizedVal;
				if (a[2] < 80)
					normalizedVal = 42;
				else if (a[2] > 240)
					normalizedVal = 213;
				else
					normalizedVal = 127;
				HueSat H = new HueSat();
				H.setValues(normalizedHue, normalizedSat, normalizedVal);
				
				if (M.containsKey(H)){
					M.put(H,M.get(H)+1);
				}else{
					M.put(H,1);
				}
			}
		}
		
		//PrintMap(M);
		Iterator<Entry<HueSat, Integer>> it = M.entrySet().iterator();
		int y1 = 0, y2 = 0, y3 = 0;
		
	    while (it.hasNext()) {
	    	
	      	@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	       	int x = (Integer) pairs.getValue();
	       	if (x > y1){
	       		top3.set(2, top3.get(1));
	       		top3.set(1, top3.get(0));
	       		top3.set(0, (HueSat) pairs.getKey());
	       		y3 = y2;
	       		y2 = y1;
	       		y1 = x;
	       	}else if(x > y2){
	       		top3.set(2, top3.get(1));
	       		top3.set(1, (HueSat) pairs.getKey());
	       		y3 = y2;
	       		y2 = x;
	       	}else if (x > y3){
	       		top3.set(0, (HueSat) pairs.getKey());
	       		y3 = x;
	       	}
	       	it.remove(); 
	    }
	    E.setValues(y1, y2, y3);
	    top3.add(3, E);
		return top3;
	}



	
	public static void PrintMap(Map<HueSat, Integer> M){
	    Iterator<HueSat> iterator = M.keySet().iterator();  
	       
	    while (iterator.hasNext()) {  
	       HueSat key = (HueSat) iterator.next();  
	       int value = M.get(key);  
	       
	       System.out.println(key.Hue + ", " +key.Sat + ", " + key.Val + "->" + value);  
	    }  
	}
	
	public static Map<Double, Integer> startmatch(List<HueSat> top3, List<List<HueSat>> L, int method){
		double[] k = new double[3];
		k[0] = 1;
		k[1] = 0.5;
		k[2] = 0.25;
		int i, j;
		double[] dist = new double[1000];
		Map<Double, Integer> M = new TreeMap<Double, Integer>();
		List<HueSat> X = new ArrayList<HueSat>();
		for (i = 0; i < L.size(); i++){
			X = L.get(i);
		/*	for (j = 0; j < 3; j++){
				X.get(j).multValues(k[j]);
				top3.get(j).multValues(k[j]);
			}
			*/
			if (method == 1){
				dist[i] = distance.getDistance_cyl(top3.get(0), X.get(0)) + k[1]*distance.getDistance_cyl(top3.get(0), X.get(1)) + k[2]*distance.getDistance_cyl(top3.get(0), X.get(2)) + k[1]*distance.getDistance_cyl(top3.get(1), X.get(0)) + k[1]*k[1]*distance.getDistance_cyl(top3.get(1), X.get(1)) + k[1]*k[2]*distance.getDistance_cyl(top3.get(1), X.get(2)) + k[2]*distance.getDistance_cyl(top3.get(2), X.get(0)) + k[2]*k[1]*distance.getDistance_cyl(top3.get(2), X.get(1)) + k[2]*k[2]*distance.getDistance_cyl(top3.get(2), X.get(2));
				M.put(dist[i], i);
			}else{
				dist[i] = distance.getDistance_cir(top3.get(0).add(top3.get(1).add(top3.get(2))), X.get(0).add(X.get(1).add(X.get(2))));
				M.put(dist[i], i);
			}
		}
		return M;
	}
	
	public static void PrintMap2(Map<Double, Integer> M, String s) throws IOException{
		File filetoread = new File(s);
        BufferedWriter output = new BufferedWriter(new FileWriter(filetoread));
        
	    Iterator<Double> iterator = M.keySet().iterator();  
	       
	    while (iterator.hasNext()) {  
	       double key = (double) iterator.next();  
	       int value = M.get(key);  
	       
	       output.write(key +"->" + value + "\n");  
	    }  
	    output.close();
	}
	
	public static void main(String[] args) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
        /*Mat I = Highgui.imread("../../../Downloads/image.orig/899.jpg");
		List<HueSat> top3 = new ArrayList<HueSat>();
		top3 = ComputeHistogram(I, top3);
		*/
		
		File filetoread = new File("data.txt");
        BufferedWriter output = new BufferedWriter(new FileWriter(filetoread));
		
		//File folder = new File("../../../Downloads/image.orig");
		//File[] listOfFiles = folder.listFiles();
		
		int count = -1;
		List<List<HueSat> > L = new ArrayList<List<HueSat>>();
		int sm;
		String file;
		//for (File file : listOfFiles) {
		for (sm = 0; sm < 1000; sm++){
			file = sm+".jpg";
		    //if (file.isFile()) {
		    	count++;
		        System.out.println(file+" Count: "+count);
		        Mat I = Highgui.imread("../../../Downloads/image.orig/"+file);
				List<HueSat> top3 = new ArrayList<HueSat>();
				top3 = ComputeHistogram(I, top3);
				L.add(count, top3);
				output.write(top3.get(0).Hue+","+top3.get(0).Sat+","+top3.get(0).Val+";"+top3.get(1).Hue+","+top3.get(1).Sat+","+top3.get(1).Val+";"+top3.get(2).Hue+","+top3.get(2).Sat+","+top3.get(2).Val+";\n");

		    //}
		}
		output.close();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String S = reader.readLine();
		while(S != "exit" ){
			Mat test = Highgui.imread(S);
			List<HueSat> top3 = new ArrayList<HueSat>();
			top3 = ComputeHistogram(test, top3);
			
			Map<Double, Integer> dist_cylinder = startmatch(top3, L, 1);
			PrintMap2(dist_cylinder, "cylinder.txt");
			//Map<Double, Integer> dist_sphere = startmatch(top3, L, 2);
			//PrintMap2(dist_sphere, "circle.txt");
			
			reader = new BufferedReader(new InputStreamReader(System.in));
			S = reader.readLine();
		}
	
/*		int sum = 0;
		sum = sum + top3.get(3).Hue + top3.get(3).Sat + top3.get(3).Val;
		System.out.println(top3.get(0).Hue+", "+top3.get(0).Sat+", "+top3.get(0).Val +"->"+ top3.get(3).Hue );
		System.out.println(top3.get(1).Hue+", "+top3.get(1).Sat+", "+top3.get(1).Val +"->"+ top3.get(3).Sat);
		System.out.println(top3.get(2).Hue+", "+top3.get(2).Sat+", "+top3.get(2).Val +"->"+ top3.get(3).Val);
			
		int total = I.width()*I.height();
		System.out.println("Total: "+total);
		System.out.println("Sum: "+sum);
		double percentage = sum*100/total;
		System.out.println("Percentage Covered: "+percentage);

*/	
		
	}
}
