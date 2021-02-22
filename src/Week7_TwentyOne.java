import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 21 - Constructivist
 * Every single function checks the sanity of its arguments and either
 * returns something sensible when the arguments are unreasonable or
 *  assigns them reasonable values.
 * All code blocks check for possible errors and escape the block when
 * things go wrong, setting the state to something reasonable, and continuing
 * to execute the rest of the function.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/21-constructivist/tf-21.py
 * https://www.baeldung.com/reading-file-in-java
 * https://stackoverflow.com/questions/29122394/word-frequency-count-java-8
 * Code written for Weeks 1-6
 */
public class Week7_TwentyOne {
	
	//read in lines from file normalize and return list of words
	public static Object extractWords(Object obj){
		List<String> wordList = new LinkedList<>();
		
		//if input was not string or not a valid path return empty list
		if(!(obj instanceof String)) {
			return wordList;
		}
		
		try {
			Paths.get((String)obj);
		}catch(InvalidPathException e) {
			return wordList;
		}
		
		try {

			wordList = Files.lines(Paths.get((String)obj))
					.filter(line -> !line.equals(""))	
					.map(line -> line.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim().split("\\s+"))
					.flatMap(Arrays::stream)
					.filter(word -> (!word.equals("") && !(word.length()<2)))
					.collect(Collectors.toList());		
			
		}catch(IOException e) {
			System.out.println("IO Error "+e.getCause() +" when opening "+(String)obj);
			return wordList;
		}
		
		return wordList;
	}
	
	//read in stop words from file return non-stop words in list
	public static Object removeStopWords(Object obj){
		Set<String> stopWords;
			
		//if input was not a list return empty list
		if(!(obj instanceof List)) {
				return new LinkedList<>();
			}
		try {
			stopWords = Files.lines(Paths.get("../stop_words.txt"))
					   .map(line -> line.split(","))
					   .flatMap(Arrays::stream)
					   .collect(Collectors.toSet());
			
		}catch(IOException e) {
			//if error on reading stop words file return list as is
			System.out.println("IO Error "+e.getMessage() +" when opening ../stop_words.txt");
			return obj;
		}
		
		List<String> wordList = (List<String>) obj;		
		wordList.removeIf(word -> stopWords.contains(word));
		
		return wordList;		
	}
	
	//count and return word frequencies in list
	public static Object frequencies(Object obj){
		
		//if input is not a list or an empty list return empty map
		if(!(obj instanceof List) || ((List<String>)obj).size()==0){
		return new LinkedHashMap<String, Long>();
		}

		Map<String, Long> wordFrequency = ((List<String>) obj).parallelStream()
							.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));	
		return wordFrequency;
	}
	
	//sort input map by value
	public static Map<String, Long> sort(Object obj){
		
		//if input is not a map or an empty map return empty map
		if(!(obj instanceof Map) || ((Map<String, Long>)obj).size()==0) {
			return new LinkedHashMap<String, Long>();
		}
		
		LinkedHashMap<String, Long> sortedMap = ((Map<String, Long>) obj).entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, 
						() -> new LinkedHashMap<String, Long>()));
		
		return sortedMap;
	}

	public static void main(String[] args) {
		String path;
		
		// if no argument specified or argument was an empty string
		try {
				if(args[0].length()>1)
					path = args[0];
				else
					path = "../pride-and-prejudice.txt";	
		}catch(ArrayIndexOutOfBoundsException e) {
			path = "../pride-and-prejudice.txt";
		}
	
		Map<String, Long> map = sort(frequencies(removeStopWords(extractWords(path))));
		
		map.entrySet().stream()
		.limit(25)
		.forEach(e -> System.out.println(e.getKey() +" - "+e.getValue()));
	}
}
