package hw3;

/**
 * @author Jane Liu
 * Homework 3: Twitter Keyword Extraction
 * 
 * Classes:
 * 	Main: Performs preprocessing, gets the top 20 hashtags and @-mentions. Gets top 20 unigrams, bigrams, 
 * 	trigrams, and fourgrams for all tokens and for just nouns. Calls StanfordNLP class methods and gets 
 * 	results from the POS tagger, dependency parser, and named entity tagger.
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


public class TwitterExtract {
	
	private static String filename = "data/sentiment140_part.csv";

	public static void main(String[] args) {

		List<String> tweets = new ArrayList<String>();
		List<String> stopwords = new ArrayList<String>();
		Map<String, Integer> hashtags = new HashMap<String, Integer>();
		Map<String, Integer> mentions = new HashMap<String, Integer>();
		
		// delete output file results.txt if it already exists
		File f = new File("results.txt");
		if(f.exists()){f.delete();}

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
		List<String> maxhash_list = maxhash.stream().map(Entry::getKey).collect(Collectors.toList());
		List<String> maxment_list = maxment.stream().map(Entry::getKey).collect(Collectors.toList());
		print(maxhash_list, "20 hashtags");
		print(maxment_list, "20 @-mentions");
				
		// Preprocessing: remove hashtags, @-mentions, URLs, stopwords, emojis, and unnecessary characters
		List<String> tokens = new ArrayList<String>();
		List<String> ngramList = new ArrayList<String>();
		Map<String, Integer> onegrams = new HashMap<String, Integer>();
		Map<String, Integer> twograms = new HashMap<String, Integer>();
		Map<String, Integer> threegrams = new HashMap<String, Integer>();
		Map<String, Integer> fourgrams = new HashMap<String, Integer>();
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
			texts[i] = texts[i].replaceAll("[!@#$%^&+=.,:;’'\"�()?]+", "");
			texts[i] = texts[i].replaceAll("[0-9]+", "");
			texts[i] = texts[i].replaceAll("^-", "");
			texts[i] = texts[i].replaceAll("-$", "");
			texts[i] = texts[i].replaceAll("\\]", "");
			texts[i] = texts[i].replaceAll("\\[", "");
			
			// Remove stopwords
			tempstr = texts[i].split("\\s+");
			for (String s: tempstr) { if(s!=null || s!="") { tokens.add(s); }}
			for(String word: stopwords){
				if(tokens.contains(word)) { tokens.removeAll(Collections.singleton(word)); }
			}
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
		
		// Get the 20 most popular unigrams, bigrams, trigrams, fourgrams
		n = 20;
		
		List<Entry<String, Integer>> max_one = getMax(onegrams, n);
		List<String> maxone_list = max_one.stream().map(Entry::getKey).collect(Collectors.toList());
		print(maxone_list, "20 unigrams");
		
		List<Entry<String, Integer>> max_two = getMax(twograms, n);
		List<String> maxtwo_list = max_two.stream().map(Entry::getKey).collect(Collectors.toList());
		print(maxtwo_list, "20 bigrams");

		List<Entry<String, Integer>> max_three = getMax(threegrams, n);
		List<String> maxthree_list = max_three.stream().map(Entry::getKey).collect(Collectors.toList());
		print(maxthree_list, "20 trigrams");

		List<Entry<String, Integer>> max_four = getMax(fourgrams, n);
		List<String> maxfour_list = max_four.stream().map(Entry::getKey).collect(Collectors.toList());
		print(maxfour_list, "20 fourgrams");

        
		// Stanford Core NLP functions
		List<String> pos_tokens = new ArrayList<String>();
        String text = "";
        for (String token : tokens) {
        	text += token + " ";
        }

        StanfordNLP snlp = new StanfordNLP();
        StanfordNLP.process(text);
        
        pos_tokens = snlp.getPOSNouns();	// get part-of-speech tagger for nouns

		Map<String, Integer> pos_onegrams = new HashMap<String, Integer>();
		Map<String, Integer> pos_twograms = new HashMap<String, Integer>();
		Map<String, Integer> pos_threegrams = new HashMap<String, Integer>();
		Map<String, Integer> pos_fourgrams = new HashMap<String, Integer>();
		
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
		
		// Get the 20 most popular unigrams, bigrams, trigrams, fourgrams with nouns only using Stanford CoreNLP (POS) tagger
		n = 20;
		
		List<Entry<String, Integer>> pos_max_one = getMax(pos_onegrams, n);
		List<String> pos_maxone_list = pos_max_one.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxone_list, "20 POS noun unigrams");
		
		List<Entry<String, Integer>> pos_max_two = getMax(pos_twograms, n);
		List<String> pos_maxtwo_list = pos_max_two.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxtwo_list, "20 POS noun bigrams");

		List<Entry<String, Integer>> pos_max_three = getMax(pos_threegrams, n);
		List<String> pos_maxthree_list = pos_max_three.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxthree_list, "20 POS noun trigrams");

		List<Entry<String, Integer>> pos_max_four = getMax(pos_fourgrams, n);
		List<String> pos_maxfour_list = pos_max_four.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxfour_list, "20 POS noun fourgrams");
		
		// Get 10 most frequency POS patterns
		n = 10;
		
		// pattern: adjective-noun
		Map<String, Integer> pos_adjnouns = new HashMap<String, Integer>();
		Map<String, Integer> pos_advadjnouns = new HashMap<String, Integer>();
		Map<String, Integer> pos_advvb = new HashMap<String, Integer>();
		List<String> adjnouns_list = new ArrayList<String>();
		List<String> advadjnouns_list = new ArrayList<String>();
		List<String> advvb_list = new ArrayList<String>();
		adjnouns_list = snlp.getAdjNouns();
		advadjnouns_list = snlp.getAdvAdjNouns();
		advvb_list = snlp.getAdvVb();
		
		for (String grams: adjnouns_list) {
			if (pos_adjnouns.containsKey(grams)) { pos_adjnouns.put(grams, pos_adjnouns.get(grams) + 1); } 
			else { pos_adjnouns.put(grams, 1); }
		}
		for (String grams: advadjnouns_list) {
			if (pos_advadjnouns.containsKey(grams)) { pos_advadjnouns.put(grams, pos_advadjnouns.get(grams) + 1); } 
			else { pos_advadjnouns.put(grams, 1); }
		}
		for (String grams: advvb_list) {
			if (pos_advvb.containsKey(grams)) { pos_advvb.put(grams, pos_advvb.get(grams) + 1); } 
			else { pos_advvb.put(grams, 1); }
		}
		
		List<Entry<String, Integer>> pos_max_adjnouns = getMax(pos_adjnouns, n);
		List<String> pos_maxadjnouns_list = pos_max_adjnouns.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxadjnouns_list, "10 POS adjective-nouns");
		
		List<Entry<String, Integer>> pos_max_pos_advadjnouns = getMax(pos_advadjnouns, n);
		List<String> pos_maxpos_advadjnouns_list = pos_max_pos_advadjnouns.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxpos_advadjnouns_list, "10 POS adverb-adjective-nouns");
		
		List<Entry<String, Integer>> pos_max_advvb = getMax(pos_advvb, n);
		List<String> pos_maxadvvb_list = pos_max_advvb.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxadvvb_list, "10 POS adjverb-verbs");
		
		// The most common named entities in the dataset
		Map<String, Integer> pos_ners = new HashMap<String, Integer>();
		List<String> ners_list = new ArrayList<String>();
		ners_list = snlp.getNERs();
		
		for (String ner : ners_list) {
			if (pos_ners.containsKey(ner)) { pos_ners.put(ner, pos_ners.get(ner) + 1); } 
			else { pos_ners.put(ner, 1); }
		}
		
		List<Entry<String, Integer>> pos_max_ners = getMax(pos_ners, n);
		List<String> pos_maxner_list = pos_max_ners.stream().map(Entry::getKey).collect(Collectors.toList());
		print(pos_maxner_list, "10 named entities");
		
		// Run my best keyword extraction method on positive and negative tweets
		new BestExtractor();
		BestExtractor.start();
		
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
	
	private static void print(List<String> alist, String type) {
		
		// output to results.txt file
	    try {
            FileWriter writer = new FileWriter("results.txt", true);
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