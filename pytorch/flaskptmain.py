#!/usr/bin/python3

from flask import Flask, request

import sys
#from torch.multiprocessing import Process, Queue

import json
from werkzeug.wrappers import Response

def classifyrunner2(queue, request):
    import classify
    cl = classify.Classify()
    cl.do_learntestclassify(queue, request)

def predictrunner(queue, request):
    import classify
    cl = classify.Classify()
    cl.do_learntestclassify(queue, request)

def hasgpu():
    import torch
    return torch.cuda.is_available()
    
app = Flask(__name__)

@app.route('/eval', methods=['POST'])
def do_eval():
    return cl.do_eval(request)

@app.route('/classify', methods=['POST'])
def do_classify():
    import classify
    cl = classify.Classify()
    return cl.do_classify(request)

@app.route('/learntest', methods=['POST'])
def do_learntest():
    import classify
    cl = classify.Classify()
    return cl.do_learntest(request)

@app.route('/learntestclassify', methods=['POST'])
def do_learntestclassify():
    def classifyrunner(queue, request):
        import classify
        cl = classify.Classify()
        try:
            return cl.do_learntestclassify(queue, request)
        except:
            import sys,traceback
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            f = open("/tmp/outpt" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
            f.write(request.get_data(as_text=True))
            traceback.print_exc(file=f)
            f.close()
            memory = "CUDA out of memory" in traceback.format_exc()
            return(Response(json.dumps({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "gpu" : hasgpu, "memory" : memory, "exception" : True }), mimetype='application/json'))
    return classifyrunner(None, request)
    queue = Queue()
    process = Process(target=classifyrunner, args=(queue, request))
    process.start()
    result = queue.get()
    process.join()
    return result

@app.route('/predictone', methods=['POST'])
def do_learntestpredictone():
    queue = Queue()
    process = Process(target=predictrunner, args=(queue, request))
    process.start()
    result = queue.get()
    process.join()
    return result

@app.route('/predict', methods=['POST'])
def do_learntestpredict():
    queue = Queue()
    process = Process(target=predictrunner, args=(queue, request))
    process.start()
    result = queue.get()
    process.join()
    return result

@app.route('/dataset', methods=['POST'])
def do_dataset():
    def classifyrunner(queue, request):
        import classify
        cl = classify.Classify()
        try:
            return cl.do_dataset(queue, request)
        except:
            import sys,traceback
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            f = open("/tmp/outpt" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
            f.write(request.get_data(as_text=True))
            traceback.print_exc(file=f)
            f.close()
            memory = "CUDA out of memory" in traceback.format_exc()
            return(Response(json.dumps({"accuracy": None, "loss": None, "gpu" : hasgpu, "memory" : memory, "exception" : True }), mimetype='application/json'))
    return classifyrunner(None, request)
    queue = Queue()
    process = Process(target=classifyrunner, args=(queue, request))
    process.start()
    result = queue.get()
    process.join()
    return result

@app.route('/filename', methods=['POST'])
def do_filename():
    def filenamerunner(queue, request):
        import classify
        cl = classify.Classify()
        return cl.do_filename(queue, request)
    return filenamerunner(None, request)
    queue = Queue()
    process = Process(target=filenamerunner, args=(queue, request))
    process.start()
    result = queue.get()
    process.join()
    return result

def argstr():
    if len(sys.argv) > 1 and sys.argv[1] == 'dev':
        return 'dev'
    else:
        return ''

if __name__ == '__main__':
#    queue = Queue()
#    process = Process(target=hasgpurunner, args=(queue, None))
#    process.start()
#    hasgpu = queue.get()
#    process.join()
    hasgpu = hasgpu()
    print("Has GPU", hasgpu)
    threaded = False
    if len(sys.argv) > 1 and (not hasgpu) and sys.argv[1] == 'multi':
        threaded = True
        print("Run threaded")
    port = 8010
    if len(sys.argv) > 1 and sys.argv[1] == 'dev':
        port = 8018
        print("Run other port")
    app.run(host='0.0.0.0', port=port, threaded=threaded)
