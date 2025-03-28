//
//  UINavigationController+Shadow.swift
//  SampleMapUIPOISelection
//
//  Created by Parth Bhatt on 25/03/25.
//  Copyright © 2025 Mapsted. All rights reserved.
//

import Foundation
import UIKit

extension UINavigationController {
    
    func addShadow() {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = .white // Set your desired color
        appearance.shadowColor = UIColor.black.withAlphaComponent(0.3) // Soft shadow
        
        self.navigationBar.standardAppearance = appearance
        self.navigationBar.scrollEdgeAppearance = appearance
    }
}
