package com.example.dynflight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side 3D flight: moves in the direction you look.
 * Works only when the server already lets you fly (creative / allowed flight),
 * so server rules are never bypassed.
 */
public class DynFlightClient implements ClientModInitializer {
    // Tweak these
    private static final double SPEED = 1.0;        // blocks per tick (~20 b/s per 1.0)
    private static final double VERTICAL_BOOST = 0.6; // space / shift speed
    private static final boolean AUTO_FLY = true;   // start flying automatically in the air

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.screen != null || p.isSpectator()) return;

        Abilities ab = p.getAbilities();
        if (!ab.mayfly) return; // server did not allow flight -> do nothing

        if (AUTO_FLY && !p.onGround() && !ab.flying) {
            ab.flying = true;
            p.onUpdateAbilities();
        }
        if (!ab.flying) return;

        var o = mc.options;
        double forward = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
        double strafe = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
        double vertical = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);

        Vec3 look = p.getLookAngle().normalize();
        Vec3 left = new Vec3(look.z, 0, -look.x).normalize();

        Vec3 motion = look.scale(forward * SPEED)
                .add(left.scale(strafe * SPEED))
                .add(0, vertical * VERTICAL_BOOST, 0);

        // No input -> hover, like vanilla creative flight
        p.setDeltaMovement(motion);
    }
}
