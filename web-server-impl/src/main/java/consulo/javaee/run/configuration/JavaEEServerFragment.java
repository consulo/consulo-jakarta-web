/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.javaee.run.configuration;

import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.jakarta.localize.JakartaLocalize;
import consulo.javaee.bundle.JavaEEServerBundleType;
import consulo.javaee.run.configuration.editor.JavaEEServerConfigurationEditor;
import consulo.localize.LocalizeValue;
import consulo.project.Project;

/**
 * The application server and the settings of its integration.
 */
public class JavaEEServerFragment extends JavaEESettingsEditorFragment {
    private final JavaEEServerBundleType myBundleType;

    public JavaEEServerFragment(JavaEEServerBundleType bundleType) {
        myBundleType = bundleType;
    }

    @Override
    public String getId() {
        return "javaeeServer";
    }

    @Override
    public LocalizeValue getName() {
        return JakartaLocalize.titleRunConfigurationEditorServer();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    protected SettingsEditor<JavaEEConfigurationImpl> createSettingsEditor(Project project) {
        return new JavaEEServerConfigurationEditor(myBundleType);
    }
}
