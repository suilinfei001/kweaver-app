package com.kweaver.dip.data.api

import android.util.Base64
import android.util.Log
import com.google.gson.JsonParser
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Singleton
class OAuth2LoginHelper @Inject constructor() {

    companion object {
        private const val TAG = "OAuth2Login"

        private const val RSA_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsyOstgbYuubBi2PUqeVj" +
            "GKlkwVUY6w1Y8d4k116dI2SkZI8fxcjHALv77kItO4jYLVplk9gO4HAtsisnNE2o" +
            "wlYIqdmyEPMwupaeFFFcg751oiTXJiYbtX7ABzU5KQYPjRSEjMq6i5qu/mL67XTk" +
            "hvKwrC83zme66qaKApmKupDODPb0RRkutK/zHfd1zL7sciBQ6psnNadh8pE24w8O" +
            "2XVy1v2bgSNkGHABgncR7seyIg81JQ3c/Axxd6GsTztjLnlvGAlmT1TphE84mi99" +
            "fUaGD2A1u1qdIuNc+XuisFeNcUW6fct0+x97eS2eEGRr/7qxWmO/P20sFVzXc2bF" +
            "1QIDAQAB"
    }

    data class LoginResult(val accessToken: String, val refreshToken: String?)

    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val domain = url.host
            cookieStore[domain]?.addAll(cookies) ?: run {
                cookieStore[domain] = cookies.toMutableList()
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val domain = url.host
            return cookieStore[domain] ?: emptyList()
        }
    }

    private val client: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .followRedirects(false)
            .followSslRedirects(false)
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun login(serverUrl: String, username: String, password: String): LoginResult {
        cookieStore.clear()

        // Step 1: Initiate OAuth2 flow → get redirect to /oauth2/auth?...
        Log.d(TAG, "Step 1: Initiating OAuth2 flow")
        val step1 = execute(Request.Builder()
            .url("$serverUrl/api/dip-hub/v1/login?username=&password=")
            .build())

        if (step1.code != 302) {
            step1.close()
            throw Exception("Expected redirect from login, got ${step1.code}")
        }
        val oauth2Url = resolveUrl(serverUrl, step1.header("Location")!!)
        step1.close()

        // Step 2: Follow to /oauth2/auth → get redirect to /oauth2/signin?login_challenge=...
        Log.d(TAG, "Step 2: Following OAuth2 auth endpoint")
        val step2 = execute(Request.Builder().url(oauth2Url).build())

        if (step2.code != 302 && step2.code != 303) {
            step2.close()
            throw Exception("Expected redirect from auth, got ${step2.code}")
        }
        val signinUrl = resolveUrl(serverUrl, step2.header("Location")!!)
        step2.close()

        // Step 3: Fetch signin page → extract challenge and CSRF from __NEXT_DATA__
        Log.d(TAG, "Step 3: Fetching signin page")
        val step3 = execute(Request.Builder().url(signinUrl).build())

        if (step3.code != 200) {
            step3.close()
            throw Exception("Expected 200 from signin page, got ${step3.code}")
        }
        val html = step3.body?.string() ?: throw Exception("Empty signin page")
        step3.close()

        val pageProps = parseNextData(html)
        val challenge = pageProps.getString("challenge")
        val csrfToken = pageProps.getString("csrftoken")
        Log.d(TAG, "Got challenge: ${challenge.take(8)}..., csrf: ${csrfToken.take(8)}...")

        // Step 4: RSA encrypt password and POST to /oauth2/signin
        Log.d(TAG, "Step 4: Posting credentials")
        val encryptedPassword = rsaEncryptPassword(password)

        val postBody = JSONObject().apply {
            put("_csrf", csrfToken)
            put("challenge", challenge)
            put("account", username)
            put("password", encryptedPassword)
            put("vcode", JSONObject().apply {
                put("id", "")
                put("content", "")
            })
            put("dualfactorauthinfo", JSONObject().apply {
                put("validcode", JSONObject().apply { put("vcode", "") })
                put("OTP", JSONObject().apply { put("OTP", "") })
            })
            put("remember", false)
            put("device", JSONObject().apply {
                put("name", "")
                put("description", "")
                put("client_type", "web")
                put("udids", JSONArray())
            })
        }

        val signinPostUrl = buildSigninPostUrl(signinUrl)
        val step4 = execute(Request.Builder()
            .url(signinPostUrl)
            .post(postBody.toString().toRequestBody("application/json".toMediaType()))
            .build())

        val signinResponseStr = step4.body?.string()
        step4.close()

        if (step4.code != 200) {
            throw Exception("Signin POST failed with ${step4.code}: $signinResponseStr")
        }

        val redirectUrl = parseSigninRedirect(signinResponseStr ?: throw Exception("Empty signin response"))
        Log.d(TAG, "Got redirect URL: ${redirectUrl.take(60)}...")

        // Step 5: Follow redirects through consent to callback → extract tokens
        Log.d(TAG, "Step 5: Following redirects to callback")
        return followRedirectsForTokens(serverUrl, resolveUrl(serverUrl, redirectUrl))
    }

    private fun execute(request: Request): Response = client.newCall(request).execute()

    private fun resolveUrl(baseUrl: String, location: String): String {
        if (location.startsWith("http")) return location
        val uri = URI(baseUrl)
        val origin = "${uri.scheme}://${uri.authority}"
        return if (location.startsWith("/")) {
            "$origin$location"
        } else {
            val parent = baseUrl.substringBeforeLast('/')
            "$parent/$location"
        }
    }

    private fun parseNextData(html: String): JSONObject {
        val startMarker = """<script id="__NEXT_DATA__" type="application/json">"""
        val endMarker = "</script>"
        val start = html.indexOf(startMarker)
        if (start == -1) throw Exception("Cannot find __NEXT_DATA__ in signin page")

        val jsonStart = start + startMarker.length
        val jsonEnd = html.indexOf(endMarker, jsonStart)
        if (jsonEnd == -1) throw Exception("Cannot find end of __NEXT_DATA__")

        val json = html.substring(jsonStart, jsonEnd)
        val root = JSONObject(json)
        return root.getJSONObject("props").getJSONObject("pageProps")
    }

    private fun rsaEncryptPassword(password: String): String {
        val keyBytes = Base64.decode(RSA_PUBLIC_KEY, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun buildSigninPostUrl(signinUrl: String): String {
        val uri = URI(signinUrl)
        val port = if (uri.port != -1) ":${uri.port}" else ""
        return "${uri.scheme}://${uri.host}$port/oauth2/signin"
    }

    private fun parseSigninRedirect(response: String): String {
        val json = JSONObject(response)
        // Response can be {"redirect":"..."} or {"data":{"redirect":"..."}}
        val data = json.optJSONObject("data")
        return if (data != null && data.has("redirect")) {
            data.getString("redirect")
        } else {
            json.getString("redirect")
        }
    }

    private fun followRedirectsForTokens(serverUrl: String, startUrl: String): LoginResult {
        var currentUrl = startUrl
        var redirects = 0

        while (redirects < 15) {
            val response = execute(Request.Builder().url(currentUrl).build())

            when {
                response.code == 200 -> {
                    val tokens = extractTokens(response)
                    if (tokens != null) return tokens
                    response.close()
                    throw Exception("No token received from callback")
                }
                response.code in 301..399 -> {
                    val location = response.header("Location")
                        ?: throw Exception("Redirect without Location header")
                    currentUrl = resolveUrl(currentUrl, location)
                    response.close()
                    redirects++
                }
                else -> {
                    val body = response.body?.string()
                    response.close()
                    throw Exception("Unexpected response ${response.code}: ${body?.take(200)}")
                }
            }
        }

        throw Exception("Too many redirects while following to callback")
    }

    private fun extractTokens(response: Response): LoginResult? {
        var accessToken: String? = null
        var refreshToken: String? = null

        val allCookies = cookieStore.values.flatten()
        for (cookie in allCookies) {
            when (cookie.name) {
                "dip.oauth2_token" -> accessToken = cookie.value
                "dip.refresh_token" -> refreshToken = cookie.value
            }
        }

        val body = response.body?.string()
        if (accessToken.isNullOrBlank() && !body.isNullOrBlank() && !body.trimStart().startsWith("<")) {
            try {
                val jsonElement = JsonParser.parseString(body)
                if (jsonElement.isJsonObject) {
                    val obj = jsonElement.asJsonObject
                    accessToken = obj.get("access_token")?.asString
                    refreshToken = refreshToken ?: obj.get("refresh_token")?.asString
                } else {
                    accessToken = body.trim()
                }
            } catch (_: Exception) {
                accessToken = body.trim()
            }
        }

        response.close()

        return if (!accessToken.isNullOrBlank()) {
            LoginResult(accessToken.trim(), refreshToken?.trim())
        } else null
    }
}
