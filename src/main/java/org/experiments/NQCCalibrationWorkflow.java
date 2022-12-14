package org.experiments;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.correlation.KendalCorrelation;
import org.evaluator.Evaluator;
import org.evaluator.Metric;
import org.evaluator.ResultTuple;
import org.evaluator.RetrievedResults;
import org.qpp.NQCSpecificityCalibrated;
import org.qpp.QPPMethod;
import org.trec.FieldConstants;
import org.trec.TRECQuery;

import java.io.File;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import org.correlation.PearsonCorrelation;

public class NQCCalibrationWorkflow {
    Similarity sim = new LMDirichletSimilarity(1000);
    protected Map<String, TopDocs> topDocsMap = new HashMap<>();
    protected Map<String, List<ResDocs>> resDocsMap = new HashMap<>(); //pairwise resfile changes
    protected Map<String, List<PairDocs>> pairDocsMap = new HashMap<>(); //pairwise resfile changes
    protected QPPEvaluator qppEvaluator;
    protected Evaluator evaluator;
    protected QPPMethod qppMethod;
    protected List<TRECQuery> queries;

    public NQCCalibrationWorkflow() throws Exception {
        qppEvaluator = new QPPEvaluator(
                Settings.getProp(),
                Settings.getCorrelationMetric(), Settings.getSearcher(), Settings.getNumWanted());
        queries = qppEvaluator.constructQueries(Settings.getQueryFile(), Settings.tsvMode);
        evaluator = qppEvaluator.executeQueries(queries, sim, Settings.getNumWanted(),
                Settings.getQrelsFile(), Settings.RES_FILE, topDocsMap);
    }

    public NQCCalibrationWorkflow(String queryFile) throws Exception {
        qppEvaluator = new QPPEvaluator(
                Settings.getProp(),
                Settings.getCorrelationMetric(), Settings.getSearcher(), Settings.getNumWanted());
        queries = qppEvaluator.constructQueries(queryFile, Settings.tsvMode);
        evaluator = qppEvaluator.executeQueries(queries, sim, Settings.getNumWanted(), Settings.getQrelsFile(), Settings.RES_FILE, topDocsMap);
    }

    public NQCCalibrationWorkflow(String queryFile, String resFile) throws Exception {
        qppEvaluator = new QPPEvaluator(
                Settings.getProp(),
                Settings.getCorrelationMetric(), Settings.getSearcher(), Settings.getNumWanted());
        queries = qppEvaluator.constructQueries(queryFile, Settings.tsvMode);
        topDocsMap = loadResFile(new File(resFile));
//        resDocsMap = loadResDocFile(new File(resFile));  //pairwise resfile changes
        //System.out.println("#### : " + topDocsMap.size());
        evaluator = qppEvaluator.executeDummy(queries, sim,
                Settings.getNumWanted(), Settings.getQrelsFile(),
                Settings.RES_FILE, topDocsMap);
    }
    
    public NQCCalibrationWorkflow(String pairScoreFile, String queryFile, String resFile) throws Exception {
        qppEvaluator = new QPPEvaluator(
                Settings.getProp(),
                Settings.getCorrelationMetric(), Settings.getSearcher(), Settings.getNumWanted());
        queries = qppEvaluator.constructQueries(queryFile, Settings.tsvMode);
        topDocsMap = loadResFile(new File(resFile));
        resDocsMap = loadResDocFile(new File(resFile));  //pairwise resfile changes
        pairDocsMap = loadPairDocFile(new File(pairScoreFile));  //pairwise resfile changes
        //System.out.println("#### : " + topDocsMap.size());
        evaluator = qppEvaluator.executeDummy(queries, sim,
                Settings.getNumWanted(), Settings.getQrelsFile(),
                Settings.RES_FILE, topDocsMap);
    }

