/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */
package consulo.jakartaee.webServer.impl.oss.editor;

import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.ex.awt.ColumnInfo;
import consulo.ui.ex.awt.UIUtil;
import consulo.xml.dom.DomElement;
import consulo.xml.util.xml.ui.AbstractTableView;
import consulo.xml.util.xml.ui.CommittablePanel;
import consulo.xml.util.xml.ui.StripeTableCellRenderer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class JavaeeSectionView implements CommittablePanel {
    private final SectionTableView view;

    private final JavaeeSection<?>[] sections;

    private final ColumnInfo<RowElement, ?>[] columns;

    private final List<RowElement> items = new ArrayList<>();

    private final Set<JavaeeSection<?>> expanded = new HashSet<>();

    private final Map<JavaeeSection<?>, JavaeeSectionInfo<DomElement>[]> mapped = new HashMap<>();

    private static final Component EXPANDED_ICON = createLabel(UIUtil.getTreeExpandedIcon());

    private static final Component COLLAPSED_ICON = createLabel(UIUtil.getTreeCollapsedIcon());

    private static final JavaeeSectionInfo<DomElement> EMPTY_COLUMN = new JavaeeSectionInfo<>(null) {
        private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

        @Override
        public String valueOf(DomElement item) {
            return "";
        }

        @Override
        public TableCellRenderer getRenderer(DomElement item) {
            return renderer;
        }
    };

    public JavaeeSectionView(Project project, String empty, JavaeeSection<?>... sections) {
        this.sections = sections.clone();
        view = new SectionTableView(project, empty);
        JTable table = view.getTable();
        table.setRowHeight(PlatformIconGroup.nodesClass().getHeight());
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.addMouseListener(createMouseListener(table));
        table.addKeyListener(createKeyListener(table));
        table.getTableHeader().setDefaultRenderer(createHeaderRenderer());
        expanded.addAll(Arrays.asList(sections));
        columns = createColumnInfos();
        view.initialize();
        reset();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void commit() {
    }

    @Override
    public void reset() {
        items.clear();
        for (JavaeeSection<?> section : sections) {
            List<? extends DomElement> values = section.getValues();
            if (!values.isEmpty()) {
                items.add(new SectionRowElement(section));
                if (expanded.contains(section)) {
                    for (DomElement value : values) {
                        items.add(new DomRowElement(section, value));
                    }
                }
            }
        }
        view.reset(columns, items);
    }

    @Override
    public JComponent getComponent() {
        return view;
    }

    @Nonnull
    private JavaeeSectionInfo<DomElement> getColumnInfo(JavaeeSection<?> section, int column) {
        JavaeeSectionInfo<DomElement>[] infos = mapped.get(section);
        return (column <= infos.length) ? infos[column] : EMPTY_COLUMN;
    }

    private MouseAdapter createMouseListener(final JTable table) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                Point point = new Point(event.getPoint());
                int row = table.rowAtPoint(point);
                int col = table.columnAtPoint(point);
                if ((row >= 0) && (col >= 0)) {
                    JComponent component = (JComponent) table.prepareRenderer(table.getCellRenderer(row, col), row, col);
                    Rectangle rect = table.getCellRect(row, col, true);
                    component.setSize(rect.getSize());
                    component.doLayout();
                    point.translate(-rect.x, -rect.y);
                    if (event.getClickCount() >= 2 || component.findComponentAt(point) instanceof IconLabel) {
                        items.get(row).handleDoubleClick();
                    }
                }
            }
        };
    }

    private KeyAdapter createKeyListener(final JTable table) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                int row = table.getSelectedRow();
                if ((row >= 0) && ((event.getKeyCode() == KeyEvent.VK_LEFT) || (event.getKeyCode() == KeyEvent.VK_RIGHT))) {
                    items.get(row).handleLeftRight(event.getKeyCode() == KeyEvent.VK_RIGHT);
                }
            }
        };
    }

    private DefaultTableCellRenderer createHeaderRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, col);
                setBorder(null);
                return this;
            }
        };
    }

    @SuppressWarnings({"unchecked"})
    private ColumnInfo<RowElement, ?>[] createColumnInfos() {
        mapped.clear();
        int num = 0;
        for (JavaeeSection<?> section : sections) {
            JavaeeSectionInfo<DomElement>[] infos = (JavaeeSectionInfo<DomElement>[]) section.createColumnInfos();
            mapped.put(section, infos);
            num = Math.max(num, infos.length);
        }
        ColumnInfo<RowElement, ?>[] infos = new ColumnInfo[num + 1];
        infos[0] = createColumn();
        for (int i = 0; i < num; i++) {
            infos[i + 1] = createColumn(i);
        }
        return infos;
    }

    private ColumnInfo<RowElement, Object> createColumn() {
        return new ColumnInfo<RowElement, Object>(LocalizeValue.empty()) {
            @Override
            @Nullable
            public Object valueOf(RowElement item) {
                return item;
            }

            @Override
            public TableCellRenderer getRenderer(final RowElement item) {
                return new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                        JTable table,
                        Object value,
                        boolean selected,
                        boolean focus,
                        int row,
                        int col
                    ) {
                        return item.getFirstComponent(table, selected, focus);
                    }
                };
            }
        };
    }

    private ColumnInfo<RowElement, String> createColumn(final int index) {
        return new ColumnInfo<RowElement, String>(LocalizeValue.empty()) {
            @Override
            @Nullable
            public String valueOf(RowElement item) {
                return item.getValue(index);
            }

            @Override
            public void setValue(RowElement item, String value) {
                item.setValue(index, value);
            }

            @Override
            public boolean isCellEditable(RowElement item) {
                return item.isEditable(index);
            }

            @Override
            @Nullable
            public TableCellRenderer getRenderer(RowElement item) {
                return item.getRenderer(index);
            }

            @Override
            @Nullable
            public TableCellEditor getEditor(RowElement item) {
                return item.getEditor(index);
            }
        };
    }

    private static Component createLabel(Icon icon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new IconLabel(icon));
        return panel;
    }

    private class SectionTableView extends AbstractTableView<RowElement> {
        SectionTableView(Project project, String empty) {
            super(project, empty, null);
        }

        @Override
        protected void adjustColumnWidths() {
            super.adjustColumnWidths();
            TableColumnModel model = getTable().getColumnModel();
            if (model.getColumnCount() > 0) {
                TableColumn column = model.getColumn(0);
                column.setMinWidth(column.getPreferredWidth());
                column.setMaxWidth(column.getPreferredWidth());
            }
        }

        @Override
        protected void wrapValueSetting(@Nonnull RowElement item, Runnable setter) {
            if (item.getElement() instanceof DomElement domElement && domElement.isValid()) {
                setter.run();
                fireChanged();
            }
        }

        @Override
        protected TableCellRenderer getTableCellRenderer(int row, int col, final TableCellRenderer renderer, Object value) {
            return (table, value1, selected, focus, row1, col1) -> {
                Component component = renderer.getTableCellRendererComponent(table, value1, selected, focus, row1, col1);
                return items.get(row1).decorate((JComponent) component, table, col1, selected);
            };
        }

        void initialize() {
            initializeTable();
        }
    }

    private interface RowElement {
        @Nonnull
        JavaeeSection<?> getSection();

        @Nullable
        Object getElement();

        void handleDoubleClick();

        void handleLeftRight(boolean right);

        Component getFirstComponent(JTable table, boolean selected, boolean focus);

        @Nullable
        String getValue(int column);

        void setValue(int column, String value);

        boolean isEditable(int column);

        @Nullable
        TableCellRenderer getRenderer(int column);

        @Nullable
        TableCellEditor getEditor(int column);

        @Nonnull
        JComponent decorate(JComponent component, JTable table, int column, boolean selected);
    }

    private class SectionRowElement implements RowElement {
        private final JavaeeSection<?> section;

        private SectionRowElement(JavaeeSection<?> section) {
            this.section = section;
        }

        @Nonnull
        @Override
        public JavaeeSection<?> getSection() {
            return section;
        }

        @Nullable
        @Override
        public Object getElement() {
            return null;
        }

        @Override
        public void handleDoubleClick() {
            if (expanded.remove(section) || expanded.add(section)) {
                reset();
            }
        }

        @Override
        public void handleLeftRight(boolean right) {
            if (right && expanded.add(section)) {
                reset();
            }
            else if (!right && expanded.remove(section)) {
                reset();
            }
        }

        @Override
        public Component getFirstComponent(JTable table, boolean selected, boolean focus) {
            return expanded.contains(section) ? EXPANDED_ICON : COLLAPSED_ICON;
        }

        @Nullable
        @Override
        public String getValue(int column) {
            return column == 0 || expanded.contains(section) ? getColumnInfo(section, column).getName().get() : null;
        }

        @Override
        public void setValue(int column, String value) {
        }

        @Override
        public boolean isEditable(int column) {
            return false;
        }

        @Nullable
        @Override
        public TableCellRenderer getRenderer(int column) {
            return null;
        }

        @Nullable
        @Override
        public TableCellEditor getEditor(int column) {
            return null;
        }

        @Nonnull
        @Override
        public JComponent decorate(JComponent component, JTable table, int column, boolean selected) {
            Border border = BorderFactory.createEmptyBorder(0, 5, 0, column == 0 || column == 2 ? 5 : 0);
            Color background = selected
                ? StripeTableCellRenderer.darken(table.getSelectionBackground())
                : new Color(table.getTableHeader().getBackground().getRGB());
            component.setBackground(background);
            border = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, background.darker()), border);
            if (expanded.contains(section)) {
                if (column > 1) {
                    border = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, background.brighter()), border);
                }
                if (column > 0) {
                    border = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, table.getGridColor()), border);
                }
            }
            component.setFont(component.getFont().deriveFont(Font.BOLD));
            component.setBorder(border);
            return component;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this
                || obj instanceof SectionRowElement that
                && section.equals(that.getSection());
        }

        @Override
        public int hashCode() {
            return section.hashCode();
        }
    }

    private class DomRowElement implements RowElement {
        private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

        private final JavaeeSection<?> section;

        private final DomElement element;

        private DomRowElement(JavaeeSection<?> section, DomElement element) {
            this.section = section;
            this.element = element;
        }

        @Nonnull
        @Override
        public JavaeeSection<?> getSection() {
            return section;
        }

        @Nullable
        @Override
        public Object getElement() {
            return element;
        }

        @Override
        public void handleDoubleClick() {
        }

        @Override
        public void handleLeftRight(boolean right) {
        }

        @Override
        public Component getFirstComponent(JTable table, boolean selected, boolean focus) {
            return renderer.getTableCellRendererComponent(table, null, selected, focus, 0, 0);
        }

        @Nullable
        @Override
        public String getValue(int column) {
            return getColumnInfo(section, column).valueOf(element);
        }

        @Override
        public void setValue(int column, String value) {
            getColumnInfo(section, column).setValue(element, value);
        }

        @Override
        public boolean isEditable(int column) {
            return getColumnInfo(section, column).isCellEditable(element);
        }

        @Nullable
        @Override
        public TableCellRenderer getRenderer(int column) {
            return getColumnInfo(section, column).getRenderer(element);
        }

        @Nullable
        @Override
        public TableCellEditor getEditor(int column) {
            return getColumnInfo(section, column).getEditor(element);
        }

        @Nonnull
        @Override
        public JComponent decorate(JComponent component, JTable table, int column, boolean selected) {
            Border border = BorderFactory.createEmptyBorder(0, 5, 0, ((column == 0) || (column == 2)) ? 5 : 0);
            if (column > 0) {
                border = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, table.getGridColor()), border);
            }
            component.setBorder(border);
            return component;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this
                || obj instanceof DomRowElement that
                && section.equals(that.getSection())
                && element.equals(that.getElement());
        }

        @Override
        public int hashCode() {
            return (31 * section.hashCode()) + element.hashCode();
        }
    }

    private static class IconLabel extends JLabel {
        private IconLabel(Icon icon) {
            super(icon);
        }
    }
}
