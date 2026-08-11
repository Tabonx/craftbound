package com.craftbound.client.jei;

final class RecipeRuntimeReadiness
{
    enum Action
    {
        NONE,
        START,
        RESTART
    }

    private boolean tagsReady;
    private boolean recipesReady;
    private boolean running;

    Action tagsReady()
    {
        tagsReady = true;
        return nextAction();
    }

    Action recipesReady()
    {
        recipesReady = true;
        return nextAction();
    }

    boolean reset()
    {
        tagsReady = false;
        recipesReady = false;
        boolean wasRunning = running;
        running = false;
        return wasRunning;
    }

    private Action nextAction()
    {
        if (!tagsReady || !recipesReady)
            return Action.NONE;

        tagsReady = false;
        recipesReady = false;
        Action action = running ? Action.RESTART : Action.START;
        running = true;
        return action;
    }
}
