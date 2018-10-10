package moonstone.ontology.framenet;

import java.util.Vector;

import org.jdom.Element;

import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

public class FrameElement {
	private Frame frame = null;
	private String id = null;
	private String coreType = null;
	private String themeName = null;
	private Vector<SemType> semTypes = null;
	
	public FrameElement(Frame frame, Element fenode) {
		this.frame = frame;
		
		this.id = fenode.getAttributeValue("ID");
		this.coreType = fenode.getAttributeValue("coreType");
		this.themeName = fenode.getAttributeValue("name");
		Vector<Element> stnodes = JDomUtils.getElementsByName(fenode, "semType");
		if (stnodes != null) {
			for (Element stnode : stnodes) {
				String stid = stnode.getAttributeValue("ID");
				SemType st = frame.ontologyCreation.getSemType(stid);
				this.semTypes = VUtils.add(this.semTypes, st);
			}
			int x = 1;
		}
	}

	public Frame getFrame() {
		return frame;
	}

	public String getId() {
		return id;
	}

	public String getCoreType() {
		return coreType;
	}

	public String getThemeName() {
		return themeName;
	}

	public Vector<SemType> getSemTypes() {
		return semTypes;
	}
	
	public boolean isCore() {
		return "core".equals(this.getCoreType().toLowerCase());
	}
	
	public String toString() {
		String str = "<FrameElement: CoreType=" + this.coreType + ", Name=" + this.themeName
				+ ",ID=" + this.id + ",SemTypes=" + this.semTypes + ">";
		return str;
	}
	

}
