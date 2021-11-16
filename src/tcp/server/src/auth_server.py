from json import dumps

import requests

def redeem_token(token):
    response = requests.post(
        'http://localhost:13949/api/server/redeem_token',
        data=dumps({'token': token}),
        headers={
            'User-Agent': 'pongccheatstcpserver/1.0.0',
            'Content-Type': 'application/json'
        }
    )

    return response.status_code == requests.codes.ok
