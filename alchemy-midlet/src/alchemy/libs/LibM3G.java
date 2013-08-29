/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package alchemy.libs;

import alchemy.core.Context;
import alchemy.core.ContextListener;
import alchemy.fs.FSManager;
import alchemy.nlib.NativeLibrary;
import alchemy.util.IO;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.m3g.AnimationController;
import javax.microedition.m3g.AnimationTrack;
import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Background;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.CompositingMode;
import javax.microedition.m3g.Fog;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.Group;
import javax.microedition.m3g.Image2D;
import javax.microedition.m3g.IndexBuffer;
import javax.microedition.m3g.KeyframeSequence;
import javax.microedition.m3g.Light;
import javax.microedition.m3g.Loader;
import javax.microedition.m3g.Material;
import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.MorphingMesh;
import javax.microedition.m3g.Node;
import javax.microedition.m3g.Object3D;
import javax.microedition.m3g.PolygonMode;
import javax.microedition.m3g.RayIntersection;
import javax.microedition.m3g.SkinnedMesh;
import javax.microedition.m3g.Sprite3D;
import javax.microedition.m3g.Texture2D;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.Transformable;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;
import javax.microedition.m3g.World;

/**
 * Bindings to M3G (JSR-184).
 *
 * @author Sergey Basalaev
 * @version 1.0
 */
public class LibM3G extends NativeLibrary {
	
	public LibM3G() throws IOException {
		load("/m3g-10.symbols");
	}
	
	/** Owner of Graphics3D object. */
	private static volatile Context owner;

	static Graphics3D graphics3D(Context c) {
		final Graphics3D instance = Graphics3D.getInstance();
		synchronized (instance) {
			if (owner == null) {
				owner = c;
				c.addContextListener(new M3GContextListener());
			} else if (owner != c) {
				throw new IllegalStateException("M3G is used by another process");
			}
		}
		return instance;
	}
	
