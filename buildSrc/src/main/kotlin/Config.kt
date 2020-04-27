object Config {
    object Versions {
        object Kotlin {
            const val kotlin = "1.3.72"
        }
        object Plugin {
            const val androidGradle = "3.6.1"
            const val publish = "0.11.1"

            // https://github.com/Kotlin/dokka/issues/819
            const val dokka = "0.9.18" // "0.10.1"
        }
        object Test {
            const val junit = "4.13"
            const val mockito = "2.28.2"
        }
        object Android {
            const val appCompat = "1.1.0"
            const val coreKtx = "1.2.0"
            const val constraintLayout = "1.1.3"
        }
        object AndroidTest {
            const val runner = "1.2.0"
            const val extJunit = "1.1.1"
            const val espressoCore = "3.2.0"
        }
    }
    object Android {
        const val buildToolsVersion = "29.0.3"
        const val minSdk = 16
        const val compileSdk = 29
    }
}