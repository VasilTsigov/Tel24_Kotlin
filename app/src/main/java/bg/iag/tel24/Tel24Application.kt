package bg.iag.tel24

import android.app.Application
import coil.Coil
import coil.ImageLoader
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class Tel24Application : Application() {

    override fun onCreate() {
        super.onCreate()

        val trustAll = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
        }
        val okHttp = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAll)
            .hostnameVerifier { _, _ -> true }
            .build()

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient(okHttp)
                .crossfade(true)
                .build()
        )
    }
}
