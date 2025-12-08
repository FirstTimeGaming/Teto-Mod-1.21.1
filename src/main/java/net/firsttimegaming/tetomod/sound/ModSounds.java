package net.firsttimegaming.tetomod.sound;

import net.firsttimegaming.tetomod.TetoMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TetoMod.MOD_ID);

    public static final Supplier<SoundEvent> PLUSH_TRADE_SUCCESS = registerSoundEvent("plush.trade_success");
    public static final Supplier<SoundEvent> PLUSH_TRADE_FAIL = registerSoundEvent("plush.trade_fail");

    public static final Supplier<SoundEvent> PLUSH_UPGRADE_SUCCESS = registerSoundEvent("plush.upgrade_success");
    public static final Supplier<SoundEvent> PLUSH_UPGRADE_FAIL = registerSoundEvent("plush.upgrade_fail");

    public static final Supplier<SoundEvent> PLUSH_REROLL_QUEST_1 = registerSoundEvent("plush.reroll_quest_1");
    public static final Supplier<SoundEvent> PLUSH_REROLL_QUEST_2 = registerSoundEvent("plush.reroll_quest_2");
    public static final Supplier<SoundEvent> PLUSH_REROLL_QUEST_3 = registerSoundEvent("plush.reroll_quest_3");


    public static final Supplier<SoundEvent> PLUSH_ATTACKED = registerSoundEvent("plush.attacked");

    public static final Supplier<SoundEvent> PLUSH_AMBIENT_1 = registerSoundEvent("plush.ambient_1");

    public static SoundEvent getRandomRerollSound() {
        int rand = (int) (Math.random() * 3);

        return switch (rand) {
            case 0 -> PLUSH_REROLL_QUEST_1.get();
            case 1 -> PLUSH_REROLL_QUEST_2.get();
            case 2 -> PLUSH_REROLL_QUEST_3.get();
            default -> PLUSH_REROLL_QUEST_1.get();
        };
    }

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TetoMod.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

