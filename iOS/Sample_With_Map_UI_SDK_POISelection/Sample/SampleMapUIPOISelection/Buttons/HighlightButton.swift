//
//  HighlightButton.swift
//  SampleMapUIPOISelection
//
//  Created by admin on 25/03/25.
//  Copyright © 2025 Mapsted. All rights reserved.
//

import Foundation
import UIKit

class HighlightButton: UIButton {
    override var isSelected: Bool {
        didSet {
            layer.borderColor = isSelected ? UIColor.systemFill.cgColor : UIColor.clear.cgColor
            layer.shadowOpacity = isSelected ? 0.5 : 0.0
            layer.borderWidth = isSelected ? 2 : 0.1
        }
    }
}
