package com.base.simfw.network;

import com.google.gson.JsonObject;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by zdmy on 2017/11/8.
 */

public interface CalService {

    final String LOGIN_API = "test/login";

    @GET(LOGIN_API)
    Observable<JsonObject> login(String username, String passwd);
}
