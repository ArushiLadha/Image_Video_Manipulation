package matching;
import java.io.IOException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.*;

public class contentaware{
		
	private static Mat gradient(Mat I){		
		Mat F = new Mat();
		Size ksize = new Size(3, 3);
		double sigmaX = 0, sigmaY = 0;
		Imgproc.GaussianBlur(I, F, ksize, sigmaX, sigmaY);
		Imgproc.cvtColor(F, F, Imgproc.COLOR_RGB2GRAY);
		Mat Fx = new Mat();
		Mat Fy = new Mat();
		Imgproc.Sobel(F, Fx, -1, 0, 1);
		Imgproc.Sobel(F, Fy, -1, 1, 0);

		Core.convertScaleAbs(Fx, Fx);
		Core.convertScaleAbs(Fy, Fy);
		
		Core.addWeighted(Fx, 0.5, Fy, 0.5, 0, F);
		return F;
	}
	
	private static int check_increment(Mat I, double scalefactor, int s1, int s2, int olds1, int olds2, double cord1, double cord2){
		double sums1 = 0, sums2 = 0;
		int news1 = (int)(olds1*scalefactor);
		if (cord1+s1+news1 < I.height()){
			Mat fors1 = I.submat((int)cord1+s1, (int)cord1+s1+news1, (int)cord2, (int)cord2+s2);
			sums1 = Core.sumElems(fors1).val[0];
		}
		int news2 = (int)(olds2*scalefactor);
		if (cord2+s2+news2 < I.width()){
			Mat fors2 = I.submat((int)cord1, (int)cord1+s1, (int)cord2+s2, (int)cord2+s2+news2);
			sums2 = Core.sumElems(fors2).val[0];
		}
		if (sums1 > sums2)
			return 1;
		else if (sums1 < sums2)
			return 2;
		else if (sums1 == 0 && sums2 == 0){
			if ((s1+news1 >= I.height()) && (s2+news2 >= I.width()))
				return 0;
			if (I.width() > I.height()){
				if (s1+news1 >= I.height())
					return 2;
				else
					return 1;
			}else{
				if (s2+news2 >= I.width())
					return 1;
				else
					return 2;
			}
		}
		else if (sums1 == sums2)
			return 3;
		return 0;
	}
	
	private static double[] findCheck(Mat I, int s1, int s2, double scalefactor, int threshold, int preserveAspectRatio){
		double[] cord = new double[5];
		int olds1 = s1;
		int olds2 = s2;
		cord[0] = 0;
		cord[1] = 0;
		cord[2] = 0;
		cord[3] = 0;
		cord[4] = 0;
		while(cord[2] < threshold && s1 <= I.height() && s2 <= I.width()){
			//System.out.println(s2+", "+s1);
			cord = check(I, s1, s2);
			cord[3] = s1;
			cord[4] = s2;
			//------Preserving aspect ratio---------//
			if (preserveAspectRatio == 1){
				//System.out.println("Checking for width and height: "+s2+", "+s1);
				s1 = (int) (s1 + (scalefactor*olds1));
				s2 = (int) (s2 + (scalefactor*olds2));
				if (cord[2] < threshold && (s2 > I.width() || s1 > I.height())){
					cord[2] = -1;
				}
			}else{
			//------ Not perserving aspect ratio-----//
				//System.out.println(cord[0]+", "+cord[1]);
				int cI = check_increment(I, scalefactor, s1, s2, olds1, olds2, cord[0], cord[1]);
				if (cI == 1 || cI == 3)
					s1 = (int) (s1 + (scalefactor*olds1));
				else if (cI == 2)
					s2 = (int) (s2 + (scalefactor*olds2));
				if (cI == 0){
					cord[2] = -1;
					s1 = I.height()+1; //....to break loop...//
				}
			}
		}
		
		return cord;
	}
	
	private static double[] check(Mat F,int s1, int s2){
		double totalSum = 0, tmpSum = 0, max = 0;
		double cord[] = new double[5];
		Scalar sum = Core.sumElems(F);
		totalSum = sum.val[0];
		int i, j, locx = 0, locy = 0;
		for (i = 0; i <= F.height()-s1; i++){
			//System.out.println(i + ", " +  F.height() + ", " + s1);
			for (j = 0; j <= F.width()-s2; j++){
				Mat tmp = F.submat(i, i+s1, j, j+s2);
				tmpSum = Core.sumElems(tmp).val[0];
				if (tmpSum > max){
					max = tmpSum;
					locx = i;
					locy = j;
				}
			}
		}
		double percentageMatch = max*100/totalSum;
		//System.out.println("Accuracy: "+percentageMatch);
		cord[0] = locx;
		cord[1] = locy;
		cord[2] = percentageMatch;
		return cord;
	}

	public static FaceBox contentAware(Mat I) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		int s1 = (int)(((double)I.width())/2);
		int s2 = (int)((double)I.height()/2);
		double scalefactor = 0.12;
		int threshold = 90;
		int preserveAspectRatio = 0;
		Mat F = contentaware.gradient(I);
		double[] cord = findCheck(F, s2, s1, scalefactor, threshold, preserveAspectRatio);
		if (cord[2] == -1 || cord[2] == 0)
			return null;
		else{
			FaceBox C = new FaceBox();
			C.setValues((int)cord[0], (int)(cord[0]+cord[3]), (int)cord[1], (int)(cord[1]+cord[4]));
			return C;
		}
	}
	
}
