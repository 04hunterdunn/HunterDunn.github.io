import pandas as pd
import numpy as np
import sqlite3
import pandas as pd
import pyarrow.dataset as pads
import pyarrow.compute as pc
import os
from SMT_data_starter import readDataSubset

def filter_df(cur_catcher, cur_level):
    conn = sqlite3.connect(':memory:')

    game_info_pull = readDataSubset('game_info')
    filter_criteria = (
        (pads.field("home_team") == cur_level) &
        (pads.field('catcher') == cur_catcher) &
        pc.is_valid(pads.field('first_baserunner')) &
        (pads.field('second_baserunner').is_null())
    )
    
    game_info_pull = game_info_pull.to_table(filter=filter_criteria, columns=["game_str","home_team","play_per_game","pitcher","catcher","first_baserunner","second_baserunner","third_baserunner"]).to_pandas()


    # Change to compatible col names
    game_info_pull.rename(columns={"play_per_game": "play_id"}, inplace=True)
    cols = ["game_str", "play_id"]
    game_info_pull["play_id"] = game_info_pull["play_id"]

    # Game Events Join
    game_events = readDataSubset("game_events")

    filter_criteria = (
        (pads.field("event_code") == 2) &
        (pads.field('player_position') == 2)
    )

    game_events = game_events.to_table(filter=filter_criteria, columns=["game_str","play_id","timestamp","player_position","event_code"]).to_pandas()

    game_events = game_events.rename(columns={
        'player_position': 'event_player_position'
    })

    game_events_pull = pd.merge(game_info_pull, game_events, on=cols, how='inner')

    # Player Position Join
    player_pos = readDataSubset("player_pos")

    filter_criteria = (
        (pads.field('player_position') == 11) # Positioning for first_baserunner
    )

    player_pos = player_pos.to_table(filter=filter_criteria, columns = ["game_str","play_id","timestamp","field_x","field_y"]).to_pandas()

    cols = ["game_str", "play_id", "timestamp"]
    player_pos_pull = pd.merge(game_events_pull, player_pos, on=cols, how='inner')

    player_pos_pull.to_sql('player_pos_pull', conn, index=False, if_exists='replace')

    # Assume 1st Base is at (62.58, 63.64)
    query = """
    SELECT
        catcher AS player_id,
        home_team AS level,
        AVG(SQRT(POWER(field_x - 62.58, 2) + POWER(field_y - 63.64, 2))) AS avg_lead,
        COUNT(*) AS total_chances
    FROM (
        SELECT
            catcher,
            game_str,
            play_id,
            home_team,
            field_x,
            field_y
        FROM player_pos_pull
    ) AS subquery
    GROUP BY catcher, home_team;
    """
    game_info_pull.to_sql('game_events_pull', conn, index=False, if_exists='replace')
    df = pd.read_sql_query(query, conn)

    print(df)

    conn.close()
    return df

def main():
    conn = sqlite3.connect(':memory:')
    # Parameters
    player_dataframe = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//pitcher_catcher_db_catcher_miss.csv")

    cpo1 = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//cpo1_db.csv")
    cpo1['level'] = cpo1['game_str'].str.split('_').str[3]

    cpo1.to_sql('cpo1', conn, index=False, if_exists='replace')

    conn.close()

    resultlist = []

    # Initialize final CSV if it doesn't exist
    file_path = "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\catcher_pickoff_1st_lead_tendency.csv"

    if not os.path.exists(file_path):
        pd.DataFrame(columns=['player_id', 'level', 'avg_lead', 'total_chances']).to_csv(file_path, index=False)

        # Read the existing CSV to keep track of processed player-level combinations
    existing_df = pd.read_csv(file_path)
    processed_combos = set(zip(existing_df['player_id'], existing_df['level']))

    print(len(player_dataframe))

    for i, row in player_dataframe.iterrows():
        player_id = row['player_id']
        #player_position = row['position']
        player_level = row['level']
        
        print(player_id)  # To track progress

        # Skip if the player-level combo has already been processed
        if (player_id, player_level) in processed_combos:
            print(f"Skipping already processed player-level combo: {player_id}-{player_level}")
            continue
        
        print("catcher")
        df = filter_df(player_id, player_level)

        print(df)

        resultlist.append(df)

        # Append the new data to the existing CSV
        df.to_csv(file_path, mode='a', header=False, index=False)
        
        # Update the set of processed combos
        processed_combos.add((player_id, player_level))

    df = pd.concat(resultlist, ignore_index=True)

    '''        
    # Calculate the Bayesian Adjustment for percent_pickoff
    # (Used to favor larger sample sizes. Will be good for differentiating players in percentiles)
    k_adjustor = 50
    prior_probability = df['total_pickoffs'].sum() / df['total_plays'].sum()
    df['bayesian_percent_pickoff'] = (df["total_pickoffs"] + k_adjustor * prior_probability) / (df["total_plays"] + k_adjustor)    
    '''
    df.to_csv(file_path, header=True, index=False)

    
if __name__ == "__main__":
    main()
