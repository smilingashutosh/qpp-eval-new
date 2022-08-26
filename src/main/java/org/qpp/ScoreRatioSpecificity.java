package org.qpp;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.TopDocs;
import org.evaluator.RetrievedResults;
import org.experiments.*;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.evaluator.RetrievedResults;
import java.lang.*;
import org.trec.TRECQuery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.DoubleStream;

import java.io.IOException;
import java.lang.*;
import org.trec.TRECQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;


 public class ScoreRatioSpecificity implements QPPMethodnew {
        IndexReader reader;
        IndexSearcher searcher;
        float p;


        public ScoreRatioSpecificity(IndexSearcher searcher,float p) {
            this.searcher = searcher;
            this.reader = searcher.getIndexReader();
            this.p = p;
        }

    public double computeSpecificitynew(Query q,RetrievedResults retInfo, TopDocs topDocs,List<ResDocs> rtupleResdoc, List<PairDocs> rtuplePairdoc, int k) {
    	k = Math.min(k,topDocs.scoreDocs.length); 	
//    	k = Math.min(k,1); 
        int topK = (int)Math.max((p*k),1);
        int bottomK = topK;
              
        List<String> lstPairDocsTop = new ArrayList<String>();
        List<String> lstPairDocsBottom = new ArrayList<String>();
        double scoreTopPair = 0;
        double scoreBottomPair = 0;
        double scoreTopPair2 = 0;
        double scoreBottomPair2 = 0;     
        double[] rsvs = retInfo.getRSVs(k);
        double avgIDF = 0;
	    try {
	        avgIDF = Arrays.stream(idfs(q)).max().getAsDouble();
	    }
	    catch (Exception ex) { ex.printStackTrace(); }


        for (int temp1=0;temp1<topK;temp1++) { // Creating the document pair D1_D2 where D1 from top docs and D2 from bottom docs

        	 for (int temp2= k-1;temp2>=(k -bottomK);temp2--) {
        		 lstPairDocsTop.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
        	 }
        }
      
        for (int temp1=k-1;temp1>=(k -bottomK);temp1--) {       // Creating the document pair D1_D2 where D1 from bottom docs and D1 from bottom docs      	

        	 for (int temp2=0;temp2<topK;temp2++) {
        		 lstPairDocsBottom.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
        	 }
        }
        

        for (int temp3=0;temp3<(rtuplePairdoc.size());temp3++) { // Aggregating the high inference and the low inference areas and calculating ratio
    	
        	if (lstPairDocsTop.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
        		
        		scoreTopPair +=rtuplePairdoc.get(temp3).getscore();   // Pij high      (p1) 
        		scoreTopPair2 = scoreTopPair2 + (1 - rtuplePairdoc.get(temp3).getscore()); //low  (1-p1)  		
     		      		
 		
        	}   
        	
        	if (lstPairDocsBottom.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
        		
        		scoreBottomPair +=rtuplePairdoc.get(temp3).getscore();   //Pji low               (p2) 
        		scoreBottomPair2 = scoreBottomPair2 + (1 - rtuplePairdoc.get(temp3).getscore()); //high    (1-p2)      
     		   		
        	} 
    	
    	}
       
               
         return  Math.log(1+((scoreBottomPair2/scoreTopPair2) + (scoreBottomPair/scoreTopPair)));
       
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    double[] idfs(Query q)  throws IOException {
        long N = reader.numDocs();
        Set<Term> qterms = new HashSet<>();

        //+++LUCENE_COMPATIBILITY: Sad there's no #ifdef like C!
        // 8.x CODE
        q.createWeight(searcher, ScoreMode.COMPLETE, 1).extractTerms(qterms);
        // 5.x CODE
        //q.createWeight(searcher, false).extractTerms(qterms);
        //---LUCENE_COMPATIBILITY
        double[] idfs = new double[qterms.size()];

        double aggregated_idf = 0;
        int i = 0;
        for (Term t: qterms) {
            int n = reader.docFreq(t);
            if (n==0) n = 1; // avoid 0 error!
            idfs[i++] = Math.log(N/(double)n);;
        }
        return idfs;
    }

    @Override
    public String name() {
        return "pairwise score-ratio";
    }
}