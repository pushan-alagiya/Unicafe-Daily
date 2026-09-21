package fi.pushan.unicafedaily.data.api

import fi.pushan.unicafedaily.data.dto.PriceValueAdapter
import fi.pushan.unicafedaily.data.dto.VisitingHoursAdapter
import fi.pushan.unicafedaily.data.dto.VisitingHoursSectionAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object UniCafeApiClient {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(PriceValueAdapter())
            .add(VisitingHoursAdapter())
            .add(VisitingHoursSectionAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: UniCafeApi by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UniCafeApi::class.java)
    }
}
