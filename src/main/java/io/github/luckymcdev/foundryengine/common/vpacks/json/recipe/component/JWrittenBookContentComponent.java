package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JWrittenBookContentComponent extends JCodecBuilderComponent<WrittenBookContent> {
    private final List<Filterable<Component>> pages = new ArrayList<>();
    private Filterable<String> title;
    private String author;
    private int generation = 0;
    private boolean resolved = false;

    public JWrittenBookContentComponent() {
        super(WrittenBookContent.CODEC);
    }

    public JWrittenBookContentComponent title(String title) {
        this.title = new Filterable<>(title, Optional.empty());
        return this;
    }

    public JWrittenBookContentComponent title(Filterable<String> title) {
        this.title = title;
        return this;
    }

    public JWrittenBookContentComponent author(String author) {
        this.author = author;
        return this;
    }

    public JWrittenBookContentComponent generation(int generation) {
        this.generation = generation;
        return this;
    }

    public JWrittenBookContentComponent page(Component page) {
        this.pages.add(new Filterable<>(page, Optional.empty()));
        return this;
    }

    public JWrittenBookContentComponent page(Filterable<Component> page) {
        this.pages.add(page);
        return this;
    }

    public JWrittenBookContentComponent resolved(boolean resolved) {
        this.resolved = resolved;
        return this;
    }

    @Override
    public WrittenBookContent manuallyBuild() {
        return new WrittenBookContent(this.title, this.author, this.generation, this.pages, this.resolved);
    }
}
