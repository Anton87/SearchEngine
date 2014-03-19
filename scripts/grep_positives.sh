cat $1 | awk -F "\t" '$5 == "true" {print $0}'
