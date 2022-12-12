object Config {
    object Versions {
        object Kotlin {
            const val kotlin = "1.7.22"
        }
        object Plugin {
            const val androidGradle = "7.3.1"
            const val publish = "0.22.0"

            // https://github.com/Kotlin/dokka/issues/819
            const val dokka = "0.9.18" // "0.10.1"
        }
        object Test {
            const val junit = "4.13.2"
            const val mockito = "4.9.0"
        }
        object Android {
            const val appCompat = "1.5.1"
            const val coreKtx = "1.9.0"
            const val constraintLayout = "2.1.4"
        }
        object AndroidTest {
            const val runner = "1.5.1"
            const val extJunit = "1.1.4"
            const val espressoCore = "3.5.0"
        }
    }
    object Android {
        const val buildToolsVersion = "33.0.1"
        const val minSdk = 16
        const val compileSdk = 33
    }
}