package edu.jhu.cs.dwatso20;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class UnconstructedArticle extends Article{

	public UnconstructedArticle(File file) throws FileNotFoundException,
			IOException {
		super(file);
	}

	public UnconstructedArticle(String name, int order) {
		super(name, order);
	}

	public UnconstructedArticle(String name, String filename, int order) {
		super(name, filename, order);
	}

	public UnconstructedArticle(String name) {
		super(name);
	}

}
