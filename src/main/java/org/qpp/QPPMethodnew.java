package org.qpp;

import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.evaluator.RetrievedResults;
import org.experiments.PairDocs;
import org.experiments.ResDocs;

public interface QPPMethodnew {
//    public double computeSpecificity(Query q, RetrievedResults retInfo, TopDocs topDocs, int k);
    public double computeSpecificitynew(Query q, RetrievedResults retInfo,TopDocs topDocs,List<ResDocs> rtupleResdoc, List<PairDocs> rtuplePairdoc, int k);
    public String name();
}




