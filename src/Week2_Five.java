import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Style 5 - Cookbook
 * Larger problem subdivided into smaller procedures
 * Procedures share data and change the shared data based on their functionality
 * Procedures are called in a sequence as needed by the task
 * 
 * References :
 * https://github.com/crista/exercises-in-programming-style/blob/master/05-cookbook/tf-05.py
 * https://www.techiedelight.com/count-frequency-elements-list-java/
 * https://www.baeldung.com/java-file-to-arraylist
 * https://dzone.com/articles/removing-items-from-arraylist-in-javanbsp8
 * https://stackoverflow.com/questions/55644583/how-to-sort-a-hashmap-based-on-values-in-descending-order
 */
public class Week2_Five {
	/*
	 * Shared data used by procedures, state of data is changed by procedures
	 */
	public static List<String> linesInText = new LinkedList<>();
	public static List<String> stopWords = new LinkedList<>();
	public static List<String> wordsList = new LinkedList<>();
	public static Map<String, Long> wordFrequency = new HashMap<>();
	
	/*
	 * Read file whose name is provided in cmd line 
	 * Write each line in file to a list
	 */
	public static void readFile(String fileName) {
		// File textFile = new File("../"+fileName.trim());
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
	}

	/*
	 * Remove non-alphanumeric characters from lines in List
	 */
	public static void filterCharacters() {
		for (int i = 0; i < linesInText.size(); i++) {
			linesInText.set(i, linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
		}
	}

	/*
	 * Obtain individual words from list and add to another list
	 */
	public static void getWords() {
		for (String line : linesInText) {
			wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
		}
	}

	/*
	 * Read in stop words and store in a list 
	 * Remove stop words from previously created list of words
	 */
	public static void removeStopWords() {
		
		try {
			stopWords = Files.readAllLines(Paths.get("stop_words.txt"));
			stopWords = new LinkedList<String>(Arrays.asList(stopWords.get(0).split(",")));

			wordsList.removeIf(word -> (stopWords.contains(word) || word.length() < 2 || word.equals("")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Get word counts and store in a Map
	 */
	public static void getFrequencies() {
		wordFrequency = wordsList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	/*
	 * Sort word map by value, print top 25 values
	 */
	public static void printMaxFrequencies() {
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
	 * main method calls procedures in required sequence to obtain needed results
	 */
	public static void main(String[] args) {
		readFile(args[0]);
		filterCharacters();
		getWords();
		removeStopWords();
		getFrequencies();
		printMaxFrequencies();
	}
}
