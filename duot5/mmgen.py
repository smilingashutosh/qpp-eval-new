
import pandas as pd
import numpy as np
colnames= ['qid','Q0','docid','rank','score','pyterrier']
#
   # monoT5 and duoT5 resfiles and pairwise probablity file       
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/01bm25.colbert/colbert.reranked.res.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/07ColBERT.E2E/E2E.res.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')
# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/09ANCE/ANCE.res.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')

# res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/11bm25.50/res.bm25.50.duot5.50',names=colnames,sep='\t',encoding='utf-8')
res_df = pd.read_csv('/home/ashutosh/qpp-eval/msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm.duot5.50',names=colnames,sep='\t',encoding='utf-8')

print(res_df.head(5))
print(len(res_df))
########### correct it before run if needed
res_df_50=res_df.sort_values(['qid','score'],ascending = False).groupby('qid').head(50).copy()
print(len(res_df_50))
qids=res_df_50['qid'].unique().tolist()
print(len(qids))
#
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
print(res_df_50_mmnorm.head(51))
print(len(res_df_50_mmnorm))

# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/01bm25.colbert/colbert.reranked.res.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/02bm25.1000.mt5.50/res.bm25.1000.monot5.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/03BM25+docT5query+BERT/bm25doct5q_VBERT.res.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/04BM25+docT5query+ColBERT/BM25_docT5q_ColBERT.res.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/05BM25+DeepCT+BERT/bm25deepCT_VBERT.res.50.mmnorm..duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/06BM25+DeepCT+ColBERT/BM25_deepct_ColBERTres.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/07ColBERT.E2E/E2E.res.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/08ColBERT.E2E+BERT-QE/colberte2e_bertqe.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/09ANCE/ANCE.res.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
# res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/11bm25.50/res.bm25.50.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')
res_df_50_mmnorm.to_csv('/home/ashutosh/qpp-eval/msmarco_runs/12BM25+BERT/bm25_VBERT.res.50.mmnorm.duot5.50.mmnorm',sep='\t',index=False,header=False,encoding='utf-8')

print("50 rec res file is written")



