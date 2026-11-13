package com.craftbound.client;

public record Rect(int x, int y, int width, int height)
{
    public boolean contains(double mouseX, double mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public int right()
    {
        return x + width;
    }

    public int bottom()
    {
        return y + height;
    }
}
