//
//  MapViewController.swift
//  Sample
//
//  Created by Mapsted on 2025-03-21.
//  Copyright © 2025 Mapsted. All rights reserved.
//

import UIKit
import MapstedCore
import MapstedMap

class MapViewController : UIViewController {
    
    @IBOutlet weak var spinnerView: UIActivityIndicatorView!
    @IBOutlet weak var mapPlaceholderView: UIView!
    @IBOutlet weak var overlayBtn: UIButton!

    //View controller in charge of map view
    private let mapViewController = MNMapViewController()
    let propertyId = 504
    let overylayItemId = "6865" // Add overlay id that you want show on map.
    var propertyInfo: PropertyInfo?
    
    //MARK: - View Lifecycle
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Enable map overlay and set custom pin marker
        MapstedMapApi.shared.useMapOverlays(enable: true)
        MapstedMapMeta.pinImageName = "new_pin"
        
        // Whether or not you want to show compass
        MapstedMapMeta.showCompass = true
        
        // To verify overlayItem, set isHidden to false and update the overlayItemId parameter.
        overlayBtn.isHidden = true
        
        showSpinner()
        if CoreApi.hasInit() {
            self.handleSuccess()
        }
        else {
            MapstedMapApi.shared.setUp(prefetchProperties: false, callback: self)
        }
    }
    
    //MARK: - Show & Hide Spinner
    
    //Start progress indicator
    func showSpinner() {
        DispatchQueue.main.async {
            self.spinnerView?.startAnimating()
        }
    }
    
    //Stop progress indicator
    func hideSpinner() {
        DispatchQueue.main.async {
            self.spinnerView?.stopAnimating()
        }
    }
    
    //MARK: - Setup UI
    
    //Setup map view and start download
    func setupUI() {
        addChild(mapViewController)
        mapViewController.view.translatesAutoresizingMaskIntoConstraints = false
        mapPlaceholderView.addSubview(mapViewController.view)
        addParentsConstraints(view: mapViewController.view)
        mapViewController.didMove(toParent: self)
        
        self.startDownload(propertyId: propertyId)
    }
    
    //MARK: - Download Property and Draw Property on Success
    
    func startDownload(propertyId: Int) {
        CoreApi.PropertyManager.startDownload(propertyId: propertyId, propertyDownloadListener: self)
    }
    
    //Handler for initialization notification
    func handleSuccess() {
        DispatchQueue.main.async {
            self.setupUI()
        }
    }
    
    //Helper method to draw property.
    func drawProperty(propertyId: Int, completion: @escaping (() -> Void)) {
        guard let propertyData = CoreApi.PropertyManager.getCached(propertyId: propertyId) else {
            print("No property Data")
            self.hideSpinner()
            return
        }
        DispatchQueue.main.async {
            //This method is used to draw property on the map.
            MapstedMapApi.shared.drawProperty(isSelected: true, propertyData: propertyData)
            if let propertyInfo = PropertyInfo(propertyId: propertyId) {
                MapstedMapApi.shared.mapView()?.moveToLocation(mercator: propertyInfo.getCentroid(), zoom: 17, duration: 0.2)
                self.propertyInfo = propertyInfo
                completion();
            }
            self.hideSpinner()
        }
    }
    
    //MARK: - Entity selection & Overlay selection
    
    @IBAction func selectEntity(_ sender: Any) {
        self.selectEntityById(propertyId: propertyId, buildingId: 504, entityId: 75)
    }
    
    @IBAction func selectOverlay(_ sender: Any) {
        let overlayId: String = overylayItemId
        self.selectOverlayById(overlayId: overlayId)
    }
    
    //Method to programmatically select entity on map and show the custom popup view. In case of the property level entity, which is not on any building, please pass the buildingId as -1. For entity on any building, please pass the valid buildingId.
    func selectEntityById(propertyId: Int, buildingId: Int, entityId: Int) {
        let mapDataType: MNDataType = buildingId > 0 ? .building : .property
        
        //Fetch entity using propertyId, buildingId and entityId.
        if let mapEntity = MNMapEntity(dataType: mapDataType, propertyId: propertyId, buildingId: buildingId, entityId: entityId) {
            MapstedMapApi.shared.selectSearchEntity(entity: mapEntity, showPopup: false)
            
            //Show custom view for entity
            let infoModel = DetailsInfoModel(type:.entity, title: mapEntity.name, website: mapEntity.entityInfo?.website, phone: mapEntity.entityInfo?.phoneNumber)
            self.showCustomView(infoModel: infoModel)
        }
    }
    
    //Method to programmatically select overlay on map and show the custom popup view.
    func selectOverlayById(overlayId: String) {
        
        let mapOverlayItem = MapstedMapApi.shared.getMapOverlayItem(byOverlayId: overlayId)
        if let mapOverlay = mapOverlayItem {
            MapstedMapApi.shared.selectMapOverlayItemOnMap(mapOverlay: mapOverlay)
            
            //Show custom view for map overlay
            let infoModel = DetailsInfoModel(type:.mapOverlay, title: mapOverlay.displayName, website: "", phone: "")
            self.showCustomView(infoModel: infoModel)
        } else {
            let alert = UIAlertController(title: "", message: "Overlay not found. Please provide a valid overlay ID", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default))
            self.present(alert, animated: true)
        }
    }
    
    //Method to programmatically select overlay on map and show the custom popup view.
    func showCustomView(infoModel: DetailsInfoModel) {
        let storyboard = UIStoryboard(name: "MapView", bundle: nil) // Use your actual storyboard name
        guard let childVC = storyboard.instantiateViewController(withIdentifier: "ChildViewController") as? ChildViewController else {
            return
        }
        addChild(childVC)
        childVC.view.frame = CGRect(x: 0, y: view.frame.height - 150, width: view.frame.width, height: 100) // Set frame
        childVC.view.layer.cornerRadius = 8.0
        childVC.didMove(toParent: self)
        view.addSubview(childVC.view)
        childVC.infoModel = infoModel
        view.bringSubviewToFront(childVC.view)
        childVC.setData()
    }
    
    //MARK: - Utility Methods to fetch SearchEntity by Id or Name
    
    //Method to fetch the search entity by entityId for a given property.
    func findSearchEntityById(propertyId: Int, buildingId: Int, entityId: Int) -> MNSearchEntity? {
        let entity = CoreApi.PropertyManager.getEntity(propertyId: propertyId, buildingId: buildingId, entityId: entityId)
        return entity
    }
    
    //Method to fetch the search entities by name for a given property.
    func findEntityByName(name: String, propertyId: Int) -> [ISearchable] {
        let matchedEntities = CoreApi.PropertyManager.findEntityByName(name: name, propertyId: propertyId)
        for match in matchedEntities {
            print("Results of findEntityByName for name: \(name) ---> entityName \(match.displayName) -- Other Details: \(match.propertyId), \(match.buildingId), \(match.entityId)")
        }
        return matchedEntities
    }
}

