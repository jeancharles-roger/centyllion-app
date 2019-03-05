workflow "Push" {
  on = "push"
  resolves = ["Test JVM"]
}

action "Test JVM" {
  uses = "MrRamych/gradle-actions@master"
  args = "jvmTest"
}
