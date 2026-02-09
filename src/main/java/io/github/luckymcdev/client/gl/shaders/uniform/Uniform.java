package io.github.luckymcdev.client.gl.shaders.uniform;

public class Uniform<V> {

    private final String name;
    private final V value;

    public Uniform(String name, V value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public V value() {
        return value;
    }
}
