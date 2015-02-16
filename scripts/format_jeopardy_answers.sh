


answers_file=${1:?"answers file missing"}

cmd="cat $answers_file"

# filter out the content within round parenthesis.
cmd="$cmd | grep -vP \"\(.*?\)\""

# filter out the content within square parenthesis
cmd="$cmd | grep -vP \"\[.*?\]\""

# filter out all lines containing or statements.
cmd="$cmd | grep -vP \"\bor\b\""

# filter out all lines containing & statements.
cmd="$cmd | grep -vP \"\B&\B\""

# filter out all the lines containing / statements.
cmd="$cmd | grep -vP \"/\""

# add to answers starting with an article "a", "an" or "the" the version without articles.
cmd="$cmd | gawk -F ' '	'{print \$0}; \$2 ~ /^(a|the)\y/ {gsub(/^(a|the)\y/, \"\", \$2); gsub(/\s+/, \" \", \$0); print \$0}'"

# replace + with \+
cmd="$cmd | sed s/\\\+/\\\\\\\+/g"

# replace * with \*
cmd="$cmd | sed s/\\\*/\\\\\\\*/g"

# replace ? with \?
cmd="$cmd | sed s/\\\?/\\\\\\\?/g"
 
# replace . with \.
cmd="$cmd | sed s/\\\./\\\\\\\./g"

# replace '\"' with '"'
cmd="$cmd | sed s/\\\\\\\\\\\"/\\\"/g"

# convert answers to regex (add \b...\b)
cmd="$cmd | gawk -F ' ' '{s=\"\"; for (i=2; i < NF; i++){s = s\$i\" \"}; s=s\$i; "

cmd="$cmd print \$1\" (^|\\\\s)\"s\"(\\\\s|$)\"; "

cmd="$cmd}'"

#echo "$cmd"

eval "$cmd"

#grep -vP "\(.*?\)" "$tmp_file" > "$tmp_file"

# filter out all the answer containing or clauses
#cat "$tmp_file" | grep -vP "\bor\b"  > "$tmp_file"
	
#cat data/JEOPARDY.answers.out | grep -vP "\(.*?\)|\bor\b|\B&\B" | gawk -F " " '{print $0}; $2 ~ /^(a|the)\y/ {gsub(/^(a|the)\y/, "", $2); gsub(/\s+/, " ", $0); print $0}' 
