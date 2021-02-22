import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 25 - Quarantine
 * Core program functions have no side eects of any kind, including IO.
 * All IO actions must be contained in computation sequences that are
 * clearly separated from the pure functions. All sequences that have IO must be called from the main program.
 * 
 * References
 * https://stackoverflow.com/questions/7097275/function-within-a-function-in-java
 * https://github.com/crista/exercises-in-programming-style/blob/master/25-quarantine/tf-25.py
 * https://stackoverflow.com/questions/22742974/in-java-8-how-do-i-transform-a-mapk-v-to-another-mapk-v-using-a-lambda
 * Code written for Weeks 1-6, TwentyOne.java
 */

//interface defined to use functions as objects
interface QInterface{
	Object call(Object obj);
}


//abstraction to which values will be converted
class TFQuarantine {
	//contains returned value from each function
	private Object result;
	//list of functions to be executed
	private List<QInterface> functions;

	//initialize list and obtain starting parameter for very first function
	TFQuarantine(Object value) {
		this.functions = new ArrayList<>();
		this.result = value;
	}

	//on every bind add caller to list
	public Object bind(QInterface value) {
		this.functions.add(value);
		return this;
	}
	
	//iterate over list of functions passing the output to the next function in list
	public void execute() {
		for(Object obj : functions) {
			result = ((QInterface) obj).call(result);
		}
		
		System.out.println(result.toString());
	}
}

public class Week7_TwentyFive {

	//read in lines from text	
//eliminate non-alphanumeric characters from lines, convert to lowercase and return words
	public static QInterface extractWords() {
		
		QInterface func = new QInterface() {
			List<String> wordsList = new LinkedList<>();
		
			@Override
			public Object call(Object obj) {
				String path = (String) obj;
				try {
					wordsList = Files.lines(Paths.get(path)).filter(line -> !line.equals(""))
							.map(line -> line.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim().split("\\s+"))
							.flatMap(Arrays::stream).filter(word -> (!word.equals("") && !(word.length() < 2)))
							.collect(Collectors.toList());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return wordsList;
			}
		};
		return func;

	}

//read in stopwords file and remove stop words from main words list

	public static QInterface removeStopWords() {
		QInterface func = new QInterface() {
			Set<String> stopWords;

			@Override
			public Object call(Object obj) {
				List<String> wordsList = (List<String>) obj;
				try {
					
					stopWords = Files.lines(Paths.get("../stop_words.txt")).map(line -> line.split(","))
							.flatMap(Arrays::stream).collect(Collectors.toSet());
					wordsList.removeIf(word -> stopWords.contains(word));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return (wordsList);
		}
		};
		return func;
	}

//determine frequencies of words
	public static QInterface frequencies() {
		
		QInterface func = new QInterface() {
			public Object call(Object obj) {
				List<String> wordsList = (List<String>) obj;
				Map<String, Long> wordFrequency = wordsList.parallelStream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
				return wordFrequency;
			}
		};
		return func;
	}
	
	public static QInterface sort() {
		
		QInterface func = new QInterface() {	
			public  Object call(Object obj) {
				Map<String,Long> wordFrequency = (Map<String,Long>) obj;
				LinkedHashMap<String, Long> sortedMap = wordFrequency.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, 
						() -> new LinkedHashMap<String, Long>()));
				return sortedMap;
			}
		};
		return func;
	}
	//Obtain top 25 words append to StringBuilder
	
	public static QInterface top25() {
		
		QInterface func = new QInterface() {
			public Object call(Object obj) {
				Map<String, Long> wordFrequency = (Map<String, Long>) obj;
				int num = 25;
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, Long> entry : wordFrequency.entrySet()) {
					if (num > 0) {
						sb.append(entry.getKey() + " - " + entry.getValue() + "\n");
						num--;
					} else
						break;
				}
				return sb;
			}
		};
		return func;
	}
	
	
	public static void main(String[] args) {
		//Instantiate the monad
		TFQuarantine one = new TFQuarantine(args[0]);
			
		//binding functions to IOMonad type casting as appropriate
		((TFQuarantine) ((TFQuarantine) ((TFQuarantine) ((TFQuarantine) ((TFQuarantine) one.bind(extractWords()))
		.bind(removeStopWords()))
		.bind(frequencies()))
		.bind(sort()))
		.bind(top25()))
		.execute();

	}

}
