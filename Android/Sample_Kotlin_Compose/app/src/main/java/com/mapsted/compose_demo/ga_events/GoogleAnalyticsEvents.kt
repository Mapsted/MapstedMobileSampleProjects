package com.mapsted.compose_demo.ga_events

import android.util.Log
import com.mapsted.corepositioning.cppObjects.bridge_interfaces.MarketingEventCallback
import com.mapsted.corepositioning.cppObjects.swig.MarketingDataCallback
import com.mapsted.corepositioning.cppObjects.swig.RoutePathType
import com.mapsted.map.MapApi
import com.mapsted.map.MapApi.PropertyPlotListener
import com.mapsted.map.MapSelectionChangeListener
import com.mapsted.map.models.events.ClickType.CLICK_TYPE_DESELECT
import com.mapsted.map.models.events.ClickType.CLICK_TYPE_DOUBLE
import com.mapsted.map.models.events.ClickType.CLICK_TYPE_DUAL
import com.mapsted.map.models.events.ClickType.CLICK_TYPE_LONG
import com.mapsted.map.models.events.ClickType.CLICK_TYPE_SINGLE
import com.mapsted.map.models.events.MapClickEvent
import com.mapsted.map.models.events.MapEvent
import com.mapsted.positioning.CoreApi
import com.mapsted.positioning.core.utils.Logger
import com.mapsted.positioning.coreObjects.Entity
import com.mapsted.positioning.navigation.NavigationCallback
import com.mapsted.positioning.navigation.NavigationError
import com.mapsted.positioning.navigation.NavigationState
import com.mapsted.positioning.routing.Route
import com.mapsted.positioning.routing.RoutePathSegment
import com.mapsted.positioning.routing.RoutePointInstruction
import com.mapsted.positioning.routing.Waypoint
import com.mapsted.ui.MapUiApi
import com.mapsted.ui.search.EntityGroup
import com.mapsted.ui.search.SearchListener

class GoogleAnalyticsEvents(val mapApi: MapApi?, val coreApi: CoreApi?, val mapUiApi: MapUiApi) {

    var propertyId = 1

    //#5 on Map Interacted ,Map Click Event
    fun setupMapClickEvents() {
        mapApi?.mapView()?.addMapClickListener(object : MapApi.MapClickListener {
            override fun onMapClickEvent(mapClickEvent: MapClickEvent?) {
                when (mapClickEvent?.clickType) {
                    CLICK_TYPE_SINGLE -> Log.e("MapClick", "Single Click")
                    CLICK_TYPE_DOUBLE -> Log.e("MapClick", "Double Click")
                    CLICK_TYPE_LONG -> Log.d("MapClick", "Long Click")
                    CLICK_TYPE_DUAL -> Log.d("MapClick", "Dual Click")
                    CLICK_TYPE_DESELECT -> Log.d("MapClick", "Deselect Click")
                    else -> Log.d("MapClick", "Unknown Click Type")
                }
            }
        })
    }

    //#5  For Map eventlistner
    fun setupMapEvents() {
        mapApi?.mapView()?.addMapEventListener(object : MapApi.MapEventListener {
            override fun onMapEvent(event: MapEvent?) {
                when (event?.eventType) {
                    MapEvent.MAP_MOVED -> Log.d("MapEvent", "Map Moved")
                    MapEvent.MAP_IDLE -> Log.d("MapEvent", "Map Idle")
                    MapEvent.MAP_STABLE -> Log.d("MapEvent", "Map Stable")
                }
            }
        })
    }

    // #6,#16  When a user selects a specific floor, When user taps any POI on the map
    fun setupSelectionChangeEvents() {
        mapApi?.mapView()?.data()
            ?.addMapSelectionChangeListener(object : MapSelectionChangeListener {
                override fun onPropertySelectionChange(propertyId: Int, previousPropertyId: Int) {
                    Log.d(
                        "SelectionChange",
                        "Property selected: $propertyId -> $previousPropertyId"
                    )
                }

                override fun onBuildingSelectionChange(
                    propertyId: Int,
                    buildingId: Int,
                    previousBuildingId: Int
                ) {
                    Log.d(
                        "SelectionChange",
                        "Building selected: $propertyId -> $buildingId on floor $previousBuildingId"
                    )
                }

                override fun onFloorSelectionChange(
                    propertyId: Int,
                    buildingId: Int,
                    floorId: Int
                ) {
                    Log.d("SelectionChange", "Floor selected: $propertyId -> $buildingId")
                }

                override fun onEntitySelectionChange(entity: Entity?) {
                    Log.e("SelectionChange", "Entity selected: ${entity?.name ?: "Unknown"}")
                }
            })
    }

