package edu.jhu.cs.dwatso20;

import java.io.File;

public class FileUtil {
	public static File getConfigFile(File base) {
		return new File(base, "publishr.properties");
	}
	
	public static File getTemplateDir(File base) {
		return new File(base, "templates");
	}
	
	public static File getArticleDir(File base) {
		return new File(base, "articles");
	}

	public static boolean containsPublishrAssets(File file) {
		return getArticleDir(file).exists() && getConfigFile(file).exists() && getTemplateDir(file).exists();
	}
}
