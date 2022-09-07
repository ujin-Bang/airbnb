package com.modoohealing.airbnb

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val mapView: MapView by lazy {
        findViewById(R.id.mapView)
    }

    private val viewPager: ViewPager2 by lazy {
        findViewById(R.id.houseViewPager)
    }

    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.recyclerView)
    }

    private val currentLocationButton: LocationButtonView by lazy {
        findViewById(R.id.currentLocationButton)
    }

    //viewPagerItem클릭이벤트
    private val viewPagerAdapter = HouseViewPagerAdapter(itemClicked = {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,"[지금 이 가격에 예약하세요!!] ${it.title} ${it.price} 사진보기 : ${it.imgUrl}")
            type = "text/plain" //텍스트 플레인 형태로 추저형태를 지닌 것들이 모두 나옴
        }
        startActivity(Intent.createChooser(intent,null))
    })
    private val recyclerViewAdapter = HouseListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState) //mapView연결

        mapView.getMapAsync(this)

        viewPager.adapter = viewPagerAdapter

        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //viewPager페이지 바뀌면 실행하는 이벤트 처리 함수.
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) { //선택된 뷰페이저의 페이지
                super.onPageSelected(position)

                val selectedHouseModel = viewPagerAdapter.currentList[position]
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat,selectedHouseModel.lng))
                    .animate(CameraAnimation.Easing)

                naverMap.moveCamera(cameraUpdate)
            }
        })

    }

    override fun onMapReady(map: NaverMap) {

        naverMap = map

        naverMap.maxZoom = 19.0 //naverMap 확대크기
        naverMap.minZoom = 7.0 //naverMap 최소크기

        //시작 위치는 시청으로 되어있음 => 역삼역으로 시작위치 변경하기
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.500723072486, 127.03680544372))
        naverMap.moveCamera(cameraUpdate)

        //현재위치 확인버튼 => 위치를 확인해야 하므로 권한필요
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false //로케이션버튼 비활성화(나오지 않게)

        currentLocationButton.map = naverMap //xml에서 만든 locationButton띄우기

        //위치권한 받기 build.gradle => location implement추가 후
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getHouseListFromAPI()
    }
    private fun getHouseListFromAPI(){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HouseService::class.java).also {
            it.getHouseList()
                .enqueue(object : Callback<HouseDto>{
                    override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                        if (!response.isSuccessful){
                            //실패 처리에 대한 구현
                            return
                        }
                        response.body()?.let { dto ->
                            Log.d("Retrofit",dto.toString())

                            dto.items.forEach { house ->
                                    val marker = Marker()
                                    marker.position = LatLng(house.lat, house.lng)
                                    //todo 마커클릭 리스너
                                    marker.onClickListener = this@MainActivity
                                    marker.map = naverMap
                                    marker.tag = house.id
                                    marker.icon = MarkerIcons.BLACK
                                    marker.iconTintColor = Color.RED

                            }
                            viewPagerAdapter.submitList(dto.items)
                            recyclerViewAdapter.submitList(dto.items)
                        }
                    }

                    override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                        //실패 처리에 대한 구현 (토스트,얼럿등)
                    }

                })
        }
    }

    //위치 권한 요청결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,

        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    //마커 온클릭리스너 함수 (MainActivity: Overlay.OnclickListener) 인터페이스 필수구현 함수
    override fun onClick(overlay: Overlay): Boolean {
        overlay.tag

        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            it.id == overlay.tag
        }
        selectedModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewPager.currentItem = position
        }
        return true
    }
}