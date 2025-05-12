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


# R script to create the play database that we will use for the dashboard
# Looking for CPO 1, CPO 2, CPO 3, PPO 1, PPO 2, PPO 3, SB 2, SB 3

# CPO1 1-2-3, no hit
# CPO2 1-2-(4/6) No Hit, runner originally on second
# CPO3 1-2-5 NO hit, runner originally on third
# PPO1 1-3 No hit, pickoff symbol
# PPO2 1-(4/6) No hit, pickoff symbol
# PPO3 1-5 No hit, pickoff symbol
# SB2 1-2-(4.6) No hit, runner originally on 1st and not 2nd
# SB3 1-2-5 No hit, runner originally on 2nd

start = 51
end = 100
level = "Home1A"
season = "season_1883"

# Filter the df to requested days and potential scenarios
filter_df <- function(start, end, season, level) {
  wanted_days <- seq(from = start, to = end, by = 0.5)
  level <- level
  season <- season
  
  # MAKE SURE DAYS YOU CHOOSE CORRESPOND TO FILE DAYS IN GIVEN YEAR
  # Use sprintf to format the strings with leading zeros
  days <- sprintf('day_%05.1f', wanted_days)
  days <- sub("\\.0$", "", days)
  
  game_info_pull <- game_info |>
    filter(Day %in% days, home_team == level, Season == season) |> 
    
    collect()
  
  # Change to compatible col names
  names(game_info_pull)[names(game_info_pull) == "play_per_game"] <- "play_id"
  cols <- c("game_str", "play_id")
  game_info_pull$play_id <- as.integer64(game_info_pull$play_id)
  
  # Ball Position Join
  ball_pos_pull <- ball_pos |>
    inner_join(game_info_pull, by = cols) |>
    
    collect()
  
  cols <- c("game_str", "play_id", "timestamp")
  ball_pos_pull$play_id <- as.integer64(ball_pos_pull$play_id)
  ball_pos_pull$timestamp <- as.integer64(ball_pos_pull$timestamp)
  
  # Player Position Join
  player_pos_pull <- player_pos |>
    inner_join(ball_pos_pull, by = cols) |>
    filter(player_position < 15) |>
    
    collect()
  
  # Widen dataframe to include individual player locations
  player_pos_pull <- player_pos_pull %>%
    pivot_wider(names_from = player_position,
                values_from = c("field_x", "field_y"),
                names_prefix = "player_",
                names_sep = "_")
  
  cols <- c("game_str", "play_per_game", "timestamp")
  player_pos_pull$play_id <- as.integer64(player_pos_pull$play_id)
  player_pos_pull$timestamp <- as.integer64(player_pos_pull$timestamp)
  
  player_pos_pull = sqldf("
                        SELECT
                        *
                        FROM player_pos_pull
                        WHERE field_x_player_11 IS NOT NULL
                        OR field_x_player_12 IS NOT NULL
                        OR field_x_player_13 IS NOT NULL"
  )
  
  cols_to_remove <- c("Season.x", "Season.y", "HomeTeam.x", "HomeTeam.y", "AwayTeam.x", "AwayTeam.y", "Day.x", "Day.y")
  
  player_pos_pull <- player_pos_pull[, !names(player_pos_pull) %in% cols_to_remove]
  names(player_pos_pull)[names(player_pos_pull) == "play_id"] <- "play_per_game"
  
  # Game Events Join
  game_events_pull <- game_events |>
    right_join(player_pos_pull, by = cols) |>
    
    collect()
  
  cols_to_remove <- c("Season.x", "HomeTeam.x", "AwayTeam.x", "Day.x", "at_bat.x", "play_id")
  game_events_pull <- game_events_pull[, !names(game_events_pull) %in% cols_to_remove]
  
  game_events_pull <- sqldf("SELECT
            game_str,
            play_per_game,
            SUM(CASE WHEN event_code = 1 THEN 1 ELSE 0 END) AS has_pitch_event,
            SUM(CASE WHEN event_code = 3 THEN 1 ELSE 0 END) AS has_known_throw,
            SUM(CASE WHEN event_code = 4 THEN 1 ELSE 0 END) AS has_ball_hit,
            SUM(CASE WHEN event_code = 6 THEN 1 ELSE 0 END) AS has_pickoff,
            SUM(CASE wHEN player_position = 2 THEN 1 ELSE 0 END) AS touch_catcher,
            SUM(CASE WHEN player_position = 3 THEN 1 ELSE 0 END) AS touch_first,
            SUM(CASE WHEN player_position = 4 THEN 1 ELSE 0 END) AS touch_second,
            SUM(CASE WHEN player_position = 6 THEN 1 ELSE 0 END) AS touch_short,
            SUM(CASE WHEN player_position = 5 THEN 1 ELSE 0 END) AS touch_third,
            SUM(CASE WHEN field_x_player_11 IS NOT NULL THEN 1 ELSE 0 END) AS original_runner_first,
            SUM(CASE WHEN field_x_player_12 IS NOT NULL THEN 1 ELSE 0 END) AS original_runner_second,
            SUM(CASE WHEN field_x_player_13 IS NOT NULL THEN 1 ELSE 0 END) AS original_runner_third,
            MAX(pitcher) AS pitcher_id,
            MAX(catcher) AS catcher_id
            FROM game_events_pull
            GROUP BY game_str, play_per_game")
  
  return (game_events_pull)
}

find_cpo1 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_known_throw > 0
            AND touch_first > 0
            AND touch_catcher > 0
            AND original_runner_first > 0
            ")
  
  return(df)
  
}

