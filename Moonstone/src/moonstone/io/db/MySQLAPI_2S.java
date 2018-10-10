package moonstone.io.db;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.mysql.jdbc.Connection;

import moonstone.annotation.Annotation;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.expression.term.Term;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.FUtils;
import workbench.api.Analysis;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.annotation.AnnotationEvent;
import workbench.api.annotation.Span;
import workbench.api.constraint.ConstraintMatch;
import workbench.api.constraint.ConstraintPacket;
import workbench.api.gui.TypePanel;
import workbench.api.gui.WBGUI;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.StartupParameters;

public class MySQLAPI_2S {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private boolean useMySQL = false;
	String corpusName = null;
	private String primaryAnnotatorName = null;
	private String secondaryAnnotatorName = null;
	private String typeSystemName = null;
	private String primaryAnnotationToolName = null;
	private String secondaryAnnotationToolName = null;
	// private workbench.api.db.MySQLAPI workbenchMySQL = null;

	private static String GrammarConceptAttributeName = "concept";

	public MySQLAPI_2S(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
		this.initialize();
	}

	private void initialize() {
		MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
		this.useMySQL = msri.getStartupParameters().isPropertyTrue("UseMySQL");
		this.corpusName = msri.getStartupParameters().getPropertyValue("MySQLCorpus");
		this.primaryAnnotatorName = msri.getStartupParameters().getPropertyValue("PrimaryAnnotatorName");
		this.secondaryAnnotatorName = msri.getStartupParameters().getPropertyValue("SecondaryAnnotatorName");
		this.typeSystemName = msri.getStartupParameters().getPropertyValue("TypeSystemName");
		this.primaryAnnotationToolName = msri.getStartupParameters().getPropertyValue("PrimaryAnnotationToolName");
		this.secondaryAnnotationToolName = msri.getStartupParameters().getPropertyValue("SecondaryAnnotationToolName");
	}

