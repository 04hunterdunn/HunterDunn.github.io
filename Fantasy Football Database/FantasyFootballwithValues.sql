--
-- File generated with SQLiteStudio v3.4.4 on Wed Dec 4 15:39:27 2024
--
-- Text encoding used: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Table: FantasyPlayers
CREATE TABLE IF NOT EXISTS FantasyPlayers (
 player_id INTEGER PRIMARY KEY,
 player_name TEXT,
 Position TEXT,
 NFL_team_id TEXT,
 FOREIGN KEY (NFL_team_id) REFERENCES NFL_teams(NFL_team_id)
);
INSERT INTO FantasyPlayers (player_id, player_name, Position, NFL_team_id) VALUES (1, 'Justin Jefferson', 'WR', '1');
INSERT INTO FantasyPlayers (player_id, player_name, Position, NFL_team_id) VALUES (2, 'Drake Maye', 'QB', '2');
INSERT INTO FantasyPlayers (player_id, player_name, Position, NFL_team_id) VALUES (3, 'Jordan Addison', 'WR', '1');
INSERT INTO FantasyPlayers (player_id, player_name, Position, NFL_team_id) VALUES (4, 'Josh Jacobs', 'RB', '5');
INSERT INTO FantasyPlayers (player_id, player_name, Position, NFL_team_id) VALUES (5, 'CeeDee Lamb', 'WR', '4');
INSERT INTO FantasyPlayers (player_id, player_name, Position, NFL_team_id) VALUES (6, 'Isiah Pacheco', 'RB', '3');

-- Table: FantasyTeams
CREATE TABLE IF NOT EXISTS FantasyTeams (
 team_id INTEGER PRIMARY KEY,
 user_id INTEGER,
 team_name TEXT,
 league_id TEXT,
 Wins INTEGER,
 Loses INTEGER,
 Ties INTEGER,
 FOREIGN KEY (league_id) REFERENCES League (leauge_id),
 FOREIGN KEY (user_id) REFERENCES Users (user_id)
);
INSERT INTO FantasyTeams (team_id, user_id, team_name, league_id, Wins, Loses, Ties) VALUES (1, 3, 'Alice''s Team', '1', 8, 2, 1);
INSERT INTO FantasyTeams (team_id, user_id, team_name, league_id, Wins, Loses, Ties) VALUES (2, 2, 'Mitch''s Team', '3', 9, 2, 0);
INSERT INTO FantasyTeams (team_id, user_id, team_name, league_id, Wins, Loses, Ties) VALUES (3, 1, 'Hunter''s Team', '2', 7, 4, 0);
INSERT INTO FantasyTeams (team_id, user_id, team_name, league_id, Wins, Loses, Ties) VALUES (4, 5, 'Eva''s Team', '4', 8, 3, 0);
INSERT INTO FantasyTeams (team_id, user_id, team_name, league_id, Wins, Loses, Ties) VALUES (5, 1, 'Team Alpha', '1', 5, 6, 0);
INSERT INTO FantasyTeams (team_id, user_id, team_name, league_id, Wins, Loses, Ties) VALUES (6, 4, 'Team Charlie', '3', 2, 9, 0);
INSERT INTO FantasyTeams (team_id, user_id, team_name, league_id, Wins, Loses, Ties) VALUES (7, 3, 'Team Bravo', '2', 4, 6, 1);

-- Table: League
CREATE TABLE IF NOT EXISTS League (
leauge_id INTEGER PRIMARY KEY, 
league_name TEXT, 
championship_id INTEGER REFERENCES Users (user_id)
);
INSERT INTO League (leauge_id, league_name, championship_id) VALUES (1, 'Big Ballas', 3);
INSERT INTO League (leauge_id, league_name, championship_id) VALUES (2, 'The Neighbors League', 1);
INSERT INTO League (leauge_id, league_name, championship_id) VALUES (3, 'Champions League', 2);
INSERT INTO League (leauge_id, league_name, championship_id) VALUES (4, 'Rookies League', 5);

