const { XChaCha20Poly1305 } = require('./native')
const { SIZEOF_INT_16, SIZEOF_INT_32 } = require('./sizeof')

class BufferWriter {
    constructor() {
        this.buffer = Buffer.alloc(0)
    }
    writeUInt16(data) {
        const buffer = Buffer.alloc(SIZEOF_INT_16)
        buffer.writeUInt16BE(data)
        this.buffer = Buffer.concat([this.buffer, buffer])
    }
    writeUInt32(data) {
        const buffer = Buffer.alloc(SIZEOF_INT_32)
        buffer.writeUInt32BE(data)
        this.buffer = Buffer.concat([this.buffer, buffer])
    }
    writeString(data) {
        this.writeUInt16(data.length)
        const buffer = Buffer.alloc(data.length)
        buffer.write(data)
        this.buffer = Buffer.concat([this.buffer, buffer])
    }
    writeBuffer(data) {
        this.writeUInt16(data.length)
        this.buffer = Buffer.concat([this.buffer, data])
    }
    finish() {
        return this.buffer
    }
}
class EncryptedBufferWriter extends BufferWriter {
    constructor(encryptionKey) {
        super()

        this.encryptionKey = encryptionKey
    }
    finish() {
        const encrypted = XChaCha20Poly1305.encrypt(this.encryptionKey, this.buffer)

        const writer = new BufferWriter()
        writer.writeBuffer(encrypted)

        return writer.finish()
    }
}

module.exports = { BufferWriter, EncryptedBufferWriter }
