package de.luckymcdev.foundryengine.common.md;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.net.URI;
import java.util.Stack;

public class MarkdownComponentVisitor {
	private final Stack<MutableComponent> stack = new Stack<>();
	private int listDepth = 0;
	private int orderedListIndex = 1;
	private final NodeVisitor visitor = new NodeVisitor(
		new VisitHandler<>(Text.class, this::visitText),
		new VisitHandler<>(StrongEmphasis.class, this::visitStrongEmphasis),
		new VisitHandler<>(Emphasis.class, this::visitEmphasis),
		new VisitHandler<>(Strikethrough.class, this::visitStrikethrough),
		new VisitHandler<>(Code.class, this::visitCode),
		new VisitHandler<>(Link.class, this::visitLink),
		new VisitHandler<>(Image.class, this::visitImage),
		new VisitHandler<>(SoftLineBreak.class, this::visitSoftLineBreak),
		new VisitHandler<>(HardLineBreak.class, this::visitHardLineBreak),
		new VisitHandler<>(Paragraph.class, this::visitParagraph),
		new VisitHandler<>(Heading.class, this::visitHeading),
		new VisitHandler<>(FencedCodeBlock.class, this::visitFencedCodeBlock),
		new VisitHandler<>(IndentedCodeBlock.class, this::visitIndentedCodeBlock),
		new VisitHandler<>(BlockQuote.class, this::visitBlockQuote),
		new VisitHandler<>(ThematicBreak.class, this::visitThematicBreak),
		new VisitHandler<>(BulletList.class, this::visitBulletList),
		new VisitHandler<>(OrderedList.class, this::visitOrderedList),
		new VisitHandler<>(BulletListItem.class, this::visitBulletListItem),
		new VisitHandler<>(OrderedListItem.class, this::visitOrderedListItem),
		new VisitHandler<>(TaskListItem.class, this::visitTaskListItem),
		new VisitHandler<>(TableBlock.class, this::visitTableBlock),
		new VisitHandler<>(TableHead.class, this::visitTableHead),
		new VisitHandler<>(TableBody.class, this::visitTableBody),
		new VisitHandler<>(TableRow.class, this::visitTableRow),
		new VisitHandler<>(TableCell.class, this::visitTableCell),
		new VisitHandler<>(HtmlBlock.class, this::visitHtmlBlock),
		new VisitHandler<>(HtmlInline.class, this::visitHtmlInline),
		new VisitHandler<>(MailLink.class, this::visitMailLink)
	);

	public MarkdownComponentVisitor() {
		stack.push(Component.literal(""));
	}

	public MutableComponent getComponent() {
		return stack.peek();
	}

	public void visit(Node document) {
		visitor.visit(document);
	}

	private void visitText(Text node) {
		stack.peek().append(node.getChars().toString());
	}

	private void visitStrongEmphasis(StrongEmphasis node) {
		MutableComponent c = Component.literal("");
		stack.push(c);
		visitor.visitChildren(node);
		stack.pop();
		stack.peek().append(c.withStyle(s -> s.withBold(true)));
	}

	private void visitEmphasis(Emphasis node) {
		MutableComponent c = Component.literal("");
		stack.push(c);
		visitor.visitChildren(node);
		stack.pop();
		stack.peek().append(c.withStyle(s -> s.withItalic(true)));
	}

	private void visitStrikethrough(Strikethrough node) {
		MutableComponent c = Component.literal("");
		stack.push(c);
		visitor.visitChildren(node);
		stack.pop();
		stack.peek().append(c.withStyle(s -> s.withStrikethrough(true)));
	}

	private void visitCode(Code node) {
		stack.peek().append(
			Component.literal(node.getText().toString())
				.withStyle(s -> s.withColor(0xE6E6E6))
		);
	}

