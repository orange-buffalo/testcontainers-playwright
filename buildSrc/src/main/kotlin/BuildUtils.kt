import groovy.namespace.QName
import groovy.util.Node
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.Configuration

fun XmlProvider.addDependenciesFromConfiguration(configuration: Configuration, scope: String, optional: Boolean? = null) {
    val dependenciesNode = asNode().getAt(QName("dependencies")).firstOrNull() as Node?
        ?: asNode().appendNode("dependencies")
    configuration.dependencies
        // added by plugin - cannot be filtered on build script level
        .filter { it.name != "kotlin-stdlib-jdk8" }
        .forEach { artifact ->
            val dependencyNode = dependenciesNode.appendNode("dependency")
            dependencyNode.appendNode("groupId", artifact.group)
            dependencyNode.appendNode("artifactId", artifact.name)
            dependencyNode.appendNode("version", artifact.version)
            dependencyNode.appendNode("scope", scope)
            if (optional != null) {
                dependencyNode.appendNode("optional", optional.toString())
            }
        }
}
