package org.perpectiveteam.plugins.aisummarize.classloader;

import org.sonar.api.Plugin;

import java.net.URLClassLoader;

/**
 * @author Michael Clarke
 */
public interface ElevatedClassLoaderFactory {

    /**
     * Create a ClassLoader with access to the classes currently exposed to the given <code>pluginClass</code> instance,
     * delegating any other class look-ups to SonarQube's core ClassLoader
     *
     * @param pluginClass the plugin class to create an elevated classloader for
     * @return a ClassLoader with access to SonarQube and plugin classes, with no filtering of which SonarQube classes
     * can be loaded
     */
    ClassLoader createClassLoader(Class<? extends Plugin> pluginClass);


    /**
     * Creates a ClassLoader that delegates class calls to a new URLClassloader referencing he same Jars as the provided
     * <code>pluginClassLoader</code> but using the <code>coreClassLoader</code> as a fall back if the requested class
     * could not be found, and then falling back to the original <code>pluginClassLoader</code> if the class still could
     * not be found. This allows loading classes from the plugin, from SonarQube core, and from any sibling plugins that
     * have been defined as dependencies of the current plugin.
     *
     * @param pluginClassLoader the ClassLoader to find the current Plugin's classes from, and to fall back to in the
     *                          event a class from a dependent plugin is required
     * @param coreClassLoader   the SonarQube core ClassLoader to use for loading non plugin classes from
     * @return a ClassLoader capable of loading both Plugin classes and SonarQube core classes
     */
    /*package*/
    default ClassLoader createClassLoader(ClassLoader pluginClassLoader, ClassLoader coreClassLoader) {
        if (!(pluginClassLoader instanceof URLClassLoader)) {
            throw new IllegalStateException(String.format("Incorrect ClassLoader type. Expected '%s' but got '%s'",
                                                          URLClassLoader.class.getName(),
                                                          pluginClassLoader.getClass().getName()));
        }

        /*
        Sonar analysis wants us to use try-with-resources to close the following Classloader after use. Unfortunately
        Sonar doesn't give us any indication of the plugin being closed/unloaded and the plugin doesn't upload all its
        classes upfront, so the ClassLoader can't be closed outside of the JVM shutting down. The following line is
        therefore marked as 'NOSONAR' to suppress the warning.
         */
        ClassLoader newPluginClassLoader =
                URLClassLoader.newInstance(((URLClassLoader) pluginClassLoader).getURLs(), coreClassLoader); //NOSONAR
        return new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return loadClass(name, false);
            }

            @Override
            public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                try {
                    return newPluginClassLoader.loadClass(name);
                } catch (ClassNotFoundException ex) {
                    return pluginClassLoader.loadClass(name);
                }
            }
        };


    }
}
