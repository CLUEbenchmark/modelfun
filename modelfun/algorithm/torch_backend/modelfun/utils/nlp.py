import jieba
import os


def tokenize(text):
    tokens = jieba.cut(text, cut_all=False)
    return list(tokens)


def get_stopword_list():
    final_list = []
    for file in os.listdir('datasets/stopwords/'):
        with open('datasets/stopwords/' + file, 'r', encoding='utf-8') as f:
            stopword_list = [word.strip('\n') for word in f.readlines()]
        final_list.extend(stopword_list)
    return final_list
