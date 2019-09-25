# File: pos_tagging.py
# Template file for Informatics 2A Assignment 2:
# 'A Natural Language Query System in Python/NLTK'

# John Longley, November 2012
# Revised November 2013 and November 2014 with help from Nikolay Bogoychev
# Revised November 2015 by Toms Bergmanis and Shay Cohen


# PART B: POS tagging

from statements import *

# The tagset we shall use is:
# P  A  Ns  Np  Is  Ip  Ts  Tp  BEs  BEp  DOs  DOp  AR  AND  WHO  WHICH  ?

# Tags for words playing a special role in the grammar:

function_words_tags = [('a','AR'), ('an','AR'), ('and','AND'),
     ('is','BEs'), ('are','BEp'), ('does','DOs'), ('do','DOp'), 
     ('who','WHO'), ('which','WHICH'), ('Who','WHO'), ('Which','WHICH'), ('?','?')]
     # upper or lowercase tolerated at start of question.

function_words = [p[0] for p in function_words_tags]

def unchanging_plurals():
    nn = []
    nns = []
    l = []
    with open("sentences.txt", "r") as f:
        for line in f:
            for word in line.split():
                if (word.split('|')[1] == 'NN'):
                    nn.append(word.split('|')[0])
                elif (word.split('|')[1] == 'NNS'):
                    nns.append(word.split('|')[0])
    for w in nn:
        if w in nns and w not in l:
            l.append(w)
    return l

unchanging_plurals_list = unchanging_plurals()

def noun_stem (s):
    """extracts the stem from a plural noun, or returns empty string"""    
    if s in unchanging_plurals_list:
      return s
    elif s[-3:] == 'men':
      s = s[:-3]+'man'
    elif re.match('(unt|.)ies', s):
        s = s[:-1]
    elif re.match('.*[^aeiou]ies',s):
        s = s[:-3]
        s = s + 'y'
    elif re.match('.*(o|x|ch|sh|ss|zz)es',s):
        s = s[:-2]
    elif re.match('.*(z|s)es',s):
        s = s[:-1]
    elif re.match('.*[^i]es',s):
        s = s[:-1]
    elif re.match('.*[aeiou]ys', s):
        s = s[:-1]
    elif re.match('.*([^aeiousxyz(ch)(sh)])s',s):
        s = s[:-1]
    else:
        s = ""
    return s


def tag_word (lx,wd):
    """returns a list of all possible tags for wd relative to lx"""
    tags = []
    noun = noun_stem(wd)
    verb = verb_stem(wd)
    for tag in ['P','A']:
        if wd in lx.getAll(tag):
            tags.append(tag)
    if (wd in lx.getAll('N')) or (noun in lx.getAll('N')):
        if wd in unchanging_plurals_list:
            tags.append('Np')
            tags.append('Ns')
        elif noun == "":
            tags.append('Ns')
        else:
            tags.append('Np')
    if (wd in lx.getAll('N')) or (noun in lx.getAll('N')):
        if noun in unchanging_plurals_list:
            tags.remove('Np')
    for tag in ['I','T']:
        if (wd in lx.getAll(tag)) or (verb in lx.getAll(tag)):
            if verb == wd:
                tags.append(tag +'p')
            else:
                tags.append(tag +'s')
    for w in function_words_tags:
        if w[0] == wd:
            tags.append(w[1])
    return tags
    

def tag_words (lx, wds):
    """returns a list of all possible taggings for a list of words"""
    if (wds == []):
        return [[]]
    else:
        tag_first = tag_word (lx, wds[0])
        tag_rest = tag_words (lx, wds[1:])
        return [[fst] + rst for fst in tag_first for rst in tag_rest]

# End of PART B.