package de.luckymcdev.foundryengine.client;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.debug.renderer.SimpleDebugScreenRenderer;
import de.luckymcdev.foundryengine.client.debug.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.client.debug.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.client.debug.screen.PostProcessDebugEntry;
import de.luckymcdev.foundryengine.client.editor.builtin.MainEditor;
import de.luckymcdev.foundryengine.client.editor.builtin.TestPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.console.ConsolePanel;
import de.luckymcdev.foundryengine.client.editor.builtin.explorer.FileExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.explorer.ResourceExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.tools.MinecraftToolsPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.visuals.PostProcessPanel;
import de.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import de.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import de.luckymcdev.foundryengine.client.ext.ModPathBroadcaster;
import de.luckymcdev.foundryengine.client.opengl.preprocessing.IncludeGLSLPreProcessor;
import de.luckymcdev.foundryengine.client.opengl.preprocessing.RegisterGLSLPreProcessorEvent;
import de.luckymcdev.foundryengine.client.particle.EngineParticleProvider;
import de.luckymcdev.foundryengine.client.particle.EngineParticles;
import de.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import de.luckymcdev.foundryengine.client.post.pipeline.builtin.*;
import de.luckymcdev.foundryengine.client.util.key.RegisterKeyBindingEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.Config;
import net.minecraft.client.particle.Particle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(value = Common.MODID, dist = Dist.CLIENT)
public class FoundryEngineModClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;

    private final int tickCount = 0;

    public FoundryEngineModClient(IEventBus modBus, ModContainer modContainer) {
        EngineParticles.PARTICLE_TYPES.register(modBus);

        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::addClientReloadListener);
        modBus.addListener(this::onRegisterKeyMapping);
        modBus.addListener(this::onRegisterDebugEntry);
        modBus.addListener(this::onRegisterDebugRenderers);
        modBus.addListener(this::registerParticles);
        modBus.addListener(this::onRegisterClientPayloadHandlers);

        BUS.addListener(this::onRegisterKeyBinding);
        BUS.addListener(this::onRegisterGLSLPreProcessors);
        BUS.addListener(this::onRegisterPanels);
        BUS.addListener(this::onRegisterPostPipelines);
        BUS.addListener(this::onClientTick);
        //BUS.addListener(this::onFrameGraphSetup);

        Config.registerClient(modContainer);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.debug("FoundryEngineModClient setup called");

        ModPathBroadcaster.onClientSetup();

        event.enqueueWork(() -> {
            BUS.post(new RegisterRenderingStuffEvent(Client.getResourceManager()));
            BUS.post(new RegisterGLSLPreProcessorEvent());
            BUS.post(new RegisterPanelEvent());
        });
    }

    private void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        BUS.post(new RegisterKeyBindingEvent(Client.getKeyBindingManager()));
        Client.getKeyBindingManager().getKeyBindings().forEach(keyBinding ->
                event.register(keyBinding.mapping())
        );
    }

    private void onRegisterKeyBinding(RegisterKeyBindingEvent event) {
        event.register(Client.EDITOR_KEY);
    }

    private void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
    }

    private void onRegisterGLSLPreProcessors(RegisterGLSLPreProcessorEvent event) {
        event.register(new IncludeGLSLPreProcessor());
    }

    private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
        event.register(Common.id("bundles_info"), new BundleDebugEntry(Common.getBundleManager()));
        event.register(Common.id("post_info"), new PostProcessDebugEntry(Client.getPostProcessManager()));
        event.register(Common.id("gamestages_info"), new GameStagesDebugEntry());
    }

    private void onRegisterDebugRenderers(RegisterDebugRenderersEvent event) {
//        event.register(minecraft -> new SimpleDebugScreenRenderer(
//                minecraft,
//                (mc, camPos, debug, frustum, partialTick) -> {
//                    Vector3d forward = camPos.add(
//                            mc.player.getViewVector(partialTick).x * 2,
//                            mc.player.getViewVector(partialTick).y * 2,
//                            mc.player.getViewVector(partialTick).z * 2,
//                            new Vector3d()
//                    );
//                    Gizmos.point(new Vec3(forward.x, forward.y, forward.z), 0xFF00FF00, 10F);
//                }
//        ));

        event.register(minecraft -> new SimpleDebugScreenRenderer(
                minecraft,
                (mc, camPos, debugValueAccess, frustum, partialTick) -> {
                    camPos.add(0, 2, 0);
                    Gizmos.billboardText(
                            "TEST",
                            new Vec3(camPos.x(), camPos.y(), camPos.z()),
                            TextGizmo.Style.forColorAndCentered(0xFF00FF).withScale(2F));
                }
        ));

    }

    private void onRegisterPanels(RegisterPanelEvent event) {
        event.register(PostProcessPanel.INSTANCE);
        event.register(TestPanel.INSTANCE);
        event.register(FileExplorerPanel.INSTANCE);
        event.register(ResourceExplorerPanel.INSTANCE);
        event.register(ConsolePanel.INSTANCE);
        event.register(MainEditor.INSTANCE);
        event.register(MinecraftToolsPanel.INSTANCE);
    }

    private void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
        event.register(new GrayscalePipeline());
        event.register(new DepthVisualizePipeline());
        event.register(new AsciiPostProcessPipeline());
        event.register(new UpsideDownPipeline());
        event.register(new InvertedColorsPipeline());
        event.register(new CRTScanlinePipeline());
    }

    private void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                EngineParticles.ENGINE_PARTICLE.get(),
                EngineParticleProvider::new
        );
    }

    private void addClientReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(Common.id("imgui_handler"), Client.getImGuiManager());
        event.addListener(Common.id("shader_manager"), Client.getShaderManager());
        event.addListener(Common.id("post_pipeline_init"), (ResourceManagerReloadListener) rm ->
                BUS.post(new RegisterPostPipelineEvent(Client.getPostProcessManager()))
        );
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Client.getEditorManager().handleTick();

        Particle particle = Client.getMinecraft().particleEngine.createParticle(
                EngineParticles.ENGINE_PARTICLE.get(),
                0, 100, 0,
                0, 0, 0
        );
        if (null == particle) return;

        particle.setParticleSpeed(0, 1, 0);

        Client.getMinecraft().particleEngine.add(particle);
    }

    /*
    Soo, we dont have the getProjectionMatrix() anymore.
    TODO: fix this
    private void onFrameGraphSetup(FrameGraphSetupEvent event) {
        Client.updateMain(event.getModelViewMatrix(), event.getProjectionMatrix());
    }
     */
}
