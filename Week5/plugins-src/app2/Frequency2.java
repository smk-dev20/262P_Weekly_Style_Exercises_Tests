
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Frequency2 implements ITop25Frequency {

	public HashMap<String,Integer> top25(List<String> wordsList){
		Map<String, Integer> wordFrequency = new HashMap<>();
		
		for(String word : wordsList) {
			if (!wordFrequency.containsKey(word)) {
				wordFrequency.put(word, 1);
			} else {
				wordFrequency.replace(word, wordFrequency.get(word) + 1);
		}
		}
		
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(wordFrequency.entrySet());

		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
				return entry2.getValue().compareTo(entry1.getValue());
			}
		});
		
		int num = 25;
		HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list) {
			if(num>0) {
			sortedMap.put(entry.getKey(), entry.getValue());
			num--;
			}else {
				break;
			}
		}
		
		return sortedMap;
		
	}
}
