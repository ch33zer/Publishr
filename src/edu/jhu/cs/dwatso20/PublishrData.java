package edu.jhu.cs.dwatso20;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import static edu.jhu.cs.dwatso20.PropertyKeys.*;

public class PublishrData {
	public Properties properties;

	public PublishrData(File file) throws IOException, InvalidConfigurationFileException {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// We should never get here since we already checked to make sure
			// the file exists
		}
		if (!isValid()) {
			throw new InvalidConfigurationFileException("File " + file + " is not a valid Publishr configuration file");
		}
	}

	private boolean isValid() {
		return properties.containsKey(TEMPLATEFILEKEY);
	}

	public String getTemplateFile() {
		return properties.getProperty(TEMPLATEFILEKEY);
	}

	public void clear() {
		properties.clear();
	}
}