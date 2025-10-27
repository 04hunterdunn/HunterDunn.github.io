library(bit64)
library(tidyr)
library(dplyr)
library(sqldf)

# When you guys use it you will need to change it to your data_directory
luke_data_directory <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R"
hunter_data_directory <- "C:\\Users\\Hunter Dunn\\OneDrive - University of St. Thomas\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R"
brennen_data_directory <- "/Users/brennenbruch/Documents/GitHub/smt2024umn/Pull Data/SMT_Data_starter.R"
jack_data_directory <- "C:\\Users\\brick\\OneDrive\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R"

is_luke <- FALSE
is_hunter <- FALSE
is_brennen <- TRUE
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
start <- 5
end <- 6
wanted_days <- seq(from = start, to = end, by = 0.5)
level <- "Home1A" # Always in home format. Just changed number to 1-4
season <- "Season_1883" # Option of 1883 or 1884 

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

