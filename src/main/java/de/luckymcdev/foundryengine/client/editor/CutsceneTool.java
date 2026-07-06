package de.luckymcdev.foundryengine.client.editor;

import de.luckymcdev.foundryengine.client.cutscene.CutsceneRenderer;
import de.luckymcdev.foundryengine.client.gizmo.WorldGizmo;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.easing.BezierPath;
import de.luckymcdev.foundryengine.common.easing.BezierPoint;
import de.luckymcdev.foundryengine.common.easing.BezierSpline;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutscenePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CutsceneTool {
	double storedDistance = 0;
	private BezierPoint selectedPoint;
	private boolean changed = false;
	private boolean wasUsing = false;
	private int useTicks = 0;

	private static BezierPoint pickHovered(Minecraft mc) {
		Vec3 eye = mc.player.getEyePosition();
		Vec3 look = mc.player.getViewVector(1.0f);
		for (Cutscene cutscene : CutsceneRenderer.getCutscenes()) {
			for (BezierSpline spline : cutscene.path.splines) {
				for (BezierPoint point : spline.points) {
					if (WorldGizmo.isHovered(point.getPos(), eye, look)) {
						return point;
					}
				}
			}
		}
		return null;
	}

	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		boolean using = EditorController.isUsingEditorItem();

		if (using && !wasUsing) {
			wasUsing = true;
			useTicks = 0;
			storedDistance = 0;
			selectedPoint = null;
			changed = false;
			CutsceneRenderer.storedPoint = null;
			CutsceneRenderer.storedDistance = 0;
		}

		if (using) {
			useTicks++;
			if (selectedPoint == null) {
				selectedPoint = pickHovered(mc);
				if (selectedPoint != null) {
					CutsceneRenderer.storedPoint = selectedPoint;
					storedDistance = selectedPoint.getPos().distanceTo(mc.player.getEyePosition());
					CutsceneRenderer.storedDistance = storedDistance;
				}
			}
		}

		if (!using && wasUsing) {
			wasUsing = false;
			if (selectedPoint != null && useTicks > 2) {
				changed = true;
			}
			if (changed) {
				ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
			}
			selectedPoint = null;
			CutsceneRenderer.storedPoint = null;
			changed = false;
		}
	}

	public boolean onScroll(double vertical) {
		if (CutsceneRenderer.storedPoint == null) {
			return false;
		}
		if (!wasUsing) {
			return false;
		}
		storedDistance = Math.max(storedDistance + (vertical * 0.25), 0);
		CutsceneRenderer.storedDistance = storedDistance;
		return true;
	}

	public void onDeactivated() {
		wasUsing = false;
		useTicks = 0;
		storedDistance = 0;
		selectedPoint = null;
		changed = false;
		CutsceneRenderer.storedPoint = null;
	}

	public void render() {
		CutsceneRenderer.render();
	}

	public void addNodeAtEnd(Cutscene cutscene) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		BezierPath path = cutscene.path;
		Vec3 newEnd = mc.player.getEyePosition();
		BezierPoint lastPoint = path.getPoints().getLast();

		BezierSpline newSpline = new BezierSpline(lastPoint, path, newEnd);
		path.splines.addLast(newSpline);
		path.updateLUT();

		cutscene.insertAnchorRotationAtEnd(new Vec2(mc.player.getXRot(), mc.player.getYRot()));

		ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
	}

	public void addNodeAtStart(Cutscene cutscene) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		BezierPath path = cutscene.path;
		Vec3 newStart = mc.player.getEyePosition();
		BezierPoint firstPoint = path.getPoints().getFirst();

		BezierSpline newSpline = new BezierSpline(firstPoint, path, newStart);
		path.splines.addFirst(newSpline);
		path.updateLUT();

		cutscene.insertAnchorRotationAtStart(new Vec2(mc.player.getXRot(), mc.player.getYRot()));

		ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
	}

	public boolean removeLastNode(Cutscene cutscene) {
		BezierPath path = cutscene.path;
		if (path.isSinglePoint()) {
			var list = CutsceneRenderer.getCutscenes();
			if (!list.isEmpty()) {
				list.remove(cutscene);
			}
			ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
			return true;
		}
		BezierPoint lastPoint = path.getPoints().getLast();
		path.removePoint(lastPoint);
		cutscene.removeAnchorRotationAtEnd();
		ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
		return false;
	}

	public boolean removeFirstNode(Cutscene cutscene) {
		BezierPath path = cutscene.path;
		if (path.isSinglePoint()) {
			var list = CutsceneRenderer.getCutscenes();
			if (!list.isEmpty()) {
				list.remove(cutscene);
			}
			ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
			return true;
		}
		BezierPoint firstPoint = path.getPoints().getFirst();
		path.removePoint(firstPoint);
		cutscene.removeAnchorRotationAtStart();
		ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
		return false;
	}

	public CompoundTag toNbt() {
		CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		for (Cutscene cutscene : CutsceneRenderer.getCutscenes()) {
			list.add(cutscene.toNbt());
		}
		tag.put("CutsceneList", list);
		return tag;
	}
}
