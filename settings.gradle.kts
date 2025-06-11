rootProject.name = "AlathraResourcePack"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // Allow project repositories
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven("https://maven.athyrium.eu/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
    }
}