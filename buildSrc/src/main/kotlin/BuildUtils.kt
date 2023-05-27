import groovy.namespace.QName
import groovy.util.Node
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.Configuration

fun XmlProvider.addDependenciesFromConfiguration(
    configuration: Configuration,
    scope: String,
    optional: Boolean? = null
) {
    configuration.dependencies
        // added by plugin - cannot be filtered on build script level
        .filter { it.name != "kotlin-stdlib-jdk8" }
        .forEach { artifact ->
            addDependency(
                groupId = artifact.group!!,
                artifactId = artifact.name,
                version = artifact.version!!,
                scope = scope,
                optional = optional
            )
        }
}

fun XmlProvider.addDependencyFromConfiguration(
    configuration: Configuration,
    groupId: String,
    artifactId: String,
    scope: String = "compile",
    optional: Boolean? = null
) {
    configuration.resolvedConfiguration.resolvedArtifacts
        .map { it.moduleVersion.id }
        .filter { it.group == groupId && it.name == artifactId }
        .forEach { artifact ->
            addDependency(
                groupId = artifact.group,
                artifactId = artifact.name,
                version = artifact.version,
                scope = scope,
                optional = optional
            )
        }
}

private fun XmlProvider.addDependency(
    groupId: String,
    artifactId: String,
    version: String,
    scope: String,
    optional: Boolean? = null
) {
    val dependenciesNode = asNode().getAt(QName("dependencies")).firstOrNull() as Node?
        ?: asNode().appendNode("dependencies")
    val dependencyNode = dependenciesNode.appendNode("dependency")
    dependencyNode.appendNode("groupId", groupId)
    dependencyNode.appendNode("artifactId", artifactId)
    dependencyNode.appendNode("version", version)
    dependencyNode.appendNode("scope", scope)
    if (optional != null) {
        dependencyNode.appendNode("optional", optional.toString())
    }
}
