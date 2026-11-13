package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.craftbound.progression.InputSlot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

class IntegrationsTest
{
    private static final InputSlot BURNER = InputSlot.ofItems(Set.of(ResourceLocation.parse("a:burner")));
    private static final InputSlot CAKE = InputSlot.ofItems(Set.of(ResourceLocation.parse("b:cake")));

    private final List<String> logged = new ArrayList<>();
    private final Integrations integrations = new Integrations();

    @Test
    void everyIntegrationAddsItsRequirements()
    {
        integrations.add(guard("A"), guard -> requiring(guard, BURNER));
        integrations.add(guard("B"), guard -> requiring(guard, CAKE));

        assertEquals(List.of(BURNER, CAKE), integrations.requirements("recipe"));
    }

    @Test
    void oneModThatNoLongerFitsLeavesTheOthersRunning()
    {
        IntegrationGuard broken = guard("A");
        IntegrationGuard working = guard("B");
        integrations.add(broken, guard -> new BookIntegration()
        {
            @Override
            public List<InputSlot> hiddenRequirements(Object recipe)
            {
                return guard.call(() -> { throw new NoSuchMethodError("moved"); }, List::of);
            }
        });
        integrations.add(working, guard -> requiring(guard, CAKE));

        assertEquals(List.of(CAKE), integrations.requirements("recipe"));
        assertEquals(List.of(CAKE), integrations.requirements("recipe"));
        assertFalse(broken.enabled());
        assertTrue(working.enabled());
        assertEquals(1, logged.size());
    }

    @Test
    void anIntegrationThatCannotBeBuiltIsLeftOut()
    {
        IntegrationGuard broken = guard("A");
        integrations.add(broken, guard -> { throw new NoClassDefFoundError("Moved"); });
        integrations.add(guard("B"), guard -> requiring(guard, CAKE));

        assertEquals(List.of(CAKE), integrations.requirements("recipe"));
        assertFalse(broken.enabled());
    }

    @Test
    void theFirstIntegrationThatOwnsTheScreenBindsIt()
    {
        MenuBinding binding = new MenuBinding(null, Set::of, () -> 0);
        integrations.add(guard("A"), guard -> new BookIntegration() {});
        integrations.add(guard("B"), guard -> binding(binding));

        assertSame(binding, integrations.binding(null, null));
    }

    @Test
    void aScreenNoIntegrationOwnsOnlyPlacesThroughJei()
    {
        integrations.add(guard("A"), guard -> new BookIntegration() {});

        assertNull(integrations.binding(null, null).craftable());
    }

    @Test
    void aBuiltInIntegrationBindsBeforeAnyMod()
    {
        MenuBinding vanilla = new MenuBinding(null, Set::of, () -> 0);
        Integrations withVanilla = new Integrations(binding(vanilla));
        withVanilla.add(guard("A"), guard -> binding(new MenuBinding(null, Set::of, () -> 1)));

        assertSame(vanilla, withVanilla.binding(null, null));
    }

    private IntegrationGuard guard(String name)
    {
        return new IntegrationGuard(name, (message, error) -> logged.add(message));
    }

    private static BookIntegration binding(MenuBinding binding)
    {
        return new BookIntegration()
        {
            @Override
            public Optional<MenuBinding> bind(AbstractContainerMenu menu, RecipePlacer transfer)
            {
                return Optional.of(binding);
            }
        };
    }

    private static BookIntegration requiring(IntegrationGuard guard, InputSlot slot)
    {
        return new BookIntegration()
        {
            @Override
            public List<InputSlot> hiddenRequirements(Object recipe)
            {
                return guard.call(() -> List.of(slot), List::of);
            }
        };
    }
}
