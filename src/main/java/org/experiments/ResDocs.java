package org.experiments;

import org.trec.TRECQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ResDocs {
	  String qid;
	  String docid;
	  String rank;
	  double score;

  public ResDocs(String qid, String docid, String rank, String score) {
//	  public ResDocs( String docid, String rank, String score) {
		  this.qid = qid;
		  this.docid = docid;
		  this.rank = rank;
		  this.score = Double.parseDouble(score);
		  
	     
	  }
  
  public String getdocid() {
      return docid;
  }

  public String getrank() {
      return rank;
  }
  public double getscore() {
      return score;
  }
  public String getqid() {
      return qid;
  }

  
  @Override
  public String toString() {
	String strprn =  qid+":" + docid +":" + rank + ":" + score;
    return (strprn);
  }  
//

	}

