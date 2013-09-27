package matching;

public class HueSat {
	public int Hue = 0;
	public int Sat = 0;
	public int Val = 0;
	public double coverage = 0;
	
	public void setValues(int h, int s, int v){
		Hue = h;
		Sat = s;
		Val = v;
	}
	
	public void setCoverage(double c){
		coverage = c;
	}
	
	public void multValues(double k){
		Hue = (int) (Hue*k);
		Sat = (int) (Sat*k);
		Val = (int) (Val*k);
	}
	
	public HueSat add (HueSat H){
		H.Hue = H.Hue + Hue;
		H.Sat = H.Sat + Sat;
		H.Val = H.Val + Val;
		return H;
	}
	
	public boolean equals(Object H){
		HueSat J = (HueSat)H;
		if (J.Hue == Hue && J.Sat == Sat && J.Val == Val)
			return true;
		return false;
	}
	
	public int hashCode(){
		return Hue*200 + Sat + 1000*Val;
	}
}
