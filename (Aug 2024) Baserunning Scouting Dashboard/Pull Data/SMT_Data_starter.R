# Welcome to the 2024 SMT Data Challenge! Here are some functions to help you get
# started. After you unzip the dataset, copy the name of the directory you saved 
# it to into the 'data_directory` field below. After making sure you have the 
# `arrow` package installed, you may call this file at the top of your work file(s)
# by calling `source("SMT_Data_starter.R"). Then, you may apply functions and 
# operations to the table names below as you would any other table and load them 
# into your working environment by calling `collect()`. For an example of this 
# process, un-comment and run the lines below the starter code. 
# 
# WARNING: The data subsets are large, especially `player_pos`. Reading the 
#   entire subset at once without filtering may incur performance issues on your 
#   machine or even crash your R session. It is recommended that you filter 
#   data subsets wisely before calling `collect()`.

# SMT Data Challenge

# When you guys use it you will need to change it to your data_directory
luke_data_directory <- '\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\Data'
hunter_data_directory <- '\\Users\\Hunter Dunn\\OneDrive - University of St. Thomas\\Documents\\GitHub\\smt2024umn\\Data'
brennen_data_directory <- '/Users/brennenbruch/Documents/GitHub/smt2024umn/Data'
jack_data_directory <- '\\Users\\brick\\OneDrive\\Documents\\GitHub\\smt2024umn\\Data'

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


###############################################################################
################## STARTER CODE: DO NOT MODIFY ################################
###############################################################################

library(arrow)
library(dplyr)

game_info <- arrow::open_csv_dataset(paste0(data_directory,"/game_info"), 
                                     partitioning = c("Season", "HomeTeam", "AwayTeam", "Day"), 
                                     hive_style = F, 
                                     unify_schemas = T, 
                                     na = c("", "NA", "NULL", NA, "\\N"))

ball_pos <- arrow::open_csv_dataset(paste0(data_directory,"/ball_pos"), 
                                    partitioning = c("Season", "HomeTeam", "AwayTeam", "Day"), 
                                    hive_style = F, 
                                    unify_schemas = T, 
                                    na = c("", "NA", "NULL", NA, "\\N"))

game_events <- arrow::open_csv_dataset(paste0(data_directory,"/game_events"), 
                                       partitioning = c("Season", "HomeTeam", "AwayTeam", "Day"), 
                                       hive_style = F, 
                                       unify_schemas = T, 
                                       na = c("", "NA", "NULL", NA, "\\N"))

player_pos <- arrow::open_csv_dataset(paste0(data_directory,"/player_pos"), 
                                      partitioning = c("Season", "HomeTeam", "AwayTeam", "Day"), 
                                      hive_style = F, 
                                      unify_schemas = T, 
                                      na = c("", "NA", "NULL", NA, "\\N"))

team_info <- arrow::open_csv_dataset(paste0(data_directory,"/team_info.csv"), 
                                     hive_style = F, 
                                     unify_schemas = T, 
                                     na = c("", "NA", "NULL", NA, "\\N"))

###############################################################################
########################## END STARTER CODE ###################################
###############################################################################

#game_info_demo <- game_info |>
#   filter(Day == "day_059", 
#          inning == 3) |> 
#   collect()
