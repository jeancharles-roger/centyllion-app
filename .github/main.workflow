workflow "Push" {
  on = "push"
  resolves = ["GitHub Action for Slack"]
}

action "Test JVM and Deploy" {
  uses = "MrRamych/gradle-actions/openjdk-11@2.1"
  secrets = ["DEPLOY_KEY", "DB_PASSWORD"]
  args = "jvmTest deployBeta"
}

action "GitHub Action for Slack" {
  uses = "Ilshidur/action-slack@1ee0e72f5aea6d97f26d4a67da8f4bc5774b6cc7"
  needs = ["Test JVM and Deploy"]
  secrets = ["SLACK_WEBHOOK"]
  args = "Centyllion deployed"
}
