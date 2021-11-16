const { app, screen, BrowserWindow } = require('electron')
const path = require('path')
const { fileURLToPath, pathToFileURL, URL } = require('url')

app.on('web-contents-created', (_, contents) => {
    contents.on('will-navigate', (_, navigationUrl) => {
        if (navigationUrl !== pathToFileURL(path.join(__dirname, './ui/index.html')).href) {
            console.error('Attempt to navigate to an unknown URL. Abort!')
            process.abort()
        }
    })
    contents.setWindowOpenHandler(() => {
        console.error('Attempt to open a new window. Abort!')
        process.abort()
    })
})

app.whenReady().then(() => {
    const scaleFactor = screen.getPrimaryDisplay().scaleFactor

    const window = new BrowserWindow({
        width: 540,
        height: 330,
        resizable: false,
        alwaysOnTop: true,
        webPreferences: {
            preload: path.join(__dirname, './client_service.js'),
            images: false,
            webgl: false,
            disableDialogs: true,
            enableWebSQL: false,
            zoomFactor: 2 - scaleFactor
        }
    })

    window.setMenu(null)
    window.loadFile('ui/index.html')

    window.webContents.session.clearStorageData()
    window.webContents.session.webRequest.onBeforeRequest({ urls: ['<all_urls>'] }, (details, callback) => {
        const url = new URL(details.url)

        if (url.protocol === 'https:' || url.protocol === 'http:') {
            if (details.method !== 'POST') {
                console.error('Attempt to make a request with an unknown method. Abort!')
                process.abort()
            } else if (url.port !== '13949') {
                console.error('Attempt to make a request on a unknown port. Abort!')
                process.abort()
            } else if (url.pathname !== '/api/client/login') {
                console.error('Attempt to make a request with an unknown path. Abort!')
                process.abort()
            }
        } else if (url.protocol === 'file:') {
            if (!path.dirname(fileURLToPath(url)).startsWith(__dirname)) {
                console.error('Attempt to load a file from an unknown directory. Abort!')
                process.abort()
            }
        } else if (url.protocol !== 'devtools:') {
            console.error('Attempt to make a request with an unknown protocol. Abort!')
            process.abort()
        }

        callback({})
    })

    window.webContents.session.setPermissionRequestHandler(() => {
        console.error('Attempt to request permissions. Abort!')
        process.abort()
    })
    window.webContents.session.setDevicePermissionHandler(() => {
        console.error('Attempt to request device permissions. Abort!')
        process.abort()
    })

    window.webContents.setAudioMuted(true)
    window.webContents.setUserAgent(`pongccheatstcpclient/${require('./package.json').version}`)
})
