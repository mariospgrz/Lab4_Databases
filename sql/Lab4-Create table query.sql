-- Players table
CREATE TABLE players (
    player_id INT PRIMARY KEY,
    username VARCHAR(50),
    email VARCHAR(100),
    registration_date DATE,
    total_wins INT,
    total_losses INT
);

-- Tournaments table
CREATE TABLE tournaments (
    tournament_id INT PRIMARY KEY,
    name VARCHAR(100),
    start_date DATE,
    end_date DATE,
    max_participants INT,
    is_open BOOLEAN DEFAULT TRUE
);

-- Participants table (many-to-many: players to tournaments)
CREATE TABLE participants (
    participant_id INT PRIMARY KEY,
    player_id INT,
    tournament_id INT,
    registration_date DATE,
    final_rank INT,
    FOREIGN KEY (player_id) REFERENCES players(player_id),
    FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id)
);

-- Matches table
CREATE TABLE matches (
    match_id INT PRIMARY KEY,
    tournament_id INT,
    player1_id INT,
    player2_id INT,
    match_date DATE,
    player1_score INT,
    player2_score INT,
    winner_id INT,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id),
    FOREIGN KEY (player1_id) REFERENCES players(player_id),
    FOREIGN KEY (player2_id) REFERENCES players(player_id),
    FOREIGN KEY (winner_id) REFERENCES players(player_id)
);