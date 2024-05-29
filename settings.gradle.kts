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
    }
}

rootProject.name = "TON Apps"

include(":apps:signer")


include(":baselineprofile:x")
include(":baselineprofile:signer")


include(":ton")
include(":core")
include(":tonapi")


include(":ui:shimmer")
include(":ui:blur")

include(":ui:uikit:core")
include(":ui:uikit:color")
include(":ui:uikit:icon")
include(":ui:uikit:list")

include(":lib:extensions")
include(":lib:security")
include(":lib:network")
include(":lib:qr")
include(":lib:icu")
include(":lib:emoji")
include(":lib:blockchain")
include(":lib:sqlite")

include(":apps:wallet:instance")
include(":apps:wallet:localization")
include(":apps:wallet:api")

include(":apps:wallet:data:core")
include(":apps:wallet:data:settings")
include(":apps:wallet:data:account")
include(":apps:wallet:data:rates")
include(":apps:wallet:data:tokens")
include(":apps:wallet:data:stonfi")
include(":apps:wallet:data:events")
include(":apps:wallet:data:collectibles")
include(":apps:wallet:data:tonconnect")
include(":apps:wallet:data:push")
include(":apps:wallet:data:browser")