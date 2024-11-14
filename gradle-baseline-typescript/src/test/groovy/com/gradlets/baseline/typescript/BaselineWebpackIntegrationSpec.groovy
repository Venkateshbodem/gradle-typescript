package com.gradlets.baseline.typescript

import nebula.test.IntegrationSpec

class BaselineWebpackIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << """
        apply plugin: 'com.gradlets.baseline-typescript'
        apply plugin: 'com.gradlets.webpack'
        
        repositories {
            npm { url 'https://registry.npmjs.org' }
        }
        version '1.0.0'
        """.stripIndent()

        file('src/main/typescript/index.ts') << 'console.log("foo");'
    }

    def 'correctly configure webpack'() {
        expect:
        def result = runTasksSuccessfully('bundleWebpack')
        result.wasExecuted('generateWebpackConfig')

        fileExists('build/webpack/bundle.js')
        fileExists('build/webpack/bundle.js.map')
        fileExists('build/webpack.config.js')
    }

    def 'tasks are cached correctly'() {
        expect:
        def result1 = runTasksSuccessfully('bundleWebpack')
        result1.wasExecuted('generateWebpackConfig')
        def result2 = runTasksSuccessfully('bundleWebpack')
        result2.wasUpToDate('bundleWebpack')
        result2.wasUpToDate('generateWebpackConfig')
    }
}
