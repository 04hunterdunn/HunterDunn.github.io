--
-- File generated with SQLiteStudio v3.4.4 on Wed Dec 4 14:56:32 2024
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

-- Table: League
CREATE TABLE IF NOT EXISTS League (
leauge_id INTEGER PRIMARY KEY, 
league_name TEXT, 
championship_id INTEGER REFERENCES Users (user_id)
);

-- Table: NFL_teams
CREATE TABLE IF NOT EXISTS NFL_teams (
 NFL_team_id INTEGER PRIMARY KEY,
 team_name TEXT
);

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

-- Table: Users
CREATE TABLE IF NOT EXISTS Users (
 user_id INTEGER PRIMARY KEY,
 fname TEXT,
 lname TEXT,
 email TEXT
);

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
