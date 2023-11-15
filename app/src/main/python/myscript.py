import pandas_datareader as pdr
from pandas_datareader.nasdaq_trader import get_nasdaq_symbols
import datetime as dt
import yfinance as yfin

# def get_stock_symbols_with_names():
#     nasdaq_symbols = get_nasdaq_symbols()
#     stock_list_with_names = nasdaq_symbols[['Security Name']].loc[nasdaq_symbols.index.isin(get_nasdaq_symbols().index)]
#
#     formatted_output = []
#
#     for index, row in stock_list_with_names.iterrows():
#         symbol = index
#         if not isinstance(row['Security Name'], float):  # Check if the value is not a float
#             name_parts = row['Security Name'].split(' ')[:3]  # Take the first three words
#             shortened_name = ' '.join(name_parts)
#         else:
#             shortened_name = str(row['Security Name'])  # Convert float to string
#         formatted_output.append(f"{symbol} {shortened_name}")  # Removed the dash
#
#     return formatted_output

def get_stock_symbols_with_names():
    nasdaq_symbols = get_nasdaq_symbols()

    # Store the result of the function call in a variable to avoid repetition
    stock_list_with_names = nasdaq_symbols[['Security Name']]

    formatted_output = [
        f"{symbol} - {' '.join(security_name.split()[:4])}" if isinstance(security_name, str) else f"{symbol} - {str(security_name)}"
        for symbol, security_name in stock_list_with_names.itertuples(index=True, name=None)
    ]

    return formatted_output


def get_recent_close_price(stock_symbol):
    try:
        end = dt.datetime.now()
        start = end - dt.timedelta(days=1)

        yfin.pdr_override()
        df = pdr.data.get_data_yahoo(stock_symbol, start, end)

        # Check if the DataFrame is not empty
        if not df.empty and 'Close' in df.columns:
            # Check if there are any rows in the 'Close' series
            if not df['Close'].empty:
                # Access the latest close price
                latest_close_price = df['Close'].iloc[-1]
                return latest_close_price
            else:
                print("Error: 'Close' series is empty.")
        else:
            print(f"Error: DataFrame is empty or 'Close' column not found for {stock_symbol}")

    except Exception as e:
        print(f"Error fetching close price for {stock_symbol}: {e}")

    # Return a default value or raise an exception based on your app's logic
    return -1
