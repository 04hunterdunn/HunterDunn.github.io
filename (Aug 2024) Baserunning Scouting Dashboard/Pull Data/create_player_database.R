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

# PARAMETERS

get_unique_pitcher_catcher <- function(begin_date, end_date, level, season) {
  wanted_days <- seq(from = begin_date, to = end_date, by = 0.5)
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
  
  # Get all pitcher ids
  pitcher <- sqldf("SELECT DISTINCT
                   pitcher AS player_id,
                   'P' AS position
                   FROM player_pos_pull
                  ")

  catcher <- sqldf("SELECT DISTINCT
                   catcher AS player_id,
                   'C' AS position
                   FROM player_pos_pull
                  ")

    
  combined <- rbind(pitcher, catcher)
  
  combined <- combined %>%
    mutate(level = level)
  
  return(combined)
}

singlea_1883 <- get_unique_pitcher_catcher(1, 200, "Home1A", "Season_1883")
singlea_1884 <- get_unique_pitcher_catcher(1, 200, "Home1A", "Season_1884")
doublea_1883 <- get_unique_pitcher_catcher(1, 200, "Home2A", "Season_1883")
doublea_1884 <- get_unique_pitcher_catcher(1, 200, "Home2A", "Season_1884")
triplea_1883 <- get_unique_pitcher_catcher(1, 200, "Home3A", "Season_1883")
triplea_1884 <- get_unique_pitcher_catcher(1, 200, "Home3A", "Season_1884")
majora_1883 <- get_unique_pitcher_catcher(1, 200, "Home4A", "Season_1883")
majora_1884 <- get_unique_pitcher_catcher(1, 200, "Home4A", "Season_1884")

pitcher_catcher_database <- rbind(singlea_1883, singlea_1884, doublea_1883, doublea_1884,
                         triplea_1883, triplea_1884, majora_1883, majora_1884)

pitcher_catcher_database <- sqldf("
                                  SELECT DISTINCT
                                  player_id,
                                  position
                                  FROM pitcher_catcher_database
                                  WHERE player_id IS NOT NULL")

file_path <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\pitcher_catcher_db.csv"
write.csv(pitcher_catcher_database, file = file_path, row.names = FALSE)
