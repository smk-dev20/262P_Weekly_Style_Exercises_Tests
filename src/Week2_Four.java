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
 */
public class Week2_Four {
	public static void main(String[] args) {

		List<List<Object>> frequency = new ArrayList<>();
		Set<String> stopWords = new HashSet<>();

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
							if (!stopWords.contains(word) && !word.equals("") && word.length() > 1) {
								boolean notExists = true;
								for (int p = 0; p < frequency.size(); p++) {
									List<Object> seenWord = frequency.get(p);
									if (seenWord.get(0).equals(word)) {
										int count = (int) seenWord.get(1);
										seenWord.set(1, count + 1);
										frequency.set(p, seenWord);
										notExists = false;
										break;
									}
								}

								if (notExists) {
									List<Object> newWord = new ArrayList<>();
									newWord.add(word);
									newWord.add(1);

									frequency.add(newWord);
								}

								// System.out.println("word "+word + " array size "+frequency.size());
								if (frequency.size() > 1) {

//									int counter = 0;
//									boolean isSorted = false;
//									while (!isSorted) {
//										isSorted = true;
//										for (int k = 0; k < frequency.size() - 1 - counter; k++) {
//											if (((int) frequency.get(k).get(1)) < ((int) frequency.get(k + 1).get(1))) {
//												List<Object> temp = frequency.get(k);
//												frequency.set(k, frequency.get(k + 1));
//												frequency.set(k + 1, temp);
//												isSorted = false;
//											}
//										}
//										counter++;
//									}

								} // sort

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

			int counter = 0;
			boolean isSorted = false;
			while (!isSorted) {
				isSorted = true;
				for (int k = 0; k < frequency.size() - 1 - counter; k++) {
					if (((int) frequency.get(k).get(1)) < ((int) frequency.get(k + 1).get(1))) {
						List<Object> temp = frequency.get(k);
						frequency.set(k, frequency.get(k + 1));
						frequency.set(k + 1, temp);
						isSorted = false;
					}
				}
				counter++;
			}

			for (int i = 0; i < 25; i++) {
				System.out.println(frequency.get(i).get(0).toString() + " - " + frequency.get(i).get(1).toString());
			}

		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
