//
//  InitialViewController.swift
//  MapstedAppTemplateSampleIOS
//
//  Created by Vimal Bosamiya on 05/09/24.
//

import Foundation
import AppTemplate
import UIKit

class InitialViewController: SplashScreenPermissionViewController {
    static func initViewController() -> InitialViewController {
        let splashScreenVC = super.instantiateViewController()
        if let initialVC = splashScreenVC as? InitialViewController {
            return initialVC
        }
        return InitialViewController()
    }
    
    override func getBottomMenu() -> MapstedMenu {
        
        //Below are the predefined TabBar items.
        // .Home, .Buildings, .Menu, .NewsFeed, .Categories, .Favourites, .Map
        
        // And to add your own Custom tabbar item then you can use as shown below.
        // .addCustomItem(icon: tabImage, name: tabTitle, providerView: myViewController)
        
        // Create your viewcontoller and add it to tab bar as custom item.
        let myViewController = UIViewController()
        let tabImage = UIImage(systemName: "house.fill")!
        let tabTitle: String = "Tab"
        let mapstedMenu = MapstedMenu.builder().addItem(menuType: .Home)
            .addItem(menuType: .Map)
            .addItem(menuType: .Menu)
            .addItem(menuType: .Categories)
            .addCustomItem(icon: tabImage, name: tabTitle, providerView: myViewController)
        return mapstedMenu
        
        
        //We can also add any predefine Tabbar items which is provide in the below code. And we can also set any number of menu items as per your project requirement.
        
        /*
         let mapstedMenu = MapstedMenu.builder().addItem(menuType: .Home)
         .addItem(menuType: .Buildings)
         .addItem(menuType: .Menu)
         .addItem(menuType: .NewsFeed)
         .addItem(menuType: .Categories)
         return mapstedMenu
         */
        
        
        //We can change the order of the tabbar item, provide in the below code.
        /*
         let mapstedMenu = MapstedMenu.builder()
         .addItem(menuType: .Home)
         .addItem(menuType: .Buildings)
         .addItem(menuType: .Menu)
         .addItem(menuType: .Favourites)
         .addItem(menuType: .Categories)
         return mapstedMenu
         */
        
        //NOTE- Menu, tabbar item is compulsory, if you want to switch the property.
    }
}
