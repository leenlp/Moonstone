package typesystem;

// TODO: Auto-generated Javadoc
/**
 * The Class Relation.
 */
public class Relation extends TypeObject {

	/** The subject. */
	TypeObject subject = null;
	
	/** The modifier. */
	TypeObject modifier = null;

	/**
	 * Instantiates a new relation.
	 *
	 * @param ts the ts
	 * @param name the name
	 * @param subject the subject
	 * @param modifier the modifier
	 */
	public Relation(TypeSystem ts, String name, TypeObject subject,
			TypeObject modifier) {
		super(ts, name);
		this.subject = subject;
		this.modifier = modifier;
	}

}
