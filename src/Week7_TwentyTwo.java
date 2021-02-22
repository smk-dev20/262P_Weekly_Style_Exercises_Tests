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
 * Every single procedure and function checks the sanity of its arguments
 * and refuses to continue when the arguments are unreasonable.
 * All code blocks check for all possible errors, possibly log context-specic
 * messages when errors occur, and pass the errors up the function call chain.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/22-tantrum/tf-22.py
 * Code written for Weeks 1-6, TwentyOne.java
 */
public class Week7_TwentyTwo {
	//read in lines from file normalize and return list of words
		public static Object extractWords(Object obj) throws IOException{
			List<String> wordList = new LinkedList<>();
			
			//if input was not string or not a valid path return after printing message
			if(!(obj instanceof String)) {
				System.out.println("I need a string!");
				System.exit(0);
			}
			
			try {
				Paths.get((String)obj);
			}catch(InvalidPathException e) {
				System.out.println("That was not a valid path! Why did you give me an empty one?");
				System.exit(0);
			}
			
			try {

				wordList = Files.lines(Paths.get((String)obj))
						.filter(line -> !line.equals(""))	
						.map(line -> line.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim().split("\\s+"))
						.flatMap(Arrays::stream)
						.filter(word -> (!word.equals("") && !(word.length()<2)))
						.collect(Collectors.toList());		
				
			}catch(IOException e) {
				System.out.println("IO Error when opening "+(String)obj+" I quit!");
				throw e;
			}
			
			return wordList;
		}
		
		//read in stop words from file return non-stop words in list
		public static Object removeStopWords(Object obj) throws IOException{
			Set<String> stopWords;
				
			//if input was not a list print message and return
			if(!(obj instanceof List)) {
				System.out.println("I need a list!");
				System.exit(0);
				}
			try {
				stopWords = Files.lines(Paths.get("../stop_words.txt"))
						   .map(line -> line.split(","))
						   .flatMap(Arrays::stream)
						   .collect(Collectors.toSet());
				
			}catch(IOException e) {
				//if error on reading stop words file return list as is
				System.out.println("IO Error when opening ../stop_words.txt. I quit!");
				throw e;
			}
			
			List<String> wordList = (List<String>) obj;		
			wordList.removeIf(word -> stopWords.contains(word));
			
			return wordList;
		}
		
		//count and return word frequencies in list
		public static Object frequencies(Object obj){
			
			//if input is not a list or an empty list return print message and return
			if(!(obj instanceof List)){
				System.out.println("I need a list!");
				System.exit(0);
			}
			if( ((List<String>)obj).size()==0) {
				System.out.println("I need a non-empty list!");
				System.exit(0);
			}

			Map<String, Long> wordFrequency = ((List<String>) obj).parallelStream()
								.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));	
			return wordFrequency;
		}
		
		//sort input map by value
		public static Map<String, Long> sort(Object obj){
			
			//if input is not a map or an empty map print msg and return
			if(!(obj instanceof Map)) {
				System.out.println("I need a map!");
				System.exit(0);
			}
			if(((Map<String, Long>)obj).size()==0) {
				System.out.println("I need a non-empty map!");
				System.exit(0);
			}
			LinkedHashMap<String, Long> sortedMap=null;
			try {
			 sortedMap = ((Map<String, Long>) obj).entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, 
						() -> new LinkedHashMap<String, Long>()));
			}catch(Exception e) {
				System.out.println("Sorted threw "+e.getCause().toString());
				throw e;
			}
			
			return sortedMap;
		}

		public static void main(String[] args) {
			String path=null;
			
			// if no argument specified or argument was an empty string
			try {
					if(args[0].length()>1)
						path = args[0];
					else {
						System.out.println("Are you kidding me? that path was an empty string!");
						System.exit(0);
					}
			}catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("You idiot! I need an input file!");	
				System.exit(0);
			}
		
			Object map = null;
			try {
				map = sort(frequencies(removeStopWords(extractWords(path))));
				if(!(map instanceof Map)){
					System.out.println("What is this? This is not a map!");
					System.exit(0);
				}
				if( ((Map<String,Long>)map).size()<25) {
					System.out.println("SRSLY? Less than 25 words!");
					System.exit(0);
				}
				
				((Map<String, Long>) map).entrySet().stream()
				.limit(25)
				.forEach(e -> System.out.println(e.getKey() +" - "+e.getValue()));
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Something is wrong "+e1.getCause().toString());
				e1.printStackTrace();
			}
			
		}
}
