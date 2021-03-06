import requests
import os

aport = os.environ.get('MYAPORT')
if aport is None:
    aport = "80"

url1 = 'http://localhost:' + aport + '/action/simulateinvest'
url2 = 'http://localhost:' + aport + '/action/improvesimulateinvest'

#headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
#headers={'Content-type':'application/json', 'Accept':'application/json'}
headers={'Content-Type' : 'application/json;charset=utf-8'}
def request1(market, data):
    return requests.post(url1 + '/market/' + str(market), json=data, headers=headers)

def request2(market, data):
    return requests.post(url2 + '/market/' + str(market), json=data, headers=headers)

def request0(data):
    return requests.post(url, data='', headers=headers)
    #return requests.post(url, data=json.dumps(data), headers=headers)

