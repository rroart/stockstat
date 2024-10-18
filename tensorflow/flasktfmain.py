#!/usr/bin/python3

from flask import Flask, request, send_from_directory

import os
import sys
from multiprocessing import Process, Queue

import json
from werkzeug.wrappers import Response

#from flask_caching import Cache
#cache = Cache()

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
#app.config['CACHE_TYPE'] = 'simple' # Set the cache type
#app.config['CACHE_DEFAULT_TIMEOUT'] = 30 # Set the default cache timeout in seconds
#app.config['CACHE_KEY_PREFIX'] = 'myapp_' # Set the cache key prefix
#cache.init_app(app)

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

@app.route('/download/<path:filename>', methods=['GET'])
def download(filename):
    # TODO validate
    full_path = os.path.join(app.root_path, "/tmp/download")
    return send_from_directory(full_path, filename, as_attachment=True)

@app.route('/imgclassify', methods=['POST'])
def do_imgclassify():
    def classifyrunner(queue, request):
        try:
            import classify
            cl = classify.Classify()
            cl.do_imgclassify(queue, request)
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

@app.route('/gpt/<ds>', methods=['POST'])
def do_gpt(ds):
    if False:
        from datetime import datetime
        dt = datetime.now()
        timestamp = dt.timestamp()
        data = cache.get(ds)
        print ("millis ", (dt.timestamp() - timestamp)*1000)
    else:
        data = None
    def classifyrunner(queue, request, cachedata):
        try:
            import classify
            cl = classify.Classify()
            cl.do_gpt(queue, request, cachedata)
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

    process = Process(target=classifyrunner, args=(aqueue, request, data))

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
                    result = {"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }
                    break
    except Exception as e:
        print(e)
        import sys,traceback
        traceback.print_exc(file=sys.stdout)
    return Response(json.dumps(result), mimetype='application/json')
    data = None
    if data is not None:
        print("Caching", ds)
        #cache.set(ds, data)
    else:
        print("Deleting", ds)
        #cache.delete(ds)

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
