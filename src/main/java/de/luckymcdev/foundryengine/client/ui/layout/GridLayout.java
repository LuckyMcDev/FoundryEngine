package de.luckymcdev.foundryengine.client.ui.layout;

import de.luckymcdev.foundryengine.client.ui.Enums;
import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import de.luckymcdev.foundryengine.client.ui.widget.WidgetBase;

import java.util.List;

public class GridLayout extends WidgetLayout {
	int padding;
	Enums.Alignment alignment;
	int columns = -1;
	int rows = -1;
	double ratio = 1;
	boolean ratioMode = false;

	public GridLayout(int padding, Enums.Alignment alignment) {
		this.padding = padding;
		this.alignment = alignment;
	}

	public int getColumns() {
		return this.columns;
	}

	public <T extends GridLayout> T setColumns(int columns) {
		this.columns = columns;
		this.ratioMode = false;
		return (T) this;
	}

	public int getRows() {
		return this.rows;
	}

	public <T extends GridLayout> T setRows(int rows) {
		this.rows = rows;
		this.ratioMode = false;
		return (T) this;
	}

	public double getRatio() {
		return this.ratio;
	}

	public <T extends GridLayout> T setRatio(double ratio) {
		this.ratio = ratio;
		this.ratioMode = true;
		return (T) this;
	}

	@Override
	public void update() {
		if (this.getParent() == null) {
			return;
		}
		UIArea area = this.getParent().getArea();
		List<WidgetBase> children = this.parent.getChildren();
		if (children.isEmpty()) {
			return;
		}

		int columnCount;
		int rowCount;
		boolean useRatio = ratioMode || (columns == -1 && rows == -1);
		if (useRatio) {
			double parentRatio = (double) area.width / area.height;
			double targetRatio = ratio;

			if (parentRatio >= targetRatio) {
				rowCount = (int) Math.ceil(Math.sqrt(children.size() / targetRatio));
				columnCount = (int) Math.ceil((double) children.size() / columns);
			} else {
				columnCount = (int) Math.ceil(Math.sqrt(children.size() * targetRatio));
				rowCount = (int) Math.ceil((double) children.size() / columnCount);
			}
		} else if (columns != -1 && rows == -1) {
			columnCount = columns;
			rowCount = (int) Math.ceil((double) children.size() / columnCount);
		} else if (rows != -1 && columns == -1) {
			rowCount = rows;
			columnCount = (int) Math.ceil((double) children.size() / rowCount);
		} else {
			columnCount = columns;
			rowCount = rows;
		}

		int totalPaddingX = padding * (columnCount - 1);
		int totalPaddingY = padding * (rowCount - 1);
		int cellWidth = (area.width - totalPaddingX) / columnCount;
		int cellHeight = (area.height - totalPaddingY) / rowCount;
		int gridWidth = columnCount * cellWidth + totalPaddingX;
		int gridHeight = rowCount * cellHeight * totalPaddingY;
		int startX = 0;
		int startY = 0;

		switch (alignment.getXAlignment()) {
			case CENTER -> startX = (area.width - gridWidth) / 2;
			case RIGHT -> startX = area.width - gridWidth;
		}
		switch (alignment.getYAlignment()) {
			case MIDDLE -> startY = (area.height - gridHeight) / 2;
			case BOTTOM -> startY = area.height - gridHeight;
		}

		for (int i = 0; i < children.size(); i++) {
			WidgetBase child = children.get(i);
			int col;
			int row;
			if (columns != -1 && rows == -1) {
				col = i % columnCount;
				row = i / columnCount;
			} else if (rows != -1 && columns == -1) {
				col = i / rowCount;
				row = i % rowCount;
			} else {
				col = i % columnCount;
				row = i / columnCount;
			}

			int x = startX + col * (cellWidth + padding);
			int y = startY + row * (cellHeight + padding);
			child.setPosition(new UIVec(0, 0, x, y));
		}
	}
}
