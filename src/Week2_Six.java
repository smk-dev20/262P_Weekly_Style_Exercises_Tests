import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 6 - Pipeline
 * Problem is decomposed into functions each taking input and returning an output
 * No shared states between functions
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/06-pipeline/tf-06.py
 * https://stackoverflow.com/questions/13565876/remove-all-occurrences-of-an-element-from-arraylist
 */
public class Week2_Six {
	public static List<String> readFile(String fileName) {
		// File textFile = new File("../"+fileName.trim());
		List<String> linesInText = new LinkedList<>();
		File textFile = new File(fileName.trim());
		Scanner scanText;
		try {
			String line = null;
			scanText = new Scanner(textFile);
			while (scanText.hasNextLine()) {
				line = scanText.nextLine();

				if (line.equals("")) 
					continue;
				
				linesInText.add(line);
			}
			scanText.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return linesInText;
	}

	/*
	 * Remove non-alphanumeric characters from lines in List
	 */
	public static List<String> filterCharacters(List<String> linesInText) {
		for (int i = 0; i < linesInText.size(); i++) {
			linesInText.set(i, linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
		}
		
		return linesInText;
	}

	/*
	 * Obtain individual words from list and add to another list
	 */
	public static List<String> getWords(List<String> linesInText) {
		List<String> wordsList = new LinkedList<>();
		for (String line : linesInText) {
			wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
		}
		return wordsList;
	}

	/*
	 * Read in stop words and store in a list Remove stop words from previously
	 * created list of words
	 */
	public static List<String> removeStopWords(List<String> wordsList) {
		List<String> stopWords = new LinkedList<>();
		try {
			stopWords = Files.readAllLines(Paths.get("stop_words.txt"));
			stopWords = new LinkedList<String>(Arrays.asList(stopWords.get(0).split(",")));
			for(int i = 0;i<wordsList.size();i++) {
				String word = wordsList.get(i);
				if(stopWords.contains(word) || word.length() < 2 || word.equals("")) {
					wordsList.removeAll(Collections.singleton(word));
				}			
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return wordsList;
	}

	/*
	 * Get word counts and store in a Map
	 */
	public static Map<String, Long> getFrequencies(List<String> wordsList) {
		Map<String, Long> wordFrequency = new HashMap<>();
		wordFrequency = wordsList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		return wordFrequency;
	}

	/*
	 * Sort word map by value, print top 25 values
	 */
	public static void printMaxFrequencies(Map<String, Long> wordFrequency) {
		LinkedHashMap<String, Long> map2 = wordFrequency.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an exception
						() -> new LinkedHashMap<String, Long>()));
		int num = 25;
		for (Map.Entry<String, Long> entry : map2.entrySet()) {
			if (num > 0) {
				System.out.println(entry.getKey() + " - " + entry.getValue());
				num--;
			} else
				break;
		}
	}
	
	/*
	 * Pipelined calls to functions
	 */
	public static void main(String[] args) {
		printMaxFrequencies(getFrequencies(removeStopWords(getWords(filterCharacters(readFile(args[0]))))));
	}
}
