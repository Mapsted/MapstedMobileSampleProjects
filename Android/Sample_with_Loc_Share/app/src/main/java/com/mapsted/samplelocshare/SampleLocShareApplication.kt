package com.mapsted.samplelocshare

import com.mapsted.MapstedBaseApplication
import com.mapsted.positioning.core.utils.common.Params

class SampleLocShareApplication : MapstedBaseApplication() {

    override fun onCreate() {
        super.onCreate()
        Params.initialize(this)
    }
}