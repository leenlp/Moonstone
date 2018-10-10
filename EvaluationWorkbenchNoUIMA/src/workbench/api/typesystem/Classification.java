package workbench.api.typesystem;

public class Classification extends Attribute {
	
	public Classification(Type type, String aname) {
		this(-1, type, aname);
	}
	
	public Classification(int cid, Type type, String aname) {
		super(cid, type, aname);
		type.addAttribute(this);
	}
}
