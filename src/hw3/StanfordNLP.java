package hw3;

/**
 * @author Jane Liu
 * Homework 3
 *
 * Classes:
 * 	StanfordNLP: generates POS tagger for nouns and other patterns, runs the syntactic dependency 
 * 	parser and named-entity tagger.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;


public class StanfordNLP {

	public static List<String> pos_nouns = new ArrayList<String>();
	public static List<String> pos_adjnouns = new ArrayList<String>();
	public static List<String> pos_advadjnouns = new ArrayList<String>();
	public static List<String> pos_advvb = new ArrayList<String>();
	public static List<String> named_entities = new ArrayList<String>();

	public static void process(String documentText) {
		
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
	    props.setProperty("coref.algorithm", "neural");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    Annotation document = new Annotation(documentText);
	    pipeline.annotate(document);

	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    List<String[]> corpus_temp = new ArrayList<String[]>();
	    int count = 0;
	    
        for(CoreMap sentence: sentences) {
        	
//          // Get the dependency graph of each sentence
//        	SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
        	
        	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
        		
        		String[] data = new String[3];
        		String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);
                String ner = token.get(NamedEntityTagAnnotation.class);
                count ++;

                data[0] = word;
                data[1] = pos;
                data[2] = ner;
                
                corpus_temp.add(data);
        	}        	
        }
        
        System.out.println();
        
        String[][] corpus = corpus_temp.toArray(new String[count][3]);
        
        // corpus contains string arrays with a word, its pos, and if it is an NER.
        for (int i=0; i<(corpus.length-3); i++) {
        
        	String word = corpus[i][0];
        	String pos = corpus[i][1];
        	String ner = corpus[i][2];
        	String word2 = corpus[i+1][0];
    		String pos2 = corpus[i+1][1];
			String word3 = corpus[i+2][0];
			String pos3 = corpus[i+2][1];

    		// find nouns only
        	if (pos.equals("NN") || pos.equals("NNP") || pos.equals("NNPS")) {
        		pos_nouns.add(word);
        	}
        	// find superlative adjectives and nouns (eg, "fastest car")
        	else if (pos.equals("JJ")) {
        		
        		if (pos2.equals("NN") || pos2.equals("NNP") || pos2.equals("NNPS")) {
        			word = word + " " + word2;
        			pos_adjnouns.add(word);
        		}
        	}
        	else if (pos.equals("RB")) {
        		
        		// find adverb-adjective-noun combinations (eg, "very happy child")
        		if (pos2.equals("JJ")) {
        			
        			if (pos3.equals("NN") || pos3.equals("NNP") || pos3.equals("NNPS")) {
        				word = word + " " + word2 + " " + word3;
        				pos_advadjnouns.add(word);
        			}
        		}
        		// find adverb-verb (eg, "quietly talk")
        		else if (pos.equals("VB")) {
        			word = word + " " + word2;
        			pos_advvb.add(word);
        		}
        	}
        	
        	// find NERs
        	if (ner.matches("PERSON") || ner.matches("DATE") || ner.matches("ORGANIZATION") || ner.matches("DURATION") 
        			|| ner.matches("LOCATION") || ner.matches("CITY") || ner.matches("NATIONALITY") 
        			|| ner.matches("TIME") || ner.matches("TITLE") || ner.matches("RELIGION") || ner.matches("MISC") 
        			|| ner.matches("COUNTRY")) {
        		named_entities.add(ner);
        	}

        }
	}
	
	public List<String> getPOSNouns() {
		return pos_nouns;
	}
	
	public List<String> getAdjNouns() {
		return pos_adjnouns;
	}
	
	public List<String> getAdvAdjNouns() {
		return pos_advadjnouns;
	}
	
	public List<String> getAdvVb() {
		return pos_advvb;
	}
	
	public List<String> getNERs() {
		return named_entities;
	}

}


