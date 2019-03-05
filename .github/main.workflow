workflow "Push" {
  on = "push"
  resolves = ["GitHub Action for Slack"]
}

action "Test JVM" {
  uses = "MrRamych/gradle-actions@master"
  args = "jvmTest"
}

action "Slack notification" {
  uses = "Ilshidur/action-slack@master"
  secrets = ["SLACK_WEBHOOK"]
  args = "A new commit has been pushed."
}

action "GitHub Action for Slack" {
  uses = "Ilshidur/action-slack@1ee0e72f5aea6d97f26d4a67da8f4bc5774b6cc7"
  needs = ["Test JVM"]
  secrets = ["SLACK_WEBHOOK"]
  args = "Test ran"
}
