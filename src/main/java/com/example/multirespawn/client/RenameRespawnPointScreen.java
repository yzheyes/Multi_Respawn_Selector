package com.example.multirespawn.client;

import com.example.multirespawn.network.ClientPackets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.UUID;

public class RenameRespawnPointScreen extends Screen {
    private final UUID pointId;
    private final String currentName;
    private final String dimension;
    private final String pos;
    private TextFieldWidget nameField;

    public RenameRespawnPointScreen(UUID pointId, String currentName, String dimension, String pos) {
        super(Text.literal("重命名重生点"));
        this.pointId = pointId;
        this.currentName = currentName;
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    protected void init() {
        int fieldWidth = Math.min(300, width - 40);
        int left = (width - fieldWidth) / 2;

        nameField = new TextFieldWidget(textRenderer, left, height / 2 - 18, fieldWidth, 20, Text.literal("Name"));
        nameField.setMaxLength(64);
        nameField.setText(currentName);
        nameField.setFocused(true);
        addDrawableChild(nameField);
        setInitialFocus(nameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("保存"), button -> save())
                .dimensions(width / 2 - 102, height / 2 + 16, 98, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("取消"), button -> close())
                .dimensions(width / 2 + 4, height / 2 + 16, 98, 20)
                .build());
    }

    private void save() {
        String newName = nameField.getText().trim();
        if (!newName.isEmpty()) {
            ClientPackets.renameRespawnPoint(pointId, newName);
        }
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 70, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(dimension + " | " + pos),
                width / 2, height / 2 - 48, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
