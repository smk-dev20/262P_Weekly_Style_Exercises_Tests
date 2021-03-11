import sys, string
import numpy as np
from collections import Counter
"""
Week 9 - Array Programing Style
Code for Exercise 9 heavily reutilizes Code in tf-03.py 
References
https://github.com/crista/exercises-in-programming-style/blob/master/03-arrays/tf-03.py
https://stackoverflow.com/questions/53839435/is-there-a-numpy-equivalent-to-string-translate
https://stackoverflow.com/questions/43473736/most-common-2-grams-using-python
"""
# Read in file as char array appending space to beginning and end of words 
characters = np.array([' ']+list(open(sys.argv[1]).read())+[' '])


# Normalize - replace non-alpha with space and change to uppercase
characters[~np.char.isalpha(characters)] = ' '
characters = np.char.upper(characters)

#characters2 = characters

#Leet characters
leet = np.array(['4','8','<','D','3','7','[','#','1','J','K','L','M','N','0','P','Q','R','5','7','U','V','W','%','Y','2'])
characters = np.vectorize(lambda x: (leet[ord(x) - ord('A')] if (x!=' ' and ord(x)!=207) else ' '))(characters)

### Split the words by finding the indices of spaces
sp = np.where(characters == ' ')

# A little trick: let's double each index, and then take pairs
sp2 = np.repeat(sp, 2)

# Get the pairs as a 2D matrix, skip the first and the last
w_ranges = np.reshape(sp2[1:-1], (-1, 2))
# Result: array([[ 0,  6],
#                [ 6,  7],
#                [ 7, 13],
#                [13, 14]], dtype=int64)
# Remove the indexing to the spaces themselves
w_ranges = w_ranges[np.where(w_ranges[:, 1] - w_ranges[:, 0] > 2)]


# Words are in between spaces, given as pairs of indices
words = list(map(lambda r: characters[r[0]:r[1]], w_ranges))

# Recode the characters as strings
swords = np.array(list(map(lambda w: ''.join(w).strip(), words)))

#translate words to leet
#swords = np.char.translate(swords,str.maketrans("ABCDEFGHIJKLMNOPQRSTUVWXYZ","48<D37[#1JKLMN0PQR57UVW%Y2"))

# Load stop words
stop_words =np.char.upper( np.array(list(set(open('../stop_words.txt').read().split(',')))))

#translate stopwords to leet
stop_words = np.char.translate(stop_words,str.maketrans("ABCDEFGHIJKLMNOPQRSTUVWXYZ","48<D37[#1JKLMN0PQR57UVW%Y2"))

#remove stop words
ns_words = swords[~np.isin(swords, stop_words)]

#Find 2 grams and get their counts
bigrams = zip(ns_words, ns_words[1:])
counts = Counter(bigrams)

# Print 5 most frequent 2-grams
for bi in counts.most_common()[:5]:
    print(np.array(bi[0]), ' - ',bi[1])


    