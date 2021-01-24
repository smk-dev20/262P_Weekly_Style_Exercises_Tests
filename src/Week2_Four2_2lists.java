import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

/*
 * MSWE 262P
 * Style 4 - Monolith
 * Problem is solved in one continuous sequence without breaking 
 * down into smaller sub-problems
 * Use of libraries is avoided as much as possible
 * 
 * References :
 * https://github.com/crista/exercises-in-programming-style/blob/master/04-monolith/tf-04.py
 */
public class Week2_Four2_2lists {
	public static void main(String[] args) {

		List<String> textWords = new ArrayList<>();
		List<Integer> count = new ArrayList<>();
		Set<String> stopWords = new HashSet<>();
		
		//read in stop words
		String stops = null;
		try {
			File stopFile = new File("stop_words.txt");
			Scanner scanStop = new Scanner(stopFile);
			while (scanStop.hasNextLine()) {
				stops = scanStop.nextLine();
			}
			scanStop.close();

			for (String word : stops.split(",")) {
				stopWords.add(word);
			}

			BufferedReader reader = new BufferedReader(new FileReader(args[0]));
			String line = reader.readLine();
			while (line != null) {
				if (line != "") {
					String word = "";
					//iterate by character forming words from only alphanumeric characters
					for (int i = 0; i < line.length(); i++) {
						char c = line.charAt(i);
						if (Character.isLetter(c)) {
							if (Character.isUpperCase(c)) {
								// change to lower
								int n = c + 32;
								c = (char) n;
							}
							word += c;
						}
						if (!Character.isLetter(c) || (i >= line.length() - 1 && word.length() > 1)) {
							//store non-stop words keeping track of frequency
							if (!stopWords.contains(word) && !word.equals("") && word.length() > 1) {
								boolean notExists = true;
								for (int p = 0; p < textWords.size(); p++) {
									if (textWords.get(p).equals(word)) {
										count.set(p, count.get(p) + 1);
										notExists = false;
										break;
									}
								}

								if (notExists) {
									textWords.add(word);
									count.add(1);
								}

								if (count.size() > 1) {
									//sort after each word is added
									int counter = 0;
									boolean isSorted = false;
									while (!isSorted) {
										isSorted = true;
										for (int k = 0; k < count.size() - 1 - counter; k++) {
											if (count.get(k) < (count.get(k + 1))) {
												int tempNum = count.get(k+1);
												count.set(k+1, count.get(k));
												count.set(k, tempNum);
												
												String tempStr = textWords.get(k+1);
												textWords.set(k+1, textWords.get(k));
												textWords.set(k, tempStr);

											}
										}
										counter++;
									}

								} // end sort

							} // not stop word
							
							// reset for next word in line
							word = "";
						}
					}
				}
				// read next line
				line = reader.readLine();
			}
			reader.close();
			
			//print top 25 words
			for (int i = 0; i < 25; i++) {
				System.out.println(textWords.get(i) + " - " + count.get(i));
			}

		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
