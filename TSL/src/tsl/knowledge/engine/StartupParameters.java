package tsl.knowledge.engine;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import tsl.error.TSLException;
import tsl.utilities.FUtils;

public class StartupParameters {

	private Properties properties = null;
	private String rootDirectory = null;
	private String ruleDirectory = null;
	private String resourceDirectory = null;
	private String KBDirectory = null;
	private String regExpPatternDirectory = null;
	private static String RuleDirectoryName = "GrammarRules";
	private static String ResourceDirectoryName = "resources";
	private static String RegExprDirectoryName = "RegExp";
	private static String KBDirectoryName = "OnyxKBFiles";
	private static String TextInputDirectoryName = "TextInputDirectory";
	private String textInputDirectory = null;
	private String resultsOutputDirectory = null;

	public StartupParameters(Properties properties, String pfilename) throws TSLException {
		if (properties == null && pfilename != null) {
			int x = 2;
			File pfile = FUtils.getResourceFile(this.getClass(), pfilename);
			if (pfile != null && pfile.exists()) {
				this.rootDirectory = pfile.getParent();
			} else {
				throw new TSLException("Unable to find property file");
			}
		}
		readProperties(properties);
	}

	private void readProperties(Properties properties) throws TSLException {
		int x = 1;
		this.properties = properties;
		String rdpath = (String) properties.get("RootDirectory");
		String current = null;
		if (rdpath != null) {
			this.rootDirectory = rdpath;
		}
		System.out.println("StartupParameters.readProperties: RootDir=" + this.rootDirectory);
		if (this.rootDirectory == null) {
			throw new TSLException("Root directory does not exist");
		}
		this.ruleDirectory = this.rootDirectory + File.separatorChar + RuleDirectoryName;
		this.resourceDirectory = this.rootDirectory + File.separatorChar + ResourceDirectoryName;
		this.KBDirectory = this.rootDirectory + File.separatorChar + KBDirectoryName;
		this.regExpPatternDirectory = this.rootDirectory + File.separatorChar + RegExprDirectoryName;
		
		String str = properties.getProperty("TextInputDirectory");
		if (str != null) {
			if (FUtils.isRelativePath(str)) {
				str = this.rootDirectory + File.separatorChar + str;
			}
			this.textInputDirectory = str;
		}
		
		str = properties.getProperty("ResultsOutputDirectory");
		if (str != null) {
			if (FUtils.isRelativePath(str)) {
				str = this.rootDirectory + File.separatorChar + str;
			}
			this.resultsOutputDirectory = str;
		}
		
		// /4/2017:  TEST: Docker container app not finding rule directory...
		if (!(new File(this.ruleDirectory).isDirectory())) {
			System.out.println("StartupParameters.readProperties: RuleDir=" + this.ruleDirectory);
			throw new TSLException("Rule directory does not exist");
		}
		if (!(new File(this.resourceDirectory).isDirectory())) {
			throw new TSLException("Resource directory does not exist");
		}
		if (!(new File(this.KBDirectory).isDirectory())) {
			throw new TSLException("KB directory does not exist");
		}
		if (!(new File(this.regExpPatternDirectory).isDirectory())) {
			throw new TSLException("RegExp directory does not exist");
		}
	}

	public Properties getProperties() {
		return this.properties;
	}

	public String getPropertyValue(String property) {
		return this.properties.getProperty(property);
	}

	public void setPropertyValue(String property, Object value) {
		this.properties.put(property, value);
	}

	public boolean isPropertyTrue(String property) {
		Object value = this.properties.getProperty(property);
		if (value != null && "true".equals(value.toString().toLowerCase())) {
			return true;
		}
		return false;
	}

	public String getFileName(String fname) {
		String fstr = null;
		fstr = this.rootDirectory + File.separatorChar + fname;
		if (new File(fstr).exists()) {
			return fstr;
		}
		return null;
	}

	public String getFileName(String dname, String value) {
		String fstr = null;
		
		if (isFullPathname(value)) {
			fstr = value;
		} else {
			fstr = dname + File.separatorChar + value;
		}
		
		
		
		if (new File(fstr).exists()) {
			return fstr;
		}
		String property = this.getPropertyValue(value);
		if (property != null && !property.equals(value)) {
			return getFileName(dname, property);
		}
		return null;
	}
	
	private boolean isFullPathname(String value) {
		if (value != null && value.length() > 4) {
			char fc = value.charAt(0);
			char sc = value.charAt(1);
			char sepc = File.separatorChar;
			if (fc == sepc || value.contains(":")) {
				return true;
			}
		}
		return false;
	}

	public String getResourceFileName(String value) {
		return getFileName(this.resourceDirectory, value);
	}

	public String getRuleFileName(String value) {
		return getFileName(this.ruleDirectory, value);
	}

	public String getRootFileName(String value) {
		return getFileName(this.rootDirectory, value);
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public String getRuleDirectory() {
		return ruleDirectory;
	}

	public String getResourceDirectory() {
		return resourceDirectory;
	}

	public String getKBDirectory() {
		return KBDirectory;
	}

	public String getRegExpPatternDirectory() {
		return regExpPatternDirectory;
	}

	public String getTextInputDirectory() {
		return textInputDirectory;
	}
	
	public String getResultsOutputDirectory() {
		return this.resultsOutputDirectory;
	}
	
	

}
