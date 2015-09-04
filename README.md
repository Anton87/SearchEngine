Itwiki
======

Indexing and retrieving tools for the itwiki corpus.

Clean up
========

Tthe following command clean up the itwiki corpus :

		shell> grep --invert-match --perl-regexp "^(Wikipedia|Progetto|Template|File|Aiuto|Portale):" "itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.txt" > "data/itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.cleaned.txt"
