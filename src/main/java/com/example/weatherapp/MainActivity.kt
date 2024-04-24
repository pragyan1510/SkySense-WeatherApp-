package com.example.weatherapp

import android.app.DownloadManager.Query
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.util.query
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val TAG : String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchWeatherdata("Delhi")
        searchCity()
    }

    private fun searchCity(){
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                    fetchWeatherdata(p0)
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherdata(cityName:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        Log.d(TAG, "onStart: Api Starts")
        val response = retrofit.getWeatherdata(cityName,"e8f0b18654ab364ca909c3277562fb83","metric")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(p0: Call<WeatherApp>, p1: Response<WeatherApp>) {
                val responseBody = p1.body()
                if (p1.isSuccessful && responseBody!=null){

                    val tempinkelvin = responseBody.main.temp
                    val tempincelsius = tempinkelvin - 273.15

                    val maxTempInKelvin = responseBody.main.temp_max
                    val minTempInKelvin = responseBody.main.temp_min

                    val maxTempInCelsius = maxTempInKelvin - 273.15
                    val minTempInCelsius = minTempInKelvin - 273.15
                    val humidity = responseBody.main.humidity
                    val wind  = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val sealevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unkown"
                    val maxtemp = responseBody.main.temp_max
                    val mintemp = responseBody.main.temp_min
                    binding.temp.text= "${tempincelsius.roundToInt()}ºC"
                    binding.weather.text = condition
                    binding.maxTemp.text =  "max temp : ${maxTempInCelsius.roundToInt()}ºC"
                    binding.minTemp.text = "min temp : ${minTempInCelsius.roundToInt()}ºC"
                    binding.humidity.text = "$humidity %"
                    binding.windspeed.text = "$wind m/s"
                    binding.sunset.text = "${time(sunset)}"
                    binding.sunrise.text = "${time(sunrise)}"
                    binding.sea.text = "$sealevel hpa"
                    binding.condition.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        binding.cityName.text = "$cityName"

                    changeimgacctoweather(condition)
                }
            }

            override fun onFailure(p0: Call<WeatherApp>, p1: Throwable) {
                Log.d("TAG", "onFailure: Api failure",p1)

            }

        })



    }

    private fun changeimgacctoweather(conditions : String) {
        when(conditions){
            "Clear Sky","Sunny","Clear"->{
                binding.lottieAnimationView.setAnimation(R.raw.sun)
                binding.root.setBackgroundResource(R.drawable.bgbg)
            }
            "Partly Clouds","Clouds","Mist","foggy"->{
                binding.root.setBackgroundResource(R.drawable.cloudy_background1)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain","Drizzle","Moderate Rain","Showers","Heavy Rain"->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow","Moderate Snow","Heavy Snow","Blizzard"->{
                binding.root.setBackgroundResource(R.drawable.snowbg)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.lottieAnimationView.setAnimation(R.raw.sun)
                binding.root.setBackgroundResource(R.drawable.bgbg)
            }
        }
        binding.lottieAnimationView.playAnimation()

    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))

    }
    private fun time(timestamp:Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))

    }

    fun dayName(timestamp:Long): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}