package io.github.luckymcdev.foundryengine.common.md;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.commonmark.node.*;

import java.net.URI;
import java.util.Stack;

public class ComponentBuildingVisitor extends AbstractVisitor {
    protected final Stack<MutableComponent> stack = new Stack<>();
    protected int listDepth = 0;
    protected int orderedListIndex = 1;

    public ComponentBuildingVisitor() {
        stack.push(Component.literal(""));
    }

    public MutableComponent getComponent() {
        return stack.peek();
    }

    @Override
    public void visit(Text text) {
        stack.peek().append(text.getLiteral());
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        MutableComponent parent = Component.literal("");
        stack.push(parent);
        visitChildren(strongEmphasis);
        stack.pop();
        parent = parent.withStyle(style -> style.withBold(true));
        stack.peek().append(parent);
    }

    @Override
    public void visit(Emphasis emphasis) {
        MutableComponent parent = Component.literal("");
        stack.push(parent);
        visitChildren(emphasis);
        stack.pop();
        parent = parent.withStyle(style -> style.withItalic(true));
        stack.peek().append(parent);
    }

    @Override
    public void visit(Code code) {
        MutableComponent codeComponent = Component.literal(code.getLiteral())
                .withStyle(style -> style.withColor(0xE6E6E6));
        stack.peek().append(codeComponent);
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        String info = fencedCodeBlock.getInfo();

        if (info != null && !info.isEmpty()) {
            MutableComponent label = Component.literal(info)
                    .withStyle(style -> style.withColor(0x888888).withItalic(true));
            stack.peek().append(label);
            stack.peek().append("\n");
        }

        MutableComponent codeBlock = Component.literal(fencedCodeBlock.getLiteral())
                .withStyle(style -> style.withColor(0xE6E6E6));
        stack.peek().append(codeBlock);
        stack.peek().append("\n");
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        MutableComponent codeBlock = Component.literal(indentedCodeBlock.getLiteral())
                .withStyle(style -> style.withColor(0xE6E6E6));
        stack.peek().append(codeBlock);
        stack.peek().append("\n");
    }

    @Override
    public void visit(Paragraph paragraph) {
        MutableComponent para = Component.literal("");
        stack.push(para);
        visitChildren(paragraph);
        stack.pop();
        para.append("\n");
        stack.peek().append(para);
    }

    @Override
    public void visit(Heading heading) {
        MutableComponent headingComponent = Component.literal("");
        stack.push(headingComponent);
        visitChildren(heading);
        stack.pop();
        int level = heading.getLevel();

        headingComponent = headingComponent.withStyle(style -> {
            var styledStyle = style.withBold(true);

            switch (level) {
                case 2:
                    return styledStyle.withColor(0xEEEEEE);
                case 3:
                    return styledStyle.withColor(0xDDDDDD);
                case 4:
                    return styledStyle.withColor(0xCCCCCC);
                case 5:
                    return styledStyle.withColor(0xBBBBBB);
                case 6:
                    return styledStyle.withColor(0xAAAAAA);
                default:
                    return styledStyle.withColor(0xFFFFFF);
            }
        });

        headingComponent.append("\n");
        stack.peek().append(headingComponent);
    }

    @Override
    public void visit(Link link) {
        MutableComponent linkComponent = Component.literal("");
        stack.push(linkComponent);
        visitChildren(link);
        stack.pop();

        String destination = link.getDestination();
        String title = link.getTitle();

        Component hoverText = title != null && !title.isEmpty()
                ? Component.literal(title + "\n" + destination)
                : Component.literal(destination);

        linkComponent = linkComponent.withStyle(style -> style
                .withUnderlined(true)
                .withColor(0x5555FF)
                .withClickEvent(new ClickEvent.OpenUrl(URI.create(destination)))
                .withHoverEvent(new HoverEvent.ShowText(hoverText))
        );
        stack.peek().append(linkComponent);
    }

    @Override
    public void visit(Image image) {
        String destination = image.getDestination();
        String title = image.getTitle();

        MutableComponent imageAlt = Component.literal("");
        stack.push(imageAlt);
        visitChildren(image);
        stack.pop();

        String altText = imageAlt.getString();
        String displayText = "[Image: " + (altText.isEmpty() ? "image" : altText) + "]";

        Component hoverText = Component.literal(
                (title != null && !title.isEmpty() ? title + "\n" : "") +
                        "URL: " + destination
        );

        MutableComponent imageComponent = Component.literal(displayText)
                .withStyle(style -> style
                        .withColor(0xFF55FF)
                        .withItalic(true)
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(destination)))
                        .withHoverEvent(new HoverEvent.ShowText(hoverText))
                );

        stack.peek().append(imageComponent);
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        stack.peek().append(" ");
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        stack.peek().append("\n");
    }

    @Override
    public void visit(BulletList bulletList) {
        listDepth++;
        visitChildren(bulletList);
        listDepth--;
        if (listDepth == 0) {
            stack.peek().append("\n");
        }
    }

    @Override
    public void visit(OrderedList orderedList) {
        listDepth++;
        int savedIndex = orderedListIndex;
        orderedListIndex = orderedList.getMarkerStartNumber();
        visitChildren(orderedList);
        orderedListIndex = savedIndex;
        listDepth--;
        if (listDepth == 0) {
            stack.peek().append("\n");
        }
    }

    @Override
    public void visit(ListItem listItem) {
        String indent = "  ".repeat(Math.max(0, listDepth - 1));

        Node parent = listItem.getParent();
        String marker;
        if (parent instanceof OrderedList) {
            marker = orderedListIndex + ". ";
            orderedListIndex++;
        } else {
            marker = "• ";
        }

        MutableComponent item = Component.literal(indent + marker);
        stack.push(item);

        Node child = listItem.getFirstChild();
        boolean firstChild = true;
        while (child != null) {
            if (child instanceof Paragraph && firstChild) {
                MutableComponent para = Component.literal("");
                stack.push(para);
                visitChildren(child);
                stack.pop();
                item.append(para);
            } else {
                child.accept(this);
            }
            firstChild = false;
            child = child.getNext();
        }

        stack.pop();
        item.append("\n");
        stack.peek().append(item);
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        MutableComponent quote = Component.literal("");
        stack.push(quote);
        visitChildren(blockQuote);
        stack.pop();

        String quoteText = quote.getString();
        String[] lines = quoteText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isEmpty() || i < lines.length - 1) {
                stack.peek().append(Component.literal("│ ")
                        .withStyle(style -> style.withColor(0x888888)));
                stack.peek().append(Component.literal(lines[i])
                        .withStyle(style -> style.withItalic(true).withColor(0xCCCCCC)));
                stack.peek().append("\n");
            }
        }
        stack.peek().append("\n");
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        MutableComponent hr = Component.literal("─".repeat(50))
                .withStyle(style -> style.withColor(0x888888));
        stack.peek().append(hr);
        stack.peek().append("\n\n");
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        // Display HTML as-is with special formatting
        MutableComponent html = Component.literal("[HTML]\n" + htmlBlock.getLiteral())
                .withStyle(style -> style.withColor(0xFFAA00).withItalic(true));
        stack.peek().append(html);
        stack.peek().append("\n");
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        MutableComponent html = Component.literal(htmlInline.getLiteral())
                .withStyle(style -> style.withColor(0xFFAA00));
        stack.peek().append(html);
    }
}