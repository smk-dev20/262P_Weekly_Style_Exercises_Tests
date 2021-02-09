import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

/*
 * MSWE 262P
 * This program is a variation of Style 17 that is implemented in Style 11.
 * i.e., Program in Style 11 is expanded to use JAVA reflections for introspection
 * 
 * Style 11
 * The larger problem is decomposed into things that make sense for the problem domain.
 * Each thing is a capsule of data that exposes procedures to the rest of the world.
 * Data is never accessed directly, only through these procedures.
 * Capsules can reappropriate procedures defined in other capsules.
 * NOTE : Program is my implementation of Style 11 I have not utilizaed the available JAVA example on Github
 * Style 17
 * The problem is decomposed using some form of abstraction (procedures, functions, objects, etc.).
 * The abstractions have access to information about themselves and others, although they cannot modify that information
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/11-things/tf-11.py
 * https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/package-summary.html
 * Video recording of lecture on Feb 4 2021 (Available on Canvas)
 * Code written for Weeks 1-4  
 */

//Sample interface to demonstrate reflection
interface ISample{
	public void run();
}

//abstract class inherited by other classes to demonstrate reflection
abstract class TFExercise{
	public Object info() {
		return this.getClass().getName();
	}
}

//read in file normalize words and return list of words
class DataStorageManager extends TFExercise{
	private List<String> linesInText = new LinkedList<>();
	private  List<String> wordsList = new LinkedList<>();
	
	public DataStorageManager(String filePath) {
		File textFile = new File(filePath.trim());
		Scanner scanText;
		try {
			String line = null;
			scanText = new Scanner(textFile);
			while (scanText.hasNextLine()) {
				line = scanText.nextLine();

				if (line.equals(""))
					continue;
				this.linesInText.add(line);
			}
			scanText.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public List<String> words(){
		for (int i = 0; i < this.linesInText.size(); i++) {
			this.linesInText.set(i, this.linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
		}
		for (String line : this.linesInText) {
			this.wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
		}
		this.wordsList.removeIf(word -> (word.length() < 2 || word.equals("")));
		
		return this.wordsList;
	}
	
   public Object info() {
	return super.info() + "My major data structure is a "+this.wordsList.getClass().getName();
	   
   }
}

//read in stop words file provide method to check if word is a stop word
class StopWordManager extends TFExercise{
	private Set<String>stopWords;
	public StopWordManager() {
		try {
			this.stopWords= new HashSet<String>(Arrays.asList(Files.readAllLines(Paths.get("stop_words.txt")).get(0).split(",")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isStopWord(String word) {
		return this.stopWords.contains(word);
	}
	
	public Object info() {
		return super.info() + "My major data structure is a "+this.stopWords.getClass().getName();
			   
	}	
}


//keeps count of number of stop words, provide method to sort words on frequency
class WordFrequencyManager extends TFExercise{
	private Map<String, Integer> wordFrequency;
	
	public WordFrequencyManager() {
		this.wordFrequency = new LinkedHashMap<>();
	}
	
	public void incrementCount(String word) {
		this.wordFrequency.put(word, this.wordFrequency.getOrDefault(word, (int) (0))+1);
		
	}
	
	public Map<String, Integer> sorted(){
		LinkedHashMap<String, Integer> sortedMap = ((LinkedHashMap<String, Integer>) this.wordFrequency).entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an exception
						() -> new LinkedHashMap<String, Integer>()));
		
		return sortedMap;
	}
	
	public Object info() {
		return super.info() + "My major data structure is a " + this.wordFrequency.getClass().getName();

	}
}

//Controller class instantiates other classes and calls them reflexively to 
// print top 25 words
class WordFrequencyController extends TFExercise implements ISample{
	
	private  DataStorageManager storageManager=null;
    private  StopWordManager stopWordManager;
	private  WordFrequencyManager wordFrequencyManager;
	
	public WordFrequencyController(String filePath) {
		this.storageManager = new DataStorageManager(filePath);
		this.stopWordManager = new StopWordManager();
		this.wordFrequencyManager = new WordFrequencyManager();
	}
	
	public void run() {
		
		Class dataManager = this.storageManager.getClass();
		Class stopManager = this.stopWordManager.getClass();
		Class freqManager = this.wordFrequencyManager.getClass();
		Method dataManagerMethod = null;
		Method stopManagerMethod = null;
		Method freqManagerMethod = null;
		try {
			dataManagerMethod = dataManager.getDeclaredMethod("words");
			stopManagerMethod = stopManager.getDeclaredMethod("isStopWord", String.class);
			freqManagerMethod = freqManager.getDeclaredMethod("incrementCount", String.class);

			for(String word : (List<String>) dataManagerMethod.invoke(this.storageManager)) {
				
				if (!(boolean)stopManagerMethod.invoke(this.stopWordManager, word)){
					freqManagerMethod.invoke(this.wordFrequencyManager, word);
				}
			}
		
			freqManagerMethod = freqManager.getDeclaredMethod("sorted");
			Map<String, Integer> frequencies = (Map<String, Integer>) freqManagerMethod.invoke(this.wordFrequencyManager);
			
			int num = 25;
			for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
				if (num > 0) {
					System.out.println(entry.getKey() + " - " + entry.getValue());
					num--;
				} else
					break;
			}
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}


public class Week5_Seventeen {

//perform introspection using JAVA reflection on class provided in parameter
public static void reflectOnClass(String name) {
	Class cls = null;
	try {
		cls = Class.forName(name);
	}catch(Exception e) {
		System.out.println("No such class name");
	}
	
	try {
	if(cls!=null) {
		System.out.println("---Interfaces---");
		Class[] interfaces = cls.getInterfaces();
		if(interfaces.length==0) {
			System.out.println(name + " does not implement any interfaces");
		}
		
		for(Class iface : interfaces) {
			System.out.println("Interface Name : "+ iface.getName()+" Interface Type : "+iface.getTypeName());
		}
		
		Field[] fields = cls.getDeclaredFields();
		System.out.println("\n---Fields---");
		if(fields.length==0) {
			System.out.println(name + " does not have any fields");
		}
		for(Field f : fields) {
			System.out.println("Field Name : "+ f.getName()+" Field Type : "+f.getType().getName());
		}
		
		Method[] methods = cls.getMethods();
		System.out.println("\n---Methods---");
		for(Method m : methods) {
			System.out.println("Method Name : "+ m.getName());
		}
	
		System.out.println("\n---Super Class---");

		System.out.println("Super Class Name : "+ cls.getSuperclass().getName());		
	}
	}catch(Exception e) {
		System.out.println("EXCEPTION : class name may not be valid");
	}
}

//main function calls method of controller class for term frequency
//prompts user for class name to inspect
public static void main(String[] args) {
	WordFrequencyController freqController = new WordFrequencyController(args[0]);
	freqController.run();

	System.out.println("*****************INSPECT**********************");
	System.out.println("Enter a class to inspect : ");
	Scanner in = new Scanner(System.in);
	String name = in.nextLine();
	System.out.println("\n##### Retrieving information about "+name+" #####");
	
	reflectOnClass(name);
	
	
}
}
