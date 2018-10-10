package tsl.inference.predicates;

import java.util.Vector;

import tsl.dbaccess.mysql.Analysis;
import tsl.dbaccess.mysql.DBTermIndex;
import tsl.dbaccess.mysql.MySQL;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.lexicon.Word;

public class Predicates {
	
	// General
	
	public static Boolean member_of(Object o, Vector v) {
		return v != null && o != null && v.contains(o);
	}
	
	// MySQL
	
	public static Vector<Vector<DBTermIndex>> get_db_term_indexes(Vector<String> terms) {
		return DBTermIndex.getDBTermIndexes(terms);
	}
	
	public static Vector<Document> get_documents_matching_query(String qstr) {
		return MySQL.getDocumentsMatchingQuery(qstr);
	}
	
	public static Vector<Document> get_sentences_matching_query(String qstr) {
		return MySQL.getDocumentsMatchingQuery(qstr);
	}
	
	public static String get_term(DBTermIndex dbti) {
		return dbti.getTerm();
	}
	
	public static String get_cui(DBTermIndex dbti) {
		return (dbti.isCui() ? dbti.getTerm() : null);
	}
	
	public static Boolean has_concept(DBTermIndex dbti) {
		return dbti.isCui() ? true : false;
	}
	
	public static Boolean is_cui(DBTermIndex dbti) {
		return dbti.isCui();
	}
	
	public static Analysis get_analysis(Document document, String source) {
		return Analysis.getAnalysis(document, source);
	}
	
	public static Boolean push_analysis_kb(Analysis analysis) {
		analysis.pushKnowledgeBase();
		return true;
	}
	
	public static Boolean pop_analysis_kb(Analysis analysis) {
		analysis.popKnowledgeBase();
		return true;
	}
	
	// Lexicon
	// 11/29/2013:  Not yet implemented on in the Word class. Need to extract more
	// information from SpecialistLexicon.
	public static Boolean word_is_verb(Word word) {
		return word.getFormValues() != null
				&& word.getFormValues().contains("verb");
	}
	
	public static Boolean verb_is_past_tense(Word word) {
		return word_is_verb(word) && word.getFormValues().contains("past");
	}
	

}
