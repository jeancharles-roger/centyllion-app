import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.version

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {

    features {
        feature {
            id = "PROJECT_EXT_3"
            type = "IssueTracker"
            param("secure:password", "")
            param("name", "Centyllion")
            param("pattern", """#(\d+)""")
            param("authType", "accesstoken")
            param("repository", "https://github.com/jeancharles-roger/centyllion")
            param("type", "GithubIssues")
            param("secure:accessToken", "credentialsJSON:e848deb0-32d8-4f3d-bd39-f36e997d9849")
            param("username", "")
        }
    }

    buildType {
        id("Build")
        name = "Build"
        artifactRules = "build/distribution/*.tar.gz => ."

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            gradle {
                tasks = "build distribution"
            }
        }
    }
}
