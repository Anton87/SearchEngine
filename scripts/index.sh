PROJECT_HOME="/home/antonio/workspace-java/SearchEngine"

analyzer="org.apache.lucene.analysis.standard.StandardAnalyzer"

if [ $1 == 'en' ]
then
     analyzer="org.apache.lucene.analysis.en.EnglishAnalyzer"
elif [ $1 == 'it' ]
then
     analyzer="org.apache.lucene.analysis.it.ItalianAnalyzer"
fi

java -cp .:$PROJECT_HOME/libs/*:$PROJECT_HOME/bin it.unitn.nlpir.wiki.LuceneIndexer \
     -index "$PROJECT_HOME/index/$1" \
     -doc "../../itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.txt" \
     -analyzer "$analyzer" \
     -docFilter "wordsNumLe5"
