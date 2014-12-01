#wiki.sh

function removeWikipediaPages
    cat $1 | awk -F "\t" '$1 !~ /^Wikipedia:/' {print $0}'


while [ -n "$(echo $1 | grep '-')" ]; do
    case $1 in 
        -help) echo '-process option -help'
        shift;;
    esac
    shift
done
