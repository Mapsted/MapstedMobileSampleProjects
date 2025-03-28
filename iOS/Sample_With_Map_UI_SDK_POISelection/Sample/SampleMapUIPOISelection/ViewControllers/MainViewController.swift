//
//  MainViewController.swift
//  SampleMapUIPOISelection
//
//  Created by Mapsted on 2025-03-25.
//  Copyright © 2025 Mapsted. All rights reserved.
//


import UIKit
import MapstedCore
import MapstedMap
import MapstedMapUi

class MainViewController : UIViewController {
    private var containerVC: ContainerViewController?
    private var mapsVC: MapstedMapUiViewController?
    var entityId: Int = -1
    var buildingId: Int = -1
    var propertyId: Int = 504 // Square One Shopping center
    
    @IBOutlet weak var spinnerView: UIActivityIndicatorView!
    
    //MARK: - Segue Handler
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let containerVC = segue.destination as? ContainerViewController, segue.identifier == "containerSegue" {
            self.containerVC = containerVC
        }
    }
    
    //MARK: - ViewController Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = "Map"
        self.navigationController?.addShadow()
        
        spinnerView.startAnimating()
        if CoreApi.hasInit() {
            self.addMapView()
        }
        else {
            MapstedMapApi.shared.setUp(prefetchProperties: false, callback: self)
        }
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        self.navigationController?.setNavigationBarHidden(false, animated: true)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        //self.unloadProperty(propertyId: self.propertyId)
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
    
    func displayProperty(propertyInfo: PropertyInfo, completion: (() -> ())? = nil) {
        self.mapsVC?.showLoadingSpinner(text: "Loading...")
        self.spinnerView.stopAnimating()
        
        mapsVC?.selectAndDrawProperty(propertyId: propertyId, callback: {[weak self] status in
            DispatchQueue.main.async {
                self?.mapsVC?.hideLoadingSpinner()
                if status {
                    self?.mapsVC?.displayPropertyOnMap {
                        completion?()
                    }
                }
                else {
                    print("Problem with status on select and draw", status)
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
                
                // After draw property on Map, Check for valid entityId and select on Map if it's valid
                self.displayPOI(propertyId: self.propertyId, buildingId: self.buildingId, entityId: self.entityId)
                
            }
        }
        else {
            print("No properties found", "")
        }
    }
    
    //MARK: - Display POI On Map
    
    //This method is used to display a POI/entity on map
    func displayPOI(propertyId: Int, buildingId: Int, entityId: Int) {
        if entityId > 0 {
            let mapDataType: MNDataType = buildingId > 0 ? .building : .property
            self.mapsVC?.displayOnMap(dataType: mapDataType, propertyId: propertyId, buildingId: buildingId, entityId: entityId, completion: {
                self.entityId = -1
                self.buildingId = -1
            })
        }
    }
}

//MARK: - Core Init Callback methods
extension MainViewController : CoreInitCallback {
    func onSuccess() {
        //Once the Map API Setup is complete, Setup the Mapview
        DispatchQueue.main.async {
            print("onSuccess", "")
            self.addMapView()
        }
    }
    
    func onFailure(errorCode: EnumSdkError) {
        print("Failed with", errorCode)
    }
    
    func onStatusUpdate(update: EnumSdkUpdate) {
        print("OnStatusUpdate", update)
    }
    
    func onStatusMessage(messageType: StatusMessageType) {
        
    }
}

extension MainViewController : PropertyActionCompleteListener{
    func completed(action: MapstedCore.PropertyAction, propertyId: Int, sucessfully: Bool, error: Error?) {
        print("Unload Property")
    }
}

