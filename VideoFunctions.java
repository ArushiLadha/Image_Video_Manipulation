import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.opencv.core.Core;
import org.opencv.core.Mat;
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

public class VideoFunctions{
	
	public static void main(String[] args) throws Exception{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//clip("paperman.mp4", 10, 20, "out.avi");
		//nShots("joined.avi", 20, 500, 400, true);
		//scale("MOV02481.MPG", 400, 200, "red", "clipped.avi");
		//Map<String, String> mdata = metadata("joker.gif");
		//System.out.println(mdata);
		//audioOverlay("joined.avi", "alarm.mp3", true);
		//videoInsert("MOV02481.MPG", "paperman.mp4", 10 ,"joined.avi");
		gifOverlay("paperman.mp4", "banana.gif", "out.avi", 200, 30, 2);
		//StaticTextOverlay("paperman.mp4", "textman.avi", "Text-Overlay!! Oh yeah! :D Oh yeah! :D", 400, 330, "left");
		
		//videoOverlay("joined.avi", "paperman.mp4", "paperman2.avi", 300, 200);
		//subtitles("MOV02481.MPG", "sub.srt", "movie.avi");
	}
	
	private static int max(int a, int b){
		if(a > b)
			return a;
		return b;
	}
	
	public static CvMat overlayImage(CvMat background, CvMat foreground, CvPoint location)
	{
		CvMat output;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		output = background.clone();
		
		for(int y = max(location.y(), 0); y < background.rows(); ++y){
		  
		    int fY = y - location.y(); // because of the translation

		    if(fY >= foreground.rows()){
		    	//System.out.println("OverFlow");
		      break;
		    }

		    for(int x = max(location.x(), 0); x < background.cols(); ++x)
		    {
		      int fX = x - location.x(); // because of the translation.

		      if(fX >= foreground.cols()){
		    	  //System.out.println("OverFlow");
		        break;
		      }

		      double opacity = ((double)foreground.get(fY * foreground.step() + fX * foreground.channels() + 3)) / 255.;

		      for(int c = 0; opacity > 0 && c < output.channels(); ++c)
		      {
		        char foregroundPx = (char) foreground.get(fY * foreground.step() + fX * foreground.channels() + c);
		        char backgroundPx = (char) background.get(y * background.step() + x * background.channels() + c);
		        output.put((y*output.step() + output.channels()*x + c), (backgroundPx * (1.-opacity) + foregroundPx * opacity));
		      }
		    }
	    }
		return output;
	}
	
	public static void gifOverlay(String videofile, String giffile, String outfile, int x, int y, int speed){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Map<String, String> vMap = metadata(videofile);
		
		String comm = "mkdir gif_frames";
		runCommand(comm);
		comm = "convert "+giffile+" -define png:color-type=6 ./gif_frames/over.png";
		runCommand(comm);
		int num_gif = new File("./gif_frames").list().length;
		System.out.println(num_gif);
		
		File file = new File("audio.mp3");
		file.delete();
		comm = "ffmpeg -i " + videofile + " -vn audio.mp3";
		runCommand(comm);
		
		int i_gif = 0;
		int j_speed;
		
		CvCapture capture = opencv_highgui.cvCreateFileCapture(videofile);
		IplImage grabbedImage = opencv_highgui.cvQueryFrame(capture);
		IplImage gifFrame;
		int fps = Integer.parseInt(vMap.get("fps"));
		
		String cvname = "temp"+outfile;
		CvVideoWriter writer = opencv_highgui.cvCreateVideoWriter(cvname, opencv_highgui.CV_FOURCC('M','J','P','G'), fps, cvSize(grabbedImage.width(), grabbedImage.height()), 1);
		while( (grabbedImage = opencv_highgui.cvQueryFrame(capture))  != null){
			String gif_name = "./gif_frames/over-" + i_gif%num_gif + ".png";
			gifFrame = opencv_highgui.cvLoadImage(gif_name, -1);
			i_gif++;
			
			j_speed = 0;			
			while(j_speed < speed){
				CvMat out = overlayImage(grabbedImage.asCvMat(), gifFrame.asCvMat(), cvPoint(x, y));
				opencv_highgui.cvWriteFrame(writer, out.asIplImage());
				
				System.out.println(i_gif + " " + j_speed);
								
				j_speed++;
				if(j_speed < speed){
					grabbedImage = opencv_highgui.cvQueryFrame(capture);
					if(grabbedImage == null)	break;					
				}
			}
		}
		System.out.println(cvname);
		comm = "ffmpeg -i audio.mp3 -i "+cvname+" -acodec copy -vcodec copy " + outfile;
		runCommand(comm);
		file = new File("temp"+outfile);
		file.delete();
		file = new File("audio.mp3");
		file.delete();
	}
	
