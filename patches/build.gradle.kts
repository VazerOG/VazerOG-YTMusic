group = "app.vazerog"

patches {
    about {
        name = "VazerOG YTMusic Patches"
        description = "Crossfade and audio enhancements for YouTube Music"
        source = "git@github.com:VazerOG/VazerOG-YTMusic.git"
        author = "VazerOG"
        contact = "https://github.com/VazerOG"
        website = "https://github.com/VazerOG/VazerOG-YTMusic"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    // Used by JsonGenerator.
    implementation(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}
