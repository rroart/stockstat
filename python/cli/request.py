import requests
import os

aport = os.environ.get('MYAPORT')
if aport is None:
    aport = "80"

ahost = os.environ.get('MYAHOST')
if ahost is None:
    ahost = "localhost"
    
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

#headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
#headers={'Content-type':'application/json', 'Accept':'application/json'}
headers={'Content-Type' : 'application/json;charset=utf-8'}
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

