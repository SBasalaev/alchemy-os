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

import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import java.io.IOException;
import javax.microedition.m3g.AnimationController;
import javax.microedition.m3g.IndexBuffer;
import javax.microedition.m3g.KeyframeSequence;
import javax.microedition.m3g.Node;
import javax.microedition.m3g.PolygonMode;
import javax.microedition.m3g.SkinnedMesh;
import javax.microedition.m3g.Texture2D;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.VertexArray;

/**
 * Functions introduced in M3G 1.1 (JSR-184).
 *
 * @author Sergey Basalaev
 */
public final class LibM3G_ext11 extends NativeLibrary {

	public LibM3G_ext11() throws IOException {
		load("/symbols/m3g-11");
		name = "libm3g_ext.1.so";
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 0: // AnimationController.getRefWorldTime(): Int
				return Ival(((AnimationController)args[0]).getRefWorldTime());
			case 1: // m3g_getTarget(): Any
				return LibM3G.graphics3D(p).getTarget();
			case 2: // m3g_getHints(): Int
				return Ival(LibM3G.graphics3D(p).getHints());
			case 3: // m3g_isDepthBufferEnabled(): Bool
				return Ival(LibM3G.graphics3D(p).isDepthBufferEnabled());
			case 4: // m3g_getViewportX(): Int
				return Ival(LibM3G.graphics3D(p).getViewportX());
			case 5: // m3g_getViewportY(): Int
				return Ival(LibM3G.graphics3D(p).getViewportY());
			case 6: // m3g_getViewportWidth(): Int
				return Ival(LibM3G.graphics3D(p).getViewportWidth());
			case 7: // m3g_getViewportHeight(): Int
				return Ival(LibM3G.graphics3D(p).getViewportHeight());
			case 8: // m3g_getDepthRangeNear(): Float
				return Fval(LibM3G.graphics3D(p).getDepthRangeNear());
			case 9: // m3g_getDepthRangeFar(): Float
				return Fval(LibM3G.graphics3D(p).getDepthRangeFar());
			case 10: // m3g_getCamera(transform: Transform = null): Camera
				return LibM3G.graphics3D(p).getCamera((Transform)args[0]);
			case 11: // m3g_getLightCount(): Int
				return Ival(LibM3G.graphics3D(p).getLightCount());
			case 12: // m3g_getLight(index: Int, transform: Transform): Light
				return LibM3G.graphics3D(p).getLight(ival(args[0]), (Transform)args[1]);
			case 13: // IndexBuffer.getIndexCount(): Int
				return Ival(((IndexBuffer)args[0]).getIndexCount());
			case 14: // IndexBuffer.getIndices(indices: [Int])
				((IndexBuffer)args[0]).getIndices((int[])args[1]);
				return null;
			case 15: // KeyframeSequence.getComponentCount(): Int
				return Ival(((KeyframeSequence)args[0]).getComponentCount());
			case 16: // KeyframeSequence.getKeyframeCount(): Int
				return Ival(((KeyframeSequence)args[0]).getKeyframeCount());
			case 17: // KeyframeSequence.getInterpolationType(): Int
				return Ival(((KeyframeSequence)args[0]).getInterpolationType());
			case 18: // KeyframeSequence.getKeyframe(index: Int, value: [Float]): Int
				return Ival(((KeyframeSequence)args[0]).getKeyframe(ival(args[1]), (float[])args[2]));
			case 19: // KeyframeSequence.getValidRangeFirst(): Int
				return Ival(((KeyframeSequence)args[0]).getValidRangeFirst());
			case 20: // KeyframeSequence.getValidRangeLast(): Int
				return Ival(((KeyframeSequence)args[0]).getValidRangeLast());
			case 21: // Node.getAlignmentTarget(axis: Int): Int
				return Ival(((Node)args[0]).getAlignmentTarget(ival(args[1])));
			case 22: // Node.getAlignmentReference(axis: Int): Node
				return ((Node)args[0]).getAlignmentReference(ival(args[1]));
			case 23: // PolygonMode.isLocalCameraLightingEnabled(): Bool
				return Ival(((PolygonMode)args[0]).isLocalCameraLightingEnabled());
			case 24: // PolygonMode.isPerspectiveCorrectionEnabled(): Bool
				return Ival(((PolygonMode)args[0]).isPerspectiveCorrectionEnabled());
			case 25: // SkinnedMesh.getBoneTransform(bone: Node, transform: Transform)
				((SkinnedMesh)args[0]).getBoneTransform((Node)args[1], (Transform)args[2]);
				return null;
			case 26: // SkinnedMesh.getBoneVertices(bone: Node, indices: [Int], weights: [Float]): Int
				return Ival(((SkinnedMesh)args[0]).getBoneVertices((Node)args[1], (int[])args[2], (float[])args[3]));
			case 27: // Texture2D.getLevelFilter(): Int
				return Ival(((Texture2D)args[0]).getLevelFilter());
			case 28: // Texture2D.getImageFilter(): Int
				return Ival(((Texture2D)args[0]).getImageFilter());
			case 29: // VertexArray.getVertexCount(): Int
				return Ival(((VertexArray)args[0]).getVertexCount());
			case 30: // VertexArray.getComponentCount(): Int
				return Ival(((VertexArray)args[0]).getComponentCount());
			case 31: // VertexArray.getComponentType(): Int
				return Ival(((VertexArray)args[0]).getComponentType());
			case 32: { // VertexArray.get(firstVertex: Int, numVertices: Int, values: Array)
				Object array = args[3];
				if (array instanceof byte[])
					((VertexArray)args[0]).get(ival(args[1]), ival(args[2]), (byte[])array);
				else if (array instanceof short[])
					((VertexArray)args[0]).get(ival(args[1]), ival(args[2]), (short[])array);
				else
					throw new IllegalArgumentException("[Byte] or [Short] array required");
				return null;
			}
			default:
				throw new IllegalArgumentException("Invalid function");
		}
	}
}
