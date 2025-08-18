package com.mapsted.sampleaddcustomicons

import com.mapsted.MapstedBaseApplication
import com.mapsted.positioning.core.utils.common.Params

class SampleAddCustomIconsApplication : MapstedBaseApplication() {

    override fun onCreate() {
        super.onCreate()
        Params.initialize(this)
    }
}