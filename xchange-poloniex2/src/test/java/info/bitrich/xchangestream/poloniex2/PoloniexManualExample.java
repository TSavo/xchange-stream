package info.bitrich.xchangestream.poloniex2;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.reactivex.disposables.CompositeDisposable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoloniexManualExample {
    private static final Logger LOG = LoggerFactory.getLogger(PoloniexManualExample.class);

    public static void main(String[] args) {
        CurrencyPair usdtBtc = new CurrencyPair(new Currency("BTC"), new Currency("USDT"));
//        CertHelper.trustAllCerts();
        StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(PoloniexStreamingExchange.class.getName());
        ExchangeSpecification defaultExchangeSpecification = exchange.getDefaultExchangeSpecification();
//        defaultExchangeSpecification.setExchangeSpecificParametersItem(StreamingExchange.SOCKS_PROXY_HOST, "localhost");
//        defaultExchangeSpecification.setExchangeSpecificParametersItem(StreamingExchange.SOCKS_PROXY_PORT, 8889);

        defaultExchangeSpecification.setExchangeSpecificParametersItem(StreamingExchange.USE_SANDBOX, true);
        defaultExchangeSpecification.setExchangeSpecificParametersItem(StreamingExchange.ACCEPT_ALL_CERITICATES, true);
        defaultExchangeSpecification.setExchangeSpecificParametersItem(StreamingExchange.ENABLE_LOGGING_HANDLER, true);

//        defaultExchangeSpecification.setApiKey("API-KEY");
//        defaultExchangeSpecification.setSecretKey("SECRET-KEY");


        exchange.applySpecification(defaultExchangeSpecification);
        exchange.connect().blockingAwait();

        CompositeDisposable compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(exchange.getStreamingMarketDataService().getOrderBook(usdtBtc).subscribe(orderBook -> {
            LOG.info("First ask: {}", orderBook.getAsks().get(0));
            LOG.info("First bid: {}", orderBook.getBids().get(0));
        }, throwable -> LOG.error("ERROR in getting order book: ", throwable)));

        compositeDisposable.add(exchange.getStreamingMarketDataService().getTicker(usdtBtc).subscribe(ticker -> {
            LOG.info("TICKER: {}", ticker);
        }, throwable -> LOG.error("ERROR in getting ticker: ", throwable)));

        compositeDisposable.add(exchange.getStreamingMarketDataService().getTrades(usdtBtc).subscribe(trade -> {
            LOG.info("TRADE: {}", trade);
        }, throwable -> LOG.error("ERROR in getting trades: ", throwable)));


        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        compositeDisposable.dispose();

        exchange.disconnect().blockingAwait();
    }
}
