package com.mapsted.compose_demo

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mapsted.compose_demo.databinding.MapUiBinding
import com.mapsted.map.MapApi
import com.mapsted.map.MapstedMapApi
import com.mapsted.map.models.layers.BaseMapStyle
import com.mapsted.map.views.MapPanType
import com.mapsted.map.views.MapstedMapRange
import com.mapsted.positioning.CoreApi
import com.mapsted.positioning.SdkError
import com.mapsted.positioning.SdkStatusUpdate
import com.mapsted.ui.CustomParams
import com.mapsted.ui.MapUiApi
import com.mapsted.ui.MapstedMapUiApi
import com.mapsted.ui.MapstedMapUiApiProvider
import java.util.Locale
import java.util.stream.Collectors

// Main activity responsible for rendering and interacting with the Mapsted map UI
class MapActivity : AppCompatActivity(), MapstedMapUiApiProvider {

    // View binding reference to access UI elements from layout file
    private lateinit var binding: MapUiBinding

    // APIs for Core functionality, Map logic, and Map UI
    private var coreApi: CoreApi? = null
    private var mapApi: MapApi? = null
    private var mapUiApi: MapUiApi? = null

    private var tActivityStart = 0L
    private var tStartInitMapsted = 0L
    private var tStartPlotRequest = 0L
    private var tPlotFinished = 0L

    companion object {
        private val TAG: String = MapActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.map_ui)
        binding.mapSpinner.show()

        // Initialize Core, Map, and Map UI APIs
        coreApi = (application as DemoApplication).getCoreApi(this)
        mapApi = MapstedMapApi.newInstance(application, coreApi!!)
        mapUiApi = MapstedMapUiApi.newInstance(application, mapApi!!)

        tActivityStart = System.currentTimeMillis()

