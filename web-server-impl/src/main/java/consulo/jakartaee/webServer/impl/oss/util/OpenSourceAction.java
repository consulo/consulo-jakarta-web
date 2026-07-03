/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package consulo.jakartaee.webServer.impl.oss.util;

import consulo.application.AllIcons;
import consulo.dataContext.DataManager;
import consulo.jakartaee.webServer.impl.oss.JavaeeBundle;
import consulo.ui.ex.OpenSourceUtil;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.CustomShortcutSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class OpenSourceAction extends AnAction {

    private final Component component;

    public OpenSourceAction(JComponent component) {
        super(JavaeeBundle.message("GenericAction.source"), null, AllIcons.Actions.EditSource);
        this.component = component;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)), component);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        OpenSourceUtil.openSourcesFrom(DataManager.getInstance().getDataContext(component), true);
    }
}
