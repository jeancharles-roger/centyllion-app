workflow "Push" {
  on = "push"
  resolves = ["Deploy Slack"]
}

action "Test JVM" {
  uses = "MrRamych/gradle-actions/openjdk-11@2.1"
  args = "jvmTest"
}

action "Test Slack" {
  uses = "Ilshidur/action-slack@1ee0e72f5aea6d97f26d4a67da8f4bc5774b6cc7"
  needs = ["Test JVM"]
  secrets = ["SLACK_WEBHOOK"]
  args = "Centyllion tested"
}

action "On master branch" {
  uses = "actions/bin/filter@master"
  needs = ["Test Slack"]
  args = "branch master"
}

action "Deploy" {
  uses = "MrRamych/gradle-actions/openjdk-11@2.1"
  needs = ["On master branch"]
  secrets = ["DEPLOY_KEY", "DB_PASSWORD"]
  args = "deployBeta"
}

action "Deploy Slack" {
  uses = "Ilshidur/action-slack@1ee0e72f5aea6d97f26d4a67da8f4bc5774b6cc7"
  needs = ["Deploy"]
  secrets = ["SLACK_WEBHOOK"]
  args = "Centyllion deployed"
}
