import pandas as pd
import numpy as np
import sqlite3
import pandas as pd
import pyarrow.dataset as pads
import pyarrow.compute as pc
import os
from SMT_data_starter import readDataSubset

def filter_df(cur_pitcher, cur_level, game_str, play_id):
    conn = sqlite3.connect(':memory:')

    # Game Events Join
    game_events = readDataSubset("game_events")

    filter_criteria = (
        (pads.field('game_str') == game_str) & 
        (pads.field('play_id') == play_id) &
        ((pads.field("event_code") == 1) & (pads.field('player_position') == 1) |  # Event code 1 and player position 1
        ((pads.field('player_position') == 2) & (pads.field('event_code') == 2)))  # OR player position 2 and event code 2
    )

    game_events = game_events.to_table(filter=filter_criteria, columns=["game_str","play_id","timestamp","player_position","event_code"]).to_pandas()

    game_events = game_events.rename(columns={
        'player_position': 'event_player_position'
    })

    print(game_events)
    if(len(game_events) < 2):
        return None
    initial_timestamp = game_events.loc[0, 'timestamp']

    final_timestamp = game_events.loc[1, 'timestamp']
    deliverytime = final_timestamp - initial_timestamp

    conn.close()
    return deliverytime

def main():
    conn = sqlite3.connect(':memory:')
    # Parameters
    player_dataframe = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//pitcher_catcher_db.csv")

    del3 = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//sb3_db.csv")
    del3['level'] = del3['game_str'].str.split('_').str[3]

    resultlist = []

    # Initialize final CSV if it doesn't exist
    file_path = "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\deliverytime3.csv"

    if not os.path.exists(file_path):
        pd.DataFrame(columns=['player_id', 'level', 'deliverytime', 'total_chances']).to_csv(file_path, index=False)

    print(len(player_dataframe))

    for i, row in del3.iterrows():
        player_id = row['pitcher']
        player_position = "P"
        player_level = row['level']
        play_id = row["play_per_game"]
        game_str = row['game_str']
        
        print(player_id)  # To track progress
        
        if player_position == "P":
            print("pitcher")
            deliverytime = filter_df(player_id, player_level, game_str, play_id)
            print(deliverytime)

            resultlist.append(deliverytime)

    del3['deliverytime'] = resultlist
    
    # Write DataFrame to a CSV file
    del3.to_csv(file_path, index=False)

    del3 = del3[del3['deliverytime'] < 1200]

    del3.to_sql('del3', conn, index=False, if_exists='replace')

    query = """
    SELECT
        pitcher AS player_id,
        level AS level,
        COUNT(*) AS total_plays,
        AVG(deliverytime) AS avg_deliverytime
    FROM (
        SELECT DISTINCT
            pitcher,
            deliverytime,
            level
        FROM del3
    ) AS subquery
    GROUP BY player_id, level;
    """

    df = pd.read_sql_query(query, conn)

    conn.close()
    
    # Write DataFrame to a CSV file
    df.to_csv(file_path, index=False)

    
if __name__ == "__main__":
    main()
