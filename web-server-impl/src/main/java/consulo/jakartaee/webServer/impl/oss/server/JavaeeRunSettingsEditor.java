/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package consulo.jakartaee.webServer.impl.oss.server;

import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.jakarta.localize.JakartaLocalize;
import consulo.jakartaee.webServer.impl.run.configuration.CommonModel;
import consulo.jakartaee.webServer.impl.run.configuration.PredefinedLogFilesListener;
import consulo.jakartaee.webServer.impl.run.configuration.PredefinedLogFilesProviderEditor;
import consulo.javaee.bundle.JavaEEServerBundleType;
import consulo.localize.LocalizeValue;
import consulo.proxy.EventDispatcher;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.LabeledLayout;

public abstract class JavaeeRunSettingsEditor<T extends JavaeeServerModel> extends SettingsEditor<CommonModel> implements PredefinedLogFilesProviderEditor {
    private final EventDispatcher<PredefinedLogFilesListener> dispatcher = EventDispatcher.create(PredefinedLogFilesListener.class);

    private JavaEEServerBundleType myBundleType;

    protected JavaeeRunSettingsEditor(JavaEEServerBundleType bundleType) {
        myBundleType = bundleType;
    }

    @Override
    public void addListener(PredefinedLogFilesListener listener) {
        dispatcher.addListener(listener);
    }

    @Override
    public void removeListener(PredefinedLogFilesListener listener) {
        dispatcher.removeListener(listener);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected void resetEditorFrom(CommonModel config) {
        resetEditorFrom((T) config.getServerModel());
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected void applyEditorTo(CommonModel config) throws ConfigurationException {
        applyEditorTo((T) config.getServerModel());
    }

    @Override
    @RequiredUIAccess
    protected Component createUIComponent() {
        return LabeledLayout.create(JakartaLocalize.titleRunConfigurationEditorServerSettings(myBundleType.getDisplayName()), getEditor());
    }

    @Override
    protected void disposeEditor() {
    }

    protected void fireLogFilesChanged() {
        try {
            dispatcher.getMulticaster().predefinedLogFilesChanged(getSnapshot());
        }
        catch (ConfigurationException ignore) {
        }
    }

    protected static int getPort(TextBox text, LocalizeValue message) throws ConfigurationException {
        try {
            return Integer.parseInt(text.getValue());
        }
        catch (NumberFormatException e) {
            throw new ConfigurationException(message);
        }
    }

    @RequiredUIAccess
    protected abstract Component getEditor();

    protected abstract void resetEditorFrom(T model);

    protected abstract void applyEditorTo(T model) throws ConfigurationException;
}
