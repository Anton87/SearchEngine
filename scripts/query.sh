java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.LuceneRetriever \
     -index ../wordsNumGt5Index \
     -query "$1"  \
     -maxHits 10
