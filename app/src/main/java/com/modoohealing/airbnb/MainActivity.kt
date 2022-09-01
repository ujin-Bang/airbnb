package com.modoohealing.airbnb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource

class MainActivity : AppCompatActivity(),OnMapReadyCallback {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val mapView: MapView by lazy {
        findViewById(R.id.mapView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState) //mapView연결

        mapView.getMapAsync(this)

    }

    override fun onMapReady(map: NaverMap) {

        naverMap = map

        naverMap.maxZoom = 19.0 //naverMap 확대크기
        naverMap.minZoom = 10.0 //naverMap 최소크기

        //시작 위치는 시청으로 되어있음 => 강남역으로 시작위치 변경하기
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.498095,127.027610))
        naverMap.moveCamera(cameraUpdate)

        //현재위치 확인버튼 => 위치를 확인해야 하므로 권한필요
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = true

        //위치권한 받기 build.gradle => location implement추가 후
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource
    }

    //위치 권한 요청결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,

        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE){
            return
        }

        if (locationSource.onRequestPermissionsResult(requestCode,permissions,grantResults)){
            if (!locationSource.isActivated){
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
}