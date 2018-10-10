package tsl.inference.forwardchaining;

public class SentenceObject {
	
	protected NamedSentence namedSentence = null;
	protected Object[] bindings = null;
	protected SentenceInstance[] childSentenceInstances = null;
	protected int depth = 0;
	
	public NamedSentence getNamedSentence() {
		return namedSentence;
	}

	public void setNamedSentence(NamedSentence namedSentence) {
		this.namedSentence = namedSentence;
	}

	public Object[] getBindings() {
		return bindings;
	}

	public void setBindings(Object[] bindings) {
		this.bindings = bindings;
	}

	public SentenceInstance[] getChildSentenceInstances() {
		return childSentenceInstances;
	}

	public void setChildSentenceInstances(SentenceInstance[] childSentenceInstances) {
		this.childSentenceInstances = childSentenceInstances;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	

}
