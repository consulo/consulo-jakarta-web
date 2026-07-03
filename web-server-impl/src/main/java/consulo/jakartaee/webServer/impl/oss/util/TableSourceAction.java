/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package consulo.jakartaee.webServer.impl.oss.util;

import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;

import javax.swing.*;

public class TableSourceAction extends OpenSourceAction implements AnActionWithSyncUpdate {

    private final JTable table;

    public TableSourceAction(JTable table) {
        super(table);
        this.table = table;
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(table.getSelectedRowCount() > 0);
    }
}
