cat candidates-train.relevant.txt | grep -P '\ttrue\t' | cut  -f 6- | awk  '{print NF}' | sort -n | uniq -c | sort -n -k 1 -r > train_paragrahs-length.txt
