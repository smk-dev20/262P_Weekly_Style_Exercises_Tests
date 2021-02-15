import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/*
 * MSWE 262P
 * Style 26 - Persistent Tables
 * The data exists beyond the execution of programs that use it, and is meant to be used by many dierent programs.
 * The data is stored in a way that makes it easier/faster to explore.
 * The input data of the problem is modeled as one or more series of domains, or types, of data.
 * The concrete data is modeled as having components of several domains, establishing relationships between the application's data
 * and the domains identified.
 * The problem is solved by issuing queries over the data.
 * 
 * References
 * https://github.com/crista/exercises-in-programming-style/blob/master/26-persistent-tables/tf-26.py
 * https://www.tutorialspoint.com/sqlite/sqlite_java.htm
 * Code written for Weeks 1-5
 */
public class Week6_TwentySix {
	
	//creates three tables for documents, words and characters
	public static void createDBSchema(Connection con) {
		Statement stmt = null;
	     try {
	    	 //drop tables if they already exist
	    	 stmt = con.createStatement();
	    	 String sql = "DROP TABLE IF EXISTS DOCUMENTS";
	         stmt.executeUpdate(sql);
	         con.commit();
	         
	         sql = "DROP TABLE IF EXISTS WORDS";
	         stmt.executeUpdate(sql);
	         con.commit();
	         
	         sql = "DROP TABLE IF EXISTS CHARACTERS";
	         stmt.executeUpdate(sql);
	         con.commit();
	         
	    	 	    	 
	    	 //create new tables with necessary schema
	         sql = "CREATE TABLE DOCUMENTS " +
	                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
	                        " NAME TEXT NOT NULL)"; 
	         stmt.executeUpdate(sql);
	         con.commit();
	         
	         sql = "CREATE TABLE WORDS " +
	                        "(ID INT," + "DOC_ID INT," +
	                        " VALUE TEXT NOT NULL)"; 
	         stmt.executeUpdate(sql);
	         con.commit();

	         sql = "CREATE TABLE CHARACTERS " +
	                        "(ID INT," + "WORD_ID INT," +
	                        " VALUE TEXT NOT NULL)"; 
	         stmt.executeUpdate(sql);
	         
	         stmt.close();	         
	         con.commit();                  
	      } catch ( Exception e ) {
	         e.printStackTrace();
	         System.exit(0);
	      }
	     System.out.println("Tables created successfully");
	}
	
	 //takes path to file returns list of non-stop words
	public static List<String> extractWords(String path){
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
	}
	
	//Take path to file and load contents into DB
	public static void loadFileIntoDB(String path, Connection con) {
		
		List<String> words = extractWords(path);
		Statement stmt = null;
		Integer docId = null;
		Integer wordId = null;
		Integer charId = null;
	      try {
	          stmt = con.createStatement();
	          String sql = "INSERT INTO DOCUMENTS (NAME) " +
	                         "VALUES ("+"\""+ path+"\"" +");"; 
	          stmt.executeUpdate(sql);
	          con.commit();
	          
	          sql = "SELECT ID FROM DOCUMENTS WHERE NAME ="+"\""+ path+"\""; 
	          ResultSet rs = stmt.executeQuery(sql);
	          while ( rs.next() ) {
	             docId = rs.getInt("ID");
	           }
	           rs.close();
	           
	           sql = "SELECT MAX(ID) FROM WORDS;";
		          ResultSet rs1 = stmt.executeQuery(sql);
		          while ( rs1.next() ) {
		             wordId = rs1.getInt(1);
		          }
		             rs1.close();
		             
		             for(String word : words) {
		            	 sql = "INSERT INTO WORDS VALUES(" +
		                         wordId +"," +docId+","+"\""+ word +"\""+");"; 
		   	          	stmt.executeUpdate(sql);
		            	 
		   	          	charId = 0;
		            	 
		            	 for(char c : word.toCharArray()) {
		            		 sql = "INSERT INTO CHARACTERS VALUES(" +
			                         charId +"," +wordId+","+"\""+ c +"\""+");";
		       	          stmt.executeUpdate(sql);
		    	          charId++;
		            	 }
		            	 wordId++;
		             }
		           
	          stmt.close();
	          con.commit();
	          con.close();
	       } catch ( Exception e ) {
	          e.printStackTrace();
	          System.exit(0);
	       }
	      System.out.println("Values loaded into tables");
	}

	public static void main(String[] args) {
		
		 Connection con = null;
		 //create DB, tables and load data into tables		
			try {
		         Class.forName("org.sqlite.JDBC");
		         con = DriverManager.getConnection("jdbc:sqlite:tf.db");
		         con.setAutoCommit(false);
		      } catch ( Exception e ) {
		         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		         System.exit(0);
		      }
			System.out.println("DB Connection established");
			createDBSchema(con);
			loadFileIntoDB(args[0],con);
			
			//query tables for needed info
			System.out.println("------TERM FREQUENCIES-----");
		try {
	         Class.forName("org.sqlite.JDBC");
	         con = DriverManager.getConnection("jdbc:sqlite:tf.db");
	         Statement stmt = con.createStatement();
	         
	          String sql = "SELECT VALUE, COUNT(*) AS C FROM WORDS GROUP BY VALUE ORDER BY C DESC"; 
	          ResultSet rs = stmt.executeQuery(sql);
	          int num = 0;
	          while ( rs.next() && num < 25 ) {
	             System.out.println(rs.getString("VALUE")+" - "+rs.getInt("C"));
	             num++;
	           }
	           rs.close();
	           
	           System.out.println("-------------");
	           sql = "SELECT COUNT(DISTINCT VALUE) FROM WORDS WHERE ID IN "
	           		+ "(SELECT DISTINCT WORD_ID FROM CHARACTERS WHERE VALUE ="+"\"" +"z"+"\")";
		          ResultSet rs1 = stmt.executeQuery(sql);
		          while ( rs1.next() ) {
		             System.out.println("Count of unique words with z "+rs1.getInt(1));
		          }
		       rs1.close();
		       stmt.close();
		       con.close();

	      } catch ( Exception e ) {
	         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	         System.exit(0);
	      }		
	}
}
