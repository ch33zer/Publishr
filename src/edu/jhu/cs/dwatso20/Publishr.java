package edu.jhu.cs.dwatso20;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Publishr {
	
	private PublishrData data;
	private Set<UnconstructedArticle> articles;
	private Template template;
	private File workingDir;
	private Publishr publishr;
	private int maxOrder = Integer.MIN_VALUE;
	public Set<UnconstructedArticle> getArticles() {
		return articles;
	}
	public void setArticles(Set<UnconstructedArticle> articles) {
		this.articles = articles;
	}
	private Publishr(String file) throws InvalidPublishrDirectoryException, IOException, InvalidConfigurationFileException {
		this(new File(file));
	}
	private Publishr(File file) throws InvalidPublishrDirectoryException, IOException, InvalidConfigurationFileException {
		publishr = this;
		workingDir = file;
		open(file);
	}

	public void open(File file) throws InvalidPublishrDirectoryException, IOException, InvalidConfigurationFileException {
		if (FileUtil.containsPublishrAssets(file))
			load(file);
		else 
			throw new InvalidPublishrDirectoryException("Folder " + file.getAbsolutePath() + " does not contain /articles, /templates, and/or publishr.properties");
	}

	private void load(File file) throws IOException, InvalidConfigurationFileException {
		data = new PublishrData(FileUtil.getConfigFile(file));
		template = new Template(new File(FileUtil.getTemplateDir(file),data.getTemplateFile()), this);
		loadArticles(FileUtil.getArticleDir(file));
	}
	
	private void loadArticles(File file) throws FileNotFoundException, IOException {
		File[] articles = file.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".properties");
			}
		});
		this.articles = new HashSet<UnconstructedArticle>();
		for (File article : articles) {
			UnconstructedArticle unconstructedArticle = new UnconstructedArticle(article);
			this.articles.add(unconstructedArticle);
			if (unconstructedArticle.getArticleOrder() > maxOrder) {
				maxOrder = unconstructedArticle.getArticleOrder();
			}
			
		}
	}

	public boolean createArticle(String name, String filename, int order) {
		if (!new File(FileUtil.getArticleDir(workingDir),filename).exists()) {
			if (maxOrder < order) {
				maxOrder=order;
			}
			UnconstructedArticle unconstructedArticle = new UnconstructedArticle(name, filename, order);
			for (String key : template.getAllPlaceholders()) {
				unconstructedArticle.addPlaceholder(key, "");
			}
			articles.add(unconstructedArticle);
			return true;
		}
		else {
			System.out.println("Article already exists at articles/"+filename);
			return false;
		}
	}
	
	public void save() throws IOException {
		File articleDir = FileUtil.getArticleDir(workingDir);
		for (UnconstructedArticle article : articles) {
			article.save(new File(articleDir, article.getArticleFilename()));
		}
	}
	
	public boolean deleteArticle(String filename) {
		return new File(FileUtil.getArticleDir(workingDir), filename).delete();
	}
	
	private void clearData() {
		if (data != null)
			data.clear();
		template = null;
		if (articles != null)
			articles.clear();
		maxOrder=Integer.MIN_VALUE;
	}
	
	public Publishr getInstance() {
		return publishr;
	}
	
	public static void main(String[] args) throws InvalidPublishrDirectoryException, IOException, InvalidConfigurationFileException {
		List<String> argsList = new ArrayList<String>(args.length);
		for (String s : args) {
			argsList.add(s);
		}
		Publishr publishr;
		//Publishr publishr = new Publishr("./publishr");
		if (argsList.size() > 0) {
			String workingDir = determineAndRemoveWorkingDirFromArgs(argsList);
			publishr = new Publishr(workingDir);
			OPERATION_MODE parseOperationMode = parseOperationMode(argsList.get(0));
			argsList.remove(0);
			switch (parseOperationMode) {
			case CREATE:
				for (String filename : argsList) {
					String name = filename;
					if (!filename.endsWith(".properties")) {
						filename = filename + ".properties";
					}
					// If create succeeds...
					if (publishr.createArticle(name, filename, publishr.getMaxOrder() + 1)) {
						System.out.println("Article " + name
								+ " created in articles/" + filename);
					}
					else {
						System.out.println("Article " + name
								+ " could not be created in articles/" + filename);
					}
				}
				publishr.save();
				break;
			case DELETE:
				
				for (String file: argsList) {
					if (!file.endsWith(".properties")) {
						file = file + ".properties";
					}
					if (publishr.deleteArticle(file)) {
						System.out.println("File " + file + " deleted.");
					}
					else {
						System.out.println("Problem deleting file " + file);
					}
				}
				break;
			case LIST:
				for (UnconstructedArticle article : publishr.getArticles()) {
					System.out.println(article.getArticleFilename() + ", name: " + article.getArticleName()+ ", order: " + article.getArticleOrder());
					System.out.println("Contents:");
					for (String key : article.getPlaceholders()) {
						System.out.print("\t" + key + ": \"" + article.getPlaceholderText(key) +"\"\n");
					}
					System.out.println("---------------------------------");
				}
				break;
			case PUBLISH:
				String publishDir = argsList.get(0);
				publishr.publishTo(publishDir);
				System.out.println("Published to " + publishDir);
				publishr.save();
				break;
			case UNDEFINED:
			default:
				printUsage("Unknown operation " + args[0]);
				break;

			}
		} else {
			printUsage("Must provide at least an operation (print|list|create|delete)");
		}
		
	}
	public void setMaxOrder(int maxOrder) {
		this.maxOrder = maxOrder;
	}
	public int getMaxOrder() {
		return maxOrder;
	}
	private static String determineAndRemoveWorkingDirFromArgs(List<String> argsList) {
		int index = 0;
		Iterator<String> argsIterator = argsList.iterator();
		while (argsIterator.hasNext()) {
			String arg = argsIterator.next();
			if (arg.equalsIgnoreCase("-p") || arg.equalsIgnoreCase("--publishrDir")) {
				if (index + 1 < argsList.size()) {
					argsIterator.remove();
					String workingDir = argsIterator.next();
					argsIterator.remove();
					return workingDir;
				}
				else {
					argsIterator.remove();
					System.out.println("Missing required operand after " + arg + ": publishrDir");
				}
			}
			index++;
		}
		return ".";
	}
	public void publishTo(String publishDir) throws IOException {
		File dir = new File(publishDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Set<ConstructedArticle> constructedArticles = buildAllArticles();
		ConstructedArticle last = null;
		for (ConstructedArticle article : constructedArticles) {
			article.writeOut(new File(dir,template.getArticleFilename(article)));
			if (last != null && last.getArticleOrder()< article.getArticleOrder()) {
				last = article;
			}
			else if (last == null) {
				last = article;
			}
		}
		if (last != null) {
			last.writeOut(new File(dir, "index."+template.getFileType()));
		}
		template.copyAllSupportingFilesTo(dir);
	}
	
	private Set<ConstructedArticle> buildAllArticles() {
		Set<ConstructedArticle> constructedArticles = new HashSet<ConstructedArticle>();
		for (UnconstructedArticle article : articles) {
			constructedArticles.add(new ConstructedArticle(article, template));
		}
		return constructedArticles;
	}
	private static Scanner input = new Scanner(System.in);
	private static String promptForString(String string) {
		System.out.println(string);
		return input.nextLine();
	}
	
	private static int promptForInt(String string) {
		System.out.println(string);
		return input.nextInt();
	}
	private static OPERATION_MODE parseOperationMode(String string) {
		if (string.equals("p") || string.equals("publish")) {
			return OPERATION_MODE.PUBLISH;
		}
		else if (string.equals("l") || string.equals("list")) {
			return OPERATION_MODE.LIST;
		}
		else if (string.equals("c") || string.equals("create")) {
			return OPERATION_MODE.CREATE;
		}
		else if (string.equals("d") || string.equals("delete")) {
			return OPERATION_MODE.DELETE;
		}
		else {
			return OPERATION_MODE.UNDEFINED;
		}
	}
	private static void printUsage(String string) {
		if (string != null && !string.trim().equals(""))
			System.out.println(string);
		System.out.println();
		System.out.println("Usage:\n" + usage());
	}
	private static String usage() {
		String usage = "publishr p|publish|l|list|c|create|d|delete [publishrDir]" +
					   "\npublishDir is an optional string that specifies which directory to read from. If not supplied, the current directory (\".\") is used.";
		return usage;
	}
}
