package bg.iag.tel24.data.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {

    private const val BASE_URL = "https://vasil.iag.bg/"

    // Доверяваме се на всички сертификати (вътрешна корпоративна апликация)
    private val trustAllManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(trustAllManager), SecureRandom())
    }

    // Добавяме ?callback=androidcb към всяка заявка – сървърът иска JSONP параметър
    private val callbackInterceptor = Interceptor { chain ->
        val req = chain.request()
        val url = req.url.newBuilder()
            .addQueryParameter("callback", "androidcb")
            .build()
        chain.proceed(req.newBuilder().url(url).build())
    }

    // Свалями JSONP обвивката: androidcb({...}) → {...}
    private val jsonpInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val raw = response.body?.string() ?: return@Interceptor response
        val trimmed = raw.trimStart()
        val json = if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            raw   // вече е JSON
        } else {
            val start = raw.indexOf('(') + 1
            val end   = raw.lastIndexOf(')')
            if (start > 0 && end > start) raw.substring(start, end) else raw
        }
        response.newBuilder()
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private val httpClient = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllManager)
        .hostnameVerifier { _, _ -> true }
        .addInterceptor(callbackInterceptor)
        .addInterceptor(jsonpInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
