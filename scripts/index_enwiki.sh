PROJECT_HOME="/home/antonio/workspace-java/SearchEngine"

java -cp .:../libs/*:../bin it.unitn.nlpir.itwiki.LuceneIndexer \
     -index en_index \
     -doc /home/antonio/workspace-java/enwiki-20140203-pages-articles-multistream-with-lists-no-images-310.txt \
     -docFilter wordsNumLe5 \
     -analyzer org.apache.lucene.analysis.en.EnglishAnalyzer
