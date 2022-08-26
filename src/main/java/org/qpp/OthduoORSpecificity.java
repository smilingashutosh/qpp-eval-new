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


 public class OthduoORSpecificity implements QPPMethodnew {
        IndexReader reader;
        IndexSearcher searcher;
        float p;


        public OthduoORSpecificity(IndexSearcher searcher,float p) {
            this.searcher = searcher;
            this.reader = searcher.getIndexReader();
            this.p = p;
        }



    public double computeSpecificitynew(Query q,RetrievedResults retInfo, TopDocs topDocs,List<ResDocs> rtupleResdoc, List<PairDocs> rtuplePairdoc, int k) {
    	k = Math.min(k,topDocs.scoreDocs.length); 	
//    	k = Math.min(k,1); 
        int topK = (int)Math.max((p*k),1);
        int bottomK = topK;
        
    	List<String> lstPairDocsTopTR = new ArrayList<String>();
        List<String> lstPairDocsBottomBL = new ArrayList<String>();
        double scoreTopPairTR = 0;
        double scoreBottomPairBL = 0;
        
    	List<String> lstPairDocsTopTLU = new ArrayList<String>();
        List<String> lstPairDocsBottomTLB = new ArrayList<String>();
        double scoreTopPairTLU = 0;
        double scoreBottomPairTLB = 0;
        
        
    	List<String> lstPairDocsTopBRU = new ArrayList<String>();
        List<String> lstPairDocsBottomBRB = new ArrayList<String>();
        double scoreTopPairBRU = 0;
        double scoreBottomPairBRB = 0;
        
        
        double scoreTopPair = 0;
        double scoreBottomPair = 0;
        double scoreTopPair2 = 0;
        double scoreBottomPair2 = 0;
        double numofRatio = 0;
        double denofRatio = 0;
        double pairdocRatio =0;
        
        
        double[] rsvs = retInfo.getRSVs(k);

        double avgIDF = 0;
	    try {
	        avgIDF = Arrays.stream(idfs(q)).max().getAsDouble();
	    }
	    catch (Exception ex) { ex.printStackTrace(); }
	    
////	    Top right & bottom left quadrant scores
//	    
//        for (int temp1=0;temp1<topK;temp1++) {
//
//       	 for (int temp2= k-1;temp2>=(k -bottomK);temp2--) {
//       		 lstPairDocsTopTR.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//       	 }
//       }
//       
//       for (int temp1=k-1;temp1>=(k -bottomK);temp1--) {           	
//
//       	 for (int temp2=0;temp2<topK;temp2++) {
//       		 lstPairDocsBottomBL.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//       	 }
//       }
//       
//       for (int temp3=0;temp3<(rtuplePairdoc.size());temp3++) {
//       	
//       	if (lstPairDocsTopTR.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//       		
//       		scoreTopPairTR +=rtuplePairdoc.get(temp3).getscore();   
//   		
//       	}   
//       	if (lstPairDocsBottomBL.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//       		
//       		scoreBottomPairBL +=rtuplePairdoc.get(temp3).getscore();   //Pji low               (p2) 
//     		
//   		
//       	} 
//   	
//   	}
//
//// Top left quadrant divided into 2 parts (upper and lower)
//        for (int temp1=0;temp1<topK;temp1++) {
//
//        	 for (int temp2= temp1+1;temp2<topK;temp2++) {
//        		 lstPairDocsTopTLU.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//        	 }
//        }
//        
//        for (int temp1=topK-1;temp1>0;temp1--) {           	
//
//        	 for (int temp2=temp1-1;temp2>=0;temp2--) {
//        		 lstPairDocsBottomTLB.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//        	 }
//        }
//        
////        System.out.println("rtuplePairdoc.size():"+rtuplePairdoc.size());
//        for (int temp3=0;temp3<(rtuplePairdoc.size());temp3++) {
//    	
//        	if (lstPairDocsTopTLU.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//        		
//        		scoreTopPairTLU +=rtuplePairdoc.get(temp3).getscore();   // Pij high      (p1) 
//	
//    		      		
//    		
//        	}   
//        	if (lstPairDocsBottomTLB.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//        		
//        		scoreBottomPairTLB +=rtuplePairdoc.get(temp3).getscore();   //Pji low               (p2) 
//       		
//    		
//        	} 
//    	
//    	}
//        
//     // Bottom Right quadrant divided into 2 parts (upper and lower)
//        for (int temp1=(k-bottomK);temp1<k;temp1++) {
//
//        	 for (int temp2= temp1+1;temp2<k;temp2++) {
//        		 lstPairDocsTopBRU.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//        	 }
//        }
//        
//        for (int temp1=(k-1);temp1>(k-bottomK);temp1--) {           	
//
//        	 for (int temp2=temp1-1;temp2>=(k-bottomK);temp2--) {
//        		 lstPairDocsBottomBRB.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//        	 }
//        }
//        
////        System.out.println("rtuplePairdoc.size():"+rtuplePairdoc.size());
//        for (int temp3=0;temp3<(rtuplePairdoc.size());temp3++) {
//    	
//        	if (lstPairDocsTopBRU.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//        		
//        		scoreTopPairBRU +=rtuplePairdoc.get(temp3).getscore();   // Pij high      (p1) 
//	
//    		      		
//    		
//        	}   
//        	if (lstPairDocsBottomBRB.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//        		
//        		scoreBottomPairBRB +=rtuplePairdoc.get(temp3).getscore();   //Pji low               (p2) 
//       		
//    		
//        	} 
//    	
//    	}
//        

     // (upper and lower)
             for (int temp1=0;temp1<topK;temp1++) {

             	 for (int temp2= temp1+1;temp2<topK;temp2++) {
             		 
             		 lstPairDocsTopTLU.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
             		String  str1 = rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid();
//             		System.out.println("str1:" + str1);
             		 lstPairDocsBottomTLB.add(rtupleResdoc.get(temp2).getdocid()+"_"+rtupleResdoc.get(temp1).getdocid());
             		 String str2 = rtupleResdoc.get(temp2).getdocid()+"_"+rtupleResdoc.get(temp1).getdocid();
//             		System.out.println("str2:" + str2);
             		for (int temp3=0;temp3<(rtuplePairdoc.size());temp3++) {
//             			if (rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid()==rtuplePairdoc.get(temp3).getdocid1_docid2()) {ut
             			String str3 = rtuplePairdoc.get(temp3).getdocid1_docid2();
//             			System.out.println("str3:" + str3);
//             			System.out.println("str1:" + str1);
//             			System.out.println("rtuplePairdoc.get(temp3).getdocid1_docid2():" + rtuplePairdoc.get(temp3).getdocid1_docid2());
             			if (str1.equals(str3)) {
//             				System.out.println("inside numofratio");
//             				System.out.println("str1:" + str1);
             				numofRatio =rtuplePairdoc.get(temp3).getscore();
             				   			}
             			if (str2.equals(str3)) {
//             				System.out.println("inside denofratio");
//             				System.out.println("str2:" + str2);
             				denofRatio =rtuplePairdoc.get(temp3).getscore();
             				   			}
             			}
             		pairdocRatio+=pairdocRatio + (numofRatio/denofRatio);
             				
             	 }
             }
             
                
      
//           System.out.println("scoreTopPair:"+scoreTopPair);
////           System.out.println("scoreBottomPair2:"+scoreBottomPair2);
//           System.out.println("scoreBottomPair:"+scoreBottomPair);
//           System.out.println("scoreTopPair2:"+scoreTopPair2);
        
        
        
//         return (scoreTopPairTR/scoreBottomPairBL) + (scoreTopPairTLU/scoreBottomPairTLB) +(scoreBottomPairTLB/scoreBottomPairBRB); //P-r= 0.1580 and K_tau = 0.1552
         
         return pairdocRatio; /*+ Math.log(scoreTopPairBRU/scoreBottomPairBRB)*/
         
      //  best  Math.log(1 + (scoreTopPair/scoreBottomPair) + (scoreBottomPair2/scoreTopPair2)) for nDCG on softmax prob
   //      return   Math.log(1 + (scoreTopPair/scoreBottomPair)); //P-r= 0.2357 and K_tau = 0.1616
//         return   (scoreTopPair-scoreBottomPair) + (scoreBottomPair2 - scoreTopPair2); //P-r= 0.2611 and K_tau = 0.1901

//        return  ((scoreTopPair+scoreBottomPair2)-(scoreBottomPair+scoreTopPair2))*avgIDF;  //        
//             return  Math.log(1 + (scoreTopPair/scoreBottomPair) + (scoreBottomPair2/scoreTopPair2));  //P-r= 0.3788 and K_tau = 0.2344           
//            return  Math.log(1 + (scoreTopPair/scoreBottomPair) + (scoreBottomPair2/scoreTopPair2)) * avgIDF;  // P-r= 0.3473 and K_tau = 0.2371
//            return  avgIDF;  // P-r= 0.2542 and K_tau = 0.1803
            
//           return  ((scoreTopPair/scoreBottomPair) + (scoreBottomPair2/scoreTopPair2)) * avgIDF; //P-r= 0.2776 and K_tau = 0.2143
           
           
            
//        return  ((scoreTopPair + scoreBottomPair2)/(scoreBottomPair + scoreTopPair2));  //P-r= 0.1991 and K_tau = 0.1646
//           return  Math.log(((scoreTopPair + scoreBottomPair2)/(scoreBottomPair + scoreTopPair2))); //P-r= 0.2960 and K_tau = 0.1841
//          return  Math.log(1+ ((scoreTopPair + scoreBottomPair2)/(scoreBottomPair + scoreTopPair2))); //P-r= 0.2947 and K_tau = 0.1845
//          return  Math.log((scoreTopPair + scoreBottomPair2)/(scoreBottomPair + scoreTopPair2))*avgIDF;  //P-r= 0.2880 and K_tau = 0.2038
        
         


        
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