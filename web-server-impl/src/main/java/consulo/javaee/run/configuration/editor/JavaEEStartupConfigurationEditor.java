package consulo.javaee.run.configuration.editor;

import consulo.application.Application;
import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.ui.awt.EnvironmentVariablesTextFieldWithBrowseButton;
import consulo.jakarta.localize.JakartaLocalize;
import consulo.javaee.run.configuration.JavaEEConfigurationImpl;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.ListBox;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;

/**
 * @author VISTALL
 * @since 2017-07-11
 */
public class JavaEEStartupConfigurationEditor extends SettingsEditor<JavaEEConfigurationImpl> {
    @Override
    @RequiredUIAccess
    protected Component createUIComponent() {
        ListBox<Executor> executorList = ListBox.create(Application.get().getExtensionList(Executor.class));
        executorList.setRender((presentation, item) -> {
            Executor executor = item.getValue();
            if (executor != null) {
                presentation.withIcon(executor.getIcon());
                presentation.append(executor.getActionName());
            }
        });

        FormBuilder form = FormBuilder.create();
        form.addLabeled(JakartaLocalize.labelRunConfigurationEditorStartupScript(), createScriptRow());
        form.addLabeled(JakartaLocalize.labelRunConfigurationEditorShutdownScript(), createScriptRow());
        form.addLabeled(
            JakartaLocalize.labelRunConfigurationEditorEnvironmentVariables(),
            new EnvironmentVariablesTextFieldWithBrowseButton().getComponent()
        );
        form.addBottom(CheckBox.create(JakartaLocalize.checkboxRunConfigurationEditorPassEnvironmentVariables()));

        return ScrollableLayout.create(VerticalLayout.create().add(executorList).add(form.build()));
    }

    @RequiredUIAccess
    private static Component createScriptRow() {
        return DockLayout.create()
            .center(TextBox.create())
            .right(CheckBox.create(JakartaLocalize.checkboxEditScriptPropertiesUseDefault()));
    }

    @Override
    protected void resetEditorFrom(JavaEEConfigurationImpl configuration) {
    }

    @Override
    protected void applyEditorTo(JavaEEConfigurationImpl configuration) throws ConfigurationException {
    }
}
