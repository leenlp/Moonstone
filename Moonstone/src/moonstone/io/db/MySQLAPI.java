package moonstone.io.db;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.mysql.jdbc.Connection;

import moonstone.annotation.Annotation;
import moonstone.grammar.Grammar;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.expression.term.Term;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.TypeRelationSentence;
import tsl.utilities.FUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.constraint.ConstraintMatch;
import workbench.api.constraint.ConstraintPacket;
import workbench.api.gui.WBGUI;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Type;

public class MySQLAPI {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private boolean useMySQL = false;
	String corpusName = null;
	// private int corpusID = -1;
	// private int primaryRunID = -1;
	// private int secondaryRunID = -1;
	private String primaryAnnotatorName = null;
	private String secondaryAnnotatorName = null;
	private String typeSystemName = null;
	private String primaryAnnotationToolName = null;
	private String secondaryAnnotationToolName = null;
	// private int primaryAnnotatorID = -1;
	// private int secondaryAnnotatorID = -1;
	// private int primaryAnnotationToolID = -1;
	// private int secondaryAnnotationToolID = -1;
	// private int typeSystemID = -1;
	private workbench.api.db.MySQLAPI workbenchMySQL = null;

	private static String GrammarConceptAttributeName = "concept";

	public MySQLAPI(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
		this.initialize();
	}

	private void initialize() {
		this.workbenchMySQL = new workbench.api.db.MySQLAPI("Workbench");
		MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
		this.useMySQL = msri.getStartupParameters().isPropertyTrue("UseMySQL");
		this.corpusName = msri.getStartupParameters().getPropertyValue("MySQLCorpus");
		this.primaryAnnotatorName = msri.getStartupParameters().getPropertyValue("PrimaryAnnotatorName");
		this.secondaryAnnotatorName = msri.getStartupParameters().getPropertyValue("SecondaryAnnotatorName");
		this.typeSystemName = msri.getStartupParameters().getPropertyValue("TypeSystemName");
		this.primaryAnnotationToolName = msri.getStartupParameters().getPropertyValue("PrimaryAnnotationToolName");
		this.secondaryAnnotationToolName = msri.getStartupParameters().getPropertyValue("SecondaryAnnotationToolName");
	}

	public void reinitializeWorkbenchFromDB() {
		MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
		if (this.useMySQL && this.corpusName != null && this.primaryAnnotatorName != null
				&& this.secondaryAnnotatorName != null) {
			WBGUI wb = this.moonstoneRuleInterface.getWorkbench();
			ConstraintMatch oldcm = wb.getAnalysis().getSelectedConstraintMatch();
			ConstraintPacket oldcp = wb.getAnalysis().getSelectedConstraintPacket();
			int primaryRunID = this.workbenchMySQL.getMaxAnnotationRun(this.primaryAnnotatorName,
					this.primaryAnnotationToolName, this.corpusName, this.typeSystemName);
			int secondaryRunID = this.workbenchMySQL.getMaxAnnotationRun(this.secondaryAnnotatorName,
					this.secondaryAnnotationToolName, this.corpusName, this.typeSystemName);
			Analysis newanalysis = this.workbenchMySQL.readAnalysis(wb.getAnalysis().getTypeSystem(),
					this.typeSystemName, this.corpusName, primaryRunID, secondaryRunID);
			newanalysis.setWorkbenchGUI(wb);
			wb.setAnalysis(newanalysis);
			newanalysis.setSelectedDocument(null);
			if (oldcp != null) {
				newanalysis.setSelectedConstraintPacket(oldcp.getName());
				newanalysis.updateStatistics();
			}
		}
	}

