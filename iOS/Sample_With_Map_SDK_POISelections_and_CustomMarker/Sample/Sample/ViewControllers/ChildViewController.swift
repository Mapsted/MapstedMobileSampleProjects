//
//  ChildViewController.swift
//  Sample
//
//  Created by Mapsted on 2025-03-21.
//  Copyright © 2025 Mapsted. All rights reserved.
//
import UIKit
import MapstedCore

class ChildViewController : UIViewController {
    
    @IBOutlet weak var entityName: UILabel!
    
    var infoModel: DetailsInfoModel? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    public func setData() {
        self.entityName.text = (infoModel?.type?.rawValue ?? "") + ": " + (infoModel?.title ?? "")
    }
}
