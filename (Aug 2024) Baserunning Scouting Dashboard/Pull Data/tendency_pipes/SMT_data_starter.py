import pandas as pd
import pyarrow.dataset as pads
import os

"""
Welcome to the 2024 SMT Data Challenge! Here is a function to help you get
started. Ensure that Pandas and Pyarrow, and os are installed before proceeding. 
The functions included below define Pyarrow datasets that can be referenced 
elsewhere in your code for the purposes of querying data. 

Calling this function on a data subtype only creates a dataset, not a table or 
DataFrame. Instead, you should call something like this to convert the dataset
to a Pandas DataFrame:

    dataset.to_table().to_pandas()
    
Other examples of filter and select statements are included in the main function
below.

WARNING: The data subsets are large, especially `player_pos`. Reading the 
  entire subset at once without filtering may incur performance issues on your 
  machine or even crash your session. It is recommended that you include filters 
  when you query the datasets before saving them into the working environment. 
  See https://arrow.apache.org/docs/python/dataset.html#filtering-data for info 
  on how to filter Arrow datasets before bringing them into memory.
"""
    
def readDataSubset(table_type, data_path="2024_SMT_Data_Challenge"):
    if table_type not in ['ball_pos', 'game_events', 'game_info', 'player_pos', 'team_info']:
        print("Invalid data subset name. Please try again with a valid data subset.")
        return -1
    
    directory = None # PUT FILE DIRECTORY HERE
    
    if table_type == 'team_info':
        return pads.dataset(source = os.path.join(directory, 'team_info.csv'),
                            format = 'csv')

    else:
        return pads.dataset(source = os.path.join(directory, table_type),
                            format = 'csv',
                            partitioning = ['Season', 'HomeTeam', 'AwayTeam', 'Day'])

def main():
    # Read data subsest
    team_info_df = readDataSubset('team_info')

    # Define criteria to filter data subset on
    # You may pass this directly to the `filter` argument of `to_table()` if you choose
    filter_criteria = (pads.field("home_team") == 'Home1A') & (pads.field('team_year') < 1884)

    # Apply your row filters and column projection in the `to_table()` function,
    # then convert to Pandas DataFrame with `to_pandas()`
    filtered_df = team_info_df.to_table(filter=filter_criteria, columns=['home_team', 'away_team']).to_pandas()

    print(filtered_df)

if __name__ == "__main__":
    main()
