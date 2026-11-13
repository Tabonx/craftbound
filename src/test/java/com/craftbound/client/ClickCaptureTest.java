package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClickCaptureTest
{
    private final ClickCapture capture = new ClickCapture();

    @Test
    void aTakenPressKeepsItsDragsAndOneRelease()
    {
        capture.take();

        assertTrue(capture.holding());
        assertTrue(capture.release());
        assertFalse(capture.holding());
        assertFalse(capture.release());
    }

    @Test
    void aPressTheBookLeftAloneKeepsNothing()
    {
        assertFalse(capture.holding());
        assertFalse(capture.release());
    }

    @Test
    void aClosedScreenDropsThePress()
    {
        capture.take();
        capture.reset();

        assertFalse(capture.release());
    }
}
