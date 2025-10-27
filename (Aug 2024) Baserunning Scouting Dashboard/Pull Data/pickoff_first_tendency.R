library(sqldf)
library(tidyr)
library(dplyr)
library(bit64)

Sys.setlocale("LC_NUMERIC", "C")

# When you guys use it you will need to change it to your data_directory
luke_data_directory <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R"

hunter_data_directory <- 'C:\\Users\\Hunter Dunn\\OneDrive - University of St. Thomas\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R'
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

# PARAMETERS

cur_level <- "Home1A"
cur_pitcher <- 683

filter_df <- function(cur_pitcher, cur_level) {
  
  game_info_pull <- game_info |>
    filter(pitcher == cur_pitcher, home_team == cur_level, !is.na(first_baserunner)) |>
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
    filter(player_position < 14) |>
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
  
  cols_to_remove <- c("Season.x", "Season.y", "HomeTeam.x", "HomeTeam.y", "AwayTeam.x", "AwayTeam.y", "Day.x", "Day.y")
  
  player_pos_pull <- player_pos_pull[, !names(player_pos_pull) %in% cols_to_remove]
  names(player_pos_pull)[names(player_pos_pull) == "play_id"] <- "play_per_game"
  
  game_events_pull <- game_events |>
    right_join(player_pos_pull, by = cols) |>
    
    collect()
  
  cols_to_remove <- c("Season.x", "HomeTeam.x", "AwayTeam.x", "Day.x", "at_bat.x", "play_id")
  game_events_pull <- game_events_pull[, !names(game_events_pull) %in% cols_to_remove]
  
  # Rename final schema
  names(game_events_pull)[names(game_events_pull) == "Season.y"] <- "Season"
  names(game_events_pull)[names(game_events_pull) == "player_position"] <- "event_player_position"
  names(game_events_pull)[names(game_events_pull) == "HomeTeam.y"] <- "HomeTeam"
  names(game_events_pull)[names(game_events_pull) == "AwayTeam.y"] <- "AwayTeam"
  names(game_events_pull)[names(game_events_pull) == "Day.y"] <- "Day"
  
  df <- sqldf("SELECT
              game_str,
              play_per_game,
              home_team,
              MAX(pitcher) as pitcher,
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
              GROUP BY game_str, play_per_game, home_team")
  
  df <- sqldf("SELECT
              pitcher,
              home_team AS level,
              SUM(has_pickoff) AS total_pickoffs,
              COUNT(*) AS total_plays
              FROM df
              GROUP BY pitcher, level
              ")
  
  return(df)
}

player_dataframe <- read.csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//pitcher_catcher_db.csv")

resultlist <- list()
print(nrow(player_dataframe))

for(i in 1:nrow(player_dataframe)) {
  player_id <- player_dataframe$player_id[i]
  player_position <- player_dataframe$position[i]
  player_level <- player_dataframe$level[i]
  
  print(player_id) # To track progress
  if(player_position == "P"){
    print("pitcher")
    df <- filter_df(player_id, player_level)
    resultlist <- append(resultlist, list(df))
  }
}

mega_df <- do.call(rbind, resultlist)

mega_df$percent_pickoff = mega_df$total_pickoffs/mega_df$total_plays

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\pickoff_first_tendency.csv"
write.csv(mega_df, file = file_path, row.names = FALSE)
