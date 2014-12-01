package util;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathPrint {
	
	public static void main(String[] args) {
	
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		
		URL[] urls = ((URLClassLoader)cl).getURLs();
		
		System.out.println("export CLASSPATH=\".\"");
		for (URL url : urls) {
			System.out.println("export CLASSPATH=${CLASSPATH}:" + url.getFile());
		}
	
	}
}
