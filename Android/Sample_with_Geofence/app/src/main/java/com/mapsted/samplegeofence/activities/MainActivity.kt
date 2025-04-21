package com.mapsted.samplegeofence.activities

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mapsted.MapstedBaseApplication
import com.mapsted.geofence.GeofenceApi
import com.mapsted.geofence.MapstedGeofenceApi
import com.mapsted.geofence.triggers.BuildingLocationCriteria
import com.mapsted.geofence.triggers.FloorLocationCriteria
import com.mapsted.geofence.triggers.GeofenceTrigger
import com.mapsted.geofence.triggers.ILocationCriteria
import com.mapsted.geofence.triggers.PoiVicinityLocationCriteria
import com.mapsted.geofence.triggers.PropertyLocationCriteria
import com.mapsted.map.MapApi
import com.mapsted.map.MapstedMapApi
import com.mapsted.map.MapstedMapApiProvider
import com.mapsted.positioning.CoreApi
import com.mapsted.positioning.MapstedCoreApi
import com.mapsted.positioning.SdkError
import com.mapsted.positioning.SdkStatusUpdate
import com.mapsted.positioning.core.utils.LogTime
import com.mapsted.positioning.core.utils.Logger
import com.mapsted.positioning.coreObjects.EntityZone
import com.mapsted.positioning.util.permissions.PermissionChecks
import com.mapsted.samplegeofence.R
import com.mapsted.samplegeofence.databinding.ActivityMainBinding
import com.mapsted.ui.CustomParams
import com.mapsted.ui.MapUiApi
import com.mapsted.ui.MapstedMapUiApi
import com.mapsted.ui.MapstedMapUiApiProvider
import com.mapsted.ui.utils.common.Settings

class MainActivity : AppCompatActivity() , MapstedMapApiProvider, MapstedMapUiApiProvider {

    private lateinit var binding: ActivityMainBinding
    private var coreApi: CoreApi? = null
    private var geofenceApi: GeofenceApi? = null
    private var PROPERTY_ID = 504
    private var BUILDING_ID = 504
    private var ENTITY_ID = 414
    private var FLOOR_ID = 941
    private lateinit var mapApi: MapApi
    private lateinit var mapUiApi: MapUiApi
    private var customParams: CustomParams? = null
    private var permissionChecks: PermissionChecks? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize core API
        coreApi = (application as? MapstedBaseApplication)?.getCoreApi(this) ?: run {
            val newCoreApi = MapstedCoreApi.newInstance(this)
            (application as? MapstedBaseApplication)?.setCoreApi(newCoreApi)
            newCoreApi
        }

        // Initialize map and UI APIs
        mapApi = MapstedMapApi.newInstance(this, coreApi!!)
        mapUiApi = MapstedMapUiApi.newInstance(this, mapApi)

        // Set up view binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Set up permission checks
        permissionChecks = PermissionChecks(
            coreApi!!,
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
        mapUiApi.setup().initialize(customParams!!, object : MapUiApi.MapUiInitCallback {
            override fun onCoreInitialized() {
                Logger.i("onCoreInitialized: ")
            }

            override fun onMapInitialized() {
                Logger.i("onMapInitialized: ")
                LogTime.getInstance().end(LogTime.LogTimeKey.MAP_SDK_INITIALIZE)
            }

            override fun onSuccess() {
                Log.d("SUCCESS", "Intialized Successfully")
                hideProgressDialog()
                intializeGeofenceSDK()
            }

            override fun onStatusUpdate(sdkUpdate: SdkStatusUpdate?) {
                Logger.d("onStatusUpdate: sdkUpdate=%s", sdkUpdate)
            }

            override fun onFailure(sdkError: SdkError?) {
                Logger.w("::setupMapstedSdk ::onFailure sdkError=%s", sdkError)
            }
        })

    }

