package workbench.api.input.knowtator;

import java.util.Vector;

import org.jdom.Element;

// TODO: Auto-generated Javadoc
/**
 * The Class KTSlotMention.
 */
public class KTSlotMention extends KTSimpleInstance {

	/** The knowtator mentioned in id. */
	public String mentionedInID = null;

	/** The knowtator mentioned in instance. */
	public KTSimpleInstance mentionedInInstance = null;

	/** The slot mention. */
	public KTSlot slotMention = null;

	/** The knowtator mention slot i ds. */
	public Vector<String> slotIDs = null;

	/** The knowtator mention slot values. */
	public Vector<KTClassMention> slotValues = null;

	/** The mention slot id. */
	public String mentionSlotID = null;

	public String stringValue = null;

	public KTSlotMention() {
		
	}
	
	public KTSlotMention(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
	}

	public KTSlotMention(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		super(kt, name, v);
	}

	public String getValue() {
		return null;
	}

}
