import pickle
from numpy import array
import re
import numpy as np
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.models import load_model
from tensorflow.keras.models import model_from_json
import tensorflow as tf
from os.path import dirname, join
# import json

def analyze_sentences(text):
    tokenizer = Tokenizer()
    tokenizer_filename = join(dirname(__file__), "tokenizer.pickle")
    with open(tokenizer_filename, 'rb') as handle:
        tokenizer = pickle.load(handle)

    text = text.replace('!', '.')
    text = text.replace('?', '.')
    text = text.split('. ')

    longest = 70
    for i in range(len(text)):
        text[i] = prepare_sentence(text[i])
        if text[i].count(" ") >= (longest - 1):
            text[i] = " ".join(text[i].split(" ", longest) [:longest])

    new_sentences = tokenizer.texts_to_sequences(text)
    npadded_sentences = pad_sequences(new_sentences, longest, padding='post')

    model_filename = join(dirname(__file__), 'financial_sentiment_analysis_model_softmax.keras')
    model = load_model(model_filename, compile=False)

    return model.predict(npadded_sentences)



def prepare_sentence(sentence):
    temp = sentence.replace('%', ' percent')
    temp = temp.replace("n't ", " not ")
    temp = re.sub(r"(\$[A-Za-z]+)|(\$[0-9])|([0-9]m)|([0-9]EUR)|([0-9]eur)|(EUR[0-9])|(eur[0-9])|(EURO[0-9])|(euro[0-9])", process_money, temp)
    temp = re.sub(r"( the | a | an |and | that | has | have | had | this | these | those | to | it | is | its | it's | are | for | of | in | on )", " ", temp)
    temp = re.sub(r"( the | a | an |and | that | has | have | had | this | these | those | to | it | is | its | it's | are | for | of | in | on )", " ", temp)
    temp = re.sub(r"( the | a | an |and | that | has | have | had | this | these | those | to | it | is | its | it's | are | for | of | in | on )", " ", temp)
    temp = re.sub(r"(The |A |An |And |That |Has |Have |Had |This |These |Those |To |It |Is |Its |It's |Are |For |Of |In |On )", " ", temp)
    temp = temp.replace('$', 'dollar ')
    temp = temp.replace('bn ', ' billion  ')
    temp = temp.replace('mn ', ' million ')
    temp = temp.replace(' m ', ' million ')
    temp = temp.replace(' EUR ', ' euro ')
    temp = temp.replace(' eur ', ' euro ')
    temp = temp.replace(',', '')
    temp = temp.replace(',', '')
    temp = re.sub(r"\s+", " ", temp)

    return temp



def process_money(match_obj):
    if match_obj.group(1) is not None:
        return re.sub(r"\$[A-Za-z]+",'comp4ny',match_obj.group(1))
    if match_obj.group(2) is not None:
        return match_obj.group(2).replace('$', 'dolllar ')
    if match_obj.group(3) is not None:
        return match_obj.group(3).replace('m ', ' million ')
    if match_obj.group(4) is not None:
        return match_obj.group(4).replace('EUR', ' eur')
    if match_obj.group(5) is not None:
        return match_obj.group(5).replace('eur', ' eur')
    if match_obj.group(4) is not None:
        return match_obj.group(6).replace('EUR', 'eur ')
    if match_obj.group(5) is not None:
        return match_obj.group(7).replace('eur', 'eur ')
    if match_obj.group(4) is not None:
        return match_obj.group(6).replace('EURO', 'euro ')
    if match_obj.group(5) is not None:
        return match_obj.group(7).replace('euro', 'euro ')



def analyze_article(article):
    article_predictions = analyze_sentences(article)
    print(article_predictions)

    prefiltered_predictions = article_predictions
    k = 0
    for sentence in prefiltered_predictions:
        if max(sentence) < 0.6:
            article_predictions = np.delete(article_predictions, k, axis = 0)
        else:
            k = k + 1

    max_article_len = 26
    article_predictions = article_predictions[0:max_article_len]
    for i in range(len(article_predictions), max_article_len):
        empty_array = [0,0,0]
        article_predictions = np.vstack([article_predictions, empty_array])

    article_predictions = article_predictions.tolist()
    model_end_filename = join(dirname(__file__), 'article_financial_sentiment_analyzer_60.keras')
    model_end = load_model(model_end_filename, compile=False)
    sentiment_probabilities = model_end.predict([article_predictions])

    return np.argmax(sentiment_probabilities[0])