df <- find_cpo1(1, 50, "Season_1883", "Home1A")
df1 <- find_cpo1(1, 50, "Season_1884", "Home1A")
df2 <- find_cpo1(51, 100, "Season_1884", "Home1A")
df3 <- find_cpo1(101, 180, "Season_1884", "Home1A")
df4 <- find_cpo1(1, 50, "Season_1883", "Home2A")
df5 <- find_cpo1(1, 50, "Season_1884", "Home2A")
df6 <- find_cpo1(51, 100, "Season_1884", "Home2A")
df7 <- find_cpo1(101, 180, "Season_1884", "Home2A")
df8 <- find_cpo1(1, 50, "Season_1883", "Home3A")
df9 <- find_cpo1(1, 50, "Season_1884", "Home3A")
df10 <- find_cpo1(51, 100, "Season_1884", "Home3A")
df11 <- find_cpo1(101, 180, "Season_1884", "Home3A")
df12 <- find_cpo1(1, 50, "Season_1883", "Home4A")
df13 <- find_cpo1(1, 50, "Season_1884", "Home4A")
df14 <- find_cpo1(51, 100, "Season_1884", "Home4A")
df15 <- find_cpo1(101, 180, "Season_1884", "Home4A")

pickoff_database <- rbind(df, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          catcher_id AS catcher
                          FROM pickoff_database")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\cpo1_db.csv"

write.csv(pickoff_database, file = file_path, row.names = FALSE)

write.table(pickoff_database, file_path, row.names = FALSE, col.names = FALSE, sep = ",", append = TRUE)

# _______________________________________________ START CPO2 ________________________________________
# ___________________________________________________________________________________________________
find_cpo2 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_known_throw > 0
            AND (touch_short > 0 OR touch_second > 0)
            AND touch_catcher > 0
            AND original_runner_second > 0
            ")
  
  return(df)
  
}

df <- find_cpo2(1, 50, "Season_1883", "Home1A")
df1 <- find_cpo2(1, 50, "Season_1884", "Home1A")
df2 <- find_cpo2(51, 100, "Season_1884", "Home1A")
df3 <- find_cpo2(101, 180, "Season_1884", "Home1A")
df4 <- find_cpo2(1, 50, "Season_1883", "Home2A")
df5 <- find_cpo2(1, 50, "Season_1884", "Home2A")
df6 <- find_cpo2(51, 100, "Season_1884", "Home2A")
df7 <- find_cpo2(101, 180, "Season_1884", "Home2A")
df8 <- find_cpo2(1, 50, "Season_1883", "Home3A")
df9 <- find_cpo2(1, 50, "Season_1884", "Home3A")
df10 <- find_cpo2(51, 100, "Season_1884", "Home3A")
df11 <- find_cpo2(101, 180, "Season_1884", "Home3A")
df12 <- find_cpo2(1, 50, "Season_1883", "Home4A")
df13 <- find_cpo2(1, 50, "Season_1884", "Home4A")
df14 <- find_cpo2(51, 100, "Season_1884", "Home4A")
df15 <- find_cpo2(101, 180, "Season_1884", "Home4A")

