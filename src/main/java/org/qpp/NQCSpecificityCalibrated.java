package org.qpp;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.evaluator.RetrievedResults;

import java.io.IOException;
import java.util.Arrays;

public class NQCSpecificityCalibrated extends BaseIDFSpecificity {
    float alpha, beta, gamma;

    public NQCSpecificityCalibrated(IndexSearcher searcher) {
        super(searcher);
    }

    public NQCSpecificityCalibrated(IndexSearcher searcher, float alpha, float beta, float gamma) {
        super(searcher);
        setParameters(alpha, beta, gamma);
    }

    public void setParameters(float alpha, float beta, float gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    @Override
    public double computeSpecificity(Query q, RetrievedResults retInfo, TopDocs topDocs, int k) {
    	k = Math.min(k,topDocs.scoreDocs.length); //to handle cases where less than topk docs available for a query
        return computeNQC(q, retInfo, k);
    }

    private double computeNQC(Query q, RetrievedResults topDocs, int k) {
    
        double[] rsvs = topDocs.getRSVs(k);
//        System.out.println("rsvs.length: "+rsvs.length);
        double mean = Arrays.stream(rsvs).average().getAsDouble();

        double avgIDF = 0;
        try {
            avgIDF = maxIDF(q);
        } catch (IOException e) {
            e.printStackTrace();
        }


        double nqc = 0;
        for (double rsv: rsvs) {
            double factor_1 = avgIDF;
            // only works for a square function; beta is to be even; we force it to be even
            if (rsv != 0) { //if rsvs are min-max normalized
            double factor_2 = (rsv - mean)*(rsv - mean)/rsv;

            double prod = Math.pow(factor_1, alpha) * Math.pow(factor_2, beta); // this is actually 2*beta
            prod = Math.pow(prod, gamma);

            nqc += prod;}
            
        }
        nqc /= (double)rsvs.length;

        return nqc * avgIDF; // high variance, high avgIDF -- more specificity
    }

    @Override
    public String name() {
        return "nqc_generic";
    }
}
