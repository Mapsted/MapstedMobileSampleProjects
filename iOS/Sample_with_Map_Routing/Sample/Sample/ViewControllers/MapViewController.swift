	//
	//  MapViewController.swift
	//  Sample
	//
	//  Created by Daniel on 2021-12-09.
	//  Copyright © 2021 Mapsted. All rights reserved.
	//

import UIKit
import MapstedCore
import MapstedMap

class MapViewController : UIViewController {
	
	@IBOutlet weak var spinnerView: UIActivityIndicatorView!
	@IBOutlet weak var mapPlaceholderView: UIView!
    
    @IBOutlet weak var btnRequestRoute: UIButton!
    @IBOutlet weak var navigationButton: UIButton!
	
		//View controller in charge of map view
	private let mapViewController = MNMapViewController()
    
    var isNavigationStarted: Bool = false
    let propertyId = 504
    var instructionString: String = ""
    
    //MARK: - View Lifecycle methods
	
	public override func viewDidLoad() {
		super.viewDidLoad()
        showSpinner()
        if CoreApi.hasInit() {
            self.onSuccess()
        }
        else {
            MapstedMapApi.shared.setUp(prefetchProperties: false, callback: self)
        }
        self.navigationButton.isEnabled = false
	}
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.addListener()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        self.removeListener()
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
	
		//Method to do UI setup
	func setupUI() {
			//Whether or not you want to show compass
		MapstedMapMeta.showCompass = true
		
			//UI Stuff
		addChild(mapViewController)
		mapViewController.view.translatesAutoresizingMaskIntoConstraints = false
		mapPlaceholderView.addSubview(mapViewController.view)
		addParentsConstraints(view: mapViewController.view)
		mapViewController.didMove(toParent: self)
        
        //Added handleSuccess once MapView is ready to avoid any plotting issues.
        
        self.startDownload(propertyId: propertyId)
	}
    
    func startDownload(propertyId: Int) {
        CoreApi.PropertyManager.startDownload(propertyId: propertyId, propertyDownloadListener: self)
    }
	
    //MARK: - Download Property and Draw Property on Success
		//Handler for initialization notification
	fileprivate func handleSuccess() {
        
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
			MapstedMapApi.shared.drawProperty(isSelected: true, propertyData: propertyData)
			if let propertyInfo = PropertyInfo(propertyId: propertyId) {
				MapstedMapApi.shared.mapView()?.moveToLocation(mercator: propertyInfo.getCentroid(), zoom: 18, duration: 0.2)
                completion();
			}
			self.hideSpinner()
		}
	}
    
    //MARK: - Button Actions
    
    @IBAction func requestRouteClicked(_ sender: Any) {
        self.makeRouteRequests(propertyId: propertyId)
        self.navigationButton.isEnabled = true
        self.navigationButton.backgroundColor = .systemBlue
    }
    
    @IBAction func StartOrStopNavigationClicked(_ sender: Any) {
        if isNavigationStarted == false {
            self.startNavigation()
            self.navigationButton.setTitle("Stop Navigation", for: .normal)
            self.navigationButton.backgroundColor = .systemRed
            self.navigationButton.isEnabled = true
        }
        else {
            self.navigationButton.setTitle("Start Navigation", for: .normal)
            self.stopNavigation()
            self.isNavigationStarted = false
            self.navigationButton.backgroundColor = .systemBlue
            self.navigationButton.isEnabled = false
        }
    }
    
    //MARK: - Routing Request methods
    
    fileprivate func makeRouteRequests(propertyId: Int) {
        let entities = CoreApi.PropertyManager.findEntityByName(name: "Appl", propertyId: propertyId)
        let otherEntities = CoreApi.PropertyManager.getSearchEntities(propertyId: propertyId)
        if let firstMatch = entities.first,
            let randomDestination1 = otherEntities.randomElement(),
            let randomDestination2 = otherEntities.randomElement(){
            self.makeRouteRequest(start: firstMatch, fromCurrentLocation: true, destinations: [randomDestination1, randomDestination2])
        }
    }
    
    //Builds and sends a route request with given start point, destinations, and route preferences.
    fileprivate func makeRouteRequest(start: ISearchable?, fromCurrentLocation: Bool, destinations: [ISearchable]) {
        let routingOptions = MNRoutingOptionsBuilder().setIncludeEscalators(true)
                                                      .setIncludeStairs(false)
                                                      .setIncludeRamps(true)
                                                      .setIncludeElevators(true)
                                                      .setItineraryOptimization(true)
                                                      .build()
        let start = start as? MNSearchEntity
        let pois = destinations.compactMap({$0 as? MNSearchEntity})
        
        //Build a route request
        let routeRequest = MNRouteRequest(propertyId: propertyId, startWaypoint: fromCurrentLocation ? nil : start, destinationWaypoints: pois, routingOptions: routingOptions, routingConstraints: MNRoutingConstraints.emptyInstance(), isFromCurrentLocation: fromCurrentLocation)
        DispatchQueue.global(qos: .userInteractive).async {
            CoreApi.RoutingManager.requestRoute(request: routeRequest, routingRequestCallback: self)
        }
    }
    
    //MARK: Start/Stop Navigation methods
    
    func startNavigation() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            MapstedMapApi.shared.startNavigation(fromPosition: nil)
            CoreApi.RoutingManager.addNavigationCallbackListener(listener: self)
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                MapstedMapApi.shared.drawCurrent(switchFloor: true)
            }
        }
    }
    
    func stopNavigation() {
        MapstedMapApi.shared.stopNavigation(finalPosition: nil)
    }
    
    //MARK: - Routing And Navigation Listener methods
    
    func addListener() {
        CoreApi.RoutingManager.addRoutingCallbackListener(listener: self)
        CoreApi.RoutingManager.addNavigationCallbackListener(listener: self)
    }
    
    func removeListener() {
        CoreApi.RoutingManager.removeRoutingCallbackListener(listener: self)
        CoreApi.RoutingManager.removeNavigationCallbackListener(listener: self)
    }
}

