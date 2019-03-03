#!/usr/bin/python3

from flask import Flask, request

import sys
from multiprocessing import Process, Queue

def classifyrunner2(queue, request):
    import classify
    cl = classify.Classify()
    cl.do_learntestclassify(queue, request)

def predictrunner(queue, request):
    import predict
    pr = predict.Predict()
    pr.do_learntestlist(queue, request)

def hasgpurunner(queue, dummy):
    import device
    device.hasgpu(queue)
    
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
    def classifyrunner(queue, request):
        import classify
        cl = classify.Classify()
        cl.do_learntestclassify(queue, request)
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
    queue = Queue()
    process = Process(target=hasgpurunner, args=(queue, None))
    process.start()
    process.join()
    hasgpu = queue.get()
    threaded = False
    if len(sys.argv) > 1 and (not hasgpu) and sys.argv[1] == 'multi':
        threaded = True
        print("Run threaded")
    port = 8000
    if len(sys.argv) > 1 and (not hasgpu) and sys.argv[1] == 'dev':
        port = 8008
        print("Run other port")
    app.run(host='0.0.0.0', port=port, threaded=threaded)
