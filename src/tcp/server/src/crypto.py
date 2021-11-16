from Crypto.Cipher import ChaCha20_Poly1305
from Crypto.Random import get_random_bytes

def xchacha20_poly1305_encrypt(key, message):
    nonce = get_random_bytes(24)
    cipher = ChaCha20_Poly1305.new(key=key, nonce=nonce)
    ciphertext, tag = cipher.encrypt_and_digest(message)

    return ciphertext + tag + nonce
def xchacha20_poly1305_decrypt(key, encrypted):
    ciphertext = encrypted[:len(encrypted) - (16 + 24)]
    tag = encrypted[len(encrypted) - (16 + 24):len(encrypted) - 24]
    nonce = encrypted[len(encrypted) - 24:]

    cipher = ChaCha20_Poly1305.new(key=key, nonce=nonce)

    return cipher.decrypt_and_verify(ciphertext, tag)
