#!/usr/bin/python3

from flask import Flask, request

import sys
from multiprocessing import Process, Queue

import json
from werkzeug.wrappers import Response

def classifyrunner2(queue, request):
    cl.do_learntestclassify(queue, request)

def predictrunner(queue, request):
    import predict
    pr = predict.Predict()
    pr.do_learntestlist(queue, request)

def hasgpurunner(queue, dummy):
    import device
    device.hasgpu(queue)
    
app = Flask(__name__)

@app.route('/', methods=['GET'])
def healthcheck():
    return(Response())

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
    def classifyrunner(queue, request):
       try:
           print("r", request.get_data(as_text=True))
           import classify
           cl = classify.Classify()
           cl.do_learntest(queue, request) 
       except:
            import sys,traceback
            memory = "CUDA error: out of memory" in traceback.format_exc()
            cudnn = "0 successful operations" in traceback.format_exc()
            queue.put(Response(json.dumps({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : memory, "cudnn" : cudnn }), mimetype='application/json'))
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            f = open("/tmp/outtf" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
            f.write(request.get_data(as_text=True))
            traceback.print_exc(file=f)
            f.close()
    aqueue = Queue()
    process = Process(target=classifyrunner, args=(aqueue, request))
    try:
        import queue
        process.start()
        while True:
            try:
                result = aqueue.get(timeout=timeout)
                break
            except queue.Empty as e:
                if not process.is_alive():
                    print("Process died")
                    result = Response(json.dumps({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }), mimetype='application/json')
                    break
    except Exception as e:
        print(e)
        import sys,traceback
        traceback.print_exc(file=sys.stdout)
    return result

@app.route('/learntestclassify', methods=['POST'])
def do_learntestclassify():
    def classifyrunner(queue, request):
        try:
            import classify
            cl = classify.Classify()
            cl.do_learntestclassify(queue, request)
        except:
            import sys,traceback
            memory = "CUDA error: out of memory" in traceback.format_exc()
            cudnn = "0 successful operations" in traceback.format_exc()
            queue.put(Response(json.dumps({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : memory, "cudnn" : cudnn }), mimetype='application/json'))
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            f = open("/tmp/outtf" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
            f.write(request.get_data(as_text=True))
            traceback.print_exc(file=f)
            f.close()
    aqueue = Queue()
    process = Process(target=classifyrunner, args=(aqueue, request))
    try:
        import queue
        process.start()
        while True:
            try:
                result = aqueue.get(timeout=timeout)
                break
            except queue.Empty as e:
                if not process.is_alive():
                    print("Process died")
                    result = Response(json.dumps({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }), mimetype='application/json')
                    break
    except Exception as e:
        print(e)
        import sys,traceback
        traceback.print_exc(file=sys.stdout)
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
        try:
            import classify
            cl = classify.Classify()
            cl.do_dataset(queue, request)
        except:
            import sys,traceback
            memory = "CUDA error: out of memory" in traceback.format_exc()
            cudnn = "0 successful operations" in traceback.format_exc()
            queue.put(Response(json.dumps({"accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : memory, "cudnn" : cudnn }), mimetype='application/json'))
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            f = open("/tmp/outtf" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
            f.write(request.get_data(as_text=True))
            traceback.print_exc(file=f)
            f.close()
    aqueue = Queue()
    process = Process(target=classifyrunner, args=(aqueue, request))
    try:
        import queue
        process.start()
        while True:
            try:
                result = aqueue.get(timeout=timeout)
                break
            except queue.Empty as e:
                if not process.is_alive():
                    print("Process died")
                    result = Response(json.dumps({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }), mimetype='application/json')
                    break
    except Exception as e:
        print(e)
        import sys,traceback
        traceback.print_exc(file=sys.stdout)
    return result

@app.route('/datasetgen', methods=['POST'])
def do_dataset_gen():
    def classifyrunner(queue, request):
        try:
            import classify
            cl = classify.Classify()
            cl.do_dataset_gen(queue, request)
        except:
            import sys,traceback
            memory = "CUDA error: out of memory" in traceback.format_exc()
            cudnn = "0 successful operations" in traceback.format_exc()
            queue.put(Response(json.dumps({"accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : memory, "cudnn" : cudnn }), mimetype='application/json'))
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            f = open("/tmp/outtf" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
            f.write(request.get_data(as_text=True))
            traceback.print_exc(file=f)
            f.close()
    aqueue = Queue()
    process = Process(target=classifyrunner, args=(aqueue, request))
    try:
        import queue
        process.start()
        while True:
            try:
                result = aqueue.get(timeout=timeout)
                break
            except queue.Empty as e:
                if not process.is_alive():
                    print("Process died")
                    result = Response(json.dumps({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }), mimetype='application/json')
                    break
    except Exception as e:
        print(e)
        import sys,traceback
        traceback.print_exc(file=sys.stdout)
    return result

@app.route('/filename', methods=['POST'])
def do_filename():
    def filenamerunner(queue, request):
        import classify
        cl = classify.Classify()
        cl.do_filename(queue, request)
    queue = Queue()
    process = Process(target=filenamerunner, args=(queue, request))
    process.start()
    result = queue.get()
    process.join()
    return result

def argstr():
    if len(sys.argv) > 1 and sys.argv[1].isnumeric():
        return sys.argv[1]
    else:
        return str(80)

if __name__ == '__main__':
    timeout = 60
    queue = Queue()
    process = Process(target=hasgpurunner, args=(queue, None))
    process.start()
    hasgpu = queue.get()
    process.join()
    threaded = False
    if len(sys.argv) > 1 and (not hasgpu) and sys.argv[1] == 'multi':
        threaded = True
        print("Run threaded")
    port = argstr()
    print("Used port", port)
    app.run(host='0.0.0.0', port=port, threaded=threaded)
