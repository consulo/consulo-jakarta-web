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

import consulo.configurable.ConfigurationException;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * A fragment showing one of the server editors of a JavaEE configuration, which host the editors of the server integration and of
 * the deployed artifacts.
 */
public abstract class JavaEESettingsEditorFragment implements RunConfigurationFragment<JavaEEConfigurationImpl> {
    @Override
    public boolean isRemovable() {
        return false;
    }

    protected abstract SettingsEditor<JavaEEConfigurationImpl> createSettingsEditor(Project project);

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<JavaEEConfigurationImpl> createEditor(Project project, Disposable parent) {
        SettingsEditor<JavaEEConfigurationImpl> editor = createSettingsEditor(project);
        Disposer.register(parent, editor);

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addBottom(editor.getUIComponent());
            }

            @RequiredUIAccess
            @Override
            public void reset(JavaEEConfigurationImpl target) {
                editor.resetFrom(target);
            }

            @RequiredUIAccess
            @Override
            public void apply(JavaEEConfigurationImpl target) throws ConfigurationException {
                editor.applyTo(target);
            }
        };
    }
}
