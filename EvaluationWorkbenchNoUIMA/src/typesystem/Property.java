package typesystem;

// TODO: Auto-generated Javadoc
/**
 * The Class Property.
 */
public class Property extends TypeObject {
	
	/** The name. */
	String name = null;
	
	/** The value. */
	String value = null;
	
	/**
	 * Instantiates a new property.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public Property(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see typesystem.TypeObject#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	

}
