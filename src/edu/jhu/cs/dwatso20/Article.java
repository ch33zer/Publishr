package edu.jhu.cs.dwatso20;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public abstract class Article {
	private Properties properties;
	private String filename;

	public Article(File file) throws FileNotFoundException, IOException {
		properties = new Properties();
		FileInputStream in = new FileInputStream(file);
		properties.load(in);
		in.close();
		setArticleFilename(file.getName());
		setPubDate(new Date());
	}

	SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
	public void setPubDate(Date date) {
		if (!properties.containsKey(PropertyKeys.ARTICLEINITIALPUBDATEKEY))
			properties.setProperty(PropertyKeys.ARTICLEINITIALPUBDATEKEY, sdf.format(date));
	}
	
	public String getPubDate() {
		return properties.getProperty(PropertyKeys.ARTICLEINITIALPUBDATEKEY);
	}
	

	public Article(Article article) {
		this.properties = article.properties;
		this.filename = article.filename;
	}
	public Article(String name) {
		this(name,name,Integer.MAX_VALUE);
	}
	public Article(String name, int order) {
		this(name, name, order);
	}
	public Article(String name, String filename, int order) {
		properties = new Properties();
		setArticleFilename(filename);
		setArticleName(name);
		setArticleOrder(order);
		setPubDate(new Date());
	}

	public Set<String> getPlaceholders() {
		Set<String> properties = this.properties.stringPropertyNames();
		return properties;
	}
	
	public String getArticleName() {
		return properties.getProperty(PropertyKeys.ARTICLENAMEKEY);
	}
	
	public void setArticleName(String name) {
		properties.setProperty(PropertyKeys.ARTICLENAMEKEY, name);
	}
	
	public void setArticleOrder(int order) {
		properties.setProperty(PropertyKeys.ARTICLEORDERKEY, new Integer(order).toString());
	}
	
	public String getArticleFilename() {
		return filename;
	}

	public void setArticleFilename(String filename) {
		this.filename = filename;
	}

	public int getArticleOrder() {
		int order;
		try {
			order = Integer.parseInt(properties.getProperty(PropertyKeys.ARTICLEORDERKEY));
		} catch (NumberFormatException e) {
			order = Integer.MAX_VALUE;
		}
		return order;
	}
	
	public String getPlaceholderText(String placeholder) {
		return properties.getProperty(placeholder);
	}
	
	public void addPlaceholder(String placeholder, String replacement) {
		if (!properties.containsKey(placeholder)) {
			properties.setProperty(placeholder, replacement);
		}
	}
	
	public void save(File file) throws IOException {
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fos = new FileOutputStream(file);
		properties.store(fos, "Modified at " + new Date().toString());
		fos.close();
	}


}
