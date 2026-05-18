package com.example.multirespawn.client;

import com.example.multirespawn.network.ClientPackets;
import com.example.multirespawn.network.RespawnPointView;
import com.example.multirespawn.respawn.RespawnSelectionService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class RespawnChoiceScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int MIN_ROW_WIDTH = 220;
    private static final int MAX_ROW_WIDTH = 560;
    private static final int HORIZONTAL_MARGIN = 10;
    private static final int TITLE_Y = 18;
    private static final int LIST_TOP = 40;
    private static final int BOTTOM_CONTROLS_HEIGHT = 62;

    private List<RespawnPointView> points;
    private final Screen previousScreen;
    private int offset;

    public RespawnChoiceScreen(List<RespawnPointView> points) {
        this(points, null);
    }

    public RespawnChoiceScreen(List<RespawnPointView> points, Screen previousScreen) {
        super(Text.translatable("screen.multirespawn.respawn_choice.title"));
        this.points = new ArrayList<>(points);
        this.previousScreen = previousScreen;
    }

    public void setPoints(List<RespawnPointView> points) {
        this.points = new ArrayList<>(points);
        clampOffset();
        rebuildWidgets();
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();

        Layout layout = getLayout();
        clampOffset();
        int visible = Math.min(layout.visibleRows(), Math.max(0, points.size() - offset));
        boolean compact = layout.compact();
        int respawnWidth = compact ? 34 : buttonWidth(Text.translatable("button.multirespawn.respawn"), 56, 90);
        int deleteWidth = compact ? 34 : buttonWidth(Text.translatable("button.multirespawn.delete"), 56, 90);
        Text respawnText = Text.translatable(compact ? "button.multirespawn.respawn.short" : "button.multirespawn.respawn");
        Text deleteText = Text.translatable(compact ? "button.multirespawn.delete.short" : "button.multirespawn.delete");

        for (int i = 0; i < visible; i++) {
            RespawnPointView point = points.get(offset + i);
            int y = layout.listTop() + i * ROW_HEIGHT;
            int respawnX = layout.left() + layout.rowWidth() - respawnWidth;
            int deleteX = respawnX - deleteWidth - 4;
            addDrawableChild(ButtonWidget.builder(respawnText, button -> {
                ClientPackets.chooseRespawnPoint(point.id());
                close();
            }).dimensions(respawnX, y, respawnWidth, 20).build());

            addDrawableChild(ButtonWidget.builder(deleteText, button ->
                    ClientPackets.deleteRespawnPoint(point.id())
            ).dimensions(deleteX, y, deleteWidth, 20).build());
        }

        int controlsY = layout.controlsTop();
        addDrawableChild(ButtonWidget.builder(Text.translatable(compact ? "button.multirespawn.world_spawn.short" : "button.multirespawn.world_spawn"), button -> {
            ClientPackets.chooseRespawnPoint(RespawnSelectionService.WORLD_SPAWN_ID);
            close();
        }).dimensions(layout.left(), controlsY, layout.controlWidth(0), 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable(compact ? "button.multirespawn.previous.short" : "button.multirespawn.previous"), button -> {
            offset = Math.max(0, offset - layout.visibleRows());
            clampOffset();
            rebuildWidgets();
        }).dimensions(layout.controlX(1), controlsY, layout.controlWidth(1), 20).build()).active = offset > 0;

        addDrawableChild(ButtonWidget.builder(Text.translatable(compact ? "button.multirespawn.next.short" : "button.multirespawn.next"), button -> {
            offset += layout.visibleRows();
            clampOffset();
            rebuildWidgets();
        }).dimensions(layout.controlX(2), controlsY, layout.controlWidth(2), 20).build()).active = offset + layout.visibleRows() < points.size();

        int lowerY = controlsY + 26;
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.multirespawn.back"), button -> {
            Screen target = previousScreen != null ? previousScreen : new DeathScreen(Text.translatable("deathScreen.title"), false);
            client.setScreen(target);
        }).dimensions(layout.left(), lowerY, layout.secondaryWidth(0), 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable(compact ? "button.multirespawn.title_screen.short" : "button.multirespawn.title_screen"), button -> {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (minecraftClient.world != null) {
                minecraftClient.world.disconnect();
            }
            minecraftClient.disconnect(new TitleScreen());
        }).dimensions(layout.secondaryX(1), lowerY, layout.secondaryWidth(1), 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, TITLE_Y, 0xFFFFFF);

        Layout layout = getLayout();

        if (points.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("message.multirespawn.no_available_points"),
                    width / 2, layout.listTop() + 18, 0xAAAAAA);
        } else {
            int actionWidth = layout.compact() ? 72 : buttonWidth(Text.translatable("button.multirespawn.respawn"), 56, 90)
                    + buttonWidth(Text.translatable("button.multirespawn.delete"), 56, 90) + 4;
            int textWidth = Math.max(40, layout.rowWidth() - actionWidth - 8);
            int visible = Math.min(layout.visibleRows(), Math.max(0, points.size() - offset));
            for (int i = 0; i < visible; i++) {
                RespawnPointView point = points.get(offset + i);
                int y = layout.listTop() + i * ROW_HEIGHT + 6;
                String label = point.name()
                        + " | " + point.dimensionId()
                        + " | " + point.pos().toShortString()
                        + " | " + point.type();
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(label, textWidth),
                        layout.left(), y, point.valid() ? 0xE0E0E0 : 0xAA5555);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void clampOffset() {
        int visibleRows = getLayout().visibleRows();
        offset = Math.max(0, Math.min(offset, Math.max(0, points.size() - visibleRows)));
    }

    private int buttonWidth(Text text, int min, int max) {
        return Math.min(max, Math.max(min, textRenderer.getWidth(text) + 16));
    }

    private Layout getLayout() {
        int rowWidth = Math.max(MIN_ROW_WIDTH, Math.min(MAX_ROW_WIDTH, width - HORIZONTAL_MARGIN * 2));
        rowWidth = Math.min(rowWidth, Math.max(1, width - HORIZONTAL_MARGIN * 2));
        int left = Math.max(2, (width - rowWidth) / 2);
        int availableRowsHeight = Math.max(ROW_HEIGHT, height - LIST_TOP - BOTTOM_CONTROLS_HEIGHT);
        int visibleRows = Math.max(1, availableRowsHeight / ROW_HEIGHT);
        visibleRows = Math.min(visibleRows, Math.max(1, points.size()));
        int controlsTop = Math.min(height - BOTTOM_CONTROLS_HEIGHT + 8, LIST_TOP + visibleRows * ROW_HEIGHT + 8);
        boolean compact = rowWidth < 330;
        return new Layout(left, rowWidth, LIST_TOP, visibleRows, controlsTop, compact);
    }

    private record Layout(int left, int rowWidth, int listTop, int visibleRows, int controlsTop, boolean compact) {
        private static final int GAP = 6;

        int controlWidth(int index) {
            int available = rowWidth - GAP * 2;
            int first = compact ? Math.max(70, available / 3) : Math.min(120, Math.max(94, available / 3));
            int rest = (available - first) / 2;
            return index == 0 ? first : rest;
        }

        int controlX(int index) {
            if (index == 0) {
                return left;
            }
            if (index == 1) {
                return left + controlWidth(0) + GAP;
            }
            return left + controlWidth(0) + GAP + controlWidth(1) + GAP;
        }

        int secondaryWidth(int index) {
            int available = rowWidth - GAP;
            return index == 0 ? Math.max(70, available / 2) : available - Math.max(70, available / 2);
        }

        int secondaryX(int index) {
            return index == 0 ? left : left + secondaryWidth(0) + GAP;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
