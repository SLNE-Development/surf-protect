package dev.slne.protect.paper.instance;

import dev.slne.surf.protect.paper.PaperMain;
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerState;
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerThread;

public class BukkitInstance {
    private ProtectionVisualizerThread protectionVisualizerThread;
    private ProtectionVisualizerState protectionVisualizerState;


    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        protectionVisualizerState = new ProtectionVisualizerState();

        protectionVisualizerThread = new ProtectionVisualizerThread(PaperMain.getInstance());
        protectionVisualizerThread.start();
    }

    /**
     * Called when the plugin is disabled
     */
    public void onDisable() {
        protectionVisualizerThread.stop();
    }

    /**
     * Returns the {@link ProtectionVisualizerThread}
     *
     * @return the {@link ProtectionVisualizerThread}
     */
    public ProtectionVisualizerThread getProtectionVisualizerThread() {
        return protectionVisualizerThread;
    }

    /**
     * @return the protectionVisualizerState
     */
    public ProtectionVisualizerState getProtectionVisualizerState() {
        return protectionVisualizerState;
    }
}
