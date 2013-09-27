package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class CustomHistogram {
	
	public static void main(String[] args) throws IOException{
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
/*		//...........Single Image.............//
		Mat I = Highgui.imread("./myntra_images/785.jpg");
		List<HueSat> top3 = new ArrayList<HueSat>();
		//Highgui.imwrite("arushi.jpeg", I);
		top3 = ComputeHistogram(I	);
*/		//....................................//

		//............For Whole Directory.....//
		File filetoread = new File("myntra_data.txt");
        BufferedWriter output = new BufferedWriter(new FileWriter(filetoread));
        
		int count = -1, sm;
		String file;
		List<List<HueSat> > L = new ArrayList<List<HueSat>>();
		
		for (sm = 0; sm < 831; sm++){
			file = sm+".jpg";
			
			count++;
		    System.out.println(file+" Count: "+count);
		    List<HueSat> top3 = new ArrayList<HueSat>();
		    
		    Mat I = Highgui.imread("./myntra_images/"+file);
		    if (I.width()*I.height() > 120000){
		    	Size dsize = new Size(300, 400);
		    	double fx = dsize.width/I.cols();
		    	Imgproc.resize(I, I, dsize, fx, fx, 0);
		    }
	
			top3 = ComputeHistogram(I);
			showColor.plotColors(top3, count);
			
			L.add(count, top3);
			int kx;
			for (kx = 0; kx < top3.size(); kx++)
				output.write(top3.get(kx).Hue+","+top3.get(kx).Sat+","+top3.get(kx).Val+":"+top3.get(kx).coverage+";");
			output.write("\n");
		}
		output.close();

	}

	public static List<HueSat> ComputeHistogram(Mat I) throws IOException {
		List<HueSat> top3 = new ArrayList<HueSat>();						//..To store top N colors..		
        Map<HueSat, Integer> M = getMap(I);
        HueSat Ht = new HueSat();
        int bgpixels = 0;
        Ht = changeHistogram.changeHistogram(M, I);
        if (M.containsKey(Ht)){
        	bgpixels = M.get(Ht);
        	M.put(Ht, 0);
        }
      
        ValueComparator bvc =  new ValueComparator(M);
        TreeMap<HueSat,Integer> sorted_map = new TreeMap<HueSat,Integer>(bvc);
        sorted_map.putAll(M);
		//extraFunctions.PrintMap(M);
		//extraFunctions.PrintMap(sorted_map);
		
		int i, TopNElements = 2;
		Iterator<HueSat> iterator = sorted_map.keySet().iterator();  
		for (i = 0; i < TopNElements; i++){
			HueSat key = (HueSat) iterator.next();
			int value = sorted_map.get(key);
			key.setCoverage((double)value);
			top3.add(i, key);
		}
		
		double factor = 1;
		double sum = I.width()*I.height() - bgpixels;
		int kx;
		for (kx = 0;kx < top3.size(); kx++)
			top3.get(kx).setCoverage(sum/top3.get(kx).coverage*factor);
		
		return top3;
	}
	
	public static Map<HueSat, Integer> getMap(Mat I){
		Map<HueSat, Integer> M = new HashMap<HueSat, Integer>();
		Mat J = new Mat();																			
		Imgproc.cvtColor(I, J, Imgproc.COLOR_RGB2HSV);						//..Conveerting to HSV color space..
		
		int NormalizedHue, NormalizedSat, NormalizedVal;
		int BinNum1, BinNum2, BinNum3;
		int HueBins=18, SatBins = 3, ValBins = 3;
		int i, j;
		for (i = 0; i < J.height(); i ++){
			for (j = 0; j < J.width(); j++){
				double[] a = J.get(i, j);		
				
				if (a[0] == 180)
					a[0] = 0;
				BinNum1 = (int)(a[0]+(180/HueBins))/(180/HueBins);
				NormalizedHue = BinNum1*(180/HueBins) - (180/(2*HueBins));
				NormalizedHue = NormalizedHue * 2;
				
				if (a[1] == 255)
					a[1] = 254;
				BinNum2 = (int)(a[1]+(255/SatBins))/(255/SatBins);
				NormalizedSat = BinNum2;
				//NormalizedSat = BinNum*(255/SatBins) - (255/(2*SatBins));
				
				if (a[2] == 255)
					a[2] = 254;
				BinNum3 = (int)(a[2]+(255/ValBins))/(255/ValBins);
				NormalizedVal = ValBins - BinNum3 + 1;
				//NormalizedVal = BinNum*(255/ValBins) - (255/(2*ValBins));
				
				HueSat H = new HueSat();
				H.setValues(NormalizedHue, NormalizedSat, NormalizedVal);
				
				if (M.containsKey(H))
					M.put(H, M.get(H)+1);
				else
					M.put(H, 1);
			}
		}
		return M;
	}
}