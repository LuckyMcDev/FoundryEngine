package io.github.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.luckymcdev.foundryengine.common.md.EngineMarkdownParser;
import io.github.luckymcdev.foundryengine.common.md.MdScreen;
import io.github.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TestCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("test")
                .requires(this::isAdmin)
                .executes(context -> {
                    CommandSourceStack source = context.getSource();

                    MutableComponent parsedMarkdown = EngineMarkdownParser.parse("""
                            # FoundryEngine Markdown Test
                            
                            Welcome to the **FoundryEngine Markdown Viewer**.
                            
                            This screen renders *GitHub Flavored Markdown (GFM)* inside a Minecraft GUI.
                            
                            ---
                            
                            ## Text Formatting
                            
                            Normal text
                            
                            **Bold text**
                            
                            *Italic text*
                            
                            ***Bold + Italic***
                            
                            ~~Strikethrough~~
                            
                            `Inline code example`
                            
                            ---
                            
                            ## Links
                            
                            [FoundryEngine GitHub](https://github.com)
                            
                            [Open Minecraft Wiki](https://minecraft.wiki)
                            
                            ---
                            
                            ## Lists
                            
                            ### Bullet list
                            
                            - Item one
                            - Item two
                            - Item three
                              - Nested item
                              - Another nested item
                                - Deep nesting
                            
                            ### Ordered list
                            
                            1. First step
                            2. Second step
                            3. Third step
                            
                            ---
                            
                            ## Task List (GFM)
                            
                            - [x] Implement Markdown parser
                            - [x] Render components
                            - [ ] Add scrolling
                            - [ ] Add clickable links
                            
                            ---
                            
                            ## Blockquote
                            
                            > This is a blockquote.
                            >
                            > It can span multiple lines and is often used for notes.
                            >
                            >> Nested quote!
                            
                            ---
                            
                            ## Code Block
                            
                            ```java
                            public class HelloWorld {
                            
                                public static void main(String[] args) {
                                    System.out.println("Hello from FoundryEngine!");
                                }
                            
                            }
                            
                            """);

                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(new MdScreen(Component.literal("Markdown Screen"), parsedMarkdown))
                    );

                    return 1;
                });
    }
}