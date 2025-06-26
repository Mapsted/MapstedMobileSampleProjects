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
import MapstedComponentsUI

class MainViewController : UIViewController {
    
    private var containerVC: ContainerViewController?
    private var mapsVC: MapstedMapUiViewController?
	
	let Logger = DebugLog()
    
    @IBOutlet weak var spinnerView: UIActivityIndicatorView!
    @IBOutlet weak var btnAddIcon: UIButton!
    @IBOutlet weak var btnRemoveIcon: UIButton!
    @IBOutlet weak var viewAddIcons: UIView!
    
    var customPinEntities: [ISearchable] = []
    var arrCustomIcons: [CustomIcon] = []
    var propertyId: Int = 0
    
    
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

        self.mapsVC?.showLoadingSpinner(text: "Loading...")
        self.spinnerView.stopAnimating()
        
		let propertyId = propertyInfo.getPropertyId()
        mapsVC?.selectAndDrawProperty(propertyId: propertyId, callback: {[weak self] status in
            DispatchQueue.main.async {
                self?.mapsVC?.hideLoadingSpinner()
                if status {
                    self?.mapsVC?.displayPropertyOnMap {
                        completion?()
                    }
                }
                else {
					print("Problem with status on select and draw - status: \(status)")
                }
            }
        })
    }
    
    //Method to handle success scenario after SDK initialized
    fileprivate func handleSuccess() {
        let propertyInfos = CoreApi.PropertyManager.getAll()
        
        guard let propertyInfo = propertyInfos.first else {
            print("No properties found in the licence")
            return
        }
        
        self.displayProperty(propertyInfo: propertyInfo) {
            self.propertyId = propertyInfo.propertyId
            self.findAllEntities(propertyId: self.propertyId)
        }
    }
    
    //MARK: - Add & Remove Icon Button Click Handler
    
    @IBAction func addIconsClicked(_ sender: Any) {
        
        self.createListOfCustomIcons()
        MapstedMapApi.shared.addIcons(propertyId: self.propertyId, icons: arrCustomIcons)
    }
    
    @IBAction func removeIconsClicked(_ sender: Any) {
        if arrCustomIcons.count > 0 {
            
            MapstedMapApi.shared.removeIcons(icons: arrCustomIcons)
            arrCustomIcons.removeAll()
        }
    }
    
    //MARK: - Configure Custom Icons
    
    func createListOfCustomIcons() {
        //Create list of custom icons from arry of entities.
        for entity in customPinEntities {
            guard let searchEntity = entity as? MNSearchEntity, let entityLocation = searchEntity.entityLocation else { continue }
            let icon = createIconFrom(mercatorZone: entityLocation)
            arrCustomIcons.append(icon)
        }
    }
    
    //Builds and returns a CustomIcon using the given MercatorZone with preset styling
    func createIconFrom(mercatorZone: MNMercatorZone) -> CustomIcon {
        let iconBuilder = CustomIcon.Builder().setMercatorZone(mercatorZone: mercatorZone)
                                              .setPreferredSize(30)
                                              .setIcon(icon: UIImage(named: "offerPin"))
                                              .setAnchorPointX(anchorPointX: 0)
                                              .setAnchorPointY(anchorPointY: -1)
        let icon = iconBuilder.build()
        return icon
    }
    
    //MARK: - Utility Methods
    
    //How to search for entities by name from CoreApi
    fileprivate func findAllEntities(propertyId: Int) {
        let allEntities = CoreApi.PropertyManager.getSearchEntities(propertyId: propertyId)
		print("Getting all search entities in \(propertyId)")
        
        customPinEntities = allEntities.filter({ [75, 90, 407, 398].contains($0.entityId) })
    }
}

//MARK: - Core Init Callback methods
extension MainViewController : CoreInitCallback {
    
    //Called when the Map API setup is successful, initializes and displays the map view.
    func onSuccess() {
        DispatchQueue.main.async {
			print("onSuccess")
            self.viewAddIcons.isHidden = false
            self.addMapView()
        }
    }
    
    //Called when the Map API setup fails
    func onFailure(errorCode: EnumSdkError) {
        print("Failed with \(errorCode)")
    }
    
    //Called when there is a status update from the SDK
    func onStatusUpdate(update: EnumSdkUpdate) {
        print("OnStatusUpdate: \(update)")
    }
    
    //Called when a status message is received from the SDK
    func onStatusMessage(messageType: StatusMessageType) {
        
    }
}
