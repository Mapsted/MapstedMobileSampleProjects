//
//  HomeViewController.swift
//  SampleMapUIPOISelection
//
//  Created by Mapsted on 2025-03-25.
//  Copyright © 2025 Mapsted. All rights reserved.
//

import UIKit
import MapstedCore
import MapstedMap

class HomeViewController : UIViewController {
    
    @IBOutlet weak var goToMapBtn: HighlightButton!
    @IBOutlet weak var goToEntityBtn: HighlightButton!

    //MARK: - View Lifecycle methods
    
	public override func viewDidLoad() {
		super.viewDidLoad()
        self.navigationItem.title = "Home"
        
        self.navigationController?.addShadow()
	}
	
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        goToEntityBtn.isSelected = false
        goToMapBtn.isSelected = false
    }
  
    override func viewWillDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
    }
    
    //MARK: - IBAction methods
   
    @IBAction func gotoEntityBtnAction(_ sender: Any) {
        goToEntityBtn.isSelected = true
        guard let mainViewController: MainViewController = self.storyboard?.instantiateViewController(identifier: "MainViewController") else {
            return
        }
        mainViewController.propertyId = 504
        mainViewController.buildingId = 504
        mainViewController.entityId = 75
        self.navigationController?.pushViewController(mainViewController, animated: true)
    }
    
    @IBAction func gotoMapBtnAction(_ sender: Any) {
        goToMapBtn.isSelected = true
        guard let mainViewController = self.storyboard?.instantiateViewController(identifier: "MainViewController") else {
            return
        }
        self.navigationController?.pushViewController(mainViewController, animated: true)
    }
}


