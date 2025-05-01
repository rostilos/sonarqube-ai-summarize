package org.perpectiveteam.plugins.aisummarize.classloader;

import org.sonar.api.Plugin;

/**
 * A {@link ElevatedClassLoaderFactory} that uses a ClassLoader from an exposed SonarQube core class as a delegate for
 * attempting to load any classes that are not found from the plugin's ClassLoader.
 *
 * @author Michael Clarke
 */
public class ClassReferenceElevatedClassLoaderFactory implements ElevatedClassLoaderFactory {

    private final String className;

    /*package*/ ClassReferenceElevatedClassLoaderFactory(String className) {
        super();
        this.className = className;
    }

    @Override
    public ClassLoader createClassLoader(Class<? extends Plugin> pluginClass) {
        Class<?> coreClass;
        try {
            coreClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    String.format("Could not load class '%s' from Plugin Classloader", className), e);
        }
        return createClassLoader(pluginClass.getClassLoader(), coreClass.getClassLoader());
    }

}
