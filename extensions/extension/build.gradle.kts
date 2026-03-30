extension {
    name = "extensions/extension.rve"
}

android {
    namespace = "app.revanced.extension"
}

dependencies {
    compileOnly(files("libs/media3-stubs.jar"))
}
