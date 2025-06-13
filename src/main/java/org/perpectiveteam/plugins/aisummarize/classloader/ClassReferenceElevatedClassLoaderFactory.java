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
        Class<?> coreClass = null;
        ClassNotFoundException lastException = null;

        ClassLoader[] classLoaders = {
                pluginClass.getClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassLoader.getSystemClassLoader(),
                this.getClass().getClassLoader()
        };

        for (ClassLoader cl : classLoaders) {
            if (cl == null) continue;
            try {
                coreClass = Class.forName(className, true, cl);
                break;
            } catch (ClassNotFoundException e) {
                lastException = e;
            }
        }

        if (coreClass == null) {
            throw new IllegalStateException(
                    String.format("Could not load class '%s' from any available classloader", className),
                    lastException);
        }

        return createClassLoader(pluginClass.getClassLoader(), coreClass.getClassLoader());
    }

}
