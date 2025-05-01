package org.perpectiveteam.plugins.aisummarize.classloader;

import org.sonar.api.Plugin;

/**
 * @author Michael Clarke
 */
public interface ElevatedClassLoaderFactoryProvider {
    ElevatedClassLoaderFactory createFactory(Plugin.Context context);
}
