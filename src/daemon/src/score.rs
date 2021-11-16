pub struct Score {
    pub left_score: i32,
    pub right_score: i32
}

impl Score {
    pub fn new(left_score: i32, right_score: i32) -> Self {
        Self { left_score, right_score }
    }
}
