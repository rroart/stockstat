import requests
import os

aport = os.environ.get('MYAPORT')
if aport is None:
    aport = "80"

ahost = os.environ.get('MYAHOST')
if ahost is None:
    ahost = "localhost"
    
imgaport = os.environ.get('MYIMGAPORT')
if imgaport is None:
    imgaport = "80"

imgahost = os.environ.get('MYIMGAHOST')
if imgahost is None:
    imgahost = "localhost"
    
url1 = 'http://' + ahost + ':' + aport + '/action/simulateinvest'
url2 = 'http://' + ahost + ':' + aport + '/action/improvesimulateinvest'
url3 = 'http://' + ahost + ':' + aport + '/action/autosimulateinvest'
url4 = 'http://' + ahost + ':' + aport + '/action/improveautosimulateinvest'
url5 = 'http://' + ahost + ':' + aport + '/event/pause'
url6 = 'http://' + ahost + ':' + aport + '/event/continue'
url7 = 'http://' + ahost + ':' + aport + '/gettasks'
url8 = 'http://' + ahost + ':' + aport + '/db/update/start'
url9 = 'http://' + ahost + ':' + aport + '/db/update/end'
url10 = 'http://' + ahost + ':' + aport + '/cache/invalidate'
url11 = 'http://' + ahost + ':' + aport + '/copy/'

imgurl1 = 'http://' + imgahost + ':' + imgaport + '/datasetgen'
imgurl2 = 'http://' + imgahost + ':' + imgaport + '/download'
imgurl3 = 'http://' + imgahost + ':' + imgaport + '/dataset'
imgurl4 = 'http://' + imgahost + ':' + imgaport + '/imgclassify'

#headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
#headers={'Content-type':'application/json', 'Accept':'application/json'}
headers={'Content-Type' : 'application/json;charset=utf-8'}
headers2={'Content-Type' : 'multipart/form-data;charset=utf-8'}
def request1(market, data):
    return requests.post(url1 + '/market/' + str(market), json=data, headers=headers)

def request2(market, data):
    return requests.post(url2 + '/market/' + str(market), json=data, headers=headers)

def request3(market, data):
    return requests.post(url3 + '/market/' + str(market), json=data, headers=headers)

def request4(market, data):
    return requests.post(url4 + '/market/' + str(market), json=data, headers=headers)

def requestpause():
    return requests.post(url5, headers=headers)

def requestcontinue():
    return requests.post(url6, headers=headers)

def requestgettasks():
    return requests.post(url7, headers=headers)

def request0(data):
    return requests.post(url, data='', headers=headers)
    #return requests.post(url, data=json.dumps(data), headers=headers)

def dbupdatestart():
    return requests.post(url8, headers=headers)

def dbupdateend():
    return requests.post(url9, headers=headers)

def cacheinvalidate():
    return requests.post(url10, headers=headers)

def copydb(indb, outdb):
    return requests.post(url11 + indb + "/" + outdb, headers=headers)

def imgrequest1(market, files):
    #return requests.post(imgurl1, json=data, headers=headers2, files=files)
    return requests.post(imgurl1, files=files)

def imgrequest2(market, afile):
    return requests.get(imgurl2 + "/" + afile)

def imgrequest3(market, data):
    return requests.post(imgurl3, json=data, headers=headers)

def imgrequest4(market, files):
    return requests.post(imgurl4, files=files)

