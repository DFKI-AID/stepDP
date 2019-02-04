import requests

while True:
    text = input("asr: ")
    payload = {'text': text}
    
    r = requests.post("http://localhost:50000/input/asr", json=payload)
    print(r.status_code, r.reason)
