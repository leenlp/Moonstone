package tsl.expression.term.array;

import tsl.expression.Expression;
import tsl.utilities.MathUtils;

// 7/7/2014:  Array of expressions and their corresponding weights, for use in
// machine learning applications.

public class ExpressionArray {
	
	private Expression[] expressions = null;
	private double[] weights = null;
	
	public ExpressionArray(Expression[] e) {
		this.expressions = e;
		this.weights = new double[e.length];
	}
	
	public ExpressionArray(Expression[] e, double[] w) {
		this.expressions = e;
		this.weights = w;
	}
	
	public double getCosineSimilarity(double[] v) {
		if (this.weights.length == v.length) {
			return MathUtils.cosineSimilarity(this.weights, v);
		}
		return -1;
	}
	
	public Expression getExpressionAt(int index) {
		return this.expressions[index];
	}
	
	public void setExpressionAt(Expression e, int index) {
		this.expressions[index] = e;
	}
	
	public double getWeightAt(int index) {
		return this.weights[index];
	}
	
	public void setWeightAt(double w, int index) {
		this.weights[index] = w;
	}

	public Expression[] getExpressions() {
		return expressions;
	}

	public void setExpressions(Expression[] expressions) {
		this.expressions = expressions;
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

}
