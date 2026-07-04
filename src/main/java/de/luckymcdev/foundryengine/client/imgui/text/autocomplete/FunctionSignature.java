package de.luckymcdev.foundryengine.client.imgui.text.autocomplete;

public record FunctionSignature(String name, String returnType, String... params) {

	public String format() {
		return returnType + " " + name + "(" + String.join(", ", params) + ")";
	}
}