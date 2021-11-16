const { XChaCha20Poly1305 } = require('./native')
const { SIZEOF_INT_16, SIZEOF_BOOL, SIZEOF_INT_32 } = require('./sizeof')

class ClientReader {
    constructor(client) {
        this.client = client
    }
    async readBytes(bytes) {
        let buffer = Buffer.alloc(0)

        let readBytes = 0
        while (readBytes < bytes) {
            const byte = this.client.read(1)

            if (byte === null) {
                if (this.client.readyState === 'closed') {
                    throw new Error('Connection closed by peer.')
                }

                await new Promise(res => this.client.once('readable', res))
            } else {
                buffer = Buffer.concat([buffer, byte])
                readBytes++
            }
        }

        return buffer
    }
    async readBuffer() {
        return (await this.readBytes(await this.readUInt16()))
    }
    async readUInt16() {
        return (await this.readBytes(SIZEOF_INT_16)).readUInt16BE()
    }
    async readUInt32() {
        return (await this.readBytes(SIZEOF_INT_32)).readUInt32BE()
    }
    async readString() {
        return (await this.readBuffer()).toString('utf-8')
    }
    async readBool() {
        return Boolean((await this.readBytes(SIZEOF_BOOL))[0])
    }
}
class EncryptedClientReader extends ClientReader {
    constructor(buffer) {
        super()

        this.buffer = buffer
        this.offset = 0
    }
    static async from(client, encryptionKey) {
        const reader = new ClientReader(client)

        const encrypted = await reader.readBuffer()

        return new EncryptedClientReader(XChaCha20Poly1305.decrypt(encryptionKey, encrypted))
    }
    readBytes(bytes) {
        const slice = this.buffer.slice(this.offset, this.offset + bytes)

        this.offset += bytes

        return slice
    }
}

module.exports = { ClientReader, EncryptedClientReader }