	public static void subtitles(String videofile, String subs, String outfile){
		String comm = "mencoder "+ videofile +" -sub "+ subs +" -o "+ outfile +" -oac pcm -ovc lavc -lavcopts vbitrate=1200";
		runCommand(comm);
	}
	
	private static CvSize getVideoSize(String filename){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		CvCapture capture = opencv_highgui.cvCreateFileCapture(filename);
		IplImage grabbedImage;
	    //CanvasFrame frame = new CanvasFrame("Echo Demo", 1);
	    	    
		grabbedImage = opencv_highgui.cvQueryFrame(capture);
		opencv_highgui.cvReleaseCapture(capture);
		return grabbedImage.cvSize();		
	}
	
	public static void videoOverlay(String basefile, String overfile, String outfile, int x, int y) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	//----------------------------------fix dimensions of the overlay file-----------------------
		CvSize basesize = getVideoSize(basefile);
		System.out.println(basesize.width());
		CvSize oversize = getVideoSize(overfile);
		System.out.println(oversize);
		
		int xdim = oversize.width(), ydim = oversize.height(); 
		if(x + oversize.width() > basesize.width()){     //--------crop video along width
			xdim = basesize.width() - x;
		}
		
		if(y + oversize.height() > basesize.height()){   //--------crop video along length
			ydim = basesize.height() - y;
		}
		
		String comm = "ffmpeg -i " + overfile + " -vf \"crop=" + xdim +":"+ ydim+ ":0:0\" -an -qscale:v 6 video1.mpg";
		System.out.println(comm);
		File file = new File("video1.mpg");
		if(file.exists())	file.delete();
				
		FileWriter fstream = new FileWriter("run.sh"); 
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
		
	    comm = "chmod 755 run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		comm = "./run.sh";
		System.out.println(comm);
		runCommand(comm);
		
	//------------------------------------fix duration of the overlay file-------------------------
		double basetime = parseTime(metadata(basefile).get("duration"));
		double overtime = parseTime(metadata("video1.mpg").get("duration"));
		
		if(overtime < basetime){	//--------------have to loop the overlay file
			String temp = ""; 
			int loop = (int) (basetime/overtime);
			
			while(loop>0){
				System.out.println("-------------------> " + loop);
				temp = temp+ '|' + "video1.mpg";
				loop--;
			}
			
			comm = "ffmpeg -i \"concat:video1.mpg" + temp+"\" -c copy video2.mpg";
	    	System.out.println(comm);
	    	file = new File("video2.mpg");
	    	if(file.exists())	file.delete();
	    	
	    	fstream = new FileWriter("run.sh"); 
		    out = new BufferedWriter(fstream);
		    out.write(comm);
		    out.close();
			
		    comm = "chmod 755 run.sh";
			System.out.println(comm);
			runCommand(comm);
			
			comm = "./run.sh";
			System.out.println(comm);
			runCommand(comm);
		}
		
		System.out.println(basetime + "  " + overtime);
		
	//-----------------------------OVERLAY THE TWO VIDEOS--------------------------
		comm = "ffmpeg -i "+ basefile +" -i video2.mpg -acodec copy -filter_complex \"overlay="+x+":"+y+"\" -qscale:v 8 "+outfile;
		System.out.println(comm);
		file = new File(outfile);
		if(file.exists())	file.delete();
		
		fstream = new FileWriter("run.sh"); 
	    out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
		
	    comm = "chmod 755 run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		comm = "./run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		file = new File("video1.mpg");
		if(file.exists())	file.delete();
		
