package annotation;

// TODO: Auto-generated Javadoc
/**
 * Objects representing relations, with relation name ("relation") and
 * EVAnnotation relatum ("relatum").
 */
public class RelationObject {

	/** The relation. */
	String relation = null;

	/** The relatum. */
	EVAnnotation relatum = null;

	/**
	 * Instantiates a new relation object.
	 * 
	 * @param relation
	 *            the relation
	 * @param relatum
	 *            the relatum
	 */
	public RelationObject(String relation, EVAnnotation relatum) {
		this.relation = relation;
		this.relatum = relatum;
	}

	/**
	 * Gets the relation.
	 * 
	 * @return the relation
	 */
	public String getRelation() {
		return relation;
	}

	/**
	 * Sets the relation.
	 * 
	 * @param relation
	 *            the new relation
	 */
	public void setRelation(String relation) {
		this.relation = relation;
	}

	/**
	 * Gets the relatum.
	 * 
	 * @return the relatum
	 */
	public EVAnnotation getRelatum() {
		return relatum;
	}

	/**
	 * Sets the relatum.
	 * 
	 * @param relatum
	 *            the new relatum
	 */
	public void setRelatum(EVAnnotation relatum) {
		this.relatum = relatum;
	}
	
	public String toString() {
		String str = "<RelationObject:Relation=" + relation + ",Relatum=" + relatum + ">";
		return str;
	}

}
