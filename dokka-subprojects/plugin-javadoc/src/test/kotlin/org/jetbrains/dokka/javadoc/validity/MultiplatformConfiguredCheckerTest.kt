/*
 * Copyright 2014-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.dokka.javadoc.validity

import org.jetbrains.dokka.DokkaConfigurationImpl
import org.jetbrains.dokka.DokkaException
import org.jetbrains.dokka.ExternalDocumentationLink
import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MultiplatformConfiguredCheckerTest : BaseAbstractTest() {

    val mppConfig: DokkaConfigurationImpl = dokkaConfiguration {
        format = "javadoc"
        sourceSets {
            sourceSet {
                name = "jvm"
                sourceRoots = listOf("src/jvm")
                analysisPlatform = "jvm"
                externalDocumentationLinks = listOf(
                    ExternalDocumentationLink("https://docs.oracle.com/javase/8/docs/api/"),
                    ExternalDocumentationLink("https://kotlinlang.org/api/core/")
                )
            }
            sourceSet {
                name = "js"
                sourceRoots = listOf("src/js")
                analysisPlatform = "js"
                externalDocumentationLinks = listOf(
                    ExternalDocumentationLink("https://docs.oracle.com/javase/8/docs/api/"),
                    ExternalDocumentationLink("https://kotlinlang.org/api/core/")
                )
            }
        }
    }

    val sppConfig: DokkaConfigurationImpl = dokkaConfiguration {
        format = "javadoc"
        sourceSets {
            sourceSet {
                sourceRoots = listOf("src")
                analysisPlatform = "jvm"
                externalDocumentationLinks = listOf(
                    ExternalDocumentationLink("https://docs.oracle.com/javase/8/docs/api/"),
                    ExternalDocumentationLink("https://kotlinlang.org/api/core/")
                )
            }
        }
    }

    @Test
    fun `mpp config should fail for javadoc`() {
        testInline(
            """
            |/src/jvm/kotlin/example/Test.kt
            |class Test 
            |/src/js/kotlin/example/Test.kt
            |class Test 
            """.trimMargin(), mppConfig
        ) {
            verificationStage = { verification ->
                var mppDetected = false
                try {
                    verification()
                } catch (e: DokkaException) {
                    mppDetected =
                        e.localizedMessage == "Pre-generation validity check failed: ${MultiplatformConfiguredChecker.errorMessage}"
                }
                assertTrue(mppDetected, "MPP configuration not detected")
            }
        }
    }

    @Test
    fun `spp config should not fail for javadoc`() {
        testInline(
            """
            |/src/main/kotlin/example/Test.kt
            |class Test 
            """.trimMargin(), sppConfig
        ) {
            verificationStage = { verification ->
                var mppDetected = false
                try {
                    verification()
                } catch (e: DokkaException) {
                    mppDetected =
                        e.localizedMessage == "Pre-generation validity check failed: ${MultiplatformConfiguredChecker.errorMessage}"
                }
                assertFalse(mppDetected, "SPP taken as multiplatform")
            }
        }
    }
}
