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
    print('A: upload() called')

    key = ''
    for x in request.files:
        print('B:', x)
        key = x
    
    value = request.files[key]
    print('C: ', type(value))

    filename = value.filename
    print('D: ' + filename)

    value.save(secure_filename(filename))
    print('E: Saved!')

    res = {'response': 200}
    return res

if __name__ == '__main__':
    app.run()