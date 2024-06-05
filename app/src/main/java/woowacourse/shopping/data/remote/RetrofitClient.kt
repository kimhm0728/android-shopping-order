package woowacourse.shopping.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://54.180.95.212:8080"
    private val httpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(
                AuthenticationInterceptor(
                    "kimhm0728",
                    "password",
                ),
            ).build()
    val retrofitApi: RetrofitService =
        Retrofit.Builder()
            .client(httpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
}
