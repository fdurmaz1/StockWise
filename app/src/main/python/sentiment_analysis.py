import pickle
from numpy import array
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.preprocessing.text import Tokenizer
import tensorflow as tf
from tensorflow.keras.models import model_from_json
import json
from tensorflow.keras.models import load_model
from os.path import dirname, join

def analyze_sentiment(text):
    tokenizer = Tokenizer()
    tokenizer_filename = join(dirname(__file__), "tokenizer.pickle")
    with open(tokenizer_filename, 'rb') as handle:
        tokenizer = pickle.load(handle)
    text = text + ""
    news = text.split(". ")
    # paragraph = "The US has warned regional players against getting pulled into the war, calling on Iran and its proxies not to escalate. The US military has said that a guided missile submarine has arrived in the Middle East, a message of deterrence directed at regional adversaries. The Pentagon last month ordered a second carrier strike group to the eastern Mediterranean and sent Air Force fighter jets to the region. Iran, which backs Hamas, has denied involvement in the October 7 attack but has said that it morally supports the “anti-Israel resistance” – which includes Hamas, Hezbollah and other Iran-backed militias.On Israel’s northern border, Iran-backed Hezbollah has engaged in an exchange of fire since the Gaza war began. Those altercations have however been confined to the border areas."
    # sentences = paragraph.split(". ")

    for i in range(len(news)):
        temp = news[i].replace('%', ' percent')
        temp = temp.replace('$', 'dollar ')
        temp = temp.replace('bn', ' billion')
        temp = temp.replace(',', '')
        news[i] = temp

    longest = 50
    # new_review = [
    #     "c0mp4ny debuts iPhone 14 Pro and iPhone 14 Pro Max",
    #     "The tech giant faces some supply chain challenges, but the outlook for its business hasn’t markedly changed this week",
    #     "The iPhone maker is now officially in a bear market alongside other technology giants",
    #     "The car-rental giant Hertz is making an audacious bet on electric vehicles, purchasing 100,000 battery-powered vehicles from c0mp4ny"
    # ]
    new_sentences = tokenizer.texts_to_sequences(news)
    npadded_sentences = pad_sequences(new_sentences, longest, padding='post')

    model_filename = join(dirname(__file__), 'financial_sentiment_analysis_model.keras')
    model = load_model(model_filename, compile=False)

    result = model.predict(npadded_sentences)

    return result
