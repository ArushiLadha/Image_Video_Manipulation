package matching;

import java.io.File;

public class renameFile {
	
		public static void renameFunc(){
	
			File folder = new File("./googleImages/");
			File[] listOfFiles = folder.listFiles();
	 
			String str;
			int count = 0;
			for(File file : listOfFiles) {
				if (file.isFile()) {
					System.out.println(count);
					str = "./googleImages/"+count+".jpg";
					File s = new File(str); 
					file.renameTo(s);
					count++;
				}
			}	
		}
		
		public static void main(String[] args){
			renameFunc();
		}
}
