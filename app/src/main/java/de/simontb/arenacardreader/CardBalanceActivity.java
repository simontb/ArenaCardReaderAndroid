package de.simontb.arenacardreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.NumberFormat;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CardBalanceActivity extends AppCompatActivity {

    public static final String EXTRA_CARD_NUMBER = "card_number";
    private static final NumberFormat EURO_NUMBER_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private ProgressBar loadingIndicator;
    private View contentHolder;
    private TextView balanceTextView;
    private TextView cardNumberTextView;
    private CardBalanceService cardBalanceService;
    private Disposable networkCall;

    private long cardNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_credit);
        cardNumber = getIntent().getLongExtra(EXTRA_CARD_NUMBER, 0L);
        loadingIndicator = findViewById(R.id.loadingBalanceIndicator);
        contentHolder = findViewById(R.id.content);
        balanceTextView = findViewById(R.id.balanceTextView);
        cardNumberTextView = findViewById(R.id.cardNumberValue);
        cardNumberTextView.setText(String.valueOf(cardNumber));
        initializeService();
    }

    private void initializeService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://online.fcbayern.com/arenacard/rest/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        cardBalanceService = retrofit.create(CardBalanceService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplayBalance();
    }

    private void loadAndDisplayBalance() {
        networkCall = cardBalanceService.getBalanceForCard(cardNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(balance -> {
                    String amount = EURO_NUMBER_FORMAT.format(balance.getLastBalance() / 100.0);
                    balanceTextView.setText(amount);
                    contentHolder.setVisibility(View.VISIBLE);
                    loadingIndicator.setVisibility(View.GONE);
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkCall.dispose();
    }

}
