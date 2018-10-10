package io.i2b2;

import java.util.Vector;

import tsl.utilities.VUtils;

public class CorefChain {
	
	Vector<Classification> chain = null;
	
	public CorefChain() {
		
	}
	
	public void addClassification(Classification c) {
		this.chain = VUtils.add(this.chain, c);
	}
	
	public Vector<Classification> getChain() {
		return this.chain;
	}

}
