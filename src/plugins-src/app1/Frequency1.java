
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


//obtain count of words sort using streams and return top 25
public class Frequency1 implements ITop25Frequency{
	
	public HashMap<String,Integer> top25(List<String> wordsList){
		Map<String, Integer> wordFrequency = new HashMap<>();
		HashMap<String, Integer> topResults = new LinkedHashMap<>();
		for(String word : wordsList) {
			wordFrequency.put(word, wordFrequency.getOrDefault(word, 0)+1);
		}
		
		LinkedHashMap<String, Integer> sortedMap = (wordFrequency).entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> null, // or throw an exception
						() -> new LinkedHashMap<String, Integer>()));
		
		
		int num = 25;
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			if (num > 0) {
				topResults.put(entry.getKey(), entry.getValue());
				num--;
			} else
				break;
		}
				
		return topResults;
	}

}
