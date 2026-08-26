package net.aros.examples.common;

import net.aros.engine.client.api.camera.AeCameras;
import net.aros.engine.client.api.camera.position.PositionInterpolation;
import net.aros.engine.client.api.camera.settings.EntityCameraSettings;
import net.aros.engine.client.api.camera.rotation.RotationMode;
import net.aros.engine.common.ArosEngine;
import net.aros.examples.common.modules.ExampleCommonModule;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.aros.engine.common.ArosEngine.MOD_ID;

public class ArosEngineExamples {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredItem<TestItem> TEST_ITEM = ITEMS.registerItem("test_item", TestItem::new);

    public ArosEngineExamples(IEventBus bus) {
        ArosEngine.getEngine().register(new ExampleCommonModule());

        ITEMS.register(bus);
    }

    private static class TestItem extends Item {
        public TestItem(Properties properties) {
            super(properties);
        }

        @Override
        @ParametersAreNonnullByDefault
        @NotNull
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            if (level.isClientSide && usedHand == InteractionHand.MAIN_HAND) {
                if (player.isShiftKeyDown()) AeCameras.reset();
                else AeCameras.setEntity(player, new EntityCameraSettings(
                        new Vec3(0.0D, 10.0D, -2.0D),
                        new Vector3f(0, 0, 0),
                        EntityCameraSettings.OffsetSpace.ENTITY,
                        PositionInterpolation.linear(0.1f),
                        RotationMode.lookAtTarget(0.6f, true),
                        true,
                        true,
                        true
                ));

//else                 AeCameras.setMoving(
//                        List.of(
//                                new CameraWaypoint(new Vec3(-227, 69, -759), 90, 0, 0),
//                                new CameraWaypoint(new Vec3(-248, 82, -751), 30, 21, 0),
//                                new CameraWaypoint(new Vec3(-302, 99, -762), 130, 57, 0),
//                                new CameraWaypoint(new Vec3(-313, 91, -879), -131, 64, 0),
//                                new CameraWaypoint(new Vec3(-243, 97, -835), -8, 27, 0),
//                                new CameraWaypoint(new Vec3(-227, 69, -759), 90, 0, 0)
//                        ),
//                        new MovingCameraSettings(
//                                MovingCameraSettings.TimeMode.totalByDistance(4 * 20),
//                                true,
//                                true,
//                                10,
//                                10
//                        )
//                );
            }
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
    }
}
