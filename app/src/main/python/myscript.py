import pandas_datareader as pdr
from pandas_datareader.nasdaq_trader import get_nasdaq_symbols
import datetime as dt
import yfinance as yfin
import pandas as pd

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
        start = end - dt.timedelta(days=5)

        yfin.pdr_override()
        df = pdr.data.get_data_yahoo(stock_symbol, start, end)


        if not df.empty and 'Close' in df.columns:
            if not df['Close'].empty:
                latest_close_price = df['Close'].iloc[-1] if not pd.isna(df['Close'].iloc[-1]) else None
                second_latest_close_price = df['Close'].iloc[-2] if not pd.isna(df['Close'].iloc[-2]) else None

                if latest_close_price is not None and second_latest_close_price is not None:
                    price_change = latest_close_price - second_latest_close_price
                    return latest_close_price, price_change
                else:
                    return None, None
            else:
                return None, None
        else:
            return None, None

    except Exception as e:
        print(f"Error fetching close price for {stock_symbol}: {e}")
        return None, None

import xgboost as xgb
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
import base64
from io import BytesIO
def predict_stock_price(stock_symbol):
    # Set time range for data
    end = dt.datetime.now()
    start = end - dt.timedelta(days=4000)

    # Fetch stock data
    yfin.pdr_override()
    df = pdr.data.get_data_yahoo(stock_symbol, start, end)

    # Split the data into 70% training and 30% testing
    train_data = df.iloc[:int(.90*len(df)), :]
    test_data = df.iloc[int(.90*len(df)):, :]

    # Define features and target variable
    features = ['Open', 'Volume']
    target = 'Close'

    # Create and train the XGBoost model
    model = xgb.XGBRegressor()
    model.fit(train_data[features], train_data[target])

    # Make predictions
    predictions = model.predict(test_data[features])

    latest_prediction = predictions[-1]
    return latest_prediction

def predict_stock_price_plot(stock_symbol):
    end = dt.datetime.now()
    start = end - dt.timedelta(days=4000)

    # Fetch stock data
    yfin.pdr_override()
    df = pdr.data.get_data_yahoo(stock_symbol, start, end)

    # Split the data into training and testing
    train_data, test_data = train_test_split(df, test_size=0.10, shuffle=False)

    # Define features and target variable
    features = ['Open', 'Volume']
    target = 'Close'

    # Create and train the XGBoost model
    model = xgb.XGBRegressor()
    model.fit(train_data[features], train_data[target])

    # Make predictions
    predictions = model.predict(test_data[features])

    # Filter for the last 3 months
    three_months_ago = end - dt.timedelta(days=90)
    filtered_df = df[df.index >= three_months_ago]
    mask = test_data.index >= three_months_ago
    filtered_predictions = predictions[mask]

    # Plot the predictions
    plt.figure(figsize=(10, 6))
    plt.plot(filtered_df['Close'], label='Close Price')
    plt.plot(test_data[target][mask].index, filtered_predictions, label='Predictions')
    plt.title(f'Stock Price Prediction for {stock_symbol} (Last 3 Months)', fontsize=20)
    plt.xlabel('Date', fontsize=16)
    plt.ylabel('Price', fontsize=16)
    plt.legend(fontsize=14)

    # Save plot to a BytesIO buffer
    buf = BytesIO()
    plt.savefig(buf, format='png', transparent=True)
    buf.seek(0)
    # Encode the buffer to Base64
    img_base64 = base64.b64encode(buf.read()).decode('utf-8')
    buf.close()

    return img_base64