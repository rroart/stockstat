#!/usr/bin/python3

from nameko.web.handlers import http

import classify
import predict

cl = classify.Classify()
pr = predict.Predict()

class HttpService:
    name = "http_service"
    @http('POST', '/eval')
    def do_eval(self, request):
        return cl.do_eval(request)

    @http('POST', '/classify')
    def do_classify(self, request):
        return cl.do_classify(request)

    @http('POST', '/learntest')
    def do_learntest(self, request):
        return cl.do_learntest(request)

    @http('POST', '/predictone')
    def do_learntestpredictone(self, request):
        return pr.do_learntest(request)

    @http('POST', '/predict')
    def do_learntestpredict():
        return pr.do_learntestlist(request)

