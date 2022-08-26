#To generate from Q1 D1 D2 paiwise_Score to (Q1, D1_D2 score) file
import pandas as pd
import numpy as np
colnames= ['qid','docid1','docid2','score','comment']

# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/bm25.1000.mt5.50/res.bm25.1000.monot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/bm25.1000.mt5.50.dt5/res.bm25.1000.mt5.50.dt5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/colbert.reranked.res.trec',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/11bm25.50/duoT5.50.score.softmax.tsv',names=colnames,sep='\t',encoding='utf-8')
res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/12BM25+BERT/duoT5.50.score.softmax.tsv',names=colnames,sep='\t',encoding='utf-8')

print(res_df.head(5))
# res_df['score'] = np.exp(res_df['score'])
res_df['docid1_docid2'] = res_df['docid1'].astype(str) + "_" + res_df['docid2'].astype(str)
# res_df['score'] = np.log(1+abs(res_df['score'].min()) + res_df['score'])
res_df = res_df [['qid','docid1_docid2','score','comment']]
print("new score datasframe")
print(res_df.head(5))

# # res_df.to_csv('res.bm25.1000.mt5.50.dt5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
res_df.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/12BM25+BERT/duoT5.50.score.softmax.pairwise.doc.tsv',sep='\t',index=False,header=False,encoding='utf-8')
#
#
# # res_df2 = pd.read_csv('res.bm25.1000.monot5.50.mmnorm',names=colnames,sep='\t',encoding='utf-8')
# res_df2 = pd.read_csv('colbert.reranked.res.trec.mmnorm',names=colnames,sep='\t',encoding='utf-8')
# print(res_df2.head(5))
# res_df2['score'] = np.log(1+res_df2['score'])
# print("new score datasframe")
# print(res_df2.head(5))

# res_df2.to_csv('res.bm25.1000.monot5.50.mmnorm.log',sep='\t',index=False,header=False,encoding='utf-8')
# res_df2.to_csv('colbert.reranked.res.trec.mmnorm.log',sep='\t',index=False,header=False,encoding='utf-8')