//MARK: - Routing Request Callback Listener methods

extension MapViewController : RoutingRequestCallback {
    func onSuccess(routeResponse: MNRouteResponse) {
        if let routeError = routeResponse.routeError {
            print("#RouteRequestDelegate: onSuccess - \(routeError.isSuccessful)")
        }
    }
    
    func onError(routeResponse: MNRouteResponse) {
        if let routeError = routeResponse.routeError {
            print("#RouteRequestDelegate: onError - \(String(describing: routeError.errorMessage))")
        }
    }
}

//MARK: - Navigation Callback Listener methods

extension MapViewController: NavigationCallback {
    func onNavigationInitSuccess(state: MNNavigationState) {
        print("#NavDelegate: Success - \(String(describing: state.description()))")
        isNavigationStarted = true
    }
    
    func onNavigationInitFailure(error: MNNavigationError) {
        print("#NavDelegate: Failure - \(error.description())")
    }
    
    func onRouteSegmentReached(currentSegment: MNRoutePathSegment, visitedSegments: [MNRoutePathSegment], upcomingSegments: [MNRoutePathSegment]) {
        print("#NavDelegate: SegmentReached - \(currentSegment.floorLabel)")
    }
    
    func onUserProgressAlongRoute(state: MNNavigationState) {
        
//        print("#NavDelegateInstruction - state.isActive: \(state.isActive) - state.userPoint: \(state.userPoint?.loc.x),\(state.userPoint?.loc.y), \(state.userPoint?.loc.z) - state.projectedUserPoint: \(state.projectedUserPoint?.x), \(state.projectedUserPoint?.y), \(state.projectedUserPoint?.z)")
        
        //state.propertyId - The propertyId for the currently active navigation
        //state.buildingId - The buildingId for the currently active navigation
        //state.isActive - The boolean to check whether the navigation is currently active.
        //state.curRoutePath - The current route path for the currently active navigation.
        //state.nextInstruction - The next instruction for the currently active navigation.
        //state.afterNextInstruction - The next to next instruction for the currently active navigation.
        //state.userPoint - The location of the user for the currently active navigation.
        //state.isFirstSegment - The boolean to check whether this is first segment for the currently active navigation.
        //state.isIntermediarySegment - The boolean to check whether this is intermediary segment for the currently active navigation.
        //state.isLastSegment - The boolean to check whether this is last segment for the currently active navigation.
        //state.distTimeToNextInstruction - The distance and time to next instruction for the currently active navigation.
        //state.distTimeToDestination - The distance and time to destination for the currently active navigation.
        
        if instructionString == "" {
            if let nextInst = state.nextInstruction, let nextInstText = nextInst.getInstruction().text {
                instructionString = nextInstText
            }
        }
        let instructionForDisplay =  instructionString + " in " + "\(state.distTimeToNextInstruction.distanceMeters) mtrs, \(state.distTimeToNextInstruction.timeMinutes) mins"
        print("#NavDelegateInstruction: RouteInstruction - ProgressAlongRoute - instruction: \(instructionForDisplay)")
    }
    
    func onRouteInstruction(currentInstruction: MNRoutePointInstruction, nextInstruction: MNRoutePointInstruction, afterNextInstruction: MNRoutePointInstruction) {
        guard let instructionText = nextInstruction.getInstruction().text else {
            instructionString = ""
            return
        }
        instructionString = instructionText
        if let distanceTime = currentInstruction.toNextInstructionDistanceTime {
            let instructionForDisplay =  instructionString + " in " + "\(distanceTime.distanceMeters) mtrs, \(distanceTime.timeMinutes) mins"
            print("#NavDelegateInstruction: RouteInstruction - instruction: \(instructionForDisplay)")
        }
        else {
            let instructionForDisplay =  instructionString
            print("#NavDelegateInstruction: RouteInstruction - instruction: \(instructionForDisplay)")
        }
    }
    
    func onRouteRecalculation(state: MNNavigationState, newRoute: MNRoute, routePathType: MNRoutePathType) {
        print("#NavDelegate: RouteRecalculation - state: \(String(describing: state.description())) - newRoute: \(String(describing: newRoute.optimalRoute)) - afterNextIroutePathType: \(routePathType)")
    }
    
    func onDestinationReached(destination: MapstedWaypoint) {
        print("#NavDelegate: DestinationReached - destination: \(destination.getName())")
    }
}

//MARK: - UI Constraints Helper method

extension MapViewController {
		//Helper method
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
        self.drawProperty(propertyId: propertyId, completion: {
        })
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

//MARK: - Map Event Listener

extension MapViewController: MapClickListener {
    func onMapClicked(event: MapstedMap.MapClickEvent) {
        if event.getClickType() == .SINGLE {
            if let clickedEntity = event.getClickEntity() {
                //Process tapped entity
            } else if let mapOverlay = event.getClickOverlay(), !mapOverlay.toolTipName.isEmpty {
                //Process tapped overlay
            }
        } else {
            //Handle other click types (e.g.,long-press, outside map area,tap on walk way)
        }
    }
}
