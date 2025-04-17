//
//  MainViewController.swift
//  Demo
//
//  Created by Mapsted on 2019-09-12.
//  Copyright © 2019 Mapsted. All rights reserved.
//

import UIKit
import MapstedCore
import LocationMarketing
import MapstedTriggersCore
import SafariServices

class MainViewController : UIViewController {
   
    @IBOutlet weak var tblView: UITableView!
    @IBOutlet weak var lblNoData: UILabel!
    @IBOutlet weak var spinnerView: UIActivityIndicatorView!
    
    private var nearbyPropertyIds = Set<Int>()
    var propertyList = [PropertyInfo]()
   
    // Creates a shared instance of LocMarketingApi for location-based marketing operations.
    let locMarketingManager = LocMarketingApi.shared

 
    //MARK: - ViewController Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = "Property"
        self.spinnerView.startAnimating()
        
        if CoreApi.hasInit() {
            print("init success")
        }
        else {
            CoreApi.initialize(callback: self)
        }

        self.setUpXIB()
    }
    
    //Setup UI
    func setUpXIB(){
        tblView.register(UINib(nibName: "PropertyListItemCell", bundle: nil), forCellReuseIdentifier: "PropertyListItemCell")
        tblView.delegate = self
        tblView.dataSource = self
    }
    
    //Get property info using property Id
    func getPropertyInfoById(){
        
        propertyList = propertyList.filter { nearbyPropertyIds.contains($0.getPropertyId()) }

        for id in nearbyPropertyIds {
            
            if propertyList.contains(where: { $0.getPropertyId() == id }) {
              continue // Skip this ID as it's already in the list
            }
            
            // Fetch property info for the ID
            guard let propertyInfo = CoreApi.PropertyManager.getInfo(propertyId: id) else {
                print("Did not get info for id: \(id)")
                continue // keep trying other IDs
            }
            propertyList.append(propertyInfo)
        }
        
        DispatchQueue.main.async {
            self.setNoDataView()
            self.tblView.reloadData()
        }
    }
    
    func setNoDataView(){
        self.lblNoData.isHidden = self.propertyList.count == 0 ? false : true
        self.tblView.isHidden = self.propertyList.count == 0 ? true : false
    }
    
    //Add near by property listener
    func setUpLocationListeners() {
        CoreApi.LocationManager.addNearbyPropertiesListener(listener: self)
    }
    
    //Initialize the SDK - LocationMarketing, using this it will automatically manages the in-App notification for event and news for near by property.
    func initializeLocationMarketing(){
        
        locMarketingManager.setListener(listener: self)

        LocMarketingApi.initialize() { success, error in
            if let _ = error {
                print("Error on initilized Location marketing: \(String(describing: error?.localizedDescription))") }
        }
    }
    
}
extension MainViewController:UITableViewDataSource,UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return propertyList.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "PropertyListItemCell", for: indexPath) as? PropertyListItemCell
        let propertyObject = propertyList[indexPath.row]
        cell?.lblPropertyName.text = propertyObject.displayName
        cell?.lblPropertyAddress.text = propertyObject.getFullAddress()
        
        let imgId = propertyObject.getCoverImages().compactMap { $0 }.first ?? ""
        let baseUrl = propertyObject.imageBaseUrlString ?? ""
        cell?.fetchImage(imageBaseUrl: baseUrl, imageUid: imgId)
        
        return cell ?? UITableViewCell()
    }
    
}
extension MainViewController : CoreInitCallback {
    
    //Called when the Map API setup is successful, initializes and displays the map view.
    func onSuccess() {
        
        self.spinnerView.stopAnimating()
        self.setNoDataView()
        
        self.setUpLocationListeners()

        self.initializeLocationMarketing()

    }
    
    //Called when the Map API setup fails
    func onFailure(errorCode: EnumSdkError) {
    }
    
    //Called when there is a status update from the SDK
    func onStatusUpdate(update: EnumSdkUpdate) {
    }
    
    //Called when a status message is received from the SDK
    func onStatusMessage(messageType: StatusMessageType) {
        
    }
}

//MARK: - Near By property Delegates

extension MainViewController: NearbyPropertiesListener {

    //Adds the given property IDs to the nearby list and fetches their details.
    func addNearbyProperties(propertyIds: Set<Int>) {
        nearbyPropertyIds.formUnion(propertyIds)
        self.getPropertyInfoById()
    }
    
    //Removes the given property IDs from the nearby list and updates the property details.
    func removeNearbyProperties(propertyIds: Set<Int>) {
        nearbyPropertyIds.subtract(propertyIds)
        self.getPropertyInfoById()
    }
}
//MARK: - Notification Delegate
extension MainViewController: MapstedNotificationDelegate,LocMarketingListener {
    
    //It will trigger when notification contains website
    func openWebsite(websiteURL: String) -> Bool {
        openURLInSafariVC(from: self, urlString:websiteURL)
        return true
    }
    
    //It will trigger when notification contains Entity
    func navigateToMap(homeEntities: [MapstedCore.HomeEntity]) {
        print("Naviagate to map",homeEntities.first?.mapEntity?.name ?? "")
    }
    
    //It will trigger when notification dismiss
    func dismiss() {
        print("Dismiss Notification")
    }
    
    //Opens the given URL in a Safari browser view
    func openURLInSafariVC(from viewController: UIViewController, urlString: String) {
        guard let url = URL(string: urlString) else {
            print("Invalid URL string: \(urlString)")
            return
        }

        let safariVC = SFSafariViewController(url: url)
        safariVC.preferredControlTintColor = .systemBlue // Optional: change button color
        viewController.present(safariVC, animated: true, completion: nil)
    }

}
