from pyterrier_t5 import MonoT5ReRanker, DuoT5ReRanker
import pyterrier as pt
import pandas as pd
import csv
if not pt.started():
    pt.init()
import os
import torch
# device =torch.device('cpu')
monoT5 = MonoT5ReRanker()
duoT5 = DuoT5ReRanker()

# dataset = pt.get_dataset('irds:msmarco-passage/trec-dl-2019/judged')
# # print('total queries : ', dataset.get_topics())
# # print('total qrels : ', dataset.get_qrels())
#
# msmarco1 = pt.get_dataset('irds:msmarco-passage/trec-dl-2019/judged')
# topics1 = msmarco1.get_topics()
# qrels1 = msmarco1.get_qrels()
#
# msmarco2 = pt.get_dataset('irds:msmarco-passage/trec-dl-2020/judged')
# topics2 = msmarco2.get_topics()
# qrels2 = msmarco2.get_qrels()
#
# qrels = pd.concat([qrels1, qrels2], ignore_index=True)
# topics = pd.concat([topics1, topics2], ignore_index=True)
# print(f"length of qrel: {len(qrels)}")
# print(f"length of topics/queries: {len(topics)}")
# # topics = topics.head(2)
# print(f"length of topics/queries now: {len(topics)}")

cols2 =['qid','query']

topics=pd.read_csv('trecdl.topics.tsv',sep='\t',names=cols2,encoding='utf-8')
print(topics.head(5))

colnames= ['qid','Q0','docid','rank','score','pyterrier']
input_res = pd.read_csv('ANCE.res.50.mmnorm', names=colnames,
                     sep='\t', encoding='utf-8')
input_res['docno']=input_res['docid']
input_res_df = input_res.merge(topics,how='left')
input_res_df = input_res_df[['qid','docid','docno','rank','score','query']]
input_res_df['docno'] = input_res_df['docid']
input_res_df['docno'] =input_res_df['docno'].astype(object)
input_res_df['qid'] = input_res_df['qid'].astype(object)
input_res_df['query'] = input_res_df['query'].astype(object)
input_res_df['rank'] = input_res_df['rank'].astype(int)
input_res_df['docid'] = input_res_df['docid'].astype(object)
input_res_df['score'] = input_res_df['score'].astype(float)

print(type(input_res_df))

input_res_df2=input_res_df.sort_values(['qid','score'],ascending = False).groupby('qid').head(5).copy()
# input_res_df2['docid']=input_res_df2['docid'].astype(int)
print(input_res_df2.dtypes)
topics2 = topics.sort_values(['qid'],ascending=True)


index_loc = './index_path'
if not os.path.exists(index_loc + "/data.properties"):
    indexer = pt.IterDictIndexer(index_loc)
    indexref = indexer.index(dataset.get_corpus_iter(), fields=["text"],meta=['docno', 'text'])
else:
    indexref = pt.IndexRef.of(index_loc+ "/data.properties")

index = pt.IndexFactory.of(indexref)
print('index ref created')

# # Setup 1 : BM25 %1000 >> monoT5%50 >> duoT5
# bm25 = pt.BatchRetrieve(index, wmodel="BM25") % 10
# # mono_pipeline = bm25 >> pt.text.get_text(dataset, "text") >> monoT5 % 50
# mono_pipeline = bm25 >> pt.text.get_text(index, "text") >> monoT5 % 5
# duo_pipeline = mono_pipeline % 5 >> duoT5

#Setup 2 : BM25 %1000 >> duoT5 %50
bm25 = pt.BatchRetrieve(index, wmodel="BM25") % 5
# duo_pipeline = bm25 >> pt.text.get_text(dataset, "text") >> duoT5 % 50
# res_bm25 = bm25(topics)
# print(res_bm25.dtypes)
# print(res_bm25.columns)
# print(type(res_bm25))

# res_bm25 = res_bm25[['qid','docid','docno','rank','score','query']]
print(res_bm25.head(5))

# duo_pipeline = bm25 >> pt.text.get_text(index, "text") >> duoT5
# duo_pipeline = res_bm25 >> pt.text.get_text(index, "text") >> duoT5
duo_pipeline = input_res_df2 >> pt.text.get_text(index, "text") >> duoT5


def get_trec_res(ranker, df):

  results = ranker( df )  # get the results for the query or queries

  # with_labels = results.merge(qrels, on=["qid", "docno"], how="left").fillna(0)  # left outer join with the qrels
  results['Q0']="Q0"
  results['pyterrier']="pyterrier"
  results_new = results[['qid','Q0','docid','rank','score','pyterrier']]
  results_new_sort = results_new.sort_values(['qid','rank'],ascending=True)
  return results_new_sort

# res_file_to_save
res_file_duot5 = get_trec_res(duo_pipeline,topics2)

# print(res_file_duot5.head(5))

#res_file_duot5.to_csv('res.bm25.100.monot5.50.duot5.50',sep='\t',index=False,header=False,encoding='utf-8')

