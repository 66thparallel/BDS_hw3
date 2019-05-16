package hw3;

/**
 * @author Jane Liu
 * Homework 3
 *
 * Classes:
 * 	BestExtractor: Runs my best keyword extraction method on the positive [“4”] and the negative [“0”] set 
 * 	of tweets independently.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class BestExtractor {
	
	private static String filename = "data/sentiment140.csv";
	
	public static void start() {
		
		List<String> tweets0 = new ArrayList<String>();
		List<String> tweets4 = new ArrayList<String>();
		List<String> stopwords = new ArrayList<String>();
		
		// delete output file results.txt if it already exists
		File f = new File("bestextraction.txt");
		if(f.exists()){f.delete();}

		// Read sentiment140.csv and extract the "text" column of positive "4" and negative "0" tweets.
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();

			while (line != null) {
				line = line.replaceAll("\"", "");
				String[] temp = line.split(",");
				String num = temp[0].replaceAll(" ", "");

				if (num.equals("0")) { tweets0.add(temp[5]); }
				else if (num.equals("4")) { tweets4.add(temp[5]); }
				
				line = reader.readLine();
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			reader = new BufferedReader(new FileReader("data/twitter_stopwords.txt"));
			String line2 = reader.readLine();

			while (line2 != null) {
				String[] temp2 = line2.split(",");
				for (String t : temp2) {
					stopwords.add(t);
				}
				line2 = reader.readLine();
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
				
		String[] texts0 = tweets0.toArray(new String[tweets0.size()]);
		String[] texts4 = tweets4.toArray(new String[tweets4.size()]);

		List<String> tokens0 = new ArrayList<String>();
		List<String> tokens4 = new ArrayList<String>();
		String[] tempstr0;
		String[] tempstr4;
		
		// clean negative tweets
		for (int i = 0; i < texts0.length; i++) {
			texts0[i] = texts0[i].replaceAll("\"", "");
			texts0[i] = texts0[i].replaceAll("#[A-Za-z0-9-_]+", "");
			texts0[i] = texts0[i].replaceAll("@[A-Za-z0-9-_]+", "");
			texts0[i] = texts0[i].replaceAll("[\\r\\n]+", " ");
			texts0[i] = texts0[i].replaceAll("&[A-Za-z0-9]+;", "");
			texts0[i] = texts0[i].replaceAll(
					"(http|https|ftp)?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)","");
			texts0[i] = texts0[i].replaceAll("[!.?,’;:\\-\\[\\]\\\\\\/|√©ô´ÔøΩÔøΩÔøΩ()~*\"'{}^+<>=_]+", "");
			texts0[i] = texts0[i].replaceAll("^( )+", "");
			texts0[i] = texts0[i].replaceAll("(  )+", " ");
			texts0[i] = texts0[i].replaceAll("\n", ", ");
			texts0[i] = texts0[i].replaceAll("[!@#$%^&+=.,:;’'\"�()?]+", "");
			texts0[i] = texts0[i].replaceAll("[0-9]+", "");
			texts0[i] = texts0[i].replaceAll("^-", "");
			texts0[i] = texts0[i].replaceAll("-$", "");
			texts0[i] = texts0[i].replaceAll("\\]", "");
			texts0[i] = texts0[i].replaceAll("\\[", "");
			
			// Remove stopwords
			tempstr0 = texts0[i].split("\\s+");
			for (String s: tempstr0) { if(s!=null || s!="") { tokens0.add(s); }}
			for(String word: stopwords){
				if(tokens0.contains(word)) { tokens0.removeAll(Collections.singleton(word)); }
			}
		}
		
		// clean positive tweets
		for (int i = 0; i < texts4.length; i++) {
			texts4[i] = texts4[i].replaceAll("\"", "");
			texts4[i] = texts4[i].replaceAll("#[A-Za-z0-9-_]+", "");
			texts4[i] = texts4[i].replaceAll("@[A-Za-z0-9-_]+", "");
			texts4[i] = texts4[i].replaceAll("[\\r\\n]+", " ");
			texts4[i] = texts4[i].replaceAll("&[A-Za-z0-9]+;", "");
			texts4[i] = texts4[i].replaceAll(
					"(http|https|ftp)?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)","");
			texts4[i] = texts4[i].replaceAll("[!.?,’;:\\-\\[\\]\\\\\\/|√©ô´ÔøΩÔøΩÔøΩ()~*\"'{}^+<>=_]+", "");
			texts4[i] = texts4[i].replaceAll("^( )+", "");
			texts4[i] = texts4[i].replaceAll("(  )+", " ");
			texts4[i] = texts4[i].replaceAll("\n", ", ");
			texts4[i] = texts4[i].replaceAll("[!@#$%^&+=.,:;’'\"�()?]+", "");
			texts4[i] = texts4[i].replaceAll("[0-9]+", "");
			texts4[i] = texts4[i].replaceAll("^-", "");
			texts4[i] = texts4[i].replaceAll("-$", "");
			texts4[i] = texts4[i].replaceAll("\\]", "");
			texts4[i] = texts4[i].replaceAll("\\[", "");
			
			// Remove stopwords
			tempstr4 = texts4[i].split("\\s+");
			for (String s: tempstr4) { if(s!=null || s!="") { tokens4.add(s); }}
			for(String word: stopwords){
				if(tokens4.contains(word)) { tokens4.removeAll(Collections.singleton(word)); }
			}
		}
		
		// Get adjective-nouns for negative tweets
		List<String> pos_tokens0 = new ArrayList<String>();
        String text0 = "";
        for (String token : tokens0) {
        	text0 += token + " ";
        }

        StanfordNLP snlp0 = new StanfordNLP();
        snlp0.process(text0);
        
        pos_tokens0 = snlp0.getAdjNouns();

		Map<String, Integer> pos_adjnouns0 = new HashMap<String, Integer>();
		
		for (String grams: pos_tokens0) {
			if (pos_adjnouns0.containsKey(grams)) { pos_adjnouns0.put(grams, pos_adjnouns0.get(grams) + 1); } 
			else { pos_adjnouns0.put(grams, 1); }
		}
		
		// Get adjective-nouns for positive tweets
		List<String> pos_tokens4 = new ArrayList<String>();
        String text4 = "";
        for (String token : tokens4) {
        	text4 += token + " ";
        }

        StanfordNLP snlp4 = new StanfordNLP();
        snlp4.process(text4);
        
        pos_tokens4 = snlp4.getAdjNouns();

		Map<String, Integer> pos_adjnouns4 = new HashMap<String, Integer>();
		
		for (String grams: pos_tokens4) {
			if (pos_adjnouns4.containsKey(grams)) { pos_adjnouns4.put(grams, pos_adjnouns4.get(grams) + 1); } 
			else { pos_adjnouns4.put(grams, 1); }
		}
		
		// Print most frequent adjective-nouns for positive and negative tweets
		int n = 30;
		
		List<Entry<String, Integer>> max_adjnoun0 = getMax(pos_adjnouns0, n);
		List<String> pos_maxone_list0 = max_adjnoun0.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxone_list0, "30 adjective-nouns for negative tweets");
		
		List<Entry<String, Integer>> max_adjnoun4 = getMax(pos_adjnouns4, n);
		List<String> pos_maxone_list4 = max_adjnoun4.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxone_list4, "30 adjective-nouns for positive tweets");
		
	}
	
	// Use a PriorityQueue to get top hashtags, @-mentions, and ngrams
	private static <K, V extends Comparable<? super V>> List<Entry<K, V>> getMax(Map<K, V> map, int n) {
		Comparator<? super Entry<K, V>> comparator = new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e0, Entry<K, V> e1) {
				V v0 = e0.getValue();
				V v1 = e1.getValue();
				return v0.compareTo(v1);
			}
		};
		PriorityQueue<Entry<K, V>> highest = new PriorityQueue<Entry<K, V>>(n, comparator);
		for (Entry<K, V> entry : map.entrySet()) {
			highest.offer(entry);
			while (highest.size() > n) {
				highest.poll();
			}
		}
		List<Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
		while (highest.size() > 0) {
			result.add(highest.poll());
		}
		return result;
	}
	
	private static void print(List<String> alist, String type) {
		
		// output to file
	    try {
            FileWriter writer = new FileWriter("bestextraction.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            bufferedWriter.write("Top " + type + ": \n");
            for (String item : alist) {
            	bufferedWriter.write(item + ", ");
            }
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
