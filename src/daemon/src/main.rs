mod packet;
mod score;
mod exploit;
mod packet_id;

use std::sync::{Arc, Mutex};
use std::os::unix::net::UnixListener;
use std::io::{BufRead, BufReader, BufWriter, ErrorKind::NotFound};
use std::io::prelude::*;
use std::fs::remove_file;
use std::env::var;
use std::panic;

use nix::unistd::{getuid, chown, Uid, Gid};

use packet_id::{serverbound_packets, clientbound_packets, to_exploit_packets, to_main_packets};
use packet::{ClientboundPacket, ToExploitPacket};
use exploit::Exploit;

const SOCKET_PATH: &str = "/tmp/pongccheatsd.sock";

fn main() {
    #[cfg(not(target_os = "linux"))]
    compile_error!("only linux is supported!");

    if getuid() != Uid::from_raw(0) || var("SUDO_UID").is_err() || var("SUDO_GID").is_err() {
        panic!("this program must be run as root using sudo!");
    }

    if let Err(err) = remove_file(SOCKET_PATH) {
        if err.kind() != NotFound {
            panic!("{}", err);
        }
    }

    let socket = UnixListener::bind(SOCKET_PATH).unwrap();

    chown(
        SOCKET_PATH,
        Some(Uid::from_raw(var("SUDO_UID").unwrap().parse::<u32>().unwrap())),
        Some(Gid::from_raw(var("SUDO_GID").unwrap().parse::<u32>().unwrap()))
    ).unwrap();

    let busy = Arc::new(Mutex::new(false));

    for stream in socket.incoming() {
        match stream {
            Ok(stream) => {
                if *busy.lock().unwrap() {
                    std::thread::spawn(move || {
                        let reader = BufReader::new(&stream);
                        let mut writer = BufWriter::new(&stream);

                        for line in reader.lines() {
                            let mut response = ClientboundPacket::new(clientbound_packets::BUSY, None);

                            if serde_json::from_str::<packet::ServerboundPacket>(&line.unwrap()).is_err() {
                                response = ClientboundPacket::new(clientbound_packets::MALFORMED_PACKET, None);
                            }

                            writer.write_all(serde_json::to_string(&response).unwrap().as_bytes()).unwrap();
                            writer.write_all("\n".as_bytes()).unwrap();
                            writer.flush().unwrap();

                            stream.shutdown(std::net::Shutdown::Both).unwrap();
                        }
                    });
                } else {
                    let busy = busy.clone();

                    std::thread::spawn(move || {
                        let result = panic::catch_unwind(|| {
                            {
                                let mut busy = busy.lock().unwrap();
                                *busy = true;
                            }

                            let mut exploit = Exploit::new();

                            let reader = BufReader::new(&stream);
                            let mut writer = BufWriter::new(&stream);

                            for line in reader.lines() {
                                let mut response = ClientboundPacket::new(clientbound_packets::OK, None);

                                if exploit.attached {
                                    if let Ok(packet) = exploit.to_main_receiver.as_ref().unwrap().try_recv() {
                                        if packet.id == to_main_packets::GAME_EXITED {
                                            exploit = Exploit::new();

                                            response = ClientboundPacket::new(clientbound_packets::NOT_ATTACHED, None);

                                            writer.write_all(serde_json::to_string(&response).unwrap().as_bytes()).unwrap();
                                            writer.write_all("\n".as_bytes()).unwrap();
                                            writer.flush().unwrap();

                                            let mut busy = busy.lock().unwrap();
                                            *busy = false;

                                            continue;
                                        }
                                    }
                                }

                                match serde_json::from_str::<packet::ServerboundPacket>(&line.unwrap()) {
                                    Ok(packet) => {
                                        match packet.id {
                                            serverbound_packets::ATTACH => {
                                                if exploit.attached {
                                                    response = ClientboundPacket::new(clientbound_packets::ALREADY_ATTACHED, None);
                                                } else if exploit.attach().is_err(){
                                                    response = ClientboundPacket::new(clientbound_packets::NOT_FOUND, None);
                                                }
                                            },
                                            serverbound_packets::CHANGE_LEFT_SCORE | serverbound_packets::CHANGE_RIGHT_SCORE => {
                                                if packet.score.is_none() || packet.score.unwrap() > 999_999 || packet.score.unwrap() < 0 {
                                                    response = ClientboundPacket::new(clientbound_packets::MALFORMED_PACKET, None);
                                                } else if !exploit.attached {
                                                    response = ClientboundPacket::new(clientbound_packets::NOT_ATTACHED, None);
                                                } else if packet.id == serverbound_packets::CHANGE_LEFT_SCORE {
                                                    exploit.to_exploit_sender.as_ref().unwrap().send(
                                                        ToExploitPacket::new(
                                                            to_exploit_packets::CHANGE_LEFT_SCORE,
                                                            Some(packet.score.unwrap())
                                                        )
                                                    ).unwrap();
                                                } else {
                                                    exploit.to_exploit_sender.as_ref().unwrap().send(
                                                        ToExploitPacket::new(
                                                            to_exploit_packets::CHANGE_RIGHT_SCORE,
                                                            Some(packet.score.unwrap())
                                                        )
                                                    ).unwrap();
                                                }
                                            },
                                            serverbound_packets::GET_LEFT_SCORE => {
                                                exploit.to_exploit_sender.as_ref().unwrap().send(
                                                    ToExploitPacket::new(
                                                        to_exploit_packets::GET_LEFT_SCORE,
                                                        None
                                                    )
                                                ).unwrap();

                                                match exploit.to_main_receiver.as_ref().unwrap().recv() {
                                                    Ok(packet) => {
                                                        response = ClientboundPacket::new(
                                                            clientbound_packets::LEFT_SCORE,
                                                            Some(packet.score.unwrap())
                                                        );
                                                    },
                                                    Err(err) => panic!("{}", err)
                                                }
                                            },
                                            serverbound_packets::GET_RIGHT_SCORE => {
                                                exploit.to_exploit_sender.as_ref().unwrap().send(
                                                    ToExploitPacket::new(
                                                        to_exploit_packets::GET_RIGHT_SCORE,
                                                        None
                                                    )
                                                ).unwrap();

                                                match exploit.to_main_receiver.as_ref().unwrap().recv() {
                                                    Ok(packet) => {
                                                        response = ClientboundPacket::new(
                                                            clientbound_packets::RIGHT_SCORE,
                                                            Some(packet.score.unwrap())
                                                        );
                                                    },
                                                    Err(err) => panic!("{}", err)
                                                }
                                            },
                                            serverbound_packets::DETACH => {
                                                if exploit.attached {
                                                    exploit.to_exploit_sender.as_ref().unwrap().send(
                                                        ToExploitPacket::new(
                                                            to_exploit_packets::DETACH,
                                                            None
                                                        )
                                                    ).unwrap();

                                                    if let Ok(packet) = exploit.to_main_receiver.as_ref().unwrap().recv() {
                                                        if packet.id == to_main_packets::DETACHED {
                                                            exploit = Exploit::new();

                                                            let mut busy = busy.lock().unwrap();
                                                            *busy = false;
                                                        }
                                                    }
                                                } else {
                                                    response = ClientboundPacket::new(clientbound_packets::NOT_ATTACHED, None);
                                                }
                                            },
                                            serverbound_packets::DETACH_AND_EXIT => {
                                                if exploit.attached {
                                                    exploit.to_exploit_sender.as_ref().unwrap().send(
                                                        ToExploitPacket::new(
                                                            to_exploit_packets::DETACH_AND_EXIT,
                                                            None
                                                        )
                                                    ).unwrap();

                                                    if let Ok(packet) = exploit.to_main_receiver.as_ref().unwrap().recv() {
                                                        if packet.id == to_main_packets::DETACHED {
                                                            exploit = Exploit::new();

                                                            let mut busy = busy.lock().unwrap();
                                                            *busy = false;
                                                        }
                                                    }
                                                } else {
                                                    response = ClientboundPacket::new(clientbound_packets::NOT_ATTACHED, None);
                                                }
                                            },
                                            _ => {
                                                response = ClientboundPacket::new(clientbound_packets::MALFORMED_PACKET, None);
                                            }
                                        }
                                    },
                                    Err(_) => {
                                        response = ClientboundPacket::new(clientbound_packets::MALFORMED_PACKET, None);
                                    }
                                }
                                writer.write_all(serde_json::to_string(&response).unwrap().as_bytes()).unwrap();
                                writer.write_all("\n".as_bytes()).unwrap();
                                writer.flush().unwrap();
                            }

                            if exploit.attached {
                                exploit.to_exploit_sender.as_ref().unwrap().send(
                                    ToExploitPacket::new(
                                        to_exploit_packets::DETACH,
                                        None
                                    )
                                ).unwrap();
                            }

                            let mut busy = busy.lock().unwrap();
                            *busy = false;
                        });

                        if result.is_err() {
                            let mut writer = BufWriter::new(&stream);

                            let response = ClientboundPacket::new(clientbound_packets::NOT_ATTACHED, None);

                            writer.write_all(serde_json::to_string(&response).unwrap().as_bytes()).unwrap();
                            writer.write_all("\n".as_bytes()).unwrap();
                            writer.flush().unwrap();

                            stream.shutdown(std::net::Shutdown::Both).unwrap();

                            let mut busy = busy.lock().unwrap();
                            *busy = false;
                        }
                    });
                }
            },
            Err(err) => {
                panic!("{}", err);
            }
        }
    }
}
