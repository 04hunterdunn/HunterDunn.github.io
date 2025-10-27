import pandas as pd
import numpy as np
import sqlite3
import pandas as pd
import pyarrow.dataset as pads
import pyarrow.compute as pc
import os
from SMT_data_starter import readDataSubset

def filter_df(cur_catcher, cur_level, game_str, play_id):
    conn = sqlite3.connect(':memory:')

    # Game Events Join
    game_events = readDataSubset("game_events")

    filter_criteria = (
        (pads.field("event_code") == 2) &
        ((pads.field('player_position') == 2) | (pads.field('player_position') == 5)) &
        (pads.field('game_str') == game_str) &
        (pads.field('play_id') == play_id)
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
    poptime = final_timestamp - initial_timestamp

    conn.close()
    return poptime

def main():
    conn = sqlite3.connect(':memory:')
    # Parameters
    player_dataframe = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//pitcher_catcher_db.csv")

    sb3 = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//sb3_db.csv")
    sb3['level'] = sb3['game_str'].str.split('_').str[3]

    resultlist = []

    # Initialize final CSV if it doesn't exist
    file_path = "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\poptime_3.csv"

    if not os.path.exists(file_path):
        pd.DataFrame(columns=['player_id', 'level', 'poptime', 'total_chances']).to_csv(file_path, index=False)

    print(len(player_dataframe))

    for i, row in sb3.iterrows():
        player_id = row['catcher']
        player_position = "C"
        player_level = row['level']
        play_id = row["play_per_game"]
        game_str = row['game_str']
        
        print(player_id)  # To track progress
        
        if player_position == "C":
            print("catcher")
            poptime = filter_df(player_id, player_level, game_str, play_id)
            print(poptime)

            resultlist.append(poptime)

    sb3['poptime'] = resultlist
    
    # Write DataFrame to a CSV file
    sb3.to_csv(file_path, index=False)

    sb3 = sb3[sb3['poptime'] < 4000]

    sb3.to_sql('sb3', conn, index=False, if_exists='replace')

    query = """
    SELECT
        catcher AS player_id,
        level AS level,
        COUNT(*) AS total_plays,
        AVG(poptime) AS avg_poptime
    FROM (
        SELECT DISTINCT
            catcher,
            poptime,
            level
        FROM sb3
    ) AS subquery
    GROUP BY player_id, level;
    """

    df = pd.read_sql_query(query, conn)

    conn.close()
    
    # Write DataFrame to a CSV file
    df.to_csv(file_path, index=False)

    
if __name__ == "__main__":
    main()
