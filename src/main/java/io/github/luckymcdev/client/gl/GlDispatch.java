package io.github.luckymcdev.client.gl;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.common.Instances;
import org.joml.*;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;

public class GlDispatch {
    public static GlDevice glDevice = Instances.getGlDevice();

    private static void wrap(GlCall call) {
        RenderSystem.assertOnRenderThread();
        call.dispatch();
    }

    private static <T> T wrapReturn(GlCallReturn<T> call) {
        RenderSystem.assertOnRenderThread();
        return call.dispatch();
    }

    public static boolean glIsEnabled(int cap) {
        return wrapReturn(() -> GL43C.glIsEnabled(cap));
    }

    public static int glGetInteger(int name) {
        return wrapReturn(() -> GL43C.glGetInteger(name));
    }

    public static boolean glGetBoolean(int name) {
        return wrapReturn(() -> GL43C.glGetBoolean(name));
    }

    public static void glGetIntegerv(int name, int[] params) {
        wrap(() -> GL43C.glGetIntegerv(name, params));
    }

    public static int glCreateShader(int type) {
        return wrapReturn(() -> GL43C.glCreateShader(type));
    }

    public static void glCompileShader(int shader) {
        wrap(() -> GL43C.glCompileShader(shader));
    }

    public static void glBindShaderSource(int shader, String source) {
        wrap(() -> GL43C.glShaderSource(shader, source));
    }

    public static int glGetShaderi(int shader, int pname) {
        return wrapReturn(() -> GL43C.glGetShaderi(shader, pname));
    }

    public static String glGetShaderInfoLog(int shader) {
        return wrapReturn(() -> GL43C.glGetShaderInfoLog(shader));
    }

    public static void glDeleteShader(int shader) {
        wrap(() -> GL43C.glDeleteShader(shader));
    }

    public static int glCreateProgram() {
        return wrapReturn(GL43C::glCreateProgram);
    }

    public static void glAttachShader(int program, int shader) {
        wrap(() -> GL43C.glAttachShader(program, shader));
    }

    public static void glDetachShader(int program, int shader) {
        wrap(() -> GL43C.glDetachShader(program, shader));
    }

    public static void glLinkProgram(int program) {
        wrap(() -> GL43C.glLinkProgram(program));
    }

    public static int glGetProgrami(int program, int pname) {
        return wrapReturn(() -> GL43C.glGetProgrami(program, pname));
    }

    public static String glGetProgramInfoLog(int program) {
        return wrapReturn(() -> GL43C.glGetProgramInfoLog(program));
    }

    public static void glUseProgram(int program) {
        wrap(() -> GL43C.glUseProgram(program));
    }

    public static void glDeleteProgram(int program) {
        wrap(() -> GL43C.glDeleteProgram(program));
    }

    public static void glBindAttribLocation(int program, int index, String name) {
        wrap(() -> GL43C.glBindAttribLocation(program, index, name));
    }

    public static int glGetUniformLocation(int program, String name) {
        return wrapReturn(() -> GL43C.glGetUniformLocation(program, name));
    }

    public static void glUniform1i(int location, int v0) {
        wrap(() -> GL43C.glUniform1i(location, v0));
    }

    public static void glUniform2i(int location, int v0, int v1) {
        wrap(() -> GL43C.glUniform2i(location, v0, v1));
    }

    public static void glUniform3i(int location, int v0, int v1, int v2) {
        wrap(() -> GL43C.glUniform3i(location, v0, v1, v2));
    }

    public static void glUniform4i(int location, int v0, int v1, int v2, int v3) {
        wrap(() -> GL43C.glUniform4i(location, v0, v1, v2, v3));
    }

    public static void glUniform1f(int location, float v0) {
        wrap(() -> GL43C.glUniform1f(location, v0));
    }

    public static void glUniform2f(int location, float v0, float v1) {
        wrap(() -> GL43C.glUniform2f(location, v0, v1));
    }

    public static void glUniform2f(int location, Vector2f value) {
        wrap(() -> GL43C.glUniform2f(location, value.x, value.y));
    }

    public static void glUniform3f(int location, float v0, float v1, float v2) {
        wrap(() -> GL43C.glUniform3f(location, v0, v1, v2));
    }

