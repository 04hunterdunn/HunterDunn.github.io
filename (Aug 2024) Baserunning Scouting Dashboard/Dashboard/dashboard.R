#library(shiny)
library(shinydashboard)
library(shinythemes)
library(dplyr)
library(readr)
library(sqldf)
library(purrr)
library(highcharter)
library(shinyBS)

source("fun_with_fps_wain.R")

# Define UI
ui <- dashboardPage(
  dashboardHeader(title = "Baserunning Scouting Dashboard"),
  dashboardSidebar(disable = TRUE),  # Disable the sidebar if not used
  dashboardBody(
    tags$head(
      tags$link(rel = "stylesheet", type = "text/css", href = "styles.css")
    ),
    theme = shinytheme("cerulean"),
    
    # Accordion at the top
    bsCollapsePanel("First Time? Introduction and How to Use This Dashboard",
                    p("Baserunning Tendency Scouting Dashboard for SMT Data Challenge 2024 (Team ID: 31)"),
                    tags$ul(
                      tags$li("Provides scouting information on baserunning tendencies."),
                      tags$li("The dashboard is best viewed in full screen."),
                      tags$li("Use tabs to switch between pitchers and catchers."),
                      tags$li("Search for a pitcher or catcher in the search bar."),
                      tags$li("Select a play type to view unique baserunning situations."),
                      tags$li("Player tendencies on the right correspond to the selected player."),
                      tags$li("Green tendencies mean the player is in the upper quartile of this metric among players at the selected farm level. Red is the bottom quartile."),
                      tags$li("Level tendencies are weighted average values for a given farm level, corresponding to the selected level."),
                      tags$li("The main goals of this dashboard are to offer a comparison point for game scouts on players they intend to go up against, and for players to be more aware of underlying tendencies they may exhibit towards baserunners."),
                      tags$li("Some good example pitchers and catchers to look at are: P 722, P 549, C 530, C 544")
                    ),
                    style = "info"
    ),
    
    tabsetPanel(
      tabPanel("Pitcher",
        # Create Initial Row
        fluidRow(
          div(class = "column-container",
              # Create search bar and play selection on left side
              box(title = NULL, status = NULL, solidHeader = FALSE, width = 3,selectInput("play_type_pitch", "Select Play Type to View", choices = c("Stolen Base Attempt 2nd", "Stolen Base Attempt 3rd", "Pickoff Attempt 1st", "Pickoff Attempt 2nd")),
                  selectInput("level_statistics_pitch", "Select Farm Level for Level Tendencies", choices = c("1A", "2A", "3A", "4A")),
                  textInput("search_pitch", "Search for Pitcher or Catcher:", ""),
                  uiOutput("search_results_pitch"),
                  class = "column column-first"
              ),
              
              # Visual of baseball field & play
              box(title = NULL, status = NULL, solidHeader = FALSE, width = 4,
                  imageOutput("animation_gif_pitch"),
                  class = "column custom-box"
              ),
              
              # Box for Level Statistics and Stat Tables
              box(title = NULL, status = NULL, solidHeader = FALSE, width = 5,
                  fluidRow(
                    column(width = 6,
                           box(title = NULL, status = NULL, solidHeader = FALSE, width = NULL,
                               div(class = "divider-title", "Pitcher Tendencies:"),
                               tags$div(
                                 style = "display: flex; flex-direction: column; gap: 10px;",  # Flexbox container for vertical layout
                                 tags$div(
                                   tags$p("AVG Lead Allowed 1st:"),
                                   uiOutput(outputId = "outLead1st_pl")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("AVG Lead Allowed 2nd:"),
                                   uiOutput(outputId = "outLead2nd_pl")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Pickoff Percentage 1st:"),
                                   uiOutput(outputId = "outPickoff1st_pl")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Pickoff Percentage 2nd:"),
                                   uiOutput(outputId = "outPickoff2nd_pl")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Delivery Time on Steals of 2nd:"),
                                   uiOutput(outputId = "outDelivery2nd_pl")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Delivery Time on Steals of 3rd:"),
                                   uiOutput(outputId = "outDelivery3rd_pl")
                                 ) %>% tagAppendAttributes(class = "stat-card")
                               ) %>% tagAppendAttributes(class = "stat-card-container"),
                               class = "main-container"
                           )
                    ),
                    column(width = 6,
                           box(title = NULL, status = NULL, solidHeader = FALSE, width = NULL,
                               div(class = "divider-title", "Level Tendencies:"),
                               tags$div(
                                 style = "display: flex; flex-direction: column; gap: 10px;",
                                 tags$div(
                                   tags$p("AVG Lead Allowed 1st:"),
                                   uiOutput(outputId = "outLead1st_l")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("AVG Lead Allowed 2nd:"),
                                   uiOutput(outputId = "outLead2nd_l")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Pickoff Percentage 1st:"),
                                   uiOutput(outputId = "outPickoff1st_l")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Pickoff Percentage 2nd:"),
                                   uiOutput(outputId = "outPickoff2nd_l")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Delivery Time on Steals of 2nd:"),
                                   uiOutput(outputId = "outDelivery2nd_l")
                                 ) %>% tagAppendAttributes(class = "stat-card"),
                                 tags$div(
                                   tags$p("Delivery Time on Steals of 3rd:"),
                                   uiOutput(outputId = "outDelivery3rd_l")
                                 ) %>% tagAppendAttributes(class = "stat-card")
                               ) %>% tagAppendAttributes(class = "stat-card-container"),
                               class = "main-container"
                      )
                  )
                )
            )
          )
        )
      ),
      tabPanel("Catcher",
               # Create Initial Row
               fluidRow(
                 div(class = "column-container",
                     # Create search bar and play selection on left side
                     box(title = NULL, status = NULL, solidHeader = FALSE, width = 3,
                         selectInput("play_type_catch", "Select Play Type to View", choices = c("Stolen Base Attempt 2nd", "Stolen Base Attempt 3rd", "Pickoff Attempt 1st", "Pickoff Attempt 2nd")),
                         selectInput("level_statistics_catch", "Select Farm Level for Level Tendencies", choices = c("1A", "2A", "3A", "4A")),
                         textInput("search_catch", "Search for Pitcher or Catcher:", ""),
                         uiOutput("search_results_catch"),
                         class = "column column-first"
                     ),
                     
                     # Visual of baseball field & play
                     box(title = NULL, status = NULL, solidHeader = FALSE, width = 4,
                         imageOutput("animation_gif_catch"),
                         class = "column custom-box"
                     ),
                     
                     # Box for Level Statistics and Stat Tables
                     box(title = NULL, status = NULL, solidHeader = FALSE, width = 5,
                         fluidRow(
                           column(width = 6,
                                  box(title = NULL, status = NULL, solidHeader = FALSE, width = NULL,
                                      div(class = "divider-title", "Pitcher Tendencies:"),
                                      tags$div(
                                        style = "display: flex; flex-direction: column; gap: 10px;",  # Flexbox container for vertical layout
                                        tags$div(
                                          tags$p("AVG Lead Allowed 1st:"),
                                          uiOutput(outputId = "outLead1st_cl")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("AVG Lead Allowed 2nd:"),
                                          uiOutput(outputId = "outLead2nd_cl")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pickoff Percentage 1st:"),
                                          uiOutput(outputId = "outPickoff1st_cl")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pickoff Percentage 2nd:"),
                                          uiOutput(outputId = "outPickoff2nd_cl")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pop Time on Steals of 2nd:"),
                                          uiOutput(outputId = "outDelivery2nd_cl")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pop Time on Steals of 3rd:"),
                                          uiOutput(outputId = "outDelivery3rd_cl")
                                        ) %>% tagAppendAttributes(class = "stat-card")
                                      ) %>% tagAppendAttributes(class = "stat-card-container"),
                                      class = "main-container"
                                  )
                           ),
                           column(width = 6,
                                  box(title = NULL, status = NULL, solidHeader = FALSE, width = NULL,
                                      div(class = "divider-title", "Level Tendencies:"),
                                      tags$div(
                                        style = "display: flex; flex-direction: column; gap: 10px;",
                                        tags$div(
                                          tags$p("AVG Lead Allowed 1st:"),
                                          uiOutput(outputId = "outLead1st_lc")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("AVG Lead Allowed 2nd:"),
                                          uiOutput(outputId = "outLead2nd_lc")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pickoff Percentage 1st:"),
                                          uiOutput(outputId = "outPickoff1st_lc")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pickoff Percentage 2nd:"),
                                          uiOutput(outputId = "outPickoff2nd_lc")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pop Time on Steals of 2nd:"),
                                          uiOutput(outputId = "outDelivery2nd_lc")
                                        ) %>% tagAppendAttributes(class = "stat-card"),
                                        tags$div(
                                          tags$p("Pop Time on Steals of 3rd:"),
                                          uiOutput(outputId = "outDelivery3rd_lc")
                                        ) %>% tagAppendAttributes(class = "stat-card")
                                      ) %>% tagAppendAttributes(class = "stat-card-container"),
                                      class = "main-container"
                                  )
                           )
                         )
                     )
                 )
               )
      )
    )
  )
)

