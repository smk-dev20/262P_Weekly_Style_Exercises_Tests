import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 30 - DataSpaces
 * Existence of one or more units that execute concurrently.
 * Existence of one or more data spaces where concurrent units store and retrieve data.
 * No direct data exchanges between the concurrent units, other than via the data spaces.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/30-dataspaces/tf-30.py
 * https://www.codejava.net/java-core/concurrency/how-to-use-threads-in-java-create-start-pause-interrupt-and-join
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/LinkedBlockingQueue.html
 * Code wriiten for Exercise 3.3 244P UCI MSWE Fall 2020
 * Code written for Weeks 1-7
 */

/*
 * Worker class consumes words from wordSpace and calculates frequency of words it sees,
 * when wordspace is empty places its calculated frequencies into shared frequencySpace
 */
class Worker implements Runnable{
	
	private BlockingQueue<String> queue;
	private BlockingQueue<Map<String,Long>> frequencySpace; 
	private Set stopWords;
	private int id;
	private Map<String, Long> wordFrequency = new HashMap<>();
	
	public Worker(BlockingQueue<String> queue, BlockingQueue<Map<String,Long>> frequencySpace, Set stopWords, int id) {
		this.stopWords = stopWords;
		this.queue = queue;
		this.frequencySpace = frequencySpace;
		this.id = id;
	}
	
	public void run() {
		while(true) {
			//get value from queue until it is empty - poll() returns null when Q empty
			String word = queue.poll();
			if(word!=null) {
				if(!stopWords.contains(word))
					wordFrequency.put(word, wordFrequency.getOrDefault(word, (long) (0))+1);
			}else {
				//all words processed add counted words to shared space
				try {
					System.out.println("Worker thread "+id + " adding frequency counts to FrequencySpace");
					frequencySpace.put(wordFrequency);
					//end loop after adding
					break;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}//end run
	
}//end Worker class

public class Week8_Thirty {
	public static void main(String[] args) {
		
		//shared data space of all words
		BlockingQueue<String> wordSpace = new LinkedBlockingQueue<String>();
		//shared data space of partially counted words
		BlockingQueue<Map<String,Long>> frequencySpace = new LinkedBlockingQueue<Map<String,Long>>();
		
		//Set of stopWords
		Set<String> stopWords = null;
		
		//main thread acts as producer of words adding them to wordSpace
		try {
			List<String> wordsList = new LinkedList<>();
			wordsList = Files.lines(Paths.get(args[0]))
					.filter(line -> !line.equals(""))	
					.map(line -> line.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim().split("\\s+"))
					.flatMap(Arrays::stream)
					.filter(word -> (!word.equals("") && !(word.length()<2)))
					.collect(Collectors.toList());
			
			for(String word : wordsList)
				wordSpace.put(word);
			
			stopWords = Files.lines(Paths.get("../stop_words.txt"))
					   .map(line -> line.split(","))
					   .flatMap(Arrays::stream)
					   .collect(Collectors.toSet());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	//create and start worker threads to consume and process the words from wordspace	
		ArrayList<Thread> wordProcessors = new ArrayList<>();		
		for(int i=0;i<5;i++) {
			Runnable worker = new Worker(wordSpace,frequencySpace,stopWords, i);
			
			Thread t = new Thread(worker);
			wordProcessors.add(t);
			t.start();
		}
		
		//wait for all worker threads to finish
		for(Thread processor: wordProcessors) {
			try {
				processor.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//main thread consumes from frequencySpace and merges counts for all words
		Map<String,Long> mergedFrequencies = new HashMap<String,Long>();
		while(!frequencySpace.isEmpty()) {
			Map<String, Long> freqMap = frequencySpace.poll();
			if(freqMap!=null) {
				for(Map.Entry map : freqMap.entrySet()) {
					if(!mergedFrequencies.containsKey(map.getKey())) {
						mergedFrequencies.put((String)map.getKey(), (Long)map.getValue());
					}else {
							long currentCount = mergedFrequencies.get(map.getKey());
							mergedFrequencies.put((String)map.getKey(), currentCount+(Long)map.getValue());
					}
				}
			}
		}
		
		//print top 25 sorted frequencies
		((Map<String, Long>) mergedFrequencies).entrySet().stream()
		.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		.limit(25)
		.forEach(e -> System.out.println(e.getKey() +" - "+e.getValue()));
	}
}
