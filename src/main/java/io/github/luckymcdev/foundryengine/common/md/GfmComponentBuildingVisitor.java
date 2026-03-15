package io.github.luckymcdev.foundryengine.common.md;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.Node;

public class GfmComponentBuildingVisitor extends ComponentBuildingVisitor {

    public void visit(Strikethrough strikethrough) {
        MutableComponent parent = Component.literal("");
        stack.push(parent);
        visitChildren(strikethrough);
        stack.pop();

        MutableComponent strikethroughComp = parent.withStyle(style -> style.withStrikethrough(true));

        stack.peek().append(strikethroughComp);
    }

    public void visit(TableBlock tableBlock) {
        stack.peek().append("\n");
        visitChildren(tableBlock);
        stack.peek().append("\n");
    }

    public void visit(TableHead tableHead) {
        visitChildren(tableHead);

        TableRow row = (TableRow) tableHead.getFirstChild();
        if (row != null) {
            int cellCount = countCells(row);
            MutableComponent separator = Component.literal("─".repeat(cellCount * 15))
                    .withStyle(style -> style.withColor(0x888888));
            stack.peek().append(separator).append("\n");
        }
    }

    public void visit(TableBody tableBody) {
        visitChildren(tableBody);
    }

    public void visit(TableRow tableRow) {
        MutableComponent rowComp = Component.literal("│ ");
        stack.push(rowComp);
        visitChildren(tableRow);
        stack.pop();
        stack.peek().append(rowComp).append("\n");
    }

    public void visit(TableCell tableCell) {
        MutableComponent cellComp = Component.literal("");
        stack.push(cellComp);
        visitChildren(tableCell);
        stack.pop();

        boolean isHeader = tableCell.getParent().getParent() instanceof TableHead;

        if (isHeader) {
            cellComp = cellComp.withStyle(style -> style.withBold(true));
        }

        String cellText = cellComp.getString();
        int padding = Math.max(0, 12 - cellText.length());

        MutableComponent paddedCell = Component.literal("")
                .append(cellComp)
                .append(" ".repeat(padding))
                .append(" │ ");

        stack.peek().append(paddedCell);
    }


    private int countCells(TableRow row) {
        int count = 0;
        Node child = row.getFirstChild();
        while (child != null) {
            if (child instanceof TableCell) {
                count++;
            }
            child = child.getNext();
        }
        return count;
    }

    @Override
    protected void visitChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();

            switch (node) {
                case Strikethrough strikethrough -> visit(strikethrough);
                case TableBlock tableBlock -> visit(tableBlock);
                case TableHead tableHead -> visit(tableHead);
                case TableBody tableBody -> visit(tableBody);
                case TableRow tableRow -> visit(tableRow);
                case TableCell tableCell -> visit(tableCell);
                default -> node.accept(this);
            }

            node = next;
        }
    }
}