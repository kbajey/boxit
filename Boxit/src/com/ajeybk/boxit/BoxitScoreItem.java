package com.ajeybk.boxit;

public class BoxitScoreItem {
  public String getPlayer() {
    return mPlayer;
  }
  String mPlayer;
  
  public String getIndex(){
	  return mIdx;
  }
  String mIdx;
  
  public String getTokens(){
	  return mTokens;
  }
  String mTokens;
  public String getTime(){
	  return mTime;
  }
  String mTime;
  boolean highlight = false;
  
  public void setHighlight(){
	  highlight = true;
  }
  public boolean getHighlight(){
	  return highlight;
  }
  
  public BoxitScoreItem(String _idx, String _name, String _tkns, String _tm) {
	  mIdx = _idx;
	  mPlayer = _name;
	  mTokens = _tkns;
	  mTime = _tm;
  }
}
