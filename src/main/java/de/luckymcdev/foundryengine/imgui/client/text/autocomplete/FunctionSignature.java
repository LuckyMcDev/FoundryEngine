package de.luckymcdev.foundryengine.imgui.client.text.autocomplete;

public record FunctionSignature(String name, String returnType, String... params) {

	public String format() {
		return returnType + " " + name + "(" + String.join(", ", params) + ")";
	}
}