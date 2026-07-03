/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package consulo.jakartaee.webServer.impl.oss.util;

import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;

import javax.swing.*;

public class TreeSourceAction extends OpenSourceAction implements AnActionWithSyncUpdate {

    private final JTree tree;

    public TreeSourceAction(JTree tree) {
        super(tree);
        this.tree = tree;
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(tree.getSelectionCount() > 0);
    }
}
