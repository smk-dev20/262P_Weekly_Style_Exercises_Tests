import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/*
 * MSWE 262P Week 1 Exercise 
 * Term Frequency
 * References :
 * https://www.javatpoint.com/how-to-sort-hashmap-by-value
 * https://www.tutorialspoint.com/get-the-current-working-directory-in-java
 * https://www.geeksforgeeks.org/path-getparent-method-in-java-with-examples/
 */
public class Week1_Ex_262P {

	/*
	 * method : getMaxFrequencies 
	 * input  : Map<String, Integer> - map of words and their frequencies 
	 * output : void 
	 * desc   : method converts input map to list, 
	 * 		    sorts list by value using overloaded Collections.sort(), 
	 * 		    generates new map from list of words sorted by frequency, 
	 *          prints first 25 values
	 */
	public static void getMaxFrequencies(Map<String, Integer> termFrequencies) {

		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(termFrequencies.entrySet());

		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
				return entry2.getValue().compareTo(entry1.getValue());
			}
		});

		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		int num = 25;
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			if (num > 0) {
				System.out.println(entry.getKey() + " - " + entry.getValue());
				num--;
			} else
				break;
		}
	}// end getMaxFrequencies
	
	/*
	 * method : getStopWords
	 * input  : String - path to stopwords file
	 * output : HashSet<String> - stopwords
	 * desc   : method scans stopwords file,			 		
	 * 			adds each unique stopword to a hashset
	 * 			returns generated hashset		
	 */
	public static HashSet<String> getStopWords(String stopFilePath) {
		String words = null;
		HashSet<String> stopWords = new HashSet<String>();
		try {
				File stopFile = new File(stopFilePath);
				Scanner scanStop = new Scanner(stopFile);
				while (scanStop.hasNextLine()) {
					words = scanStop.nextLine();
				}
				scanStop.close();

				for (String word : words.split(",")) {
					stopWords.add(word);
				}

		} catch (FileNotFoundException exception) {
			System.out.println("Error related to stop words file");
			exception.printStackTrace();
		}
		
		return stopWords;
	}// end getStopWords
	
	/*
	 * method : getTermFrequencies
	 * input  : String - path to text file, HashSet<String> - stopwords
	 * output : Map<String,Integer> - words and their frequencies
	 * desc   : method scans text file line by line, 
	 * 			ignores empty lines, converts all text to lower case,removes non-alphanumeric characters,
	 * 			adds each unique non-empty, non-stopword of length>2 to map keeping track of frequencies,
	 * 			returns generated map		
	 */
	public static Map<String, Integer> getTermFrequencies(String textFilePath, HashSet<String> stopWords) {
		String line = null;
		Map<String, Integer> termFrequencies = new LinkedHashMap<String, Integer>();

		try {
				File textFile = new File(textFilePath);
				Scanner scanText = new Scanner(textFile);

				while (scanText.hasNextLine()) {
					line = scanText.nextLine();
					if (line.equals(""))
						continue;
					line = line.replaceAll("[^a-zA-Z0-9]", " ");
					String[] wordsInLine = line.split("\\s+");

					for (String textWord : wordsInLine) {
						textWord = textWord.trim().toLowerCase();
						if (!textWord.equals("") && !stopWords.contains(textWord) && textWord.length() > 1) {
							if (!termFrequencies.containsKey(textWord)) {
								termFrequencies.put(textWord, 1);
							} else {
								termFrequencies.replace(textWord, termFrequencies.get(textWord) + 1);
						}
					}
				} // end for textWord
			} // end while scanText
			scanText.close();
		} catch (FileNotFoundException exception) {
			System.out.println("Error related to text file");
			exception.printStackTrace();
		}

		return termFrequencies;
	}// end getTermFrequencies
	
	/*
	 * method : main
	 * input  : text file name from command line
	 * output : void	
	 * desc   : determines current and parent directory paths,
	 * 			generates path to stop words file and text file as string,
	 * 			calls methods getStopWords(), getTermFrequencies(), getMaxFrequencies() 			
	 */
	public static void main(String[] args) {

		HashSet<String> stopWords = null;
		Map<String, Integer> termFrequencies = null;

		try {
				String currentDirectory = System.getProperty("user.dir");
				Path path = Paths.get(currentDirectory);

				String stopFilePath = path.getParent() + "/" + "stop_words.txt";
				stopWords = getStopWords(stopFilePath);

				String novel = args[0].trim();
				String textFilePath = path.getParent() + "/" + novel;
				termFrequencies = getTermFrequencies(textFilePath, stopWords);

				getMaxFrequencies(termFrequencies);
			} 
			catch (Exception exception) {
				System.out.println("Error in Main");
				exception.printStackTrace();
		}
	}// end main
}// end class Week1_Ex_262P
