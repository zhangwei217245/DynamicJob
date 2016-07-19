package x.spirit.dynamicjob.core.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;


/**
 * Created by zhangwei on 7/19/16.
 */
public class CoreNLPUtils {

    // Initialize the CoreNLP text processing pipeline
    public static Properties props = new Properties();
    public static StanfordCoreNLP pipeline;

    static{
        // Set text processing pipeline's annotators
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        // Use Shift-Reduce Constituency Parsing (O(n),
        // http://nlp.stanford.edu/software/srparser.shtml) vs CoreNLP's default
        // Probabilistic Context-Free Grammar Parsing (O(n^3))
        //props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
        pipeline = new StanfordCoreNLP(props);
    }

    public static int sentiment(String document){
        Annotation annotation = pipeline.process(document);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        int sentiScore = Long.valueOf(Math.round(sentences.stream().mapToInt(s->{
            Tree tree = s.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree) - 2;
            System.out.println(s+","+sentiment);
            return sentiment;
        }).average().orElse(0.0d))).intValue();
        return sentiScore;
    }

    public static void main(String[] args) {
        String[] a = {"The mapToInt operation returns a new stream of type IntStream (which is a stream that contains only integer values). ",
        "The operation applies the function specified in its parameter to each element in a particular stream. ",
                "In this example, the function is Person::getAge, which is a method reference that returns the age of the member. ",
                "(Alternatively, you could use the lambda expression e -> e.getAge().) ",
                "Consequently, the mapToInt operation in this example returns a stream that contains the ages of all male members in the collection roster"};
        StringBuffer sb = new StringBuffer();
        long start = System.currentTimeMillis();
        for (String s : a){
            System.out.println(sentiment(s));
            sb.append(s);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println((double)duration/(double)a.length);

        start = System.currentTimeMillis();
        for (int i = 0; i< 1000; i++) {
            new Document(sb.toString()).sentences().forEach(sentence -> System.out.println(sentence));
        }
        duration = System.currentTimeMillis() - start;

        System.out.println((double)duration/(double)1000);

        int sent = sentiment(sb.toString().replace(',',' ').replace('.',' '));
        System.out.println(sent);
    }
}
