package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'DeployBeta'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("DeployBeta")) {
    expectSteps {
        step {
            name = "Upload distribution"
            type = "ssh-deploy-runner"
            param("jetbrains.buildServer.deployer.sourcePath", "centyllion-*.tgz")
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
            param("jetbrains.buildServer.deployer.targetUrl", "centyllion.com:/home/ubuntu/data/beta/files")
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
            param("secure:jetbrains.buildServer.deployer.password", "zxxddb8a30a2da357f67b6a3468afce8392c23170b755891609cf108d8b64dc922201e161547acd4d1b")
            param("teamcitySshKey", "Centyllion Deploy")
        }
        step {
            name = "Deploy"
            type = "ssh-exec-runner"
            param("jetbrains.buildServer.deployer.targetUrl", "centyllion.com")
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
            param("jetbrains.buildServer.sshexec.command", """
                cd /home/ubuntu/data/beta
                ./deploy_ssh.sh
            """.trimIndent())
            param("secure:jetbrains.buildServer.deployer.password", "zxxddb8a30a2da357f67b6a3468afce8392c23170b755891609cf108d8b64dc922201e161547acd4d1b")
            param("teamcitySshKey", "Centyllion Deploy")
        }
    }
    steps {
        update<BuildStep>(0) {
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:5322f3d4-579d-4273-9528-dc144703da7a")
        }
        update<BuildStep>(1) {
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:5322f3d4-579d-4273-9528-dc144703da7a")
        }
    }
}
