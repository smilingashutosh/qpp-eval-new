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
            evaluatedMetricValues[i] = evaluator.compute(query.id, Metric.AP);
            rr = new RetrievedResults(query.id, topDocs); // this has to be set with the topdocs
            qppEstimates[i] = (float)qppMethod.computeSpecificity(
                    query.getLuceneQueryObj(), rr, topDocs, qppTopK);
            i++;
        }
        corr_set[0] = new PearsonCorrelation().correlation(evaluatedMetricValues, qppEstimates); 
        System.out.println(String.format("Pearson's = %.4f", corr_set[0]));
        
        corr_set[1] = new KendalCorrelation().correlation(evaluatedMetricValues, qppEstimates);        
        System.out.println(String.format("Kendall's = %.4f", corr_set[1]));
//      

        return corr_set;
    }

    public int calibrateTopK(List<TRECQuery> trainQueries) {
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
       
        final String resFile = "msmarco_runs/BM25.20192020.res";
//        final String resFile = "msmarco_runs/colbert.reranked.res.trec";
//        final String resFile = "msmarco_runs/trecdl.monot5.rr.pos-scores.res";
//        final String resFile = "msmarco_runs/colberte2e.res.1000";
//        final String resFile = "msmarco_runs/ANCE.res.1000";
        

        Settings.init("msmarco.properties");

        try {
            QPPMethod[] qppMethods = {
//                    new NQCSpecificity(Settings.getSearcher()),
                    new WIGSpecificity(Settings.getSearcher()),
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.2f), // 20% as top and bottom
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.5f), // 20% as top and bottom
//                    new OddsRatioSpecificity(Settings.getSearcher(), 0.3f), // 30% as top and bottom
//                  new ScoreRatioSpecificity(Settings.getSearcher(), 0.2f), // 20% as top and bottom
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
