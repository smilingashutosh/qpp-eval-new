package org.qpp;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.evaluator.RetrievedResults;
import java.lang.*;
import org.trec.TRECQuery;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.DoubleStream;

public class OddsRatioSpecificity extends BaseIDFSpecificity {
    float p;

    public OddsRatioSpecificity(IndexSearcher searcher, float p) {
        super(searcher);
        this.p = p;
    }

    @Override
    public double computeSpecificity(Query q, RetrievedResults retInfo, TopDocs topDocs, int k) {
    	k = Math.min(k,topDocs.scoreDocs.length);    	

//        int topK = (int)(p*k);
        int topK = (int)Math.max((p*k),1);
//        System.out.println("topk:"+topK);
//        if (topK<1) {
//        	topK =1;
//        }
//        if (topK > k/2) {
//        	 topK = (int) Math.floor(k/2);
//        }
        int bottomK = topK;
        if (bottomK ==1) {
        	bottomK = 2; //patch to handle 0 score at bottom as avgtopk/bottomAvg is infinity
        }
        double topdiff=0; ///new added
        double bottomdiff=0; 

        double[] rsvs = retInfo.getRSVs(k);
//        for (int temp1=0;temp1<topK;temp1++) {
//        	for (int temp2=k-bottomK;temp2<k;temp2++) {
////        		oddRatio+=(topDocs.scoreDocs[temp1].score + 0.000001)/(topDocs.scoreDocs[temp2].score + 0.000001 );
//        		topdiff+=(topDocs.scoreDocs[temp1].score - topDocs.scoreDocs[temp2].score);
//        		
//        	}
//        }
//        
//            for (int temp2=k-bottomK;temp2<k;temp2++) {
//        	for (int temp1=0;temp1<topK;temp1++) {
//        		bottomdiff+=(topDocs.scoreDocs[temp2].score - topDocs.scoreDocs[temp1].score);
//        		
//        	}
//        }
//        System.out.println("oddRatio value:" + oddRatio);
////        for(double score : rsvs){
//////            score = Math.log(1000+score);
////            score = (1000+score);
//        }
        double avgIDF = 0;
        try {
            avgIDF = Arrays.stream(idfs(q)).max().getAsDouble();
//            avgIDF = Arrays.stream(idfs(q)).average().getAsDouble();
        }
        catch (Exception ex) { ex.printStackTrace(); }

        double topAvg = Arrays.stream(rsvs).limit(topK).average().getAsDouble();
        double bottomAvg = Arrays.stream(rsvs).skip(k-bottomK).average().getAsDouble();
  

//        return topAvg/bottomAvg ; //Without average IDF factor

//        return (topdiff - bottomdiff) * avgIDF;
//        return (oddRatio) ;
        
//      return (topAvg - bottomAvg); //Without average IDF factor
//        System.out.println("bottomAvg is :" +bottomAvg);
//       System.out.println("qpp is :" +topAvg/bottomAvg * avgIDF);
//        return topAvg/bottomAvg * avgIDF;   // OR * maxIDF
       
//        return ((topAvg)/bottomAvg);			// OR
//        return ((topAvg/bottomAvg)) * avgIDF;
         return Math.log(1+(topAvg/bottomAvg)) * avgIDF; //log(1+OR)*maxIDF
    }

    @Override
    public String name() {
        return "odds-ratio";
    }
}
