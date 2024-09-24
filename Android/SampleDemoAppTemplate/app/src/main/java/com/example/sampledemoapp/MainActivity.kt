package com.example.sampledemoapp

import android.os.Bundle
import com.mapsted.app_template.AppTemplateMainActivity
import com.mapsted.app_template.presentation.menu.MenuUtil
import com.mapsted.app_template.presentation.menu.MenuUtil.BottomMenu
import com.mapsted.map.views.MapPanType
import com.mapsted.ui.CustomParams
import com.mapsted.ui.utils.common.Settings

class MainActivity : AppTemplateMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setCustomParams(getCustomParams())
        super.onCreate(savedInstanceState)
        //This is to set a custom bottom navigation menu; otherwise, the default behavior will apply to the bottom navigation menu.
        super.setupCustomMenu(buildBottomMenu())
    }

    private fun getCustomParams(): CustomParams {
        val params = CustomParams.newBuilder(this)
            // Add additional custom content here
            .setEnablePropertyListSelection(false)
            .setEnableMapOverlayFeature(true)
            .setEnableStagingEnvironmentSettings(true)
            .setEnableBlueDotCustomizationSettings(true)
            .setDefaultDistanceUnit(Settings.DistanceUnits.AUTO)
            .setMapPanType(MapPanType.RESTRICT_TO_SELECTED_PROPERTY)
            .build()
        return params
    }

    private fun buildBottomMenu(): BottomMenu {
        //Below are the predefined bottom navigation menu items.
        // MenuUtil.MenuType.Home
        // MenuUtil.MenuType.Building
        // MenuUtil.MenuType.Menu
        // MenuUtil.MenuType.Category
        // MenuUtil.MenuType.Favorites
        // MenuUtil.MenuType.Map

        // And to add your own Custom bottom navigation menu item then you can use as shown below.
        // .addCustomItem(R.drawable.ic_add,"Menu","com.mapsted.app_template.presentation.menu.MenuFragment"//here you can pass any custom fragment)

        return BottomMenu.Builder()
            .addMenu(
                com.mapsted.app_template.R.drawable.icon_menu_buildings,
                "Buildings",
                menuType = MenuUtil.MenuType.Building
            )
            .addMenu(
                com.mapsted.app_template.R.drawable.ic_menu_home,
                "Home",
                menuType = MenuUtil.MenuType.Home
            )
            .addCustomMenu(
                com.mapsted.app_template.R.drawable.three_dash_icon,
                "Menu",
                "com.mapsted.app_template.presentation.menu.MenuFragment"
            )
            .addMenu(
                com.mapsted.app_template.R.drawable.icon_menu_map,
                "Maps",
                menuType = MenuUtil.MenuType.Map
            )
            .addMenu(
                com.mapsted.app_template.R.drawable.icon_menu_favourites,
                "Favorite",
                menuType = MenuUtil.MenuType.Favorites
            )
            .build()

        // Create your fragment and add it to bottom navigation menu as custom item.
        /*return BottomMenu.Builder()
            .addMenu(R.drawable.icon_menu_buildings, getString(R.string.buildings), menuType = MenuUtil.MenuType.Building)
            .addCustomMenu(R.drawable.three_dash_icon,"Menu","com.mapsted.app_template.presentation.menu.MenuFragment")
            .addMenu(R.drawable.icon_menu_favourites, getString(R.string.favourite), menuType = MenuUtil.MenuType.Favorites)
            .addMenu(R.drawable.icon_menu_map, getString(R.string.map), menuType = MenuUtil.MenuType.Map)
            .addMenu(R.drawable.ic_menu_home, getString(R.string.home), menuType = MenuUtil.MenuType.Home)
            .build()*/

        //We can also add any predefine bottom navigation menu items which is provide in the below code.
        /*return BottomMenu.Builder()
            .addMenu(R.drawable.ic_menu_home, getString(R.string.home), menuType = MenuUtil.MenuType.Home)
            .addMenu(R.drawable.icon_menu_buildings, getString(R.string.buildings), menuType = MenuUtil.MenuType.Building)
            .addMenu(R.drawable.three_dash_icon, getString(com.mapsted.app_template.R.string.option_menu), menuType = MenuUtil.MenuType.Menu)
            .addMenu(R.drawable.icon_menu_favourites, getString(R.string.favourite), menuType = MenuUtil.MenuType.Favorites)
            .addMenu(R.drawable.icon_menu_map, getString(R.string.map), menuType = MenuUtil.MenuType.Map)
            .build()*/

        //We can change the order of the bottom navigation menu item, provide in the below code.
        /*return BottomMenu.Builder()
            .addMenu(R.drawable.icon_menu_buildings, getString(R.string.buildings), menuType = MenuUtil.MenuType.Building)
            .addMenu(R.drawable.ic_menu_home, getString(R.string.home), menuType = MenuUtil.MenuType.Home)
            .addMenu(R.drawable.icon_menu_map, getString(R.string.map), menuType = MenuUtil.MenuType.Map)
            .addMenu(R.drawable.three_dash_icon, getString(com.mapsted.app_template.R.string.option_menu), menuType = MenuUtil.MenuType.Menu)
            .addMenu(R.drawable.icon_menu_favourites, getString(R.string.favourite), menuType = MenuUtil.MenuType.Favorites)
            .build()*/

        //NOTE- Menu, bottom navigation bar item is compulsory, if you want to switch the property.
    }
}