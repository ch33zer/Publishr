package edu.jhu.cs.dwatso20;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Template {
	private StringBuilder fileContents;
	private String fileType;
	private String folder;
	private String fileName;
	private Publishr publishr;
	//NOTE: All keyHandler keys MUST be uppercase otherwise the special keys will be returned to from getAllPlaceholders
	private Map<String, KeyHandler> keyHandlers = new HashMap<String, KeyHandler>();
	{
		keyHandlers.put(PropertyKeys.PREVKEY, new KeyHandler() {

			@Override
			public String handle(String key, String articleText,
					Article currentArticle) {
				int targetOrder = currentArticle.getArticleOrder();
				int closest = Integer.MIN_VALUE;
				Article closestArticle = currentArticle;
				for (Article article : publishr.getArticles()) {
					
					if (article.getArticleOrder() < targetOrder) {
						if (article.getArticleOrder() > closest) {
							closest = article.getArticleOrder();
							closestArticle = article;
						}
					}
				}
				return replaceInString(articleText, key, "<a href=\"" + getArticleFilename(closestArticle)+"\">Previous</a>");
			}
		});
		keyHandlers.put(PropertyKeys.NEXTKEY, new KeyHandler() {

			@Override
			public String handle(String key, String articleText,
					Article currentArticle) {
				int targetOrder = currentArticle.getArticleOrder();
				int closest = Integer.MAX_VALUE;
				Article closestArticle = currentArticle;
				for (Article article : publishr.getArticles()) {
					
					if (article.getArticleOrder() > targetOrder) {
						if (article.getArticleOrder() < closest) {
							closest = article.getArticleOrder();
							closestArticle = article;
						}
					}
				}
				return replaceInString(articleText, key, "<a href=\"" + getArticleFilename(closestArticle)+"\">Next</a>");
				}
		});
		keyHandlers.put(PropertyKeys.TODAYKEY, new KeyHandler() {
			
			@Override
			public String handle(String key, String articleText, Article article) {
				return replaceInString(articleText, key, sdf.format(new Date()));
			}
		});
	}
	SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");

	public Template(File file, Publishr publishr) throws IOException {
		this.publishr = publishr;
		folder = file.getParent();
		fileName = file.getName();
		String fileName = file.getName();
		fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
		fileContents = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			fileContents.append(line);
			fileContents.append(ls);
		}
		reader.close();
	}
	
	public String getFileType() {
		return fileType;
	}
	
	public String getArticleFilename(Article article) {
		return article.getArticleFilename().trim().replace(".properties", "."+getFileType());
	}
	

	public String buildArticleFromTemplate(
			Article article) {
		String newArticle = fileContents.toString();
		newArticle = replaceSpecialKeys(article, newArticle);
		newArticle = replaceNormalKeys(article, newArticle);
		return newArticle;
	}

	private String replaceSpecialKeys(Article article, String newArticle) {
		for (Entry<String, KeyHandler> entry : keyHandlers.entrySet()) {
			newArticle = entry.getValue().handle(entry.getKey(), newArticle, article);
		}
		return newArticle;
	}

	private String replaceInString(String oldArticle, String key, String value) {
		// Regex breakdown: \\{ is just a literal {, [\\s]* is any number of
		// whitespace characters, (?i) means that the regex is case
		// insensitive from this point until (?-i),\\Q is the start of a quote, so
		// that whatever is in the key is taken literally, \\E ends the
		// quote, (?-i) ends the case insensitivity, the rest is the same as
		// the beginning
		oldArticle = oldArticle.replaceAll("\\{\\{\\{[\\s]*(?i)\\Q" + key
				+ "\\E(?-i)[\\s]*\\}\\}\\}", value);
		return oldArticle;
	}

	private String replaceNormalKeys(Article article, String newArticle) {
		for (String key : article.getPlaceholders()) {
			String value = article.getPlaceholderText(key);
			newArticle = replaceInString(newArticle, key, value);
		}
		return newArticle;
	}
	
	public String[] getAllPlaceholders() {
		String stringPattern = "\\{\\{\\{[\\s]*(\\S+?)[\\s]*\\}\\}\\}";
		Pattern pattern = Pattern.compile(stringPattern);
	    // In case you would like to ignore case sensitivity you could use this
	    // statement
	    // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(fileContents.toString());
	    // Check all occurance
	    ArrayList<String> groups = new ArrayList<String>();
	    while (matcher.find()) {
			String group = matcher.group();
			groups.add( group.substring(3, group.length() - 3).trim());
	    }
	    Iterator<String> groupsIter = groups.iterator();
	    while (groupsIter.hasNext()) {
	    	String placeHolder = groupsIter.next();
	    	for (String specialPlaceholder : keyHandlers.keySet()) {
	    		if (placeHolder.equalsIgnoreCase(specialPlaceholder)) {
	    			groupsIter.remove();
	    		}
	    	}
	    }
	    return groups.toArray(new String[groups.size()]);
	}

	public void copyAllSupportingFilesTo(File dir) throws IOException {
		File[] supportingFiles = new File(folder).listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File arg0, String arg1) {
						return !arg1.equals(fileName);
					}
				});
		if (supportingFiles != null) {
			for (File file : supportingFiles) {
				copyFolder(file, new File(dir, file.getName()));
			}
		}
	}

    public static void copyFolder(File src, File dest)
        	throws IOException{
     
        	if(src.isDirectory()){
     
        		//if directory not exists, create it
        		if(!dest.exists()){
        		   dest.mkdir();
        		}
     
        		//list all the directory contents
        		String files[] = src.list();
     
        		for (String file : files) {
        		   //construct the src and dest file structure
        		   File srcFile = new File(src, file);
        		   File destFile = new File(dest, file);
        		   //recursive copy
        		   copyFolder(srcFile,destFile);
        		}
     
        	}else{
        		//if file, then copy it
        		//Use bytes stream to support all file types
        		InputStream in = new FileInputStream(src);
        	        OutputStream out = new FileOutputStream(dest); 
     
        	        byte[] buffer = new byte[1024];
     
        	        int length;
        	        //copy the file content in bytes 
        	        while ((length = in.read(buffer)) > 0){
        	    	   out.write(buffer, 0, length);
        	        }
     
        	        in.close();
        	        out.close();
        	}
        }
	

}
