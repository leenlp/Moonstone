package tsl.dbaccess.mysql;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import com.mysql.jdbc.Connection;
import tsl.documentanalysis.document.Document;
import tsl.expression.term.Term;
import tsl.utilities.FUtils;
import tsl.utilities.VUtils;

public class MySQLAPI {

	private static com.mysql.jdbc.Connection connection = null;
	private static String ConnectionString = "jdbc:mysql://localhost/ieviz?user=root&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";
	private static String[] AllDBTableNames = new String[] { "AnnotationAttributes", "AnnotationRelations",
			"AnnotationRuns", "Annotations", "AnnotationTools", "AnnotationTypeAttributes",
			"AnnotationTypeAttributeValues", "AnnotationTypeRelations", "AnnotationTypes", "Annotators", "Corpora",
			"Documents", "Typesystem" };
	private static String DBSchemaName = "ieviz";

	public MySQLAPI(String msg) {
	}

	public String readDocumentText(int docid) {
		String text = null;
		try {
			Connection c = getConnection();
			String sql = "select text from ieviz.Documents where documentID = ?";
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

	public Vector<Document> readCorpusDocuments(String corpusName) {
		Vector<Document> documents = null;
		int corpusID = this.getIDByField("ieviz.Corpora", "corpusID", "corpusName", corpusName);
		if (corpusID >= 0) {
			try {
				Connection c = this.getConnection();
				String sql = "select documentID from ieviz.Documents where corpID = ?";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setInt(1, corpusID);
				ps.execute();
				ResultSet rs = ps.executeQuery();
				Vector<Integer> docids = new Vector(0);
				while (rs.next()) {
					int id = rs.getInt(1);
					docids.add(id);
				}
				rs.close();
				ps.close();
				for (int docid : docids) {
					sql = "select name, text from ieviz.Documents where documentID = ?";
					ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
					ps.setInt(1, docid);
					ps.executeQuery();
					rs = ps.executeQuery();
					if (rs.next()) {
						String name = rs.getString(1);
						String text = rs.getString(2);
						Document doc = new Document(docid, null, name, null, text);
						documents = VUtils.add(documents, doc);
					}
					rs.close();
					ps.close();

				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return documents;
	}

	public boolean storeDocumentsFromDirectory(String cname, String dname) {
		Vector<File> files = FUtils.readFilesFromDirectory(dname);
		if (files != null) {
			int cid = this.storeCorpus(cname, dname, "<AutoStore>");
			for (File file : files) {
				int did = this.getIDByFields("ieviz.Documents", "documentID", new String[] { "name", "corpID" },
						new Object[] { file.getName(), cid });
				if (did < 0) {
					String text = FUtils.readFile(file.getAbsolutePath());
					String url = "file:/" + file.getAbsolutePath();
					Document doc = new Document(-1, url, file.getName(), file.getAbsolutePath(), text);
					doc.setCorpusID(cid);
					int docid = this.storeDocument(doc);
					doc.setDocumentID(docid);
				}
			}
			return true;
		}
		return false;
	}

	public int storeDocument(Document document) {
		int did = -1;
		try {
			did = this.getIDByFields("ieviz.Documents", "documentID", new String[] { "name", "corpID" },
					new Object[] { document.getName(), document.getCorpusID() });
			if (did < 0) {
				Connection c = getConnection();
				String sql = "insert into ieviz.documents (location, corpID, name, text) values (?, ?, ?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, document.getUrl());
				ps.setInt(2, document.getCorpusID());
				ps.setString(3, document.getName());
				ps.setString(4, document.getText());
				ps.execute();
				ps.close();
				did = this.getIDByFields("ieviz.Documents", "documentID", new String[] { "name", "corpID" },
						new Object[] { document.getName(), document.getCorpusID() });
				document.setDocumentID(did);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return did;
	}

	public int storeCorpus(String corpusName, String location, String description) {
		int cid = this.getIDByField("Corpora", "corpusID", "corpusName", corpusName);
		try {
			if (cid < 0) {
				Connection c = getConnection();
				String sql = "insert into ieviz.Corpora (corpusName, location, description) values (?, ?, ?)";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setString(1, corpusName);
				ps.setString(2, location);
				ps.setString(3, description);
				ps.execute();
				ps.close();
				cid = this.getIDByField("Corpora", "corpusID", "corpusName", corpusName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cid;
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

}
