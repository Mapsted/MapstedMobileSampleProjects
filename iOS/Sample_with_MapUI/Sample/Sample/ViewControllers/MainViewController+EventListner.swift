
//
//  Main.swift
//  Sample
//
//  Created by admin on 12/06/25.
//  Copyright © 2025 Mapsted. All rights reserved.
//

import MapstedCore
import MapstedMap
import MapstedMapUi

// MARK: - Property Download Delegate
extension MainViewController: PropertyAutoDownloadListener {
    
    //Called when the property download completes successfully.
    func onSuccess(propertyId: Int) {
        print("Download Succesfully")
    }
    
    //Called periodically to indicate download progress of a property.
    func onProgress(propertyId: Int, percentage: Float) {
        print("Downloading property -- \(propertyId)... \(percentage)% ")
    }
    
}
//MARK: - Add MapClickListener Delegate
extension MainViewController: MapClickListener{
    
    func onMapClicked(event: MapstedMap.MapClickEvent) {
        
        if event.getClickType() == .SINGLE {
            if let clickedEntity = event.getClickEntity() {
                //Process tapped entity
                print("onMapClicked Selected Entity Name:-\(clickedEntity.name), EntityId:\(clickedEntity.entityId())")

            } else if let mapOverlay = event.getClickOverlay(), !mapOverlay.toolTipName.isEmpty {
                //Process if tapped on overlay
            }
            else if let markerName = event.getMarkerName(), let markerType = event.getMarkerType(), markerType == "TAG" {
                //Process If Tag marker is selected
            }
        }
        else {
           // Process when clicek on outside of building.
        }
    }
    
}
//MARK: - Add MapEvent delegate

extension MainViewController: MapEventListener {
    
    func onMapEvent(event: MapstedMap.MapEvent) {
        if event == .MAP_IDLE {
            //print("#Click - onMapEvent called - OnMapIdle")
        }
        else if event == .MAP_MOVED {
            //print("#Click - onMapEvent called - OnMapMoved")
        }
        else if event == .MAP_STABLE {
            //print("#Click - onMapEvent called - OnMapStable")
        }
        else if event == .MAP_INTERACTION {
            //print("#Click - onMapEvent called - OnMapInteraction")
        }
    }
    
}
//MARK: - Add MapSelectionChangeObserver delegate

extension MainViewController: MapSelectionChangeObserver {
   
    // When the selected property has changed
    func selectedPropertyChanged(toProperty propertyId: Int) {
        print("#selectedPropertyChanged event----\(propertyId)")
    }
    // When the selected building has changed
    func selectedBuildingChanged(toBuilding buildingId: Int) {
        print("#selectedBuildingChanged event----\(buildingId)")
    }
    //When the selected floor has changed
    func selectedFloorChanged(toBuilding buildingId: Int, toFloor floorId: Int) {
        print("#selectedFloorChanged event----\(buildingId)---Floor----\(floorId)")
    }
}
