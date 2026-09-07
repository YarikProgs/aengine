package net.aros.engine.client;

import net.aros.engine.client.init.AeKeybinds;
import net.aros.engine.client.init.AeUIs;
import net.aros.engine.client.init.AeValuesUIProviders;
import net.aros.engine.client.script.events.ClientEventSubscriptions;
import net.aros.engine.client.script.procedures.ClientProcedureDecoding;
import net.aros.engine.client.script.procedures.ClientProcedures;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.network.ProcedureCallPayload;
import net.aros.engine.common.script.ScriptReloadListener;
import net.aros.engine.common.script.procedure.ProcedureSerializationException;
import net.aros.examples.client.ArosEngineClientExamples;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mod(value = ArosEngine.MOD_ID, dist = Dist.CLIENT)
public final class ArosEngineClient {
    public ArosEngineClient(IEventBus bus) {
        AeKeybinds.init();
        AeValuesUIProviders.init();
        AeUIs.init();

        bus.addListener(RegisterClientReloadListenersEvent.class, e -> e.registerReloadListener(new ScriptReloadListener(
                () -> {
                    ClientProcedures.clear();
                    ClientEventSubscriptions.clear();
                }, this::createScriptValidator)));

        if (ArosEngine.CONFIG.debug.examplesEnabled()) {
            new ArosEngineClientExamples(bus);
        }
    }

    private Predicate<Resource> createScriptValidator() {
        Set<String> trustedPacks = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks()
                .stream()
                .filter(pack -> pack.getPackSource() != PackSource.SERVER)
                .map(Pack::getId)
                .collect(Collectors.toSet());

        return resource -> trustedPacks.contains(resource.sourcePackId());
    }

    public static void handleProcedureCall(ProcedureCallPayload payload, IPayloadContext ctx) {
        try {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Object[] args = ClientProcedureDecoding.decodeProcedureArgs(payload.id(), level.registryAccess(), payload.args());

            ClientProcedures.call(payload.id(), args);
        } catch (ProcedureSerializationException ignore) {
        }
    }
}
