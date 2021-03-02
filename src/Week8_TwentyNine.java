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
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 29 - Actors
 * The larger problem is decomposed into things that make sense for the problem domain.
 * Each thing has a queue meant for other things to place messages in it.
 * Each thing is a capsule of data that exposes only its ability to receive messages via the queue.
 * Each thing has its own thread of execution independent of the others.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/12-letterbox/tf-12.py
 * https://github.com/crista/exercises-in-programming-style/blob/master/29-actors/tf-29.py
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ArrayBlockingQueue.html
 * Code written for Weeks 1-7
 */

/*
 * Thread class inherited by all Actors 
 * On actor object creation every actor has its own thread and a queue which will contain
 * messages and payload for the actor to work on.
 * 
 */
class ActiveWFObject extends Thread {

	private String name;
	protected boolean stopMe;
	private Thread curThread;
	//thread safe blocking queue - so that no messages are lost
	protected ArrayBlockingQueue<List<Object>> queue; 

	ActiveWFObject() {
		curThread = new Thread(this);
		name = this.getClass().toString();
		queue = new ArrayBlockingQueue(100);
		stopMe = false;
		curThread.start();
	}
	
	void dispatch(List<Object> message) {
		//overridden by children		
	}
	
	//run() called by start of thread 
	//As long as thread is not killed thread will read
	//data from queue and process it, if there is no message in the queue thread blocks until there is data
	public void run() {
		while(!this.stopMe) {
			try {
				List<Object> message = queue.take();
				this.dispatch(message);
				
				if(((String)message.get(0)).equals("die")) {
					this.stopMe = true;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}//end class Active WFObject


/*
 * DataStorageActor
 * Based on message in the queue will read in lines from text 
 * or process words and place the words in the Queue for the StopWordActor
 * If message is one that it does not recognize forwards message to StopWordActor
 */
class DataStorageActor extends ActiveWFObject {
	private StopWordActor stopWordManager;
	private ControllerActor wordFreqController;
	private List<String> linesInText = new LinkedList<>();
	private List<String> wordsList = new LinkedList<>();

	public void dispatch(List<Object> message) {

		if (((String) message.get(0)).equals("init")) {
			this.init(message);
		} else if (((String) message.get(0)).equals("sendWordFreq")) {
			this.processWords(message);
		} else {
			List<Object> msgCopy = message;
			try {
				this.stopWordManager.queue.put(msgCopy);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}// end dispatch

	public void init(List<Object> message) {
		String path = (String) message.get(1);
		this.stopWordManager = (StopWordActor) message.get(2);
		try {
			linesInText = Files.lines(Paths.get(path)).filter(line -> !line.equals(""))
					.map(line -> line.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim())
					.collect(Collectors.toList());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// end init

	public void processWords(List<Object> message) {
		Object recipient = message.get(1);

		this.wordFreqController = (ControllerActor) recipient;

		wordsList = linesInText.parallelStream().map(line -> line.split("\\s+")).flatMap(Arrays::stream)
				.filter(word -> (!word.equals("") && !(word.length() < 2))).collect(Collectors.toList());

		for (String word : wordsList) {
			String wrdCopy = word;
			try {
				this.stopWordManager.queue.put(Arrays.asList("filter", wrdCopy));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			this.stopWordManager.queue.put(Arrays.asList("top25", this.wordFreqController));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// end processWords

}// end DataStorageActor class

/*
 * StopWordActor
 * Based on message in its queue will read and store stopwords or
 * filter word received from DataStorageActor and place non-stop word in Queue of WordFrequencyActor
 * If it does not recognize the input message forwards it to WordFrequencyActor
 */
class StopWordActor  extends ActiveWFObject {
		private Set<String> stopWords = new HashSet<String>();
		private WordFrequencyActor wordFrequencyManager;
		
		public void dispatch(List<Object> message) {
			if(((String)message.get(0)).equals("init")) {
				init(message);
			}
			else if (((String)message.get(0)).equals("filter")) {
				this.filter(message);
			}else {
				try {
					this.wordFrequencyManager.queue.put(message);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
		}//end dispatch
		
		private void init(List<Object> message) {
			try {
				stopWords = Files.lines(Paths.get("../stop_words.txt"))
						   .map(line -> line.split(","))
						   .flatMap(Arrays::stream)
						   .collect(Collectors.toSet());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.wordFrequencyManager = (WordFrequencyActor) message.get(1);
		}//end init
		
		private void filter(List<Object> message) {
		
			String word = (String)message.get(1);
			if(!stopWords.contains(word)){
				try {
					this.wordFrequencyManager.queue.put(Arrays.asList("word", word));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
	}//end filter
}//end StopWordActor class

/*
 * WordFrequencyActor
 * Based on the message in the queue will calculate the frequency for the input word or
 * Sort all frequencies in descending order and place the sorted frequencies in the queue
 * of the ControllerActor
 */
class WordFrequencyActor  extends ActiveWFObject {
		  ControllerActor wordFreqController;
			private Map<String,Long> wordFrequency = new HashMap<>();
			
			public void dispatch(List<Object> message) {

				if(((String)message.get(0)).equals("word")) {
					this.incrementCount(message);
				}
				else if (((String)message.get(0)).equals("top25")) {
					this.top25(message);
				}
			}//end dispatch
			
			private void incrementCount(List<Object> message) {

				String word = (String)message.get(1);
				wordFrequency.put(word, wordFrequency.getOrDefault(word, (long) 0)+1);

			}//end incrementCount
			
			private void top25(List<Object> message) {

				Object recipient = message.get(1);
				this.wordFreqController = (ControllerActor) recipient;
				Map<String, Long> freqsSorted = sorted(wordFrequency);
				try {
					this.wordFreqController.queue.put(Arrays.asList("top25",freqsSorted));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}//end top25
			
			private Map<String, Long> sorted(Map<String, Long> wordFreq){
				 return ((Map<String, Long>) wordFreq).entrySet().stream()
							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, 
								() -> new LinkedHashMap<String, Long>()));
			}//end sorted
			
}//end WordFrequencyActor class


/*
 * ControllerActor
 * Based on message in its queue will place the appropriate message in the queue of DataStorageActor
 * to initiate the forwarding of words for counting or prints the top25 words before
 * issuing a termination message to DataStorageActor and terminating itself
 */
class ControllerActor extends ActiveWFObject{

			private DataStorageActor storageManager;
			
			public void dispatch(List<Object> message) {
				if(((String)message.get(0)).equals("run")) {
					this.run(message);
				}else if(((String)message.get(0)).equals("top25")) {
					this.display(message);
				}else {
					System.out.println("Message not understood in ContollerActor "+message.get(0));
					System.exit(0);
				}
			}//end dispatch
			
			private void run(List<Object> message) {
				this.storageManager = (DataStorageActor) message.get(1);
				try {
					this.storageManager.queue.put(Arrays.asList("sendWordFreq", this));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			private void display(List<Object> message) {
				Map<String, Long> map = (Map<String, Long>) message.get(1);
				((Map<String, Long>) map).entrySet().stream()
										 .limit(25)
				                         .forEach(e -> System.out.println(e.getKey() +" - "+e.getValue()));
				
				try {
					this.storageManager.queue.put(Arrays.asList("die"));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				this.stopMe = true;
			}			
}//end ControllerActor class
				
public class Week8_TwentyNine {	
	public static void main(String[] args) {
		try {		
			//create Actor objects to initiate threads and Actor Queues - all actors will work concurrently
			WordFrequencyActor wordFreqManager = new WordFrequencyActor();	
			StopWordActor stopWordManager = new StopWordActor();
			DataStorageActor storageManager = new DataStorageActor();
			ControllerActor wordFreqController = new ControllerActor();
			
			//place initial messages onto Actor queues
			stopWordManager.queue.put(Arrays.asList("init", wordFreqManager));
			storageManager.queue.put(Arrays.asList("init",args[0],stopWordManager));			
			wordFreqController.queue.put(Arrays.asList("run",storageManager));
			
			//Wait for all Actor threads to finish
			for(Thread t : Arrays.asList(wordFreqManager,stopWordManager,storageManager,wordFreqController)){
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}//end main
}//end class Week8
