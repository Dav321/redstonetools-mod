package tools.redstone.redstonetools.mixin.update;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tools.redstone.redstonetools.utils.UpdateChecker;

import static tools.redstone.redstonetools.RedstoneToolsClient.MOD_VERSION;

@Mixin(TitleScreen.class)
public class CheckUpdateMixin extends Screen {
    @Unique
    private static MutableText updateStatus = (MutableText) Text.of("Redstone Tools Version: " + MOD_VERSION + "(Bug found, report on Github)");

    public CheckUpdateMixin() {
        super(Text.of("UpdateText(Bug found, report on Github)"));
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void checkUpdate(CallbackInfo ci) {
        var isOnLatestVersion = UpdateChecker.isOnLatestVersion();

        Style underline = Style.EMPTY;
        if (isOnLatestVersion) {
            updateStatus = (MutableText) Text.of("Redstone Tools " + MOD_VERSION);
        } else {
            updateStatus = (MutableText) Text.of("Redstone Tools " + MOD_VERSION + " (");
            updateStatus.append(Text.of("Click to Update").getWithStyle(underline.withUnderline(true)).getFirst());
            updateStatus.append(")");
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void updateTextInjection(CallbackInfo ci) {
        var update = UpdateChecker.getLatestVersion();

        this.addDrawableChild(new PressableTextWidget(4, 4, textRenderer.getWidth(updateStatus), textRenderer.fontHeight, updateStatus,
                button -> update.ifPresent(uri -> Util.getOperatingSystem().open(uri)), textRenderer));
    }
}