	public void processDocumentsFromCorpus() {
		int annid = this.workbenchMySQL.storeAnnotator("moonstone", "University of Utah");
		int runid = this.workbenchMySQL.createAnnotationRun("moonstone", this.primaryAnnotatorName, this.corpusName,
				this.typeSystemName);
		boolean processRelations = true;
		int tsid = this.workbenchMySQL.getTypeSystemID(this.getTypeSystemName());
		Vector<Document> documents = this.workbenchMySQL.getTSLMySQL().readCorpusDocuments(this.corpusName);
		if (documents != null) {
			for (Document doc : documents) {
				this.moonstoneRuleInterface.getControl().applyNarrativeGrammarToText(doc, true, true, true);
				Vector<Annotation> annotations = this.moonstoneRuleInterface.getSentenceGrammar()
						.getDisplayedAnnotations();
				if (annotations != null) {
					tsl.dbaccess.mysql.MySQLAPI m = this.workbenchMySQL.getTSLMySQL();
					int aid = m.getMaxID("ieviz.Annotations", "annotationID");
					for (Annotation annotation : annotations) {
						if (annotation.getAnnotationDBID() < 0) {
							aid = this.assignAnnotationDBID(annotation, ++aid);
						}
					}
					for (Annotation annotation : annotations) {
						this.storeMoonstoneAnnotation(annotation, runid, this.getTypeSystemName());
					}
				}
			}
		}
	}

