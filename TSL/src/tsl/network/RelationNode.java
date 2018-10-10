package tsl.network;

import tsl.dbaccess.mysql.DBTermInstance;

public class RelationNode {
	
	private int termInstanceID = 0;
	private DBTermInstance dbTermInstance = null;
	// private Jung Node
	
	public RelationNode(int termID) {
		this.termInstanceID = termID;
	}

}
