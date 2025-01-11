package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.registries.blockentityrenderers.CircuitBlockEntityRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class EntityRenderers {
    public static void registerEntityRenderers(IEventBus bus) {
        bus.addListener(EntityRenderers::registerBlockEntityRenderer);
    }

    private static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BlockEntities.CIRCUIT.get(),
                CircuitBlockEntityRenderer::new
        );
    }
}
