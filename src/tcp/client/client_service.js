const net = require('net')
const crypto = require('crypto')
const EventEmitter = require('events')
const { contextBridge } = require('electron')

const { XChaCha20Poly1305 } = require('./client_service/lib/native')
const { ClientReader, EncryptedClientReader } = require('./client_service/lib/client_reader')

const clientboundPackets = require('./client_service/lib/clientbound_packets')
const {
    ServerboundHelloPacket,
    ServerboundEncryptionResponsePacket,
    ServerboundAuthenticatePacket,
    ServerboundAttachPacket,
    ServerboundConnectPacket,
    ServerboundGetRightScorePacket,
    ServerboundGetLeftScorePacket,
    ServerboundChangeLeftScorePacket,
    ServerboundChangeRightScorePacket,
    ServerboundDetachPacket
} = require('./client_service/lib/serverbound_packets')


let client = null
let encryptionKey = null
let socketError = null
let loginToken = null
let encryptionEnabled = false

const RENDERERBOUND_EVENTS = [
    'initialized',
    'authenticated',
    'disconnected',
    'attached',
    'leftScore',
    'rightScore',
    'scoreChanged',
    'detached'
]
const rendererBoundEventListeners = Object.fromEntries(RENDERERBOUND_EVENTS.map(event => [event, null]))

const tearClientDown = () => {
    client.destroy()

    encryptionKey = null
    socketError = null
    loginToken = null
    encryptionEnabled = false
    client = null
}

contextBridge.exposeInMainWorld(
    'ClientService',
    {
        init(serverAddress) {
            if (typeof serverAddress !== 'string') {
                throw new TypeError('serverAddress should be a string!')
            } else if (client) {
                throw new Error('Already initialized!')
            }

            client = new net.Socket()

            const clientEmitter = new EventEmitter()

            clientEmitter.on('encryptionRequest', ({ publicKey, encryptionTestValue }) => {
                encryptionKey = crypto.randomBytes(32)

                new ServerboundEncryptionResponsePacket(
                    client,
                    crypto.publicEncrypt(publicKey, encryptionKey),
                    XChaCha20Poly1305.encrypt(encryptionKey, encryptionTestValue)
                )
            })
            clientEmitter.on('enableEncryption', () => {
                encryptionEnabled = true
            })
            clientEmitter.on('authenticateNow', () => {
                new ServerboundAuthenticatePacket(client, encryptionKey, loginToken)
            })
            clientEmitter.on('authenticated', () => {
                rendererBoundEventListeners.authenticated?.()
            })
            clientEmitter.on('connectResponse', ({ error }) => {
                if (error) {
                    rendererBoundEventListeners.attached?.(error)
                } else {
                    new ServerboundAttachPacket(client, encryptionKey)
                }
            })
            clientEmitter.on('attachResponse', ({ error }) => {
                rendererBoundEventListeners.attached?.(error)
            })
            clientEmitter.on('getLeftScoreResponse', ({ error, score }) => {
                rendererBoundEventListeners.leftScore?.(error, score)
            })
            clientEmitter.on('getRightScoreResponse', ({ error, score }) => {
                rendererBoundEventListeners.rightScore?.(error, score)
            })
            clientEmitter.on('changeLeftScoreResponse', ({ error }) => {
                rendererBoundEventListeners.scoreChanged?.(error)
            })
            clientEmitter.on('changeRightScoreResponse', ({ error }) => {
                rendererBoundEventListeners.scoreChanged?.(error)
            })
            clientEmitter.on('detachResponse', () => {
                rendererBoundEventListeners.detached?.()
            })

            client.on('connect', async () => {
                rendererBoundEventListeners.initialized?.()

                try {
                    while (true) {
                        let reader

                        if (encryptionEnabled) {
                            reader = await EncryptedClientReader.from(client, encryptionKey)
                        } else {
                            reader = new ClientReader(client)
                        }

                        const packetId = await reader.readUInt16()

                        for (const packetClass of Object.values(clientboundPackets)) {
                            if (packetClass.PACKET_ID === packetId) {
                                if (packetClass === clientboundPackets.ClientboundKickPacket) {
                                    rendererBoundEventListeners.disconnected?.((await packetClass.read(reader)).reason)

                                    tearClientDown()

                                    return
                                } else {
                                    const packet = await packetClass.read(reader)

                                    clientEmitter.emit(
                                        packetClass.name
                                            .replace(/^Clientbound/, '')
                                            .replace(/Packet$/, '')
                                            .replace(/./, character => character.toLowerCase()),
                                        packet
                                    )
                                }
                            }
                        }
                    }
                } catch ({ message }) {
                    rendererBoundEventListeners.disconnected?.(socketError ?? message)
                    tearClientDown()
                }
            })

            return new Promise((res, rej) => {
                const connectionErrorHandler = ({ message }) => {
                    tearClientDown()
                    rej(new Error(message))
                }
                client.on('error', connectionErrorHandler)
                client.connect(13950, serverAddress, () => {
                    client.off('error', connectionErrorHandler)
                    client.on('error', ({ message }) => {
                        socketError = message
                    })
                    res()
                })
            })
        },
        login(token) {
            if (typeof token !== 'string') {
                throw new TypeError('token should be a string!')
            }

            loginToken = token

            new ServerboundHelloPacket(client)
        },
        attach() {
            new ServerboundConnectPacket(client, encryptionKey)
        },
        getLeftScore() {
            new ServerboundGetLeftScorePacket(client, encryptionKey)
        },
        getRightScore() {
            new ServerboundGetRightScorePacket(client, encryptionKey)
        },
        changeLeftScore(score) {
            new ServerboundChangeLeftScorePacket(client, encryptionKey, score)
        },
        changeRightScore(score) {
            new ServerboundChangeRightScorePacket(client, encryptionKey, score)
        },
        detach() {
            new ServerboundDetachPacket(client, encryptionKey)
        },
        disconnect() {
            tearClientDown()
            rendererBoundEventListeners.disconnected?.()
        },
        events: {
            on(eventName, listener) {
                if (!rendererBoundEventListeners.hasOwnProperty(eventName)) {
                    throw new ReferenceError('Unknown event name.')
                } else if (rendererBoundEventListeners[eventName] !== null) {
                    throw new TypeError('Event listener is already set!')
                } else if (typeof listener !== 'function') {
                    throw new TypeError('Event listener should be a function.')
                }

                rendererBoundEventListeners[eventName] = listener
            }
        }
    }
)
