package workbench.api.input.knowtator;

public class KTRelation extends KTSimpleInstance {

	KTClass relation = null;
	KTAnnotation firstArgument = null;
	KTAnnotation secondArgument = null;

	public KTRelation(KTClass relation, KTAnnotation firstArgument,
			KTAnnotation secondArgument) throws Exception {
		this.relation = relation;
		this.firstArgument = firstArgument;
		this.secondArgument = secondArgument;
		firstArgument.addRelation(this);
	}
	
	public String toString() {
		String rstr = this.relation.name;
		String fastr = firstArgument.getText();
		String sastr = secondArgument.getText();
		String str = "<" + rstr + "=" + fastr + "," + sastr + ">";
		return str;
	}
}