        // Start setting up the Map UI
        setupMapUiApi()
        // add below lines to integrate top notifications sdk,
        // val  _topNotificationApi = MapstedTopbarNotificationApi(supportFragmentManager, binding.notificationContainer,mapApi)
    }

    override fun onDestroy() {
        Log.i(TAG, "::onDestroy")

        // Clean up resources used by Map and UI APIs
        mapUiApi?.lifecycle()?.onDestroy()
        mapApi?.lifecycle()?.onDestroy()
        coreApi?.lifecycle()?.onDestroy()

        super.onDestroy()
    }

    // MapstedMapUiApiProvider implementation
    override fun provideMapstedUiApi(): MapUiApi? {
        return mapUiApi
    }
    override fun provideMapstedMapApi(): MapApi? {
        return mapApi
    }
    override fun provideMapstedCoreApi(): CoreApi? {
        return coreApi
    }


    override fun onBackPressed() {
        // If the map UI handles back press, intercept it
        if (mapUiApi != null && mapUiApi!!.lifecycle().onBackPressed()) {
            Log.i(TAG, "::onBackPressed - Handled by Mapsted")
            return
        }

        Log.i(TAG, "::onBackPressed - Processed")
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass configuration changes to Map UI
        mapUiApi?.lifecycle()?.onConfigurationChanged(this, newConfig)
    }

    // Initializes Mapsted UI with required setup and loads the map
    private fun setupMapUiApi() {
        // Prepare custom map parameters
        val params = CustomParams.newBuilder(this, binding.flBaseMap, binding.flMapUi)
            .setBaseMapStyle(BaseMapStyle.GREY)
            .setMapPanType(MapPanType.RESTRICT_TO_SELECTED_PROPERTY)
            .setMapZoomRange(MapstedMapRange(6.0f, 24.0f))
            .setEnableMapOverlayFeature(true)
            .build()

        tStartInitMapsted = System.currentTimeMillis()

        // Initialize the Map UI with given parameters and callbacks
        mapUiApi?.setup()?.initialize(
            params,
            object : MapUiApi.MapUiInitCallback {
                override fun onCoreInitialized() {
                    Log.d(TAG, "setupMapUiApi: onCoreInitialized")
                }

                override fun onMapInitialized() {
                    Log.d(TAG, "setupMapUiApi: onMapInitialized")
                }

                override fun onSuccess() {
                    binding.mapSpinner.hide()
                    Log.d(TAG, "setupMapUiApi: onSuccess")

                    // Fetch available properties from the license
                    // Grab first property in licence
                    coreApi?.properties()?.getInfos() {  propertyInfos -> run {
                        val numProperties = propertyInfos?.size
                        val propertyId = if (numProperties != null && numProperties > 0) propertyInfos.keys.first() else -1;

                        Log.d(TAG, "setupMapUiApi: onSuccess: ${propertyInfos?.keys}")

                        tStartPlotRequest = System.currentTimeMillis()

                        // Select the property and request to draw it on the map
                        mapApi?.data()?.selectPropertyAndDrawIfNeeded(
                            propertyId,
                            object : MapApi.PropertyPlotListener {

                                override fun onCached(success: Boolean) {
                                    // Optional: Handle property cached
                                }

                                override fun onPlotted(success: Boolean) {
                                    tPlotFinished = System.currentTimeMillis()

                                    val dtActivityToPlotSec = (tPlotFinished - tActivityStart) / 1000.0
                                    val dtMapInitSec = (tStartPlotRequest - tStartInitMapsted) / 1000.0
                                    val dtPlotSec = (tPlotFinished - tStartPlotRequest) / 1000.0

                                    val msg = String.format(Locale.CANADA, "Property ($propertyId): %s -> PlotTime: %.1f s",
                                        if (success) "Success" else "Failed", dtPlotSec)

                                    Log.d(TAG, msg)

                                    Log.d(TAG, String.format(Locale.CANADA,
                                        "dtActivityToPlot_s: %.3f s, dtMapInit_s: %.3f, dtPlot_s: %.3f",
                                        dtActivityToPlotSec, dtMapInitSec, dtPlotSec))

                                }

                                override fun onPlottedBuilding(buildingId: Int, success: Boolean) {
                                    Log.d(TAG, String.format(Locale.CANADA, "Property ($propertyId), Building ($buildingId): %s",
                                        if (success) "Success" else "Failed"))
                                }

                                override fun onPlottedBuildingsComplete(
                                    successfulBuildingIds: MutableSet<Int>?,
                                    failedBuildingIds: MutableSet<Int>?
                                ) {
                                    val successBuildingsStr = successfulBuildingIds?.stream()?.map { b -> b.toString() }?.collect(Collectors.joining(","))
                                    val failedBuildingsStr = failedBuildingIds?.stream()?.map { b -> b.toString() }?.collect(Collectors.joining(","))

                                    Log.d(TAG, String.format(Locale.CANADA, "onPlottedBuildingsComplete ($propertyId), successBuildings { %s }, failedBuildings { %s }",
                                        successBuildingsStr, failedBuildingsStr))
                                }

                            }
                        )
                        // Start location services
//                        coreApi?.setup()?.startLocationServices(this@MapActivity,
//                            object : CoreApi.LocationServicesCallback {
//                                override fun onSuccess() {
//                                    Log.d(TAG, "coreApi callback -> LocServices: onSuccess")
//                                }
//                                override fun onFailure(sdkError: SdkError) {
//                                    Log.d(TAG, "coreApi callback -> LocServices: onFailure: $sdkError")
//                                }
//                            })
                    }
                    }
                }

                override fun onFailure(sdkError: SdkError) {
                    Log.d(TAG, "setupMapUiApi: onFailure: $sdkError")
                }

                override fun onStatusUpdate(sdkUpdate: SdkStatusUpdate) {
                    Log.d(TAG, "setupMapUiApi: onStatusUpdate: $sdkUpdate")
                }
            },
            // Callback for location services during UI setup
            object : CoreApi.LocationServicesCallback {
                override fun onSuccess() {
                    Log.d(TAG, "LocServices: onSuccess")
                }
                override fun onFailure(sdkError: SdkError) {
                    Log.d(TAG, "LocServices: onFailure: $sdkError")
                }
            }
        )
    }
}