pickoff_database <- rbind(df, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          catcher_id AS catcher
                          FROM df1")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\cpo2_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)

# _______________________________________________ START CPO3 ________________________________________
# ___________________________________________________________________________________________________
find_cpo3 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_known_throw > 0
            AND touch_third > 0
            AND touch_catcher > 0
            AND original_runner_third > 0
            ")
  
  return(df)
  
}

df <- find_cpo3(1, 50, "Season_1883", "Home1A")
df1 <- find_cpo3(1, 50, "Season_1884", "Home1A")
df2 <- find_cpo3(51, 100, "Season_1884", "Home1A")
df3 <- find_cpo3(101, 180, "Season_1884", "Home1A")
df4 <- find_cpo3(1, 50, "Season_1883", "Home2A")
df5 <- find_cpo3(1, 50, "Season_1884", "Home2A")
df6 <- find_cpo3(51, 100, "Season_1884", "Home2A")
df7 <- find_cpo3(101, 180, "Season_1884", "Home2A")
df8 <- find_cpo3(1, 50, "Season_1883", "Home3A")
df9 <- find_cpo3(1, 50, "Season_1884", "Home3A")
df10 <- find_cpo3(51, 100, "Season_1884", "Home3A")
df11 <- find_cpo3(101, 180, "Season_1884", "Home3A")
df12 <- find_cpo3(1, 50, "Season_1883", "Home4A")
df13 <- find_cpo3(1, 50, "Season_1884", "Home4A")
df14 <- find_cpo3(51, 100, "Season_1884", "Home4A")
df15 <- find_cpo3(101, 180, "Season_1884", "Home4A")

pickoff_database <- rbind(df, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          catcher_id AS catcher
                          FROM pickoff_database")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\cpo3_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)


write.table(pickoff_database, file_path, row.names = FALSE, col.names = FALSE, sep = ",", append = TRUE)

# _______________________________________________ START PPO1 ________________________________________
# ___________________________________________________________________________________________________
find_ppo1 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_pickoff > 0
            AND touch_catcher = 0
            AND original_runner_first > 0
            ")
  
  return(df)
  
}

df <- find_ppo1(1, 50, "Season_1883", "Home1A")
df1 <- find_ppo1(1, 50, "Season_1884", "Home1A")
df2 <- find_ppo1(51, 100, "Season_1884", "Home1A")
df3 <- find_ppo1(101, 180, "Season_1884", "Home1A")
df4 <- find_ppo1(1, 50, "Season_1883", "Home2A")
df5 <- find_ppo1(1, 50, "Season_1884", "Home2A")
df6 <- find_ppo1(51, 100, "Season_1884", "Home2A")
df7 <- find_ppo1(101, 180, "Season_1884", "Home2A")
df8 <- find_ppo1(1, 50, "Season_1883", "Home3A")
df9 <- find_ppo1(1, 50, "Season_1884", "Home3A")
df10 <- find_ppo1(51, 100, "Season_1884", "Home3A")
df11 <- find_ppo1(101, 180, "Season_1884", "Home3A")
df12 <- find_ppo1(1, 50, "Season_1883", "Home4A")
df13 <- find_ppo1(1, 50, "Season_1884", "Home4A")
df14 <- find_ppo1(51, 100, "Season_1884", "Home4A")
df15 <- find_ppo1(101, 180, "Season_1884", "Home4A")

pickoff_database <- rbind(df, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          pitcher_id AS pitcher
                          FROM pickoff_database")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\ppo1_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)

write.table(pickoff_database, file_path, row.names = FALSE, col.names = FALSE, sep = ",", append = TRUE)


# _______________________________________________ START PPO2 ________________________________________
# ___________________________________________________________________________________________________
find_ppo2 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_pickoff > 0
            AND touch_catcher = 0
            AND original_runner_second > 0
            AND (touch_second > 0 OR touch_short > 0)
            ")
  
  return(df)
  
}