//MARK: - UI Constraints Helper method

extension MapViewController {
    
    func addParentsConstraints(view: UIView?) {
        guard let superview = view?.superview else {
            return
        }
        
        guard let view = view else {return}
        
        view.translatesAutoresizingMaskIntoConstraints = false
        
        let viewDict: [String: Any] = Dictionary(dictionaryLiteral: ("self", view))
        let horizontalLayout = NSLayoutConstraint.constraints(
            withVisualFormat: "|[self]|", options: NSLayoutConstraint.FormatOptions.directionLeadingToTrailing, metrics: nil, views: viewDict)
        let verticalLayout = NSLayoutConstraint.constraints(
            withVisualFormat: "V:|[self]|", options: NSLayoutConstraint.FormatOptions.directionLeadingToTrailing, metrics: nil, views: viewDict)
        superview.addConstraints(horizontalLayout)
        superview.addConstraints(verticalLayout)
    }
}

//MARK: - Core Init Callback methods

extension MapViewController : CoreInitCallback {
    
    func onSuccess() {
        //Once the Map API Setup is complete, Setup the Mapview
        self.handleSuccess()
    }
    
    func onFailure(errorCode: EnumSdkError) {
        print("Failed with \(errorCode)")
    }
    
    func onStatusUpdate(update: EnumSdkUpdate) {
        print("OnStatusUpdate: \(update)")
    }
    
    func onStatusMessage(messageType: StatusMessageType) {
        //Handle message
    }
}

//MARK: - Property Download Listener Callback methods

extension MapViewController : PropertyDownloadListener {
    
    func onSuccess(propertyId: Int) {
        self.drawProperty(propertyId: propertyId, completion: {})
    }
    
    func onSuccess(propertyId: Int, buildingId: Int) {
        print("Successfully downloaded \(propertyId) - \(buildingId)")
    }
    
    func onFailureWithProperty(propertyId: Int) {
        print("Failed to download \(propertyId)")
    }
    
    func onFailureWithBuilding(propertyId: Int, buildingId: Int) {
        print("Failed to download \(propertyId) - \(buildingId)")
    }
    
    func onProgress(propertyId: Int, percentage: Float) {
        print("Downloaded \(percentage * 100)% of \(propertyId)")
    }
}

//MARK: - Map Event Listener

extension MapViewController: MapEventListener {
    
    func onMapEvent(event: MapstedMap.MapEvent) {
        if event == .MAP_IDLE {
            //Handle map idle
        } else if event == .MAP_MOVED {
            //Handle map moved
        } else if event == .MAP_STABLE {
            //Handle map stable
        } else if event == .MAP_INTERACTION {
            //Handle map stable
        }
    }
}

//MARK: - Map Click Listener

extension MapViewController: MapClickListener {
    //This method is map click listener delegate which gives click even when something is clicked on map, be it map overlay, be it entity or it is tapped outside.
    func onMapClicked(event: MapstedMap.MapClickEvent) {
        if event.getClickType() == .SINGLE {
            if let clickedEntity = event.getClickEntity() {
                
                MapstedMapApi.shared.selectSearchEntity(entity: clickedEntity, showPopup: false)
                
                //Show custom popup for entity
                let infoModel = DetailsInfoModel(type:.entity ,title: clickedEntity.name,website: clickedEntity.entityInfo?.website,phone: clickedEntity.entityInfo?.phoneNumber)
                self.showCustomView(infoModel: infoModel)
            }
            else if let mapOverlay = event.getClickOverlay(), !mapOverlay.toolTipName.isEmpty {
                
                MapstedMapApi.shared.selectMapOverlayItemOnMap(mapOverlay: mapOverlay)
                
                //Show custom popup for map overlay
                let infoModel = DetailsInfoModel(type:.mapOverlay, title: mapOverlay.displayName, website:"", phone: "")
                self.showCustomView(infoModel: infoModel)
                
            }
        } else {
            //Handle other click types (e.g.,long-press, outside map area,tap on walk way)
        }
    }
}
