package edu.jhu.cs.dwatso20;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ConstructedArticle extends Article {
	
	@Override
	public String toString() {
		return "ConstructedArticle [constructedArticle=" + constructedArticle + "]";
	}
	private String constructedArticle = "";
	public ConstructedArticle(UnconstructedArticle article, Template template) {
		super (article);
		constructedArticle = template.buildArticleFromTemplate(article);
	}
	/**
	 * @return the constructedArticle
	 */
	public String getConstructedArticle() {
		return constructedArticle;
	}
	

	public void writeOut(File file) throws FileNotFoundException {
		if (file.exists()) {
			file.delete();
		}
		PrintWriter writer = new PrintWriter(file);
		writer.print(constructedArticle);
		writer.close();
	}
}