	private void visitLink(Link node) {
		MutableComponent c = Component.literal("");
		stack.push(c);
		visitor.visitChildren(node);
		stack.pop();

		String url = node.getUrl().toString();
		String title = node.getTitle().toString();

		Component hover = !title.isEmpty()
			? Component.literal(title + "\n" + url)
			: Component.literal(url);

		stack.peek().append(c.withStyle(s -> s
			.withUnderlined(true)
			.withColor(0x5555FF)
			.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
			.withHoverEvent(new HoverEvent.ShowText(hover))
		));
	}

	private void visitImage(Image node) {
		String url = node.getUrl().toString();
		String title = node.getTitle().toString();

		MutableComponent alt = Component.literal("");
		stack.push(alt);
		visitor.visitChildren(node);
		stack.pop();

		String altText = alt.getString();
		String display = "[Image: " + (altText.isEmpty() ? "image" : altText) + "]";
		Component hover = Component.literal((!title.isEmpty() ? title + "\n" : "") + "URL: " + url);

		stack.peek().append(
			Component.literal(display).withStyle(s -> s
				.withColor(0xFF55FF)
				.withItalic(true)
				.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
				.withHoverEvent(new HoverEvent.ShowText(hover))
			)
		);
	}

	private void visitMailLink(MailLink node) {
		String address = node.getText().toString();
		stack.peek().append(
			Component.literal(address).withStyle(s -> s
				.withUnderlined(true)
				.withColor(0x5555FF)
				.withClickEvent(new ClickEvent.OpenUrl(URI.create("mailto:" + address)))
				.withHoverEvent(new HoverEvent.ShowText(Component.literal("mailto:" + address)))
			)
		);
	}

	private void visitSoftLineBreak(SoftLineBreak node) {
		stack.peek().append(" ");
	}

	private void visitHardLineBreak(HardLineBreak node) {
		stack.peek().append("\n");
	}

	private void visitParagraph(Paragraph node) {
		MutableComponent c = Component.literal("");
		stack.push(c);
		visitor.visitChildren(node);
		stack.pop();
		c.append("\n");
		stack.peek().append(c);
	}

	private void visitHeading(Heading node) {
		MutableComponent c = Component.literal("");
		stack.push(c);
		visitor.visitChildren(node);
		stack.pop();

		int level = node.getLevel();
		MutableComponent styled = c.withStyle(s -> {
			var bold = s.withBold(true);
			return switch (level) {
				case 2 -> bold.withColor(0xEEEEEE);
				case 3 -> bold.withColor(0xDDDDDD);
				case 4 -> bold.withColor(0xCCCCCC);
				case 5 -> bold.withColor(0xBBBBBB);
				case 6 -> bold.withColor(0xAAAAAA);
				default -> bold.withColor(0xFFFFFF);
			};
		});
		styled.append("\n");
		stack.peek().append(styled);
	}

	private void visitFencedCodeBlock(FencedCodeBlock node) {
		String info = node.getInfo().toString();

		if (!info.isEmpty()) {
			stack.peek().append(
				Component.literal("```" + info)
					.withStyle(s -> s.withColor(0x888888).withItalic(true))
			).append("\n");
		}

		stack.peek().append(
			Component.literal(node.getContentChars().toString().stripTrailing())
				.withStyle(s -> s.withColor(0xE6E6E6))
		);

		if (!info.isEmpty()) {
			stack.peek().append(
				Component.literal("```")
					.withStyle(s -> s.withColor(0x888888).withItalic(true))
			);
		}
		stack.peek().append("\n");
	}

	private void visitIndentedCodeBlock(IndentedCodeBlock node) {
		stack.peek().append(
			Component.literal(node.getContentChars().toString().stripTrailing())
				.withStyle(s -> s.withColor(0xE6E6E6))
		).append("\n");
	}

	private void visitBlockQuote(BlockQuote node) {
		MutableComponent inner = Component.literal("");
		stack.push(inner);
		visitor.visitChildren(node);
		stack.pop();

		for (String line : inner.getString().split("\n", -1)) {
			if (!line.isEmpty()) {
				stack.peek()
					.append(Component.literal("│ ").withStyle(s -> s.withColor(0x888888)))
					.append(Component.literal(line).withStyle(s -> s.withItalic(true).withColor(0xCCCCCC)))
					.append("\n");
			}
		}
		stack.peek().append("\n");
	}

