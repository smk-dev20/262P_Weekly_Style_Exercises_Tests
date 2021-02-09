

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Twenty {
	static IWords tfwords; 
	static ITop25Frequency tffreqs ;
	
	@SuppressWarnings("deprecation")
	public static void loadPlugins() {

		Class wordClass = null;
		Class freqClass = null;
		URL classURL = null;
		try {
			File configFile = new File("config.properties");
			FileReader reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);

			classURL = new URL("jar","","file:"+props.getProperty("pluginPath")+props.getProperty("plugin")+"!/");
			System.out.println("file:"+props.getProperty("pluginPath")+props.getProperty("plugin"));
			//URL[] classUrls = { classURL };

			URLClassLoader cloader = URLClassLoader.newInstance(new URL[] {classURL});


			wordClass = cloader.loadClass(props.getProperty("wordClass"));
			freqClass = cloader.loadClass(props.getProperty("freqClass"));
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (wordClass != null) {
				tfwords = (IWords) wordClass.newInstance();
			}
			if (freqClass != null) {
				tffreqs = (ITop25Frequency) freqClass.newInstance();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// end loadPlugins
	
public static void main(String[] args) {
	
	loadPlugins();
	
	HashMap<String, Integer> wordFreqs;
	wordFreqs = tffreqs.top25(tfwords.extractWords(args[0]));
	
	for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
	}
}
}
