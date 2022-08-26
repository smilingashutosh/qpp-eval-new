package org.experiments;
import java.util.*;

import org.apache.lucene.search.TopDocs;
import org.correlation.PearsonCorrelation;
import org.correlation.KendalCorrelation;
import org.evaluator.Metric;
import org.evaluator.RetrievedResults;
import org.qpp.*;
import org.trec.TRECQuery;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridWorkflow_Pairwise extends NQCCalibrationWorkflow {
//    QPPMethod qppMethod;
    QPPMethodnew qppMethodnew;

//    GridWorkflow(QPPMethod qppMethod, String queryFile, String resFile) throws Exception {
//        super(queryFile, resFile);
//        this.qppMethod = qppMethod;
//    }
    GridWorkflow_Pairwise(QPPMethodnew qppMethodnew, String pairScoreFile, String queryFile, String resFile) throws Exception {
        super(pairScoreFile, queryFile, resFile);
        this.qppMethodnew = qppMethodnew;
    }
    

    
    public double[] computeCorr(List<TRECQuery> queries, QPPMethod qppMethod, int qppTopK) {
        int numQueries = queries.size();
        double[] qppEstimates = new double[numQueries]; // stores qpp estimates for the list of input queries
        double[] evaluatedMetricValues = new double[numQueries]; // stores GTs (AP/nDCG etc.) for the list of input queries
        int i = 0;
        double[] corr_set = new double[2];

        for (TRECQuery query : queries) {
            RetrievedResults rr = null;
            TopDocs topDocs = topDocsMap.get(query.id);
//            System.out.println("query.id:" +query.id);
            
            //pairwise res file changes
            List<ResDocs> rtupleResdoc = resDocsMap.get(query.id);
//            System.out.println("print rtuple_new size:" + rtuple_new.size());
//            List<String> lst1 = new ArrayList<String>();
            List<PairDocs> rtuplePairdoc = pairDocsMap.get(query.id);
            
//            List<String> lstPairDocsTop = new ArrayList<String>();
//            List<String> lstPairDocsBottom = new ArrayList<String>();
//            double scoreTopPair = 0;
//            double scoreBottomPair = 0;
//            
//            for (int temp1=0;temp1<(rtupleResdoc.size())*0.2;temp1++) {
//
//            	 for (int temp2= rtupleResdoc.size()-1;temp2>=(rtupleResdoc.size() -(rtupleResdoc.size())*0.2);temp2--) {
//            		 lstPairDocsTop.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//            	 }
//            }
//            
//            for (int temp1=rtupleResdoc.size()-1;temp1>=(rtupleResdoc.size() -(rtupleResdoc.size())*0.2);temp1--) {           	
//
//            	 for (int temp2=0;temp2<(rtupleResdoc.size())*0.2;temp2++) {
//            		 lstPairDocsBottom.add(rtupleResdoc.get(temp1).getdocid()+"_"+rtupleResdoc.get(temp2).getdocid());
//            	 }
//            }
//
//            for (int temp3=0;temp3<(rtuplePairdoc.size());temp3++) {
//        	
//            	if (lstPairDocsTop.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//            		scoreTopPair +=rtuplePairdoc.get(temp3).getscore();           		           		
//        		
//            	}   
//            	if (lstPairDocsBottom.contains(rtuplePairdoc.get(temp3).getdocid1_docid2())) {
//            		scoreBottomPair +=rtuplePairdoc.get(temp3).getscore();           		           		
//        		
//            	} 
//        	
//        	}
//            System.out.println("scoreTopPair:"+scoreTopPair);
//            System.out.println("scoreBottomPair:"+scoreBottomPair);
            
//            evaluatedMetricValues[i] = evaluator.compute(query.id, Metric.AP);
             evaluatedMetricValues[i] = evaluator.compute(query.id, Metric.nDCG);
            rr = new RetrievedResults(query.id, topDocs); // this has to be set with the topdocs
//            qppEstimates[i] = (float)qppMethod.computeSpecificity(
//                    query.getLuceneQueryObj(), rr, topDocs, qppTopK);
            qppEstimates[i] = (float)qppMethodnew.computeSpecificitynew(
                    query.getLuceneQueryObj(),rr, topDocs,rtupleResdoc,rtuplePairdoc,qppTopK);
            i++;
        }
        corr_set[0] = new PearsonCorrelation().correlation(evaluatedMetricValues, qppEstimates); 
        System.out.println(String.format("Pearson's = %.4f", corr_set[0]));
        
        corr_set[1] = new KendalCorrelation().correlation(evaluatedMetricValues, qppEstimates);        
        System.out.println(String.format("Kendall's = %.4f", corr_set[1]));
        double sum = 0;  
        
        for (double temp : evaluatedMetricValues) {   
        	sum+=temp;  }       	
        System.out.println("The MAP value is : " + sum/(double)evaluatedMetricValues.length);  

//      

        return corr_set;
    }

    public int calibrateTopK(List<TRECQuery> trainQueries) {
        final int[] qppTopKChoices = {10, 20, 30, 40, 50};
//        final int[] qppTopKChoices = {10, 20, 30, 40, 50, 60, 70, 80, 100};
        int best_qppTopK = 0;
        double max_corr = -1;

        for (int qppTopK: qppTopKChoices) {
            System.out.println(String.format("Executing QPP Method %s (%d)", qppMethodnew.name(), qppTopK));
            double corr[] = computeCorr(trainQueries, qppMethod, qppTopK);
            if (corr[0] > max_corr) {
                max_corr = corr[0];                
                best_qppTopK = qppTopK;
            }
//            if (corr[1] > max_corr) {
//                max_corr = corr[1];                
//                best_qppTopK = qppTopK;
//            }
        }
        return best_qppTopK;
    }

    public double[] epoch_new() {
        final float TRAIN_RATIO = 0.5f;
        TrainTestInfo trainTestInfo = new TrainTestInfo(queries, TRAIN_RATIO);
        int tuned_topk = calibrateTopK(trainTestInfo.getTrain());
        System.out.println("Optimal top-k = " + tuned_topk);
        double corr[] = computeCorr(trainTestInfo.getTest(), qppMethod, tuned_topk);
        System.out.println("Test set Pearson correlation = " + corr[0]);
        System.out.println("Test set Kendal correlation = " + corr[1]);       
        return corr;
    }
    
    public void avgAcrossEpochs() {
        final int NUM_EPOCHS = 30; // change it to 30!

        double[] avg = new double[2];
        for (int i=1; i <= NUM_EPOCHS; i++) {
            System.out.println("Random split: " + i);

            double[] avg_tmp = epoch_new();
            avg[0] += avg_tmp[0];
            avg[1] += avg_tmp[1];
        }

        System.out.println(String.format("Result over %d runs of tuned 50:50 splits P-r= %.4f and K_tau = %.4f", NUM_EPOCHS, avg[0]/NUM_EPOCHS,avg[1]/NUM_EPOCHS));
    }

    public static void main(String[] args) {
        final String queryFile = "data/trecdl1920.queries";
       
//       monoT5 and duoT5 resfiles and pairwise probablity file      
//         final String resFile = "msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm";
//        final String resFile = "msmarco_runs/10bm25.1000.mt5.50.dt5/res.bm25.1000.mt5.50.dt5.50.mmnorm";

//         final String pairScoreFile = "msmarco_runs/10bm25.1000.mt5.50.dt5/duoT5.50.score.softmax.pairwise.doc.tsv"; //duot5 pairwise score
//        final String pairScoreFile = "msmarco_runs/10bm25.1000.mt5.50.dt5//duoT5.50.score.logsoftmax.pairwise.doc.tsv"; //duot5 pairwise score
        
//      01 Colbert rerank
//        final String resFile = "msmarco_runs/01bm25.colbert/colbert.reranked.res.50.mmnorm";
//      final String resFile = "msmarco_runs/01bm25.colbert/colbert.reranked.res.50.mmnorm.duot5.50";
//        final String pairScoreFile = "msmarco_runs/01bm25.colbert/duoT5.50.score.softmax.pairwise.doc.tsv";
        
//        02 BM25 monot5
//      final String resFile = "msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm";
//         final String resFile = "msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm.duot5.50";
//         final String pairScoreFile = "msmarco_runs/02bm25.1000.mt5.50/duoT5.50.score.softmax.pairwise.doc.tsv"; //duot5 pairwise score
        
////      03 BM25 docT5query BERT
//        final String resFile = "msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.50.mmnorm";
//      final String resFile = "msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.50.mmnorm.duot5.50";
//        final String pairScoreFile = "msmarco_runs/03BM25+docT5query+BERT/duoT5.50.score.softmax.pairwise.doc.tsv"; 
        
//        04 BM25 docT5 ColBERT
//        final String resFile = "msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.50.mmnorm";
//      final String resFile = "msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.50.mmnorm..duot5.50";
//        final String pairScoreFile = "msmarco_runs/04BM25+docT5query+ColBERT/duoT5.50.score.softmax.pairwise.doc.tsv"; 
        

        
//      05 BM25 Deepct BERT
//        final String resFile = "msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.50.mmnorm";
//      final String resFile = "msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.50.mmnorm..duot5.50";
//        final String pairScoreFile = "msmarco_runs/05BM25+DeepCT+BERT/duoT5.50.score.softmax.pairwise.doc.tsv";
        
////      06 BM25 Deepct Colbert
//        final String resFile = "msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.50.mmnorm";
//      final String resFile = "msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.50.mmnorm.duot5.50";
//        final String pairScoreFile = "msmarco_runs/06BM25+DeepCT+ColBERT/duoT5.50.score.softmax.pairwise.doc.tsv";
        
//      07 Colbert e2e
//        final String resFile = "msmarco_runs/07ColBERT.E2E/E2E.res.50.mmnorm";
//      final String resFile = "msmarco_runs/07ColBERT.E2E/E2E.res.50.mmnorm.duot5.50";
//        final String pairScoreFile = "msmarco_runs/07ColBERT.E2E/duoT5.50.score.softmax.pairwise.doc.tsv";

//      08 colberte2e bertqe
//         final String resFile = "msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.50.mmnorm";
//      final String resFile = "msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.50.mmnorm.duot5.50";
//         final String pairScoreFile = "msmarco_runs/08ColBERT.E2E+BERT-QE/duoT5.50.score.softmax.pairwise.doc.tsv";

//      09 ANCEresfiles and pairwise probability file 
//         final String resFile = "msmarco_runs/09ANCE/ANCE.res.50.mmnorm";
//      final String resFile = "msmarco_runs/09ANCE/ANCE.res.50.mmnorm.duot5.50";
//         final String pairScoreFile = "msmarco_runs/09ANCE/duoT5.50.score.softmax.pairwise.doc.tsv"; 
        
//      11 BM25 
//          final String resFile = "msmarco_runs/11bm25.50/res.bm25.50";
//        final String resFile = "msmarco_runs/11bm25.50/res.bm25.50.duot5.50";
//        final String pairScoreFile = "msmarco_runs/11bm25.50/duoT5.50.score.softmax.pairwise.doc.tsv"; 
// 		12 BM25 + BERT 
      final String resFile = "msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm";
//      final String resFile = "msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm.duot5.50.mmnorm";
      final String pairScoreFile = "msmarco_runs/12BM25+BERT/duoT5.50.score.softmax.pairwise.doc.tsv";
      
        
        

        Settings.init("msmarco.properties");

        try {
            QPPMethodnew[] qppMethods = {
//                    new NQCSpecificity(Settings.getSearcher()),
//                    new WIGSpecificity(Settings.getSearcher()),
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.2f), // 20% as top and bottom
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.3f), // 30% as top and bottom
//                  new OddsRatioSpecificity(Settings.getSearcher(), 0.5f), // 50% as top and bottom
//            	  new ScoreRatioSpecificity(Settings.getSearcher(), 0.1f), // 20% as top and bottom
//                  new ScoreRatioSpecificity(Settings.getSearcher(), 0.2f), // 20% as top and bottom
//                   new ScoreRatioSpecificity(Settings.getSearcher(), 0.3f), // 30% as top and bottom
//                  new ScoreRatioSpecificity(Settings.getSearcher(), 0.4f), // 30% as top and bottom
                  new ScoreRatioSpecificity(Settings.getSearcher(), 0.5f), // 50% as top and bottom
//                  new ScoreRatioSpecificity2(Settings.getSearcher(), 0.5f), // 50% as top and bottom
            };

            for (QPPMethodnew qppMethod: qppMethods) {
                System.out.println("Getting results for QPP method " + qppMethod.name());
                GridWorkflow_Pairwise gridWorkflow = new GridWorkflow_Pairwise(qppMethod, pairScoreFile,queryFile, resFile);
                gridWorkflow.avgAcrossEpochs();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
