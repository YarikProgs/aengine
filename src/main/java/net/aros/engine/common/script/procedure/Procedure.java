package net.aros.engine.common.script.procedure;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Procedure {
    private final ResourceLocation id;
    private final Class<?>[] parameterTypes;
    private final Function<Object[], Object> function;

    public Procedure(ResourceLocation id, Class<?>[] parameterTypes, Function<Object[], Object> function) {
        this.id = id;
        this.parameterTypes = parameterTypes;
        this.function = function;
    }

    public Object call(Object... arguments) throws ProcedureException {
        expectSameLength(arguments);
        expectSameTypes(arguments);
        try {
            return function.apply(arguments);
        } catch (Exception e) {
            throw new ProcedureException("Unexpected exception during procedure call", e);
        }
    }

    private void expectSameLength(Object @NotNull ... arguments) throws ProcedureException {
        if (parameterTypes.length == arguments.length) return;

        throw new ProcedureException("Invalid arguments count: expected "
                + parameterTypes.length + ", got: " + arguments.length);
    }

    private void expectSameTypes(Object @NotNull ... arguments) throws ProcedureException {
        Class<?>[] argumentTypes = Arrays.stream(arguments).map(Object::getClass).toArray(Class[]::new);
        if (Arrays.equals(parameterTypes, argumentTypes)) return;

        throw new ProcedureException("Invalid argument types: expected "
                + Arrays.toString(parameterTypes) + ", got: " + Arrays.toString(argumentTypes));
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public String toString() {
        return "Procedure<"
                + Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", "))
                + ">(" + id + ")";
    }
}
