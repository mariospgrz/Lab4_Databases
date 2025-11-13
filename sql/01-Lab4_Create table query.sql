#Marios Andreou 27753 Vafeiadis Paraskevas 27407 Andreas Yianni 27944 Adonis Anastasiou 30804
CREATE DATABASE IF NOT EXISTS lab4_db;
USE lab4_db;

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


ALTER TABLE players #Adding the uniquness and not null constrains 
	MODIFY username VARCHAR(50) NOT NULL,
    MODIFY email VARCHAR(100) NOT NULL,
    MODIFY registration_date DATE NOT NULL,
    MODIFY total_wins INT NOT NULL DEFAULT 0,  #both start at 0 and must not be null
    MODIFY total_losses INT NOT NULL DEFAULT 0;
    
 ALTER TABLE players
	ADD CONSTRAINT unique_players_username UNIQUE (username),
    ADD CONSTRAINT unique_players_email UNIQUE (email);
    
ALTER TABLE players
	ADD CONSTRAINT NoNeg_players_wins CHECK (total_wins>=0),
    ADD CONSTRAINT NoNeg_players_losses CHECK (total_losses>=0);

ALTER TABLE tournaments #Modifing again as before 
  MODIFY name VARCHAR(100) NOT NULL,
  MODIFY start_date DATE NOT NULL,
  MODIFY max_participants INT NOT NULL,
  MODIFY is_open BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE tournaments
  ADD CONSTRAINT tournaments_maxpart CHECK (max_participants > 0);
  
ALTER TABLE participants
MODIFY player_id INT NOT NULL, #player_id thelw na uparxh panta
MODIFY tournament_id INT NOT NULL, #tournament_Id thelw na uparxh panta 
MODIFY registration_date DATE NOT NULL, #registration_date thelw na upatch panta
ADD UNIQUE (player_id,tournament_id); #thelw to combination tou player kai tou tournament na einai unique diladi o idios player na mhn kani register 2 fores sto idio tournament;

#Dropping the constrains on the foreign keys to modify them , make the changes
ALTER TABLE matches
DROP FOREIGN KEY matches_ibfk_1,
DROP FOREIGN KEY matches_ibfk_2,
DROP FOREIGN KEY matches_ibfk_3,
MODIFY tournament_id INT NOT NULL,
MODIFY player1_id INT NOT NULL,
MODIFY player2_id INT NOT NULL,
ADD CONSTRAINT ScoreNotNeg CHECK (player1_score >= 0 AND player2_score >= 0);

DELIMITER // #had to make the trigger to check if the players matched because the constrained we used in the alter table keptmaking errors in the later steps of TASK2
		         #so we used chatgpt for insight on this trigger query
CREATE TRIGGER PlayersMatched
BEFORE INSERT ON matches FOR EACH ROW
BEGIN IF NEW.player1_id = NEW.player2_id THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Players Can not be the same'; END IF;
END//

DELIMITER ;

#Readd the foreign key constrains that were already added
ALTER TABLE matches
ADD CONSTRAINT matches_ibfk_1 FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id),
ADD CONSTRAINT matches_ibfk_2 FOREIGN KEY (player1_id) REFERENCES players(player_id),
ADD CONSTRAINT matches_ibfk_3 FOREIGN KEY (player2_id) REFERENCES players(player_id);

-- Κάνουμε drop τα παλιά foreign keys, γιατί η MYSQL δεν επιτρέπει να αλλάξεις  ON DELETE/UPDATE 
-- αν δεν κάνεις πρώτα DROP το foreign key.
ALTER TABLE participants
	DROP FOREIGN KEY participants_ibfk_1,
	DROP FOREIGN KEY participants_ibfk_2;
    
-- Ξαναδημιουργώ τα foreign keys.
ALTER TABLE participants
	ADD CONSTRAINT UP_DEL_participants_players FOREIGN KEY (player_id) REFERENCES players(player_id)
    ON DELETE CASCADE -- Αν σβηστεί παίκτης / σβήνονται οι συμμετοχές του
    ON UPDATE CASCADE, -- Αν αλλάξει το player_id / Ενημερώνονται οι συμμετοχές
ADD CONSTRAINT UP_DEL_participants_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id)
    ON DELETE CASCADE -- Αν σβήστεί το τουρνουά / σβήνονται και οι συμμετοχές του. 
    ON UPDATE CASCADE; -- Αν αλλάξει το tournament_id / ενημερώνονται και οι συμμετοχές.
    
-- MACTHES TABLE drop FKs
ALTER TABLE matches
	DROP foreign key matches_ibfk_1,
    DROP foreign key matches_ibfk_2,
	DROP foreign key matches_ibfk_3,
	DROP foreign key matches_ibfk_4;

-- ADD NEW FKs with contraints
ALTER TABLE matches
  ADD CONSTRAINT UP_DEL_matches_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT UP_DEL_matches_player1 FOREIGN KEY (player1_id) REFERENCES players(player_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT UP_DEL_matches_player2 FOREIGN KEY (player2_id) REFERENCES players(player_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT UP_DEL_matches_winner FOREIGN KEY (winner_id) REFERENCES players(player_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;     
     
    