    // Permission result callback
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    // Callback for location permissions granted
    private val onPermissionReadyCallback: PermissionChecks.LocationPermissionListener =
        object : PermissionChecks.LocationPermissionListener {
            override fun onLocationPermissionsGranted() {

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

    private fun intializeGeofenceSDK(){
        // Create instance of GeofenceApi
        geofenceApi = MapstedGeofenceApi.newInstance(this, coreApi)

        // Create a list of geofence triggers
        val geofenceTriggers: java.util.ArrayList<GeofenceTrigger> = ArrayList()

        // Add geofence trigger for entering the property
        geofenceTriggers.add(createGeofenceTriggerProperty("Trigger-Property-Entry-${PROPERTY_ID}",ILocationCriteria.LocationTriggerDirection.ON_ENTER))
        // Add geofence trigger for exiting the property
        geofenceTriggers.add(createGeofenceTriggerProperty("Trigger-Property-Exit-${PROPERTY_ID}",ILocationCriteria.LocationTriggerDirection.ON_EXIT))

        // Add geofence trigger for entering the building
        geofenceTriggers.add(createGeofenceTriggerBuilding("Trigger-Building-Entry-${BUILDING_ID}",ILocationCriteria.LocationTriggerDirection.ON_ENTER))
        // Add geofence trigger for existing the building
        geofenceTriggers.add(createGeofenceTriggerBuilding("Trigger-Building-Exit-${BUILDING_ID}",ILocationCriteria.LocationTriggerDirection.ON_EXIT))

        // Add geofence trigger for entering the floor
        geofenceTriggers.add(createGeofenceTriggerFloor("Trigger-Floor-Entry-${FLOOR_ID}",ILocationCriteria.LocationTriggerDirection.ON_ENTER))
        // Add geofence trigger for existing the floor
        geofenceTriggers.add(createGeofenceTriggerFloor("Trigger-Floor-Exit-${FLOOR_ID}",ILocationCriteria.LocationTriggerDirection.ON_EXIT))

        // Add geofence trigger for entering the Entity
        geofenceTriggers.add(createGeofenceTriggerEntity("Trigger-Entity-Entry-${ENTITY_ID}",ILocationCriteria.LocationTriggerDirection.ON_ENTER))
        // Add geofence trigger for existing the Entity
        geofenceTriggers.add(createGeofenceTriggerEntity("Trigger-Entity-Exit-${ENTITY_ID}",ILocationCriteria.LocationTriggerDirection.ON_EXIT))

        geofenceApi?.geofenceTriggers()?.addGeofenceTriggers(PROPERTY_ID, geofenceTriggers)

        // Add callback listener
        geofenceApi!!.geofenceTriggers().addListener { propertyId: Int, geofenceId: String? ->
//            boolean unSubscribeSuccess = geofenceApi.geofenceTriggers().removeGeofenceTrigger(propertyId, geofenceId);
            //            Logger.v("onGeofenceTriggered: pId: %d -> geofenceId: %s, unSubscribe: %s",
//                    propertyId, geofenceId, unSubscribeSuccess ? "Success" : "Failed");
            var alertTitle: String? = ""
            var alertMessage: String? = ""

            when (geofenceId) {
                "Trigger-Entity-Entry-414" -> {
                    alertTitle = "Entity Entry alert"
                    alertMessage = "You are entering Foot Locker at Square One Shopping Centre."
                }
                "Trigger-Entity-Exit-414" -> {
                    alertTitle = "Entity Exit Alert"
                    alertMessage = "You just exited Foot Locker at Square One Shopping Centre."
                }
                "Trigger-Property-Entry-504" -> {
                    alertTitle = "Property Entry alert"
                    alertMessage = "You are entering the Square One Shopping Centre property."
                }
                "Trigger-Property-Exit-504" -> {
                    alertTitle = "Property Exit alert"
                    alertMessage = "You just exited the Square One Shopping Centre property."
                }
                "Trigger-Building-Entry-504" -> {
                    alertTitle = "Building Entry alert"
                    alertMessage = "You are entering the Square One Shopping Centre building."
                }
                "Trigger-Building-Exit-504" -> {
                    alertTitle = "Building Exit alert"
                    alertMessage = "You just exited the Square One Shopping Centre building."
                }
                "Trigger-Floor-Entry-941" -> {
                    alertTitle = "Floor Entry alert"
                    alertMessage = "You are entering the Floor - L1 on Square One Shopping Centre."
                }
                "Trigger-Floor-Exit-941" -> {
                    alertTitle = "Floor Exit alert"
                    alertMessage = "You just exited the Floor - L1 on Square One Shopping Centre."
                }
                else -> println("Unknown")
            }

            runOnUiThread {
                showAlertDialog(alertTitle, alertMessage)
            }

            Logger.v(
                "onGeofenceTriggered: pId: %d -> geofenceId: %s",
                propertyId,
                geofenceId
            )
        }

    }

    fun showAlertDialog(alertTitle: String?, alertMessage: String?){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(alertTitle)
        builder.setMessage(alertMessage)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun createGeofenceTriggerEntity(triggerId: String, direction: ILocationCriteria.LocationTriggerDirection): GeofenceTrigger {
        val locationCriteria = PoiVicinityLocationCriteria.Builder()
            .addEntityZone(EntityZone(PROPERTY_ID, BUILDING_ID, FLOOR_ID, ENTITY_ID))
            .setActivationDistanceTh(30.0f)
            .setTriggerDirection(direction)
            .build()

        return GeofenceTrigger.Builder(PROPERTY_ID, triggerId)
            .setLocationCriteria(locationCriteria)
            .build()
    }

    fun createGeofenceTriggerFloor(triggerId: String, direction: ILocationCriteria.LocationTriggerDirection): GeofenceTrigger {
        val locationCriteria = FloorLocationCriteria.Builder(FLOOR_ID)
            .setTriggerDirection(direction)
            .build()

        return GeofenceTrigger.Builder(PROPERTY_ID, triggerId)
            .setLocationCriteria(locationCriteria)
            .build()
    }

    fun createGeofenceTriggerBuilding(triggerId: String, direction: ILocationCriteria.LocationTriggerDirection): GeofenceTrigger {
        val locationCriteria = BuildingLocationCriteria.Builder(BUILDING_ID)
            .setTriggerDirection(direction)
            .build()

        return GeofenceTrigger.Builder(PROPERTY_ID, triggerId)
            .setLocationCriteria(locationCriteria)
            .build()
    }

    fun createGeofenceTriggerProperty(triggerId: String, direction: ILocationCriteria.LocationTriggerDirection): GeofenceTrigger {
        val locationCriteria = PropertyLocationCriteria.Builder(PROPERTY_ID)
            .setTriggerDirection(direction)
            .build()

        return GeofenceTrigger.Builder(PROPERTY_ID, triggerId)
            .setLocationCriteria(locationCriteria)
            .build()
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
        runOnUiThread {
            binding.progressBarContainer.visibility = View.VISIBLE
        }
    }

    // Clean up APIs on activity destroy
    override fun onDestroy() {
        mapUiApi.lifecycle().onDestroy()
        mapApi.lifecycle().onDestroy()
        super.onDestroy()
    }

    // CoreApi Provider implementation
    override fun provideMapstedCoreApi(): CoreApi {
        return coreApi!!
    }

    // MapstedMapApi Provider implementation
    override fun provideMapstedMapApi(): MapApi {
        return mapApi
    }

    // MapstedMapUiApi Provider implementation
    override fun provideMapstedUiApi(): MapUiApi {
        return mapUiApi
    }


}