df <- find_ppo2(1, 50, "Season_1883", "Home1A")
df1 <- find_ppo2(1, 50, "Season_1884", "Home1A")
df2 <- find_ppo2(51, 100, "Season_1884", "Home1A")
df3 <- find_ppo2(101, 180, "Season_1884", "Home1A")
df4 <- find_ppo2(1, 50, "Season_1883", "Home2A")
df5 <- find_ppo2(1, 50, "Season_1884", "Home2A")
df6 <- find_ppo2(51, 100, "Season_1884", "Home2A")
df7 <- find_ppo2(101, 180, "Season_1884", "Home2A")
df8 <- find_ppo2(1, 50, "Season_1883", "Home3A")
df9 <- find_ppo2(1, 50, "Season_1884", "Home3A")
df10 <- find_ppo2(51, 100, "Season_1884", "Home3A")
df11 <- find_ppo2(101, 180, "Season_1884", "Home3A")
df12 <- find_ppo2(1, 50, "Season_1883", "Home4A")
df13 <- find_ppo2(1, 50, "Season_1884", "Home4A")
df14 <- find_ppo2(51, 100, "Season_1884", "Home4A")
df15 <- find_ppo2(101, 180, "Season_1884", "Home4A")

pickoff_database <- rbind(df, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          pitcher_id AS pitcher
                          FROM df1")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\ppo2_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)

write.table(pickoff_database, file_path, row.names = FALSE, col.names = FALSE, sep = ",", append = TRUE)

# _______________________________________________ START PPO3 ________________________________________
# ___________________________________________________________________________________________________
find_ppo3 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_pickoff > 0
            AND touch_catcher = 0
            AND original_runner_third > 0
            AND touch_third > 0
            ")
  
  return(df)
  
}

df <- find_ppo3(1, 50, "Season_1883", "Home1A")
df1 <- find_ppo3(1, 50, "Season_1884", "Home1A")
df2 <- find_ppo3(51, 100, "Season_1884", "Home1A")
df3 <- find_ppo3(101, 180, "Season_1884", "Home1A")
df4 <- find_ppo3(1, 50, "Season_1883", "Home2A")
df5 <- find_ppo3(1, 50, "Season_1884", "Home2A")
df6 <- find_ppo3(51, 100, "Season_1884", "Home2A")
df7 <- find_ppo3(101, 180, "Season_1884", "Home2A")
df8 <- find_ppo3(1, 50, "Season_1883", "Home3A")
df9 <- find_ppo3(1, 50, "Season_1884", "Home3A")
df10 <- find_ppo3(51, 100, "Season_1884", "Home3A")
df11 <- find_ppo3(101, 180, "Season_1884", "Home3A")
df12 <- find_ppo3(1, 50, "Season_1883", "Home4A")
df13 <- find_ppo3(1, 50, "Season_1884", "Home4A")
df14 <- find_ppo3(51, 100, "Season_1884", "Home4A")
df15 <- find_ppo3(101, 180, "Season_1884", "Home4A")

pickoff_database <- rbind(df, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          pitcher_id AS pitcher
                          FROM pickoff_database")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\ppo3_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)

write.table(pickoff_database, file_path, row.names = FALSE, col.names = FALSE, sep = ",", append = TRUE)

# _______________________________________________ START SB2 ________________________________________
# ___________________________________________________________________________________________________
find_sb2 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_pickoff = 0
            AND touch_catcher > 0
            AND has_pitch_event > 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_short > 0 OR touch_second > 0
            ")
  
  return(df)
  
}

