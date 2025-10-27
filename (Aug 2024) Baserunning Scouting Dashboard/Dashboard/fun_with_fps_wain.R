## Load relevant libraries
if(!require(tidyverse)) install.packages("tidyverse") # a helper package for gganimate
if(!require(sportyR)) install.packages("sportyR") # a helper package for gganimate
if(!require(gganimate)) install.packages("gganimate") # a helper package for gganimate
library(tidyverse) # for data cleaning, wrangling, etc
library(sportyR) # for baseball field visualizations
if(!require(gifski)) install.packages("gifski") # a helper package for gganimate
library(gganimate) # to make the animated plot
if(!require(plyr)) install.packages("plyr") # a helper package for gganimate

# When you guys use it you will need to change it to your data_directory
luke_data_directory <- "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R"
hunter_data_directory <- 'None'
brennen_data_directory <- "/Users/brennenbruch/Documents/GitHub/smt2024umn/Pull Data/SMT_Data_starter.R"
jack_data_directory <- "C:\\Users\\brick\\OneDrive\\Documents\\GitHub\\smt2024umn\\Pull Data\\SMT_Data_starter.R"

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


## Import Data
source(data_directory)

# Function to extract details from game string
extract_details <- function(game_str) {
  # Split the string by underscores
  parts <- strsplit(game_str, "_")[[1]]
  
  # Extract the relevant parts
  year <- parts[1]
  day <- parts[2]
  level <- parts[4]
  
  return(list(year = year, day = day, level = level))
}

##  Write function to animate play sequences

animate_play <- function(game_id, play) {
  
  details <- extract_details(game_id)
  
  year <- details$year
  day <- details$day
  level <- details$level
  
  
  # Set the specs for the gif we want to create (lower res to make it run quicker)
  options(gganimate.dev_args = list(width = 3, height = 3, units = 'in', res = 120))
  
  fps <- 10
  
  time_of_pitch <- game_events %>%
    filter(game_str == game_id, play_id == play) %>%
    collect() %>%
    as.data.frame() %>%
    select(timestamp) %>%
    slice_head(n = 1) %>%
    pull(timestamp)
  
  # Process player position data
  player_data <- player_pos %>%
    filter(game_str == game_id, play_id == play, player_position < 14) %>%
    collect() %>%
    as.data.frame() %>%  # Ensure it's a data frame
    mutate(type = if_else(player_position %in% c(10:13), "batter", "fielder"),
           position_z = NA) %>%
    dplyr::rename(position_x = field_x, position_y = field_y)
  
  # Process ball position data
  ball_data <- ball_pos %>%
    filter(game_str == game_id, play_id == play) %>%
    collect() %>%
    as.data.frame() %>%  # Ensure it's a data frame
    dplyr::rename(position_x = ball_position_x,
                  position_y = ball_position_y,
                  position_z = ball_position_z) %>%
    mutate(type = "ball", player_position = NA)
  
  # Combine player and ball data into one data frame
  tracking_data <- dplyr::bind_rows(player_data, ball_data) %>%
    arrange(timestamp) %>%
    mutate(timestamp_adj = plyr::round_any(timestamp, fps))
  
  # Filter to start from pitch time
  tracking_data <- tracking_data %>%
    filter(timestamp >= time_of_pitch) %>%
    mutate(frame_id = match(timestamp_adj, unique(timestamp_adj)))
  
  # Set the focus area to include the entire infield
  xlim_vals <- c(-85, 85)
  ylim_vals <- c(-5, 150)
  
  # Make field design focusing on the entire infield
  p <- geom_baseball(league = "MiLB") +
    geom_point(data = tracking_data %>% filter(type != "ball"),
               aes(x = position_x, y = position_y, fill = type),
               shape = 21, size = 3,
               show.legend = F) +
    geom_text(data = tracking_data %>% filter(type == "fielder"),
              aes(x = position_x, y = position_y, label = player_position),
              color = "black", size = 2,
              show.legend = F) +
    geom_point(data = tracking_data %>% filter(type == "ball"),
               aes(x = position_x, y = position_y, size = position_z),
               fill = "white", shape = 21, show.legend = F) +
    transition_time(frame_id) +
    coord_cartesian(xlim = xlim_vals, ylim = ylim_vals) +  # Zoom into the infield area
    shadow_wake(0.1, exclude_layer = c(1:16))+
    ggtitle(paste("Day:", day, "Year:", year,"Play:", play, "Level:", level)) +  # Add title here
    theme(plot.title = element_text(color = "white", size = 10))  # Set title color to white
  
  max_frame <- max(tracking_data$frame_id)
  
  animation = animate(p, fps = fps, nframes = max_frame)
  file_name <- sprintf("www/%s_%d.gif", game_id, play)
  anim_save(file_name, animation)
}
