pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("http://mobilesdk.mapsted.com:8081/artifactory/gradle-mapsted-developer")
            isAllowInsecureProtocol = true
            credentials {
                username = "admin"
                password = "LkSQk27jeUt86MJk"
            }
        }
//        maven {
//            url = uri("http://mobilesdk.mapsted.com:8081/artifactory/gradle-mapsted-developer")
//        }
//        maven { url = uri("https://mobilesdk.mapsted.com:8443/artifactory/gradle-mapsted") }
    }
}

rootProject.name = "SampleKotlinCompose"
include(":app")