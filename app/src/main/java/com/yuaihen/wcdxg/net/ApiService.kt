package com.yuaihen.wcdxg.net

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.xiaolei.OkhttpCacheInterceptor.CacheInterceptor
import com.xiaolei.OkhttpCacheInterceptor.Header.CacheHeaders
import com.yuaihen.wcdxg.BuildConfig
import com.yuaihen.wcdxg.base.BaseApplication
import com.yuaihen.wcdxg.base.Constants
import com.yuaihen.wcdxg.base.NetConstants
import com.yuaihen.wcdxg.net.model.BannerModel
import com.yuaihen.wcdxg.net.model.LoginModel
import com.yuaihen.wcdxg.utils.LogUtil
import com.yuaihen.wcdxg.utils.SPUtils
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


/**
 * Created by Yuaihen.
 * on 2020/6/10
 */
interface ApiService {

    /**
     * 注册
     */
    @FormUrlEncoded
    @POST(NetConstants.REGISTER)
    suspend fun register(
        @Field("username") userName: String,
        @Field("password") password: String,
        @Field("repassword") repassword: String
    ): LoginModel

    /**
     * 登录
     */
    @FormUrlEncoded
    @POST(NetConstants.LOGIN)
    suspend fun login(
        @Field("username") userName: String,
        @Field("password") password: String,
    ): LoginModel

    /**
     * 退出登录
     */
    @GET(NetConstants.LOGOUT)
    suspend fun logout(): LoginModel

    /**
     * 获取轮播图
     */
    @Headers(CacheHeaders.NORMAL)
    @GET(NetConstants.GET_BANNER)
    suspend fun getBanner(): BannerModel

    /**
     * 下载文件
     */
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody


    /*--------------------------------------Retrofit-----------------------------------------------------*/
    companion object {
        val TAG = "data"

        //定义后台返回的异常code
        const val SUCCESS = 0 // 成功
        const val UN_LOGIN = -1001 //未登录的错误码
        const val FAILURE = -1 // 失败
        const val ERROR_500 = "服务器开小差了，请检查网络连接后重试~"
        const val ERROR_404 = "链接地址不存在，请重试~"
        const val ERROR_OTHER = "网络连接出错，请重试~"

        @Volatile
        private var instance: ApiService? = null

        fun getInstance(): ApiService = instance ?: synchronized(ApiService::class.java) {
            instance ?: Retrofit.Builder()
                .baseUrl(NetConstants.APP_BASE_URL)
                .client(getClient())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
                .apply { instance = this }

        }


        private fun getClient(): OkHttpClient {
//            val file = FileUtils.getCacheFolder()
//            val cache = Cache(file, 1024 * 1024 * 100)

            val builder = OkHttpClient.Builder()
            builder.apply {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                addInterceptor(CacheInterceptor(BaseApplication.getContext()))
                retryOnConnectionFailure(true)
                addInterceptor() {
                    //get response cookie
                    val request = it.request()
                    val response = it.proceed(request)
                    val requestUrl = request.url.toString()
                    val domain = request.url.host
                    //保存登录时返回的cookie
                    if ((requestUrl.contains("user/login") || requestUrl.contains("user/register"))
                        && response.headers("Set-Cookie").isNotEmpty()
                    ) {
                        val cookies = response.headers("Set-Cookie")
                        val cookie = encodeCookie(cookies)
                        LogUtil.d("hello", "intercept: $cookie")
                        saveCookie(requestUrl, domain, cookie)
                    }
                    response

                }
                addInterceptor {
                    //set request cookie
                    val request = it.request()
                    val builder = request.newBuilder()
                    val domain = request.url.host
                    //get domain cookie
                    if (domain.isNotEmpty()) {
                        val cookie =
                            SPUtils.getCookiePreferences().decodeString(domain, "") ?: ""
                        if (cookie.isNotEmpty()) {
                            builder.addHeader("Cookie", cookie)
                        }
                    }

                    it.proceed(request)
                }

                //添加Cache拦截器 有网时添加到缓存 无网取出缓存
//                val file = FileUtils.getCacheFolder()
//                val cache = Cache(file, 1024 * 1024 * 100)
//                cache(cache).addInterceptor {
//                    //有网络时缓存到本地
//                    val request = it.request()
//                    if (NetworkUtils.getNetworkType() != NetworkUtils.NetworkType.NETWORK_NO) {
//                        val newRequest = request.newBuilder()
//                            .cacheControl(CacheControl.FORCE_CACHE)
//                            .build()
//                        it.proceed(newRequest)
//                    } else {
//                        //无网络时取出缓存
//                        val maxTime = 24 * 60 * 60
//                        val response = it.proceed(request)
//                        val newResponse = response.newBuilder()
//                            .header("Cache-Control", "public, only-if-cached, max-stale=$maxTime")
//                            .removeHeader("Progma")
//                            .build()
//                        newResponse
//                    }
//                }
            }

            if (BuildConfig.DEBUG) {
                val log = LoggingInterceptor.Builder()
                    .setLevel(Level.BODY)
                    .log(Platform.INFO)
                    .tag(TAG)
//                        .request("Request")
//                        .response("Response")
                    .build()

                builder.addInterceptor(log)
            }
            return builder.build()
        }

        /**
         * Cookie去重
         */
        private fun encodeCookie(cookies: List<String>): String {
            val sb = StringBuilder()
            val set = hashSetOf<String>()
            cookies.forEach {
                LogUtil.d("hello", "encodeCookie: $it")
                val value = it.split(";")
                value.forEach { v ->
                    set.add(v)
                }
            }

            set.forEachIndexed { index, s ->
                sb.append(s)
                if (index != set.size - 1) {
                    sb.append(";")
                }
            }
            return sb.toString()
        }

        /**
         * 保存Cookie到SP
         */
        private fun saveCookie(requestUrl: String?, domain: String?, cookie: String) {
            requestUrl ?: return
            SPUtils.getCookiePreferences().encode(requestUrl, cookie)
            domain ?: return
            SPUtils.getCookiePreferences().encode(domain, cookie)
            SPUtils.getCookiePreferences().encode(Constants.Cookie, cookie)
        }
    }
}