use napi_derive::*;
use napi::*;

use chacha20poly1305::{XChaCha20Poly1305, XNonce, Key};
use chacha20poly1305::aead::{Aead, NewAead};

use getrandom::getrandom;

#[js_function(2)]
pub fn encrypt(ctx: CallContext) -> Result<JsBuffer> {
    let raw_key = ctx.get::<napi::JsBuffer>(0).unwrap().into_value().unwrap();
    let raw_message = ctx.get::<napi::JsBuffer>(1).unwrap().into_value().unwrap();

    let key = Key::from_slice(raw_key.as_ref());

    let cipher = XChaCha20Poly1305::new(key);

    let mut nonce_bytes = [0u8; 24];
    getrandom(&mut nonce_bytes).unwrap();

    let nonce = XNonce::from_mut_slice(&mut nonce_bytes);
    let mut encrypted = cipher.encrypt(nonce, raw_message.as_ref()).unwrap();

    encrypted.extend_from_slice(&nonce_bytes);

    Ok(ctx.env.create_buffer_with_data(encrypted).unwrap().into_raw())
}

#[js_function(2)]
pub fn decrypt(ctx: CallContext) -> Result<JsBuffer> {
    let raw_key = ctx.get::<napi::JsBuffer>(0).unwrap().into_value().unwrap();
    let raw_data = ctx.get::<napi::JsBuffer>(1).unwrap().into_value().unwrap();

    let encrypted = raw_data.as_ref();

    let ciphertext_and_tag = &encrypted[..encrypted.len() - 24];
    let nonce = &encrypted[encrypted.len() - 24..];

    let key = Key::from_slice(raw_key.as_ref());
    let cipher = XChaCha20Poly1305::new(key);

    let nonce = XNonce::from_slice(nonce);

    let decrypted = cipher.decrypt(nonce, ciphertext_and_tag).unwrap();

    Ok(ctx.env.create_buffer_with_data(decrypted).unwrap().into_raw())
}

#[module_exports]
fn init(mut exports: JsObject, env: Env) -> Result<()> {
    let mut xchacha20_poly1305 = env.create_object().unwrap();

    xchacha20_poly1305.create_named_method("encrypt", encrypt).unwrap();
    xchacha20_poly1305.create_named_method("decrypt", decrypt).unwrap();

    exports.set_named_property("XChaCha20Poly1305", xchacha20_poly1305).unwrap();

    Ok(())
}
