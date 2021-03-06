import fr.xpdustry.toxopid.task.MindustryExec

plugins {
    id("distributor.base-conventions")
    id("distributor.publishing-conventions")
}

dependencies {
    compileOnly(project(":distributor-core"))
    api("org.mozilla:rhino:1.7.14")
}

tasks.withType<MindustryExec> {
    addArtifact(project(":distributor-core").tasks.named<Jar>("shadowJar"))
}
