cat candidates-train.relevant.txt | grep -P '\ttrue\t' | cut  -f 3 | sort -n | uniq -c | sort -n -k 1 -r > train-answers_pos.txt