	// 7/12/2018: NOT USED
	public String readDocumentText(int docid) {
		String text = null;
		try {
			Connection c = getConnection();
			String sql = "select text from billscubadb.Documents where documentID = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, docid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				text = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}

	public boolean analyzeDirectoryFiles(String cname, String dname) {
		Vector<File> files = FUtils.readFilesFromDirectory(dname);
		if (files != null) {
			int runid = storeAnnotationRunDef("moonstone", "moonstone");
			for (File file : files) {
				String text = FUtils.readFile(file);
				Document doc = new Document(file.getName(), text);
				int did = this.storeDocument(doc, runid);
				doc.setDocumentID(did);
				this.moonstoneRuleInterface.getControl().applyNarrativeGrammarToText(doc, true, true, true);
				Vector<Annotation> annotations = this.moonstoneRuleInterface.getDocumentGrammar()
						.getDisplayedAnnotations();
				if (annotations != null) {
					for (Annotation annotation : annotations) {
						this.storeMoonstoneAnnotation(annotation);
					}
				}
			}
			this.closeConnection();
			return true;
		}
		this.closeConnection();
		return false;
	}

	public int storeAnnotationRunDef(String runname, String rundesc) {
		int runid = -1;
		try {
			Connection c = this.getConnection();
			String sql = "insert into billscubadb.nlp_run_def (run_name, run_description) values (?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, runname);
			ps.setString(2, rundesc);
			ps.execute();
			ps.close();
			runid = this.getMaxIDWhere("nlp_run_def", "run_id", "run_name", runname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return runid;
	}

	// 7/12/2018
	public int storeDocument(Document document, int runID) {
		int did = -1;
		try {
			Connection c = getConnection();
			String sql = "insert into billscubadb.nlp_result_doc (run_id, rpt_id) values (?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, runID);
			ps.setString(2, document.getName());
			ps.execute();
			ps.close();
			did = this.getMaxIDWhere("billscubadb.nlp_result_doc", "result_doc_id", "doc_src", document.getName());
			document.setDocumentID(did);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return did;
	}

	public void storeMoonstoneAnnotation(Annotation annotation) {
		try {
			annotation.setStoredInDB(false);
			if (annotation.isStoredInDB() || !annotation.isInterpreted()) {
				return;
			}
			int x = 0;
			annotation.setStoredInDB(true);
			Connection c = this.getConnection();
			Document doc = annotation.getDocument();
			TypeConstant tc = annotation.getType();
			String concept = annotation.getConcept().toString();
			if (tc == null) {
				return;
			}
			String sql = "insert into NLP_RESULT_SNIPPET (RESULT_DOC_ID, SNIPPET_1, "
					+ "MENTION_TYPE, TERM_SNIPPET_1_START_LOC, TERM_SNIPPET_1_END_LOC) values(?, ?, ?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, doc.getDocumentID());
			ps.setString(2, annotation.getText());
			ps.setString(3, tc.getName());
			ps.setInt(4, annotation.getTextStart());
			ps.setInt(5, annotation.getTextEnd());
			ps.execute();
			ps.close();
			int aid = this.getMaxIDWhere("nlp_result_snippet", "snippet_id", "result_doc_id",
					String.valueOf(doc.getDocumentID()));
			annotation.setAnnotationDBID(aid);
			if (annotation.getSemanticInterpretation() != null
					&& annotation.getSemanticInterpretation().hasProperties()) {
				for (String attribute : annotation.getSemanticInterpretation().getPropertyNames()) {
					Object value = annotation.getProperty(attribute);
					sql = "insert into nlp_result_features (snippet_id, result_doc_id, "
							+ "feature_name, feature_value) values (?, ?, ?, ?)";
					ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
					ps.setInt(1, aid);
					ps.setInt(2, doc.getDocumentID());
					ps.setString(3, attribute);
					ps.setString(4, value.toString());
					ps.execute();
					ps.close();
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

	public String getCorpusName() {
		return corpusName;
	}

	/////////////////////////////////////////////////////////////////
	////////// WORKBENCH MYSQL CODE /////////////////////////////////

	public Analysis readAnalysis(TypeSystem oldts, String tsname, String corpusName, int primaryRunID,
			int secondaryRunID) {
		Analysis analysis = null;
		try {
			// 7/11/2018: No accomodation for workbench type system yet.
			// TypeSystem ts = readWorkbenchTypeSystem(tsname);

			int cid = this.getIDByField("billscubadb.Corpora", "corpusID", "corpusName", corpusName);
			EvaluationWorkbench wb = initializeOldWorkbench();
			analysis = new Analysis(this.moonstoneRuleInterface.getWorkbench(), wb);
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
				tp.setRootTypeObject(analysis.getTypeSystem().getRootType());
				tp.createJTree();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return analysis;
	}

	// Bill Scuba variety, 9/5/2018
	public void readWorkbenchAnnotations(Analysis analysis, int runID, boolean isPrimary) {
		try {
			int x = 2;
			Connection c = this.getConnection();
			
			String sql = 
					"select billscubadb.nlp_result_doc.DOC_SRC, " 
					+ "billscubadb.nlp_result_doc.NLP_PIPELINE_ID, " 
					
					+ "billscubadb.nlp_result_snippet.SNIPPET_ID, "
					+ "billscubadb.nlp_result_snippet.TERM_SNIPPET_1_START_LOC, " 
					+ "billscubadb.nlp_result_snippet.TERM_SNIPPET_1_END_LOC, " 
					+ "billscubadb.nlp_result_snippet.MENTION_TYPE, " 
					
					+ "billscubadb.nlp_result_features.FEATURE_NAME, "
					+ "billscubadb.nlp_result_features.FEATURE_VALUE "
					
					+ "FROM billscubadb.nlp_result_doc INNER JOIN billscubadb.nlp_result_snippet, "
					+ "billscubadb.nlp_result_features "
					
					+ "WHERE billscubadb.nlp_result_doc.run_id = ? "
					+ " AND billscubadb.nlp_result_snippet.RESULT_DOC_ID = billscubadb.nlp_result_doc.result_doc_id "
					+ " AND billscubadb.nlp_result_features.SNIPPET_ID = billscubadb.nlp_result_snippet.SNIPPET_ID "

					+ "ORDER BY billscubadb.nlp_result_doc.DOC_SRC, billscubadb.nlp_result_snippet.SNIPPET_ID";
					
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, runID);
			ResultSet rs = ps.executeQuery();
			AnnotationCollection ac = null;
			AnnotationEvent ae = null;
			String lastDocumentName = "aaa";
			int lastLevel = -1;
			int lastAnnotationID = -1;
			AnnotationEvent lastAE = null;
			workbench.api.annotation.Annotation lastAnnotation = null;
			workbench.api.annotation.Annotation annotation = null;
			Hashtable<Integer, workbench.api.annotation.Annotation> aidhash = new Hashtable();
			while (rs.next()) {
				int docid = rs.getInt(1);
				String docsrc = rs.getString(1);
				int pipelineid = rs.getInt(2);
				int snippetid = rs.getInt(3);
				int tstart = rs.getInt(4);
				int tend = rs.getInt(5);
				String mentionType = rs.getString(6);
				String attrname = rs.getString(7);
				String valuename = rs.getString(8);

				Type type = analysis.getTypeSystem().getType(mentionType);
				String aidstr = String.valueOf(snippetid);
				if (!docsrc.equals(lastDocumentName) || ae == null) {
					ae = analysis.getAnnotationEvent(docsrc);
					if (ae == null) {
						ae = new AnnotationEvent(analysis, null, docsrc);
						ae.setDocumentID(docid);
						analysis.addAnnotationEvent(ae);
						analysis.putDocumentAnnotationEventHash(docsrc, ae);
					}
					lastAE = ae;
					lastDocumentName = docsrc;
				} else {
					ae = lastAE;
				}
				if (snippetid != lastAnnotationID) {
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
					annotation = new workbench.api.annotation.Annotation(ac, type);
					annotation.addSpan(tstart, tend);  // Need way to add multiple spans
					annotation.setAnnotationID(snippetid);
					annotation.setClassificationValue(mentionType);
					aidhash.put(snippetid, annotation);
					lastAnnotation = annotation;
					lastAnnotationID = snippetid;
					ac.addAnnotation(annotation, aidstr);
				} else {
					annotation = lastAnnotation;
				}
				Attribute attr = type.getAttribute(attrname);
				if (attr != null) {
					annotation.putAttributeValue(attr, valuename);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void storeWorkbenchAnnotations(Analysis analysis) {
		if (analysis.getAllPrimarySnippetAnnotations() != null) {
			int maxid = this.getMaxID("billscubadb.Annotations", "annotationID");
			for (workbench.api.annotation.Annotation annotation : analysis.getAllPrimarySnippetAnnotations()) {
				annotation.setAnnotationID(++maxid);
				storeWorkbenchAnnotation(annotation, analysis.getPrimaryRunID());
			}
		}
		if (analysis.getAllSecondarySnippetAnnotations() != null) {
			int maxid = this.getMaxID("billscubadb.Annotations", "annotationID");
			for (workbench.api.annotation.Annotation annotation : analysis.getAllSecondarySnippetAnnotations()) {
				annotation.setAnnotationID(++maxid);
				storeWorkbenchAnnotation(annotation, analysis.getSecondaryRunID());
			}
		}
		this.closeConnection();
	}

	public void storeWorkbenchAnnotation(workbench.api.annotation.Annotation annotation, int runID) {
		Connection c = this.getConnection();
		String sql = "insert into billscubadb.nlp_result_snippet (SNIPPET_ID, MENTION_TYPE, TERM_SNIPPET_1_START_LOC, TERM_SNIPPET_1_END_LOC) values (?, ?, ?, ?)";
		try {
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setInt(1, annotation.getAnnotationID());
			ps.setInt(2, runID); /// WHERE TO GET RUN ID?
			ps.setString(3, annotation.getClassificationValue().toString());
			ps.setInt(3, annotation.getStart());
			ps.setInt(4, annotation.getEnd());
			ps.execute();
			ps.close();
			String attrsql = "insert into billscubadb.NLP_RESULT_FEATURES (SNIPPET_ID, FEATURE_NAME, FEATURE_VALUE) values (?, ?, ?)";
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

	

	/////////////////////////////////////////////////////////////////
	////////// TSL MYSQL CODE ///////////////////////////////////////

	private static com.mysql.jdbc.Connection connection = null;
	private static String ConnectionString = "jdbc:mysql://localhost/billscubadb?user=root&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";
	private static String[] AllDBTableNames = new String[] { "nlp_run_def", "nlp_result_doc", "nlp_result_snippet",
			"nlp_result_features" };
	private static String DBSchemaName = "billscubadb";

	public int getMaxIDWhere(String tablename, String fieldname, String reffieldname, String refvalue) {
		return getMaxID(tablename, fieldname);
	}

	public int getMaxID(String tablename, String fieldname) {
		int maxid = 0;
		try {
			Connection c = getConnection();
			String sql = "select MAX(" + fieldname + ") from " + tablename;
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				maxid = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return maxid;
	}

	public int getIDByFields(String tablename, String idfieldname, String[] fieldnames, Object[] values) {
		return getIDByFields(tablename, idfieldname, fieldnames, values, false);
	}

	public int getIDByFields(String tablename, String idfieldname, String[] fieldnames, Object[] values,
			boolean isMax) {
		int id = -1;
		int x = 1;
		String sql = null;
		try {
			Connection c = getConnection();
			if (isMax) {
				sql = "select MAX(" + idfieldname + ") from " + tablename + " where ";
			} else {
				sql = "select " + idfieldname + " from " + tablename + " where ";
			}
			for (int i = 0; i < fieldnames.length; i++) {
				if (values[i] instanceof String || values[i] instanceof Term) {
					sql += "" + fieldnames[i] + " = '" + values[i] + "'";
				} else {
					sql += "" + fieldnames[i] + " = " + values[i];
				}
				if (i < fieldnames.length - 1) {
					sql += " and ";
				}
			}
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public int getIDByField(String tablename, String idfieldname, String fieldname, Object value) {
		int id = -1;
		int x = 1;
		try {
			Connection c = getConnection();
			String sql = "select " + idfieldname + " from " + tablename + " where " + fieldname + " = ";
			if (value instanceof String) {
				sql += "'" + value + "'";
			} else {
				sql += value;
			}
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	// 9/18/2017
	public ArrayList<String> getStringsByFields(String tablename, String targetfieldname, String[] fieldnames,
			Object[] values) {
		ArrayList<String> rv = new ArrayList<String>(0);
		String sql = null;
		try {
			Connection c = getConnection();
			if (fieldnames != null) {
				sql = "select " + targetfieldname + " from " + tablename + " where ";
				for (int i = 0; i < fieldnames.length; i++) {
					if (values[i] instanceof String || values[i] instanceof Term) {
						sql += "" + fieldnames[i] + " = '" + values[i] + "'";
					} else {
						sql += "" + fieldnames[i] + " = " + values[i];
					}
					if (i < fieldnames.length - 1) {
						sql += " and ";
					}
				}
			} else {
				sql = "select " + targetfieldname + " from " + tablename;
			}
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Object o = rs.getObject(1);
				rv.add(o.toString());
			}
			Collections.sort(rv);
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rv;
	}

	public void deleteByIDField(String tablename, String idfieldname, Object value) {
		try {
			Connection c = getConnection();
			String sql = "delete from " + tablename + " where " + idfieldname + " = ";
			if (value instanceof String) {
				sql += "'" + value + "'";
			} else {
				sql += value;
			}
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateIntField(String tableName, String idFieldName, int value, String[] fieldnames, String[] values,
			Boolean[] isString) {
		try {
			Connection c = getConnection();
			String sql = "update " + tableName + " set " + idFieldName + " = " + value + " where ";
			for (int i = 0; i < fieldnames.length; i++) {
				if (isString[i]) {
					sql += "" + fieldnames[i] + " = '" + values[i] + "'";
				} else {
					sql += "" + fieldnames[i] + " = " + values[i];
				}
				if (i < fieldnames.length - 1) {
					sql += " and ";
				}
			}
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateIntField(String tableName, String idFieldName, int value, String fieldname, Object fieldvalue) {
		try {
			Connection c = getConnection();
			String sql = "update " + tableName + " set " + idFieldName + " = " + value + " where " + fieldname + " = ";
			if (fieldvalue instanceof String) {
				sql += "'" + value + "'";
			} else {
				sql += fieldvalue.toString();
			}
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void emptyTable(String tableName) {
		try {
			Connection c = getConnection();
			String sql = "delete from " + tableName;
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.execute();
			ps.close();
			this.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		try {
			if (connection != null) {
				return connection;
			}
			Class driverClass = Class.forName("com.mysql.jdbc.Driver");
			driverClass.newInstance();

			Properties props = new Properties();
			String istr = String.valueOf(1024 * 1024 * 256);
			props.setProperty("maxAllowedPacket", istr);

			String cstr = ConnectionString;

			System.out.println("MySQL.getConnection():  Cstr=" + cstr);

			connection = (Connection) DriverManager.getConnection(ConnectionString, props);

			System.out.println("Connection succeeded...");
			return connection;
		} catch (Exception ex) {
			System.out.println("SQLException: " + ex.getMessage());
		}
		return null;
	}

	public void closeConnection() {
		try {
			connection.close();
			connection = null;
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
		}
		connection = null;
	}

	public void emptyAllTables() {
		for (int i = 0; i < AllDBTableNames.length; i++) {
			String tname = DBSchemaName + "." + AllDBTableNames[i];
			emptyTable(tname);
		}
	}

	///// From Desktop DB Manager /////
	public static EvaluationWorkbench initializeOldWorkbench() throws Exception {
		EvaluationWorkbench wb = new EvaluationWorkbench();
		new StartupParameters(wb, true, true);
		wb.reinitializeTypeSystem();
		AnnotationAnalysis aa = new AnnotationAnalysis(wb);
		aa.initializeAnnotators();
		return wb;
	}

}
