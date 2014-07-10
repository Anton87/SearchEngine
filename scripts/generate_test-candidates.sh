PROJECT_HOME="/home/antonio/workspace-java/SearchEngine"

analyzer="org.apache.lucene.analysis.standard.StandardAnalyzer"

if [ $1 == 'en' ]
then
     analyzer="org.apache.lucene.analysis.en.EnglishAnalyzer"
elif [ $1 == 'it' ]
then
     analyzer="org.apache.lucene.analysis.it.ItalianAnalyzer"
fi

java -cp .:$PROJECT_HOME/libs/*:$PROJECT_HOME/bin it.unitn.nlpir.wiki.CandidateGenerator \
     -index "$PROJECT_HOME/index/$1" \
     -analyzer "$analyzer" \
     -questions "$PROJECT_HOME/trec/$1/trec13factpats.questions.txt"  \
     -maxHits 100 \
     -candidates "$PROJECT_HOME/target/$1/candidates-test.txt"
     
java -cp .:$PROJECT_HOME/libs/*:$PROJECT_HOME/bin it.unitn.nlpir.wiki.RelevantFlagger \
     -patterns "$PROJECT_HOME/trec/$1/trec13factpats.txt" \
     -candidates "$PROJECT_HOME/target/$1/candidates-test.txt" \
     -relevantCandidates "$PROJECT_HOME/target/$1/candidates-test.relevant.txt" \
     -patternlib jregex
     
cat "../target/$1/candidates-test.relevant.txt" | awk '$6 == "true" {print $0}' > "../target/$1/candidates-test.true.txt" 
     
cat "$PROJECT_HOME/target/$1/candidates-test.relevant.txt" | awk -F"\t" '{ gsub(/ +/, "_", $2); $5=$5=="true"; print $1,"0",$2,$5 }' > "$PROJECT_HOME/target/$1/candidates-test.qrels.txt"

cat "$PROJECT_HOME/target/$1/candidates-test.relevant.txt" | awk -F"\t" 'BEGIN { OFS="\t" } { gsub(/ +/, "_", $2); print $1,"Q0",$2,$3,$4,"STANDARD" }' > "$PROJECT_HOME/target/$1/candidates-test.top.txt"

/home/antonio/Scaricati/trec_eval-8.0/trec_eval -a "$PROJECT_HOME/target/$1/candidates-test.qrels.txt" "$PROJECT_HOME/target/$1/candidates-test.top.txt" > "$PROJECT_HOME/target/$1/candidates-test.trec_eval.txt"
     


