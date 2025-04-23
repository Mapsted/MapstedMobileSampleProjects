package com.mapsted.samplegeofence

import com.mapsted.MapstedBaseApplication
import com.mapsted.positioning.core.utils.common.Params

class SampleGeofenceApplication : MapstedBaseApplication() {

    override fun onCreate() {
        super.onCreate()
        Params.initialize(this)
    }

}