	public int assignAnnotationDBID(Annotation annotation, int anid) {
		int highest = anid;
		annotation.setAnnotationDBID(anid);
		if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				if (child.getAnnotationDBID() < 0) {
					highest = assignAnnotationDBID(child, ++highest);
				}
			}
		}
		return highest;
	}

	public void storeMoonstoneAnnotation(Annotation annotation, int runid, String tsname) {
		try {
			if (annotation.isStoredInDB() || !annotation.isInterpreted()) {
				return;
			}
			int x = 1;
			annotation.setStoredInDB(true);
			int anid = annotation.getAnnotationDBID();
			int tsid = this.workbenchMySQL.getTypeSystemID(tsname);
			tsl.dbaccess.mysql.MySQLAPI m = this.workbenchMySQL.getTSLMySQL();
			Connection c = m.getConnection();
			Document d = annotation.getDocument();
			TypeConstant tc = annotation.getType();
			String concept = annotation.getConcept().toString();
			if (tc == null) {
				return;
			}
			int tid = m.getIDByField("ieviz.AnnotationTypes", "typeID", "pathname", tc.getPathname());
			String spanstr = annotation.getTextStart() + "-" + annotation.getTextEnd();
			String sql = "insert into ieviz.Annotations (annotationID, runID, parentannotationID, typeID, level, spans) values (?, ?, ?, ?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, anid);
			ps.setInt(2, runid);
			ps.setInt(3, -1);
			ps.setInt(4, tid);
			ps.setInt(5, -1);
			ps.setString(6, spanstr);
			ps.execute();
			ps.close();
			if (annotation.getSemanticInterpretation() != null
					&& annotation.getSemanticInterpretation().hasProperties()) {
				for (String attribute : annotation.getSemanticInterpretation().getPropertyNames()) {
					Object value = annotation.getProperty(attribute);
					int attrid = m.getIDByFields("ieviz.AnnotationTypeAttributes", "attributeID",
							new String[] { "attrTypeID", "attributename" }, new Object[] { tid, attribute });
					if (attrid < 0) {
						continue;
					}
					int vid = -1;
					if (!(value instanceof Annotation)) {
						vid = m.getIDByFields("ieviz.AnnotationTypeAttributeValues", "valueID",
								new String[] { "typeAttrID", "value" }, new Object[] { attrid, value });
						if (vid >= 0) {
							String attrsql = "insert into ieviz.AnnotationAttributes (annid, attrID, valID) values (?, ?, ?)";
							ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(attrsql);
							ps.setInt(1, anid);
							ps.setInt(2, attrid);
							ps.setInt(3, vid);
							ps.execute();
							ps.close();
						}
					}
				}

				Vector<RelationSentence> rsv = annotation.getSemanticInterpretation().getRelationSentences();
				if (rsv != null) {
					// NOT FINISHED
					for (RelationSentence rs : rsv) {
						String rname = rs.getRelation().getName();
						Term modifier = rs.getModifier();
						String classname = modifier.getClass().getName();

						if (modifier instanceof Annotation) {
							Annotation modifierAnnotation = (Annotation) modifier;
							int mid = modifierAnnotation.getAnnotationDBID();
							// 8/18/2017: For now, just use first
							// relation with the
							// given name. I need a way to determine how
							// to search for
							// abstract argument type ids based on
							// annotation type ids...
							int rid = this.workbenchMySQL.getTSLMySQL().getIDByFields("ieviz.AnnotationTypeRelations",
									"relationID", new String[] { "relationName", "tsid" },
									new Object[] { rname, tsid });
							if (rid >= 0) {
								this.storeMoonstoneAnnotationRelation(rid, anid, mid);
								this.storeMoonstoneAnnotation(modifierAnnotation, runid, tsname);
							} else {
								x = 1;
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void storeMoonstoneAnnotationRelation(int rid, int sid, int mid) {
		try {
			tsl.dbaccess.mysql.MySQLAPI m = this.workbenchMySQL.getTSLMySQL();
			Connection c = m.getConnection();
			String sql = "insert into ieviz.AnnotationRelations (relationID, subjectAnnotationID, modifierAnnotationID) values (?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, rid);
			ps.setInt(2, sid);
			ps.setInt(3, mid);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void storeMoonstoneAnnotation_Before_8_26(Annotation annotation, int anid, int runid) {
		tsl.dbaccess.mysql.MySQLAPI m = this.workbenchMySQL.getTSLMySQL();
		Document d = annotation.getDocument();
		TypeConstant tc = annotation.getType();
		String concept = annotation.getConcept().toString();
		int tid = m.getIDByField("ieviz.AnnotationTypes", "typeID", "pathname", tc.getPathname());
		int attrid = m.getIDByFields("ieviz.AnnotationTypeAttributes", "attributeID",
				new String[] { "attrTypeID", "attributename" }, new Object[] { tid, GrammarConceptAttributeName });
		int vid = m.getIDByFields("ieviz.AnnotationTypeAttributeValues", "valueID",
				new String[] { "typeAttrID", "value" }, new Object[] { attrid, concept });
		Connection c = m.getConnection();
		String sql = "insert into ieviz.Annotations (annotationID, runID, parentannotationID, typeID, level, spans) values (?, ?, ?, ?, ?, ?)";
		String spanstr = annotation.getTextStart() + "-" + annotation.getTextEnd();
		try {
			if (vid >= 0) {
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setInt(1, anid);
				ps.setInt(2, runid);
				ps.setInt(3, -1);
				ps.setInt(4, tid);
				ps.setInt(5, -1);
				ps.setString(6, spanstr);
				ps.execute();
				ps.close();
				String attrsql = "insert into ieviz.AnnotationAttributes (annid, attrID, valID) values (?, ?, ?)";
				ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(attrsql);
				ps.setInt(1, anid);
				ps.setInt(2, attrid);
				ps.setInt(3, vid);
				ps.execute();
				ps.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void storeAnnotationTypeSystem(Ontology ontology, String tsname) {
		this.workbenchMySQL.storeTSLTypeSystem(ontology, tsname);
		this.storeGrammarRuleTypeAttributes(ontology);
	}

	// 8/18/2017-- NEEDS FIXING UP
	public void storeGrammarRuleTypeAttributes(Ontology ontology) {
		tsl.dbaccess.mysql.MySQLAPI m = new tsl.dbaccess.mysql.MySQLAPI("Workbench");
		int tsid = ontology.getTypeSystemID();
		try {
			Connection c = m.getConnection();
			for (Rule rule : this.moonstoneRuleInterface.getSentenceGrammar().getAllRules()) {
				if (rule.getSemanticRelations() != null) {
					for (PatternRelationSentence prs : rule.getSemanticRelations()) {
						String rname = prs.getRelation().getName();
						int rid = this.workbenchMySQL.getTSLMySQL().getIDByFields("ieviz.AnnotationTypeRelations",
								"relationID", new String[] { "relationName", "tsid" }, new Object[] { rname, tsid });
						if (rid < 0) {
							String attrsql = "insert into ieviz.AnnotationTypeRelations (relationName, tsID) values (?, ?)";
							com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
									.prepareStatement(attrsql);
							ps.setString(1, rname);
							ps.setInt(2, tsid);
							ps.execute();
							ps.close();
						}
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void storeGrammarRuleTypeAttributes_BEFORE_8_18_2017() {
		tsl.dbaccess.mysql.MySQLAPI m = new tsl.dbaccess.mysql.MySQLAPI("Workbench");
		try {
			for (Rule rule : this.moonstoneRuleInterface.getSentenceGrammar().getAllRules()) {
				if (rule.isSingleton() && rule.getResultType() instanceof TypeConstant
						&& rule.getResultConcept() instanceof StringConstant) {
					String vstr = rule.getResultConcept().toString();
					TypeConstant tc = (TypeConstant) rule.getResultType();
					int tid = m.getIDByField("ieviz.AnnotationTypes", "typeID", "pathname", tc.getPathname());
					int aid = m.getIDByFields("ieviz.AnnotationTypeAttributes", "attributeID",
							new String[] { "typeID", "attributename" },
							new Object[] { tid, GrammarConceptAttributeName });
					Connection c = m.getConnection();
					if (tid >= 0 && aid < 0) {
						int maxaid = m.getMaxID("ieviz.AnnotationTypeAttributes", "attributeID");
						aid = maxaid + 1;
						String attrsql = "insert into ieviz.AnnotationTypeAttributes (attributeID, typeID, attributename, isclassification) values (?, ?, ?, ?)";
						com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
								.prepareStatement(attrsql);
						ps.setInt(1, aid);
						ps.setInt(2, tid);
						ps.setString(3, GrammarConceptAttributeName);
						ps.setBoolean(4, true);
						ps.execute();
						ps.close();
					}
					int vid = m.getIDByFields("ieviz.AnnotationTypeAttributeValues", "valueID",
							new String[] { "attributeID", "value" }, new Object[] { aid, vstr });
					if (vid < 0 && aid >= 0) {
						int maxvid = m.getMaxID("ieviz.AnnotationTypeAttributeValues", "valueID");
						vid = maxvid + 1;
						String vsql = "insert into ieviz.AnnotationTypeAttributeValues (valueID, attributeID, value) values (?, ?, ?)";
						com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
								.prepareStatement(vsql);
						ps.setInt(1, vid);
						ps.setInt(2, aid);
						ps.setString(3, vstr);
						ps.execute();
						ps.close();
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean hasCompleteWorkbenchParameters() {
		return this.isUseMySQL() && this.corpusName != null && this.getPrimaryAnnotatorName() != null
				&& this.getSecondaryAnnotatorName() != null;
	}

	public MoonstoneRuleInterface getMoonstoneRuleInterface() {
		return moonstoneRuleInterface;
	}

	public boolean isUseMySQL() {
		return useMySQL;
	}

	public String getPrimaryAnnotatorName() {
		return primaryAnnotatorName;
	}

	public String getSecondaryAnnotatorName() {
		return secondaryAnnotatorName;
	}

	public String getTypeSystemName() {
		return typeSystemName;
	}

	public static String getGrammarConceptAttributeName() {
		return GrammarConceptAttributeName;
	}

	public String getPrimaryAnnotationToolName() {
		return primaryAnnotationToolName;
	}

	public String getSecondaryAnnotationToolName() {
		return secondaryAnnotationToolName;
	}

	public String getCorpusName() {
		return corpusName;
	}

	public workbench.api.db.MySQLAPI getWorkbenchMySQL() {
		return workbenchMySQL;
	}

}
