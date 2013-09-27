package matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

import org.opencv.core.Mat;

public class changeHistogram {
	//public static Map<HueSat, Integer> changeHistogram(Map<HueSat, Integer> M, Mat I) throws IOException{
	public static HueSat changeHistogram(Map<HueSat, Integer> M, Mat I) throws IOException{
		HueSat H = findBackgroundColor(I);
		//int bgpixels = 0;
		//if (H != null){
		//	if (M.containsKey(H))
		//		bgpixels = M.get(H);
		//		M.put(H, 0);
		//}
		//return bgpixels;
		return H;
	}
	
	public static HueSat findBackgroundColor(Mat I) throws IOException{
		FaceBox F = contentaware.contentAware(I);
		if (F == null){
			return null;
		}else{
			//System.out.println(F.x1+", "+F.x2+", "+F.y1+", "+F.y2);
			Mat temp = regionPick(I, F);
			List<HueSat> top3 = ComputeHistogramAgain(temp);
			return top3.get(0);
		}	
	}
	
	public static Mat regionPick(Mat I, FaceBox F){
		int[] cord = new int[4];
		int l1, l2;
		if (F.x1 > (I.height() - F.x2)){
			l1 = F.x1;
			cord[0] = 0; cord[1] = F.x1;
		}else{
			l1 = I.height() - F.x2;
			cord[0] = F.x2; cord[1] = I.height();
		}
		if (F.y1 > (I.width() - F.y2)){
			l2 = F.y1;
			cord[2] = 0; cord[3] = F.y1;
		}else{
			l2 = I.width() - F.y2;
			cord[2] = F.y2; cord[3] = I.width();
		}
		
		
		if (l1 > l2){
			cord[2] = 0;
			cord[3] = I.width();
		}else{
			cord[0] = 0; 
			cord[1] = I.height();
		}
		//System.out.println(cord[0]+","+cord[1]+","+cord[2]+","+cord[3]);
		Mat temp = I.submat(cord[0], cord[1], cord[2], cord[3]);
		//Highgui.imwrite("trial.jpg", temp);
		return temp;
	}
	
	
	public static List<HueSat> ComputeHistogramAgain(Mat I) throws IOException {
		List<HueSat> top3 = new ArrayList<HueSat>();						//..To store top N colors..		
        Map<HueSat, Integer> M = CustomHistogram.getMap(I);
        int bgpixels = 0;
        
        ValueComparator bvc =  new ValueComparator(M);
        TreeMap<HueSat,Integer> sorted_map = new TreeMap<HueSat,Integer>(bvc);
        sorted_map.putAll(M);
		//extraFunctions.PrintMap(M);
		//extraFunctions.PrintMap(sorted_map);
		
		int i, TopNElements = 1;
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
	
}
