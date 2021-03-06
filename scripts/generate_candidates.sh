java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.RelevantFlagger \
     -patterns "../trec/trec13factpats.txt" \
     -candidates "../target/candidates-test2.bm25.txt" \
     -relevantCandidates "../target/candidates-test2.bm25.relevant.$1.txt" \
     -pattern "$1"
grep -P "\ttrue\t" "../target/candidates-test2.bm25.relevant.$1.txt" | cut -f 1 | uniq | wc -l
echo ""
     
java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.RelevantFlagger \
     -patterns "../trec/trec8-12.patterns" \
     -candidates "../target/candidates-train2.bm25.txt" \
     -relevantCandidates "../target/candidates-train2.bm25.relevant.$1.txt" \
     -pattern "$1"
grep -P "\ttrue\t" "../target/candidates-train2.bm25.relevant.$1.txt" | cut -f 1 | uniq | wc -l
echo ""

java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.RelevantFlagger \
     -patterns "../trec/trec13factpats.txt" \
     -candidates "../target/candidates-test2.txt" \
     -relevantCandidates "../target/candidates-test2.relevant.$1.txt" \
     -pattern "$1"
grep -P "\ttrue\t" "../target/candidates-test2.relevant.$1.txt" | cut -f 1 | uniq | wc -l
echo ""
     
java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.RelevantFlagger \
     -patterns "../trec/trec8-12.patterns" \
     -candidates "../target/candidates-train2.txt" \
     -relevantCandidates "../target/candidates-train2.relevant.$1.txt" \
     -pattern "$1"
grep -P "\ttrue\t" "../target/candidates-train2.relevant.$1.txt" | cut -f 1 | uniq | wc -l
echo ""
