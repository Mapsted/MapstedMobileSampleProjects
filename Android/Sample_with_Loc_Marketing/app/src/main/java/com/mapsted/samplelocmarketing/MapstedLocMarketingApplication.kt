package com.mapsted.samplelocmarketing

import com.mapsted.MapstedBaseApplication
import com.mapsted.positioning.core.utils.common.Params

class MapstedLocMarketingApplication : MapstedBaseApplication() {

    override fun onCreate() {
        super.onCreate()
        Params.initialize(this)
    }

}