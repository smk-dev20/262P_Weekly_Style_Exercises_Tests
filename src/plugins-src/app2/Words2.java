

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

//read in stop words and text files, stop words removed after whole of text has been read
public class Words2 implements IWords {
	public List<String> extractWords(String path) {

		List<String> wordsList = new LinkedList<>();
		Set<String> stopWords;

		File textFile = new File(path.trim());
		Scanner scanText;

		try {
			stopWords = new HashSet<String>(
					Arrays.asList(Files.readAllLines(Paths.get("../stop_words.txt")).get(0).split(",")));

			String line = null;
			List<String> linesInText = new LinkedList<>();
			scanText = new Scanner(textFile);

			while (scanText.hasNextLine()) {
				line = scanText.nextLine();

				if (line.equals(""))
					continue;
				linesInText.add(line);
			}
			scanText.close();

			for (int i = 0; i < linesInText.size(); i++) {
				linesInText.set(i, linesInText.get(i).replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim());
			}
			for (String l : linesInText) {
				wordsList.addAll(new ArrayList<String>(Arrays.asList(l.split("\\s+"))));
			}
			wordsList.removeIf(word -> (word.length() < 2 || word.equals("") || stopWords.contains(word)));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return wordsList;
	}// end method
}//end class
