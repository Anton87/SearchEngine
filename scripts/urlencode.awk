#!/usr/bin/awk

BEGIN {
    FS = "\t";
    for (i = 0; i <= 255; i++) {
		ord[sprintf("%c", i)] = i
    }
}

function escape(str, c, len, res) {
    len = length(str)
    res = ""
    for (i = 1; i <= len; i++) {
		c = substr(str, i, 1);
		if (c ~ /[0-9A-Za-z]/)
	    	res = res c
		else
	    	res = res "%" sprintf("%02X", ord[c])
    }
    return res
}

#{ print escape($0) }
{
    
    print $1"\t"escape($2)"\t"$3"\t"$4"\t"$5"\t"$6
    #res = ""
    #res += $1
    #print res
    #res = ""
    #res += $1 + "\t"
    #res += escape($2) + "\t"
    #for (i = 3; i < NF-1; i++) {
    #    res += $i + "\t"
    #}
    #res += $(NF-1)
    #print res
}

#print $1"\t"escape($2)
