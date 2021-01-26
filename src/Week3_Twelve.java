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
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 12 - Letterbox
 * Problem is decomposed into things (here classes) which expose a single procedure
 * that has the ability to receive and dispatch messages
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/12-letterbox/tf-12.py
 * Code written for Week1 and Week2
 */
public class Week3_Twelve {
	
	//read in data from file and store words in list
	static class DataStorageManager{
		private List<String> linesInText = new LinkedList<>();
		private  List<String> wordsList = new LinkedList<>();
			
		public List<String> dispatch(List<String> message) {
			if(message.get(0).equals("init")) {
		
				return this.init(message.get(1));
			}else if(message.get(0).equals("words")) {
				return this.words();
			}else {
				System.out.println("Message not understood DataStorageManager "+message.get(0));
				System.exit(0);
			}
			return message;			
		}
		
		private List<String> init(String filePath){
			File textFile = new File("../"+filePath.trim());
			//File textFile = new File(filePath.trim());
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
		
		private List<String> words(){
			for (int i = 0; i < linesInText.size(); i++) {
				linesInText.set(i, linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
			}
			for (String line : linesInText) {
				wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
			}
			wordsList.removeIf(word -> (word.length() < 2 || word.equals("")));
			return wordsList;
		}
		
	}	

	//generate list of stopwords and check for stopwords
 static	class StopWordManager{
		private List<String> stopWords = new LinkedList<>();
	
		public Object dispatch(List<String>message) {
			
			if(message.get(0).equals("stop")){
				return this.stopWords();			
			}else if(message.get(0).equals("isStopWord")) {
				return stopWords.contains(message.get(1));
			}
			
			else {
				System.out.println("Message not understood StopWord Manager list dispatch "+message.get(0));
				System.exit(0);
			}
		
			return message;
		}
		
		private List<String> stopWords(){
			try {
				stopWords = Files.readAllLines(Paths.get("../stop_words.txt"));
				stopWords = new LinkedList<String>(Arrays.asList(stopWords.get(0).split(",")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return stopWords; 
		}
	}	
	
 //determine word frequencies and sort
 static class WordFrequencyManager{
		private Map<String, Long> wordFrequency = new HashMap<>();
		
		public Map<String, Long> dispatch(List<String> message){
			if(message.get(0).equals("incrementCount")) {
				return this.incrementCount(message.get(1));
			}else if(message.get(0).equals("sorted")) {
				return this.sorted();
			}else {
		
				System.out.println("Message not understood in WordFrequencyManager "+message.get(0));
				System.exit(0);
			}
			//empty return
			return new HashMap<>();			
		}
		
		
		private Map<String, Long> incrementCount(String word){
		wordFrequency.put(word, wordFrequency.getOrDefault(word, (long) (0))+1);
				
		 return wordFrequency;
		}
		
		private Map<String, Long> sorted(){
			LinkedHashMap<String, Long> map2 = wordFrequency.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an exception
							() -> new LinkedHashMap<String, Long>()));
			
			return map2;
		}
	}

 //control class initializes all others
	static class WordFrequencyController{
		
		private static DataStorageManager storageManager;
		private static  StopWordManager stopWordManager;
		private static  WordFrequencyManager wordFrequencyManager;
		
		public void dispatch(List<String> message){
	
			if(message.get(0).equals("init")) {
				 this.init(message.get(1));
			}else if(message.get(0).equals("run")) {
				 this.run();
			}else {
		
				System.out.println("Message not understood in WordFrequencyController "+message.get(0));
				System.exit(0);
			}		
		}
		
		private void init(String filePath) {
		
			storageManager = new DataStorageManager();
			stopWordManager = new StopWordManager();
			wordFrequencyManager = new WordFrequencyManager();
			
			storageManager.dispatch(Arrays.asList("init",filePath));
			stopWordManager.dispatch((Arrays.asList("stop")));
		}
		
		private void run() {
			for(String word :storageManager.dispatch(Arrays.asList("words"))) {
				if (!(Boolean)stopWordManager.dispatch(Arrays.asList("isStopWord",word))){
					wordFrequencyManager.dispatch(Arrays.asList("incrementCount",word));
				}
			}
						
			Map<String, Long> frequencies = wordFrequencyManager.dispatch(Arrays.asList("sorted"));
			
			int num = 25;
			for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
				if (num > 0) {
					System.out.println(entry.getKey() + " - " + entry.getValue());
					num--;
				} else
					break;
			}
		}
		

	}
	
	public static void main(String[] args) {
		WordFrequencyController freqController = new WordFrequencyController();
		freqController.dispatch(Arrays.asList("init",args[0]));
		freqController.dispatch(Arrays.asList("run"));
	}

}
