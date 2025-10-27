import sqlite3

con = sqlite3.connect("FantasyFootball.db")
cur = con.cursor()

# read the airline.sql DDL file 
with open("FantasyFootball.sql", "r") as f:
    ddl = f.read()

# execute the DDL 
cur.executescript(ddl)

# insert some data into the plane table (tailno, make, model, capacity, mph)
teams_data = [
    (1, "Minnesota Vikings"),
    (2, "New England Patriots"),
    (3, "Kansas City Chiefs"),
    (4, "Dallas Cowboys"),
    (5, "Green Bay Packers")
]
cur.executemany("INSERT INTO NFL_teams VALUES (?, ?)", teams_data)

# insert some data into the passengers table (first, middle, last, ssn)
fantasy_players = [
    (1, "Justin Jefferson", "WR", 1),
    (2, "Drake Maye", "QB", 2),
    (3, "Jordan Addison", "WR", 1),
    (4, "Josh Jacobs", "RB", 5),
    (5, "CeeDee Lamb", "WR", 4),
    (6, "Isiah Pacheco", "RB", 3)
]
cur.executemany("INSERT INTO FantasyPlayers VALUES (?, ?, ?, ?)", fantasy_players)

# insert some data into the flight table (flight_no, dep_loc, dep_time, arr_loc, arr_time, tail_no)
users = [
    (1, "Hunter", "Dunn", "04hunterdunn@gmail.com"),
    (2, "Mitchell", "Piehl", "pieh6361@stthomas.edu"),
    (3, "Alice", "Brown", "alice.brown@example.com"),
    (4, "Charlie", "Davis", "charlie.davis@example.com"),
    (5, "Eva", "Smith", "eva.smith@example.com")
]
cur.executemany("INSERT INTO Users VALUES (?, ?, ?, ?)", users)

# insert some data into the onboard table (ssn, flight_no, seat)
leagues = [
    (1, "Big Ballas", 3),
    (2, "The Neighbors League", 1),
    (3, "Champions League", 2),
    (4, "Rookies League", 5)
]
cur.executemany("INSERT INTO League VALUES (?, ?, ?)", leagues)

fantasy_teams = [
    (1, 3, "Alice's Team", 1, 8, 2, 1),
    (2, 2, "Mitch's Team", 3, 9, 2, 0),
    (3, 1, "Hunter's Team", 2, 7, 4, 0),
    (4, 5, "Eva's Team", 4, 8, 3, 0),
    (5, 1, "Team Alpha", 1, 5, 6, 0),
    (6, 4, "Team Charlie", 3, 2, 9, 0),
    (7, 3, "Team Bravo", 2, 4, 6, 1)
]
cur.executemany("INSERT INTO FantasyTeams VALUES (?, ?, ?, ?, ?, ?, ?)", fantasy_teams)

player_on_fantasy_team = [
    (3, 1, 2),
    (2, 5, 3),
    (1, 6, 1),
    (4, 4, 4),
    (5, 2, 1),
    (6, 3, 3),
    (7, 2, 2)
]
cur.executemany("INSERT INTO PlayerOnFantasyTeam VALUES (?, ?, ?)", player_on_fantasy_team)

team_performance = [
    (1, 8, 102, 6, True),
    (6, 8, 97, 1, False),
    (3, 10, 135, 7, True),
    (7, 10, 130, 3, False),
    (2, 11, 143, 6, True),
    (6, 11, 120, 2, False)
]
cur.executemany("INSERT INTO TeamPerformance VALUES (?, ?, ?, ?, ?)", team_performance)

# commit the changes
con.commit()

con.close()
