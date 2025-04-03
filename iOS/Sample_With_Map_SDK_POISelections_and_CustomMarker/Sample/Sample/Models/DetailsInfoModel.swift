//
//  DetailsInfoModel.swift
//  Sample
//
//  Created by Mapsted on 2025-03-21.
//  Copyright © 2025 Mapsted. All rights reserved.
//

import Foundation

public enum ObjectType: String {
    case entity = "Entity Name"
    case mapOverlay = "Overlay Name"
}

struct DetailsInfoModel {
    var type: ObjectType?
    var title: String = ""
    var website: String?
    var phone: String?
}
