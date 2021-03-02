import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 32 - Double Map Reduce
 * Input data is divided in blocks.
 * A map function applies a given worker function to each block of data, potentially in parallel.
 * The results of the many worker functions are reshuffled.
 * The reshuffled blocks of data are given as input to a second map function that takes a reducible function as input.
 * Optional step: a reduce function takes the results of the many worker functions and recombines them into a coherent output.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/32-double-map-reduce/tf-32.py
 * https://stackoverflow.com/questions/27130005/how-to-read-n-amount-of-lines-from-a-file
 * https://stackoverflow.com/questions/51428490/java-8-stream-mapstring-liststring-sum-of-values-for-each-key
 * Code written for Weeks 1-7
 */
public class Week8_ThirtyTwo {
	
	//class acts as a generator yielding 200 lines of text at a time on demand
	static class LineGen implements Iterator<List<String>>{
		BufferedReader reader = null;
		String line = null;
		
		public LineGen(String path) {
			try {
				reader = new BufferedReader(new FileReader(path));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public boolean hasNext() {
			boolean stillReading =  false;
			try {
				stillReading = reader.ready();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return stillReading;
		}

		@Override
		public List<String> next() {
			List<String> linesList = new LinkedList<>();
			int i = 0;
			try {
				while(i<200 && reader.ready()) {
				line =  reader.readLine();	
				linesList.add(line);
				i++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return linesList;
		}		
	}//end class LineGen

/*
 * Mapper class that obtains lines of text from generator splits into words
 * and indicates that it has seen seen word once.	
 */
static class SplitWordsMap{
		
		private Set stopWords;
		private Iterator<List<String>> lineGen;
		List<List<String>> splits = new LinkedList<>();
		
		public SplitWordsMap(String path) {
			
			lineGen = new LineGen(path);
			//load stop words
			try {
				stopWords = Files.lines(Paths.get("../stop_words.txt"))
						   .map(line -> line.split(","))
						   .flatMap(Arrays::stream)
						   .collect(Collectors.toSet());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end constructor
		
		/*
		 * Generates splits of the form
		 * [(w1, 1), (w2, 1), ..., (wn, 1)]
		 */
		public List<List<String>> generateSplits(){
			while(lineGen.hasNext()) {
				List<String> lines = lineGen.next();
				
				List<List<String>> currentSplit = lines.parallelStream()
													.filter(line -> !line.equals(""))	
													.map(line -> line.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim().split("\\s+"))
													.flatMap(Arrays::stream)
													.filter(word -> (!word.equals("") && !(word.length()<2) && !stopWords.contains(word)))
													.map(word -> Arrays.asList(word,"1"))
													.collect(Collectors.toList());
				splits.addAll(currentSplit);			
			} // end while
			return splits;
		}		
	}//end class SplitWordsMap
		
	public static void main(String[] args) {
		
		//Initiate mapper and generate splits from text
		SplitWordsMap splitMapper = new SplitWordsMap(args[0]);
		List<List<String>> splits = splitMapper.generateSplits();
		
		/*
		 * Maps the splits into groups based on each unique word and
		 * Generates map object having the form 
		 * { w1 : [(w1, 1), (w1, 1)...],
      	 *   w2 : [(w2, 1), (w2, 1)...],...}
		 */
		Map<Object, List<List<String>>> regroup = splits.parallelStream()
											.collect(Collectors.groupingBy(pair -> pair.get(0)));
	    /*
	     *Reduces the previous result into a map of words and their frequencies 
	     *(word, frequency) - frequency is the sum of previous occurrences			
	     */
		Map<Object, Object> wordFrequency = regroup.entrySet()
									    .stream()
									    .collect(Collectors.toMap(
									         Entry::getKey,                       
									         entry -> entry.getValue()
									                       .stream()
									                       .map(pair -> Integer.parseInt(pair.get(1)))
									                       .reduce(Integer::sum)));
	    
		//Generate new map with appropriate data types for later manipulation
		Map<String, Integer> resultSet = new HashMap<>();
		wordFrequency.forEach((k, v) -> resultSet.put((String)k, 
				Integer.valueOf(((Optional<Integer>) v).get())));
	
		//sort by frequency and print top25
		resultSet.entrySet().stream()
							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.limit(25)
							.forEach(entry -> System.out.println(entry.getKey()+" - "+entry.getValue()));
	
	}
}
