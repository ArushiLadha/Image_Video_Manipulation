package matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opencv.core.Mat;

public class extraFunctions {

	public static void PrintMap(Map<HueSat, Integer> M){
		Iterator<HueSat> iterator = M.keySet().iterator();  
       
		while (iterator.hasNext()) {  
			HueSat key = (HueSat) iterator.next();
			int value = M.get(key);  
			System.out.println(key.Hue + ", " +key.Sat + ", " + key.Val + "->" + value);  
		}  
	}
	
	public static void PrintList(List<List<HueSat>> L){
		int i, j;
		for (i = 0; i < L.size(); i++){
			for (j = 0; j < L.get(i).size(); j++){
				System.out.print(L.get(i).get(j).Hue+", "+L.get(i).get(j).Sat+", "+L.get(i).get(j).Val+":"+L.get(i).get(j).coverage+";");
			}
			System.out.println();
		}
	}	
}
