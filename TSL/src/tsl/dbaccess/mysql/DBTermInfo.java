package tsl.dbaccess.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import tsl.utilities.VUtils;

public class DBTermInfo {
	private String term = null;
	private Vector<DBTermIndex> termIndexes = null;
	private double weight = 0d;

	public DBTermInfo(String term, Vector<DBTermIndex> termIndexes) {
		this.term = term;
		this.termIndexes = termIndexes;
	}

	public static Vector<DBTermInfo> gatherAllTermInfos() {
		Vector<DBTermInfo> termInfos = null;
		try {
			String sql = "select term, indexes from TERMINDEXES";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) MySQL
					.getMySQL().getConnection().prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				String term = rs.getString(1);
				String istr = rs.getString(2);
				Vector<DBTermIndex> tis = DBTermIndex
						.extractDBTermIndexesFromIndexString(term, istr);
				DBTermInfo tinfo = new DBTermInfo(term, tis);
				termInfos = VUtils.add(termInfos, tinfo);
			}
			if (termInfos != null) {
				double minCount = 1000000d;
				for (DBTermInfo ti : termInfos) {
					if (ti.termIndexes != null && ti.termIndexes.size() < minCount) {
						minCount = ti.termIndexes.size();
					}
				}
				for (DBTermInfo ti : termInfos) {
					if (DBWordPattern.isWordAttributeValue(ti.term)) {
						ti.weight = 1d;
					} else if (ti.termIndexes != null) {
						ti.weight = minCount / ti.termIndexes.size();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return termInfos;
	}

	public String getTerm() {
		return term;
	}

	public Vector<DBTermIndex> getTermIndexes() {
		return termIndexes;
	}

	public double getWeight() {
		return weight;
	}

}
