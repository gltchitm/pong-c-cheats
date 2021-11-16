use serde::{Serialize, Deserialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct ServerboundPacket {
    pub id: i8,
    pub score: Option<i32>
}

#[derive(Serialize, Deserialize)]
pub struct ClientboundPacket {
    pub error: i8,
    pub score: Option<i32>
}

impl ClientboundPacket {
    pub fn new(error: i8, score: Option<i32>) -> Self {
        Self { error, score }
    }
}

pub struct ToExploitPacket {
    pub id: i8,
    pub score: Option<i32>
}

impl ToExploitPacket {
    pub fn new(id: i8, score: Option<i32>) -> Self {
        Self { id, score }
    }
}

#[derive(Debug)]
pub struct ToMainPacket {
    pub id: i8,
    pub score: Option<i32>
}

impl ToMainPacket {
    pub fn new(id: i8, score: Option<i32>) -> Self {
        Self { id, score }
    }
}
