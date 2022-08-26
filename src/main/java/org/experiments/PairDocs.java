package org.experiments;

import org.trec.TRECQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PairDocs {
	  String qid;
	  String docid1_docid2;
//	  String rank;
	  double score;

  public PairDocs(String qid, String docid1_docid2, String score) {
//	  public ResDocs( String docid, String rank, String score) {
		  this.qid = qid;
		  this.docid1_docid2 = docid1_docid2;
//		  this.rank = rank;
		  this.score = Double.parseDouble(score);
		  
	     
	  }
  
  public String getdocid1_docid2() {
      return docid1_docid2;
  }

//  public String getrank() {
//      return rank;
//  }
  public double getscore() {
      return score;
  }
  public String getqid() {
      return qid;
  }

  
  @Override
  public String toString() {
	String strprn =  qid+":" + docid1_docid2 + ":" + score;
    return (strprn);
  }  
//

	}

