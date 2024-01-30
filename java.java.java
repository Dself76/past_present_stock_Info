import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class StockCalculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter ticker symbol: ");
        String tickerSymbol = scanner.nextLine();

        System.out.print("Enter start date (MM/dd/yyyy): ");
        String startDateStr = scanner.nextLine();

        System.out.print("Enter end date (MM/dd/yyyy): ");
        String endDateStr = scanner.nextLine();

        System.out.print("Enter investment amount: ");
        double investmentAmount = scanner.nextDouble();

        try {
            Calendar startDate = parseDate(startDateStr);
            Calendar endDate = parseDate(endDateStr);

            Stock stock = YahooFinance.get(tickerSymbol, startDate, endDate, Interval.DAILY);
            BigDecimal[] returns = calculateReturns(stock, BigDecimal.valueOf(investmentAmount));

            System.out.println("Total Return: " + returns[0]);
            System.out.println("Interest Gained: " + returns[1]);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static Calendar parseDate(String dateStr) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(format.parse(dateStr));
        return calendar;
    }

    public static BigDecimal[] calculateReturns(Stock stock, BigDecimal investmentAmount) {
        List<HistoricalQuote> history = stock.getHistory();
        if (history.size() > 0) {
            BigDecimal startPrice = history.get(0).getClose();
            BigDecimal endPrice = history.get(history.size() - 1).getClose();

            BigDecimal sharesBought = investmentAmount.divide(startPrice, BigDecimal.ROUND_HALF_UP);
            BigDecimal interestGained = sharesBought.multiply(endPrice.subtract(startPrice));
            BigDecimal totalReturn = investmentAmount.add(interestGained);

            return new BigDecimal[]{totalReturn, interestGained};
        } else {
            return new BigDecimal[]{investmentAmount, BigDecimal.ZERO};
        }
    }
}
