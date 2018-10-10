package io.knowtator;

import java.util.Vector;

import org.jdom.Element;

// TODO: Auto-generated Javadoc
/**
 * The Class KTSlotMention.
 */
public class KTSlotMention extends KTSimpleInstance {

	/** The knowtator mentioned in id. */
	String mentionedInID = null;

	/** The knowtator mentioned in instance. */
	KTSimpleInstance mentionedInInstance = null;

	/** The slot mention. */
	KTSlot slotMention = null;

	/** The knowtator mention slot i ds. */
	Vector<String> slotIDs = null;

	/** The knowtator mention slot values. */
	Vector<KTClassMention> slotValues = null;

	/** The mention slot id. */
	String mentionSlotID = null;

	String stringValue = null;

	public KTSlotMention(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
	}

	public KTSlotMention(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		super(kt, name, v);
	}

	public Object getValue() throws Exception {
		return null;
	}

}
