package matching;

import java.util.Comparator;
import java.util.Map;


public class ValueComparator implements Comparator<HueSat> {

	    Map<HueSat, Integer> base;
	    public ValueComparator(Map<HueSat, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.
	    @Override
	    public int compare(HueSat a, HueSat b) {
	    	return ((Integer) base.get(b)).compareTo((Integer) base.get(a));
	   } 
}