    public static void glUniform3f(int location, Vector3f value) {
        wrap(() -> GL43C.glUniform3f(location, value.x, value.y, value.z));
    }

    public static void glUniform4f(int location, float v0, float v1, float v2, float v3) {
        wrap(() -> GL43C.glUniform4f(location, v0, v1, v2, v3));
    }

    public static void glUniform4f(int location, Vector4f value) {
        wrap(() -> GL43C.glUniform4f(location, value.x, value.y, value.z, value.w));
    }

    public static void glUniform2i(int location, Vector2i value) {
        wrap(() -> GL43C.glUniform2i(location, value.x, value.y));
    }

    public static void glUniform3i(int location, Vector3i value) {
        wrap(() -> GL43C.glUniform3i(location, value.x, value.y, value.z));
    }

    public static void glUniform4i(int location, Vector4i value) {
        wrap(() -> GL43C.glUniform4i(location, value.x, value.y, value.z, value.w));
    }

    public static void glUniform1ui(int location, int v0) {
        wrap(() -> GL43C.glUniform1ui(location, v0));
    }

    public static void glUniform2ui(int location, int v0, int v1) {
        wrap(() -> GL43C.glUniform2ui(location, v0, v1));
    }

    public static void glUniform3ui(int location, int v0, int v1, int v2) {
        wrap(() -> GL43C.glUniform3ui(location, v0, v1, v2));
    }

    public static void glUniform4ui(int location, int v0, int v1, int v2, int v3) {
        wrap(() -> GL43C.glUniform4ui(location, v0, v1, v2, v3));
    }

