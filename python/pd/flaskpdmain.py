#!/usr/bin/python3

from flask import Flask, request

app = Flask(__name__)

import datareader

dr = datareader.Datareader()

@app.route('/learntestclassify', methods=['POST'])
def do_learntestclassify():
    return dr.do_datareader(request)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8001)
