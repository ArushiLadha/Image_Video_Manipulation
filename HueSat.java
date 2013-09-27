
public class HueSat {
	public int Hue = 0;
	public int Sat = 0;
	public int Val = 0;
	
	public void setValues(int h, int s, int v){
		Hue = h;
		Sat = s;
		Val = v;
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