-- Table: NFL_teams
CREATE TABLE IF NOT EXISTS NFL_teams (
 NFL_team_id INTEGER PRIMARY KEY,
 team_name TEXT
);
INSERT INTO NFL_teams (NFL_team_id, team_name) VALUES (1, 'Minnesota Vikings');
INSERT INTO NFL_teams (NFL_team_id, team_name) VALUES (2, 'New England Patriots');
INSERT INTO NFL_teams (NFL_team_id, team_name) VALUES (3, 'Kansas City Chiefs');
INSERT INTO NFL_teams (NFL_team_id, team_name) VALUES (4, 'Dallas Cowboys');
INSERT INTO NFL_teams (NFL_team_id, team_name) VALUES (5, 'Green Bay Packers');

-- Table: PlayerOnFantasyTeam
CREATE TABLE IF NOT EXISTS PlayerOnFantasyTeam (
 team_id INTEGER,
 player_id INTEGER,
 league_id INTEGER,
 FOREIGN KEY (league_id) REFERENCES League (leauge_id),
 FOREIGN KEY (player_id) REFERENCES FantasyPlayers (player_id),
 FOREIGN KEY (team_id) REFERENCES FantasyTeams (team_id),
 PRIMARY KEY (team_id, player_id, league_id)
);
INSERT INTO PlayerOnFantasyTeam (team_id, player_id, league_id) VALUES (3, 1, 2);
INSERT INTO PlayerOnFantasyTeam (team_id, player_id, league_id) VALUES (2, 5, 3);
INSERT INTO PlayerOnFantasyTeam (team_id, player_id, league_id) VALUES (1, 6, 1);
INSERT INTO PlayerOnFantasyTeam (team_id, player_id, league_id) VALUES (4, 4, 4);
INSERT INTO PlayerOnFantasyTeam (team_id, player_id, league_id) VALUES (5, 2, 1);
INSERT INTO PlayerOnFantasyTeam (team_id, player_id, league_id) VALUES (6, 3, 3);
INSERT INTO PlayerOnFantasyTeam (team_id, player_id, league_id) VALUES (7, 2, 2);

-- Table: TeamPerformance
CREATE TABLE IF NOT EXISTS TeamPerformance (
 team_id INTEGER,
 week_num INTEGER,
 points_scored INTEGER,
 opponent_id INTEGER,
 Win BOOLEAN,
 FOREIGN KEY (opponent_id) REFERENCES FantasyTeams (team_id),
 FOREIGN KEY (team_id) REFERENCES FantasyTeams (team_id),
 PRIMARY KEY (team_id, week_num)
);
INSERT INTO TeamPerformance (team_id, week_num, points_scored, opponent_id, Win) VALUES (1, 8, 102, 6, 1);
INSERT INTO TeamPerformance (team_id, week_num, points_scored, opponent_id, Win) VALUES (6, 8, 97, 1, 0);
INSERT INTO TeamPerformance (team_id, week_num, points_scored, opponent_id, Win) VALUES (3, 10, 135, 7, 1);
INSERT INTO TeamPerformance (team_id, week_num, points_scored, opponent_id, Win) VALUES (7, 10, 130, 3, 0);
INSERT INTO TeamPerformance (team_id, week_num, points_scored, opponent_id, Win) VALUES (2, 11, 143, 6, 1);
INSERT INTO TeamPerformance (team_id, week_num, points_scored, opponent_id, Win) VALUES (6, 11, 120, 2, 0);

-- Table: Users
CREATE TABLE IF NOT EXISTS Users (
 user_id INTEGER PRIMARY KEY,
 fname TEXT,
 lname TEXT,
 email TEXT
);
INSERT INTO Users (user_id, fname, lname, email) VALUES (1, 'Hunter', 'Dunn', '04hunterdunn@gmail.com');
INSERT INTO Users (user_id, fname, lname, email) VALUES (2, 'Mitchell', 'Piehl', 'pieh6361@stthomas.edu');
INSERT INTO Users (user_id, fname, lname, email) VALUES (3, 'Alice', 'Brown', 'alice.brown@example.com');
INSERT INTO Users (user_id, fname, lname, email) VALUES (4, 'Charlie', 'Davis', 'charlie.davis@example.com');
INSERT INTO Users (user_id, fname, lname, email) VALUES (5, 'Eva', 'Smith', 'eva.smith@example.com');

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
