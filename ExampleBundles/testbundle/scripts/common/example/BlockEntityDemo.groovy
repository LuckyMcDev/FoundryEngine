package common.example

import de.luckymcdev.foundryengine.client.render.blockentity.EngineBlockEntityRenderers
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.common.builder.blockentity.BlockEntityBuilder
import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.BlockHitResult

class BlockEntityDemo implements BundleEntrypoint {

    static final String BUNDLEID = "testbundle"

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(BUNDLEID, path)
    }

    private static final BlockEntityBuilder<?> COUNTER_BE = BlockEntityBuilder.create(id("counter"))
            .tick { level, pos, state, be ->
                // read stored value, tick logic
                println "Counter BE ticking at $pos"
            }
            .onLoad { input ->
                println "Counter BE loaded"
            }
            .onSave { output ->
                println "Counter BE saved"
            }
            .renderer(EngineBlockEntityRenderers.noop())

    private static final BlockBuilder COUNTER_BLOCK = BlockBuilder.create(id("counter_block"))
            .properties { p -> p.strength(2.0f, 3.0f) }
            .blockEntity(COUNTER_BE)
            .use { state, level, pos, player, hitResult ->
                if (!level.isClientSide()) {
                    player.sendSystemMessage(Component.literal("Counter block clicked!"))
                }
                return InteractionResult.SUCCESS
            }

    @Override
    void onLoad() {
        BundleEvents.registry {
            it.blocks(COUNTER_BLOCK)
        }
    }

    @Override
    void onUnload() {
    }
}
