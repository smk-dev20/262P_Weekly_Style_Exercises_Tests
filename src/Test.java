
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Test {
public static void main(String[] args) {
	
List<String> words = Arrays.asList("abc", "def","xyz","pqr");
Function<String, Boolean> func = word -> words.contains(word);
System.out.println(func.apply("abc"));

System.out.println("\nlambda chaining:");
List<String> l = Arrays.asList("1", "22", "333", "4444", "55555", "666666", "7777777", "88888888", "999999999");
Function<String, ?> f = s -> s.length();
Consumer<String> c = s -> System.out.println(f.apply(s));;
l.forEach(c);
	
Runnable runnable2 = () -> 
{
	   System.out.println("Runnable using lambda " + 
Thread.currentThread().getName());
};
Thread thread2 = new Thread(runnable2);
thread2.start();
	
/*	class WordCount{
		private String word;
		private int count;
		
		public WordCount(String word, int num){
			this.word = word;
			this.count = num;
		}
	}
*/	
//	List<Object> wordCount = new ArrayList<>();
//	List<Object> wordCount2 = new ArrayList<>();
//
//	List<List<Object>> frequency = new ArrayList<>();
//
//	 wordCount.add("abc");
//	 wordCount.add(1);
//	frequency.add(wordCount);
//	
//	 wordCount2.add("def");
//	 wordCount2.add(2);
//	frequency.add(wordCount2);
//	
//
//	 System.out.println("Initial");
//	 for(List<Object> seenWord : frequency) {
//		System.out.println(seenWord.get(0).toString() +" - "+seenWord.get(1).toString()); 	
//	 }
//	 
//	 
//	 
//	 String word = "def";
//	 boolean notExists = true;
//	 for(int i=0;i<frequency.size();i++) {
//		 List<Object> seenWord = frequency.get(i);
//		 if(seenWord.get(0).equals(word)) {
//			 int count =  (int)seenWord.get(1);
//			 seenWord.set(1, count+1);
//			 frequency.set(i,seenWord);
//			 notExists = false;
//			 break;
//		 }			 
//	 }
//	 
//	 if(notExists) {
//		 List<Object> newWord3 = new ArrayList<>();
//		 newWord3.add(word);
//		 newWord3.add(1);
//
//		 frequency.add(newWord3);
//	 }
//	 System.out.println("After add");
//	 for(List<Object>  seenWord : frequency) {
//		System.out.println(seenWord.get(0).toString() +" - "+seenWord.get(1).toString()); 	
//	 } 
//	
//	 for(int i=0;i<frequency.size()-1;i++) {
//		 for(int j=1;j<frequency.size();j++) {
//			 if((int)frequency.get(i).get(1)<(int)frequency.get(j).get(1)) {
//				 List<Object> temp = frequency.get(j);
//				 frequency.set(j, frequency.get(i));
//				 frequency.set(i, temp);
//				 
//			 }
//		 }
//	 }
//	 
//	 System.out.println("Sorted");
//	 for(List<Object> seenWord : frequency) {
//		System.out.println(seenWord.get(0).toString() +" - "+seenWord.get(1).toString()); 	
//	 } 
//	 
	 
	 
	 
	 
	 
/* for(int k=0;k<frequency.size()-1;k++) {
	 int j = k+1;
	 while(j<frequency.size()) {
			if(((int)frequency.get(k).get(1))<((int)frequency.get(j).get(1))) {
				List<Object> temp = frequency.get(k);
				frequency.set(k, frequency.get(j));
				frequency.set(j, temp);
			}
			j++;
	 }
 }*/
}
}
