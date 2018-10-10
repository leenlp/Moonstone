package moonstone.learning.basilisk;

import java.util.Vector;

import tsl.utilities.VUtils;

public class Lexicon {
	
	Vector<String> words = null;
	
	public Lexicon(String[] words) {
		if (words != null) {
			this.words = VUtils.arrayToVector(words);
		}
	}
	
	public void addWords(Vector<String> words) {
		this.words = VUtils.append(this.words, words);
	}
	
	public void addWord(String word) {
		this.words = VUtils.add(this.words, word);
	}

	public Vector<String> getWords() {
		return words;
	}
	
	public boolean containsWord(String word) {
		return this.words.contains(word);
	}
	
	public boolean containsSubword(String word) {
		for (String lword : this.getWords()) {
			if (word.contains(lword)) {
				return true;
			}
		}
		return false;
	}

}
