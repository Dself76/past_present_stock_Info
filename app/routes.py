from flask import Flask, render_template, request, jsonify
import yfinance as yf
import datetime

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

def calculate_returns(tickerDf, investment_amount):
    # Calculate the returns based on the closing price
    if not tickerDf.empty:
        closing_price_start = tickerDf['Close'].iloc[0]  # Starting closing price
        closing_price_end = tickerDf['Close'].iloc[-1]   # Ending closing price
        shares_bought = investment_amount / closing_price_start

        interest_gained = shares_bought * (closing_price_end - closing_price_start)  # Calculate interest gained
        total_return = investment_amount + interest_gained  # Calculate total return
        return total_return, interest_gained
    else:
        return investment_amount, 0  # If there is no data, return the original investment and 0 interest gained


@app.route('/get_data', methods=['POST'])
def get_historical_data():
    try:
        tickerSymbol = request.form.get('tickerSymbol')
        start_date = request.form.get('startDate')
        end_date = request.form.get('endDate')
        investment_amount = request.form.get('investmentAmount')

        # Validate the input data
        if not tickerSymbol or not start_date or not end_date or not investment_amount:
            raise ValueError("Missing or invalid form data")

        investment_amount = float(investment_amount)  # Convert to float

        # Parse the date input
        start_date_obj = datetime.datetime.strptime(start_date, '%m/%d/%Y').date()
        end_date_obj = datetime.datetime.strptime(end_date, '%m/%d/%Y').date()

        # Fetch the historical data from yfinance
        tickerData = yf.Ticker(tickerSymbol)
        tickerDf = tickerData.history(start=start_date_obj, end=end_date_obj)

        # Process the ticker data to calculate the returns
        total_return, interest_gained = calculate_returns(tickerDf, investment_amount)

        # Prepare the response as an HTML snippet
        return render_template('results_snippet.html', result={
            'total_return': total_return,
            'interest_gained': interest_gained,
            'original_investment': investment_amount
        })

    except Exception as e:
        # log the error here
        app.logger.error(f"An error occurred: {e}")
        # For the user, you might want to return a user-friendly error message
        return render_template('error.html', error="An error occurred while processing your request.")


if __name__ == '__main__':
    app.run(debug=True)
