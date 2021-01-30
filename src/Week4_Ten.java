import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * MSWE 262P
 * Style 10 - The One
 * Existence of an abstraction to which values can be converted.
 * This abstraction provides operations to (1) wrap around values, so that
 * they become the abstraction; (2) bind itself to functions, to establish
 * sequences of functions; and (3) unwrap the value, to examine the final
 * result.
 * Larger problem is solved as a pipeline of functions bound together, with
 * unwrapping happening at the end.
 * Particularly for The One style, the bind operation simply calls the given
 * function, giving it the value that it holds, and holds on to the returned value.
 * References
 * https://github.com/crista/exercises-in-programming-style/tree/master/10-the-one
 * Code written for Week1, Week2 & Week 3
 */

//interface defined to use functions as objects
interface Interface{
	Object call(Object value);
}

//abstraction to which values will be converted
class TFTheOne{
	
	private Object value;
	
	TFTheOne(Object val){
		this.value = val;
	}
	
	public TFTheOne bind(Interface function) {
		value = function.call(value);
		return this;
	}

	public void print() {
		System.out.println(value.toString());
	}
}

//read in lines from text
class ReadTextFile implements Interface{
	 List<String> linesInText = new LinkedList<>();
	  
	@Override
	public Object call(Object value) {
		String pathToFile = (String) value;

			File textFile = new File("../"+pathToFile.trim());
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
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return(linesInText);
	}	
}

//eliminate non-alphanumeric characters from lines and convert to lowercase
class CharacterFilter implements Interface{
	
	@Override
	public Object call(Object value) {
		List<String> linesInText = (List<String>)value;
		
		for (int i = 0; i < linesInText.size(); i++) {
			linesInText.set(i, linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
		}
		
		return(linesInText);
	}	
}

//separate lines into words and remove empty or words of size<2 
class WordGenerator implements Interface{
	 List<String> wordsList = new LinkedList<>();
	@Override
	public Object call(Object value) {
		List<String> linesInText = (List<String>)value;

		for (String line : linesInText) {
			wordsList.addAll(new ArrayList<String>(Arrays.asList(line.split("\\s+"))));
		}
		wordsList.removeIf(word -> (word.length() < 2 || word.equals("")));
		
		return(wordsList);
	}	 
}

//read in stopwords file and remove stop words from main words list
class StopWordRemove implements Interface{

	@Override
	public Object call(Object value) {
		List<String> wordsList = (List<String>)value;
		
		List<String> data = new LinkedList<>();
		try {
			data = Files.readAllLines(Paths.get("../stop_words.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<String>stopWords = new HashSet<String>(Arrays.asList(data.get(0).split(",")));
		wordsList.removeIf(word -> (stopWords.contains(word)));
		
		return(wordsList);
	}	 
}

//determine frequencies of words
class CountFrequency implements Interface{
	 Map<String,Integer> map = new LinkedHashMap<>();
	@Override
	public Object call(Object value) {
		List<String> wordsList = (List<String>)value;
		
		for(String word : wordsList) {
			map.put(word,map.getOrDefault(word, 0)+1);
		}		
		return(map);		
	}	 
}

//sort in descending order of frequencies
class SortFrequency implements Interface{
	@Override
	public Object call(Object value) {
		LinkedHashMap<String, Integer> sortedMap = ((LinkedHashMap<String, Integer>) value).entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an exception
						() -> new LinkedHashMap<String, Integer>()));
		
		return(sortedMap);
	}	 
}

//Obtain top 25 words append to StringBuilder
class Top25 implements Interface{

	@Override
	public Object call(Object value) {
		int num = 25;
		StringBuilder sb = new StringBuilder(); 
		for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) value).entrySet()) {
			if (num > 0) {
				sb.append(entry.getKey() + " - " + entry.getValue()+"\n");
				num--;
			} else
				break;
		}	
		return sb;
	}
}



public class Week4_Ten {
public static void main(String[] args) {
	//Instantiate the monad
	TFTheOne one = new TFTheOne(args[0]);
	
	//bind the functions using monad in the needed order
	one.bind(new ReadTextFile()).bind(new CharacterFilter()).bind(new WordGenerator()).bind(new StopWordRemove()).
	bind(new CountFrequency()).bind(new SortFrequency()).bind(new Top25()).print();
}
}
