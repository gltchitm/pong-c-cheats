pub mod serverbound_packets {
    pub const ATTACH: i8 = 0;
    pub const CHANGE_LEFT_SCORE: i8 = 1;
    pub const CHANGE_RIGHT_SCORE: i8 = 2;
    pub const GET_LEFT_SCORE: i8 = 3;
    pub const GET_RIGHT_SCORE: i8 = 4;
    pub const DETACH: i8 = 5;
    pub const DETACH_AND_EXIT: i8 = 6;
}

pub mod clientbound_packets {
    pub const OK: i8 = 0;
    pub const LEFT_SCORE: i8 = 1;
    pub const RIGHT_SCORE: i8 = 2;
    pub const BUSY: i8 = 3;
    pub const ALREADY_ATTACHED: i8 = 4;
    pub const NOT_ATTACHED: i8 = 5;
    pub const NOT_FOUND: i8 = 6;
    pub const MALFORMED_PACKET: i8 = 7;
}

pub mod to_exploit_packets {
    pub const CHANGE_LEFT_SCORE: i8 = 0;
    pub const CHANGE_RIGHT_SCORE: i8 = 1;
    pub const GET_LEFT_SCORE: i8 = 2;
    pub const GET_RIGHT_SCORE: i8 = 3;
    pub const DETACH: i8 = 4;
    pub const DETACH_AND_EXIT: i8 = 5;
}

pub mod to_main_packets {
    pub const LEFT_SCORE: i8 = 0;
    pub const RIGHT_SCORE: i8 = 1;
    pub const GAME_EXITED: i8 = 2;
    pub const DETACHED: i8 = 3;
}
