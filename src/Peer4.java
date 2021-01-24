import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Peer4 {
    public static void main(String[] args) throws FileNotFoundException {
        String fileName = "";
        if (args.length > 0) {
            fileName = args[0];
        }
        String path = System.getProperty("user.dir");
        File prideFile = new File(path + "/"+fileName);
        File stopFile = new File("stop_words.txt");

        printTop25(sortMap(countFrequency(addStopWords(stopFile), prideFile)));
    }

    public static HashMap<String, Integer> addStopWords(File stopFile) throws FileNotFoundException {
        HashMap<String, Integer> stopMap = new HashMap<>();

        Scanner stopScanner = new Scanner(stopFile).useDelimiter(",");
        Pattern pattern = Pattern.compile("[a-zA-Z]+");

        // Adding stop words
        while(stopScanner.hasNext()) {
            Matcher matcher = pattern.matcher(stopScanner.next());
            if (matcher.find()) {
                String word = matcher.group().toLowerCase();
                stopMap.put(word, stopMap.getOrDefault(word, 0) + 1);
            }
        }
        stopScanner.close();
        return stopMap;
    }

    public static HashMap<String, Integer> countFrequency(HashMap<String, Integer> stopMap, File targetFile) throws FileNotFoundException {
        HashMap<String, Integer> map = new HashMap<>();
        Pattern pattern = Pattern.compile("[a-zA-Z]+");

        //Counting word frequency
        Scanner scanner = new Scanner(targetFile);

        while (scanner.hasNext()) {
            Matcher matcher = pattern.matcher(scanner.next());
            if (matcher.find()) {
                String word = matcher.group().toLowerCase();
                System.out.println(word);
                if (!stopMap.containsKey(word)) {
                    map.put(word, map.getOrDefault(word, 0) + 1);
                }
            }
        }
        scanner.close();
        return map;
    }

    public static LinkedList<Map.Entry<String, Integer>> sortMap(HashMap<String, Integer> map) {
        // Sorting word frequency
        LinkedList<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());

        list.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        // Descending order
        Collections.reverse(list);
        return list;
    }

    public static void printTop25(LinkedList<Map.Entry<String, Integer>> list) {
        //Printing top 25 words
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%s - %s", list.get(i).getKey(), list.get(i).getValue());
            System.out.println();
        }
    }
}