df <- find_sb2(1, 50, "Season_1883", "Home1A")
df <- sqldf("SELECT *
            FROM df
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df1 <- find_sb2(1, 50, "Season_1884", "Home1A")
df1 <- sqldf("SELECT *
            FROM df1
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df2 <- find_sb2(51, 100, "Season_1884", "Home1A")
df2 <- sqldf("SELECT *
            FROM df2
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df3 <- find_sb2(101, 180, "Season_1884", "Home1A")
df3 <- sqldf("SELECT *
            FROM df3
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df4 <- find_sb2(1, 50, "Season_1883", "Home2A")
df4 <- sqldf("SELECT *
            FROM df4
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df5 <- find_sb2(1, 50, "Season_1884", "Home2A")
df5 <- sqldf("SELECT *
            FROM df5
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df6 <- find_sb2(51, 100, "Season_1884", "Home2A")
df6 <- sqldf("SELECT *
            FROM df6
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df7 <- find_sb2(101, 180, "Season_1884", "Home2A")
df7 <- sqldf("SELECT *
            FROM df7
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df8 <- find_sb2(1, 50, "Season_1883", "Home3A")
df8 <- sqldf("SELECT *
            FROM df8
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df9 <- find_sb2(1, 50, "Season_1884", "Home3A")
df9 <- sqldf("SELECT *
            FROM df9
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df10 <- find_sb2(51, 100, "Season_1884", "Home3A")
df10 <- sqldf("SELECT *
            FROM df10
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df11 <- find_sb2(101, 180, "Season_1884", "Home3A")
df11 <- sqldf("SELECT *
            FROM df11
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df12 <- find_sb2(1, 50, "Season_1883", "Home4A")
df12 <- sqldf("SELECT *
            FROM df12
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df13 <- find_sb2(1, 50, "Season_1884", "Home4A")
df13 <- sqldf("SELECT *
            FROM df13
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df14 <- find_sb2(51, 100, "Season_1884", "Home4A")
df14 <- sqldf("SELECT *
            FROM df14
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")
df15 <- find_sb2(101, 180, "Season_1884", "Home4A")
df15 <- sqldf("SELECT *
            FROM df15
            WHERE has_ball_hit = 0
            AND original_runner_first > 0
            AND original_runner_second = 0
            AND touch_catcher > 0")

pickoff_database <- rbind(df, df1, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          pitcher_id AS pitcher,
                          catcher_id AS catcher
                          FROM pickoff_database")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\sb2_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)

# _______________________________________________ START SB3 ________________________________________
# ___________________________________________________________________________________________________
find_sb3 <- function(start, end, season, level) {
  df <- filter_df(start, end, season, level)
  
  df <- sqldf("SELECT
            *
            FROM df
            WHERE has_ball_hit = 0
            AND has_pickoff = 0
            AND touch_catcher > 0
            AND has_pitch_event > 0
            AND original_runner_third = 0
            AND original_runner_second > 0
            AND touch_third > 0
            ")
  
  return(df)
  
}

df <- find_sb3(1, 50, "Season_1883", "Home1A")
df1 <- find_sb3(1, 50, "Season_1884", "Home1A")
df2 <- find_sb3(51, 100, "Season_1884", "Home1A")
df3 <- find_sb3(101, 180, "Season_1884", "Home1A")
df4 <- find_sb3(1, 50, "Season_1883", "Home2A")
df5 <- find_sb3(1, 50, "Season_1884", "Home2A")
df6 <- find_sb3(51, 100, "Season_1884", "Home2A")
df7 <- find_sb3(101, 180, "Season_1884", "Home2A")
df8 <- find_sb3(1, 50, "Season_1883", "Home3A")
df9 <- find_sb3(1, 50, "Season_1884", "Home3A")
df10 <- find_sb3(51, 100, "Season_1884", "Home3A")
df11 <- find_sb3(101, 180, "Season_1884", "Home3A")
df12 <- find_sb3(1, 50, "Season_1883", "Home4A")
df13 <- find_sb3(1, 50, "Season_1884", "Home4A")
df14 <- find_sb3(51, 100, "Season_1884", "Home4A")
df15 <- find_sb3(101, 180, "Season_1884", "Home4A")

pickoff_database <- rbind(df, df1, df2, df3, df4, df5, df6, df7, df8, df9, df10,
                          df11, df12, df13, df14, df15)

pickoff_database <- sqldf("SELECT
                          game_str,
                          play_per_game,
                          pitcher_id AS pitcher,
                          catcher_id AS catcher
                          FROM pickoff_database")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\sb3_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)
