package util;

import java.io.File;

import com.google.common.base.Preconditions;

public class FileUtil {
	
	public static boolean createParentDir(String filepath) {
		Preconditions.checkNotNull(filepath, "filepath is null");
		Preconditions.checkArgument(!filepath.trim().equals(""), "filepath not specified");
		
		File file = new File(filepath).getParentFile();

		if (file.exists() && file.isDirectory()) {
			//System.err.println("Cannot create " + file.getPath() + " dir. The directory already exist.");
			return false;
		}		
		if (file.exists() && file.isFile()) {
			System.err.println("Cannot create " + file.getPath() + " dir. A file exist with such name.");
			return false;
		}
		if (!file.mkdirs()) {
			System.err.println("Cannot create dir " + file.getPath() + ".");
			return false;
		}
		return true;		
	}

}
