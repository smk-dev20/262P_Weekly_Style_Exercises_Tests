import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/*
 * MSWE 262P
 * Style 28 - Lazy Rivers
 * Data comes to functions in streams, rather than as a complete whole all at at once
 * Functions are filters / transformers from one kind of data stream to another
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/28-lazy-rivers/tf-28.py
 * https://stackoverflow.com/questions/14109926/iterators-to-read-and-process-file-in-java
 * https://stackoverflow.com/questions/3925130/java-how-to-get-iteratorcharacter-from-string
 * Code written for Weeks 1-5
 */

public class Week6_TwentyEight {
	
	//every call to next() reads in a line from the file and returns the line
	static class LineGen implements Iterator<String>{
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
		public String next() {
			
			try {
				line =  reader.readLine();					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return line;
		}
		
	}
	
	// every call to next() pulls one line from previous iterator and returns list of words
	// formed from the line after processing for lowercase/length etc
	static class WordGen implements Iterator<List<String>>{
		private Iterator<String> lineGen;
		
		public WordGen(String path) {
			 lineGen = new LineGen(path);
		}
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return lineGen.hasNext();
		}

		@Override
		public List<String> next() {
			List<String> words = new LinkedList<>();
			
			while(lineGen.hasNext()) {
			String line = lineGen.next();
			if(line!="") {
			words.addAll(new ArrayList<String>(Arrays.asList(line.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim().split("\\s+"))));	
			words.removeIf(word -> (word.length() < 2 || word.equals("")));
			}
			 return words;
			}//end hasNext
			
			return words;
		}//end next	
	}
	
	//call to next() pulls from previous iterator removes stop words and returns updated words list
	static class NonStopWordGen implements Iterator<List<String>>{
		private Iterator<List<String>> wordGen;
		Set<String> stopWords;
		
		public NonStopWordGen(String path) {
			wordGen = new WordGen(path);
			try {
				stopWords = new HashSet<String>(
						Arrays.asList(Files.readAllLines(Paths.get("../stop_words.txt")).get(0).split(",")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return wordGen.hasNext();
		}

		@Override
		public List<String> next() {
			
			List<String> words = new LinkedList<>();
			while(wordGen.hasNext()) {
				List<String> allWordsInLine = wordGen.next();
				for(String word : allWordsInLine) {
					if(!stopWords.contains(word)) {
						words.add(word);
					}
				}	
				return words;
			}//end hasNext
			
			//return final words list on termination of previous iterator
			return words;
		}
		
	}
	
	//call to next() pulls from previous iterator until 5000+ words are read and added to map
	//sorted map of those 5000+ words is returned
	static class CountAndSortMapGen implements Iterator<LinkedHashMap<String,Integer>>{

		private Iterator<List<String>> nonStopGen;
		private Map<String, Integer> map = new LinkedHashMap<>();
		public CountAndSortMapGen(String path) {
			nonStopGen = new NonStopWordGen(path);
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return nonStopGen.hasNext();
		}

		@Override
		public LinkedHashMap<String, Integer> next() {
			int i = 1;

			while (nonStopGen.hasNext()) {
				List<String> words = nonStopGen.next();
				for (String word : words) {
					map.put(word, map.getOrDefault(word, (int) (0)) + 1);
					i++;
				} // end for

				// return sorted map when 5000 words or if current list length resulted in 5000+ words
				if (i % 5000 == 0 || i > 5000) {
					LinkedHashMap<String, Integer> sortedMap = ((LinkedHashMap<String, Integer>) map).entrySet()
							.stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw
																											// an
																											// exception
									() -> new LinkedHashMap<String, Integer>()));
					return sortedMap;
				}
			} // end hasNext

			// return last set of words length <5000
			LinkedHashMap<String, Integer> sortedMap = ((LinkedHashMap<String, Integer>) map).entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an
																									// exception
							() -> new LinkedHashMap<String, Integer>()));
			return sortedMap;
		}
		
	}
	
	
	
public static void main(String[] args) {
	CountAndSortMapGen gen = new CountAndSortMapGen(args[0]);
	while(gen.hasNext()) {
		Map<String, Integer> map = gen.next();
		int num = 25;
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (num > 0) {
				System.out.println(entry.getKey() + " - " + entry.getValue());
				num--;
			} else
				break;
		}
		System.out.println("---------------");
	}
}
}
