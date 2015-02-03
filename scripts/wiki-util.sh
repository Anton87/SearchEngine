#   wiki.sh
#   
#   First:
#       usage: source scripts/wiki-utils.sh
#
#   Then:
#       ssplit -lang it -segmenter TextPro -wikiFile /path/to/file -outputWikiFile /path/to/output/file
#


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# load the classpath stored in the classpath.txt file.
# The classpath can be generated by using the (ClassPathPrint) class.

source $DIR/../classpath.txt
echo "DIR: $DIR"

#
# Escape the names of the paragraphs/sentences (2nd field) in the answer candidates file.
#
#
function urlencode {
    echo "escaping..."
    
    echo "\$#: "$#
    if [ $# -ne 2 ]; then
        echo "wrong args number."
        echo "usage: escape <candidate-answers> <output-file>"
        return
    fi
    
    input_file="$1"
    output_file="$2"
    
    if [ -z "$input_file" ]; then
        echo "input-file not specified"
        return
    fi
    
    if [ -z "$output_file" ]; then
        echo "output-file not specified"
        return
    fi
    
    if [ ! -f "$input_file" ]; then
        echo "file does not exist: $input_file"
        return
    fi
    
    #echo "DIR: $DIR/urlencode.awk"
    
    cat "$input_file" | awk -f "$DIR"/urlencode.awk > "$output_file"
    
    unset input_file
    unset output_file
}


# Extract from a file of sentences the name of paragraphs containing a specified pattern in their name.
#  .e.g.: from Armonium:01:00 extract Armonium:01
# awk -F "\t" '{for (i=0;i<length($1);i++){if (substr($1, i, 1)==":"){pos=i}}; print substr($1, 0, pos)}' | sort | uniq | wc -l


#
#   This command can be used to extract clean Wikipedia from useless pages.
#
#   removePages /^Wikipedia:\|^Template:\|^Aiuto:\|^Progetto:\|^File:\|^Portale:/ ../../itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.txt itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.cleaned.txt  itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.filtered-out.txt 



function removePages {
    echo "removePages... "
    
    while [ -n "$(echo $1 | grep '-')" ]; do
        case $1 in 
            -h)
                ;&
            --help)
                echo "process option -help..."
                echo "removePages <awkPattern> <inputFile> <outputFile> [filteredLinesFile]"
                return;;
        esac
        shift
    done
    
    if [ $# -lt 3 ] || [ $# -gt 4 ]; then
        echo "Wrong args number."
        return
    fi
    awkPattern="$1"
    inputFile="$2"
    outputFile="$3"
    
    echo "awkPattern:   $awkPattern"
    echo "inputFile:    $inputFile"
    echo "outputFile:   $outputFile"
    if [ $# -eq 4 ]; then echo "filteredLinesFile:    $4"; fi
        
    
    if [ -z "$awkPattern" ] ; then echo "awkPattern not specified" ; return ; fi
    
    if [ -z "$inputFile" ]; then
        echo "inputFile not specifed"; return; 
    elif [ ! -f "$inputFile" ]; then
        echo "No such file: $inputFile"; return; 
    fi
        
    if [ -z "$outputFile" ]; then echo "outputFile not spcified" ; return ; fi
    
    awk -F "\t" "\$1 !~ ${awkPattern} {print \$0}" "$inputFile" > "$outputFile"
    
    if [ $# -ne 4 ]; then return; fi
    
    filteredLinesFile="$4"
    echo "filteredLinesFile:    $filteredLinesFile"

    if [ -z "$filteredLinesFile" ]; then echo "filteredLinesFile not specified"; return; fi
    
    awk -F "\t" "\$1 ~ ${awkPattern} {print \$0}" "$inputFile" > "$filteredLinesFile"

}

#function removeWikipediaPages
#    cat $1 | awk -F "\t" '$1 !~ /^Wikipedia:/' {print $0}'

function removeWikipediaPages {
    echo "removeWikipediaPages... "
    
    while [ -n "$(echo $1 | grep '-')" ]; do
        case $1 in 
            -help) echo 'process option -help'
                echo "removeWikipediaPages -wikiFile /path/to/file -outputWikiFile /path/to/output/file"
                return;;
            -wikiFile) echo 'process option -wikiFile'
                echo "-wikiFile $2"
                wikiFile="$2"
                shift;;
            -outputWikiFile) echo 'process option -outputWikiFile'
                echo "-outputWikiFile $2"
                outputWikiFile="$2"
                shift;;
        esac
        shift
    done
    
    echo "wikiFile: \"$wikiFile\""
    echo "outputWikiFile: \"$outputWikiFile\""
    
    if [ -z "$wikiFile" ]; then
        echo "wikiFile not specified"
        return 1
    fi
        
    if [ ! -f "$wikiFile" ]; then
        echo "file does not exist: $wikiFile"
        return 1
    fi
    
    if [ -z "$outputWikiFile" ]; then
        echo "outputWikiFile not specified"
        return 1
    fi    
    
    awk -F "\t" '$1 !~ /^Wikipedia:/ {print $0}' "$wikiFile" > "$outputWikiFile"
    awk -F "\t" '$1  ~ /^Wikipedia:/ {print $0}' "$wikiFile" > "${wikiFile%.*}.wikipedia-pages.txt"
    
    unset wikiFile
    unset outputWikiFile
}


