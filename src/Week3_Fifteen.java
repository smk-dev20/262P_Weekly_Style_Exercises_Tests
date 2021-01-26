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
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 15 - Hollywood
 * Problem is decomposed into entities.
 * Entities provide interfaces for others to register for callbacks
 * At different times in computation entities call the others that have registered
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/15-hollywood/tf-15.py
 * Code written for Week1 and Week2 and Thirteen for Week3
 */


public class Week3_Fifteen {
	
	//framework registers entities and calls them on event occurrence
	static class WordFrequencyFrameWork{
		private List<Object> loadEventHandlers = new LinkedList<>();
		private List<Object> workEventHandlers = new LinkedList<>();
		private List<Object> endEventHandlers = new LinkedList<>();
		
		public void registerForLoadEvent(Object handler) {
			this.loadEventHandlers.add( handler);
		}
		
		public void registerForWorkEvent(Object handler) {
			this.workEventHandlers.add(handler);
		}
		
		public void registerForEndEvent(Object handler) {
			this.endEventHandlers.add(handler);
		}
		
		public void run(String filePath) {
			for(Object handler : this.loadEventHandlers) {
				if(handler instanceof Consumer)
					((Consumer<String>) handler).accept(filePath);
				if(handler instanceof Runnable)
					((Runnable) handler).run();
			}
			
			for(Object handler : this.workEventHandlers) {
				((Runnable) handler).run();
			}
			
			for(Object handler : this.endEventHandlers) {
				((Runnable) handler).run();
			}	
		}		
	}//end class WordFrequencyFrameWork

	//read in data process and store in list
 static class DataStorage{
		private List<String> linesInText = new LinkedList<>();
		private  List<String> wordsList = new LinkedList<>();
		private StopWordsFilter stopFilter = null;
		private List<Object> wordEventHandlers = new LinkedList<>();
		
		private DataStorage(WordFrequencyFrameWork wfApp, StopWordsFilter stopFilter) {
			this.stopFilter = stopFilter;
			
			Consumer<String> func = path -> load(path);
			wfApp.registerForLoadEvent(func);
			wfApp.registerForWorkEvent(produceWords);
		}
		
		private void load(String filePath) {
			File textFile = new File("../"+filePath.trim());
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
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	Runnable produceWords = () -> {
		for (String line : linesInText) {
			wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
		}
		wordsList.removeIf(word -> (word.length() < 2 || word.equals("")));
		
		for(String word : wordsList) {
			if(!this.stopFilter.isStopWord(word)) {
				for(Object handler : this.wordEventHandlers) {
					((Consumer<String>) handler).accept(word);
				}
			}
		}
	};
	
	public void registerForWordEvent(Object handler) {
		this.wordEventHandlers.add(handler);
	} 
	
	}//end class DataStorage
	
	
 //generate stop words check for stop word
static class StopWordsFilter{
		private List<String> stopWords = new LinkedList<>();
		
		private StopWordsFilter(WordFrequencyFrameWork wfApp) {
			Runnable func = () -> load();
			wfApp.registerForLoadEvent(func);
		}
		
		private void load(){
			try {
				stopWords = Files.readAllLines(Paths.get("../stop_words.txt"));
				stopWords = new LinkedList<String>(Arrays.asList(stopWords.get(0).split(",")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public boolean isStopWord(String word) {
			return stopWords.contains(word);
		}		
		
	}//end class StopWordsFilter

	//determine frequency sort and print
	static class WordFrequencyCounter{
		
		private Map<String, Long> wordFrequency = new HashMap<>();
		
		private WordFrequencyCounter(WordFrequencyFrameWork wfApp, DataStorage dataStorage) {
			Consumer<String> func = currentWord -> incrementCount(currentWord);
			dataStorage.registerForWordEvent(func);
			wfApp.registerForEndEvent(printFrequencies);
		}
		
		private void incrementCount(String word) {
			wordFrequency.put(word, wordFrequency.getOrDefault(word, (long) (0))+1);
		}
		
		Runnable printFrequencies =()-> {
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
		};
		
	}//end class WordFrequencyCounter
	
	/****************** Upto here 15.1 ***********************************************/
	
	//registers for word events, counts z words and prints
	static class ZFrequencyCounter{
		private int zFreq = 0;
		private ZFrequencyCounter(WordFrequencyFrameWork wfApp, DataStorage dataStorage) {
			Consumer<String> func = currentWord -> incrementCount(currentWord);
			dataStorage.registerForWordEvent(func);
			wfApp.registerForEndEvent(printZFrequency);
		}
		
		private void incrementCount(String word) {
			if(word.contains("z")){
				zFreq++;
			}
		}
		
		Runnable printZFrequency =()-> {
			System.out.println("Words with z - "+zFreq);
		};
		
	}//end zFrequency
	
	
public static void main(String[] args) {
	
	WordFrequencyFrameWork wfApp = new WordFrequencyFrameWork();	
	StopWordsFilter stopFilter = new StopWordsFilter(wfApp);
	DataStorage dataStorage = new DataStorage(wfApp, stopFilter);
	WordFrequencyCounter freqCounter = new WordFrequencyCounter(wfApp, dataStorage);
	
	//added for 15.2
	ZFrequencyCounter zCounter = new ZFrequencyCounter(wfApp,dataStorage);
	
	wfApp.run(args[0]);
	
}
}
