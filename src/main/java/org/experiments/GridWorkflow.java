package org.experiments;

import org.apache.lucene.search.TopDocs;
import org.correlation.PearsonCorrelation;
import org.correlation.KendalCorrelation;
import org.evaluator.Metric;
import org.evaluator.RetrievedResults;
import org.qpp.*;
import org.trec.TRECQuery;

import java.util.List;

public class GridWorkflow extends NQCCalibrationWorkflow {
    QPPMethod qppMethod;

    GridWorkflow(QPPMethod qppMethod, String queryFile, String resFile) throws Exception {
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
//	      System.out.println("evaluatedMetricValues: "+evaluatedMetricValues[1]);
//	      System.out.println("qppEstimates: "+qppEstimates[1]);
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
//        final int[] qppTopKChoices = {10};
         final int[] qppTopKChoices = {10, 20, 30, 40, 50};
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
//      final int[] qppTopKChoices = {10, 20, 30, 40, 50};
      final int[] qppTopKChoices = {20};
      final float[] alphaChoices = {0.25f,0.5f,1.0f,1.5f,2.0f};
      final float[] betaChoices = {0.25f,0.5f,1.0f,1.5f,2.0f};
      final float[] gammaChoices = {0.25f,0.5f,1.0f,1.5f,2.0f};
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
        final float TRAIN_RATIO = 0.5f;
//        final float TRAIN_RATIO = 1.0f; //for thesis report
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
                corr = computeCorr(trainTestInfo.getTest(), qppMethod, (int) tuned_params[3]);  
//                corr = computeCorr(trainTestInfo.getTrain(), qppMethod, (int) tuned_params[3]); //for thesis report
        		
        	}
        		
        	else {
        		tuned_topk = calibrateTopK(trainTestInfo.getTrain());
        		System.out.println("Optimal top-k = " + tuned_topk);
        	    corr = computeCorr(trainTestInfo.getTest(), qppMethod, tuned_topk);
//        	    corr = computeCorr(trainTestInfo.getTrain(), qppMethod, tuned_topk); //changes for thesis report
        	}
        }
        
 
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
//        final String queryFile = "data/pass_2019.queries";  // for generalization hypertuning
       
       
//        final String resFile = "msmarco_runs/01bm25.colbert/colbert.reranked.res.50.mmnorm";
//          final String resFile = "msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm";
     

//\         final String resFile = "msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.50.mmnorm"; //0.40933962997600987
  	 
       
//        final String resFile = "msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.50.mmnorm";
//        final String resFile = "msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.50.mmnorm";
        
//      final String resFile = "msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.50.mmnorm";
//         final String resFile = "msmarco_runs/07ColBERT.E2E/E2E.res.50.mmnorm";
//         final String resFile = "msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.50.mmnorm";
//        final String resFile = "msmarco_runs/09ANCE/ANCE.res.50.mmnorm";
//      final String resFile = "msmarco_runs/10bm25.1000.mt5.50.dt5/res.bm25.1000.mt5.50.dt5.50.mmnorm";
//         final String resFile = "msmarco_runs/11bm25.50/res.bm25.50"; //0.40933962997600987
//         final String resFile = "msmarco_runs/11bm25.50/res.bm25.50.mmnorm"; 
         
         final String resFile = "msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm";
//       final String resFile = "msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm.duot5.50";

        

        Settings.init("msmarco.properties");

        try {
            QPPMethod[] qppMethods = {
//                      new NQCSpecificity(Settings.getSearcher()),
//                    new WIGSpecificity(Settings.getSearcher()),
//                    new AvgIDFSpecificity(Settings.getSearcher()),
                      new UEFSpecificity(new NQCSpecificity(Settings.getSearcher())),
//                      new NQCSpecificityCalibrated(Settings.getSearcher(),0.25f,0.25f,0.25f),                    
//                     new OddsRatioSpecificity(Settings.getSearcher(), 0.2f), //0.2772 and K_tau = 0.0919  log:0.2480 and K_tau = 0.0941
//                       new OddsRatioSpecificity(Settings.getSearcher(), 0.3f), // 0.2883 and K_tau = 0.0963  log0.2589 and K_tau = 0.0897
//                       new OddsRatioSpecificity(Settings.getSearcher(), 0.4f), //0.3093 and K_tau = 0.0963 log 0.2738 and K_tau = 0.0986
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.5f), // 0.3144 and K_tau = 0.1008 0.2762 and K_tau = 0.1008
//                    new ScoreRatioSpecificity(Settings.getSearcher(), 0.5f), // 50% as top and bottom
//                  new ScoreRatioSpecificity2(Settings.getSearcher(), 0.5f), // 50% as top and bottom
            };

            for (QPPMethod qppMethod: qppMethods) {
                System.out.println("Getting results for QPP method " + qppMethod.name());
                GridWorkflow gridWorkflow = new GridWorkflow(qppMethod, queryFile, resFile);
                gridWorkflow.avgAcrossEpochs();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
