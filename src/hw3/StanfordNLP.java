package hw3;

/**
 * @author Jane Liu
 * Homework 3
 *
 * Classes:
 * 	StanfordNLP: generates POS tagger for nouns, runs syntactic dependency parser, runs the named-entity tagger.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class StanfordNLP {

	public static List<String> pos_nouns = new ArrayList<String>();
	public static List<String> pos_adjnouns = new ArrayList<String>();
	public static List<String> pos_advadjnouns = new ArrayList<String>();
	public static List<String> pos_advvb = new ArrayList<String>();
	public static Map<String, Integer> named_entities = new HashMap<String, Integer>();

	public static void process(String documentText) {
		
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
	    props.setProperty("coref.algorithm", "neural");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    
	    Annotation document = new Annotation(documentText);
	    pipeline.annotate(document);

	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
        for(CoreMap sentence: sentences) {
        	
        	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
        		String word = token.get(TextAnnotation.class);
        		String pos = token.get(PartOfSpeechAnnotation.class);
        		String ner = token.get(NamedEntityTagAnnotation.class);
        		
        		// get nouns
        		if(pos.matches("NN|NNP|NNPS|PRP")) {
        			pos_nouns.add(word);
        		}
        		
        		// get superlative adjective and noun (eg, "fastest horse")
        		
        		
        		
        		if(pos.matches("JJS NN")) {
        			pos_adjnouns.add(word);
        		}

//        		// get adverb-adject-nouns (eg, "loudly playing music")
//        		if(pos.matches("RB JJ NN|NNP|NNPS|PRP")) {
//        			pos_advadjnouns.add(word);
//        		}        		
//        		
//        		// get adverb-verb gerunds (eg, "happily singing")
//        		if(pos.matches("RB VBG")) {
//        			pos_advvb.add(word);
//        		}      		
 
        		
//        		// Get NERs
//        		if(ner!=null) {
//        			if (named_entities.containsKey(word)) { named_entities.put(word, pos_twograms.get(word) + 1); } 
//    				else { named_entities.put(word, 1); }
//        		}
        	}
        }
	}
	
	public List<String> getPOSNouns() {
		
		for (String noun : pos_nouns) {
			System.out.print(noun + ", ");
		}
		System.out.println();
		
		return pos_nouns;
	}
	
	public List<String> getAdjNouns() {
		for (String adjnoun : pos_adjnouns) {
			System.out.print(adjnoun + ", ");
		}
		System.out.println();
		
		return pos_adjnouns;
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
