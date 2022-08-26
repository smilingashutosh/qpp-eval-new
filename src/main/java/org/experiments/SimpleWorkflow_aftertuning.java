package org.experiments;

import org.apache.lucene.search.TopDocs;
import org.correlation.PearsonCorrelation;
import org.correlation.KendalCorrelation;
import org.evaluator.Metric;
import org.evaluator.RetrievedResults;
import org.qpp.*;
import org.trec.TRECQuery;

import java.util.Arrays;
import java.util.List;

public class SimpleWorkflow_aftertuning extends NQCCalibrationWorkflow {
    QPPMethod qppMethod;

    SimpleWorkflow_aftertuning(QPPMethod qppMethod, String queryFile, String resFile) throws Exception {
        super(queryFile, resFile);
        this.qppMethod = qppMethod;
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
//            System.out.println("query.id:"+query.id);
//            evaluatedMetricValues[i] = evaluator.compute(query.id, Metric.AP);
             evaluatedMetricValues[i] = evaluator.compute(query.id, Metric.nDCG);
//            System.out.println("evaluatedMetricValues[i]:" + evaluatedMetricValues[i]);
            
            rr = new RetrievedResults(query.id, topDocs); // this has to be set with the topdocs
            qppEstimates[i] = (float)qppMethod.computeSpecificity(
                    query.getLuceneQueryObj(), rr, topDocs, qppTopK);
//            System.out.println("qppEstimates[i]:" +qppEstimates[i]);
            i++;
        }
        corr_set[0] = new PearsonCorrelation().correlation(evaluatedMetricValues, qppEstimates); 
//        System.out.println("evaluatedMetricValues: "+evaluatedMetricValues[1]);
//        System.out.println("qppEstimates: "+qppEstimates[1]);
        System.out.println(String.format("Pearson's = %.4f", corr_set[0]));
        
        corr_set[1] = new KendalCorrelation().correlation(evaluatedMetricValues, qppEstimates);        
        System.out.println(String.format("Kendall's = %.4f", corr_set[1]));
        
        double sum = 0;  
      
        for (double temp : evaluatedMetricValues) {   
        	sum+=temp;  }     
        System.out.println("number of queries:" + evaluatedMetricValues.length);
        System.out.println("The Average MAP/nDCG value is : " + sum/(double)evaluatedMetricValues.length);  


