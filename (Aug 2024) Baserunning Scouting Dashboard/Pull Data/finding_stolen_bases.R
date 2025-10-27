library(sqldf)
library(tidyr)
library(dplyr)
library(bit64)

# When you guys use it you will need to change it to your data_directory
luke_data_directory <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R"

hunter_data_directory <- 'None'
brennen_data_directory <- 'None'
jack_data_directory <- 'None'

is_luke <- TRUE
is_hunter <- FALSE
is_brennen <- FALSE
is_jack <- FALSE

if(is_luke){
  data_directory <- luke_data_directory
} else if(is_hunter){
  data_directory <- hunter_data_directory
} else if(is_brennen) {
  data_directory <- brennen_data_directory
} else{
  data_directory <- jack_data_directory
}

source(data_directory)

df <- sqldf("SELECT
            game_str,
            play_per_game,
            SUM(CASE WHEN event_code = 1 THEN 1 ELSE 0 END) AS has_pitch_event,
            SUM(CASE WHEN event_code = 3 THEN 1 ELSE 0 END) AS has_known_throw,
            SUM(CASE WHEN event_code = 4 THEN 1 ELSE 0 END) AS has_ball_hit,
            SUM(CASE WHEN event_code = 6 THEN 1 ELSE 0 END) AS has_pickoff,
            SUM(CASE wHEN event_player_position = 2 THEN 1 ELSE 0 END) AS touch_catcher,
            SUM(CASE WHEN event_player_position = 3 THEN 1 ELSE 0 END) AS touch_first,
            SUM(CASE WHEN event_player_position = 4 THEN 1 ELSE 0 END) AS touch_second,
            SUM(CASE WHEN event_player_position = 6 THEN 1 ELSE 0 END) AS touch_short,
            SUM(CASE WHEN event_player_position = 5 THEN 1 ELSE 0 END) AS touch_third
            FROM game_events_pull
            GROUP BY game_str, play_per_game")

# Looking for stolen bases attempted at second
df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_known_throw > 0
            AND touch_second > 0 OR touch_short > 0
            ")
# Why it doesn't filter this in the first place. I don't know
df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_pickoff = 0
            ")

