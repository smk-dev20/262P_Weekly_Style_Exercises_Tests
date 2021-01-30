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

/*
 * MSWE 262P
 * Style 9 - Kick Forward / Continuous Passing Style
 * Variation of the Pipeline style, with the following additional constraints:
 * Each function takes an additional parameter, usually the last, which is
 * another function.
 * The function parameter is applied at the end of the current function.
 * The function parameter is given, as input, what would be the output of the current function.
 * The larger problem is solved as a pipeline of functions, but where the
 * next function to be applied is given as a parameter to the current
 * function.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/tree/master/09-kick-forward
 * Code written for Week1, Week2 & Week3
 */

//interface defined to allow for use of functions as objects
interface IFunction{
	void function(Object value, IFunction next);
}


//read in lines from text, send result to next class FilterCharacters
class ReadFile implements IFunction{
	 List<String> linesInText = new LinkedList<>();
	  
	@Override
	public void function(Object value, IFunction next) {
		String pathToFile = (String) value;

			File textFile = new File("../"+pathToFile.trim());
			Scanner scanText;
			try {
				String currentLine = null;
				scanText = new Scanner(textFile);
				while (scanText.hasNextLine()) {
					currentLine = scanText.nextLine();

					if (currentLine.equals(""))
						continue;
					linesInText.add(currentLine);
				}
				scanText.close();			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			next.function(linesInText, new Normalize());
	}	
}

//eliminate non-alphanumeric characters from lines and convert to lowercase
//send result to next class Normalize
 class FilterCharacters implements IFunction{
	
	@Override
	public void function(Object value, IFunction next) {
		List<String> linesInText = (List<String>)value;
		
		for (int i = 0; i < linesInText.size(); i++) {
			linesInText.set(i, linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
		}
		
		next.function(linesInText, new RemoveStopWords());
	}	
}
 
//separate lines into words and remove empty or words of size<2 
//send result to next class RemoveStopWords
 class Normalize implements IFunction{
	 List<String> wordsList = new LinkedList<>();
	@Override
	public void function(Object value, IFunction next) {
		List<String> linesInText = (List<String>)value;

		for (String line : linesInText) {
			wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
		}
		wordsList.removeIf(word -> (word.length() < 2 || word.equals("")));
		
		next.function(wordsList, new CountFrequencies());
	}	 
 }
 
 //read in stopwords file and remove stop words from main words list
 //send result to next class CountFrequencies
 class RemoveStopWords implements IFunction{

	@Override
	public void function(Object value, IFunction next) {
		List<String> wordsList = (List<String>)value;
		
		List<String> data = new LinkedList<>();
		try {
			data = Files.readAllLines(Paths.get("../stop_words.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<String>stopWords = new HashSet<String>(Arrays.asList(data.get(0).split(",")));
		wordsList.removeIf(word -> (stopWords.contains(word)));
		
		next.function(wordsList, new Sort());
	}	 
 }
 
 //determine frequencies of words, send result to next class Sort
 class CountFrequencies implements IFunction{
	 Map<String,Integer> map = new LinkedHashMap<>();
	@Override
	public void function(Object value, IFunction next) {
		List<String> wordsList = (List<String>)value;
		
		for(String word : wordsList) {
			map.put(word,map.getOrDefault(word, 0)+1);
		}		
		next.function(map,new Print());		
	}	 
 }
 
 //sort in descending order of frequencies, send result to next class Print
 class Sort implements IFunction{
	@Override
	public void function(Object value, IFunction next) {
		LinkedHashMap<String, Integer> sortedMap = ((LinkedHashMap<String, Integer>) value).entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an exception
						() -> new LinkedHashMap<String, Integer>()));
		
		next.function(sortedMap, null);
	}	 
 }
 
 //print top 25 words, no further action return to main
 class Print implements IFunction{

	@Override
	public void function(Object value, IFunction next) {
		int num = 25;
		for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) value).entrySet()) {
			if (num > 0) {
				System.out.println(entry.getKey() + " - " + entry.getValue());
				num--;
			} else
				break;
		}	
		return;
	}
 }

public class Week4_Nine {
public static void main(String[] args) {
	// call first class with name of file and a reference to the next class to call
	new ReadFile().function(args[0],new FilterCharacters());
}
}
