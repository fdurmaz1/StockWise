import pandas_datareader as pdr
from pandas_datareader.nasdaq_trader import get_nasdaq_symbols

def get_stock_symbols_with_names():
    nasdaq_symbols = get_nasdaq_symbols()
    stock_list_with_names = nasdaq_symbols[['Security Name']].loc[nasdaq_symbols.index.isin(get_nasdaq_symbols().index)]

    formatted_output = []

    for index, row in stock_list_with_names.iterrows():
        symbol = index
        if not isinstance(row['Security Name'], float):  # Check if the value is not a float
            name_parts = row['Security Name'].split(' ')[:3]  # Take the first three words
            shortened_name = ' '.join(name_parts)
        else:
            shortened_name = str(row['Security Name'])  # Convert float to string
        formatted_output.append(f"{symbol} {shortened_name}")  # Removed the dash

    return formatted_output

