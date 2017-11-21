package ar.com.sebas.rxkotlinsamples

import android.database.Observable
import io.reactivex.Flowable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by lopez.sebastian on 21/11/2017.
 */
interface GithubApiService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): io.reactivex.Observable<List<Repo>>

    companion object Factory {
        fun create(): GithubApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.github.com/")
                    .build()

            return retrofit.create(GithubApiService::class.java)
        }
    }
}