# Define server function  
server <- function(input, output, session) {
  # Load initial data and format buttons (ID POS LEVEL)
  p_df <- read_csv("pitcher_catcher_db.csv") %>%
    mutate(search_string = paste(player_id, position, level, sep = " ")) %>%
    select(player_id, position, level, search_string) %>%
    filter(position == "P") %>%
    distinct()
    
  c_df <- read_csv("pitcher_catcher_db.csv") %>%
      mutate(search_string = paste(player_id, position, level, sep = " ")) %>%
      select(player_id, position, level, search_string) %>%
      filter(position == "C") %>%
      distinct()
  
  # Reactive expression to filter the search options based on search query for pitchers
  filtered_data_pitch <- reactive({
    req(input$search_pitch)
    p_df %>%
      filter(grepl(input$search_pitch, search_string, ignore.case = TRUE))
  })
  
  # Reactive expression to filter the search options based on search query for catchers
  filtered_data_catch <- reactive({
    req(input$search_catch)
    c_df %>%
      filter(grepl(input$search_catch, search_string, ignore.case = TRUE))
  })
  
  # Output pitcher filtered search results (Gives options that match the search query to chose from)
  output$search_results_pitch <- renderUI({
    req(filtered_data_pitch())
    data <- filtered_data_pitch()
    if (nrow(data) == 0) return(NULL)
    
    actionButtons <- lapply(1:nrow(data), function(i) {
      # Labels for buttons for user to click on
      label_text <- paste(data$player_id[i], data$position[i], data$level[i], sep = " ")
      actionButton(
        inputId = paste0("result_", i),
        label = label_text,
        # Trigger data change on new click
        onclick = sprintf("Shiny.onInputChange('%s', '%s')", "player_selection_pitch", label_text)
      )
    })
    do.call(tagList, actionButtons)
  })
  
  # Output catcher filtered search results (Gives options that match the search query to chose from)
  output$search_results_catch <- renderUI({
    req(filtered_data_catch())
    data <- filtered_data_catch()
    if (nrow(data) == 0) return(NULL)
    
    actionButtons <- lapply(1:nrow(data), function(i) {
      # Labels for buttons for user to click on
      label_text <- paste(data$player_id[i], data$position[i], data$level[i], sep = " ")
      actionButton(
        inputId = paste0("result_", i),
        label = label_text,
        # Trigger data change on new click
        onclick = sprintf("Shiny.onInputChange('%s', '%s')", "player_selection_catch", label_text)
      )
    })
    do.call(tagList, actionButtons)
  })
  
  # Levelwise Pitcher's Lead off of First
  output$outLead1st_l <- renderUI({
    req(input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pitcher_pickoff_1st_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Levelwise Catcher's Lead off of First
  output$outLead1st_lc <- renderUI({
    req(input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_1st_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Levelwise Pitcher's Lead off of Second
  output$outLead2nd_l <- renderUI({
    req(input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pitcher_pickoff_2nd_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Levelwise Catcher's Lead off of Second
  output$outLead2nd_lc <- renderUI({
    req(input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_2nd_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Levelwise Pickoff % for Pitchers to First
  output$outPickoff1st_l <- renderUI({
    req(input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pickoff_first_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg * 100, 2), "%", '</div>'))
  })
  
  # Levelwise Pickoff % for Catchers to First
  output$outPickoff1st_lc <- renderUI({
    req(input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_first_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg * 100, 2), "%", '</div>'))
  })
  
  # Levelwise Pickoff % for Pitchers to Second
  output$outPickoff2nd_l <- renderUI({
    req(input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pickoff_second_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg * 100, 2), "%" ,'</div>'))
  })
  
  # Levelwise Pickoff % for Catchers to Second
  output$outPickoff2nd_lc <- renderUI({
    req(input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_second_tendency.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg * 100, 2), "%", '</div>'))
  })
  
  # Levelwise delivery time for pitchers on steals of second
  output$outDelivery2nd_l <- renderUI({
    req(input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/deliverytime2.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_deliverytime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Levelwise pop time for catchers on steals of second
  output$outDelivery2nd_lc <- renderUI({
    req(input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/poptime_2.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_poptime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Levelwise delivery time for pitchers on steals of third
  output$outDelivery3rd_l <- renderUI({
    req(input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/deliverytime3.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_deliverytime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Levelwise pop time for catchers on steals of third
  output$outDelivery3rd_lc <- renderUI({
    req(input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/poptime_3.csv")
    
    filtered_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_poptime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Define global reactive value list for player data
  selected_player_pitch <- reactiveValues(info = NULL)
  selected_player_catch <- reactiveValues(info = NULL)
  
  # Update selected player info on pitcher selection
  observeEvent(input$player_selection_pitch, {
    selected_info <- unlist(strsplit(input$player_selection_pitch, " "))
    selected_player_id <- selected_info[1]
    selected_position <- selected_info[2]
    selected_level <- selected_info[3]
    
    # Store selected player information in reactive values
    selected_player_pitch$info <- list(
      player_id = as.numeric(selected_player_id),
      position = selected_position,
      level = selected_level
    )
  })
  
  # Update selected player info on catcher selection
  observeEvent(input$player_selection_catch, {
    selected_info <- unlist(strsplit(input$player_selection_catch, " "))
    selected_player_id <- selected_info[1]
    selected_position <- selected_info[2]
    selected_level <- selected_info[3]
    
    # Store selected player information in reactive values
    selected_player_catch$info <- list(
      player_id = as.numeric(selected_player_id),
      position = selected_position,
      level = selected_level
    )
  })
  
  # Individual Pitcher lead off of 1st
  output$outLead1st_pl <- renderUI({
    req(selected_player_pitch$info, input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pitcher_pickoff_1st_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_pitch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    quantiles <- quantile(level_df$avg_lead, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Individual Catcher lead off of 1st
  output$outLead1st_cl <- renderUI({
    req(selected_player_catch$info, input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_1st_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_catch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    quantiles <- quantile(level_df$avg_lead, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Individual Pitcher Lead off 2nd
  output$outLead2nd_pl <- renderUI({
    req(selected_player_pitch$info, input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pitcher_pickoff_2nd_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_pitch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    quantiles <- quantile(level_df$avg_lead, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Individual Catcher lead off of 2nd
  output$outLead2nd_cl <- renderUI({
    req(selected_player_catch$info, input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_2nd_lead_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_catch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    quantiles <- quantile(level_df$avg_lead, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_chances
    avg_lead <- filtered_df$avg_lead
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(avg_lead * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg, 2), "ft", '</div>'))
  })
  
  # Individual pitcher pickoff % 1st
  output$outPickoff1st_pl <- renderUI({
    req(selected_player_pitch$info, input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pickoff_first_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_pitch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    quantiles <- quantile(level_df$percent_pickoff, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg > quantiles[2]){
      color = 'green'
    }
    else if(weighted_avg < quantiles[1]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg * 100, 2), "%", '</div>'))
  })
  
  # Individual catcher pickoff % 1st
  output$outPickoff1st_cl <- renderUI({
    req(selected_player_catch$info, input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_first_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_catch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    quantiles <- quantile(level_df$percent_pickoff, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg > quantiles[2]){
      color = 'green'
    }
    else if(weighted_avg < quantiles[1]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg * 100, 2), "%", '</div>'))
  })
  
  # Individual pitcher pickoff % second
  output$outPickoff2nd_pl <- renderUI({
    req(selected_player_pitch$info, input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/pickoff_second_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_pitch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    quantiles <- quantile(level_df$percent_pickoff, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg > quantiles[2]){
      color = 'green'
    }
    else if(weighted_avg < quantiles[1]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg * 100, 2), "%" ,'</div>'))
  })
  
  # Individual catcher pickoff % 2nd
  output$outPickoff2nd_cl <- renderUI({
    req(selected_player_catch$info, input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/catcher_pickoff_second_tendency.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_catch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    quantiles <- quantile(level_df$percent_pickoff, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    percent_pick <- filtered_df$percent_pickoff
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(percent_pick * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg > quantiles[2]){
      color = 'green'
    }
    else if(weighted_avg < quantiles[1]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg * 100, 2), "%", '</div>'))
  })
  
  # Individual Pitcher delivery time on steals to second
  output$outDelivery2nd_pl <- renderUI({
    req(selected_player_pitch$info, input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/deliverytime2.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_pitch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    quantiles <- quantile(level_df$avg_deliverytime, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_deliverytime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Individual catcher delivery time on steals to second
  output$outDelivery2nd_cl <- renderUI({
    req(selected_player_catch$info, input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/poptime_2.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_catch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    quantiles <- quantile(level_df$avg_poptime, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_poptime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Individual Pitcher delivery time on steals to third
  output$outDelivery3rd_pl <- renderUI({
    req(selected_player_pitch$info, input$level_statistics_pitch)
    
    df <- read.csv("./tendency_data/deliverytime3.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_pitch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_pitch)
    
    quantiles <- quantile(level_df$avg_deliverytime, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_deliverytime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Individual catcher delivery time on steals to third
  output$outDelivery3rd_cl <- renderUI({
    req(selected_player_catch$info, input$level_statistics_catch)
    
    df <- read.csv("./tendency_data/poptime_3.csv")
    
    filtered_df <- df %>%
      filter(player_id == selected_player_catch$info$player_id)
    
    level_df <- df %>%
      filter(level == input$level_statistics_catch)
    
    quantiles <- quantile(level_df$avg_poptime, probs = c(0.25, 0.75))
    
    total_chances <- filtered_df$total_plays
    del_time <- filtered_df$avg_poptime
    total_plays <- sum(total_chances)
    
    # Calculate weighted average
    weighted_avg <- sum(del_time * total_chances) / total_plays
    
    if(is.na(weighted_avg) || is.nan(weighted_avg)) {
      return(HTML('<div class="stat-card">N/A</div>'))
    }
    
    if(weighted_avg < quantiles[1]){
      color = 'green'
    }
    else if(weighted_avg > quantiles[2]){
      color = 'red'
    }
    else{
      color = 'black'
    }
    
    # Return the weighted average with a CSS class
    HTML(paste('<div class="stat-card" style="color:', color, ';">', round(weighted_avg / 1000, 4), "s" ,'</div>'))
  })
  
  # Create gif of play to be presented on dashboard for pitchers
  render_gif_pitch <- reactive({
    req(selected_player_pitch$info, input$play_type_pitch)
    
    selected_player_id <- selected_player_pitch$info$player_id
    selected_position <- selected_player_pitch$info$position
    
    play_file <- switch(input$play_type_pitch,
                        "Pickoff Attempt 1st" = "ppo1_db.csv",
                        "Pickoff Attempt 2nd" = "ppo2_db.csv",
                        "Stolen Base Attempt 2nd" = "sb2_db.csv",
                        "Stolen Base Attempt 3rd" = "sb3_db.csv")
    
    df <- read_csv(play_file)
    
    filtered_df <- if (selected_position == "P") {
      sqldf(paste0("SELECT * FROM df WHERE pitcher = '", selected_player_id, "'"))
    } else {
      sqldf(paste0("SELECT * FROM df WHERE catcher = '", selected_player_id, "'"))
    }
    
    print(nrow(filtered_df))
    
    if (nrow(filtered_df) == 0) {
      return(list(src = "www/error_message.png", contentType = 'image/png', alt = "Error Message"))
    }
    
    elements <- filtered_df[1, ]
    game_str <- elements$game_str
    play_per_game <- elements$play_per_game
    file_name <- sprintf("www/%s_%d.gif", game_str, play_per_game)
    
    if (!file.exists(file_name)) {
      animate_play(game_str, play_per_game)
    }
    
    list(src = file_name, contentType = 'image/gif', alt = "gganimate gif")
  })
  
  # Create gif of play to be presented on dashboard for catchers
  render_gif_catch <- reactive({
    req(selected_player_catch$info, input$play_type_catch)
    
    selected_player_id <- selected_player_catch$info$player_id
    selected_position <- selected_player_catch$info$position
    
    play_file <- switch(input$play_type_catch,
                        "Pickoff Attempt 1st" = "cpo1_db.csv",
                        "Pickoff Attempt 2nd" = "cpo2_db.csv",
                        "Stolen Base Attempt 2nd" = "sb2_db.csv",
                        "Stolen Base Attempt 3rd" = "sb3_db.csv")
    
    
    df <- read_csv(play_file)
    
    filtered_df <- if (selected_position == "P") {
      sqldf(paste0("SELECT * FROM df WHERE pitcher = '", selected_player_id, "'"))
    } else {
      sqldf(paste0("SELECT * FROM df WHERE catcher = '", selected_player_id, "'"))
    }
    
    if (nrow(filtered_df) == 0) {
      return(list(src = "www/error_message.png", contentType = 'image/png', alt = "Error Message"))
    }
    
    elements <- filtered_df[1, ]
    game_str <- elements$game_str
    play_per_game <- elements$play_per_game
    file_name <- sprintf("www/%s_%d.gif", game_str, play_per_game)
    
    if (!file.exists(file_name)) {
      animate_play(game_str, play_per_game)
    }
    
    list(src = file_name, contentType = 'image/gif', alt = "gganimate gif")
  })
  
  output$animation_gif_pitch <- renderImage({
    render_gif_pitch()
  }, deleteFile = FALSE)
  
  output$animation_gif_catch <- renderImage({
    render_gif_catch()
  }, deleteFile = FALSE)
}

# Create Shiny object
shinyApp(ui = ui, server = server)