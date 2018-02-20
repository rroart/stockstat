#!/usr/bin/python3

from flask import Flask, request

app = Flask(__name__)

import classify
import predict

cl = classify.Classify()
pr = predict.Predict()

@app.route('/eval', methods=['POST'])
def do_eval():
    return cl.do_eval(request)

@app.route('/classify', methods=['POST'])
def do_classify():
    return cl.do_classify(request)

@app.route('/learntest', methods=['POST'])
def do_learntest():
    return cl.do_learntest(request)

@app.route('/predictone', methods=['POST'])
def do_learntestpredict():
    return pr.do_learntest(request)

@app.route('/predict', methods=['POST'])
def do_learntestpredict2():
    return pr.do_learntestlist(request)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000)
