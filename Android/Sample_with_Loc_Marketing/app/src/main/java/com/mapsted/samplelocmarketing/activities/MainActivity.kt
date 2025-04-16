package com.mapsted.samplelocmarketing.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.mapsted.MapstedBaseApplication
import com.mapsted.inapp_notification.InAppNotification
import com.mapsted.inapp_notification.InAppNotificationsApi
import com.mapsted.inapp_notification.MapstedInAppNotificationsApi
import com.mapsted.locmarketing.LocationMarketingApi
import com.mapsted.locmarketing.MapstedLocationMarketingApi
import com.mapsted.locmarketing.model.HomeEntity
import com.mapsted.positioning.CoreApi
import com.mapsted.positioning.CoreApi.Location.NearbyPropertiesChangedListener
import com.mapsted.positioning.core.utils.Logger
import com.mapsted.samplelocmarketing.PropertyDetails
import com.mapsted.samplelocmarketing.R
import com.mapsted.samplelocmarketing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LocationMarketingApi.LocationMarketingCallback {

    var coreApi: CoreApi? = null
    private var marketingApi: LocationMarketingApi? = null
    private lateinit var binding: ActivityMainBinding
    private var _inAppNotificationApi: InAppNotificationsApi? = null
    val listPropertiesId = java.util.ArrayList<Int>()
    private val propertyList = ArrayList<PropertyDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val mapstedBaseApplication = application as MapstedBaseApplication
        // Initialize Core API from application context
        coreApi = mapstedBaseApplication.getCoreApi(mapstedBaseApplication)

        // Start fetching data if Core API is ready
        coreApi?.let {
            showHideProgress(View.VISIBLE)
            setupLocMarketingSdk(it)
            getNearByProperties()
        }

    }

    // Retrieve and display nearby properties
    private fun getNearByProperties() {
        val nearbyProperties = coreApi!!.locations().nearbyProperties
        if (!nearbyProperties.isEmpty()) {
            listPropertiesId.addAll(nearbyProperties)
            for (propertyId in listPropertiesId) {
                addPropertyInList(propertyId)
            }
            updatePropertyDetails()
        }
        addNearByPropertyListener()
    }

    // Add property details to local list
    private fun addPropertyInList(propertyId: Int) {
        val propertyInfo = coreApi!!.properties().getInfo(propertyId)
        val propertyImage = propertyInfo?.coverImagesUrl?.first()
        val propertyName = if (propertyInfo != null
        ) (if (propertyInfo.longName != null
        ) propertyInfo.longName
        else propertyInfo.shortName)
        else ""
        val propertyDetails = PropertyDetails(propertyName, propertyId, propertyImage)
        propertyList.add(propertyDetails)
    }

    /**
     * Registers a listener to monitor nearby property changes.
     *
     * - When new properties are detected (`onAdded`), they are added to the list and displayed.
     * - When properties are no longer nearby (`onRemoved`), they are removed from the list and UI.
     * - UI is updated accordingly to reflect changes in nearby properties.
     */
    private fun addNearByPropertyListener() {
        coreApi!!.locations()
            .addNearbyPropertyChangeListener(object : NearbyPropertiesChangedListener {
                override fun onAdded(set: Set<Int>) {
                    for (propertyId in set) {
                        if (!listPropertiesId.contains(propertyId)) {
                            listPropertiesId.add(propertyId)
                            addPropertyInList(propertyId)
                        }
                    }
                    if (listPropertiesId.isNotEmpty()) {
                        showHideProgress(View.GONE)
                    }
                    updatePropertyDetails()
                }

                override fun onRemoved(set: Set<Int>) {
                    for (propertyId in set) {
                        listPropertiesId.remove(propertyId)
                        propertyList.removeIf { it.propertyId == propertyId }
                    }
                    updatePropertyDetails()
                }
            })
    }

    // Update UI with the first property in the list
    private fun updatePropertyDetails() {
        runOnUiThread {
            binding.tvPropertyName.setText(propertyList.firstOrNull()?.name)
            Glide.with(this).load(propertyList.firstOrNull()?.propertyImage)
                .into(binding.imgProperty)
        }
    }

    // Set up location marketing SDK
    private fun setupLocMarketingSdk(coreApi: CoreApi) {

        marketingApi = MapstedLocationMarketingApi.newInstance(
            applicationContext,
            coreApi,
            supportFragmentManager,
            getInAppNotificationApi(),
            this
        )
        marketingApi?.setup()?.initialize()
    }

    //  initialization of the in-app notifications API
    private fun getInAppNotificationApi(): InAppNotificationsApi {
        Logger.d("getInAppNotificationApi:  ")
        if (_inAppNotificationApi == null) {
            val fragmentContainerView = binding.inAppNotificationsContainer
            _inAppNotificationApi =
                MapstedInAppNotificationsApi(supportFragmentManager, fragmentContainerView)
        }
        return _inAppNotificationApi!!
    }

    // Show or hide progress bar
    private fun showHideProgress(visibility: Int) {
        runOnUiThread {
            binding.progressBar.visibility = visibility
        }
    }

    // Navigation app call back
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.d("onActivityResult: requestCode %s, resultCode %s", requestCode, resultCode)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        Logger.d("onDestroy:  ")
        Process.killProcess(Process.myPid())
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Toast.makeText(
            applicationContext,
            getString(R.string.optimizing_the_app_for_the_new_configurations),
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        super.onConfigurationChanged(newConfig)
        finishAffinity()
        System.exit(0)
    }

    override fun performEventAction(p0: String?, p1: MutableList<HomeEntity>?) {

    }

    override fun inAppNotificationClicked(p0: InAppNotification?) {

    }


}
