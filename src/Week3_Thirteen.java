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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 13 - Closed Maps
 * Problem is decomposed into things, each thing is a map from keys to values. Some values are 
 * procedures/functions
 * Procedures/functions close on the map itself by referring to its slots
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/12-letterbox/tf-12.py
 * Code written for Week1 and Week2
 */


public class Week3_Thirteen {

public static void extractWords(Map<String, Object> map, String filePath) {
 List<String> linesInText = new LinkedList<>();
  List<String> wordsList = new LinkedList<>();
	File textFile = new File(filePath.trim());
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
		for (int i = 0; i < linesInText.size(); i++) {
			linesInText.set(i, linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
		}
		for (String line : linesInText) {
			wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
		}
		wordsList.removeIf(word -> (word.length() < 2 || word.equals("")));
		
		map.put("data", wordsList);
	
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}


public static void loadStopWords(Map<String, Object> map) {
	List<String> stopWords = new LinkedList<>();
	try {
		stopWords = Files.readAllLines(Paths.get("stop_words.txt"));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	stopWords = new LinkedList<String>(Arrays.asList(stopWords.get(0).split(",")));
	
	map.put("stopWords",stopWords);
}

public static void incrementCount(Map<String, Object> map, String word) {
	Map<String, Long>frequencies =(Map) map.get("freqs");
	if(!frequencies.containsKey(word)){
		frequencies.put(word,(long)1);
	}else {
		frequencies.put(word, frequencies.getOrDefault(word, (long)0)+1);
	}
	map.put("freqs",(Object)frequencies);

}

public static void sorted(Map<String, Object>map) {
	Map<String, Long>frequencies =(Map) map.get("freqs");
	LinkedHashMap<String, Long> map2 = frequencies.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an exception
					() -> new LinkedHashMap<String, Long>()));
	
	map.put("sorted",map2);
}


@SuppressWarnings("unchecked")
public static void main(String[] args) {

	Map<String, Object> dataStorageObj = new HashMap<String, Object>();
	Map<String, Object> stopWordsObj = new HashMap<String, Object>();
	Map<String, Object> wordFreqObj = new HashMap<String, Object>();

	// lambdas for datastorage
	Consumer<String> func = path -> extractWords(dataStorageObj, path);
	Supplier<List<String>> func2 = () -> (List<String>) dataStorageObj.get("data");

	// lambdas for stopWords
	Consumer<Map<String, Object>> stopfunc = obj -> loadStopWords(obj);
	Function<String, Boolean> isStopfunc = word -> ((List<String>) stopWordsObj.get("stopWords")).contains(word);

	// lambdas for wordFreq
	Consumer<String> wordFunc = currentWord -> incrementCount(wordFreqObj, currentWord);
	Consumer<Map<String, Object>> sortFunc = freqObj -> sorted(freqObj);

	dataStorageObj.put("data", new ArrayList<String>());
	dataStorageObj.put("init", func);
	dataStorageObj.put("words", func2);

	stopWordsObj.put("stopWords", new ArrayList<String>());
	stopWordsObj.put("init", stopfunc);
	stopWordsObj.put("isStopWord", isStopfunc);

	wordFreqObj.put("freqs", new HashMap<String, Long>());
	wordFreqObj.put("incrementCount", wordFunc);
	wordFreqObj.put("sorted", sortFunc);

	// initialize datastore and stopwords
	((Consumer<String>) dataStorageObj.get("init")).accept(args[0]);
	((Consumer<Map<String, Object>>) stopWordsObj.get("init")).accept(stopWordsObj);

	// create word-freq pairs
	for (String word : ((Supplier<List<String>>) dataStorageObj.get("words")).get()) {
		if (!((Function<String, Boolean>) stopWordsObj.get("isStopWord")).apply(word)) {
			((Consumer<String>) wordFreqObj.get("incrementCount")).accept(word);
		}
	}
//Sort and print
//	((Consumer<Map<String, Object>>) wordFreqObj.get("sorted")).accept(wordFreqObj);
//
//	Map<String, Long>frequencies =(Map) wordFreqObj.get("sorted");
//	int num = 25;
//	for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
//		if (num > 0) {
//			System.out.println(entry.getKey() + " - " + entry.getValue());
//			num--;
//		} else
//			break;
//	}
	/****************** Upto here was 13.1 *********************/
	
   //sort and print functionailty added to Runnable for requirement 13.2
	Runnable runnable = () -> {
		((Consumer<Map<String, Object>>) wordFreqObj.get("sorted")).accept(wordFreqObj);

		Map<String, Long> frequencies = (Map) wordFreqObj.get("sorted");
		int num = 25;
		for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
			if (num > 0) {
				System.out.println(entry.getKey() + " - " + entry.getValue());
				num--;
			} else
				break;
		}
	};

	//Runnable method added to wordFreq map
	wordFreqObj.put("top25", new Thread(runnable));
    //calling added method
	((Thread) wordFreqObj.get("top25")).start();
}


}


