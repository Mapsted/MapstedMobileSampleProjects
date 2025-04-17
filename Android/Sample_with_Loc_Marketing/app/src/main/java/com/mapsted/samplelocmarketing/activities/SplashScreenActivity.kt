package com.mapsted.samplelocmarketing.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mapsted.MapstedBaseApplication
import com.mapsted.corepositioning.cppObjects.swig.Position
import com.mapsted.positioning.CoreApi
import com.mapsted.positioning.CoreApi.CoreInitCallback
import com.mapsted.positioning.CoreApi.Location.PositionChangeListener
import com.mapsted.positioning.CoreApi.LocationServicesCallback
import com.mapsted.positioning.MapstedCoreApi
import com.mapsted.positioning.SdkError
import com.mapsted.positioning.SdkStatusUpdate
import com.mapsted.positioning.core.utils.Logger
import com.mapsted.positioning.core.utils.helpers.ErrorHelper
import com.mapsted.positioning.util.permissions.PermissionChecks
import com.mapsted.samplelocmarketing.R

class SplashScreenActivity : AppCompatActivity() {

    private var permissionChecks: PermissionChecks? = null
    var coreApi: CoreApi? = null
    var splashLoading: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        intiViews()
        initMapstedCoreSdk()
    }

    private fun intiViews() {
        splashLoading = findViewById(R.id.splash_loading)
    }

    // Check and request necessary permissions
    private fun checkPermisisons() {
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

    // Initialize Mapsted Core SDK and handle permissions
    private fun initMapstedCoreSdk() {
        splashLoading?.visibility = View.VISIBLE
        if (coreApi == null) {
            val application = application
            if (application != null) {
                val mapstedBaseApplication = application as MapstedBaseApplication
                coreApi = mapstedBaseApplication.getCoreApi(mapstedBaseApplication)
                if (coreApi == null) {
                    coreApi = MapstedCoreApi.newInstance(mapstedBaseApplication)
                    try {
                        mapstedBaseApplication.setCoreApi(coreApi)
                    } catch (e: Exception) {
                        Logger.e(ErrorHelper.getError(e))
                    }
                }
            } else {
                coreApi = MapstedCoreApi.newInstance(this)
            }
        }
        // Initialize SDK setup
        coreApi!!.setup().initialize(null, object : CoreInitCallback {
            override fun onSuccess() {
                // Setup permission checks and callbacks
                permissionChecks = PermissionChecks(
                    coreApi!!,
                    this@SplashScreenActivity, onPermissionReadyCallback, null
                ) { success: Boolean? ->
                    startLocationService()
                }

                if (permissionChecks!!.hasLocationPermissions()) {
                    startLocationService()
                } else {
                    checkPermisisons()
                }
            }

            override fun onFailure(sdkError: SdkError) {
                runOnUiThread {
                    splashLoading?.visibility = View.GONE
                }
            }

            override fun onStatusUpdate(sdkStatusUpdate: SdkStatusUpdate) {

            }
        })
    }

    // Start Mapsted location services and listen for location updates
    private fun startLocationService() {
        coreApi!!.analytics().setLocationLogging(true)
        coreApi!!.setup().restrictOffPremiseBackgroundLocationScanning(false)
        val mapstedBaseApplication = application as MapstedBaseApplication
        coreApi!!.setup()
            .startLocationServices(mapstedBaseApplication, object : LocationServicesCallback {
                override fun onSuccess() {
                    coreApi?.locations()
                        ?.addPositionChangeListener(object : PositionChangeListener {
                            override fun accept(t: Position?) {
                                runOnUiThread {
                                    splashLoading?.visibility = View.GONE
                                    openMainActivity()
                                }
                            }
                        })
                }

                override fun onFailure(sdkError: SdkError) {

                }
            })

    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private val onPermissionReadyCallback: PermissionChecks.LocationPermissionListener =
        object : PermissionChecks.LocationPermissionListener {
            override fun onLocationPermissionsGranted() {

            }
        }
}