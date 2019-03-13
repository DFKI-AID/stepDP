import requests

while True:
    text = input("intent: ")
    payload = {'intent': text}
    
    r = requests.post("http://localhost:50000/input/intent", json=payload)
    print(r.status_code, r.reason)
