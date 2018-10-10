package workbench.api.db;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.mysql.jdbc.Connection;

import tsl.documentanalysis.document.Document;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.TypeRelationSentence;
import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.annotation.AnnotationEvent;
import workbench.api.annotation.Span;
import workbench.api.gui.TypePanel;
import workbench.api.gui.WBGUI;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Classification;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;

public class MySQLAPI {

	// private static com.mysql.jdbc.Connection connection = null;
	// private static String ConnectionString =
	// "jdbc:mysql://localhost/ieviz?user=root&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";

	tsl.dbaccess.mysql.MySQLAPI TSLMySQL = null;

	public MySQLAPI(String msg) {
		this.TSLMySQL = new tsl.dbaccess.mysql.MySQLAPI("Workbench");
	}

	// Read Workbench type system from DB.
	public TypeSystem readWorkbenchTypeSystem(String tsname) {
		TypeSystem ts = new TypeSystem();
		try {
			Connection c = TSLMySQL.getConnection();
			int tsid = TSLMySQL.getIDByField("ieviz.Typesystem", "typeSystemID", "typeSystemName", tsname);
			ts.setTypeSystemID(tsid);
			ts.setName(tsname);
			String sql = "select typeID, parenttypeID, typename, color from ieviz.AnnotationTypes "
					+ "where typeSystemID = " + tsid;
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			Hashtable<Integer, Type> typehash = new Hashtable();
			Hashtable<Type, Integer> ptypehash = new Hashtable();
			while (rs.next()) {
				int typeID = rs.getInt(1);
				int parenttypeID = rs.getInt(2);
				String typename = rs.getString(3);
				String colorname = rs.getString(4);
				Color color = (colorname != null ? Color.getColor(colorname) : null);
				Type type = new Type(typeID, ts, null, typename, color);
				type.setTypeID(typeID);
				typehash.put(typeID, type);
				if (parenttypeID >= 0) {
					ptypehash.put(type, parenttypeID);
				}
			}
			rs.close();
			ps.close();

			int y = 2;
			String attrsql = "select attributeID, attributename, isclassification from ieviz.AnnotationTypeAttributes where attrTypeID = ?";
			String valuesql = "select value, valueID, isdefault from ieviz.AnnotationTypeAttributeValues where typeAttrID = ?";
			for (Enumeration<Type> e = ptypehash.keys(); e.hasMoreElements();) {
				Type type = e.nextElement();
				Integer tid = type.getTypeID();
				Integer pid = ptypehash.get(type);
				if (pid >= 0) {
					Type ptype = typehash.get(pid);
					type.setParent(ptype);
				}
				int x = 2;
				com.mysql.jdbc.PreparedStatement attrps = (com.mysql.jdbc.PreparedStatement) c
						.prepareStatement(attrsql);
				attrps.setInt(1, tid);
				ResultSet attrrs = attrps.executeQuery();
				while (attrrs.next()) {
					int z = 2;
					Integer aid = attrrs.getInt(1);
					String aname = attrrs.getString(2);
					boolean isclassification = attrrs.getBoolean(3);
					Attribute attr = null;
					if (isclassification) {
						attr = new Classification(aid, type, aname);
					} else {
						attr = new Attribute(aid, type, aname);
					}
					type.addAttribute(attr);
					attr.setAttributeID(aid);
					com.mysql.jdbc.PreparedStatement valueps = (com.mysql.jdbc.PreparedStatement) c
							.prepareStatement(valuesql);
					valueps.setInt(1, aid);
					ResultSet valuers = valueps.executeQuery();
					while (valuers.next()) {
						String value = valuers.getString(1);
						int vid = valuers.getInt(2);
						boolean isdefault = valuers.getBoolean(3);
						attr.addValue(value);
					}
					valuers.close();
					valueps.close();
				}
				attrrs.close();
				attrps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (ts != null) {
			ts.getRootType();
		}
		return ts;
	}

	// NOT USED YET
	public void storeWorkbenchData(Analysis analysis) {
		storeWorkbenchTypeSystem(analysis.getTypeSystem(), analysis.getTypeSystem().getName(),
				"Some description somehow...");
		for (Document document : analysis.getAllDocuments()) {
			this.TSLMySQL.storeDocument(document);
		}
		storeWorkbenchAnnotations(analysis);
	}

	public void storeTSLTypeSystem(Ontology tslOntology, String tsname) {
		Hashtable<Object, Type> thash = new Hashtable();
		int tsid = this.storeTypeSystem(tsname, "Some description...");
		TypeSystem ts = new TypeSystem();
		ts.setName(tsname);
		ts.setTypeSystemID(tsid);
		tslOntology.setTypeSystemID(tsid);
		int maxtid = this.TSLMySQL.getMaxID("ieviz.AnnotationTypes", "typeID");
		if (tslOntology.getAllTypeConstants() != null) {
			for (TypeConstant tc : tslOntology.getAllTypeConstants()) {
				Type type = new Type(++maxtid, ts, null, tc.getName(), null);
				type.setFullName(tc.getPathname());
				thash.put(tc, type);
				thash.put(tc.getName(), type);
			}
			for (TypeConstant tc : tslOntology.getAllTypeConstants()) {
				Type type = thash.get(tc.getName());
				if (tc.getParents() != null) {
					TypeConstant ptc = (TypeConstant) tc.getParents().firstElement();
					Type ptype = thash.get(ptc.getName());
					type.setParent(ptype);
				}
			}
			int maxaid = this.TSLMySQL.getMaxID("ieviz.AnnotationTypeAttributes", "attributeID");
			for (TypeConstant tc : tslOntology.getAllTypeConstants()) {
				Type type = thash.get(tc.getName());
				if (tc.getTypedStringConstants() != null) {
					Classification cattr = new Classification(++maxaid, type, "concept");
					for (StringConstant sc : tc.getTypedStringConstants()) {
						cattr.addValue(sc.getName());
					}
				}
				if (tc.getPropertyConstants() != null) {
					for (PropertyConstant pc : tc.getPropertyConstants()) {
						Attribute attr = Attribute.createAttribute(++maxaid, type, pc.getName());
					}
				}
				if (tc.getTypeRelationSentences() != null) {
					for (TypeRelationSentence trs : tc.getTypeRelationSentences()) {
						Type stype = thash.get(trs.getSubjectType());
						Type mtype = thash.get(trs.getModifierType());
						// this.storeTypeRelation(trs.getRelation().getName(),
						// tsid);
						this.storeTypeRelation(trs.getRelation().getName(), tsid, stype, mtype);
					}
				}
			}
			for (TypeConstant tc : tslOntology.getAllTypeConstants()) {
				Type type = thash.get(tc.getName());
				this.storeWorkbenchType(type, tslOntology.getTypeSystemID());
			}
		}
		this.closeConnection();
	}

	// Need to figure out later how to add argument types. Current Workbench
	// type system
	// doesn't support them...
	public int storeTypeRelation(String rname, int tsid) {
		int rid = this.TSLMySQL.getIDByFields("ieviz.AnnotationTypeRelations", "relationID",
				new String[] { "relationName", "tsID" }, new Object[] { rname, tsid });
		try {
			if (rid < 0) {
				Connection c = this.TSLMySQL.getConnection();
				String sql = "insert into ieviz.AnnotationTypeRelations (relationName, tsID) " + "values (?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, rname);
				ps.setInt(2, tsid);
				ps.execute();
				ps.close();
				rid = this.TSLMySQL.getIDByFields("ieviz.AnnotationTypeRelations", "relationID",
						new String[] { "relationName", "tsID" }, new Object[] { rname, tsid });
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rid;
	}

	// The right way...
	public int storeTypeRelation(String rname, int tsid, Type stype, Type mtype) {
		int rid = this.TSLMySQL.getIDByFields("ieviz.AnnotationTypeRelations", "relationID",
				new String[] { "relationName", "tsID", "subjecttypeID", "modifiertypeID" },
				new Object[] { rname, tsid, stype.getTypeID(), mtype.getTypeID() });
		try {
			if (rid < 0) {
				Connection c = this.TSLMySQL.getConnection();
				String sql = "insert into ieviz.AnnotationTypeRelations (relationName, tsID, subjecttypeID, modifiertypeID) "
						+ "values (?, ?, ?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, rname);
				ps.setInt(2, tsid);
				ps.setInt(3, stype.getTypeID());
				ps.setInt(4, mtype.getTypeID());
				ps.execute();
				ps.close();
				rid = this.TSLMySQL.getIDByFields("ieviz.AnnotationTypeRelations", "relationID",
						new String[] { "relationName", "tsID", "subjecttypeID", "modifiertypeID" },
						new Object[] { rname, tsid, stype.getTypeID(), mtype.getTypeID() });
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rid;
	}

	public void storeWorkbenchTypeSystem(TypeSystem ts, String tsname, String description) {
		int tsid = this.TSLMySQL.getIDByField("TypeSystem", "typeSystemID", "typeSystemName", tsname);
		try {
			if (tsid < 0) {
				Connection c = this.TSLMySQL.getConnection();
				String sql = "insert into ieviz.TypeSystem (typeSystemName, typeSystemDescription) values (" + "'"
						+ tsname + "', '" + description + "')";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.execute();
				ps.close();
				tsid = this.TSLMySQL.getIDByField("TypeSystem", "typeSystemID", "typeSystemName", tsname);
			}
			if (ts.getTypes() != null) {
				for (Type type : ts.getTypes()) {
					storeWorkbenchType(type, tsid);
				}
			}
			this.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getTypeSystemID(String tsname) {
		return this.getTypeSystemID(tsname, "??");
	}

	public int getTypeSystemID(String tsname, String description) {
		int tsid = this.TSLMySQL.getIDByField("TypeSystem", "TypeSystemID", "typeSystemName", tsname);
		if (tsid < 0) {
			try {
				Connection c = TSLMySQL.getConnection();
				String sql = "insert into ieviz.Typesystem (typeSystemName, description) values (?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, tsname);
				ps.setString(2, description);
				ps.execute();
				ps.close();
				tsid = this.TSLMySQL.getIDByField("TypeSystem", "TypeSystemID", "typeSystemName", tsname);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tsid;
	}

	public void storeWorkbenchType(Type type, int tsid) {
		try {
			Connection c = TSLMySQL.getConnection();
			int ptypeid = (type.getParent() != null ? type.getParent().getTypeID() : -1);
			String colorname = (type.getColor() != null ? type.getColor().toString() : null);
			String pathname = type.getFullname();
			String sql = "insert into ieviz.AnnotationTypes (typeID, parenttypeID, typeSystemID, typename, pathname, color) values (?, ?, ?, ?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			int tid = type.getTypeID();
			ps.setInt(1, type.getTypeID());
			ps.setInt(2, ptypeid);
			ps.setInt(3, tsid);
			ps.setString(4, type.getName());
			ps.setString(5, pathname);
			ps.setString(6, colorname);
			ps.execute();
			ps.close();
			if (type.getAttributeStrings() != null) {
				for (String aname : type.getAttributeStrings()) {
					Attribute attr = (Attribute) type.getAttribute(aname);
					String attrsql = "insert into ieviz.AnnotationTypeAttributes (attributeID, attrTypeID, attributename, isclassification) values (?, ?, ?, ?)";
					ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(attrsql);
					ps.setInt(1, attr.getAttributeID());
					ps.setInt(2, attr.getParent().getTypeID());
					ps.setString(3, attr.getName());
					ps.setBoolean(4, attr.isClassification());
					ps.execute();
					ps.close();
					if (attr.getValues() != null) {
						for (String value : attr.getValues()) {
							String avsql = "insert into ieviz.AnnotationTypeAttributeValues (typeAttrID, value, isdefault) values (?, ?, ?)";
							ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(avsql);
							ps.setInt(1, attr.getAttributeID());
							ps.setString(2, value);
							ps.setBoolean(3, false);
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

	public Analysis readAnalysis(TypeSystem oldts, String tsname, String corpusName, int primaryRunID,
			int secondaryRunID) {
		Analysis analysis = null;
		try {
			int x = 1;
			TypeSystem ts = readWorkbenchTypeSystem(tsname);

			// Before 8/28/2017
			// TypeSystem ts = null;
			// if (oldts != null) {
			// ts = oldts;
			// } else {
			// ts = readWorkbenchTypeSystem(tsname);
			// }

			int cid = this.TSLMySQL.getIDByField("ieviz.Corpora", "corpusID", "corpusName", corpusName);
			analysis = new Analysis(ts);
			analysis.setPrimaryRunID(primaryRunID);
			analysis.setSecondaryRunID(secondaryRunID);
			analysis.setCorpusID(cid);
			readWorkbenchAnnotations(analysis, primaryRunID, true);
			readWorkbenchAnnotations(analysis, secondaryRunID, false);
			analysis.postProcessAnnotationCollectionsMySQL();

			// 8/28/2017
			if (WBGUI.WorkbenchGUI != null) {
				TypePanel tp = WBGUI.WorkbenchGUI.getTypePanel();
				tp.setAnalysis(analysis);
				tp.setRootTypeObject(ts.getRootType());
				tp.createJTree();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return analysis;
	}

	// NEED: When I start a new document, create a new annotation hash,
	// and pass last annotation hash to a method that will extract relations.

	public void readWorkbenchAnnotations(Analysis analysis, int annotationRunID, boolean isPrimary) {
		try {
			int x = 2;
			Connection c = TSLMySQL.getConnection();
			String sql = "select ieviz.Annotations.annotationID, " + "ieviz.Annotations.parentannotationID, "
					+ "ieviz.Annotations.typeID, ieviz.Annotations.level, ieviz.Annotations.spans, "
					+ "ieviz.Documents.documentID, ieviz.Documents.name, "
					+ "ieviz.AnnotationTypeAttributes.attributename, ieviz.AnnotationTypeAttributeValues.value, "
					+ "ieviz.Annotators.annotatorName FROM ieviz.Annotations INNER JOIN ieviz.Documents, "
					+ "		ieviz.AnnotationTypeAttributes, ieviz.AnnotationAttributes, "
					+ "		ieviz.AnnotationTypeAttributeValues, ieviz.Annotators, ieviz.AnnotationRuns "
					+ "WHERE ieviz.Annotations.runID = ieviz.AnnotationRuns.annotationRunID "
					+ "     and ieviz.AnnotationAttributes.attrID = ieviz.AnnotationTypeAttributes.attributeID "
					+ "		and ieviz.AnnotationAttributes.annID = ieviz.Annotations.annotationID "
					+ "		and ieviz.AnnotationAttributes.valID = ieviz.AnnotationTypeAttributeValues.valueID "
					+ "		and ieviz.AnnotationTypeAttributeValues.typeAttrID = ieviz.AnnotationTypeAttributes.attributeID "
					+ "     and ieviz.Annotators.annotatorID = ieviz.AnnotationRuns.runAnnotatorID "
					+ "     and ieviz.Documents.corpID = ieviz.AnnotationRuns.runCorpusID "
					+ "		and ieviz.annotationRuns.annotationRunID = ? " + " ORDER BY ieviz.Documents.name, "
					+ "		ieviz.Annotations.level, " + "		ieviz.Annotations.annotationID";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, annotationRunID);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			AnnotationCollection ac = null;
			AnnotationEvent ae = null;
			String lastDocumentName = "aaa";
			int lastLevel = -1;
			int lastAnnotationID = -1;
			AnnotationEvent lastAE = null;
			Annotation lastAnnotation = null;
			Annotation annotation = null;
			Hashtable<Integer, Annotation> aidhash = new Hashtable();
			while (rs.next()) {
				x = 1;
				int annotationID = rs.getInt(1);
				int parentID = rs.getInt(2);
				int typeID = rs.getInt(3);
				int level = rs.getInt(4);
				String spanstr = rs.getString(5);
				int docid = rs.getInt(6);
				String dname = rs.getString(7);
				String attrname = rs.getString(8);
				String valuename = rs.getString(9);
				String annotatorName = rs.getString(10);
				Type type = analysis.getTypeSystem().getType(typeID);
				String aidstr = String.valueOf(annotationID);
				if (!dname.equals(lastDocumentName) || ae == null) {
					ae = analysis.getAnnotationEvent(dname);
					if (ae == null) {
						ae = new AnnotationEvent(analysis, null, dname);
						ae.setDocumentID(docid);
						analysis.addAnnotationEvent(ae);
						analysis.putDocumentAnnotationEventHash(dname, ae);
					}
					lastAE = ae;
					lastDocumentName = dname;
				} else {
					ae = lastAE;
				}
				if (annotationID != lastAnnotationID) {
					ac = (isPrimary ? ae.getPrimaryAnnotationCollection() : ae.getSecondaryAnnotationCollection());
					if (ac == null) {
						ac = new AnnotationCollection();
						ac.setAnnotationEvent(ae);
						ac.setPrimary(isPrimary);
						if (isPrimary) {
							ae.setPrimaryAnnotationCollection(ac);
						} else {
							ae.setSecondaryAnnotationCollection(ac);
						}
					}
					annotation = new Annotation(ac, type);
					String[] sstrs = spanstr.split(",");
					for (String substr : sstrs) {
						String[] sestrs = substr.split("-");
						int start = Integer.parseInt(sestrs[0]);
						int end = Integer.parseInt(sestrs[1]);
						annotation.addSpan(start, end);
					}
					annotation.setAnnotationID(annotationID);
					annotation.setAnnotatorName(annotatorName);
					aidhash.put(annotationID, annotation);
					lastAnnotation = annotation;
					lastAnnotationID = annotationID;
				} else {
					annotation = lastAnnotation;
				}
				ac.addAnnotation(annotation, aidstr);
				Attribute attr = type.getAttribute(attrname);
				if (attr != null) {
					annotation.putAttributeValue(attr, valuename);
					if (attr.isClassification()) {
						annotation.setClassificationValue(valuename);
						// I think I got this wrong...
						// annotation.setClassificationValue(attr);
					}
				}
			}
			rs.close();
			ps.close();
			for (Enumeration<Annotation> e = aidhash.elements(); e.hasMoreElements();) {
				Annotation subject = e.nextElement();
				addAnnotationRelations(subject, aidhash);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addAnnotationRelations(Annotation subject, Hashtable<Integer, Annotation> aidhash) {
		Connection c = TSLMySQL.getConnection();
		String sql = "select ieviz.AnnotationRelations.modifierAnnotationID, ieviz.AnnotationTypeRelations.relationName "
				+ "from ieviz.AnnotationRelations INNER JOIN ieviz.AnnotationTypeRelations "
				+ "WHERE ieviz.AnnotationTypeRelations.relationID = ieviz.AnnotationRelations.relationID "
				+ " 	AND ieviz.AnnotationRelations.subjectAnnotationID = ?";
		try {
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, subject.getAnnotationID());
			ps.execute();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int mid = rs.getInt(1);
				String rname = rs.getString(2);
				Annotation modifier = aidhash.get(mid);
				if (rname != null && modifier != null) {
					subject.addRelation(rname, subject, modifier);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void storeWorkbenchAnnotations(Analysis analysis) {
		if (analysis.getAllPrimarySnippetAnnotations() != null) {
			int maxid = this.TSLMySQL.getMaxID("ieviz.Annotations", "annotationID");
			for (Annotation annotation : analysis.getAllPrimarySnippetAnnotations()) {
				annotation.setAnnotationID(++maxid);
				storeWorkbenchAnnotation(annotation, analysis.getPrimaryRunID());
			}
		}
		if (analysis.getAllSecondarySnippetAnnotations() != null) {
			int maxid = this.TSLMySQL.getMaxID("ieviz.Annotations", "annotationID");
			for (Annotation annotation : analysis.getAllSecondarySnippetAnnotations()) {
				annotation.setAnnotationID(++maxid);
				storeWorkbenchAnnotation(annotation, analysis.getSecondaryRunID());
			}
		}
		this.closeConnection();
	}

	public void storeWorkbenchAnnotation(Annotation annotation, int runID) {
		Connection c = TSLMySQL.getConnection();
		String sql = "insert into ieviz.Annotations (annotationID, runID, parentannotationID, typeID, level, spans) values (?, ?, ?, ?, ?, ?)";
		int parentAnnotationID = (annotation.getParent() != null ? annotation.getParent().getAnnotationID() : -1);
		String spanstr = "";
		for (int i = 0; i < annotation.getSpans().size(); i++) {
			Span span = annotation.getSpans().elementAt(i);
			spanstr += span.getStart() + "-" + span.getEnd();
			if (i < annotation.getSpans().size() - 1) {
				spanstr += ",";
			}
		}
		try {
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, annotation.getAnnotationID());
			ps.setInt(2, runID); /// WHERE TO GET RUN ID?
			ps.setInt(3, parentAnnotationID);
			ps.setInt(4, annotation.getDocument().getDocumentID());
			ps.setInt(5, annotation.getType().getTypeID());
			ps.setInt(6, annotation.getLevel());
			ps.setString(7, spanstr);
			ps.execute();
			ps.close();
			String attrsql = "insert into ieviz.AnnotationAttributes (annotationID, attributename, value) values (?, ?, ?)";
			for (Attribute attr : annotation.getAllAttributes()) {
				String value = annotation.getAttributeValue(attr).toString();
				ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(attrsql);
				ps.setInt(1, annotation.getAnnotationID());
				ps.setString(2, attr.getName());
				ps.setString(3, value);
				ps.execute();
				ps.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int storeAnnotator(String annotatorName, String affiliation) {
		int aid = this.TSLMySQL.getIDByField("Annotators", "annotatorID", "annotatorName", annotatorName);
		try {
			if (aid < 0) {
				Connection c = TSLMySQL.getConnection();
				String sql = "insert into ieviz.Annotators (annotatorName, affiliation) values (?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, annotatorName);
				ps.setString(2, affiliation);
				ps.execute();
				ps.close();
				aid = this.TSLMySQL.getIDByField("Annotators", "annotatorID", "annotatorName", annotatorName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.closeConnection();
		return aid;
	}

	public int getAnnotationToolID(String toolName, String description) {
		int tid = this.TSLMySQL.getIDByField("ieviz.AnnotationTools", "toolID", "toolName", toolName);
		try {
			if (tid < 0) {
				Connection c = TSLMySQL.getConnection();
				String sql = "insert into ieviz.AnnotationTools (toolName, description) values (?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, toolName);
				ps.setString(2, description);
				ps.execute();
				ps.close();
				tid = this.TSLMySQL.getIDByField("ieviz.AnnotationTools", "toolID", "toolName", toolName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tid;
	}

	public int storeCorpus(String corpusName, String location, String description) {
		int cid = this.TSLMySQL.getIDByField("Corpora", "corpusID", "corpusName", corpusName);
		try {
			if (cid < 0) {
				Connection c = TSLMySQL.getConnection();
				String sql = "insert into ieviz.Corpora (corpusName, location, description) values (?, ?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, corpusName);
				ps.setString(2, location);
				ps.setString(3, description);
				ps.execute();
				ps.close();
				cid = this.TSLMySQL.getIDByField("Corpora", "corpusID", "corpusName", corpusName);
				this.closeConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cid;
	}

	public int storeAnnotationTool(String toolName, String toolDescription) {
		int tid = this.TSLMySQL.getIDByField("ieviz.AnnotationTools", "toolID", "toolName", toolName);
		if (tid < 0) {
			try {
				Connection c = TSLMySQL.getConnection();
				String sql = "insert into ieviz.AnnotationTools (toolName, description) values (?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, toolName);
				ps.setString(2, toolDescription);
				ps.execute();
				ps.close();
				tid = this.TSLMySQL.getIDByField("ieviz.AnnotationTools", "toolID", "toolName", toolName);
				this.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tid;
	}

	public int storeTypeSystem(String tsname, String description) {
		int tsid = -1;
		try {
			tsid = this.TSLMySQL.getIDByField("TypeSystem", "typeSystemID", "typeSystemName", tsname);
			if (tsid < 0) {
				Connection c = TSLMySQL.getConnection();
				String sql = "insert into ieviz.TypeSystem (typeSystemName, description) values (?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, tsname);
				ps.setString(2, description);
				ps.execute();
				ps.close();
				tsid = this.TSLMySQL.getIDByField("TypeSystem", "typeSystemID", "typeSystemName", tsname);
				this.closeConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tsid;
	}

	public int getMaxAnnotationRun(String annotatorName, String toolName, String corpusName, String typeSystemName) {
		tsl.dbaccess.mysql.MySQLAPI m = this.getTSLMySQL();
		int annotatorID = storeAnnotator(annotatorName, "<AutoStored>");
		int corpusID = this.storeCorpus(corpusName, "<AutoLocation>", "<AutoStored>");
		int toolID = this.storeAnnotationTool(toolName, "<AutoStored>");
		int typeSystemID = this.storeTypeSystem(typeSystemName, "<AutoStored>");
		int maxrunid = m.getIDByFields("ieviz.AnnotationRuns", "annotationRunID",
				new String[] { "runAnnotatorID", "runCorpusID", "runToolID", "runTypeSystemID" },
				new Object[] { annotatorID, corpusID, toolID, typeSystemID }, true);
		return maxrunid;
	}

	public int createAnnotationRun(String annotatorName, String toolName, String corpusName, String typeSystemName) {
		int x = 1;
		tsl.dbaccess.mysql.MySQLAPI m = this.getTSLMySQL();
		int annotatorID = storeAnnotator(annotatorName, "<AutoStored>");
		int toolID = this.storeAnnotationTool(toolName, "<AutoStored>");
		int corpusID = this.storeCorpus(corpusName, "<AutoLocation>", "<AutoStored>");
		int typeSystemID = this.storeTypeSystem(typeSystemName, "<AutoStored>");
		int runid = m.getMaxID("ieviz.AnnotationRuns", "annotationRunID");
		try {
			Connection c = m.getConnection();
			String sql = "insert into ieviz.AnnotationRuns (annotationRunID, runAnnotatorID, runCorpusID, runToolID, runTypeSystemID) "
					+ "values (?, ?, ?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, ++runid);
			ps.setInt(2, annotatorID);
			ps.setInt(3, corpusID);
			ps.setInt(4, toolID);
			ps.setInt(5, typeSystemID);
			ps.execute();
			ps.close();
			this.closeConnection();
		} catch (SQLException e) {
			runid = -1;
			e.printStackTrace();
		}
		return runid;
	}

	public void closeConnection() {
		this.TSLMySQL.closeConnection();
	}

	public tsl.dbaccess.mysql.MySQLAPI getTSLMySQL() {
		return TSLMySQL;
	}

}
