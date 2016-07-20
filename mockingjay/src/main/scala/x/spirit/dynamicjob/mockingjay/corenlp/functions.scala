package x.spirit.dynamicjob.mockingjay.corenlp

import java.util.Properties

import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{CleanXmlAnnotator, StanfordCoreNLP}
import edu.stanford.nlp.pipeline.CoreNLPProtos.Sentiment
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.simple.{Document, Sentence}
import edu.stanford.nlp.util.Quadruple
import org.apache.spark.rdd.RDD
import x.spirit.dynamicjob.core.utils.StringUtils._

import scala.collection._
import scala.collection.JavaConverters._

/**
  * Created by zhangwei on 7/17/16.
  */
object functions {
  private var sentimentPipeline: StanfordCoreNLP = null;

  private def getOrCreateSentimentPipeline(): StanfordCoreNLP = {
    if (sentimentPipeline == null) {
      val props = new Properties()
      props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment")
      sentimentPipeline = new StanfordCoreNLP(props)
      System.out.println("Loading corenlp annotators.")
    }
    sentimentPipeline
  }

  case class OpenIE(subject: String, relation: String, target: String, confidence: Double) {
    def this(quadruple: Quadruple[String, String, String, java.lang.Double]) =
      this(quadruple.first, quadruple.second, quadruple.third, quadruple.fourth)
  }

  case class CorefMention(sentNum: Int, startIndex: Int, mention: String)

  case class CorefChain(representative: String, mentions: Seq[CorefMention])

  case class SemanticGraphEdge(
                                        source: String,
                                        sourceIndex: Int,
                                        relation: String,
                                        target: String,
                                        targetIndex: Int,
                                        weight: Double)


  /**
    * Cleans XML tags in a document.
    */
  def cleanxml( document: String):String = {
    val words = new Sentence(document).words().asScala
    val labels = words.map { w =>
      val label = new CoreLabel()
      label.setWord(w)
      label
    }
    val annotator = new CleanXmlAnnotator()
    annotator.process(labels.asJava).asScala.map(_.word()).mkString(" ")
  }

  def cleanNonAlphabet(document : String,removeDash:Boolean=false, removeComma:Boolean=false,
                       removePeriod:Boolean=false):String ={
    var rst = document.replaceAll("[^a-zA-Z\\s\\.,_-]", "").replace('_',' ');
    if (removeDash) {rst = rst.replace('-',' ')}
    if (removePeriod) {rst = rst.replace('.',' ')}
    if (removeComma) {rst = rst.replace(',',' ')}
    rst;
  }

  def cleanNonAscii(document: String, removeDash:Boolean=false, removeComma:Boolean=false,
                    removePeriod:Boolean=false):String = {
    var rst = document.replaceAll("[^\\x00-\\x7F\\s\\.,_-]", "").replace('_',' ');
    if (removeDash) {rst = rst.replace('-',' ')}
    if (removePeriod) {rst = rst.replace('.',' ')}
    if (removeComma) {rst = rst.replace(',',' ')}
    rst;
  }

  def cleanUrl(document: String):String = {
    removeUrl(document);
  }

  def purifyProfileName(name : String):String = {
    cleanNonAlphabet(cleanUrl(name), true, true, true)
  }

  def purifyTweet(tweet:String):String = {
    cleanNonAlphabet(cleanUrl(tweet))
  }

  def purifyTweetAsSentences(tweet:String):Iterable[String] = {
    ssplit(purifyTweet(tweet).replace(',','.'))
  }

  /**
    * Tokenizes a sentence into words.
    *
    * @see [[Sentence#words]]
    */
  def tokenize (sentence: String) : Iterable[String] ={
    //sentence.replace('.',' ').replace(',',' ').split("[\\s]+")
    new Sentence(sentence).words().asScala
  }

  /**
    * Splits a document into sentences.
    *
    * @see [[Document#sentences]]
    */
  def ssplit (document: String) : Iterable[String] = {
    //document.split('.').map(_.trim)
    new Document(document).sentences().asScala.map(_.text())
  }

  /**
    * Generates the part of speech tags of the sentence.
    *
    * @see [[Sentence#posTags]]
    */
  def pos (sentence: String) : mutable.Buffer[String]={
    new Sentence(sentence).posTags().asScala
  }

  /**
    * Generates the word lemmas of the sentence.
    *
    * @see [[Sentence#lemmas]]
    */
  def lemma (sentence: String): mutable.Buffer[String] ={
    new Sentence(sentence).lemmas().asScala
  }

  /**
    * Generates the named entity tags of the sentence.
    *
    * @see [[Sentence#nerTags]]
    */
  def ner(sentence: String) : Iterable[String] ={
    new Sentence(sentence).nerTags().asScala
  }

  /**
    * Generates the semantic dependencies of the sentence.
    *
    * @see [[Sentence#dependencyGraph]]
    */
  def depparse (sentence: String):mutable.Buffer[SemanticGraphEdge] ={
    new Sentence(sentence).dependencyGraph().edgeListSorted().asScala.map { edge =>
      SemanticGraphEdge(
        edge.getSource.word(),
        edge.getSource.index(),
        edge.getRelation.toString,
        edge.getTarget.word(),
        edge.getTarget.index(),
        edge.getWeight)
    }
  }

  /**
    * Generates the coref chains of the document.
    */
  def coref (document: String ):Seq[CorefChain]={
    new Document(document).coref().asScala.values.map { chain =>
      val rep = chain.getRepresentativeMention.mentionSpan
      val mentions = chain.getMentionsInTextualOrder.asScala.map { m =>
        CorefMention(m.sentNum, m.startIndex, m.mentionSpan)
      }
      CorefChain(rep, mentions)
    }.toSeq
  }

  /**
    * Generates the Natural Logic notion of polarity for each token in a sentence,
    * returned as "up", "down", or "flat".
    *
    * @see [[Sentence#natlogPolarities]]
    */
  def natlog (sentence: String) ={
    new Sentence(sentence).natlogPolarities().asScala
      .map(_.toString)
  }

  /**
    * Generates a list of Open IE triples as flat (subject, relation, target, confidence) quadruples.
    *
    * @see [[Sentence#openie]]
    */
  def openie (sentence: String):Seq[OpenIE] ={
    new Sentence(sentence).openie().asScala.map(q => new OpenIE(q)).toSeq
  }


  /**
    * Measures the sentiment of an input sentence on a scale of 0 (strong negative) to 4 (strong
    * positive).
    * If the input contains multiple sentences, only the first one is used.
    *
    * @see [[Sentiment]]
    */
  def sentiment (sentence: String): Int = {
    val pipeline = getOrCreateSentimentPipeline()
    val annotation = pipeline.process(sentence).get(classOf[CoreAnnotations.SentencesAnnotation]).asScala
    if (annotation == null || annotation.isEmpty) {
      return 0;
    } else {
      val senti_scores = annotation.map(_.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree]))
        .map((RNNCoreAnnotations.getPredictedClass(_) - 2))
      val avg_score = Math.round(senti_scores.sum.toDouble/senti_scores.length.toDouble).toInt;
      return avg_score;
    }
  }

}
