
import pandas as pd
import numpy as np
colnames= ['qid','Q0','docid','rank','score','pyterrier']

res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/12BM25+BERT/bm25_VBERT.2019.res',names=colnames,sep=' ',encoding='utf-8')
res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/12BM25+BERT/bm25_VBERT.2020.res',names=colnames,sep=' ',encoding='utf-8')
#
# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/09ANCE/ANCE.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/09ANCE/ANCE.2020.res',names=colnames,sep=' ',encoding='utf-8')

#400 queries
# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.2020.res',names=colnames,sep=' ',encoding='utf-8')

# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/07ColBERT.E2E/E2E.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/07ColBERT.E2E/E2E.2020.res',names=colnames,sep=' ',encoding='utf-8')

# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERT.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERT.2020.res',names=colnames,sep=' ',encoding='utf-8')

# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.2020.res',names=colnames,sep=' ',encoding='utf-8')

#04
# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.2020.res',names=colnames,sep=' ',encoding='utf-8')

#03
# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.2020.res',names=colnames,sep=' ',encoding='utf-8')


#02 not needed


#01
# res_df1 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.2019.res',names=colnames,sep=' ',encoding='utf-8')
# res_df2 = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.2020.res',names=colnames,sep=' ',encoding='utf-8')


#


print(res_df1.head(5))
print(len(res_df1))
print(res_df2.head(5))
print(len(res_df2))

res_df = pd.concat([res_df1,res_df2])
print(res_df.head(5))
print(len(res_df))
########### correct it before run
# res_df_50=res_df.sort_values(['qid','score'],ascending = False).groupby('qid').head(50).copy()
res_df_50=res_df.sort_values(['qid','score'],ascending = False).groupby('qid').head(50).copy()
print(len(res_df_50))
qids=res_df_50['qid'].unique().tolist()
print(len(qids))
# # res_df['score'] = ((res_df['score']-res_df['score'].min())/(res_df['score'].max()-res_df['score'].min()) )
# print(res_df_50['score'].min())
temp=0
for qid in qids:
    res_df_50_qid=res_df_50[res_df_50['qid'] == qid].copy()
    res_df_50_qid['score'] = ((res_df_50_qid['score'] - res_df_50_qid['score'].min()) / (res_df_50_qid['score'].max() - res_df_50_qid['score'].min()))
    if (temp == 0):
        res_df_50_mmnorm=res_df_50_qid
        temp += 1
        # print("firsttime")
        # # print(len(df1))
    else:
        # print("subs time")
        res_df_50_mmnorm = pd.concat([res_df_50_mmnorm,res_df_50_qid])
        # print(len(df1))
        temp += 1
res_df_50_mmnorm['rank'] = res_df_50_mmnorm.groupby('qid')['score'].rank('dense',ascending=False).astype(int)
# print(res_df_50_mmnorm.head(51))
print(len(res_df_50_mmnorm))
#     # print(res_df_50[res_df_50['qid'] == qid]['score'])
#     for i in range(50):
#         res_df_50.loc[i,'score']=  (res_df_50[res_df_50['qid'] == qid]['score'] -  res_df_50[res_df_50['qid'] == qid]['score'].min())
# # res_df_50['score'] = ((res_df_50['score']-res_df_50['score'].min())/(res_df_50['score'].max()-res_df_50['score'].min()) )
# print(res_df_50.head(5))
# print(res_df_50['score'].min())
# # print(res_df_100['qid'].value_counts())

res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')


# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/09ANCE/ANCE.res.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/07ColBERT.E2E/E2E.res.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')

# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')

# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
#03
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')

# 03 files MAP
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.500.mmnorm.score',sep='\t',index=False,header=False,encoding='utf-8')
#already there
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
#01
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/01bm25.colbert/colbert.reranked.res.100.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')


print("50 rec res file is written")



