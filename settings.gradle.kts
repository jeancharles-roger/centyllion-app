rootProject.name = "netbiodyn"

include("core")
include("web")

include("compose:shared")
include("db")

dependencyResolutionManagement {
    versionCatalogs { create("libs") { from(files("versions.toml")) } }
}