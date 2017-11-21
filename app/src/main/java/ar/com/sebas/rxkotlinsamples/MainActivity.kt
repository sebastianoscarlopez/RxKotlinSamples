package ar.com.sebas.rxkotlinsamples

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.util.HalfSerializer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import retrofit2.Call
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.Result
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

//    private lateinit var subject: PublishSubject<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        txtContent.text = ""
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_Counter -> {
                counter()
            }
            R.id.nav_CounterDefer -> {
                Observable.defer { Observable.just(counter()) }
                        .subscribeOn(Schedulers.io()) // Equals to a simple call whether AndroidSchedulers.mainThread() is used
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
            }
            R.id.nav_CounterObservable -> {
                (1..10000000).toObservable()
                    .subscribeOn(Schedulers.io()) // Equals to a simple call whether AndroidSchedulers.mainThread() is used
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            if(it % 100 == 0) {
                                txtContent.text = it.toString() // It fail whether observeOn is different from AndroidSchedulers.mainThread()
                            }
                        },
                        onError =  { it.printStackTrace() },
                        onComplete = { Log.d("cuenta", "onComplete") }
                    )
            }
            R.id.nav_subject -> {
                val subject = PublishSubject.create<Int>()

                subject
                        .doOnNext {
                            txtContent.text = it.toString()
                        }
                        .subscribe()

                (1..10000000)
                        .toObservable()
                        .sample(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io()) // Equals to a simple call whether AndroidSchedulers.mainThread() is used
                        .observeOn(AndroidSchedulers.mainThread())
                        .forEach({
                            subject.onNext(it)
                        })
            }
            R.id.nav_gitHubUser -> {
                val apiService = GithubApiService.create()
                apiService.listRepos("sebastianoscarlopez")
                        .flatMap { Observable.fromIterable(it) }
                        .concatMap { Observable.just(it).delay(500, TimeUnit.MILLISECONDS) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({
                            result ->
                            Log.d("Result", "Repos: ${result?.name}")

                            txtContent.text = result?.name ?: ""
                        }, { error ->
                            error.printStackTrace()
                        })
            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun counter() : Int {
        Log.d("counter", "empezo")
        for(i in 1..1000000){
            Log.d("cuenta", i.toString())
            //txtContent.text = i.toString() // It fail whether observeOn is different from AndroidSchedulers.mainThread()
        }
        Log.d("counter", "termino")
        return 1
    }
}