    public static void glUniform1iv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform1iv(location, value));
    }

    public static void glUniform2iv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform2iv(location, value));
    }

    public static void glUniform3iv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform3iv(location, value));
    }

    public static void glUniform4iv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform4iv(location, value));
    }

    public static void glUniform1uiv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform1uiv(location, value));
    }

    public static void glUniform2uiv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform2uiv(location, value));
    }

    public static void glUniform3uiv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform3uiv(location, value));
    }

    public static void glUniform4uiv(int location, IntBuffer value) {
        wrap(() -> GL43C.glUniform4uiv(location, value));
    }

    public static void glUniform1fv(int location, FloatBuffer value) {
        wrap(() -> GL43C.glUniform1fv(location, value));
    }

    public static void glUniform2fv(int location, FloatBuffer value) {
        wrap(() -> GL43C.glUniform2fv(location, value));
    }

    public static void glUniform3fv(int location, FloatBuffer value) {
        wrap(() -> GL43C.glUniform3fv(location, value));
    }

    public static void glUniform4fv(int location, FloatBuffer value) {
        wrap(() -> GL43C.glUniform4fv(location, value));
    }

    public static void glUniformMatrix2fv(int location, boolean transpose, FloatBuffer value) {
        wrap(() -> GL43C.glUniformMatrix2fv(location, transpose, value));
    }

    public static void glUniformMatrix2f(int location, Matrix2f value) {
        glUniformMatrix2f(location, false, value);
    }

    public static void glUniformMatrix2f(int location, boolean transpose, Matrix2f value) {
        wrap(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(4);
                value.get(buffer);
                GL43C.glUniformMatrix2fv(location, transpose, buffer);
            }
        });
    }

    public static void glUniformMatrix3fv(int location, boolean transpose, FloatBuffer value) {
        wrap(() -> GL43C.glUniformMatrix3fv(location, transpose, value));
    }

    public static void glUniformMatrix3f(int location, Matrix3f value) {
        glUniformMatrix3f(location, false, value);
    }

    public static void glUniformMatrix3f(int location, boolean transpose, Matrix3f value) {
        wrap(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(9);
                value.get(buffer);
                GL43C.glUniformMatrix3fv(location, transpose, buffer);
            }
        });
    }

    public static void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value) {
        wrap(() -> GL43C.glUniformMatrix4fv(location, transpose, value));
    }

    public static void glUniformMatrix4f(int location, Matrix4f value) {
        glUniformMatrix4f(location, false, value);
    }

    public static void glUniformMatrix4f(int location, boolean transpose, Matrix4f value) {
        wrap(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(16);
                value.get(buffer);
                GL43C.glUniformMatrix4fv(location, transpose, buffer);
            }
        });
    }

    public static int glGetUniformBlockIndex(int program, String name) {
        return wrapReturn(() -> GL43C.glGetUniformBlockIndex(program, name));
    }

    public static void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        wrap(() -> GL43C.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding));
    }

    public static int glGenTextures() {
        return wrapReturn(GL43C::glGenTextures);
    }

    public static void glBindTexture(int target, int texture) {
        wrap(() -> GL43C.glBindTexture(target, texture));
    }

    public static void glActiveTexture(int texture) {
        wrap(() -> GL43C.glActiveTexture(texture));
    }

    public static void glTexParameteri(int target, int pname, int param) {
        wrap(() -> GL43C.glTexParameteri(target, pname, param));
    }

    public static void glTexParameterf(int target, int pname, float param) {
        wrap(() -> GL43C.glTexParameterf(target, pname, param));
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
                                    int format, int type, ByteBuffer pixels) {
        wrap(() -> GL43C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels));
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
                                       int format, int type, ByteBuffer pixels) {
        wrap(() -> GL43C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels));
    }

    public static void glGenerateMipmap(int target) {
        wrap(() -> GL43C.glGenerateMipmap(target));
    }

    public static void glDeleteTextures(int texture) {
        wrap(() -> GL43C.glDeleteTextures(texture));
    }

    public static int glGenBuffers() {
        return wrapReturn(GL43C::glGenBuffers);
    }

    public static void glBindBuffer(int target, int buffer) {
        wrap(() -> GL43C.glBindBuffer(target, buffer));
    }

    public static void glBufferData(int target, long size, int usage) {
        wrap(() -> GL43C.glBufferData(target, size, usage));
    }

    public static void glBufferData(int target, int[] data, int usage) {
        wrap(() -> GL43C.glBufferData(target, data, usage));
    }

    public static void glBufferData(int target, IntBuffer data, int usage) {
        wrap(() -> GL43C.glBufferData(target, data, usage));
    }

    public static void glBufferData(int target, ByteBuffer data, int usage) {
        wrap(() -> GL43C.glBufferData(target, data, usage));
    }

    public static void glBufferData(int target, float[] data, int usage) {
        wrap(() -> GL43C.glBufferData(target, data, usage));
    }

    public static void glBufferData(int target, FloatBuffer data, int usage) {
        wrap(() -> GL43C.glBufferData(target, data, usage));
    }

    public static void glBufferSubData(int target, long offset, ByteBuffer data) {
        wrap(() -> GL43C.glBufferSubData(target, offset, data));
    }

    public static void glDeleteBuffers(int buffer) {
        wrap(() -> GL43C.glDeleteBuffers(buffer));
    }

    public static int glGenVertexArrays() {
        return wrapReturn(GL43C::glGenVertexArrays);
    }

    public static void glBindVertexArray(int array) {
        wrap(() -> GL43C.glBindVertexArray(array));
    }

    public static void glEnableVertexAttribArray(int index) {
        wrap(() -> GL43C.glEnableVertexAttribArray(index));
    }

    public static void glDisableVertexAttribArray(int index) {
        wrap(() -> GL43C.glDisableVertexAttribArray(index));
    }

    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) {
        wrap(() -> GL43C.glVertexAttribPointer(index, size, type, normalized, stride, pointer));
    }

    public static void glVertexAttribDivisor(int index, int divisor) {
        wrap(() -> GL43C.glVertexAttribDivisor(index, divisor));
    }

    public static void glDeleteVertexArrays(int array) {
        wrap(() -> GL43C.glDeleteVertexArrays(array));
    }

    public static int glGenFramebuffers() {
        return wrapReturn(GL43C::glGenFramebuffers);
    }

    public static void glBindFramebuffer(int target, int framebuffer) {
        wrap(() -> GL43C.glBindFramebuffer(target, framebuffer));
    }

    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        wrap(() -> GL43C.glFramebufferTexture2D(target, attachment, textarget, texture, level));
    }

    public static void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        wrap(() -> GL43C.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer));
    }

    public static int glCheckFramebufferStatus(int target) {
        return wrapReturn(() -> GL43C.glCheckFramebufferStatus(target));
    }

    public static void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1,
                                         int dstX0, int dstY0, int dstX1, int dstY1,
                                         int mask, int filter) {
        wrap(() -> GL43C.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter));
    }

    public static void glDeleteFramebuffers(int framebuffer) {
        wrap(() -> GL43C.glDeleteFramebuffers(framebuffer));
    }

    public static int glGenRenderbuffers() {
        return wrapReturn(GL43C::glGenRenderbuffers);
    }

    public static void glBindRenderbuffer(int target, int renderbuffer) {
        wrap(() -> GL43C.glBindRenderbuffer(target, renderbuffer));
    }

    public static void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        wrap(() -> GL43C.glRenderbufferStorage(target, internalformat, width, height));
    }

    public static void glDeleteRenderbuffers(int renderbuffer) {
        wrap(() -> GL43C.glDeleteRenderbuffers(renderbuffer));
    }

    public static void glEnable(int cap) {
        wrap(() -> GL43C.glEnable(cap));
    }

    public static void glDisable(int cap) {
        wrap(() -> GL43C.glDisable(cap));
    }

    public static void glBlendFunc(int sfactor, int dfactor) {
        wrap(() -> GL43C.glBlendFunc(sfactor, dfactor));
    }

    public static void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        wrap(() -> GL43C.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha));
    }

    public static void glDepthFunc(int func) {
        wrap(() -> GL43C.glDepthFunc(func));
    }

    public static void glDepthMask(boolean flag) {
        wrap(() -> GL43C.glDepthMask(flag));
    }

    public static void glCullFace(int mode) {
        wrap(() -> GL43C.glCullFace(mode));
    }

    public static void glFrontFace(int mode) {
        wrap(() -> GL43C.glFrontFace(mode));
    }

    public static void glViewport(int x, int y, int width, int height) {
        wrap(() -> GL43C.glViewport(x, y, width, height));
    }

    public static void glScissor(int x, int y, int width, int height) {
        wrap(() -> GL43C.glScissor(x, y, width, height));
    }

    public static void glClearColor(float r, float g, float b, float a) {
        wrap(() -> GL43C.glClearColor(r, g, b, a));
    }

    public static void glClear(int mask) {
        wrap(() -> GL43C.glClear(mask));
    }

    public static void glPolygonMode(int face, int mode) {
        wrap(() -> GL43C.glPolygonMode(face, mode));
    }

    public static void glLineWidth(float width) {
        wrap(() -> GL43C.glLineWidth(width));
    }

    public static void glStencilFunc(int func, int ref, int mask) {
        wrap(() -> GL43C.glStencilFunc(func, ref, mask));
    }

    public static void glStencilOp(int sfail, int dpfail, int dppass) {
        wrap(() -> GL43C.glStencilOp(sfail, dpfail, dppass));
    }

    public static void glStencilMask(int mask) {
        wrap(() -> GL43C.glStencilMask(mask));
    }

    public static void glDrawArrays(int mode, int first, int count) {
        wrap(() -> GL43C.glDrawArrays(mode, first, count));
    }

    public static void glDrawElements(int mode, int count, int type, long indices) {
        wrap(() -> GL43C.glDrawElements(mode, count, type, indices));
    }

    public static void glDrawArraysInstanced(int mode, int first, int count, int primcount) {
        wrap(() -> GL43C.glDrawArraysInstanced(mode, first, count, primcount));
    }

    public static void glDrawElementsInstanced(int mode, int count, int type, long indices, int primcount) {
        wrap(() -> GL43C.glDrawElementsInstanced(mode, count, type, indices, primcount));
    }

    public static int glGetError() {
        return wrapReturn(GL43C::glGetError);
    }

    public static void glFinish() {
        wrap(GL43C::glFinish);
    }

    public static void glFlush() {
        wrap(GL43C::glFlush);
    }

    public static void glObjectLabel(int identifier, int name, String label) {
        wrap(() -> GL43C.glObjectLabel(identifier, name, label));
    }

    private interface GlCall extends Runnable {
        void dispatch();

        @Override
        default void run() {
            dispatch();
        }
    }

    private interface GlCallReturn<T> extends Supplier<T> {
        T dispatch();

        @Override
        default T get() {
            return dispatch();
        }
    }
}
