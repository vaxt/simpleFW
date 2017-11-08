package com.base.simfw.network;

import android.content.Context;
import android.util.SparseArray;

import com.base.simfw.config.Constant;
import com.base.simfw.utils.LogUtils;
import com.base.simfw.utils.NetUtil;
import com.google.gson.JsonObject;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zdmy on 2017/11/8.
 */

public class RetrofitManager {
    private final static String TAG = RetrofitManager.class.getSimpleName();

    private static Context mAppContext;
    private static volatile OkHttpClient mOkHttpClient;

    private CalService mCalservice;

    private static SparseArray<RetrofitManager> retrofitManagerArray = new SparseArray<>();

    /**
     * 设缓存有效期为两天
     */
    private static final long CACHE_STALE_SEC = 60 * 60 * 24 * 2;


    private final Interceptor mLoggingInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.nanoTime();
            LogUtils.i(TAG, String.format("Sending request %s on %s%n%s ", request.url(), chain.connection(), request.headers()));
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();
            LogUtils.i(TAG, String.format(Locale.getDefault(), "Receive response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));
            return response;
        }
    };

    private final Interceptor mRewriteCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetUtil.isNetworkAvailable(mAppContext)) {
                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
            }
            Response originalResponse = chain.proceed(request);
            if (NetUtil.isNetworkAvailable(mAppContext)) {
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder().header("Cache-Control", cacheControl)
                        .removeHeader("Pragma").build();
            } else {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_STALE_SEC)
                        .removeHeader("Pragma")
                        .build();
            }
        }
    };

    private RetrofitManager(HostManager.HostType type) {
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.baseUrl(HostManager.getHost(type))
                .client(getOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mCalservice = retrofit.create(CalService.class);
        //can init other data
    }


    public static RetrofitManager getInstance(Context appContext, HostManager.HostType type) {
        RetrofitManager retrofitManager = retrofitManagerArray.get(type.ordinal());
        mAppContext = appContext;
        if (retrofitManager == null) {
            retrofitManager = new RetrofitManager(type);
            retrofitManagerArray.put(type.ordinal(), retrofitManager);
            return retrofitManager;
        }
        return retrofitManager;
    }


    private OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            synchronized (RetrofitManager.class) {
                Cache cache = new Cache(new File(mAppContext.getCacheDir(), "HttpCache"), 1024 * 1024 * 10);
                if (mOkHttpClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder().cache(cache);
                    builder.connectTimeout(6, TimeUnit.SECONDS)
                            .readTimeout(6, TimeUnit.SECONDS)
                            .writeTimeout(6, TimeUnit.SECONDS)
                            .addInterceptor(mRewriteCacheControlInterceptor)
                            .addNetworkInterceptor(mRewriteCacheControlInterceptor);
                    if (Constant.DEBUG) {
                        builder.addInterceptor(mLoggingInterceptor);
                    }
                    mOkHttpClient = builder.build();

                }
            }
        }
        return mOkHttpClient;
    }


    public Observable<JsonObject> login(String username,String password){
        return mCalservice.login(username,password);
    }

}
