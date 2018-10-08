package de.simontb.arenacardreader;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CardBalanceService {

    @GET("card/{cardNumber}")
    Observable<CardBalanceModel> getBalanceForCard(@Path("cardNumber") long cardNumber);

}