    public Map<String, TopDocs> loadResFile(File resFile) {
        Map<String, TopDocs> topDocsMap = new HashMap<>();
        try {
            List<String> lines = FileUtils.readLines(resFile, UTF_8);

            String prev_qid = null, qid = null;
            RetrievedResults rr = null;

            for (String line: lines) {
                String[] tokens = line.split("\\s+");
                qid = tokens[0];

                if (prev_qid!=null && !prev_qid.equals(qid)) {
                    topDocsMap.put(prev_qid, convert(rr));
                    rr = new RetrievedResults(qid);
                }
                else if (prev_qid == null) {
                    rr = new RetrievedResults(qid);
                }

                int offset = Settings.getDocOffsetFromId(tokens[2]);
                int rank = Integer.parseInt(tokens[3]);
                double score = Float.parseFloat(tokens[4]);

                rr.addTuple(String.valueOf(offset), rank, score);
                prev_qid = qid;
            }
            if (qid!=null)
                topDocsMap.put(qid, convert(rr));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return topDocsMap;
    }

    private TopDocs convert(RetrievedResults rr) {
        int nret = rr.getNumRet();
        ScoreDoc[] sd = new ScoreDoc[nret];

        int i = 0;
        for (ResultTuple resultTuple: rr.getTuples()) {
            sd[i++] = new ScoreDoc(
                Integer.parseInt(resultTuple.getDocName()),
                (float)(resultTuple.getScore())
            );
        }
        return new TopDocs(new TotalHits(nret, TotalHits.Relation.EQUAL_TO), sd);
    }
    
  //pairwise resfile changes
    public Map<String, List<ResDocs>> loadResDocFile(File resFile) {
        Map<String, List<ResDocs>> resDocsMap = new HashMap<>();
        List<ResDocs> rtuples_new = new ArrayList<ResDocs>();
        try {
            List<String> lines = FileUtils.readLines(resFile, UTF_8);

            String prev_qid = null, qid = null;
//            RetrievedResults rr = null;
            ResDocs listval = null;
            int i = 0;

            for (String line: lines) {
                String[] tokens = line.split("\\s+");
                qid = tokens[0];

                if (prev_qid!=null && !prev_qid.equals(qid)) {
//                	System.out.println("rtuples_new:"+rtuples_new);
                	resDocsMap.put(prev_qid, rtuples_new);
                	rtuples_new = new ArrayList<ResDocs>();                	
                }

                listval = new ResDocs(qid,tokens[2],tokens[3],tokens[4]);
//                System.out.println("listval:" + listval.toString());
                rtuples_new.add(listval);  
//                System.out.println("rtuples_new:" + rtuples_new);               
                prev_qid = qid;
                i++;
            }
            
            if (qid!=null)
            	resDocsMap.put(prev_qid, rtuples_new);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return resDocsMap;
    }
    
    public Map<String, List<PairDocs>> loadPairDocFile(File pairScoreFile) {
        Map<String, List<PairDocs>> pairDocsMap = new HashMap<>();
        List<PairDocs> rtuples_new = new ArrayList<PairDocs>();
        try {
            List<String> lines = FileUtils.readLines(pairScoreFile, UTF_8);

            String prev_qid = null, qid = null;
//            RetrievedResults rr = null;
            PairDocs listval = null;
            int i = 0;

            for (String line: lines) {
                String[] tokens = line.split("\\s+");
                qid = tokens[0];

                if (prev_qid!=null && !prev_qid.equals(qid)) {
//                	System.out.println("rtuples_new:"+rtuples_new);
                	pairDocsMap.put(prev_qid, rtuples_new);
                	rtuples_new = new ArrayList<PairDocs>();                	
                }

                listval = new PairDocs(qid,tokens[1],tokens[2]);
//                System.out.println("listval:" + listval.toString());
                rtuples_new.add(listval);  
//                System.out.println("rtuples_new:" + rtuples_new);               
                prev_qid = qid;
                i++;
            }
            
            if (qid!=null)
            	pairDocsMap.put(prev_qid, rtuples_new);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return pairDocsMap;
    }



    public double computeCorrelation(List<TRECQuery> queries, QPPMethod qppMethod, int qppTopK) {
        int numQueries = queries.size();
        double[] qppEstimates = new double[numQueries]; // stores qpp estimates for the list of input queries
        double[] evaluatedMetricValues = new double[numQueries]; // stores GTs (AP/nDCG etc.) for the list of input queries
        int i = 0;

        for (TRECQuery query : queries) {
            RetrievedResults rr = null;
            TopDocs topDocs = topDocsMap.get(query.id);
            evaluatedMetricValues[i] = evaluator.compute(query.id, Metric.AP);
            
            rr = new RetrievedResults(query.id, topDocs); // this has to be set with the topdocs
//            if (i==0) {
//            System.out.println("rr result: "+rr);}
            qppEstimates[i] = (float)qppMethod.computeSpecificity(
                    query.getLuceneQueryObj(), rr, topDocs, qppTopK);
            i++;
        }

        double corr = new PearsonCorrelation().correlation(evaluatedMetricValues, qppEstimates);
        System.out.println(String.format("Pearson's = %.4f", corr));
        return corr;
    }

    public double computeCorrelation(List<TRECQuery> queries, QPPMethod qppMethod) {
        return computeCorrelation(queries, qppMethod, Settings.getQppTopK());
    }
    
    public float[] calibrateParams(List<TRECQuery> trainQueries) {
        final int qppTopK = Settings.getQppTopK();
        final float[] alpha_choices = {/*0.25f, 0.5f, 1.0f,*/ 1.5f, /*2.0f*/};
        final float[] beta_choices = {0.25f, /*0.5f, 1.0f, 1.5f, 2.0f*/};
        final float[] gamma_choices = {/*0.25f,*/ 0.5f /*, 1.0f, 1.5f, 2.0f*/};
        float[] best_choice = new float[3]; // best (alpha, beta, gamma)
        double max_corr = 0;

        for (float alpha: alpha_choices) {
            for (float beta: beta_choices) {
                for (float gamma: gamma_choices) {
                    qppMethod = new NQCSpecificityCalibrated(Settings.getSearcher(), alpha, beta, gamma);
                    System.out.println(String.format("Executing NQC (%.2f, %.2f, %.2f)", alpha, beta, gamma));
                    double corr = computeCorrelation(trainQueries, qppMethod);
                    if (corr > max_corr) {
                        max_corr = corr;
                        best_choice[0] = alpha;
                        best_choice[1] = beta;
                        best_choice[2] = gamma;
                    }
                }
            }
        }
        return best_choice;
    }

    public double epoch() {
        final float TRAIN_RATIO = 0.5f;
        TrainTestInfo trainTestInfo = new TrainTestInfo(queries, TRAIN_RATIO);
        float[] tuned_params = calibrateParams(trainTestInfo.getTrain());
        QPPMethod qppMethod = new NQCSpecificityCalibrated(
                            Settings.getSearcher(),
                            tuned_params[0], tuned_params[1], tuned_params[2]);

        return computeCorrelation(queries, qppMethod);
    }

    public void averageAcrossEpochs() {
        final int NUM_EPOCHS = 30; // change it to 30!
        double avg = 0;
        for (int i=1; i <= NUM_EPOCHS; i++) {
            System.out.println("Random split: " + i);
            avg += epoch();
        }
        System.out.println(String.format("Result over %d runs of tuned 50:50 splits = %.4f", NUM_EPOCHS, avg/NUM_EPOCHS));
    }

    public static void main(String[] args) {
        final String queryFile = "data/topics.robust.all";
        Settings.init("qpp.properties");

        try {
            NQCCalibrationWorkflow nqcCalibrationWorkflow = new NQCCalibrationWorkflow(queryFile);
            nqcCalibrationWorkflow.averageAcrossEpochs();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
