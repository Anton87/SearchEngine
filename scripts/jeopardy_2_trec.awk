#awk -F "," '{qids[$1]++; s=$1"-"qids[$1];for(i=6;i<NF-1;i++){s=s","$i}; s=s","$i; print s}' | more
BEGIN {
    FS = ",";
    OFS = "\t";
}
{
    $1 = $1 "-" ++qids[$1];
    #print $1
    
    s = ""
    for (i = 6; i < NF - 1; i++) {
        s = s $i FS;
    }
    s = s $i;
    gsub(/^"/, "", s);
    gsub(/"$/, "", s);
    gsub(/""/, "\"", s);
    
    $2 = s;
    
    NF = 2;
    $1 = $1;
    print $0
}


