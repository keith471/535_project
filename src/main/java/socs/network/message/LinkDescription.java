package socs.network.message;

import java.io.Serializable;

/**
 * Describes a link
 * 
 * @author kstricks
 *
 */
public class LinkDescription implements Serializable {
  private String linkID;
  private int portNum;
  private int tosMetrics;
  
  // Constructor
  public LinkDescription(String linkID, int portNum, int tosMetrics) {
	  this.linkID = linkID;
	  this.portNum = portNum;
	  this.tosMetrics = tosMetrics;
  }

  @Override
public String toString() {
    return linkID + ","  + portNum + "," + tosMetrics;
  }

// Getters and Setters
public String getLinkID() {
	return linkID;
}

public void setLinkID(String linkID) {
	this.linkID = linkID;
}

public int getPortNum() {
	return portNum;
}

public void setPortNum(int portNum) {
	this.portNum = portNum;
}

public int getTosMetrics() {
	return tosMetrics;
}

public void setTosMetrics(int tosMetrics) {
	this.tosMetrics = tosMetrics;
}
}
