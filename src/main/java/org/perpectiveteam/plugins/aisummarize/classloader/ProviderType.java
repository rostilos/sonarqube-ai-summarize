package org.perpectiveteam.plugins.aisummarize.classloader;

import org.sonar.api.Plugin;
import org.sonar.api.rules.ActiveRule;

import java.util.Arrays;

/**
 * @author Michael Clarke
 */
enum ProviderType {
    CLASS_REFERENCE {
        @Override
        ElevatedClassLoaderFactory createFactory(Plugin.Context context) {
            return new ClassReferenceElevatedClassLoaderFactory(context.getBootConfiguration()
                                                                        .get(ElevatedClassLoaderFactoryProvider.class
                                                                                     .getName() + ".targetType").orElse(ActiveRule.class.getName()));
        }
    },

    REFLECTIVE {
        @Override
        ElevatedClassLoaderFactory createFactory(Plugin.Context context) {
            return new ReflectiveElevatedClassLoaderFactory();
        }
    };

    abstract ElevatedClassLoaderFactory createFactory(Plugin.Context context);

    /*package*/
    static ProviderType fromName(String name) {
        return Arrays.stream(values()).filter(v -> v.name().equals(name)).findFirst().orElseThrow(
                () -> new IllegalStateException(String.format("No provider with type '%s' could be found", name)));
    }

}
