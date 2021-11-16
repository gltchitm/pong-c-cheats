express = require 'express'
{ customAlphabet } = require 'nanoid'
{ v4: uuidv4 } = require 'uuid'

app = express()

app.use express.json()

ALPHABET = 'abcdefghijklmnopqrstuvwxyz'

nanoid = customAlphabet ALPHABET, 10

username = nanoid()
password = nanoid()
token = uuidv4()

app.post '/api/client/login', ({ body }, res) ->
    if body.username == username and body.password == password
        res
            .status 200
            .json {
                message: null,
                token
            }
    else
        res
            .status 401
            .json {
                message: 'Incorrect username or password.',
                token: null
            }
app.post '/api/server/redeem_token', ({ body }, res) ->
    if body.token == token
        res
            .status 200
            .json { message: null }
    else
        res
            .status 403
            .json { message: 'Incorrect token!' }

app.listen 13949, ->
    console.log 'Authenticate with these credentials:'
    console.log 'Username:', username
    console.log 'Password:', password
