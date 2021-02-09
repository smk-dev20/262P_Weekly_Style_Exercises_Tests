import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * MSWE 262P
 * 
 * Style 20 
 * The problem is decomposed using some form of abstraction (procedures, functions, objects, etc.).
 * All or some of those abstractions are physically encapsulated into their own, usually pre-compiled, packages. Main program and each of the
 * packages are compiled independently. These packages are loaded dynamically
 * by the main program, usually in the beginning (but not necessarily).
 * Main program uses functions/objects from the dynamically loaded packages, without knowing which exact implementations will be used. New
 * implementations can be used without having to adapt or recompile the main program.
 * Existence of an external specification of which packages to load. This can be done by a conguration le, path conventions, user input or other
 * mechanisms for external specification of code to be loaded at runtime.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/tree/master/20-plugins
 * https://stackoverflow.com/questions/194698/how-to-load-a-jar-file-at-runtime
 * Video recording and slides for Feb 4 2021 Available on canvas
 * Code written for Weeks 1-4
 */


//class functions as the framework, loads plugins and calls needed classes to get term frequencies
public class Twenty {
	//two interfaces are defined which need to be implemented by the plugins
	public static IWords tfwords; 
	public static ITop25Frequency tffreqs ;
	
	//read path to plugins jar, jar name and classes in jar to load from properties file
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
	
	//get top 25 words from dynamically loaded classes
	HashMap<String, Integer> wordFreqs;
	wordFreqs = tffreqs.top25(tfwords.extractWords(args[0]));
	
	for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
	}
}
}
