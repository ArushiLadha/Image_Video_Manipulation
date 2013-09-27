package matching;

public class distance{
	
	public static void main(String[] args) throws Exception{
		
	}
	
	public static double getDistance_cone(HueSat a, HueSat b){
		double x1, y1, z1;
		double x2, y2, z2;
		double toRad = Math.PI/180;
		double scale = 40;
		x1 = a.Sat * Math.cos(a.Hue * toRad);
		y1 = a.Sat * Math.sin(a.Hue * toRad);
		z1 = a.Val/scale;
		y1 = y1/(3*z1);
		
		x2 = b.Sat * Math.cos(b.Hue * toRad);
		y2 = b.Sat * Math.sin(b.Hue * toRad);
		z2 = b.Val/scale;
		y2 = y2/(3*z2);
		
		return Math.sqrt(Math.pow((x1 -x2), 2) + Math.pow((y1 -y2), 2) + Math.pow((z1 -z2), 2));
	}
	
	public static double getDistance_cyl(HueSat a, HueSat b){
		double x1, y1, z1;
		double x2, y2, z2;
		double toRad = Math.PI/180;
		x1 = a.Sat * Math.cos(a.Hue * toRad);
		y1 = a.Sat * Math.sin(a.Hue * toRad);
		z1 = a.Val;
		
		x2 = b.Sat * Math.cos(b.Hue * toRad);
		y2 = b.Sat * Math.sin(b.Hue * toRad);
		z2 = b.Val;
		
		return Math.sqrt(Math.pow((x1 -x2), 2) + Math.pow((y1 -y2), 2) + Math.pow((z1 -z2), 2));
	}
	
	public static double getDistance_cir(HueSat a, HueSat b){
		double x1, y1, z1;
		double x2, y2, z2;
		double toRad = Math.PI/180;
		double phi;
		
		if(a.Val == 43)	phi = 60;
		else if (a.Val == 128) phi = 30;
		else	phi = 0;
		x1 = a.Sat* Math.sin(a.Hue * toRad) *Math.cos(phi *toRad); 
		y1 = a.Sat* Math.sin(a.Hue * toRad) *Math.sin(phi *toRad);
		z1 = a.Sat* Math.cos(a.Hue);
		
		if(b.Val == 43)	phi = 60;
		else if (b.Val <= 128) phi = 30;
		else	phi = 0;
		x2 = b.Sat* Math.sin(b.Hue * toRad) *Math.cos(phi *toRad); 
		y2 = b.Sat* Math.sin(b.Hue * toRad) *Math.sin(phi *toRad);
		z2 = b.Sat* Math.cos(b.Hue);
		
		return Math.sqrt(Math.pow((x1 -x2), 2) + Math.pow((y1 -y2), 2) + Math.pow((z1 -z2), 2));
	}
}
