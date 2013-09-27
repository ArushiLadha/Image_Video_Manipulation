package slashimages;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

public class FaceDetection{
	public static final String XML_FILE_FRONT = "/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml";
//	public static final String XML_FILE = "haarcascade_profileface.xml";
	
	private static IplImage convertByteArrayToImage(byte[] b) throws IOException{
		InputStream in = new ByteArrayInputStream(b);
		BufferedImage bI = ImageIO.read(in);
		IplImage I = IplImage.createFrom(bI);
		return I;
	}
	
	private static byte[] convertImageToByteArray(IplImage I) throws IOException{
		byte[] b;
		BufferedImage buffer = I.getBufferedImage();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(buffer, "jpg", baos);
		b = baos.toByteArray();
		return b;
	}
	
	private static class MyComparator implements Comparator<Vector<Integer> >{

		@Override
		public int compare(Vector<Integer> arg0, Vector<Integer> arg1) {
			if ((arg0.get(2) - arg0.get(0)) > (arg1.get(2) - arg1.get(0)))
				return -1;
			else
				return 1;
		}
	}
	
	private static int countFaces(IplImage I){
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(XML_FILE_FRONT));
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(I, cascade, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
		cvClearMemStorage(storage);
		int total_Faces = sign.total();
		return total_Faces;
	}
	
	public static Vector<Vector<Integer> > detectAllFaces(byte[] b) throws IOException{
		int match = 3;
		Vector<Vector<Integer> > Coordinates = new Vector<Vector<Integer> >();
		IplImage src = convertByteArrayToImage(b);
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(XML_FILE_FRONT));
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(src, cascade, storage, 1.1, match, CV_HAAR_DO_CANNY_PRUNING);
		cvClearMemStorage(storage);
		int total_Faces = sign.total();
		if (total_Faces == 0)
			return null;
		
		//-----Adding Side Face Detection From CleanCode----1--//
 