    // #10,#13,#31 Navigation Started, multi_point_route_initiated , When any error occurs during navigation
    // #11,#12  Navigation Completed,Navigation Rerouted
    fun setupRoutingRequestEvents() {
        coreApi?.routing()?.addNavigationListener(object : NavigationCallback{
            override fun onDestinationReached(destination: Waypoint) {
                Logger.d("Reached destination")
            }

            override fun onNavigationInitFailure(error: NavigationError) {
                Logger.e("onNavigationInitFailure: %s", error.errorMessage)
            }

            override fun onNavigationInitSuccess(navState: NavigationState) {
                Logger.d("Navigation started")
            }

            override fun onNavigationStopped() {
                Logger.d("Navigation stopped")
            }

            override fun onRouteInstruction(
                currentInstruction: RoutePointInstruction,
                nextInstruction: RoutePointInstruction,
                afterInstruction: RoutePointInstruction?
            ) {
                Logger.d("currentInstruction: %s", currentInstruction)
                Logger.d("nextInstruction: %s", nextInstruction)
                Logger.d("afterInstruction: %s", afterInstruction)
            }

            override fun onRouteRecalculation(
                state: NavigationState,
                newRoute: Route,
                routePathType: RoutePathType
            ) {
                Logger.d("onRouteRecalculation: %s", state)
            }

            override fun onRouteSegmentReached(
                currentSegment: RoutePathSegment,
                visitedSegments: List<RoutePathSegment>,
                upcomingSegments: List<RoutePathSegment>
            ) {
                Logger.d("onRouteSegmentReached: %s", currentSegment)
            }

            override fun onUserProgressAlongRoute(state: NavigationState) {
                Logger.d("onRouteRecalculation: %s", state)
            }
        })
    }

    //#27,#28  proximity offer triggered( Will provide campaignId & Adani can link that with the trigger), Recommendation triggered
    fun setupMarketingEvents() {
        coreApi?.marketing()?.addMarketingEventListener(object : MarketingEventCallback {
            override fun onMarketingContentRequest(
                propertyId: Int,
                callback: MarketingDataCallback?
            ) {

            }

            override fun onMarketingEvent(
                propertyId: Int,
                triggerId: String?,
                campaignId : String?
            ) {
                /*marketingApi?.repo()?.getFeedsUsingPropertyId(propertyId){ feedList ->
                    val feed = feedList.first { it.campaignId == campaignId }
                    // use feed instance for own operation
                }*/
            }
        })
    }

    // #30 Map load time
    fun trackMapLoadTime() {
        mapApi?.mapView()?.data()
            ?.selectPropertyAndDrawIfNeeded(propertyId, object : PropertyPlotListener {
                override fun onCached(isCached: Boolean) {
                    Log.d("MapLoad", "Map cache status: $isCached")
                }

                override fun onPlotted(success: Boolean) {
                    Log.d("MapLoad", "Map plotted: $success")
                }

                override fun onPlottedBuilding(buildingId: Int, success: Boolean) {
                    Log.d("MapLoad", "Building $buildingId plotted: $success")
                }

                override fun onPlottedBuildingsComplete(
                    plotted: MutableSet<Int>?,
                    failed: MutableSet<Int>?
                ) {
                    Log.d("MapLoad", "Plotted buildings: ${plotted?.size}, Failed: ${failed?.size}")
                }
            })
    }

    // #25 On Search result click
    fun onSearchResultSelection() {
        mapUiApi.events()?.setSearchListener(object : SearchListener {
            override fun onCrossPropertySearchEntityGroupSelected(item: EntityGroup) {

            }

            override fun onFeedItemSelected(feedId: String?) {

            }

            override fun onAlertSelected(alertId: String?) {

            }

        })
    }

}