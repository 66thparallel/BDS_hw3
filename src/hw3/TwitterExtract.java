package hw3;

/**
 * @author Jane Liu
 * Homework 3: Twitter Keyword Extraction
 * 
 * Classes:
 * 	Main: 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;


public class TwitterExtract {

	public static void main(String[] args) {

		List<String> tweets = new ArrayList<String>();
		List<String> stopwords = new ArrayList<String>();
		Map<String, Integer> hashtags = new HashMap<String, Integer>();
		Map<String, Integer> mentions = new HashMap<String, Integer>();
		String filename = "data/sentiment140.csv";

		// Read sentiment140.csv and extract the "text" column of raw tweets.
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();

			while (line != null) {
				String[] temp = line.split("\",");
				tweets.add(temp[5]);
				line = reader.readLine();
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		// Read a list of Twitter stopwords
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

		String[] texts = new String[tweets.size()];
		tweets.toArray(texts);

		// Create hashmap of hashtags and their frequency
		String regexhash = "(?:\\s|\\A)[#]+([A-Za-z0-9-_]+)";

		for (String t : texts) {
			Pattern p = Pattern.compile(regexhash);
			Matcher m = p.matcher(t);
			String key = null;

			while (m.find()) {
				key = m.group();

				if (hashtags.containsKey(key)) {
					hashtags.put(key, hashtags.get(key) + 1);
				} else {
					hashtags.put(key, 1);
				}
			}
		}

		// Create hashmap of @-mentions and their frequency
		String regexment = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";

		for (String t : texts) {
			Pattern p = Pattern.compile(regexment);
			Matcher m = p.matcher(t);
			String key = null;

			while (m.find()) {
				key = m.group();

				if (mentions.containsKey(key)) {
					mentions.put(key, mentions.get(key) + 1);
				} else {
					mentions.put(key, 1);
				}
			}
		}

		// Get the 20 most popular hashtags and @-mentions using a PriorityQueue
		int n = 20;
		List<Entry<String, Integer>> maxhash = getMax(hashtags, n);
		List<Entry<String, Integer>> maxment = getMax(mentions, n);
		System.out.println("Top " + n + " hashtags:");
		for (Entry<String, Integer> entry : maxhash) {
			System.out.println(entry);
		}
		System.out.println("\nTop " + n + " @-mentions:");
		for (Entry<String, Integer> entry : maxment) {
			System.out.println(entry);
		}

		// Preprocessing: remove hashtags, @-mentions, URLs, stopwords, emojis, and unnecessary characters
		List<String> tokens = new ArrayList<String>();
		List<String> pos_tokens = new ArrayList<String>();
		List<String> ngramList = new ArrayList<String>();
		Map<String, Integer> onegrams = new HashMap<String, Integer>();
		Map<String, Integer> twograms = new HashMap<String, Integer>();
		Map<String, Integer> threegrams = new HashMap<String, Integer>();
		Map<String, Integer> fourgrams = new HashMap<String, Integer>();
		Map<String, Integer> pos_onegrams = new HashMap<String, Integer>();
		Map<String, Integer> pos_twograms = new HashMap<String, Integer>();
		Map<String, Integer> pos_threegrams = new HashMap<String, Integer>();
		Map<String, Integer> pos_fourgrams = new HashMap<String, Integer>();
		Map<String, Integer> pos_patterns = new HashMap<String, Integer>();
		Map<String, Integer> dependency_parser = new HashMap<String, Integer>();
		String[] tempstr;
		
		for (int i = 0; i < texts.length; i++) {
			texts[i] = texts[i].replaceAll("\"", "");
			texts[i] = texts[i].replaceAll("#[A-Za-z0-9-_]+", "");
			texts[i] = texts[i].replaceAll("@[A-Za-z0-9-_]+", "");
			texts[i] = texts[i].replaceAll("[\\r\\n]+", " ");
			texts[i] = texts[i].replaceAll("&[A-Za-z0-9]+;", "");
			texts[i] = texts[i].replaceAll(
					"(http|https|ftp)?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)","");
			texts[i] = texts[i].replaceAll("[!.?,’;:\\-\\[\\]\\\\\\/|√©ô´ÔøΩÔøΩÔøΩ()~*\"'{}^+<>=_]+", "");
			texts[i] = texts[i].replaceAll("^( )+", "");
			texts[i] = texts[i].replaceAll("(  )+", " ");
			texts[i] = texts[i].replaceAll("\n", ", ");
			
			// Remove stopwords
			tempstr = texts[i].split(" ");
			for (String s: tempstr) { if(s != null) { tokens.add(s); }}
			for(String word: stopwords){
				if(tokens.contains(word)) { tokens.removeAll(Collections.singleton(word)); }
			}
			
			// Find unigrams
			for (String grams: tokens) {
				if (onegrams.containsKey(grams)) { onegrams.put(grams, onegrams.get(grams) + 1); } 
				else { onegrams.put(grams, 1); }
			}ngramList.clear();
			
			// Find bigrams
			int k = 1;
			n = 2;
			String ngramstr = "";
			for (int j=0; j<tokens.size()-n; j++) {
				ngramstr = tokens.get(j);			
				while (k < n) {
					ngramstr += " " + tokens.get(j+k);
					k++;
				}
				k = 1;
				ngramList.add(ngramstr);
			}
			for (String grams: ngramList) {
				if (twograms.containsKey(grams)) { twograms.put(grams, twograms.get(grams) + 1); } 
				else { twograms.put(grams, 1); }
			}ngramList.clear();
			
			// Find trigrams
			n = 3;
			k = 1;
			for (int l=0; l<tokens.size()-n; l++) {
				ngramstr = tokens.get(l);
				while (k < n) { ngramstr += " " + tokens.get(l+k); k++; }
				k = 1;
				ngramList.add(ngramstr);
			}
			for (String grams: ngramList) {
				if (threegrams.containsKey(grams)) { threegrams.put(grams, threegrams.get(grams) + 1); } 
				else { threegrams.put(grams, 1); }
			}ngramList.clear();

			// Find fourgrams
			n = 4;
			k = 1;
			for (int m=0; m<tokens.size()-n; m++) {
				ngramstr = tokens.get(m);
				while (k < n) { ngramstr += " " + tokens.get(m+k); k++; }
				k = 1;
				ngramList.add(ngramstr);
			}
			for (String grams: ngramList) {
				if (fourgrams.containsKey(grams)) { fourgrams.put(grams, fourgrams.get(grams) + 1); } 
				else { fourgrams.put(grams, 1); }
			}ngramList.clear();
			
			// create a Stanford CoreNLP object with POS tagging, dependency parser, and NER
			Map<String, Integer> named_entities = new HashMap<String, Integer>();
			String basicstr = "";
			
	        Properties props = new Properties();
	        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, ner");
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	        
	        // convert list of tokenized text to a string for Stanford NLP library functions
	        for (String t: tokens) { basicstr += t + " "; pos_tokens.add(t); }
	        
	        // Remove all parts-of-speech except nouns with (POS) tagger
	        Annotation document = new Annotation(basicstr);
	        pipeline.annotate(document);
	        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	        sentences = document.get(SentencesAnnotation.class);
	        
	        for(CoreMap sentence: sentences) {
	        	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        		String word = token.get(TextAnnotation.class);
	        		String pos = token.get(PartOfSpeechAnnotation.class);
	        		String ner = token.get(NamedEntityTagAnnotation.class);
	        		// Get nouns
	        		if(pos.matches("NN|NNP|NNPS|PRP")) {
	        			pos_tokens.add(word);
	        		}
//	        		// Get NERs
//	        		if(ner!=null) {
//	        			if (named_entities.containsKey(word)) { named_entities.put(word, pos_twograms.get(word) + 1); } 
//	    				else { named_entities.put(word, 1); }
//	        		}
	        	}
	        }
	        
			// Find unigram nouns only
			for (String grams: pos_tokens) {
				if (pos_onegrams.containsKey(grams)) { pos_onegrams.put(grams, pos_onegrams.get(grams) + 1); } 
				else { pos_onegrams.put(grams, 1); }
			}ngramList.clear();
			
			// Find bigram nouns only
			k = 1;
			n = 2;
			ngramstr = "";
			for (int j=0; j<pos_tokens.size()-n; j++) {
				ngramstr = pos_tokens.get(j);			
				while (k < n) {
					ngramstr += " " + pos_tokens.get(j+k);
					k++;
				}
				k = 1;
				ngramList.add(ngramstr);
			}
			for (String grams: ngramList) {
				if (pos_twograms.containsKey(grams)) { pos_twograms.put(grams, pos_twograms.get(grams) + 1); } 
				else { pos_twograms.put(grams, 1); }
			}ngramList.clear();
			
			// Find trigram nouns only
			n = 3;
			k = 1;
			for (int l=0; l<pos_tokens.size()-n; l++) {
				ngramstr = pos_tokens.get(l);
				while (k < n) { ngramstr += " " + pos_tokens.get(l+k); k++; }
				k = 1;
				ngramList.add(ngramstr);
			}
			for (String grams: ngramList) {
				if (pos_threegrams.containsKey(grams)) { pos_threegrams.put(grams, pos_threegrams.get(grams) + 1); } 
				else { pos_threegrams.put(grams, 1); }
			}ngramList.clear();

			// Find fourgram nouns only
			n = 4;
			k = 1;
			for (int m=0; m<pos_tokens.size()-n; m++) {
				ngramstr = pos_tokens.get(m);
				while (k < n) { ngramstr += " " + pos_tokens.get(m+k); k++; }
				k = 1;
				ngramList.add(ngramstr);
			}
			for (String grams: ngramList) {
				if (pos_fourgrams.containsKey(grams)) { pos_fourgrams.put(grams, pos_fourgrams.get(grams) + 1); } 
				else { pos_fourgrams.put(grams, 1); }
			}
			ngramList.clear(); pos_tokens.clear(); tokens.clear();
		}

		// Get the 20 most popular unigrams, bigrams, trigrams, fourgrams
		n = 20;
		
		List<Entry<String, Integer>> max_one = getMax(onegrams, n);
		System.out.println("Top " + n + " unigrams: ");
		for (Entry<String, Integer> entry : max_one) { System.out.println(entry); } System.out.println("");
		
		List<Entry<String, Integer>> max_two = getMax(twograms, n);
		System.out.println("Top " + n + " bigrams: ");
		for (Entry<String, Integer> entry : max_two) { System.out.println(entry); } System.out.println("");

		List<Entry<String, Integer>> max_three = getMax(threegrams, n);
		System.out.println("Top " + n + " trigrams: ");
		for (Entry<String, Integer> entry : max_three) { System.out.println(entry); } System.out.println("");

		List<Entry<String, Integer>> max_four = getMax(fourgrams, n);
		System.out.println("Top " + n + " fourgrams: ");
		for (Entry<String, Integer> entry : max_four) { System.out.println(entry); } System.out.println("");
		
		// Get the 20 most popular unigrams, bigrams, trigrams, fourgrams with nouns only using Stanford CoreNLP (POS) tagger
		n = 20;
		
		List<Entry<String, Integer>> pos_max_one = getMax(pos_onegrams, n);
		System.out.println("Top " + n + " unigram (nouns only): ");
		for (Entry<String, Integer> entry : pos_max_one) { System.out.println(entry); } System.out.println("");
		
		List<Entry<String, Integer>> pos_max_two = getMax(pos_twograms, n);
		System.out.println("Top " + n + " bigrams (nouns only): ");
		for (Entry<String, Integer> entry : pos_max_two) { System.out.println(entry); } System.out.println("");

		List<Entry<String, Integer>> pos_max_three = getMax(pos_threegrams, n);
		System.out.println("Top " + n + " trigrams (nouns only): ");
		for (Entry<String, Integer> entry : pos_max_three) { System.out.println(entry); } System.out.println("");

		List<Entry<String, Integer>> pos_max_four = getMax(pos_fourgrams, n);
		System.out.println("Top " + n + " fourgrams (nouns only): ");
		for (Entry<String, Integer> entry : pos_max_four) { System.out.println(entry); } System.out.println("");
		
//		// Get the 10 most popular named entities
//		n = 10;
//		List<Entry<String, Integer>> ner_words = getMax(ner_words, n);
//		System.out.println("The " + n + " most common named entities: ");
//		for (Entry<String, Integer> entry : ner_words) { System.out.println(entry); } System.out.println("");
	
	}

	// Use a PriorityQueue to get top 20 hashtags, @-mentions, and ngrams
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
}