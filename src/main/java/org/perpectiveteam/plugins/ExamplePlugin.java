package org.perpectiveteam.plugins;

import org.sonar.api.Plugin;
import org.perpectiveteam.plugins.hooks.PostJobInScanner;

public class ExamplePlugin implements Plugin {

  @Override
  public void define(Context context) {
    context.addExtension(PostJobInScanner.class);
  }
}
