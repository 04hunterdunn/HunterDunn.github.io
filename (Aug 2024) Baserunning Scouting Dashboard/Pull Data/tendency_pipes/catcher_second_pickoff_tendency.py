import pandas as pd
import numpy as np
import sqlite3
import pandas as pd
import pyarrow.dataset as pads
import pyarrow.compute as pc
import os
from SMT_data_starter import readDataSubset

def filter_df(cur_catcher, cur_level, pickoff_totals):
    conn = sqlite3.connect(':memory:')

    cur_level = "Home" + cur_level

    game_info_pull = readDataSubset('game_info')
    filter_criteria = (
        (pads.field("home_team") == cur_level) &
        (pads.field('catcher') == cur_catcher) &
        pc.is_valid(pads.field('second_baserunner'))
    )
    
    game_info_pull = game_info_pull.to_table(filter=filter_criteria, columns=["game_str","home_team","play_per_game","pitcher","catcher","first_baserunner","second_baserunner","third_baserunner"]).to_pandas()

    # Aggregation
    query = """
    SELECT
        catcher AS player_id,
        home_team AS level,
        COUNT(*) AS total_plays
    FROM (
        SELECT DISTINCT
            catcher,
            game_str,
            play_per_game,
            home_team
        FROM game_events_pull
    ) AS subquery
    GROUP BY catcher, home_team;
    """
    game_info_pull.to_sql('game_events_pull', conn, index=False, if_exists='replace')

    df = pd.read_sql_query(query, conn)

    print(df)

    new_df = pd.merge(df, pickoff_totals, on=['player_id', 'level'], how='left')
    new_df = new_df.fillna(0)
    new_df['percent_pickoff'] = new_df['total_pickoffs'] / new_df['total_plays']

    conn.close()
    return new_df

def main():
    conn = sqlite3.connect(':memory:')
    # Parameters
    player_dataframe = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//pitcher_catcher_db.csv")


    cpo2 = pd.read_csv("C://Users//lwstr//OneDrive//Documents//GitHub//smt2024umn//Dashboard//cpo2_db.csv")
    cpo2['level'] = cpo2['game_str'].str.split('_').str[3]

    cpo2.to_sql('cpo2', conn, index=False, if_exists='replace')


    query = """
        SELECT catcher AS player_id,
        level,
        COUNT(*) AS total_pickoffs
        FROM cpo2
        WHERE catcher IS NOT NULL
        GROUP BY player_id, level
    """
    pickoff_totals = pd.read_sql_query(query, conn)
    print(pickoff_totals)

    conn.close()

    resultlist = []

    # Initialize final CSV if it doesn't exist
    file_path = "C:\\Users\\lwstr\\OneDrive\\Documents\\GitHub\\smt2024umn\\catcher_pickoff_second_tendency.csv"

    print(len(player_dataframe))

    for i, row in player_dataframe.iterrows():
        player_id = row['player_id']
        player_position = row['position']
        player_level = row['level']
        
        print(player_id)  # To track progress
        
        if player_position == "C":
            print("catcher")
            df = filter_df(player_id, player_level, pickoff_totals)
            
            # Calculate 'percent_pickoff' column before appending
            #df['percent_pickoff'] = df['total_pickoffs'] / df['total_plays']


            print(df)

            resultlist.append(df)

    df = pd.concat(resultlist, ignore_index=True)
            
    # Calculate the Bayesian Adjustment for percent_pickoff
    # (Used to favor larger sample sizes. Will be good for differentiating players in percentiles)
    k_adjustor = 50
    prior_probability = df['total_pickoffs'].sum() / df['total_plays'].sum()
    df['bayesian_percent_pickoff'] = (df["total_pickoffs"] + k_adjustor * prior_probability) / (df["total_plays"] + k_adjustor)    
    df.to_csv(file_path, header=True, index=False)

if __name__ == "__main__":
    main()
