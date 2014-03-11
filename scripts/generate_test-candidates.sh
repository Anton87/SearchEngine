PROJECT_HOME="/home/antonio/workspace-java/Itwiki"

java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.CandidateGenerator \
     -index "$PROJECT_HOME/wordsNumGt5Index" \
     -questions "../trec/trec13.test.questions.italian.txt"  \
     -maxHits 100 \
     -candidates "$PROJECT_HOME/target/candidates-test"
    
java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.RelevantFlagger \
     -patterns "../trec/trec13factpats.italian.txt" \
     -candidates "$PROJECT_HOME/target/candidates-test" \
     -relevantCandidates "$PROJECT_HOME/target/candidates-test.relevant" \
     -patternlib java
    
cat "$PROJECT_HOME/target/candidates-test.relevant" | awk -F"\t" '{ gsub(/ +/, "_", $2); $5=$5=="true"; print $1,"0",$2,$5 }' > "$PROJECT_HOME/target/candidates-test.qrels"

cat "$PROJECT_HOME/target/candidates-test.relevant" | awk -F"\t" 'BEGIN { OFS="\t" } { gsub(/ +/, "_", $2); print $1,"Q0",$2,$3,$4,"STANDARD" }' > "$PROJECT_HOME/target/candidates-test.top"

/home/antonio/Scaricati/trec_eval-8.0/trec_eval -a "$PROJECT_HOME/target/candidates-test.qrels" "$PROJECT_HOME/target/candidates-test.top" > "$PROJECT_HOME/target/candidates-test.trec_eval"
     
     
