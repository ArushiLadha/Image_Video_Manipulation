package matching;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class readFile{
	
	public static List<List<HueSat>> readfile(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		List<List<HueSat> > L = new ArrayList<List<HueSat>>();
		String CurrentLine;
		int ind = 0;
		while ((CurrentLine = br.readLine()) != null) {
			String[] s = new String[3];
			s = CurrentLine.split(";");
			int i;
			List<HueSat> top3 = new ArrayList<HueSat>();
			for (i = 0; i < s.length; i++){
				String[] cs = new String[2];
				cs = s[i].split(":");
				String[] ss = new String[3];
				ss = cs[0].split(",");
				HueSat H = new HueSat();
				H.setValues(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]));
				H.setCoverage(Double.parseDouble(cs[1]));
				top3.add(i, H);
			}
			L.add(ind, top3);
			ind++;
		}
		return L;
	}
}