	private void visitThematicBreak(ThematicBreak node) {
		stack.peek().append(
			Component.literal("─".repeat(50)).withStyle(s -> s.withColor(0x888888))
		).append("\n\n");
	}

	private void visitBulletList(BulletList node) {
		listDepth++;
		visitor.visitChildren(node);
		listDepth--;
		if (listDepth == 0) stack.peek().append("\n");
	}

	private void visitOrderedList(OrderedList node) {
		listDepth++;
		int saved = orderedListIndex;
		orderedListIndex = node.getStartNumber();
		visitor.visitChildren(node);
		orderedListIndex = saved;
		listDepth--;
		if (listDepth == 0) stack.peek().append("\n");
	}

	private void visitBulletListItem(BulletListItem node) {
		visitListItemInternal(node, "• ");
	}

	private void visitOrderedListItem(OrderedListItem node) {
		String marker = orderedListIndex + ". ";
		orderedListIndex++;
		visitListItemInternal(node, marker);
	}

	private void visitTaskListItem(TaskListItem node) {
		String marker = node.isItemDoneMarker() ? "☑ " : "☐ ";
		visitListItemInternal(node, marker);
	}

	private void visitListItemInternal(ListItem node, String marker) {
		String indent = "  ".repeat(Math.max(0, listDepth - 1));
		MutableComponent item = Component.literal(indent + marker);
		stack.push(item);

		Node child = node.getFirstChild();
		boolean first = true;
		while (child != null) {
			if (child instanceof Paragraph && first) {
				MutableComponent para = Component.literal("");
				stack.push(para);
				visitor.visitChildren(child);
				stack.pop();
				item.append(para);
			} else {
				visitor.visit(child);
			}
			first = false;
			child = child.getNext();
		}

		stack.pop();
		item.append("\n");
		stack.peek().append(item);
	}

	private void visitTableBlock(TableBlock node) {
		stack.peek().append("\n");
		visitor.visitChildren(node);
		stack.peek().append("\n");
	}

	private void visitTableHead(TableHead node) {
		visitor.visitChildren(node);

		TableRow row = (TableRow) node.getFirstChild();
		if (row != null) {
			int cols = countCells(row);
			stack.peek().append(
				Component.literal("─".repeat(cols * 15)).withStyle(s -> s.withColor(0x888888))
			).append("\n");
		}
	}

	private void visitTableBody(TableBody node) {
		visitor.visitChildren(node);
	}

	private void visitTableRow(TableRow node) {
		MutableComponent row = Component.literal("│ ");
		stack.push(row);
		visitor.visitChildren(node);
		stack.pop();
		stack.peek().append(row).append("\n");
	}

	private void visitTableCell(TableCell node) {
		MutableComponent cell = Component.literal("");
		stack.push(cell);
		visitor.visitChildren(node);
		stack.pop();

		boolean header = node.getParent() != null && node.getParent().getParent() instanceof TableHead;
		if (header) cell = cell.withStyle(s -> s.withBold(true));

		int padding = Math.max(0, 12 - cell.getString().length());
		stack.peek().append(
			Component.literal("").append(cell).append(" ".repeat(padding)).append(" │ ")
		);
	}

	private int countCells(TableRow row) {
		int count = 0;
		Node child = row.getFirstChild();
		while (child != null) {
			if (child instanceof TableCell) count++;
			child = child.getNext();
		}
		return count;
	}

	private void visitHtmlBlock(HtmlBlock node) {
		stack.peek().append(
			Component.literal("[HTML]\n" + node.getChars())
				.withStyle(s -> s.withColor(0xFFAA00).withItalic(true))
		).append("\n");
	}

	private void visitHtmlInline(HtmlInline node) {
		stack.peek().append(
			Component.literal(node.getChars().toString())
				.withStyle(s -> s.withColor(0xFFAA00))
		);
	}
}