

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Words1 implements IWords {

	public List<String> extractWords(String path) {
		Set<String> stopWords = new HashSet<>();
		List<String> wordsList = new LinkedList<>();
		String line = null;
		String stops = null;
		try {
			File stopFile = new File("../stop_words.txt");
			Scanner scanStop = new Scanner(stopFile);
			while (scanStop.hasNextLine()) {
				stops = scanStop.nextLine();
			}
			scanStop.close();

			for (String word : stops.split(",")) {
				stopWords.add(word);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		try {
			File textFile = new File("../"+path);
			Scanner scanText = new Scanner(textFile);

			while (scanText.hasNextLine()) {
				line = scanText.nextLine();
				if (line.equals(""))
					continue;
				line = line.replaceAll("[^a-zA-Z0-9]", " ");
				String[] wordsInLine = line.split("\\s+");

				for (String textWord : wordsInLine) {
					textWord = textWord.trim().toLowerCase();
					if (!textWord.equals("") && !stopWords.contains(textWord) && textWord.length() > 1) {
						wordsList.add(textWord);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordsList;
	}// end method
}//end class
