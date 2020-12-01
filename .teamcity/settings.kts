import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.ParameterDisplay
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.add
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

    params {
        add {
            password(
                "PASSWORD",
                "credentialsJSON:51b652cf-c42d-49ba-b32b-d7e373105bd3",
                label = "Password",
                description = "Centyllion JKS Password",
                display = ParameterDisplay.HIDDEN,
                readOnly = true
            )
        }
    }

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

    buildType(Build)
    buildType(DeployBeta)
    buildType(DeployApp)
}

object Build : BuildType({
    name = "Build"
    artifactRules = "build/distributions/* => ."

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {}
    }

    steps {
        gradle {
            buildFile = "build.gradle.kts"
            tasks = "jvmTest distribution"
            jdkHome = "%env.JDK_11%"
        }
    }
})

object DeployBeta : BuildType({
    name = "Deploy Beta"

    dependencies {
        dependency(Build) {
            snapshot {}
            artifacts {
                artifactRules = "centyllion-*.tgz"
            }
        }
    }
    triggers {
        finishBuildTrigger {
            buildType = "${Build.id}"
            successfulOnly = true
        }
    }

    steps {
        step {
            name = "Upload distribution"
            type = "ssh-deploy-runner"
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
            param("teamcitySshKey", "Centyllion Deploy")
            param(
                "secure:jetbrains.buildServer.deployer.password",
                "zxxddb8a30a2da357f67b6a3468afce8392c23170b755891609cf108d8b64dc922201e161547acd4d1b"
            )
            param("jetbrains.buildServer.deployer.sourcePath", "centyllion-*.tgz")
            param("jetbrains.buildServer.deployer.targetUrl", "centyllion.com:/home/ubuntu/data/beta/files")
        }
        step {
            name = "Deploy"
            type = "ssh-exec-runner"
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("teamcitySshKey", "Centyllion Deploy")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
            param(
                "secure:jetbrains.buildServer.deployer.password",
                "zxxddb8a30a2da357f67b6a3468afce8392c23170b755891609cf108d8b64dc922201e161547acd4d1b"
            )
            param("jetbrains.buildServer.deployer.targetUrl", "centyllion.com")
            param(
                "jetbrains.buildServer.sshexec.command", """
                cd /home/ubuntu/data/beta
                export PASSWORD=%PASSWORD%
                ./deploy_ssh.sh
            """.trimIndent()
            )
        }
    }
})

object DeployApp : BuildType({
    name = "Deploy App"

    steps {
        step {
            name = "Deploy"
            type = "ssh-exec-runner"
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("teamcitySshKey", "Centyllion Deploy")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
            param(
                "secure:jetbrains.buildServer.deployer.password",
                "zxxddb8a30a2da357f67b6a3468afce8392c23170b755891609cf108d8b64dc922201e161547acd4d1b"
            )
            param("jetbrains.buildServer.deployer.targetUrl", "centyllion.com")
            param(
                "jetbrains.buildServer.sshexec.command", """
                cd /home/ubuntu/data/app
                export PASSWORD=%PASSWORD%
                ./deploy_ssh.sh
            """.trimIndent()
            )
        }
    }
})