	protected Object invokeNative(int index, Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // AnimationController.new()
				return new AnimationController();
			case 1: // AnimationController.setActiveInterval(start: Int, end: Int)
				((AnimationController)args[0]).setActiveInterval(ival(args[1]), ival(args[2]));
				return null;
			case 2: // AnimationController.getActiveIntervalStart(): Int
				return Ival(((AnimationController)args[0]).getActiveIntervalStart());
			case 3: // AnimationController.getActiveIntervalEnd(): Int
				return Ival(((AnimationController)args[0]).getActiveIntervalEnd());
			case 4: // AnimationController.setSpeed(speed: Float, worldTime: Int)
				((AnimationController)args[0]).setSpeed(fval(args[1]), ival(args[2]));
				return null;
			case 5: // AnimationController.getSpeed(): Float
				return Fval(((AnimationController)args[0]).getSpeed());
			case 6: // AnimationController.setPosition(seqTime: Float, worldTime: Int)
				((AnimationController)args[0]).setPosition(fval(args[1]), ival(args[2]));
				return null;
			case 7: // AnimationController.getPosition(worldTime: Int): Float
				return Fval(((AnimationController)args[0]).getPosition(ival(args[1])));
			case 8: // AnimationController.setWeight(weight: Float)
				((AnimationController)args[0]).setWeight(fval(args[1]));
				return null;
			case 9: // AnimationController.getWeight(): Float
				return Fval(((AnimationController)args[0]).getWeight());
			case 10: // AnimationTrack.new(seq: KeyframeSequence, property: Int)
				return new AnimationTrack((KeyframeSequence)args[0], ival(args[1]));
			case 11: // AnimationTrack.setController(controller: AnimationController)
				((AnimationTrack)args[0]).setController((AnimationController)args[1]);
				return null;
			case 12: // AnimationTrack.getController(): AnimationController
				return ((AnimationTrack)args[0]).getController();
			case 13: // AnimationTrack.getKeyframeSequence(): KeyframeSequence
				return ((AnimationTrack)args[0]).getKeyframeSequence();
			case 14: // AnimationTrack.getTargetProperty(): Int
				return Ival(((AnimationTrack)args[0]).getTargetProperty());
			case 15: // Appearance.new()
				return new Appearance();
			case 16: // Appearance.setLayer(layer: Int)
				((Appearance)args[0]).setLayer(ival(args[1]));
				return null;
			case 17: // Appearance.getLayer(): Int
				return Ival(((Appearance)args[0]).getLayer());
			case 18: // Appearance.setFog(fog: Fog)
				((Appearance)args[0]).setFog((Fog)args[1]);
				return null;
			case 19: // Appearance.getFog(): Fog
				return ((Appearance)args[0]).getFog();
			case 20: // Appearance.setPolygonMode(mode: PolygonMode)
				((Appearance)args[0]).setPolygonMode((PolygonMode)args[1]);
				return null;
			case 21: // Appearance.getPolygonMode(): PolygonMode
				return ((Appearance)args[0]).getPolygonMode();
			case 22: // Appearance.setCompositingMode(mode: CompositingMode)
				((Appearance)args[0]).setCompositingMode((CompositingMode)args[1]);
				return null;
			case 23: // Appearance.getCompositingMode(): CompositingMode
				return ((Appearance)args[0]).getCompositingMode();
			case 24: // Appearance.setTexture(index: Int, texture: Texture2D)
				((Appearance)args[0]).setTexture(ival(args[1]), (Texture2D)args[2]);
				return null;
			case 25: // Appearance.getTexture(index: Int): Texture2D
				return ((Appearance)args[0]).getTexture(ival(args[1]));
			case 26: // Appearance.setMaterial(material: Material)
				((Appearance)args[0]).setMaterial((Material)args[1]);
				return null;
			case 27: // Appearance.getMaterial(): Material
				return ((Appearance)args[0]).getMaterial();
			case 28: //	Background.new()
				return new Background();
			case 29: // Background.setColorClearEnable(enable: Bool)
				((Background)args[0]).setColorClearEnable(bval(args[1]));
				return null;
			case 30: // Background.isColorClearEnabled(): Bool
				return Ival(((Background)args[0]).isColorClearEnabled());
			case 31: // Background.setDepthClearEnable(enable: Bool)
				((Background)args[0]).setDepthClearEnable(bval(args[1]));
				return null;
			case 32: // Background.isDepthClearEnabled(): Bool
				return Ival(((Background)args[0]).isDepthClearEnabled());
			case 33: // Background.setColor(argb: Int)
				((Background)args[0]).setColor(ival(args[1]));
				return null;
			case 34: // Background.getColor(): Int
				return Ival(((Background)args[0]).getColor());
			case 35: // Background.setImage(image: Image2D)
				((Background)args[0]).setImage((Image2D)args[1]);
				return null;
			case 36: // Background.getImage(): Image2D
				return ((Background)args[0]).getImage();
			case 37: // Background.setImageMode(modeX: Int, modeY: Int)
				((Background)args[0]).setImageMode(ival(args[1]), ival(args[2]));
				return null;
			case 38: // Background.getImageModeX(): Int
				return Ival(((Background)args[0]).getImageModeX());
			case 39: // Background.getImageModeY(): Int
				return Ival(((Background)args[0]).getImageModeY());
			case 40: // Background.setCrop(cropX: Int, cropY: Int, w: Int, h: Int)
				((Background)args[0]).setCrop(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 41: // Background.getCropX(): Int
				return Ival(((Background)args[0]).getCropX());
			case 42: // Background.getCropY(): Int
				return Ival(((Background)args[0]).getCropY());
			case 43: // Background.getCropWidth(): Int
				return Ival(((Background)args[0]).getCropWidth());
			case 44: // Background.getCropHeight(): Int
				return Ival(((Background)args[0]).getCropHeight());
			case 45: //	Camera.new()
				return new Camera();
			case 46: // Camera.setParallel(fovy: Float, aspectRatio: Float, near: Float, far: Float)
				((Camera)args[0]).setParallel(fval(args[1]), fval(args[2]), fval(args[3]), fval(args[4]));
				return null;
			case 47: // Camera.setPerspective(fovy: Float, aspectRatio: Float, near: Float, far: Float)
				((Camera)args[0]).setPerspective(fval(args[1]), fval(args[2]), fval(args[3]), fval(args[4]));
				return null;
			case 48: // Camera.setGeneric(transform: Transform)
				((Camera)args[0]).setGeneric((Transform)args[1]);
				return null;
			case 49: // Camera.getProjection(transform: Transform): Int
				return Ival(((Camera)args[0]).getProjection((Transform)args[1]));
			case 50: // CompositingMode.new()
				return new CompositingMode();
			case 51: // CompositingMode.setBlending(mode: Int)
				((CompositingMode)args[0]).setBlending(ival(args[1]));
				return null;
			case 52: // CompositingMode.getBlending(): Int
				return Ival(((CompositingMode)args[0]).getBlending());
			case 53: // CompositingMode.setAlphaThreshold(threshold: Float)
				((CompositingMode)args[0]).setAlphaThreshold(fval(args[1]));
				return null;
			case 54: // CompositingMode.getAlphaThreshold(): Float
				return Fval(((CompositingMode)args[0]).getAlphaThreshold());
			case 55: // CompositingMode.setAlphaWriteEnable(enable: Bool)
				((CompositingMode)args[0]).setAlphaWriteEnable(bval(args[1]));
				return null;
			case 56: // CompositingMode.isAlphaWriteEnabled(): Bool
				return Ival(((CompositingMode)args[0]).isAlphaWriteEnabled());
			case 57: // CompositingMode.setColorWriteEnable(enable: Bool)
				((CompositingMode)args[0]).setColorWriteEnable(bval(args[1]));
				return null;
			case 58: // CompositingMode.isColorWriteEnabled(): Bool
				return Ival(((CompositingMode)args[0]).isColorWriteEnabled());
			case 59: // CompositingMode.setDepthWriteEnable(enable: Bool)
				((CompositingMode)args[0]).setDepthWriteEnable(bval(args[1]));
				return null;
			case 60: // CompositingMode.isDepthWriteEnabled(): Bool
				return Ival(((CompositingMode)args[0]).isDepthWriteEnabled());
			case 61: // CompositingMode.setDepthTestEnable(enable: Bool)
				((CompositingMode)args[0]).setDepthTestEnable(bval(args[1]));
				return null;
			case 62: // CompositingMode.isDepthTestEnabled(): Bool
				return Ival(((CompositingMode)args[0]).isDepthTestEnabled());
			case 63: // CompositingMode.setDepthOffset(factor: Float, units: Float)
				((CompositingMode)args[0]).setDepthOffset(fval(args[1]), fval(args[2]));
				return null;
			case 64: // CompositingMode.getDepthOffsetFactor(): Float
				return Fval(((CompositingMode)args[0]).getDepthOffsetFactor());
			case 65: // CompositingMode.getDepthOffsetUnits(): Float
				return Fval(((CompositingMode)args[0]).getDepthOffsetUnits());
			case 66: // Fog.new()
				return new Fog();
			case 67: // Fog.setMode(mode: Int)
				((Fog)args[0]).setMode(ival(args[1]));
				return null;
			case 68: // Fog.getMode(): Int
				return Ival(((Fog)args[0]).getMode());
			case 69: // Fog.setLinear(near: Float, far: Float)
				((Fog)args[0]).setLinear(fval(args[1]), fval(args[2]));
				return null;
			case 70: // Fog.getNearDistance(): Float
				return Fval(((Fog)args[0]).getNearDistance());
			case 71: // Fog.getFarDistance(): Float
				return Fval(((Fog)args[0]).getFarDistance());
			case 72: // Fog.setDensity(density: Float)
				((Fog)args[0]).setDensity(fval(args[1]));
				return null;
			case 73: // Fog.getDensity(): Float
				return Fval(((Fog)args[0]).getDensity());
			case 74: // Fog.setColor(rgb: Int)
				((Fog)args[0]).setColor(ival(args[1]));
				return null;
			case 75: // Fog.getColor(): Int
				return Ival(((Fog)args[0]).getColor());
			case 76: // m3g_isUsed(): Bool
				return Ival(owner != null);
			case 77: // m3g_bindTarget(target: Any, depthBuffer: Bool, hints: Int)
				graphics3D(c).bindTarget(args[0], bval(args[1]), ival(args[2]));
				return null;
			case 78: // m3g_releaseTarget()
				graphics3D(c).releaseTarget();
				return null;
			case 79: // m3g_setViewport(x: Int, y: Int, w: Int, h: Int)
				graphics3D(c).setViewport(ival(args[0]), ival(args[1]), ival(args[2]), ival(args[3]));
				return null;
			case 80: // m3g_setDepthRange(near: Float, far: Float)
				graphics3D(c).setDepthRange(fval(args[0]), fval(args[1]));
				return null;
			case 81: // m3g_clear(background: Background)
				graphics3D(c).clear((Background)args[0]);
				return null;
			case 82: // m3g_renderWorld(world: World)
				graphics3D(c).render((World)args[0]);
				return null;
			case 83: // m3g_renderNode(node: Node, transform: Transform)
				graphics3D(c).render((Node)args[0], (Transform)args[1]);
				return null;
			case 84: // m3g_render(vertices: VertexBuffer, triangles: IndexBuffer, appearance: Appearance, transform: Transform, scope: Int)
				graphics3D(c).render((VertexBuffer)args[0], (IndexBuffer)args[1], (Appearance)args[2], (Transform)args[3], ival(args[4]));
				return null;
			case 85: // m3g_setCamera(camera: Camera, transform: Transform)
				graphics3D(c).setCamera((Camera)args[0], (Transform)args[1]);
				return null;
			case 86: // m3g_addLight(light: Light, transform: Transform): Int
				return Ival(graphics3D(c).addLight((Light)args[0], (Transform)args[1]));
			case 87: // m3g_setLight(index: Int, light: Light, transform: Transform)
				graphics3D(c).setLight(ival(args[0]), (Light)args[1], (Transform)args[2]);
				return null;
			case 88: // m3g_resetLights()
				graphics3D(c).resetLights();
				return null;
			case 89: // m3g_getProperties(): Dict
				return Graphics3D.getProperties();
			case 90: // m3g_loadData(data: [Byte], offset: Int): [Object3D]
				return Loader.load((byte[])args[0], ival(args[1]));
			case 91: { // m3g_loadFile(name: String): [Object3D]
				InputStream in = null;
				try {
					in = FSManager.fs().read((String)args[0]);
					return Loader.load(IO.readFully(in), 0);
				} finally {
					if (in != null) try {
						in.close();
					} catch (IOException ioe) { }
				}
			}
			case 92: // Group.new()
				return new Group();
			case 93: // Group.addChild(child: Node)
				((Group)args[0]).addChild((Node)args[1]);
				return null;
			case 94: // Group.removeChild(child: Node)
				((Group)args[0]).removeChild((Node)args[1]);
				return null;
			case 95: // Group.getChildCount(): Int
				return Ival(((Group)args[0]).getChildCount());
			case 96: // Group.getChild(index: Int): Node
				return ((Group)args[0]).getChild(ival(args[1]));
			case 97: // Group.pickByRay(scope: Int, ox: Float, oy: Float, oz: Float, dx: Float, dy: Float, dz: Float, ri: RayIntersection): Bool
				return Ival(((Group)args[0]).pick(ival(args[1]), fval(args[2]), fval(args[3]), fval(args[4]), fval(args[5]), fval(args[6]), fval(args[7]), (RayIntersection)args[8]));
			case 98: // Group.pickByCamera(scope: Int, x: Float, y: Float, camera: Camera, ri: RayIntersection): Bool
				return Ival(((Group)args[0]).pick(ival(args[1]), fval(args[2]), fval(args[3]), (Camera)args[4], (RayIntersection)args[5]));
			case 99: // createImage2D(format: Int, image: Image): Image2D
				return new Image2D(ival(args[0]), args[1]);
			case 100: // Image2D.new(format: Int, width: Int, height: Int)
				return new Image2D(ival(args[0]), ival(args[1]), ival(args[2]));
			case 101: // Image2D.set(x: Int, y: Int, width: Int, height: Int, image: [Byte])
				((Image2D)args[0]).set(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]), (byte[])args[5]);
				return null;
			case 102: // Image2D.isMutable(): Bool
				return Ival(((Image2D)args[0]).isMutable());
			case 103: // Image2D.getFormat(): Int
				return Ival(((Image2D)args[0]).getFormat());
			case 104: // Image2D.getWidth(): Int
				return Ival(((Image2D)args[0]).getWidth());
			case 105: // Image2D.getHeight(): Int
				return Ival(((Image2D)args[0]).getHeight());
			case 106: // KeyframeSequence.new(numKeyframes: Int, numComponents: Int, interpolation: Int)
				return new KeyframeSequence(ival(args[0]), ival(args[1]), ival(args[2]));
			case 107: // KeyframeSequence.setKeyframe(index: Int, time: Int, value: [Float])
				((KeyframeSequence)args[0]).setKeyframe(ival(args[1]), ival(args[2]), (float[])args[3]);
				return null;
			case 108: // KeyframeSequence.setValidRange(first: Int, last: Int)
				((KeyframeSequence)args[0]).setValidRange(ival(args[1]), ival(args[2]));
				return null;
			case 109: // KeyframeSequence.setDuration(duration: Int)
				((KeyframeSequence)args[0]).setDuration(ival(args[1]));
				return null;
			case 110: // KeyframeSequence.getDuration(): Int
				return Ival(((KeyframeSequence)args[0]).getDuration());
			case 111: // KeyframeSequence.setRepeatMode(mode: Int)
				((KeyframeSequence)args[0]).setRepeatMode(ival(args[1]));
				return null;
			case 112: // KeyframeSequence.getRepeatMode(): Int
				return Ival(((KeyframeSequence)args[0]).getRepeatMode());
			case 113: // Light.new()
				return new Light();
			case 114: // Light.setMode(mode: Int)
				((Light)args[0]).setMode(ival(args[1]));
				return null;
			case 115: // Light.getMode(): Int
				return Ival(((Light)args[0]).getMode());
			case 116: // Light.setIntensity(intensity: Float)
				((Light)args[0]).setIntensity(fval(args[1]));
				return null;
			case 117: // Light.getIntensity(): Float
				return Fval(((Light)args[0]).getIntensity());
			case 118: // Light.setColor(rgb: Int)
				((Light)args[0]).setColor(ival(args[1]));
				return null;
			case 119: // Light.getColor(): Int
				return Ival(((Light)args[0]).getColor());
			case 120: // Light.setSpotAngle(angle: Float)
				((Light)args[0]).setSpotAngle(fval(args[1]));
				return null;
			case 121: // Light.getSpotAngle(): Float
				return Fval(((Light)args[0]).getSpotAngle());
			case 122: // Light.setSpotExponent(exponent: Float)
				((Light)args[0]).setSpotExponent(fval(args[1]));
				return null;
			case 123: // Light.getSpotExponent(): Float
				return Fval(((Light)args[0]).getSpotExponent());
			case 124: // Light.setAttenuation(constant: Float, linear: Float, quadratic: Float)
				((Light)args[0]).setAttenuation(fval(args[1]), fval(args[2]), fval(args[3]));
				return null;
			case 125: // Light.getConstantAttenuation(): Float
				return Fval(((Light)args[0]).getConstantAttenuation());
			case 126: // Light.getLinearAttenuation(): Float
				return Fval(((Light)args[0]).getLinearAttenuation());
			case 127: // Light.getQuadraticAttenuation(): Float
				return Fval(((Light)args[0]).getQuadraticAttenuation());
			case 128: // Material.new()
				return new Material();
			case 129: // Material.setColor(target: Int, argb: Int)
				((Material)args[0]).setColor(ival(args[1]), ival(args[2]));
				return null;
			case 130: // Material.getColor(target: Int): Int
				return Ival(((Material)args[0]).getColor(ival(args[1])));
			case 131: // Material.setShininess(shininess: Float)
				((Material)args[0]).setShininess(fval(args[1]));
				return null;
			case 132: // Material.getShininess(): Float
				return Fval(((Material)args[0]).getShininess());
			case 133: // Material.setVertexColorTrackingEnable(enable: Bool)
				((Material)args[0]).setVertexColorTrackingEnable(bval(args[1]));
				return null;
			case 134: // Material.isVertexColorTrackingEnabled(): Bool
				return Ival(((Material)args[0]).isVertexColorTrackingEnabled());
			case 135: { // Mesh.new(vertices: VertexBuffer, submeshes: [IndexBuffer], appearances: [Appearance])
				Object[] objSubmeshes = (Object[])args[1];
				IndexBuffer[] submeshes = null;
				if (objSubmeshes != null) {
					submeshes = new IndexBuffer[objSubmeshes.length];
					System.arraycopy(objSubmeshes, 0, submeshes, 0, objSubmeshes.length);
				}
				Object[] objAppearances = (Object[])args[2];
				Appearance[] appearances = null;
				if (objAppearances != null) {
					appearances = new Appearance[objAppearances.length];
					System.arraycopy(objAppearances, 0, appearances, 0, objAppearances.length);
				}
				return new Mesh((VertexBuffer)args[0], submeshes, appearances);
			}
			case 136: // Mesh.setAppearance(index: Int, appearance: Appearance)
				((Mesh)args[0]).setAppearance(ival(args[1]), (Appearance)args[2]);
				return null;
			case 137: // Mesh.getAppearance(index: Int): Appearance
				return ((Mesh)args[0]).getAppearance(ival(args[1]));
			case 138: // Mesh.getIndexBuffer(index: Int): IndexBuffer
				return ((Mesh)args[0]).getIndexBuffer(ival(args[1]));
			case 139: // Mesh.getVertexBuffer(): VertexBuffer
				return ((Mesh)args[0]).getVertexBuffer();
			case 140: // Mesh.getSubmeshCount(): Int
				return Ival(((Mesh)args[0]).getSubmeshCount());
			case 141: { // MorphingMesh.new(base: VertexBuffer, targets: [VertexBuffer], submeshes: [IndexBuffer], appearances: [Appearance]);
				Object[] objTargets = (Object[])args[1];
				VertexBuffer[] targets = null;
				if (objTargets != null) {
					targets = new VertexBuffer[objTargets.length];
					System.arraycopy(objTargets, 0, targets, 0, objTargets.length);
				}
				Object[] objSubmeshes = (Object[])args[2];
				IndexBuffer[] submeshes = null;
				if (objSubmeshes != null) {
					submeshes = new IndexBuffer[objSubmeshes.length];
					System.arraycopy(objSubmeshes, 0, submeshes, 0, objSubmeshes.length);
				}
				Object[] objAppearances = (Object[])args[3];
				Appearance[] appearances = null;
				if (objAppearances != null) {
					appearances = new Appearance[objAppearances.length];
					System.arraycopy(objAppearances, 0, appearances, 0, objAppearances.length);
				}
				return new MorphingMesh((VertexBuffer)args[0], targets, submeshes, appearances);
			}
			case 142: // MorphingMesh.getMorphTarget(index: Int): VertexBuffer
				return ((MorphingMesh)args[0]).getMorphTarget(ival(args[1]));
			case 143: // MorphingMesh.getMorphTargetCount(): Int
				return Ival(((MorphingMesh)args[0]).getMorphTargetCount());
			case 144: // MorphingMesh.setWeights(weights: [Float])
				((MorphingMesh)args[0]).setWeights((float[])args[1]);
				return null;
			case 145: // MorphingMesh.getWeights(weights: [Float])
				((MorphingMesh)args[0]).getWeights((float[])args[1]);
				return null;
			case 146: // Node.setRenderingEnable(enable: Bool)
				((Node)args[0]).setRenderingEnable(bval(args[1]));
				return null;
			case 147: // Node.isRenderingEnabled(): Bool
				return Ival(((Node)args[0]).isRenderingEnabled());
			case 148: // Node.setPickingEnable(enable: Bool)
				((Node)args[0]).setPickingEnable(bval(args[1]));
				return null;
			case 149: // Node.isPickingEnabled(): Bool
				return Ival(((Node)args[0]).isPickingEnabled());
			case 150: // Node.setScope(scope: Int)
				((Node)args[0]).setScope(ival(args[1]));
				return null;
			case 151: // Node.getScope(): Int
				return Ival(((Node)args[0]).getScope());
			case 152: // Node.setAlphaFactor(alphaFactor: Float)
				((Node)args[0]).setAlphaFactor(fval(args[1]));
				return null;
			case 153: // Node.getAlphaFactor(): Float
				return Fval(((Node)args[0]).getAlphaFactor());
			case 154: // Node.getParent(): Node
				return ((Node)args[0]).getParent();
			case 155: // Node.getTransformTo(target: Node, transform: Transform): Bool
				return Ival(((Node)args[0]).getTransformTo((Node)args[1], (Transform)args[2]));
			case 156: // Node.align(reference: Node)
				((Node)args[0]).align((Node)args[1]);
				return null;
			case 157: // Node.setAlignment(zRef: Node, zTarget: Int, yRef: Node, yTarget: Int)
				((Node)args[0]).setAlignment((Node)args[1], ival(args[2]), (Node)args[3], ival(args[4]));
				return null;
			case 158: // Object3D.animate(time: Int): Int
				return Ival(((Object3D)args[0]).animate(ival(args[1])));
			case 159: // Object3D.duplicate(): Object3D
				return ((Object3D)args[0]).duplicate();
			case 160: // Object3D.find(userID: Int): Object3D
				return ((Object3D)args[0]).find(ival(args[1]));
			case 161: { // Object3D.getReferences(refs: [Object3D]): Int
				Object[] objRefs = (Object[])args[1];
				Object3D[] refs = null;
				if (objRefs != null) refs = new Object3D[objRefs.length];
				int result = ((Object3D)args[0]).getReferences(refs);
				if (objRefs != null) System.arraycopy(refs, 0, objRefs, 0, refs.length);
				return Ival(result);
			}
			case 162: // Object3D.setUserID(userID: Int)
				((Object3D)args[0]).setUserID(ival(args[1]));
				return null;
			case 163: // Object3D.getUserID(): Int
				return Ival(((Object3D)args[0]).getUserID());
			case 164: // Object3D.setUserObject(userObj: Any)
				((Object3D)args[0]).setUserObject(args[1]);
				return null;
			case 165: // Object3D.getUserObject(): Any
				return ((Object3D)args[0]).getUserObject();
			case 166: // Object3D.addAnimationTrack(track: AnimationTrack)
				((Object3D)args[0]).addAnimationTrack((AnimationTrack)args[1]);
				return null;
			case 167: // Object3D.getAnimationTrack(index: Int)
				return ((Object3D)args[0]).getAnimationTrack(ival(args[1]));
			case 168: // Object3D.removeAnimationTrack(track: AnimationTrack)
				((Object3D)args[0]).removeAnimationTrack((AnimationTrack)args[1]);
				return null;
			case 169: // Object3D.getAnimationTrackCount(): Int
				return Ival(((Object3D)args[0]).getAnimationTrackCount());
			case 170: // PolygonMode.new()
				return new PolygonMode();
			case 171: // PolygonMode.setCulling(mode: Int)
				((PolygonMode)args[0]).setCulling(ival(args[1]));
				return null;
			case 172: // PolygonMode.getCulling(): Int
				return Ival(((PolygonMode)args[0]).getCulling());
			case 173: // PolygonMode.setWinding(mode: Int)
				((PolygonMode)args[0]).setWinding(ival(args[1]));
				return null;
			case 174: // PolygonMode.getWinding(): Int
				return Ival(((PolygonMode)args[0]).getWinding());
			case 175: // PolygonMode.setShading(mode: Int)
				((PolygonMode)args[0]).setShading(ival(args[1]));
				return null;
			case 176: // PolygonMode.getShading(): Int
				return Ival(((PolygonMode)args[0]).getShading());
			case 177: // PolygonMode.setTwoSidedLightingEnable(enable: Bool)
				((PolygonMode)args[0]).setTwoSidedLightingEnable(bval(args[1]));
				return null;
			case 178: // PolygonMode.isTwoSidedLightingEnabled(): Bool
				return Ival(((PolygonMode)args[0]).isTwoSidedLightingEnabled());
			case 179: // PolygonMode.setLocalCameraLightingEnable(enable: Bool)
				((PolygonMode)args[0]).setLocalCameraLightingEnable(bval(args[1]));
				return null;
			case 180: // PolygonMode.setPerspectiveCorrectionEnable(enable: Bool)
				((PolygonMode)args[0]).setPerspectiveCorrectionEnable(bval(args[1]));
				return null;
			case 181: // RayIntersection.new()
				return new RayIntersection();
			case 182: // RayIntersection.getIntersected(): Node
				return ((RayIntersection)args[0]).getIntersected();
			case 183: // RayIntersection.getRay(ray: [Float])
				((RayIntersection)args[0]).getRay((float[])args[1]);
				return null;
			case 184: // RayIntersection.getDistance(): Float
				return Fval(((RayIntersection)args[0]).getDistance());
			case 185: // RayIntersection.getSubmeshIndex(): Int
				return Ival(((RayIntersection)args[0]).getSubmeshIndex());
			case 186: // RayIntersection.getTextureS(index: Int): Float
				return Fval(((RayIntersection)args[0]).getTextureS(ival(args[1])));
			case 187: // RayIntersection.getTextureT(index: Int): Float
				return Fval(((RayIntersection)args[0]).getTextureT(ival(args[1])));
			case 188: // RayIntersection.getNormalX(): Float
				return Fval(((RayIntersection)args[0]).getNormalX());
			case 189: // RayIntersection.getNormalY(): Float
				return Fval(((RayIntersection)args[0]).getNormalY());
			case 190: // RayIntersection.getNormalZ(): Float
				return Fval(((RayIntersection)args[0]).getNormalZ());
			case 191: { // SkinnedMesh.new(vertices: VertexBuffer, submeshes: [IndexBuffer], appearances: [Appearance], skeleton: Group)
				Object[] objSubmeshes = (Object[])args[1];
				IndexBuffer[] submeshes = null;
				if (objSubmeshes != null) {
					submeshes = new IndexBuffer[objSubmeshes.length];
					System.arraycopy(objSubmeshes, 0, submeshes, 0, objSubmeshes.length);
				}
				Object[] objAppearances = (Object[])args[2];
				Appearance[] appearances = null;
				if (objAppearances != null) {
					appearances = new Appearance[objAppearances.length];
					System.arraycopy(objAppearances, 0, appearances, 0, objAppearances.length);
				}
				return new SkinnedMesh((VertexBuffer)args[0], submeshes, appearances, (Group)args[3]);
			}
			case 192: // SkinnedMesh.getSkeleton(): Group
				return ((SkinnedMesh)args[0]).getSkeleton();
			case 193: // SkinnedMesh.addTransform(bone: Node, weight: Int, firstVertex: Int, numVertices: Int)
				((SkinnedMesh)args[0]).addTransform((Node)args[1], ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 194: // Sprite3D.new(scaled: Bool, image: Image2D, appearance: Appearance)
				return new Sprite3D(bval(args[0]), (Image2D)args[1], (Appearance)args[2]);
			case 195: // Sprite3D.isScaled(): Bool
				return Ival(((Sprite3D)args[0]).isScaled());
			case 196: // Sprite3D.setAppearance(appearance: Appearance)
				((Sprite3D)args[0]).setAppearance((Appearance)args[1]);
				return null;
			case 197: // Sprite3D.getAppearance(): Appearance
				return ((Sprite3D)args[0]).getAppearance();
			case 198: // Sprite3D.setImage(image: Image2D)
				((Sprite3D)args[0]).setImage((Image2D)args[1]);
				return null;
			case 199: // Sprite3D.getImage(): Image2D
				return ((Sprite3D)args[0]).getImage();
			case 200: // Sprite3D.setCrop(cropX: Int, cropY: Int, width: Int, height: Int)
				((Sprite3D)args[0]).setCrop(ival(args[1]), ival(args[2]), ival(args[3]), ival(args[4]));
				return null;
			case 201: // Sprite3D.getCropX(): Int
				return Ival(((Sprite3D)args[0]).getCropX());
			case 202: // Sprite3D.getCropY(): Int
				return Ival(((Sprite3D)args[0]).getCropY());
			case 203: // Sprite3D.getCropWidth(): Int
				return Ival(((Sprite3D)args[0]).getCropWidth());
			case 204: // Sprite3D.getCropHeight(): Int
				return Ival(((Sprite3D)args[0]).getCropHeight());
			case 205: // Texture2D.new(image: Image2D)
				return new Texture2D((Image2D)args[0]);
			case 206: // Texture2D.setImage(image: Image2D)
				((Texture2D)args[0]).setImage((Image2D)args[1]);
				return null;
			case 207: // Texture2D.getImage(): Image2D
				return ((Texture2D)args[0]).getImage();
			case 208: // Texture2D.setFiltering(levelFilter: Int, imageFilter: Int)
				((Texture2D)args[0]).setFiltering(ival(args[1]), ival(args[2]));
				return null;
			case 209: // Texture2D.setWrapping(wrapS: Int, wrapT: Int)
				((Texture2D)args[0]).setWrapping(ival(args[1]), ival(args[2]));
				return null;
			case 210: // Texture2D.getWrappingS(): Int
				return Ival(((Texture2D)args[0]).getWrappingS());
			case 211: // Texture2D.getWrappingT(): Int
				return Ival(((Texture2D)args[0]).getWrappingT());
			case 212: // Texture2D.setBlending(func: Int)
				((Texture2D)args[0]).setBlending(ival(args[1]));
				return null;
			case 213: // Texture2D.getBlending(): Int
				return Ival(((Texture2D)args[0]).getBlending());
			case 214: // Texture2D.setBlendColor(rgb: Int)
				((Texture2D)args[0]).setBlendColor(ival(args[1]));
				return null;
			case 215: // Texture2D.getBlendColor(): Int
				return Ival(((Texture2D)args[0]).getBlendColor());
			case 216: // Transform.new()
				return new Transform();
			case 217: // Transform.clone(): Transform
				return new Transform((Transform)args[0]);
			case 218: // Transform.setIdentity()
				((Transform)args[0]).setIdentity();
				return null;
			case 219: // Transform.set(matrix: [Float])
				((Transform)args[0]).set((float[])args[1]);
				return null;
			case 220: // Transform.get(matrix: [Float])
				((Transform)args[0]).get((float[])args[0]);
				return null;
			case 221: // Transform.invert()
				((Transform)args[0]).invert();
				return null;
			case 222: // Transform.transpose()
				((Transform)args[0]).transpose();
				return null;
			case 223: // Transform.postMultiply(transform: Transform)
				((Transform)args[0]).postMultiply((Transform)args[1]);
				return null;
			case 224: // Transform.postScale(sx: Float, sy: Float, sz: Float)
				((Transform)args[0]).postScale(fval(args[1]), fval(args[2]), fval(args[3]));
				return null;
			case 225: // Transform.postRotate(angle: Float, ax: Float, ay: Float, az: Float)
				((Transform)args[0]).postRotate(fval(args[1]), fval(args[2]), fval(args[3]), fval(args[4]));
				return null;
			case 226: // Transform.postRotateQuat(qx: Float, qy: Float, qz: Float, qw: Float)
				((Transform)args[0]).postRotateQuat(fval(args[1]), fval(args[2]), fval(args[3]), fval(args[4]));
				return null;
			case 227: // Transform.postTranslate(tx: Float, ty: Float, tz: Float)
				((Transform)args[0]).postTranslate(fval(args[1]), fval(args[2]), fval(args[3]));
				return null;
			case 228: // Transform.transform(in: VertexArray, out: [Float], w: Bool)
				((Transform)args[0]).transform((float[])args[1]);
				return null;
			case 229: // Transformable.setOrientation(angle: Float, ax: Float, ay: Float, az: Float)
				((Transformable)args[0]).setOrientation(fval(args[1]), fval(args[2]), fval(args[3]), fval(args[4]));
				return null;
			case 230: // Transformable.preRotate(angle: Float, ax: Float, ay: Float, az: Float)
				((Transformable)args[0]).preRotate(fval(args[1]), fval(args[2]), fval(args[3]), fval(args[4]));
				return null;
			case 231: // Transformable.postRotate(angle: Float, ax: Float, ay: Float, az: Float)
				((Transformable)args[0]).postRotate(fval(args[1]), fval(args[2]), fval(args[3]), fval(args[4]));
				return null;
			case 232: // Transformable.getOrientation(angleAxis: [Float])
				((Transformable)args[0]).getOrientation((float[])args[1]);
				return null;
			case 233: // Transformable.setScale(sx: Float, sy: Float, sz: Float)
				((Transformable)args[0]).setScale(fval(args[1]), fval(args[2]), fval(args[3]));
				return null;
			case 234: // Transformable.scale(sx: Float, sy: Float, sz: Float)
				((Transformable)args[0]).scale(fval(args[1]), fval(args[2]), fval(args[3]));
				return null;
			case 235: // Transformable.getScale(xyz: [Float])
				((Transformable)args[0]).getScale((float[])args[1]);
				return null;
			case 236: // Transformable.setTranslation(tx: Float, ty: Float, tz: Float)
				((Transformable)args[0]).setTranslation(fval(args[1]), fval(args[2]), fval(args[3]));
				return null;
			case 237: // Transformable.translate(tx: Float, ty: Float, tz: Float)
				((Transformable)args[0]).translate(fval(args[1]), fval(args[2]), fval(args[3]));
				return null;
			case 238: // Transformable.getTranslation(xyz: [Float])
				((Transformable)args[0]).getTranslation((float[])args[1]);
				return null;
			case 239: // Transformable.setTransform(transform: Transform)
				((Transformable)args[0]).setTransform((Transform)args[1]);
				return null;
			case 240: // Transformable.getTransform(transform: Transform)
				((Transformable)args[0]).getTransform((Transform)args[1]);
				return null;
			case 241: // Transformable.getCompositeTransform(transform: Transform)
				((Transformable)args[0]).getCompositeTransform((Transform)args[1]);
				return null;
			case 242: // TriangleStripArray.new(indices: [Int], stripLengths: [Int])
				return new TriangleStripArray((int[])args[0], (int[])args[1]);
			case 243: // VertexArray.new(numVertices: Int, numComponents: Int, componentSize: Int)
				return new VertexArray(ival(args[0]), ival(args[1]), ival(args[2]));
			case 244: { // VertexArray.set(firstVertex: Int, numVertices: Int, values: Array)
				Object array = args[2];
				if (array instanceof byte[])
					((VertexArray)args[0]).set(ival(args[1]), ival(args[2]), (byte[])array);
				else if (array instanceof short[])
					((VertexArray)args[0]).set(ival(args[1]), ival(args[2]), (short[])array);
				else
					throw new IllegalArgumentException("[Byte] or [Short] array required");
				return null;
			}
			case 245: // VertexBuffer.new()
				return new VertexBuffer();
			case 246: // VertexBuffer.getVertexCount(): Int
				return Ival(((VertexBuffer)args[0]).getVertexCount());
			case 247: // VertexBuffer.setPositions(positions: VertexArray, scale: Float, bias: [Float])
				((VertexBuffer)args[0]).setPositions((VertexArray)args[1], fval(args[2]), (float[])args[3]);
				return null;
			case 248: // VertexBuffer.setTexCoords(index: Int, texCoords: VertexArray, scale: Float, bias: [Float])
				((VertexBuffer)args[0]).setTexCoords(ival(args[1]), (VertexArray)args[2], fval(args[3]), (float[])args[4]);
				return null;
			case 249: // VertexBuffer.setNormals(normals: VertexArray)
				((VertexBuffer)args[0]).setNormals((VertexArray)args[1]);
				return null;
			case 250: // VertexBuffer.setColors(colors: VertexArray)
				((VertexBuffer)args[0]).setColors((VertexArray)args[1]);
				return null;
			case 251: // VertexBuffer.getPositions(scaleBias: [Float]): VertexArray
				return ((VertexBuffer)args[0]).getPositions((float[])args[1]);
			case 252: // VertexBuffer.getTexCoords(index: Int, scaleBias: [Float]): VertexArray
				return ((VertexBuffer)args[0]).getTexCoords(ival(args[1]), (float[])args[2]);
			case 253: // VertexBuffer.getNormals(): VertexArray
				return ((VertexBuffer)args[0]).getNormals();
			case 254: // VertexBuffer.getColors(): VertexArray
				return ((VertexBuffer)args[0]).getColors();
			case 255: // VertexBuffer.setDefaultColor(argb: Int)
				((VertexBuffer)args[0]).setDefaultColor(ival(args[1]));
				return null;
			case 256: // VertexBuffer.getDefaultColor(): Int
				return Ival(((VertexBuffer)args[0]).getDefaultColor());
			case 257: // World.new()
				return new World();
			case 258: // World.setBackground(background: Background)
				((World)args[0]).setBackground((Background)args[1]);
				return null;
			case 259: // World.getBackground(): Background
				return ((World)args[0]).getBackground();
			case 260: // World.setActiveCamera(camera: Camera)
				((World)args[0]).setActiveCamera((Camera)args[1]);
				return null;
			case 261: // World.getActiveCamera(): Camera
				return ((World)args[0]).getActiveCamera();
			default:
				throw new RuntimeException("Invalid function");
		}
	}

	public String soname() {
		return "libm3g.1.so";
	}
	
	private static class M3GContextListener implements ContextListener {

		public void contextEnded(Context c) {
			final Graphics3D graphics = Graphics3D.getInstance();
			synchronized (graphics) {
				graphics.releaseTarget();
				graphics.setDepthRange(0f, 1f);
				graphics.setCamera(null, null);
				graphics.resetLights();
				owner = null;
			}
		}
	}
}
