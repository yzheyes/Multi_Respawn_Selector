package com.example.multirespawn.client;

import com.example.multirespawn.network.ClientPackets;
import com.example.multirespawn.network.RespawnPointView;
import com.example.multirespawn.respawn.RespawnSelectionService;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class RespawnChoiceScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 7;

    private List<RespawnPointView> points;
    private int offset;

    public RespawnChoiceScreen(List<RespawnPointView> points) {
        super(Text.literal("选择重生点"));
        this.points = new ArrayList<>(points);
    }

    public void setPoints(List<RespawnPointView> points) {
        this.points = new ArrayList<>(points);
        this.offset = Math.min(offset, Math.max(0, points.size() - VISIBLE_ROWS));
        rebuildWidgets();
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();

        int listTop = 50;
        int rowWidth = Math.min(460, width - 40);
        int left = (width - rowWidth) / 2;
        int visible = Math.min(VISIBLE_ROWS, Math.max(0, points.size() - offset));

        for (int i = 0; i < visible; i++) {
            RespawnPointView point = points.get(offset + i);
            int y = listTop + i * ROW_HEIGHT;
            addDrawableChild(ButtonWidget.builder(Text.literal("重生"), button -> {
                ClientPackets.chooseRespawnPoint(point.id());
                close();
            }).dimensions(left + rowWidth - 56, y, 52, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("删除"), button ->
                    ClientPackets.deleteRespawnPoint(point.id())
            ).dimensions(left + rowWidth - 112, y, 52, 20).build());
        }

        int controlsY = listTop + VISIBLE_ROWS * ROW_HEIGHT + 10;
        addDrawableChild(ButtonWidget.builder(Text.literal("世界出生点"), button -> {
            ClientPackets.chooseRespawnPoint(RespawnSelectionService.WORLD_SPAWN_ID);
            close();
        }).dimensions(left, controlsY, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("上一页"), button -> {
            offset = Math.max(0, offset - VISIBLE_ROWS);
            rebuildWidgets();
        }).dimensions(left + 122, controlsY, 70, 20).build()).active = offset > 0;

        addDrawableChild(ButtonWidget.builder(Text.literal("下一页"), button -> {
            offset = Math.min(Math.max(0, points.size() - VISIBLE_ROWS), offset + VISIBLE_ROWS);
            rebuildWidgets();
        }).dimensions(left + 198, controlsY, 70, 20).build()).active = offset + VISIBLE_ROWS < points.size();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);

        int rowWidth = Math.min(460, width - 40);
        int left = (width - rowWidth) / 2;
        int listTop = 50;

        if (points.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("没有可用重生点，使用世界出生点"),
                    width / 2, 80, 0xAAAAAA);
        } else {
            int visible = Math.min(VISIBLE_ROWS, Math.max(0, points.size() - offset));
            for (int i = 0; i < visible; i++) {
                RespawnPointView point = points.get(offset + i);
                int y = listTop + i * ROW_HEIGHT + 6;
                String label = point.name()
                        + " | " + point.dimensionId()
                        + " | " + point.pos().toShortString()
                        + " | " + point.type();
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(label, rowWidth - 122),
                        left, y, point.valid() ? 0xE0E0E0 : 0xAA5555);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
