from pyterrier_t5 import MonoT5ReRanker, DuoT5ReRanker
import pyterrier as pt
import pandas as pd
import csv
pt.init()
import os
monoT5 = MonoT5ReRanker()
duoT5 = DuoT5ReRanker()

dataset = pt.get_dataset('irds:msmarco-passage/trec-dl-2019/judged')
# print('total queries : ', dataset.get_topics())
# print('total qrels : ', dataset.get_qrels())

msmarco1 = pt.get_dataset('irds:msmarco-passage/trec-dl-2019/judged')
topics1 = msmarco1.get_topics()
qrels1 = msmarco1.get_qrels()

msmarco2 = pt.get_dataset('irds:msmarco-passage/trec-dl-2020/judged')
topics2 = msmarco2.get_topics()
qrels2 = msmarco2.get_qrels()

qrels = pd.concat([qrels1, qrels2], ignore_index=True)
topics = pd.concat([topics1, topics2], ignore_index=True)
print(f"length of qrel: {len(qrels)}")
print(f"length of topics/queries: {len(topics)}")
# topics = topics.head(2)
print(f"length of topics/queries now: {len(topics)}")



index_loc = './index_path'
if not os.path.exists(index_loc + "/data.properties"):
    indexer = pt.IterDictIndexer(index_loc)
    indexref = indexer.index(dataset.get_corpus_iter())
else:
    indexref = pt.IndexRef.of(index_loc+ "/data.properties")

index = pt.IndexFactory.of(indexref)
print('index ref created')

# Setup 1 : BM25 %1000 >> monoT5%50 >> duoT5
bm25 = pt.BatchRetrieve(index, wmodel="BM25") % 1000
mono_pipeline = bm25 >> pt.text.get_text(dataset, "text") >> monoT5 % 50
duo_pipeline = mono_pipeline % 50 >> duoT5

# Setup 2 : BM25 %1000 >> duoT5 %50
# bm25 = pt.BatchRetrieve(index, wmodel="BM25") % 1000
# duo_pipeline = bm25 >> pt.text.get_text(dataset, "text") >> duoT5 % 50
#
print(pt.Experiment(
  [
   duo_pipeline,
  ],
    topics,
    qrels,
  names=["BM25 >> monoT5 >> duoT5"],
  eval_metrics=["map"],
  # save_dir="./"
))