#

#  Print all the paragraphs containg a a given pattern

#  Options:

#      -p|--perl    Accept a Perl Regular Expression

#      -g|--group   Group all the results by the Paragraph's prefix names

#  Params:

#      <pattern>    The searched pattern

#      <filename>   The filename to search for the pattern

#  usage: printParagraphsContainingPattern [-p|--perl] [-g|--group] <pattern> <filename>

#  example: printParagraphsContainingPattern ":)"  "data/itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.no-wikipedia-pages.openNlp-segmented.txt"

#
function printParagraphsContainingPattern {

    echo "printParagraphsContainingPattern..."
    
    while [ -n "$(echo $1 | grep '-')" ]; do
        case $1 in
            -g)
                ;&
            --group)
                echo "--group=True"
                group="True";;
                #group="cut -d ':' -f 1 | sort | uniq -c | sed -e 's/^ *//g' | sort -nr -k 1 ";;
            -p)
                ;&
            --perl)
                echo "--perl"
                echo "perl=True"
                perl="-P";;            
            -h)
                ;&
            --help)
                echo "usage: printParagraphsContainingPattern [-p|--perl] [-g|--group] <pattern> <filename>";;
            *) 
                echo "unrecognized option: $1"
                return;;            
        esac
        shift
    done
    echo "No. of args: $#"
    echo "args: $* "
    
    if [ $# -ne 2 ]; then
        echo "Wrong args number."
        return
    fi
    
    pattern="$1"
    filepath="$2"
    
    echo "pattern:    $pattern"
    echo "filepath:    $filepath"
    if [ -z "$group" ]; then
        # Just print to STDOUT all the paragraphs containing the specified PATTERN
        cat "$filepath" | grep $perl "$pattern"     
    else
        # Print to STDOUT the first part of the page names containing the specified PATTERN, sorted by freq.
        #  e.g.:
        #       1006    Aiuto
        #       630     Progetto
        #       19      Template
        #       ...        
        cat "$filepath" | grep $perl "$pattern" | cut -d ":" -f 1 | sort | uniq -c | sed -e 's/^ *//g' | sort -nr -k 1
    fi
    #cat data/itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.no-wikipedia-pages.openNlp-segmented.txt | grep ":)" | cut -d ":" -f 1 | sort | uniq -c | sed -e 's/^ *//g' | sort -nr -k 1 | more

    unset pattern
    unset filepath
    unset perl
    unset group
    


}


function ssplit {
    echo "ssplitting... "
    
    while [ -n "$(echo $1 | grep '-')" ]; do
        case $1 in 
            -help) #echo 'process option -help'
                java -cp "$CLASSPATH" it.unitn.nlpir.itwiki.ssplitter.WikiParagraphsSegmenter --help
                return;;
            -lang) #echo 'process option -lang'
                #echo "-lang $2"
                lang="$2"
                shift;;
            -segmenter) #echo 'process option -segmenter'
                #echo "-segmenter $2"
                segmenter="$2"
                shift;;
            -wikiFile) #echo 'process option -wikiFile'
                #echo "-wikiFile $2"
                wikiFile="$2"
                shift;;
            -outputWikiFile) #echo 'process option -outputWikiFile'
                #echo "-outputWikiFile $2"
                outputWikiFile="$2"
                shift;;
        esac
        shift
    done
    echo "lang: $lang"
    echo "segmenter: $segmenter"
    echo "wikiFile: $wikiFile"
    echo "outputWikiFile: $outputWikiFile"
    java -cp "$CLASSPATH" it.unitn.nlpir.itwiki.ssplitter.WikiParagraphsSegmenter -lang $lang -segmenter $segmenter -wikiFile "$wikiFile" -outputWikiFile "$outputWikiFile"
    
    unset lang
    unset segmenter
    unset wikiFile
    unset outputWikiFile
}