		file = new File("video2.mpg");
		if(file.exists())	file.delete();
	}
	
	private static Map<CvFont, CvPoint> findtextsize(int x, int y, String align, String text, CvSize imgsize){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    double hScale=1.5;
	    double vScale=1.5;
	    int    lineWidth=1;
	    int[] baseline = {0};
	    boolean flag = true;
	    CvSize textsize = new CvSize();
	    int startx = x, starty = y;
	    CvFont font;
	    do{
	    	font = new CvFont();
	    	cvInitFont(font,CV_FONT_HERSHEY_TRIPLEX, hScale,vScale,0,lineWidth,CV_AA);
	    	cvGetTextSize(text, font, textsize, baseline);
		
	    	if(align == "left"){
	    		startx = x;
	    		starty = y+textsize.height();			
	    	}	
	    	else if(align  ==  "right"){
	    		startx = x-textsize.width();
	    		starty = y+textsize.height();			
	    	}
	    	else if(align == "center"){
	    		startx = x-textsize.width()/2;
	    		starty = y+textsize.height();
	    	}
	    	if((startx + textsize.width() < imgsize.width()) && (starty < imgsize.height()) 
	    			&& (startx  > 0) && (starty - textsize.height() > 0))
	    		flag = false;
	    	
	    	if(((startx + textsize.width() >= imgsize.width()) || (startx  <= 0) || (starty >= imgsize.height()) || (starty - textsize.height()<= 0)) 
	    			&& (hScale - 0.01 > 0) && (vScale - 0.01 > 0)){
	    		vScale = vScale - 0.01;
	    		hScale = hScale - 0.01;
	    	}
	    	
		}while(flag);
	    
	    CvPoint P = new CvPoint(startx, starty);
	    Map<CvFont, CvPoint> M = new HashMap<CvFont, CvPoint>();
	    M.put(font, P);
	    return M;
	}
	
	public static void StaticTextOverlay(String filename, String outfile, String text, int x, int y, String align) throws com.googlecode.javacv.FrameRecorder.Exception{
		String comm;
		File file = new File("video1.avi");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i "+ filename+ " -an -vcodec copy video1.avi";
		runCommand(comm);
		file = new File("audio1.mp3");
		if(file.exists()) file.delete(); 
		comm = "ffmpeg -i "+ filename+ " -vn audio1.mp3";
		runCommand(comm);
		
		CvCapture capture = opencv_highgui.cvCreateFileCapture("video1.avi");
		IplImage grabbedImage;
	    //CanvasFrame frame = new CanvasFrame("Echo Demo", 1);
	    	    
		grabbedImage = opencv_highgui.cvQueryFrame(capture);
		int fps = Integer.parseInt(metadata("video1.avi").get("fps"));
		
		Map<CvFont, CvPoint> M = findtextsize(x, y, align, text, grabbedImage.cvSize());
		CvFont font = new CvFont();
		for (CvFont key : M.keySet()) {
		    System.out.println("Key = " + key);
		    font = key;
		}
		
		CvPoint P = M.get(font);
		
		CvVideoWriter videoWriter = opencv_highgui.cvCreateVideoWriter("out.avi", opencv_highgui.CV_FOURCC('M','J','P','G'), fps, cvSize(grabbedImage.width(), grabbedImage.height()), 1);
		
	    do {	    	
	    	cvPutText (grabbedImage, text, P, font, cvScalar(0.0,0.0,0.0,255.0));
	    	
	    //	frame.showImage( grabbedImage);
	    	opencv_highgui.cvWriteFrame(videoWriter, grabbedImage);
	    }while (/*frame.isVisible() &&*/ (grabbedImage = opencv_highgui.cvQueryFrame(capture)) != null);
	    
	    //frame.dispose();
	    opencv_highgui.cvReleaseVideoWriter(videoWriter);
	    opencv_highgui.cvReleaseCapture(capture);
	    
	    file = new File(outfile);
		if(file.exists()) file.delete();
	    comm = "ffmpeg -i audio1.mp3 -i out.avi -acodec copy -vcodec copy "+ outfile;
	    runCommand(comm);
	    file = new File("video1.avi"); file.delete();
	    file = new File("audio1.mp3"); file.delete();
	    file = new File("out.avi"); file.delete();
	}
	
	public static void videoInsert(String filename, String toinsert, double time, String outfile) throws IOException{
		double filetime = parseTime(metadata(filename).get("duration"));
		System.out.println("filetime = "+filetime+" time = "+time);
		if(filetime <= time){
			concat(filename, toinsert, outfile);			
		}
		else if(time==0){
			concat(toinsert, filename, outfile);
		}
		else{
			File file = new File("part1.avi");
			if(file.exists()) file.delete();
			String comm = "ffmpeg -ss 0 -t " + time +" -i " +filename + " -sameq part1.avi";
			runCommand(comm);
			file = new File("part2.avi");
			if(file.exists()) file.delete();
			comm = "ffmpeg -ss " + time +" -t "+ (filetime-time) +" -i " +filename + " -sameq part2.avi";
			runCommand(comm);
			concat("part1.avi", toinsert, "concat1.avi");
			concat("concat1.avi", "part2.avi", outfile);			
			
			file = new File("part1.avi");
			if(file.exists()) file.delete();
			file = new File("part2.avi");
			if(file.exists()) file.delete();
			file = new File("concat1.avi");
			if(file.exists()) file.delete();
		}		
	}
	
	private static void concat(String filename, String toinsert, String outfile) throws IOException {
		
		String comm;
		File file = new File("video1.avi");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i "+ filename+ " -an -vcodec copy video1.avi";
		runCommand(comm);
		file = new File("audio1.mp3");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i "+ filename+ " -vn audio1.mp3";
		runCommand(comm);
		
		file = new File("video2.avi");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i "+ toinsert+ " -an -vcodec copy video2.avi";
		runCommand(comm);
		file = new File("audio2.mp3");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i "+ toinsert+ " -vn audio2.mp3";
		runCommand(comm);
		
		file = new File("intermediate1.mpg");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i video1.avi -qscale:v 1 intermediate1.mpg";
		runCommand(comm);
		file = new File("intermediate2.mpg");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i video2.avi -qscale:v 1 intermediate2.mpg";
		runCommand(comm);
		
		file = new File("intermediate_all.mpg");
		if(file.exists()) file.delete();
		comm = "cat intermediate1.mpg intermediate2.mpg > intermediate_all.mpg";
		System.out.println(comm);
		FileWriter fstream = new FileWriter("run.sh"); 
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
		
	    comm = "chmod 755 run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		comm = "./run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		file = new File("vout.avi");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i intermediate_all.mpg -qscale:v 2 vout.avi";		
		runCommand(comm);
		
		file = new File("intermediate_audio.mp3");
		if(file.exists()) file.delete();
		comm = "cat audio1.mp3 audio2.mp3 > intermediate_audio.mp3";
		System.out.println(comm);
		fstream = new FileWriter("run.sh"); 
	    out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
		
	    comm = "chmod 755 run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		comm = "./run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		file = new File("aout.mp3");
		if(file.exists()) file.delete();
		comm = "ffmpeg -i intermediate_audio.mp3 -qscale:v 2 aout.mp3";
		runCommand(comm);
		
		file = new File(outfile);
		if(file.exists()) file.delete();
		comm = "ffmpeg -i vout.avi -i aout.mp3 -acodec copy -vcodec copy " + outfile;
		runCommand(comm);
		
		file = new File("video1.avi");
		if(file.exists()) file.delete();
		file = new File("audio1.mp3");
		if(file.exists()) file.delete();
		file = new File("video2.avi");
		if(file.exists()) file.delete();
		file = new File("audio2.mp3");
		if(file.exists()) file.delete();
		file = new File("intermediate1.mpg");
		if(file.exists()) file.delete();
		file = new File("intermediate2.mpg");
		if(file.exists()) file.delete();
		file = new File("intermediate_all.mpg");
		if(file.exists()) file.delete();
		file = new File("vout.avi");
		if(file.exists()) file.delete();
		file = new File("intermediate_audio.mp3");
		if(file.exists()) file.delete();
		file = new File("aout.mp3");
		if(file.exists()) file.delete();
		
	}

	public static void audioOverlay(String videofile, String audiofile, boolean repeat) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Map<String, String> amap = metadata(audiofile);
		Map<String, String> vmap = metadata(videofile);
				
		double atime = parseTime(amap.get("duration"));
		double vtime = parseTime(vmap.get("duration"));

		System.out.println(atime);
		System.out.println(vtime);
		
		if((atime!=0)&&(vtime!=0)){
			File file = new File ("out.avi");
			if(file.exists()) file.delete();
			
			if(atime >= vtime){
				String comm = "ffmpeg -i "+ audiofile +" -i "+ videofile +" -vcodec copy -acodec copy -shortest out.avi";
				if(!runCommand(comm)) System.out.println("Error occurred, couldnot overlay");
			}
			else{
				if(repeat){
					int loop = (int)(vtime/atime);
					
					file = new File ("looped.mp3");
					if(file.exists()) file.delete();
					
					String temp = ""; 
					while(loop>0){
						System.out.println("-------------------> " + loop);
						temp = temp+ '|' + audiofile;
						loop--;
					}
					
					String comm = "ffmpeg -i \"concat:"+audiofile + temp+"\" -c copy looped.mp3";
			    	System.out.println(comm);
					
					FileWriter fstream = new FileWriter("run.sh"); 
				    BufferedWriter out = new BufferedWriter(fstream);
				    out.write(comm);
				    out.close();
					
				    comm = "chmod 755 run.sh";
					System.out.println(comm);
					runCommand(comm);
					
					comm = "./run.sh";
					runCommand(comm);
					
					comm = "ffmpeg -i looped.mp3 -i "+ videofile +" -vcodec copy -acodec copy -shortest out.avi";
					if(!runCommand(comm)) System.out.println("Error occurred, couldnot overlay");
					
					file = new File ("looped.mp3");
					if(file.exists()) file.delete();
				}
				else{
					file = new File("audio.mp3");
					if(file.exists())	file.delete();
					String comm = "ffmpeg -i "+videofile +" -ss " + atime + " -t " + (vtime - atime) + " -vn audio.mp3";
					runCommand(comm);
										
					file = new File("audio2.mp3");
					if(file.exists())	file.delete();
					comm = "ffmpeg -i \"concat:"+audiofile+"|audio.mp3\" audio2.mp3";
					System.out.println(comm);
					
					FileWriter fstream = new FileWriter("run.sh"); 
				    BufferedWriter out = new BufferedWriter(fstream);
				    out.write(comm);
				    out.close();
					
				    comm = "chmod 755 run.sh";
					System.out.println(comm);
					runCommand(comm);
					
					comm = "./run.sh";
					runCommand(comm);
					
					file = new File("out.avi");
					if(file.exists())	file.delete();
					comm = "ffmpeg -i audio2.mp3 -i "+ videofile +" -vcodec copy -acodec copy out.avi";
					if(!runCommand(comm)) System.out.println("Error occurred, couldnot overlay");
					
					file = new File("audio.mp3");
					if(file.exists())	file.delete();
					file = new File("audio2.mp3");
					if(file.exists())	file.delete();
				}
			}
		}
	}
	
	private static double parseTime (String duration){
		int i, j;
		String hours;
		i = 0;
		j = duration.indexOf(':');
		hours = duration.substring(i, j);
		//System.out.println("hours : " + hours);
		duration = duration.substring(0, j) + ' ' + duration.substring(j+1);
		//System.out.println(duration);
		
		String min;
		i = j+1;
		j = duration.indexOf(':');
		min = duration.substring(i, j);
		//System.out.println("min : " + min);
		duration = duration.substring(0, j) + ' ' + duration.substring(j+1);
		//System.out.println(duration);
		
		String sec = duration.substring(j+1);
		//System.out.println("sec : " + sec);
		
		Double length = Double.parseDouble(hours)*3600 + Double.parseDouble(min)*60 + Double.parseDouble(sec); 
		return length;
	}
	
	public static Map<String, String> metadata(String filename){
		try {
        	String s = null;       	 
        	String comm = "ffmpeg -i " + filename;
        	Map<String, String> map = new HashMap<String, String>();
        	
        	System.out.println(comm);
        	Process p = Runtime.getRuntime().exec(comm);     	
        	        	        	            
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(p.getErrorStream()));

            // read the output from the commandinp2.avi
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            // read any errors from the attempted command finds out the duration of the video.
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                if(s.contains("No such file")){
                	return null;
                }
                else {
                	parse(s, map);
                }
            }
            System.out.println(map);
            return map;
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            return 	null;
        }	
	}
	
	private static void parse(String s, Map<String, String> map){
		if(s.contains("Duration")){
			String[] y = s.split(" ");
			int len = y.length;
			for(int i =0; i< len; i++){
				if(y[i].contains("Duration")){
					//System.out.println(y[i+1].substring(0, y[i+1].length()-1));
					map.put("duration", y[i+1].substring(0, y[i+1].length()-1));
					i++;
				}
				else if(y[i].contains("start")){
					map.put("start", y[i+1].substring(0, y[i+1].length()-1));
					i++;
				}
				else if(y[i].contains("bitrate")){
					map.put("bitrate", y[i+1]);
					i++;
				}
				
			}			
		}
		else if(s.contains("Video")){
			String[] y = s.split(",");
			int len= y.length;
			for(int i = 0; i<len; i++){				
				if(y[i].contains("Video")){
					String[] x = y[i].split("Video:");					
					int l = x.length;
					if(!x[l-1].contains("(")){
						if(!map.containsKey("Vcodec")) 	map.put("Vcodec", x[x.length-1]);
					}
					else{
						String[] z= x[l-1].split(" ");
						if(!map.containsKey("Vcodec")) 	map.put("Vcodec", z[1]);
					}
				}
				else if(y[i].contains("SAR")){
					String[] x = y[i].split(" ");
					for(int j = 0; j < x.length; j++){
						if(x[j].contains("x")){
							if(!map.containsKey("frame")) 	map.put("frame", x[j]);
							break;
						}							
					}
				}
				else if(y[i].contains("fps")){
					if(!map.containsKey("fps")) 	map.put("fps", y[i].split(" ")[1]);
				}
				else if(y[i].contains("tbr")){
					if(!map.containsKey("tbr")) 	map.put("tbr", y[i].split(" ")[1]);
				}
				else if(y[i].contains("tbn")){
					if(!map.containsKey("tbn")) 	map.put("tbn", y[i].split(" ")[1]);
				}
				else if(y[i].contains("tbc")){
					if(!map.containsKey("tbc")) 	map.put("tbc", y[i].split(" ")[1]);
				}
			}				
		}
		
		if(s.contains("Audio")){
			String[] y = s.split(",");
			for(int j = 0; j<y.length; j++){
				if(y[j].contains("Audio")){
					String[] x = y[j].split("Audio:");
					int len = x.length;
					if(!x[len-1].contains("(")){
						if(!map.containsKey("Acodec")) 	map.put("Acodec", x[x.length-1]);
					}
					else{
						String[] z= x[len-1].split(" ");
						if(!map.containsKey("Acodec")) 	map.put("Acodec", z[1]);
					}
				}
				else if(y[j].contains("Hz")){
					if(!map.containsKey("frequency")) 	map.put("frquency", y[j].split(" ")[1]);
				}
				else if(y[j].contains("kb")){
					if(!map.containsKey("Abitrate")) 	map.put("Abitrate", y[j].split(" ")[1]);
				}
			}
		}
		//System.out.println(map); 
	}
	
	public static void scale(String filename, int width, int height, String bg, String outfilename) throws IOException{
		File file = new File (outfilename);
    	if(file.exists()) file.delete();
    	
    	String comm = "ffmpeg -i "+ filename +" -sameq -vf scale=\""+width+":-1,pad="+width+':'+height+":(ow-iw)/2:(oh-ih)/2:" +bg +"\" " + outfilename;
    	System.out.println(comm);
		
		FileWriter fstream = new FileWriter("run.sh"); 
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
		
	    comm = "chmod 755 run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		comm = "./run.sh";
		System.out.println(comm);
		if(!runCommand(comm)){
			file = new File (outfilename);
	    	if(file.exists()) file.delete();
			
			comm = "ffmpeg -i "+ filename +" -sameq -vf scale=\"-1:"+height+",pad="+width+':'+height+":(ow-iw)/2:(oh-ih)/2:"+ bg+"\" "+ outfilename;
	    	System.out.println(comm);
			
			fstream = new FileWriter("run.sh"); 
		    out = new BufferedWriter(fstream);
		    out.write(comm);
		    out.close();
			
		    comm = "chmod 755 run.sh";
			System.out.println(comm);
			runCommand(comm);
			
			comm = "./run.sh";
			System.out.println(comm);
			if(!runCommand(comm))
				System.out.println("Some error occured! couldnot scale");
		}
	}
	
	public static void format_conversion(String filename, String outfilename){
		File file = new File (outfilename);
    	
		if(file.exists()) file.delete();
    	String comm = "ffmpeg -i " + filename +" -vcodec copy -acodec copy " + outfilename;
    	if(!runCommand(comm))
    		System.out.println("Some error occured! couldnot convert");
		
	}
	
	public static void clip(String filename, double start, double duration, String outfilename) throws IOException{
		
		File file = new File (outfilename);
    	if(file.exists()) file.delete();
    	
    	String comm = "ffmpeg -ss "+start +" -t " +duration+ " -i " + filename + " -vcodec copy -acodec copy "+ outfilename;
		System.out.println(comm);
		FileWriter fstream = new FileWriter("run.sh"); 
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
		
	    comm = "chmod 755 run.sh";
		System.out.println(comm);
		runCommand(comm);
		
		comm = "./run.sh";
		System.out.println(comm);
		if(!runCommand(comm))
			System.out.println("Some error occured! couldnot clip");
		
		file = new File("run.sh");
		file.delete();
	}
	// flag true ----> frames at every num seconds. flag false -----> total num frames from the video
	public static void nShots(String filename, double num, int width, int height, boolean flag) throws MagickException, IOException{
		String comm = "rm -rf out run.sh";
		runCommand(comm);
		comm = "mkdir out";
		runCommand(comm);
		
		if(flag == true)
			comm = "ffmpeg -skip_frame nokey -i " + filename +" -vf \"scale="+width+":-1,pad="+width+':'+height+":(ow-iw)/2:(oh-ih)/2,fps=fps=1/"+num+"\" -an -vsync 0 ./out/out%d.png";
		else{
			double frames = parseTime(metadata(filename).get("duration"))/(num - 1);
			comm = "ffmpeg -skip_frame nokey -i " + filename +" -vf \"scale="+width+":-1,pad="+width+':'+height+":(ow-iw)/2:(oh-ih)/2,fps=fps=1/"+frames+"\" -an -vsync 0 ./out/out%d.png";
		}
		System.out.println(comm);
		
		FileWriter fstream = new FileWriter("run.sh"); 
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
	    
	    comm = "chmod 755 run.sh";
	    runCommand(comm);
	    comm = "./run.sh";
		
		if(!runCommand(comm)){
			System.out.println("Some error occured! couldnot find keyframes");
			return;
		}
	}

	public static void screenshot(String filename) throws IOException{
		String comm;		
		comm = "ffmpeg -i " + filename +" -vf \"thumbnail,scale=640:360\" -frames:v 1 screenshot.png";
		
		FileWriter fstream = new FileWriter("run.sh"); 
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(comm);
	    out.close();
	    
	    comm = "rm -r screenshot.png";
		runCommand(comm);
				
	    comm = "chmod 755 run.sh";
	    runCommand(comm);
	    comm = "./run.sh";
	    
		if(!runCommand(comm)){			
			comm = "ffmpeg -skip_frame nokey -i " + filename + " -frames:v 1 -an -vsync 0 screenshot.png";
			fstream = new FileWriter("run.sh");
			out = new BufferedWriter(fstream);
			out.write(comm);
			out.close();
		    comm = "./run.sh";
			if(!runCommand(comm)){
				System.out.println("No keyframe found\n");
			}
		}
		
		comm = "rm -r run.sh";
		runCommand(comm);
	}
	
	private static boolean runCommand(String comm){
        try {
        	String s = null;       	 
        	
        	System.out.println(comm);
        	Process p = Runtime.getRuntime().exec(comm);
        	//Process p = Runtime.getRuntime().exec(new String[]{"ffmpeg", "-i", "inp2.avi","-vf","thumbnail", "-vf","scale=640x360","-frames:v","1","screenshot2.png"});
        	
        	
        	        	            
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(p.getErrorStream()));

            // read the output from the commandinp2.avi
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            //Boolean flag = false;
            // read any errors from the attempted command finds out the duration of the video.
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                if(s.contains("Output file is empty") || s.contains("Error")){
                	return false;
                }
            }
           
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            return 	false;
        }
        return true;
	}
	
}