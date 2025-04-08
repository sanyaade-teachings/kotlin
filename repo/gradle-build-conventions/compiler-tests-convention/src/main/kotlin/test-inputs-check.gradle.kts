import java.io.File
import java.io.IOException
import java.util.HashSet
import org.gradle.internal.os.OperatingSystem

val disableInputsCheck = project.providers.gradleProperty("kotlin.test.instrumentation.disable.inputs.check").orNull?.toBoolean() == true
if (!disableInputsCheck) {
    val defineJDKEnvVariables = listOf(8, 11, 17, 21)
    tasks.withType<Test>().configureEach {
        val service = project.extensions.getByType<JavaToolchainService>()
        val permissionsTemplateFile = rootProject.file("tests-permissions.template.policy")
        inputs.file(permissionsTemplateFile).withPathSensitivity(PathSensitivity.RELATIVE)
        val rootDirPath = rootDir.canonicalPath
        val gradleUserHomeDir = project.gradle.gradleUserHomeDir.absolutePath
        val policyFileProvider: Provider<RegularFile> = layout.buildDirectory.file("tests-inputs-security.policy")

        doFirst {
            fun parentsReadPermission(file: File): List<String> {
                val parents = mutableListOf<String>()
                var p: File? = file.parentFile
                while (p != null && p.canonicalPath != rootDirPath) {
                    parents.add("""permission java.io.FilePermission "${p.absolutePath}", "read";""")
                    p = p.parentFile
                }
                return parents
            }

            val addedDirs = HashSet<File>()
            val inputPermissions = inputs.files.files.flatMapTo(HashSet<String>()) { file ->
                if (file.isDirectory()) {
                    addedDirs.add(file)
                    listOf(
                        """permission java.io.FilePermission "${file.absolutePath}/", "read";""",
                        """permission java.io.FilePermission "${file.absolutePath}/-", "read${
                            // We write to the testData folder from tests...
                            if (file.canonicalPath.contains("/testData/")) ",write" else ""
                        }";""",
                    )
                } else if (file.extension == "class") {
                    listOfNotNull(
                        """permission java.io.FilePermission "${file.parentFile.absolutePath}/-", "read";""".takeIf { addedDirs.add(file.parentFile) }
                    )
                } else if (file.extension == "jar") {
                    listOf(
                        """permission java.io.FilePermission "${file.absolutePath}", "read";""",
                        """permission java.io.FilePermission "${file.parentFile.absolutePath}", "read";""",
                    )
                } else if (file != null) {
                    val parents = parentsReadPermission(file)
                    listOf(
                        """permission java.io.FilePermission "${file.absolutePath}", "read${
                            if (file.extension == "txt") {
                                ",delete"
                            } else {
                                ""
                            }
                        }";""",
                    ) + parents
                } else emptyList()
            }
            inputs.properties.forEach {
                inputPermissions.add("""permission java.util.PropertyPermission "${it.key}", "read";""")
            }
            environment.forEach {
                inputPermissions.add("""permission java.util.RuntimePermission "${it.key}";""")
            }

            fun calcCanonicalTempPath(): String {
                val file = File(System.getProperty("java.io.tmpdir"))
                try {
                    val canonical = file.getCanonicalPath()
                    if (!OperatingSystem.current().isWindows || !canonical.contains(" ")) {
                        return canonical
                    }
                } catch (_: IOException) {
                }
                return file.absolutePath
            }

            val temp_dir = calcCanonicalTempPath()

            val extDirs = System.getProperty("java.ext.dirs")?.split(":")?.flatMap {
                listOf(
                    """permission java.io.FilePermission "$it", "read";""",
                    """permission java.io.FilePermission "$it/-", "read";""",
                )
            } ?: emptyList()


            val policyFile = policyFileProvider.get().asFile
            policyFile.parentFile.mkdirs()
            policyFile.writeText(
                permissionsTemplateFile.readText()
                    .replace(
                        "{{temp_dir}}",
                        (parentsReadPermission(File(temp_dir)) + """permission java.io.FilePermission "$temp_dir/-", "read,write,delete";""" + """permission java.io.FilePermission "$temp_dir", "read";""").joinToString(
                            "\n    "
                        )
                    )
                    .replace(
                        "{{jdk}}",
                        (listOf(
                            """permission java.io.FilePermission "${
                                javaLauncher.orNull?.executablePath?.asFile?.parentFile?.parentFile?.parentFile?.parentFile?.canonicalPath ?: error(
                                    "No java launcher"
                                )
                            }/-", "read,execute";""",
                        ) + defineJDKEnvVariables.map { version ->
                            val jdkHome =
                                service.launcherFor {
                                    languageVersion.set(JavaLanguageVersion.of(version))
                                }.orNull?.executablePath?.asFile?.parentFile?.parentFile?.parentFile?.parentFile?.canonicalPath
                                    ?: error("Can't find toolchain for $version")
                            """permission java.io.FilePermission "$jdkHome/-", "read,execute";"""
                        } + extDirs
                                ).joinToString("\n    ")
                    )
                    .replace(
                        "{{gradle_user_home}}",
                        """$gradleUserHomeDir"""
                    )
                    .replace("{{inputs}}", inputPermissions.sorted().joinToString("\n    "))
            )

            println("Security policy for test inputs generated to ${policyFile.absolutePath}")
            jvmArgs(
                "-Djava.security.manager=java.lang.SecurityManager",
                "-Djava.security.debug=failure",
                "-Djava.security.policy=${policyFile.absolutePath}",
            )
        }
    }
}
