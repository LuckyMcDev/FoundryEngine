# FoundryEngine
### A Mod that tries to turn minecraft into a Game Engine.

[Docs](docs/generated/html/index.html)

Icon by game-icons.net

TODO:

Dear ImGui Assertion Failed: Glyphs.Size > 0 && "Font has not loaded glyph!"
Assertion Located At: /tmp/imgui/jni/imgui_draw.cpp:3626
java.lang.Exception: Stack trace
at java.base/java.lang.Thread.dumpStack(Thread.java:2177)
at imgui.ImGui$1.imAssertCallback(ImGui.java:63)
at imgui.assertion.ImAssertCallback.imAssert(ImAssertCallback.java:21)
at imgui.ImFontAtlas.nGetTexDataAsRGBA32(Native Method)
at imgui.ImFontAtlas.getTexDataAsRGBA32(ImFontAtlas.java:544)
at imgui.ImFontAtlas.getTexDataAsRGBA32(ImFontAtlas.java:537)
at TRANSFORMER/foundryengine@1.0.1/de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3.createFontsTexture(ImGuiImplGl3.java:474)
at TRANSFORMER/foundryengine@1.0.1/de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3.createDeviceObjects(ImGuiImplGl3.java:608)
at TRANSFORMER/foundryengine@1.0.1/de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3.newFrame(ImGuiImplGl3.java:235)
at TRANSFORMER/foundryengine@1.0.1/de.luckymcdev.foundryengine.client.imgui.ImGuiManager.begin(ImGuiManager.java:162)
at TRANSFORMER/minecraft@1.21.11/net.minecraft.client.renderer.GameRenderer.handler$zze000$foundryengine$tb$
renderReturn(GameRenderer.java:1003)
at TRANSFORMER/minecraft@1.21.11/net.minecraft.client.renderer.GameRenderer.render(GameRenderer.java:607)
at TRANSFORMER/minecraft@1.21.11/net.minecraft.client.Minecraft.runTick(Minecraft.java:1345)
at TRANSFORMER/minecraft@1.21.11/net.minecraft.client.Minecraft.run(Minecraft.java:924)
at TRANSFORMER/minecraft@1.21.11/net.minecraft.client.main.Main.main(Main.java:229)
at net.neoforged.fml.startup.Client.main(Client.java:19)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
at java.base/java.lang.reflect.Method.invoke(Method.java:580)
at net.neoforged.devlaunch.Main.main(Main.java:57)
2026-03-04T18:14:48.376054Z Server thread WARN Error parsing URI C:
\\Data\\Projects\\FoundryEngine\\build\\moddev\\clientLog4j2.xml
