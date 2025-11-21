#!/usr/bin/python3

from flask import Flask, request, send_from_directory

import sys
# Use torch.multiprocessing but ensure spawn start method is set before creating processes
import torch.multiprocessing as _tmp_mp
from torch.multiprocessing import Process, Queue
try:
    # try to set spawn start method; ignore if already set
    _tmp_mp.set_start_method('spawn', force=False)
except RuntimeError:
    pass

import os
import json
from werkzeug.wrappers import Response

def classifyrunner2(queue, request_data):
    import classify
    cl = classify.Classify()
    # request_data is the raw request body (string)
    cl.do_learntestclassify(queue, request_data)

def predictrunner(queue, request_data):
    import classify
    cl = classify.Classify()
    cl.do_learntestclassify(queue, request_data)

def hasgpu():
    import torch
    return torch.cuda.is_available()
    
app = Flask(__name__)

@app.route('/', methods=['GET'])
def healthcheck():
    return(Response())

@app.route('/eval', methods=['POST'])
def do_eval():
    import classify
    cl = classify.Classify()
    return cl.do_eval(request)

@app.route('/classify', methods=['POST'])
def do_classify():
    import classify
    cl = classify.Classify()
    return cl.do_classify(request)

def learntestrunner(queue, request_data):
        try:
            import classify
            cl = classify.Classify()
            return cl.do_learntest(queue, request_data)
        except:
            import sys,traceback
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            try:
                f = open("/tmp/outpt" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
                f.write(request_data if request_data is not None else "")
                traceback.print_exc(file=f)
                f.close()
            except Exception:
                pass
            memory = "CUDA out of memory" in traceback.format_exc()
            queue.put({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "gpu" : hasgpu, "memory" : memory, "exception" : True })


@app.route('/learntest', methods=['POST'])
def do_learntest():
    aqueue = Queue()
    request_data = request.get_data(as_text=True)
    # default result to avoid referenced-before-assignment warnings
    result = {"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }
    process = Process(target=learntestrunner, args=(aqueue, request_data))
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

def learntestclassifyrunner(queue, request_data):
        try:
            import classify
            cl = classify.Classify()
            return cl.do_learntestclassify(queue, request_data)
        except:
            import sys,traceback
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            try:
                f = open("/tmp/outpt" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
                f.write(request_data if request_data is not None else "")
                traceback.print_exc(file=f)
                f.close()
            except Exception:
                pass
            memory = "CUDA out of memory" in traceback.format_exc()
            queue.put({"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "gpu" : hasgpu, "memory" : memory, "exception" : True })


@app.route('/learntestclassify', methods=['POST'])
def do_learntestclassify():
    aqueue = Queue()
    request_data = request.get_data(as_text=True)
    # default result to avoid referenced-before-assignment warnings
    result = {"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }
    process = Process(target=learntestclassifyrunner, args=(aqueue, request_data))
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

@app.route('/predictone', methods=['POST'])
def do_learntestpredictone():
    queue = Queue()
    request_data = request.get_data(as_text=True)
    process = Process(target=predictrunner, args=(queue, request_data))
    process.start()
    result = queue.get()
    process.join()
    return result

@app.route('/predict', methods=['POST'])
def do_learntestpredict():
    queue = Queue()
    request_data = request.get_data(as_text=True)
    process = Process(target=predictrunner, args=(queue, request_data))
    process.start()
    result = queue.get()
    process.join()
    return result

def datasetrunner(queue, request_data):
        try:
            import classify
            cl = classify.Classify()
            return cl.do_dataset(queue, request_data)
        except:
            import sys,traceback
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            try:
                f = open("/tmp/outpt" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
                f.write(request_data if request_data is not None else "")
                traceback.print_exc(file=f)
                f.close()
            except Exception:
                pass
            memory = "CUDA out of memory" in traceback.format_exc()
            queue.put({"accuracy": None, "loss": None, "gpu" : hasgpu, "memory" : memory, "exception" : True })


@app.route('/dataset', methods=['POST'])
def do_dataset():
    aqueue = Queue()
    request_data = request.get_data(as_text=True)
    # default result to avoid referenced-before-assignment warnings
    result = {"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }
    process = Process(target=datasetrunner, args=(aqueue, request_data))
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

def datasetgenrunner(queue, request_data):
        try:
            import classify
            cl = classify.Classify()
            return cl.do_dataset_gen(queue, request_data)
        except:
            import sys,traceback
            traceback.print_exc(file=sys.stdout)
            print("\n")
            import random
            try:
                f = open("/tmp/outpt" + argstr() + str(random.randint(1000,9999)) + ".txt", "w")
                f.write(request_data if request_data is not None else "")
                traceback.print_exc(file=f)
                f.close()
            except Exception:
                pass
            memory = "CUDA out of memory" in traceback.format_exc()
            queue.put({"accuracy": None, "loss": None, "gpu" : hasgpu, "memory" : memory, "exception" : True })


@app.route('/datasetgen', methods=['POST'])
def do_dataset_gen():
    aqueue = Queue()
    request_data = request.get_data(as_text=True)
    # default result to avoid referenced-before-assignment warnings
    result = {"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }
    process = Process(target=datasetgenrunner, args=(aqueue, request_data))
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

@app.route('/filename', methods=['POST'])
def do_filename():
    def filenamerunner(queue, request):
        import classify
        cl = classify.Classify()
        return cl.do_filename(queue, request)
    # execute in current process for simplicity (original code returned filenamerunner(None, request))
    return filenamerunner(None, request)
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


def gptmidirunner(queue, request_data, filenames, cachedata):
    try:
        import classify
        cl = classify.Classify()
        cl.do_gptmidi(queue, request_data, filenames, cachedata)
    except:
        import sys, traceback
        memory = "CUDA error: out of memory" in traceback.format_exc()
        cudnn = "0 successful operations" in traceback.format_exc()
        queue.put(Response(json.dumps(
            {"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception": True,
             "gpu": hasgpu, "memory": memory, "cudnn": cudnn}), mimetype='application/json'))
        traceback.print_exc(file=sys.stdout)
        print("\n")
        import random
        try:
            f = open("/tmp/outpt" + argstr() + str(random.randint(1000, 9999)) + ".txt", "w")
            f.write(request_data if request_data is not None else "")
            traceback.print_exc(file=f)
            f.close()
        except Exception:
            pass


@app.route('/gptmidi/<ds>', methods=['POST'])
def do_gptmidi(ds):
    if False:
        from datetime import datetime
        dt = datetime.now()
        timestamp = dt.timestamp()
        data = cache.get(ds)
        print ("millis ", (dt.timestamp() - timestamp)*1000)
    else:
        data = None
    # Extract json/form and any uploaded files in parent process (these are not picklable)
    import classify
    cl_parent = classify.Classify()
    # prefer form['json'] if present
    try:
        myjson = request.form['json'] if 'json' in request.form else request.get_data(as_text=True)
    except Exception:
        myjson = request.get_data(as_text=True)
    # extract files using cl_parent.get_file which expects a Flask request; run here and pass filenames
    try:
        (filename, filename2) = cl_parent.get_file(request)
        filenames = [filename]
    except Exception:
        filenames = []

    aqueue = Queue()
    # ensure myjson and filenames are serializable strings/lists and set default result
    result = {"classifycatarray": None, "classifyprobarray": None, "accuracy": None, "loss": None, "exception" : True, "gpu" : hasgpu, "memory" : False, "cudnn" : False }

    process = Process(target=gptmidirunner, args=(aqueue, myjson, filenames, data))

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
#    queue = Queue()
#    process = Process(target=hasgpurunner, args=(queue, None))
#    process.start()
#    hasgpu = queue.get()
#    process.join()
    timeout = 60
    hasgpu = hasgpu()
    print("Has GPU", hasgpu)
    threaded = False
    if len(sys.argv) > 1 and (not hasgpu) and sys.argv[1] == 'multi':
        threaded = True
        print("Run threaded")
    port = argstr()
    print("Used port", port)
    try:
        port_int = int(port)
    except Exception:
        port_int = 80
    app.run(host='0.0.0.0', port=port_int, threaded=threaded)
