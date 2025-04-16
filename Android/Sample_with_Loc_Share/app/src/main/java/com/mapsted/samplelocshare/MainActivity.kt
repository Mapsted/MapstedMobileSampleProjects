package com.mapsted.samplelocshare

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import com.mapsted.MapstedBaseApplication
import com.mapsted.locshare.LocationShareApi
import com.mapsted.locshare.MapstedLocationShareApi
import com.mapsted.locshare.data.models.location.LiveLocationResponse
import com.mapsted.map.MapApi
import com.mapsted.map.MapstedMapApi
import com.mapsted.map.MapstedMapApiProvider
import com.mapsted.positioning.CoreApi
import com.mapsted.positioning.MapstedCoreApi
import com.mapsted.positioning.SdkError
import com.mapsted.positioning.SdkStatusUpdate
import com.mapsted.positioning.core.utils.LogTime
import com.mapsted.positioning.core.utils.Logger
import com.mapsted.positioning.util.permissions.PermissionChecks
import com.mapsted.samplelocshare.databinding.ActivityMainBinding
import com.mapsted.ui.CustomParams
import com.mapsted.ui.MapUiApi
import com.mapsted.ui.MapUiApi.MapUiInitCallback
import com.mapsted.ui.MapstedMapUiApi
import com.mapsted.ui.MapstedMapUiApiProvider
import com.mapsted.ui.utils.common.Settings


class MainActivity : AppCompatActivity(), MapstedMapApiProvider, MapstedMapUiApiProvider {

    private var liveLocationShareAPI: LocationShareApi? = null
    private lateinit var mapApi: MapApi
    private lateinit var mapUiApi: MapUiApi
    private var customParams: CustomParams? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var coreApi: CoreApi
    private var permissionChecks: PermissionChecks? = null
    private var propertyId: Int? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize core API
        coreApi = (application as? MapstedBaseApplication)?.getCoreApi(this) ?: run {
            val newCoreApi = MapstedCoreApi.newInstance(this)
            (application as? MapstedBaseApplication)?.setCoreApi(newCoreApi)
            newCoreApi
        }

        // Initialize map and UI APIs
        mapApi = MapstedMapApi.newInstance(this, coreApi)
        mapUiApi = MapstedMapUiApi.newInstance(this, mapApi)

        // Set up view binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Initialize Location Share API
        liveLocationShareAPI = MapstedLocationShareApi(
            this.applicationContext,
            coreApi, mapUiApi
        )

        // Set up permission checks
        permissionChecks = PermissionChecks(
            coreApi,
            this@MainActivity, onPermissionReadyCallback, null
        ) { success: Boolean? ->
            intilizeMapUiSdk()
        }

