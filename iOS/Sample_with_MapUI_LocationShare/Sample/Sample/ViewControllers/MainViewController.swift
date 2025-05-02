//
//  MainViewController.swift
//  Demo
//
//  Created by Mapsted on 2019-09-12.
//  Copyright © 2019 Mapsted. All rights reserved.
//

import UIKit
import MapstedCore
import MapstedMap
import MapstedMapUi
import MapstedLocationShare

class MainViewController : UIViewController {
    
    @IBOutlet weak var spinnerView: UIActivityIndicatorView!
    
    var shareMyLocationBtn = UIButton(type: .system)
    private var containerVC: ContainerViewController?
    private var mapsVC: MapstedMapUiViewController?
        
    let Logger = DebugLog()
    var propertyInfo: PropertyInfo?
    
    //Use to Manage the location share state
    var isLocationShareEnable: Bool = false
    
    //MARK: - Segue Handler
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let containerVC = segue.destination as? ContainerViewController, segue.identifier == "containerSegue" {
            self.containerVC = containerVC
        }
    }
    
    //MARK: - ViewController Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        spinnerView.startAnimating()
        
        if CoreApi.hasInit() {
            handleSuccess()
        }
        else {
            MapstedMapApi.shared.setUp(prefetchProperties: false, callback: self)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    //MARK: - Intialize and add MapView and display property
    
    func addMapView() {

        if mapsVC == nil {
            if let mapsVC = MapstedMapUiViewController.shared as? MapstedMapUiViewController {
                self.mapsVC = mapsVC
                containerVC?.addController(controller: mapsVC, yOffset: 0, isNew: false)
            }
        }
    
        self.handleSuccess()
    }
    
    //Helper method to draw property.
    func displayProperty(propertyInfo: PropertyInfo, completion: (() -> ())? = nil) {
        //zoom to property
        self.mapsVC?.showLoadingSpinner(text: "Loading...")
        self.spinnerView.stopAnimating()
        
        let propertyId = propertyInfo.getPropertyId()
        
        //This method is used to draw property on the map.
        mapsVC?.selectAndDrawProperty(propertyId: propertyId, callback: {[weak self] status in
            DispatchQueue.main.async {
                self?.mapsVC?.hideLoadingSpinner()
                if status {
                    self?.mapsVC?.displayPropertyOnMap {
                        self?.addButtonOnTopOfMap()
                        completion?()
                    }
                }
                else {
                    self?.Logger.Log("Problem with status on select and draw", status)
                }
            }
        })
    }
    
    //Method to handle success scenario after SDK initialized
    fileprivate func handleSuccess() {
        let propertyInfos = CoreApi.PropertyManager.getAll()
        if propertyInfos.count > 0 {
            let firstProperty = propertyInfos[0]

            self.displayProperty(propertyInfo: firstProperty) {
                
                //Assigns the first property to propertyInfo and adds a listener for live location changes.
                self.propertyInfo = firstProperty
                
                //Add location share listener
                self.addLocationShareListener()
            }
        }
        else {
            self.Logger.Log("No properties found", "")
        }
    }
    
    
    func addLocationShareListener(){
        LocationShareApi.location.addShareLiveLocationChangeListener(listener: self)
    }
    
    //Toggles location sharing on/off and updates the button title accordingly.
    @IBAction func startAndStopLocationToggle(_ sender: Any) {
        if isLocationShareEnable {
            self.stopLocationSharing()
            self.shareMyLocationBtn.setTitle("Start Location Share", for: .normal)
        } else {
            self.startLocationSharing()
            self.shareMyLocationBtn.setTitle("Stop Location Share", for: .normal)
        }
        isLocationShareEnable = !isLocationShareEnable
    }
    
    deinit {
        LocationShareApi.location.removeShareLiveLocationChangeListener(listener: self)
    }
}

//MARK: - Location share/stop

extension MainViewController{
    
    //Add custom button on top of map view - Available in sdk version - 6.1.8
    func addButtonOnTopOfMap(){
        
        // Create the button
        shareMyLocationBtn.setTitle("Start Location Share", for: .normal)
        shareMyLocationBtn.backgroundColor = .darkGray
        shareMyLocationBtn.setTitleColor(.white, for: .normal)
        shareMyLocationBtn.layer.cornerRadius = 8
        
        // Disable autoresizing mask to use Auto Layout
        shareMyLocationBtn.translatesAutoresizingMaskIntoConstraints = false
        
        // Add the button to the view
        mapsVC?.addCustomViewOnMap(shareMyLocationBtn)
        
        // Add constraints
        NSLayoutConstraint.activate([
            shareMyLocationBtn.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            shareMyLocationBtn.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 40),
            shareMyLocationBtn.widthAnchor.constraint(equalToConstant: 190),
            shareMyLocationBtn.heightAnchor.constraint(equalToConstant: 45)
        ])
        
        // Add target for tap event
        shareMyLocationBtn.addTarget(self, action: #selector(startAndStopLocationToggle), for: .touchUpInside)
        
     // Remove customview using below function
     //self.mapsVC?.removeAddedCustomViewFromMap(self.shareMyLocationBtn)
    }

    //Starts sharing the location updates for the specified property, and opens the share view with a URL if the location sharing is successful.
    @objc private func startLocationSharing() {
        
        guard let position = CoreApi.LocationManager.getPosition() else {
            return
        }
        LocationShareApi.location.startLocationShareUpdates(propertyId: self.propertyInfo?.propertyId ?? 0, position: position) { [weak self] response in
            DispatchQueue.main.async {
                if let res = response, res.success, let url = res.url {
                    self?.openShareView(textToShare: url)
                }
            }
        }
    }
    
    //Stops the location sharing updates.
    func stopLocationSharing() {
        if LocationShareApi.location.isShareLiveLocationEnabled() {
            // Update the data source immediately
            LocationShareApi.location.stopLocationShareUpdates()
        }
    }
    
    //Presents a share view to allow sharing
    func openShareView(textToShare: String) {
        let activityViewController : UIActivityViewController = UIActivityViewController(
            activityItems: [textToShare], applicationActivities: nil)
        // Anything you want to exclude
        activityViewController.excludedActivityTypes = [
            UIActivity.ActivityType.assignToContact,
            UIActivity.ActivityType.saveToCameraRoll,
            UIActivity.ActivityType.openInIBooks,
            UIActivity.ActivityType.airDrop
        ]
        
        self.present(activityViewController, animated: true)
    }
}

//MARK: - Core Init Callback methods
extension MainViewController : CoreInitCallback {
    
    //Called when the Map API setup is successful, initializes and displays the map view.
    func onSuccess() {
        //Once the Map API Setup is complete, Setup the Mapview
        DispatchQueue.main.async {
            self.Logger.Log("onSuccess", "")
            self.addMapView()
        }
    }
    
    //Called when the Map API setup fails
    func onFailure(errorCode: EnumSdkError) {
        self.Logger.Log("Failed with", errorCode)
    }
    
    //Called when there is a status update from the SDK
    func onStatusUpdate(update: EnumSdkUpdate) {
        self.Logger.Log("OnStatusUpdate", update)
    }
    
    //Called when a status message is received from the SDK
    func onStatusMessage(messageType: StatusMessageType) {
        
    }
}

//MARK: - Location Share Callback methods
extension MainViewController: ShareLiveLocationListener{
    
    //Triggered when live location sharing is enabled or disabled.
    func onShareLiveLocationEnabled(enabled: Bool) {
        print("Location share----",enabled)
    }    
}

