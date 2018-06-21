#!/usr/bin/python3

from flask import Flask, request

from multiprocessing import Process, Queue

def classifyrunner(queue, request):
    import classify
    cl = classify.Classify()
    cl.do_learntestclassify(queue, request)

def predictrunner(queue, request):
    import predict
    pr = predict.Predict()
    pr.do_learntestlist(queue, request)

app = Flask(__name__)

@app.route('/eval', methods=['POST'])
def do_eval():
    return cl.do_eval(request)

@app.route('/classify', methods=['POST'])
def do_classify():
    return cl.do_classify(request)

@app.route('/learntest', methods=['POST'])
def do_learntest():
    return cl.do_learntest(request)

@app.route('/learntestclassify', methods=['POST'])
def do_learntestclassify():
    queue = Queue()
    process = Process(target=classifyrunner, args=(queue, request))
    process.start()
    process.join()
    result = queue.get()
    return result

@app.route('/predictone', methods=['POST'])
def do_learntestpredictone():
    queue = Queue()
    process = Process(target=predictrunner, args=(queue, request))
    process.start()
    process.join()
    result = queue.get()
    return result

@app.route('/predict', methods=['POST'])
def do_learntestpredict():
    queue = Queue()
    process = Process(target=predictrunner, args=(queue, request))
    process.start()
    process.join()
    result = queue.get()
    return result

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, threaded=True)