        // Check and request permissions
        if (permissionChecks!!.hasLocationPermissions()) {
            intilizeMapUiSdk()
        } else {
            checkPermissions()
        }

    }


    private fun addCustomViewOnMap() {
        // Create Custom View
        val myView: View =
            LayoutInflater.from(this).inflate(R.layout.custom_view_on_map, null, false)
        val startLocationShareBtn =
            myView.findViewById<AppCompatButton>(R.id.start_location_share)
        val stopLocationShareBtn =
            myView.findViewById<AppCompatButton>(R.id.stop_location_share)

        startLocationShareBtn.setOnClickListener {
            startLocationShare()
        }

        stopLocationShareBtn.setOnClickListener {
            stopLocationShare()
        }

        // Display custom View on the map
        mapApi.mapView().customView().addViewToMapFragment("my_custom_tag", myView)
        // Get custom View on the map
//        val getView = mapApi.mapView().customView().getViewOnMap("my_custom_tag")
        // Remove custom View from the map
//        mapApi.mapView().customView().removeViewFromMap("my_custom_tag")
    }

    // Permission result callback
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Callback for location permissions granted
    private val onPermissionReadyCallback: PermissionChecks.LocationPermissionListener =
        object : PermissionChecks.LocationPermissionListener {
            override fun onLocationPermissionsGranted() {

            }
        }

    // Check for required permissions
    private fun checkPermissions() {
        if (!permissionChecks!!.hasLocationPermissions()) {
            permissionChecks!!.processLocationPermissionChecks()
        }

        if (!permissionChecks!!.hasBluetoothPermissions()) {
            permissionChecks!!.processBluetoothPermissionChecks()
        }

        if (!permissionChecks!!.hasNotificationPermission()) {
            permissionChecks!!.processNotificationPermissionChecks()
        }
    }

    // Create and configure CustomParams for Map UI
    fun getCustomParams(activity: AppCompatActivity): CustomParams {
        return CustomParams.newBuilder(activity)
            .setEnablePropertyListSelection(false)
            .setEnableMapOverlayFeature(true)
            .setEnableStagingEnvironmentSettings(false)
            .setEnableBlueDotCustomizationSettings(false)
            .setDefaultDistanceUnit(Settings.DistanceUnits.AUTO)
            //.setMapPanType(MapPanType.RESTRICT_TO_SELECTED_PROPERTY)
            .build()
    }

    // Initialize Mapsted Map UI SDK
    fun intilizeMapUiSdk() {

        if (customParams == null)
            customParams = getCustomParams(this)

        if (!customParams?.isForegroundServiceResumeActivitySet!!) {
            val curClass: Class<*> = javaClass
            Logger.v("Setting foreground service resume activity: %s", curClass.name)
            customParams?.setForegroundServiceResumeActivity(javaClass)
        }
        // Set up required views and visibility
        customParams?.activity = this
        // When you initialize your SDK, you must provide both containers for the BaseMap and MapUi
        customParams?.setBaseMapContainerView(binding.flBaseMap)
        customParams?.mapUiContainerView = binding.flMapUi
        customParams?.setDefaultMapUiContainerVisibility(true)

        showProgressDialog()

        // Initialize the Map UI API
        mapUiApi.setup().initialize(customParams!!, object : MapUiInitCallback {
            override fun onCoreInitialized() {
                Logger.i("onCoreInitialized: ")
            }

            override fun onMapInitialized() {
                addCustomViewOnMap()
                Logger.i("onMapInitialized: ")
                LogTime.getInstance().end(LogTime.LogTimeKey.MAP_SDK_INITIALIZE)
            }

            override fun onSuccess() {
//                addCustomViewOnMap()
                Log.d("SUCCESS", "Intialized Successfully")
                // After initialization, retrieve property and set up location share SDK
                coreApi.properties()?.getInfos { propertyInfos ->
                    val numProperties = propertyInfos?.size
                    propertyId =
                        if (numProperties != null && numProperties > 0) propertyInfos.keys.first() else -1;
                    setUpLocShareSDK(coreApi, mapUiApi)
                    hideProgressDialog()
                }
            }

            override fun onStatusUpdate(sdkUpdate: SdkStatusUpdate?) {
                Logger.d("onStatusUpdate: sdkUpdate=%s", sdkUpdate)
            }

            override fun onFailure(sdkError: SdkError?) {
                Logger.w("::setupMapstedSdk ::onFailure sdkError=%s", sdkError)
            }
        })

    }

    // Set up the Location Share SDK
    private fun setUpLocShareSDK(coreApi: CoreApi, mapUiApi: MapUiApi) {
        liveLocationShareAPI = MapstedLocationShareApi(this.applicationContext, coreApi, mapUiApi)
        liveLocationShareAPI?.setup()?.initialize()
        (liveLocationShareAPI as MapstedLocationShareApi).addLiveLocationTriggerListener { status: Boolean ->
            Logger.d(
                "addLiveLocationTriggerListener: status=%s,  ",
                status
            )
            hideProgressDialog()
        }

    }

    // Trigger start sharing live location
    fun startLocationShare() {
        val position = coreApi.locations()?.getLastKnownPosition()
        if (liveLocationShareAPI == null || position == null) {
            return
        }
        showProgressDialog()
        liveLocationShareAPI?.events()?.shareLiveLocation(
            propertyId ?: 0, position
        ) { liveLocationResponse: LiveLocationResponse? ->
            Handler(Looper.getMainLooper()).post {
                hideProgressDialog()
                if (liveLocationResponse != null && liveLocationResponse.success) {
                    launchLocationShareIntent(liveLocationResponse.url)
                }
            }
        }

    }

    // Stop location sharing
    fun stopLocationShare() {
        liveLocationShareAPI?.let {
            showProgressDialog()
            it.events()
                .stopLiveLocationShare {
                    hideProgressDialog()
                }
        }

    }
    // Handle device configuration changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mapUiApi.lifecycle()?.onConfigurationChanged(this, newConfig)
    }

    // This methods will return the link to the user location via implicit intent
    /*TODO This logic should be moved into location share sdk*/
    private fun launchLocationShareIntent(shareURL: String?) {
        val sb = java.lang.StringBuilder()
        sb.append(shareURL)
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.setType("text/plain")
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString())
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }



    // UI utility to hide progress bar
    private fun hideProgressDialog() {
        runOnUiThread {
            binding.progressBarContainer.visibility = View.GONE
        }
    }

    // UI utility to show progress bar
    private fun showProgressDialog() {
        binding.progressMessage.text = getString(R.string.loading)
        binding.progressBarContainer.visibility = View.VISIBLE
    }

    // Clean up APIs on activity destroy
    override fun onDestroy() {
        if (liveLocationShareAPI != null) {
            liveLocationShareAPI!!.lifecycle().onDestroy()
        }
        mapUiApi.lifecycle().onDestroy()
        mapApi.lifecycle().onDestroy()
        super.onDestroy()
    }

    // CoreApi Provider implementation
    override fun provideMapstedCoreApi(): CoreApi {
        return coreApi
    }

    // MapstedMapApi Provider implementation
    override fun provideMapstedMapApi(): MapApi {
        return mapApi
    }

    override fun provideMapstedUiApi(): MapUiApi {
        return mapUiApi
    }

}