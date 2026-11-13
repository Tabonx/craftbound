package com.craftbound.client;

// Where a recipe drawable lands inside the book's body: scaled down to fit, never up, and centered.
// The drawable's own coordinates start at (boundsX, boundsY).
public record RecipeViewport(float scale, int originX, int originY, int boundsX, int boundsY)
{
    public static RecipeViewport fit(Rect body, int boundsX, int boundsY, int boundsWidth, int boundsHeight)
    {
        int width = Math.max(1, boundsWidth);
        int height = Math.max(1, boundsHeight);
        float scale = Math.min(1f, Math.min((float) body.width() / width, (float) body.height() / height));
        int originX = body.x() + (body.width() - Math.round(width * scale)) / 2;
        int originY = body.y() + (body.height() - Math.round(height * scale)) / 2;
        return new RecipeViewport(scale, originX, originY, boundsX, boundsY);
    }

    public double localX(double mouseX)
    {
        return (mouseX - originX) / scale + boundsX;
    }

    public double localY(double mouseY)
    {
        return (mouseY - originY) / scale + boundsY;
    }
}
