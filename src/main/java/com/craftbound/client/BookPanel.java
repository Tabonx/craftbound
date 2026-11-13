package com.craftbound.client;

import net.minecraft.client.gui.GuiGraphics;

// One of the book's two states, browsing the grid or reading a recipe. Each has its own rail and its
// own idea of a page, and the book shell hands it what lands inside.
interface BookPanel
{
    void layout(BookGeometry geometry);

    BookRail rail();

    // A tab on the rail was clicked.
    void selectTab(int index, int button);

    int page();

    int pages();

    void step(int delta);

    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    PendingTooltip tooltip();

    boolean click(double mouseX, double mouseY, int button, boolean doubleClick);
}
