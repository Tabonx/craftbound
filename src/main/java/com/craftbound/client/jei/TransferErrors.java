package com.craftbound.client.jei;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

// A transfer error hands its tooltip only to a JEI tooltip builder, whose methods change with
// nearly every JEI version. A proxy that keeps the text lines and ignores the rest fits them all.
final class TransferErrors
{
    static List<Component> tooltip(IRecipeTransferError error)
    {
        List<Component> lines = new ArrayList<>();
        ITooltipBuilder builder = (ITooltipBuilder) Proxy.newProxyInstance(ITooltipBuilder.class.getClassLoader(),
                new Class<?>[] {ITooltipBuilder.class}, (proxy, method, args) ->
                {
                    switch (method.getName())
                    {
                        case "add" -> add(lines, args[0]);
                        case "addAll" -> ((Collection<?>) args[0]).forEach(line -> add(lines, line));
                        default -> { }
                    }
                    return method.getReturnType() == List.class ? List.of() : null;
                });
        error.getTooltip(builder);
        return lines;
    }

    private static void add(List<Component> lines, Object line)
    {
        if (line instanceof Component component)
            lines.add(component);
        else if (line instanceof FormattedText text)
            lines.add(Component.literal(text.getString()));
    }

    private TransferErrors() {}
}
