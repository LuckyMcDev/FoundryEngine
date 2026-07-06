package de.luckymcdev.foundryengine.client.ui;

public class Enums {
	public enum MouseButton {
		LEFT(0),
		RIGHT(1),
		MIDDLE(2);

		final int button;

		MouseButton(int button) {
			this.button = button;
		}

		public int button() {
			return this.button;
		}
	}

	public enum Alignment {
		TOP_LEFT(XAlignment.LEFT, YAlignment.TOP),
		TOP(XAlignment.CENTER, YAlignment.TOP),
		TOP_RIGHT(XAlignment.RIGHT, YAlignment.TOP),
		LEFT(XAlignment.LEFT, YAlignment.MIDDLE),
		CENTER(XAlignment.CENTER, YAlignment.MIDDLE),
		RIGHT(XAlignment.RIGHT, YAlignment.MIDDLE),
		BOTTOM_LEFT(XAlignment.LEFT, YAlignment.BOTTOM),
		BOTTOM(XAlignment.CENTER, YAlignment.BOTTOM),
		BOTTOM_RIGHT(XAlignment.RIGHT, YAlignment.BOTTOM);

		final XAlignment xAlignment;
		final YAlignment yAlignment;

		Alignment(XAlignment xAlignment, YAlignment yAlignment) {
			this.xAlignment = xAlignment;
			this.yAlignment = yAlignment;
		}

		public XAlignment getXAlignment() {
			return xAlignment;
		}

		public YAlignment getYAlignment() {
			return yAlignment;
		}
	}

	public enum XAlignment {
		LEFT,
		CENTER,
		RIGHT
	}

	public enum YAlignment {
		TOP,
		MIDDLE,
		BOTTOM
	}
}