        return corr_set;
    }

    public int calibrateTopK(List<TRECQuery> trainQueries) {
        final int[] qppTopKChoices = {50};
//         final int[] qppTopKChoices = {10, 20, 30, 40, 50};
//        final int[] qppTopKChoices = {10, 20, 30, 40, 50, 60, 70, 80, 100};
        int best_qppTopK = 0;
        double max_corr = -1;

        for (int qppTopK: qppTopKChoices) {
            System.out.println(String.format("Executing QPP Method %s (%d)", qppMethod.name(), qppTopK));
            double corr[] = computeCorr(trainQueries, qppMethod, qppTopK);
            if (corr[0] > max_corr) {
                max_corr = corr[0];                
                best_qppTopK = qppTopK;
            }
        }
        return best_qppTopK;
    }
    
    public float[]  calibrateParamNQC(List<TRECQuery> trainQueries) {

      final int[] qppTopKChoices = {50};
//        final int[] qppTopKChoices = {10, 20, 30, 40, 50};
      final float[] alphaChoices = {/*0.25f,0.5f,1.0f,1.5f,*/2.0f};
      final float[] betaChoices = {0.25f/*,0.5f,1.0f,1.5f,2.0f*/};
      final float[] gammaChoices = {/*0.25f,0.5f,1.0f,1.5f,*/2.0f};
      int best_qppTopK = 0;
      float[] best_choice = new float[4]; // best (alpha, beta, gamma)
      double max_corr = -1;

      for (int qppTopK: qppTopKChoices) {
    	  
    	  for (float alpha: alphaChoices) {
    		  for (float beta: betaChoices) {
    			  for (float gamma: gammaChoices) {
                      qppMethod = new NQCSpecificityCalibrated(Settings.getSearcher(), alpha, beta, gamma);
                      System.out.println(String.format("Executing NQC (%.2f, %.2f, %.2f,%d)", alpha, beta, gamma,qppTopK));
//			          System.out.println(String.format("Executing QPP Method %s (%d)", qppMethod.name(), qppTopK));
			          double corr[] = computeCorr(trainQueries, qppMethod, qppTopK);
			          if (corr[0] > max_corr) {
			              max_corr = corr[0];  
	                      best_choice[0] = alpha;
	                      best_choice[1] = beta;
	                      best_choice[2] = gamma;
	                      best_choice[3]=qppTopK;
			              best_qppTopK = qppTopK;
          }
    			  }
    		  }	
    	  }
      }
      return best_choice ;
  }

    public double[] epoch_new() {
//        final float TRAIN_RATIO = 0.5f;
        final float TRAIN_RATIO = 1.0f;
        int tuned_topk = 0;
        double[] corr= new double[2];
        TrainTestInfo trainTestInfo = new TrainTestInfo(queries, TRAIN_RATIO);
       
        
        if (!qppMethod.name().equals("avgidf")) { // nothing to tune for pre-ret methods!
        	if (qppMethod.name().equals("nqc_generic")) {
                float[] tuned_params = calibrateParamNQC(trainTestInfo.getTrain());
                System.out.println(String.format("Optimal params alpha, beta, gamma,topk (%.2f, %.2f, %.2f,%.2f)", tuned_params[0], tuned_params[1], tuned_params[2],tuned_params[3]));
                QPPMethod qppMethod = new NQCSpecificityCalibrated(
                                    Settings.getSearcher(),
                                    tuned_params[0], tuned_params[1], tuned_params[2]);

//                return computeCorrelation(queries, qppMethod);
                corr = computeCorr(trainTestInfo.getTrain(), qppMethod, (int) tuned_params[3]);
        		
        	}
        		
        	else {
        		tuned_topk = calibrateTopK(trainTestInfo.getTrain());
        		System.out.println("Optimal top-k = " + tuned_topk);
//        	    corr = computeCorr(trainTestInfo.getTest(), qppMethod, tuned_topk);
        	    corr = computeCorr(trainTestInfo.getTrain(), qppMethod, tuned_topk); //changes for thesis report
        	}
        }
        
 
        System.out.println("Test set Pearson correlation = " + corr[0]);
        System.out.println("Test set Kendal correlation = " + corr[1]);       
        return corr;
    }
    
    public void avgAcrossEpochs() {
        final int NUM_EPOCHS = 1; // change it to 30!

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
//        final String queryFile = "data/pass_2019.queries";
//        final String queryFile = "data/pass_2020.queries";
       
  
        
//        monoT5 and duoT5 resfiles and pairwise probablity file      
//        final String resFile = "msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm";
//       final String resFile = "msmarco_runs/10bm25.1000.mt5.50.dt5/res.bm25.1000.mt5.50.dt5.50.mmnorm";

//        final String pairScoreFile = "msmarco_runs/10bm25.1000.mt5.50.dt5/duoT5.50.score.softmax.pairwise.doc.tsv"; //duot5 pairwise score
//       final String pairScoreFile = "msmarco_runs/10bm25.1000.mt5.50.dt5//duoT5.50.score.logsoftmax.pairwise.doc.tsv"; //duot5 pairwise score
       
//     01 Colbert rerank
//       final String resFile = "msmarco_runs/01bm25.colbert/colbert.reranked.res.50.mmnorm";
//     final String resFile = "msmarco_runs/01bm25.colbert/colbert.reranked.res.50.mmnorm.duot5.50";
//       final String pairScoreFile = "msmarco_runs/01bm25.colbert/duoT5.50.score.softmax.pairwise.doc.tsv";
       
//       02 BM25 monot5
//       final String resFile = "msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm";
//     final String resFile = "msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm.duot5.50";
//     final String pairScoreFile = "msmarco_runs/10bm25.1000.mt5.50.dt5/duoT5.50.score.softmax.pairwise.doc.tsv"; //duot5 pairwise score
       
////     03 BM25 docT5query BERT
//       final String resFile = "msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.50.mmnorm";
//     final String resFile = "msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.50.mmnorm.duot5.50";
//       final String pairScoreFile = "msmarco_runs/03BM25+docT5query+BERT/duoT5.50.score.softmax.pairwise.doc.tsv"; 
       
//       04 BM25 docT5 ColBERT
//          final String resFile = "msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.50.mmnorm";
//     final String resFile = "msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.50.mmnorm.duot5.50";
//       final String pairScoreFile = "msmarco_runs/04BM25+docT5query+ColBERT/duoT5.50.score.softmax.pairwise.doc.tsv"; 
       

       
//     05 BM25 Deepct BERT
//       final String resFile = "msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.50.mmnorm";
//        final String resFile = "msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.50.mmnorm.duot5.50";
//       final String pairScoreFile = "msmarco_runs/05BM25+DeepCT+BERT/duoT5.50.score.softmax.pairwise.doc.tsv";
       
////     06 BM25 Deepct Colbert
//         final String resFile = "msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.50.mmnorm";
//     final String resFile = "msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.50.mmnorm.duot5.50";
//       final String pairScoreFile = "msmarco_runs/06BM25+DeepCT+ColBERT/duoT5.50.score.softmax.pairwise.doc.tsv";
       
//     07 Colbert e2e
//       final String resFile = "msmarco_runs/07ColBERT.E2E/E2E.res.50.mmnorm";
//      final String resFile = "msmarco_runs/07ColBERT.E2E/E2E.res.50.mmnorm.duot5.50";
//       final String pairScoreFile = "msmarco_runs/07ColBERT.E2E/duoT5.50.score.softmax.pairwise.doc.tsv";

//     08 colberte2e bertqe
//        final String resFile = "msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.50.mmnorm";
//       final String resFile = "msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.50.mmnorm.duot5.50";
//        final String pairScoreFile = "msmarco_runs/08ColBERT.E2E+BERT-QE/duoT5.50.score.softmax.pairwise.doc.tsv";

//     09 ANCEresfiles and pairwise probability file 
//        final String resFile = "msmarco_runs/09ANCE/ANCE.res.50.mmnorm";
//     final String resFile = "msmarco_runs/09ANCE/ANCE.res.50.mmnorm.duot5.50";
//        final String pairScoreFile = "msmarco_runs/09ANCE/duoT5.50.score.softmax.pairwise.doc.tsv"; 
       
//     11 BM25 
//        final String resFile = "msmarco_runs/11bm25.50/res.bm25.50";
//       final String resFile = "msmarco_runs/11bm25.50/res.bm25.50.duot5.50";
//       final String pairScoreFile = "msmarco_runs/11bm25.50/duoT5.50.score.softmax.pairwise.doc.tsv";  
//		12 BM25 + BERT
        final String resFile = "msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm";
        

        Settings.init("msmarco.properties");

        try {
            QPPMethod[] qppMethods = {
                     new NQCSpecificity(Settings.getSearcher()),
//                    new WIGSpecificity(Settings.getSearcher()),
//                    new AvgIDFSpecificity(Settings.getSearcher()),
//                     new UEFSpecificity(new NQCSpecificity(Settings.getSearcher())),
//                       new NQCSpecificityCalibrated(Settings.getSearcher(),0.25f,0.25f,0.25f),
//                    
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.2f), // 20% as top and bottom
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.3f), // 30% as top and bottom
//                      new OddsRatioSpecificity(Settings.getSearcher(), 0.4f), // 40% as top and bottom
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.5f), // 50% as top and bottom
//                  new ScoreRatioSpecificity(Settings.getSearcher(), 0.2f), // 20% as top and bottom
            };

            for (QPPMethod qppMethod: qppMethods) {
                System.out.println("Getting results for QPP method " + qppMethod.name());
                SimpleWorkflow_aftertuning gridWorkflow = new SimpleWorkflow_aftertuning(qppMethod, queryFile, resFile);
                gridWorkflow.avgAcrossEpochs();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
