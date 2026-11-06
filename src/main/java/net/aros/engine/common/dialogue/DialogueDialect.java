package net.aros.engine.common.dialogue;

import net.aros.haru.compile.Compiler;
import net.aros.haru.link.Linker;
import net.aros.haru.value.HaruFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogueDialect {
    private final String name;
    private final List<String> externalVariables = new ArrayList<>();
    private final Map<String, HaruFunction> functions = new HashMap<>();

    public DialogueDialect(String name) {
        this.name = name;
    }

    public DialogueDialect declareVar(String externalVariable) {
        externalVariables.add(externalVariable);
        return this;
    }

    public DialogueDialect defineFn(String name, HaruFunction function) {
        functions.put(name, function);
        return this;
    }

    public Compiler setupCompiler(@NotNull Compiler compiler) {
        compiler.declareExternalAll(externalVariables);
        return compiler;
    }

    public Linker setupLinker(@NotNull Linker linker) {
        linker.defineAll(functions);
        return linker;
    }

    public String name() {
        return name;
    }
}
