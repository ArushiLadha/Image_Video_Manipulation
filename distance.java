import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.*;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_highgui;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;
import com.googlecode.javacv.cpp.opencv_highgui.CvVideoWriter;
import com.googlecode.javacv.cpp.opencv_imgproc;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

public class distance{
	
	public static void main(String[] args) throws Exception{
		
	}
	
	public static double getDistance_cyl(HueSat a, HueSat b){
		double x1, y1, z1;
		double x2, y2, z2;
		double toRad = Math.PI/180;
		double scale = 40;
		x1 = a.Sat * Math.cos(a.Hue * toRad);
		y1 = a.Sat * Math.sin(a.Hue * toRad);
		z1 = a.Val/scale;
		
		x2 = b.Sat * Math.cos(b.Hue * toRad);
		y2 = b.Sat * Math.sin(b.Hue * toRad);
		z2 = b.Val/scale;
		
		return Math.sqrt(Math.pow((x1 -x2), 2) + Math.pow((y1 -y2), 2) + Math.pow((z1 -z2), 2));
	}
	
	public static double getDistance_cir(HueSat a, HueSat b){
		double x1, y1, z1;
		double x2, y2, z2;
		double toRad = Math.PI/180;
		double phi;
		
		if(a.Val <= 85)	phi = 60;
		else if (a.Val <= 170) phi = 30;
		else	phi = 0;
		x1 = a.Sat* Math.sin(a.Hue * toRad) *Math.cos(phi *toRad); 
		y1 = a.Sat* Math.sin(a.Hue * toRad) *Math.sin(phi *toRad);
		z1 = a.Sat* Math.cos(a.Hue);
		
		if(b.Val <= 85)	phi = 60;
		else if (b.Val <= 170) phi = 30;
		else	phi = 0;
		x2 = b.Sat* Math.sin(b.Hue * toRad) *Math.cos(phi *toRad); 
		y2 = b.Sat* Math.sin(b.Hue * toRad) *Math.sin(phi *toRad);
		z2 = b.Sat* Math.cos(b.Hue);
		
		return Math.sqrt(Math.pow((x1 -x2), 2) + Math.pow((y1 -y2), 2) + Math.pow((z1 -z2), 2));
	}
}
