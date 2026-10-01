package consulo.javaee.run.configuration.editor;

import consulo.configurable.ConfigurationException;
import consulo.content.bundle.Sdk;
import consulo.disposer.Disposer;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.jakarta.localize.JakartaLocalize;
import consulo.jakartaee.webServer.impl.run.configuration.ApplicationServerSelectionListener;
import consulo.jakartaee.webServer.impl.run.configuration.CommonModel;
import consulo.javaee.bundle.JavaEEServerBundleType;
import consulo.javaee.run.configuration.JavaEEConfigurationImpl;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2017-07-09
 */
public class JavaEEServerConfigurationEditor extends SettingsEditor<JavaEEConfigurationImpl> {
    private final JavaEEServerBundleType myBundleType;

    @Nullable
    private BundleBox myBundleBox;
    @Nullable
    private DockLayout myServerSettingsPanel;
    @Nullable
    private SettingsEditor<CommonModel> myServerEditor;
    @Nullable
    private JavaEEConfigurationImpl myConfiguration;
    private boolean myResetting;

    public JavaEEServerConfigurationEditor(JavaEEServerBundleType bundleType) {
        myBundleType = bundleType;
    }

    @Override
    @RequiredUIAccess
    protected Component createUIComponent() {
        BundleBox bundleBox = BundleBoxBuilder.create(this)
            .withSdkTypeFilterByType(myBundleType)
            .withNoneItem()
            .build();
        bundleBox.getComponent().addValueListener(event -> serverSelected(event.getValue()));
        myBundleBox = bundleBox;

        FormBuilder form = FormBuilder.create();
        form.addLabeled(JakartaLocalize.labelRunConfigurationPropertiesApplicationServer(), bundleBox.getComponent());

        DockLayout serverSettingsPanel = DockLayout.create();
        myServerSettingsPanel = serverSettingsPanel;

        SettingsEditor<CommonModel> serverEditor = myServerEditor;
        if (serverEditor != null) {
            serverSettingsPanel.center(serverEditor.getUIComponent());
        }

        JavaEEConfigurationImpl configuration = myConfiguration;
        if (configuration != null) {
            selectServer(bundleBox, configuration);
        }

        return VerticalLayout.create().add(form.build()).add(serverSettingsPanel);
    }

    @RequiredUIAccess
    private void serverSelected(@Nullable BundleBox.BundleBoxItem item) {
        JavaEEConfigurationImpl configuration = myConfiguration;
        SettingsEditor<CommonModel> serverEditor = myServerEditor;
        if (myResetting || configuration == null || serverEditor == null) {
            return;
        }

        Sdk server = item == null ? null : item.getBundle();

        configuration.APPLICATION_SERVER_NAME = server == null ? null : server.getName();

        if (serverEditor instanceof ApplicationServerSelectionListener applicationServerSelectionListener) {
            applicationServerSelectionListener.serverSelected(server);
        }

        serverEditor.resetFrom(configuration);

        fireEditorStateChanged();
    }

    @RequiredUIAccess
    private void selectServer(BundleBox bundleBox, JavaEEConfigurationImpl configuration) {
        myResetting = true;
        try {
            bundleBox.setSelectedBundle(configuration.APPLICATION_SERVER_NAME);
        }
        finally {
            myResetting = false;
        }
    }

    @Override
    @RequiredUIAccess
    @SuppressWarnings("unchecked")
    protected void resetEditorFrom(JavaEEConfigurationImpl configuration) {
        myConfiguration = configuration;

        SettingsEditor<CommonModel> oldServerEditor = myServerEditor;
        if (oldServerEditor != null) {
            Disposer.dispose(oldServerEditor);
            myServerEditor = null;
        }

        SettingsEditor<CommonModel> serverEditor = configuration.getServerModel().getEditor();
        if (serverEditor != null) {
            Disposer.register(this, serverEditor);
            myServerEditor = serverEditor;
        }

        DockLayout serverSettingsPanel = myServerSettingsPanel;
        if (serverSettingsPanel != null) {
            serverSettingsPanel.removeAll();
        }

        if (serverEditor != null) {
            Component serverComponent = serverEditor.getUIComponent();
            if (serverSettingsPanel != null) {
                serverSettingsPanel.center(serverComponent);
            }
            serverEditor.resetFrom(configuration);
        }

        BundleBox bundleBox = myBundleBox;
        if (bundleBox != null) {
            selectServer(bundleBox, configuration);
        }
    }

    @Override
    protected void applyEditorTo(JavaEEConfigurationImpl configuration) throws ConfigurationException {
        SettingsEditor<CommonModel> serverEditor = myServerEditor;
        if (serverEditor != null) {
            serverEditor.applyTo(configuration);
        }

        BundleBox bundleBox = myBundleBox;
        if (bundleBox != null) {
            configuration.APPLICATION_SERVER_NAME = bundleBox.getSelectedBundleName();
        }
    }
}
