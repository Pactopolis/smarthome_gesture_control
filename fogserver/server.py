from flask import Flask, json, request
from werkzeug.utils import secure_filename
app = Flask(__name__)

@app.route('/testget', methods=['GET'])
def testget():
    res = {'response': 200, 'value': 'Hello, World!'}
    return res

@app.route('/testpost', methods=['POST'])
def testpost():
    data = request.json
    print(data)
    res = {'response': 200}
    return res

@app.route('/api/v1/upload', methods=['POST'])
def upload():
    key = ''
    for x in request.files:
        key = x
    
    value = request.files[key]
    filename = value.filename
    value.save(secure_filename(filename))

    res = {'response': 200}
    return res

if __name__ == '__main__':
    app.run()