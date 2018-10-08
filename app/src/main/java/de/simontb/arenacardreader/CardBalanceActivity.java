package de.simontb.arenacardreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.net.CookieManager;
import java.net.CookiePolicy;

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

    private TextView balanceTextView;
    private CardBalanceService cardBalanceService;
    private Disposable networkCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_credit);
        balanceTextView = findViewById(R.id.balanceTextView);
        initializeService();
        loadAndDisplayBalance();
    }

    private void initializeService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://online.fcbayern.com/arenacard/rest/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        cardBalanceService = retrofit.create(CardBalanceService.class);
    }

    private void loadAndDisplayBalance() {
        long cardNumber = getIntent().getLongExtra(EXTRA_CARD_NUMBER, 0L);
        networkCall = cardBalanceService.getBalanceForCard(cardNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(balance -> {
                    double amount = balance.getLastBalance() / 100.0;
                    balanceTextView.setText("Balance: " + amount + "€");
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkCall.dispose();
    }

}
