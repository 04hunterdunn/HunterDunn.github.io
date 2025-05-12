library(dplyr)

# Read the first CSV file
df1 <- read.csv("C:/Users/lwstr/OneDrive/Documents/GitHub/smt2024umn/Dashboard/ppo1_db.csv")

# Read the second CSV file
df2 <- read.csv("C:/Users/lwstr/OneDrive/Documents/GitHub/smt2024umn/Dashboard/tendency_data/pitcher_pickoff_1st_lead_tendency.csv")

df1_filtered <- df1 %>%
  select(pitcher) %>%
  distinct()

# Filter df2 based on player_id and level from df1
df2_filtered <- df2 %>%
  inner_join(df1_filtered, by = c("player_id", "level"))

# Identify rows in df1_filtered that do not have matching rows in df2_filtered
unmatched_rows <- df1_filtered %>%
  anti_join(df2_filtered, by = c("player_id", "level"))

df1_filtered_final <- df1 %>%
  anti_join(unmatched_rows, by = c("player_id", "level"))

output_file_path <- "C:/Users/lwstr/OneDrive/Documents/GitHub/smt2024umn/Dashboard/pitcher_catcher_db.csv"

# Write df1_filtered_final to a CSV file
write.csv(df1_filtered_final, file = output_file_path, row.names = FALSE)
