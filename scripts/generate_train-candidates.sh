PROJECT_HOME="/home/antonio/workspace-java/Itwiki"

<<COMMENT
java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.CandidateGenerator \
     -index "$PROJECT_HOME/wordsNumGt5Index" \
     -questions "$PROJECT_HOME/trec/train2393.num-recovered.questions.italian.txt"  \
     -maxHits 100 \
     -candidates "$PROJECT_HOME/target/candidates-train" \
     -similarity "org.apache.lucene.search.similarities.BM25Similarity"
COMMENT
     
java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.RelevantFlagger \
     -patterns "../trec/trec8-12.patterns" \
     -candidates "$PROJECT_HOME/target/candidates-train" \
     -relevantCandidates "$PROJECT_HOME/target/candidates-train.relevant" \
     -patternlib java
    
cat "$PROJECT_HOME/target/candidates-train.relevant" | awk -F"\t" '{ gsub(/ +/, "_", $2); $5=$5=="true"; print $1,"0",$2,$5 }' > "$PROJECT_HOME/target/candidates-train.qrels"

cat "$PROJECT_HOME/target/candidates-train.relevant" | awk -F"\t" 'BEGIN { OFS="\t" } { gsub(/ +/, "_", $2); print $1,"Q0",$2,$3,$4,"STANDARD" }' > "$PROJECT_HOME/target/candidates-train.top"

/home/antonio/Scaricati/trec_eval-8.0/trec_eval -a -M100 "$PROJECT_HOME/target/candidates-train.qrels" "$PROJECT_HOME/target/candidates-train.top" > "$PROJECT_HOME/target/candidates-train.trec_eval"
     
     
