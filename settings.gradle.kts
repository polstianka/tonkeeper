pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "TON Apps"

include(":apps:signer")


include(":baselineprofile:main")
include(":baselineprofile:x")


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
include(":lib:ledger")

include(":apps:wallet:instance:app")
include(":apps:wallet:instance:main")
include(":apps:wallet:instance:x")
include(":apps:wallet:localization")
include(":apps:wallet:api")

include(":apps:wallet:data:core")
include(":apps:wallet:data:settings")
include(":apps:wallet:data:account")
include(":apps:wallet:data:rates")
include(":apps:wallet:data:tokens")
include(":apps:wallet:data:events")
include(":apps:wallet:data:collectibles")
include(":apps:wallet:data:tonconnect")
include(":apps:wallet:data:push")
include(":apps:wallet:data:browser")
include(":apps:wallet:data:backup")
include(":apps:wallet:data:rn")
include(":apps:wallet:data:passcode")
include(":apps:wallet:data:staking")
include(":apps:wallet:data:purchase")
include(":apps:wallet:data:battery")
