package socs.network.message;

import java.io.Serializable;

/**
 * Describes a link
 * 
 * @author kstricks
 *
 */
public class LinkDescription implements Serializable {
  public String linkID;
  public int portNum;
  public int tosMetrics;

  @Override
public String toString() {
    return linkID + ","  + portNum + "," + tosMetrics;
  }
}