		for(int i = 0; i < total_Faces; i++){
			CvRect r = new CvRect(cvGetSeqElem(sign, i));
			Vector<Integer> V = new Vector<Integer>();
			V.add(r.x());
			V.add(r.y());
			V.add(r.width()+r.x());
			V.add(r.height()+r.y());
			Coordinates.add(V);
			//cvRectangle (src, cvPoint(r.x(), r.y()), cvPoint(r.width() + r.x(), r.height() + r.y()), CvScalar.RED, 2, CV_AA, 0);
			//-------Extracting Face of an image Form CleanCode---------------//
			}
		//-----Adding Side Face Detection From CleanCode----2--//
		Collections.sort(Coordinates, new MyComparator());
		return Coordinates;
	}	
	
	private static Vector<Integer> AllFace(Vector<Vector<Integer> > C){
		Vector<Integer> V = new Vector<Integer>();
		int i;
		int minX = C.get(0).get(0), minY = C.get(0).get(1);
		int maxX = -1, maxY = -1;
		for (i = 0; i < C.size(); i++){
			if (C.get(i).get(0) < minX)
				minX = C.get(i).get(0);
			if (C.get(i).get(2) > maxX)
				maxX = C.get(i).get(2);
			if (C.get(i).get(1) < minY)
				minY = C.get(i).get(1);
			if (C.get(i).get(3) > maxY)
				maxY = C.get(i).get(3);
		}
		V.add(minX);
		V.add(minY);
		V.add(maxX);
		V.add(maxY);
		return V;
	}
	
	public static Vector<Integer> boundingBox(byte[] b) throws IOException{
		Vector<Vector<Integer> > Coordinates = detectAllFaces(b);
		Vector<Integer> C = AllFace(Coordinates);
		return C;
	}
	
	private static Integer changeCordinates(double v, int flagIncDec, double alter, int width,  int height){
		int x = (int)alter;
		if(flagIncDec == 0){
			int k = (int) (v - x);
			if (k >= 0)
				return k;
			return 0;
		}else if (flagIncDec == 1){
			int k = (int) (v + x);
			if (k < width)
				return k;
			else 
				return width;
		}else{
			int k = (int) (v + x);
			if (k < height)
				return k;
			else
				return height;
		}
	}
	
	private static Vector<Integer> biggerFaceImage(Vector<Integer> V, double X, int width, int height){
		if (X == 0)
			return V;
		Vector<Integer> NewCordinates = new Vector<Integer>();
		double side = V.get(2) - V.get(0);
		double area = side * side;
		double requiredSide = Math.sqrt(area + ((X/100)*area));
		double change = requiredSide - side;
		NewCordinates.add(changeCordinates(V.get(0), 0, change/2, width, height));
		NewCordinates.add(changeCordinates(V.get(1), 0, change/2, width, height));
		NewCordinates.add(changeCordinates(V.get(2), 1, change/2, width, height));
		NewCordinates.add(changeCordinates(V.get(3), 2, change/2, width, height));
		return NewCordinates;
	}
	
	private static Vector<Integer> getMostSignificantFace(byte[] Is) throws IOException{
		Vector<Vector<Integer>> Vi = detectAllFaces(Is);
		if (Vi == null)
			return null;
		Vector<Integer> V = Vi.get(0);
		return V;
	}
	
	private static IplImage SubImage(IplImage image, int x, int y, int width, int height){
		IplImage result;
		opencv_core.cvSetImageROI( image, opencv_core.cvRect( x, y, width, height ));
		result = opencv_core.cvCreateImage( opencv_core.cvSize(width, height), image.depth(), image.nChannels() );
		opencv_core.cvCopy(image,result, null);
		//result = image.clone();
		opencv_core.cvResetImageROI(image);
		return result;
	}
	
	public static byte[] significantFace(byte[] Is, double X) throws IOException{
		IplImage I = convertByteArrayToImage(Is);
		int height = I.height();
		int width = I.width();
		Vector<Integer> V = getMostSignificantFace(Is);
		if (V == null)
			return null;
		
		Vector<Integer> N = biggerFaceImage(V, X, width, height);
		IplImage S = SubImage(I, N.get(0), N.get(1), N.get(2) - N.get(0), N.get(3) - N.get(1));
		//---------------Just To Display The Rectangles From CleanCode----------------//
		return convertImageToByteArray(S);
	}
	
	public static byte[] thumbnail(byte[] Is, Map<String, String> M) throws Exception{
		if (M.containsKey("w") && M.containsKey("h"))
			return thumbnailFinalPadding(Is, Integer.parseInt(M.get("w")), Integer.parseInt(M.get("h")));
		else if  (M.containsKey("w")){
			IplImage I = convertByteArrayToImage(Is);
			double aspectRatio = I.width()/I.height();
			int height = (int)(aspectRatio/Integer.parseInt(M.get("w")));
			return thumbnailFinal(Is, Integer.parseInt(M.get("w")), height);
		}else if (M.containsKey("h")){
			IplImage I = convertByteArrayToImage(Is);
			double aspectRatio = I.width()/I.height();
			int width = (int)(aspectRatio*Integer.parseInt(M.get("h")));
			return thumbnailFinal(Is, width, Integer.parseInt(M.get("h")));
		}else
			return null;
	}
	
	private static byte[] thumbnailFinal(byte[] Is, int newwidth, int newheight) throws IOException{
		IplImage I = convertByteArrayToImage(Is);
		int height = I.height();
		int width = I.width();
		int newTop = 0, newBottom = height;
		int newLeft = 0, newRight= width;
		int intoflagwidth = 0;
		int intoflagheight = 0;
		int facewidth = 0, faceheight = 0;
		
//........Case when the entire image is smaller than the thumbnail size..............//		
		if (width < newwidth){
			//....scale the entire image in width....
			BufferedImage bI = I.getBufferedImage();
			BufferedImage sI = Scalr.resize(bI, Scalr.Mode.FIT_EXACT, newwidth, height);
			sI = Scalr.pad(bI, 10);
			I = IplImage.createFrom(sI);
			width = newwidth;
			intoflagwidth = 1;
		}
		if (height < newheight){
			//....scale the entire image in height..
			BufferedImage bI = I.getBufferedImage();
			BufferedImage sI = Scalr.resize(bI, Scalr.Mode.FIT_EXACT, width, newheight);
			I = IplImage.createFrom(sI);
			height = newheight;
			intoflagheight = 1;
		}
		Vector<Integer> V = new Vector<Integer>();
		
		if (intoflagwidth != 1 || intoflagheight != 1){
			Is = convertImageToByteArray(I);
			V = getMostSignificantFace(Is);
			if (V == null)
				return null;
			
			newLeft = V.get(0);
			newTop = V.get(1);
			newRight = V.get(2);
			newBottom = V.get(3);
			
			facewidth = V.get(2) - V.get(0);
			faceheight = V.get(3) - V.get(1);
		}
		
//........Case when the face image is smaller than the thumbnail size...............//
		//....approx(30% on top, 70 % on bottom, 50 % on left and 50 % on right expansion)...//
		if (newheight >= faceheight && intoflagheight == 0){
			double topPercentage = 0.4;
			double diffHeight = newheight - faceheight;
			int top = (int)(diffHeight * topPercentage);
			int bottom = (int)(diffHeight - top);
			if ((IsFeasible(V.get(1), top, width, height, 0) == -1) && (IsFeasible(V.get(3), bottom, width, height, 2) == -1)){
				newTop = changeCordinates(V.get(1), 0, top, width, height);
				newBottom = changeCordinates(V.get(3), 2, bottom, width, height);
			}else if ((IsFeasible(V.get(1), top, width, height, 0) == -1)){
				int addToTop = IsFeasible(V.get(3), bottom, width, height, 2);
				newTop = changeCordinates(V.get(1), 0, top+addToTop, width, height);
				newBottom = changeCordinates(V.get(3), 2, height-V.get(3), width, height);
			}else{
				int addToBottom = IsFeasible(V.get(1), top, width, height, 0);
				newTop = changeCordinates(V.get(1), 0, V.get(1), width, height);
				newBottom = changeCordinates(V.get(3), 2, bottom+addToBottom, width, height);
			}
			intoflagheight = 1;
			I = SubImage(I, 0, newTop, I.width(), newBottom-newTop);
		}
		
		if (newwidth >= facewidth && intoflagwidth == 0){	
			double diffWidth = newwidth - facewidth;
			int left = (int)(diffWidth/2);
			int right = (int)(diffWidth - left);
			if ((IsFeasible(V.get(0), left, width, height, 0) == -1) && (IsFeasible(V.get(2), right, width, height, 1) == -1)){			
				newLeft = changeCordinates(V.get(0), 0, left, width, height);
				newRight = changeCordinates(V.get(2), 1, right, width, height);
			}else if ((IsFeasible(V.get(0), left, width, height, 0) == -1)){
				int addToLeft = IsFeasible(V.get(2), right, width, height, 1);
				newLeft = changeCordinates(V.get(0), 0, left+addToLeft, width, height);
				newRight = changeCordinates(V.get(2), 1, V.get(2), width, height);
			}else{
				int addToRight = IsFeasible(V.get(0), left, width, height, 0);
				newLeft = changeCordinates(V.get(0), 0, width-V.get(2), width, height);
				newRight = changeCordinates(V.get(2), 1, right+addToRight, width, height);
			}
			intoflagwidth = 1;
			I = SubImage(I, newLeft, 0, newRight-newLeft, I.height());
		}
		
		if (intoflagwidth == 0 && intoflagheight == 0){
			IplImage temp = SubImage(I, V.get(0), V.get(1), facewidth, faceheight);
			if (facewidth > newwidth){
				BufferedImage bI = temp.getBufferedImage();
				bI = Scalr.resize(bI,Scalr.Mode.FIT_EXACT, newwidth, temp.height());
				I = IplImage.createFrom(bI);
				intoflagwidth = 1;
			}
			
			if (faceheight > newheight){
				BufferedImage bI = I.getBufferedImage();
				bI = Scalr.resize(bI, Scalr.Mode.FIT_EXACT, I.width(), newheight);
				I = IplImage.createFrom(bI);
				intoflagheight = 1;
			}
		}
			
		if (facewidth > newwidth && intoflagwidth == 0){
			BufferedImage bI = I.getBufferedImage();
			bI = Scalr.resize(bI,Scalr.Mode.FIT_EXACT, newwidth, I.height());
			I = IplImage.createFrom(bI);
		}
		
		if (faceheight > newheight && intoflagheight == 0){
			BufferedImage bI = I.getBufferedImage();
			bI = Scalr.resize(bI, Scalr.Mode.FIT_EXACT, I.width(), newheight);
			I = IplImage.createFrom(bI);
		}
		
		cvSaveImage("IntermediateResult.jpg", I);
		
		
		BufferedImage originalImage=ImageIO.read(new File("IntermediateResult.jpg"));
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ImageIO.write(originalImage, "jpg", baos );
		byte[] imageInByte=baos.toByteArray();
		return imageInByte;
		
	}
	
	private static Integer IsFeasible(int value, int alter, int width, int height, int flag){
		if (flag == 0){  //...top and left...//
			if ((value - alter) >= 0)
				return -1;
			else
				return alter-value;
		}else if (flag  == 1){ //...right..//
			if ((value + alter) < width)
				return -1;
			else
				return value+alter-width;
		}else{ //....bottom...//
			if ((value + alter) < height)
				return -1;
			else
				return value+alter-height;
		}
	}
	
	public static int orientation(byte[] Is) throws IOException{
		int angle = 0;
		IplImage I = convertByteArrayToImage(Is);
		int count = countFaces(I);
		BufferedImage bI = I.getBufferedImage();
		bI = Scalr.rotate(bI, Scalr.Rotation.CW_90);
		I = IplImage.createFrom(bI);
		int count90 = countFaces(I);
		if (count90 > count){
			angle = 90;
			count = count90;
		}
		bI = Scalr.rotate(bI, Scalr.Rotation.CW_90);
		I = IplImage.createFrom(bI);
		int count180 = countFaces(I);
		if (count180 > count){
			angle = 180;
			count = count180;
		}
		bI = Scalr.rotate(bI, Scalr.Rotation.CW_90);
		I = IplImage.createFrom(bI);
		int count270 = countFaces(I);
		if (count270 > count){
			angle = 270;
			count = count270;
		}
		return angle;
	}
	
	private static byte[] thumbnailFinalPadding(byte[] Is, int framewidth, int frameheight) throws Exception{
		IplImage I = convertByteArrayToImage(Is);
		int width = I.width();
		int height = I.height();
		Vector<Integer> V = getMostSignificantFace(Is);
		if (V.isEmpty())
			return null;
		int facewidth = V.get(2) - V.get(0);
		int faceheight = V.get(3) - V.get(1);
		int newLeft = 0, newRight = width;
		int newTop = 0, newBottom = height;
		if (framewidth >= width && frameheight >= height){
			int w = (framewidth - width)/2;
			int h = (frameheight - height)/2;
			BufferedImage bI = I.getBufferedImage();
			bI = padding(bI, w, h);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bI, "jpg", baos);
			byte[] b = baos.toByteArray();
			return b;
			
		}else if (frameheight < height && framewidth >= width) {
			//...padding in width in the final image..../....not in height.....//
			if (frameheight >= faceheight){
				double topPercentage = 0.4;
				double diffHeight = frameheight - faceheight;
				int top = (int)(diffHeight * topPercentage);
				int bottom = (int)(diffHeight - top);
				if ((IsFeasible(V.get(1), top, width, height, 0) == -1) && (IsFeasible(V.get(3), bottom, width, height, 2) == -1)){
					newTop = changeCordinates(V.get(1), 0, top, width, height);
					newBottom = changeCordinates(V.get(3), 2, bottom, width, height);
				}else if ((IsFeasible(V.get(1), top, width, height, 0) == -1)){
					int addToTop = IsFeasible(V.get(3), bottom, width, height, 2);
					newTop = changeCordinates(V.get(1), 0, top+addToTop, width, height);
					newBottom = changeCordinates(V.get(3), 2, height-V.get(3), width, height);
				}else{
					int addToBottom = IsFeasible(V.get(1), top, width, height, 0);
					newTop = changeCordinates(V.get(1), 0, V.get(1), width, height);
					newBottom = changeCordinates(V.get(3), 2, bottom+addToBottom, width, height);
				}
				I = SubImage(I, 0, newTop, width, newBottom-newTop);
				int w = (framewidth - width)/2;
				BufferedImage bI = I.getBufferedImage();
				bI = padding(bI, w, 0);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bI, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
				
			}else{
				//....scale down the face.. and pad in width...//
				int scaleheight = frameheight*height/faceheight;
				BufferedImage bI = I.getBufferedImage();
				bI = Scalr.resize(bI, Scalr.Mode.FIT_TO_HEIGHT, scaleheight);
				I = IplImage.createFrom(bI);
				int newtop = V.get(1)*I.height()/height;
				int newbottom = V.get(3)*I.height()/height;
				I = SubImage(I, 0, newtop, I.width(), newbottom-newtop);
				BufferedImage sI = I.getBufferedImage();
				I = IplImage.createFrom(sI);
				int w = (framewidth - I.width())/2;
				sI = padding(sI, w, 0);
				I = IplImage.createFrom(sI);
				
				cvSaveImage("IntermediateResult.jpg", I);
				BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(Im, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
			}
		}else if (frameheight >= height && framewidth < width) {
			if (framewidth >= facewidth){
				double diffWidth = framewidth - facewidth;
				int left = (int)(diffWidth/2);
				int right = (int)(diffWidth - left);
				if ((IsFeasible(V.get(0), left, width, height, 0) == -1) && (IsFeasible(V.get(2), right, width, height, 1) == -1)){			
					newLeft = changeCordinates(V.get(0), 0, left, width, height);
					newRight = changeCordinates(V.get(2), 1, right, width, height);
				}else if ((IsFeasible(V.get(0), left, width, height, 0) == -1)){
					int addToLeft = IsFeasible(V.get(2), right, width, height, 1);
					newLeft = changeCordinates(V.get(0), 0, left+addToLeft, width, height);
					newRight = changeCordinates(V.get(2), 1, V.get(2), width, height);
				}else{
					int addToRight = IsFeasible(V.get(0), left, width, height, 0);
					newLeft = changeCordinates(V.get(0), 0, width-V.get(2), width, height);
					newRight = changeCordinates(V.get(2), 1, right+addToRight, width, height);
				}
				I = SubImage(I, newLeft, 0, newRight-newLeft, height);
				int h = (frameheight - height)/2;
				BufferedImage bI = I.getBufferedImage();
				bI = padding(bI, 0, h);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bI, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
				
			}else{
				//....scale down the face.. and pad in width...//
				int scalewidth = framewidth*width/facewidth;
				BufferedImage bI = I.getBufferedImage();
				bI = Scalr.resize(bI, Scalr.Mode.FIT_TO_WIDTH, scalewidth);
				I = IplImage.createFrom(bI);
				int newleft = V.get(0)*I.width()/width;
				int newright = V.get(2)*I.width()/width;
				I = SubImage(I, newleft, 0, newright-newleft, I.height());
				BufferedImage sI = I.getBufferedImage();
				I = IplImage.createFrom(sI);
				int h = (frameheight - I.height())/2;
				sI = padding(sI, 0, h);
				I = IplImage.createFrom(sI);
				
				cvSaveImage("IntermediateResult.jpg", I);
				BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(Im, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
			}
		}else if(frameheight >= faceheight && framewidth >= facewidth){
			double topPercentage = 0.4;
			double diffHeight = frameheight - faceheight;
			int top = (int)(diffHeight * topPercentage);
			int bottom = (int)(diffHeight - top);
			if ((IsFeasible(V.get(1), top, width, height, 0) == -1) && (IsFeasible(V.get(3), bottom, width, height, 2) == -1)){
				newTop = changeCordinates(V.get(1), 0, top, width, height);
				newBottom = changeCordinates(V.get(3), 2, bottom, width, height);
			}else if ((IsFeasible(V.get(1), top, width, height, 0) == -1)){
				int addToTop = IsFeasible(V.get(3), bottom, width, height, 2);
				newTop = changeCordinates(V.get(1), 0, top+addToTop, width, height);
				newBottom = changeCordinates(V.get(3), 2, height-V.get(3), width, height);
			}else{
				int addToBottom = IsFeasible(V.get(1), top, width, height, 0);
				newTop = changeCordinates(V.get(1), 0, V.get(1), width, height);
				newBottom = changeCordinates(V.get(3), 2, bottom+addToBottom, width, height);
			}
			I = SubImage(I, 0, newTop, width, newBottom-newTop);
			
			double diffWidth = framewidth - facewidth;
			int left = (int)(diffWidth/2);
			int right = (int)(diffWidth - left);
			if ((IsFeasible(V.get(0), left, width, height, 0) == -1) && (IsFeasible(V.get(2), right, width, height, 1) == -1)){			
				newLeft = changeCordinates(V.get(0), 0, left, width, height);
				newRight = changeCordinates(V.get(2), 1, right, width, height);
			}else if ((IsFeasible(V.get(0), left, width, height, 0) == -1)){
				int addToLeft = IsFeasible(V.get(2), right, width, height, 1);
				newLeft = changeCordinates(V.get(0), 0, left+addToLeft, width, height);
				newRight = changeCordinates(V.get(2), 1, V.get(2), width, height);
			}else{
				int addToRight = IsFeasible(V.get(0), left, width, height, 0);
				newLeft = changeCordinates(V.get(0), 0, width-V.get(2), width, height);
				newRight = changeCordinates(V.get(2), 1, right+addToRight, width, height);
			}
			I = SubImage(I, newLeft, 0, newRight-newLeft, I.height());
			cvSaveImage("IntermediateResult.jpg", I);
			BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(Im, "jpg", baos);
			byte[] b = baos.toByteArray();
			return b;
		}else if(framewidth < facewidth && frameheight < faceheight){
			I = SubImage(I, V.get(0), V.get(1), facewidth, faceheight);
			BufferedImage bI = I.getBufferedImage();
			if (framewidth > frameheight){
				bI = Scalr.resize(bI, Scalr.Mode.FIT_TO_HEIGHT, frameheight);
				int w = (framewidth - bI.getWidth())/2;
				bI = padding(bI, w, 0);
			}
			else if(framewidth < frameheight){
				bI = Scalr.resize(bI, Scalr.Mode.FIT_TO_HEIGHT, framewidth);
				int h = (frameheight - bI.getHeight())/2;
				bI = padding(bI, 0, h);
			}else{
				bI = Scalr.resize(bI, framewidth);
			}
			I = IplImage.createFrom(bI);
			cvSaveImage("IntermediateResult.jpg", I);
			BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(Im, "jpg", baos);
			byte[] b = baos.toByteArray();
			return b;
		}else if (framewidth >= facewidth && frameheight < faceheight){
			//....scale down in the ratio so that face height matches frame height....//
			int scaleheight = frameheight*height/faceheight;
			BufferedImage bI = I.getBufferedImage();
			bI = Scalr.resize(bI, Scalr.Mode.FIT_TO_HEIGHT, scaleheight);
			int newtop = V.get(1)*bI.getHeight()/height;
			int newbottom = V.get(3)*bI.getHeight()/height;
			I = IplImage.createFrom(bI);
			I = SubImage(I, 0, newtop, I.width(), newbottom-newtop);
			
			if (framewidth >= I.width()){
				int w = (framewidth - I.width())/2;
				bI = I.getBufferedImage();
				bI = padding(bI, w, 0);
				I = IplImage.createFrom(bI);
				cvSaveImage("IntermediateResult.jpg", I);
				BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(Im, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
			}else{
				int newleft = V.get(0)*bI.getWidth()/width;
				int newright = V.get(2)*bI.getWidth()/width;
				width = bI.getWidth();
				height = bI.getHeight();
				facewidth = newright - newleft;
				double diffWidth = framewidth - facewidth;
				int left = (int)(diffWidth/2);
				int right = (int)(diffWidth - left);
				if ((IsFeasible(newleft, left, width, height, 0) == -1) && (IsFeasible(newright, right, width, height, 1) == -1)){			
					newLeft = changeCordinates(newleft, 0, left, width, height);
					newRight = changeCordinates(newright, 1, right, width, height);
				}else if ((IsFeasible(newleft, left, width, height, 0) == -1)){
					int addToLeft = IsFeasible(newright, right, width, height, 1);
					newLeft = changeCordinates(newleft, 0, left+addToLeft, width, height);
					newRight = changeCordinates(newright, 1, newright, width, height);
				}else{
					int addToRight = IsFeasible(newleft, left, width, height, 0);
					newLeft = changeCordinates(newleft, 0, width-newright, width, height);
					newRight = changeCordinates(newright, 1, right+addToRight, width, height);
				}
				I = SubImage(I, newLeft, 0, newRight-newLeft, I.height());
				cvSaveImage("IntermediateResult.jpg", I);
				BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(Im, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
			}
		}else{
			int scalewidth = framewidth*width/facewidth;
			BufferedImage bI = I.getBufferedImage();
			bI = Scalr.resize(bI, Scalr.Mode.FIT_TO_WIDTH, scalewidth);
			int newleft = V.get(0)*bI.getWidth()/width;
			int newright = V.get(2)*bI.getWidth()/width;
			I = IplImage.createFrom(bI);
			I = SubImage(I, newleft, 0, newright-newleft, I.height());
			
			if (frameheight >= I.height()){
				int h = (frameheight - I.height())/2;
				bI = I.getBufferedImage();
				bI = padding(bI, 0, h);
				I = IplImage.createFrom(bI);
				cvSaveImage("IntermediateResult.jpg", I);
				BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(Im, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
			}else{
				int newtop = V.get(1)*bI.getHeight()/height;
				int newbottom = V.get(3)*bI.getHeight()/height;
				width = bI.getWidth();
				height = bI.getHeight();
				faceheight = newbottom - newtop;
				double diffHeight = frameheight - faceheight;
				double topPercentage = 0.4;
				int top = (int)(diffHeight * topPercentage);
				int bottom = (int)(diffHeight - top);
				if ((IsFeasible(newtop, top, width, height, 0) == -1) && (IsFeasible(newbottom, bottom, width, height, 2) == -1)){
					newTop = changeCordinates(newtop, 0, top, width, height);
					newBottom = changeCordinates(newbottom, 2, bottom, width, height);
				}else if ((IsFeasible(newtop, top, width, height, 0) == -1)){
					int addToTop = IsFeasible(newbottom, bottom, width, height, 2);
					newTop = changeCordinates(newtop, 0, top+addToTop, width, height);
					newBottom = changeCordinates(newbottom, 2, height-newbottom, width, height);
				}else{
					int addToBottom = IsFeasible(newtop, top, width, height, 0);
					newTop = changeCordinates(newtop, 0, newtop, width, height);
					newBottom = changeCordinates(newbottom, 2, bottom+addToBottom, width, height);
				}
				I = SubImage(I, 0, newTop, width, newBottom-newTop);
				cvSaveImage("IntermediateResult.jpg", I);
				BufferedImage Im=ImageIO.read(new File("IntermediateResult.jpg"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(Im, "jpg", baos);
				byte[] b = baos.toByteArray();
				return b;
			}
		}
	}
	
	private static BufferedImage padding(BufferedImage image, int w, int h){
		BufferedImage newImage = new BufferedImage(image.getWidth()+2*w, image.getHeight()+2*h, image.getType());
		Graphics g = newImage.getGraphics();
		//g.setColor(Color.white);
		//g.fillRect(0,0,image.getWidth()+2*w,image.getHeight()+2*h);
		g.drawImage(image, w, h, null);
		g.dispose();
		return newImage;
	}
}