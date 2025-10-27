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

get_all_pickoffs <- function(begin_date, end_date, level, season) {
  wanted_days <- seq(from = begin_date, to = end_date, by = 0.5)
  
  # MAKE SURE DAYS YOU CHOOSE CORRESPOND TO FILE DAYS IN GIVEN YEAR
  # Use sprintf to format the strings with leading zeros
  days <- sprintf('day_%05.1f', wanted_days)
  days <- sub("\\.0$", "", days)
  
  # Start to filter down data to only instances for potential steals
  # Still need to add functionality (Like if runner on third, second, first)
  # or if (second, third)
  # !is.na(value)
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
  
  # Rename final schema
  names(game_events_pull)[names(game_events_pull) == "Season.y"] <- "Season"
  names(game_events_pull)[names(game_events_pull) == "player_position"] <- "event_player_position"
  names(game_events_pull)[names(game_events_pull) == "HomeTeam.y"] <- "HomeTeam"
  names(game_events_pull)[names(game_events_pull) == "AwayTeam.y"] <- "AwayTeam"
  names(game_events_pull)[names(game_events_pull) == "Day.y"] <- "Day"
  
  # Get key for all instances of pickoffs in selected data
  result = sqldf("SELECT
  game_str, 
  play_per_game,
  event_player_position,
  Season,
  pitcher,
  catcher
  FROM game_events_pull
  WHERE event_code = 6
  ")
  
  return(result)
}

singlea_1883 <- get_all_pickoffs(1, 200, "Home1A", "Season_1883")
singlea_1884 <- get_all_pickoffs(1, 200, "Home1A", "Season_1884")
doublea_1883 <- get_all_pickoffs(1, 200, "Home2A", "Season_1883")
doublea_1884 <- get_all_pickoffs(1, 200, "Home2A", "Season_1884")
triplea_1883 <- get_all_pickoffs(1, 200, "Home3A", "Season_1883")
triplea_1884 <- get_all_pickoffs(1, 200, "Home3A", "Season_1884")
majora_1883 <- get_all_pickoffs(1, 200, "Home4A", "Season_1883")
majora_1884 <- get_all_pickoffs(1, 200, "Home4A", "Season_1884")

pickoff_database <- rbind(singlea_1883, singlea_1884, doublea_1883, doublea_1884,
                                  triplea_1883, triplea_1884, majora_1883, majora_1884)

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\pickoff_db.csv"
write.csv(pickoff_database, file = file_path, row.names = FALSE)

count_by_pitcher <- sqldf("
                          SELECT
                          pitcher,
                          COUNT(*) AS total_instances
                          FROM pickoff_database
                          GROUP BY pitcher
                          ORDER BY total_instances desc")

