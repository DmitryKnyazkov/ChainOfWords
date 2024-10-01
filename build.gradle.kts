// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

// для того чтобы работал рум нужно по ссылка https://plugins.gradle.org/plugin/org.jetbrains.kotlin.kapt
//    перейти и скопировать то что ниже. соответственно сюда вставить. Иначе рум работать не будет.
    id("org.jetbrains.kotlin.kapt") version "2.0.20" apply false
}