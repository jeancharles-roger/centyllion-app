rootProject.name = "netbiodyn"

include("core")
include("web")

include("compose:shared")

dependencyResolutionManagement {
    versionCatalogs { create("libs") { from(files("versions.toml")) } }
}