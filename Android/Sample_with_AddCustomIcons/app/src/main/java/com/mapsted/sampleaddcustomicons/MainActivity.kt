package com.mapsted.sampleaddcustomicons

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mapsted.MapstedBaseApplication
import com.mapsted.inapp_notification.InAppNotificationsApi
import com.mapsted.inapp_notification.MapstedInAppNotificationsApi
import com.mapsted.locmarketing.LocationMarketingApi
import com.mapsted.locmarketing.MapstedLocationMarketingApi
import com.mapsted.map.MapApi
import com.mapsted.map.MapstedMapApi
import com.mapsted.map.MapstedMapApiProvider
import com.mapsted.map.models.plotting.MapIcon
import com.mapsted.positioning.CoreApi
import com.mapsted.positioning.MapstedCoreApi
import com.mapsted.positioning.SdkError
import com.mapsted.positioning.SdkStatusUpdate
import com.mapsted.positioning.core.utils.LogTime
import com.mapsted.positioning.core.utils.Logger
import com.mapsted.positioning.coreObjects.SearchEntity
import com.mapsted.positioning.util.permissions.PermissionChecks
import com.mapsted.sampleaddcustomicons.databinding.ActivityMainBinding
import com.mapsted.ui.CustomParams
import com.mapsted.ui.MapUiApi
import com.mapsted.ui.MapUiApi.MapUiInitCallback
import com.mapsted.ui.MapstedMapUiApi
import com.mapsted.ui.MapstedMapUiApiProvider
import com.mapsted.ui.utils.common.Settings
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), MapstedMapApiProvider, MapstedMapUiApiProvider {

    private lateinit var mapApi: MapApi
    private lateinit var mapUiApi: MapUiApi
    private var customParams: CustomParams? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var coreApi: CoreApi
    private var permissionChecks: PermissionChecks? = null
    private var propertyId: Int? = -1
    private var marketingApi: LocationMarketingApi? = null
    private var _inAppNotificationApi: InAppNotificationsApi? = null
    val customIconsList = ArrayList<MapIcon>()

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

    private fun getInAppNotificationApi(): InAppNotificationsApi {
        Logger.d("getInAppNotificationApi:  ")
        if (_inAppNotificationApi == null) {
            val fragmentContainerView = binding.inAppNotificationsContainer
            _inAppNotificationApi =
                MapstedInAppNotificationsApi(supportFragmentManager, fragmentContainerView)
        }
        return _inAppNotificationApi!!
    }


    private fun addCustomViewOnMap() {
        // Create Custom View
        val myView: View =
            LayoutInflater.from(this).inflate(R.layout.custom_view_on_map, null, false)
        val addIconsBtn =
            myView.findViewById<AppCompatButton>(R.id.add_icons)
        val removeIconsBtn =
            myView.findViewById<AppCompatButton>(R.id.remove_icons)

        addIconsBtn.setOnClickListener {
            addCustomIcons()
        }

        removeIconsBtn.setOnClickListener {
            removeCustomIcons()
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
        // When we initialize the SDK, we must provide both containers for the BaseMap and MapUi
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
                    hideProgressDialog()
                }
                setupLocMarketingSdk(coreApi)
                onNewIntent(intent)
            }

            override fun onStatusUpdate(sdkUpdate: SdkStatusUpdate?) {
                Logger.d("onStatusUpdate: sdkUpdate=%s", sdkUpdate)
            }

            override fun onFailure(sdkError: SdkError?) {
                Logger.w("::setupMapstedSdk ::onFailure sdkError=%s", sdkError)
            }
        })

    }

    // Initialize the Location Marketing SDK
    private fun setupLocMarketingSdk(coreApi: CoreApi) {
        /// create an instance of location marketing api
        marketingApi = MapstedLocationMarketingApi.newInstance(
            applicationContext,              // For showing dialogs, notifications, etc.
            coreApi,                         // Core SDK integration
            supportFragmentManager,          // For handling dialog fragments
            getInAppNotificationApi()        // To show in-app notifications
        ) { entity ->
            mapApi.data()?.selectEntity(entity, {
                if (it) {
                    // Retrieve the necessary identifiers (propertyId, buildingId, floorId, entityId) from the entity object for further use.
                } else {

                }
            })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (marketingApi?.events()?.canHandle(intent) == true) {
            marketingApi?.events()?.onNewIntent(intent)
        }
    }

    // Trigger add custom icons on map
    fun addCustomIcons() {
        getAllEntitiesForProperty()
        mapApi.mapView().customPlot().addIcons(customIconsList)
    }

    // remove custom icons on map
    fun removeCustomIcons() {
        mapApi.mapView().customPlot().removeIcons(customIconsList)
    }

    fun getAllEntitiesForProperty() {
        lifecycleScope.launch {
            propertyId?.let {
                coreApi.properties().getSearchEntityListByPropertyId(it, { searchEntities ->
                    val allowedIds = setOf(75, 90, 407, 398)
                    val filteredList = searchEntities.filter { it.entityZones.first().entityId in allowedIds }
                    createCustomIcons(filteredList)
                })
            }
        }
    }

    fun createCustomIcons(searchEntities: List<SearchEntity>): ArrayList<MapIcon> {
        customIconsList.clear()
        for(i in 0 until searchEntities.size) {
            searchEntities[i].getLocations(coreApi, { zones ->
                val mapIcon = MapIcon.Builder() // Replaced line
                    .setFallbackDrawableRes(R.drawable.ic_icon_map_location)
                    .setMercatorZone(zones.get(0))
                    .setSize(24f)
                    .build()
                customIconsList.add(mapIcon)
            })
        }
        return customIconsList;
    }


    // Handle device configuration changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mapUiApi.lifecycle()?.onConfigurationChanged(this, newConfig)
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