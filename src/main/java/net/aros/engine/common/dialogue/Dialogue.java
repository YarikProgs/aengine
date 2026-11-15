package net.aros.engine.common.dialogue;

import net.aros.haru.link.LinkedProgram;
import net.minecraft.resources.ResourceLocation;

public record Dialogue(ResourceLocation id, DialogueDialect dialect, DialogueMetadata metadata, LinkedProgram linkedProgram) {
}
