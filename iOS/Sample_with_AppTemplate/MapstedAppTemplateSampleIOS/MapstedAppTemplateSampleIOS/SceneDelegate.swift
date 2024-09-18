//
//  SceneDelegate.swift
//  MapstedAppTemplateSampleIOS
//
//  Created by Vimal Bosamiya on 04/09/24.
//

import UIKit
import AppTemplate
class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?
    
    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        // Ensure the scene is of type UIWindowScene
        guard let windowScene = scene as? UIWindowScene else { return }
        
        // Initialize the window if needed
        if self.window == nil {
            self.window = UIWindow(windowScene: windowScene)
        }
        
        //Compute copyright text OR use from localizable strings
        let copyRightText = "Copyright © 2014-2022 Mapsted Corp.\nAll rights reserved."

        //Set the name of the animation gif image without the extension and splash screen copyright text. This will display while the app intiailizes.
        //Set the SplashScreenInfo and if required then provide  this optional parameters  copyrightInfoText,splashScreenGifLightMode,splashScreenGifDarkMode. This will display while the app intiailizes.
        AppTemplateParams.shared.splashScreenInfo = SplashScreenInfo(copyrightInfoText: copyRightText, splashScreenGifLightMode: "Logo-Animation", splashScreenGifDarkMode: "Logo-Animation-Dark")

        //Set the SplashScreenPermissionViewController as root viewcontroller to the window.
        //For the default TabBar use below code
        let permissionController = SplashScreenPermissionViewController.instantiateViewController()
        
        //If you want to customize the tabbar then comment the above code which is set for default tabbar and uncomment the below code which can be used for customising the tabbar.
        //let permissionController = InitialViewController.initViewController()
        
        let navController = UINavigationController(rootViewController: permissionController)
        navController.isNavigationBarHidden = true

        self.window?.rootViewController = navController

        self.window?.makeKeyAndVisible()
    }


    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
    }


}

