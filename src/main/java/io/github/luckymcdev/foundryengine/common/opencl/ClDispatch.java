package io.github.luckymcdev.foundryengine.common.opencl;

import io.github.luckymcdev.foundryengine.common.opencl.task.ClWorker;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL12.clGetKernelArgInfo;

public class ClDispatch {

    // ========== Platform and Device Operations ==========

    public static int getPlatformIDs(PointerBuffer platforms, IntBuffer numPlatforms) {
        return wrap(() -> clGetPlatformIDs(platforms, numPlatforms));
    }

    public static int getDeviceIDs(long platform, long deviceType, PointerBuffer devices, IntBuffer numDevices) {
        return wrap(() -> clGetDeviceIDs(platform, deviceType, devices, numDevices));
    }

    public static int getDeviceInfo(long device, int paramName, ByteBuffer paramValue, PointerBuffer paramValueSizeRet) {
        return wrap(() -> clGetDeviceInfo(device, paramName, paramValue, paramValueSizeRet));
    }

    public static int getDeviceInfo(long device, int paramName, IntBuffer paramValue, PointerBuffer paramValueSizeRet) {
        return wrap(() -> clGetDeviceInfo(device, paramName, paramValue, paramValueSizeRet));
    }

    public static int getDeviceInfo(long device, int paramName, LongBuffer paramValue, PointerBuffer paramValueSizeRet) {
        return wrap(() -> clGetDeviceInfo(device, paramName, paramValue, paramValueSizeRet));
    }

    // ========== Context Operations ==========

    public static long createContext(PointerBuffer properties, long device,
                                     org.lwjgl.opencl.CLContextCallback pfnNotify,
                                     long userData, IntBuffer errcode) {
        return wrap(() -> clCreateContext(properties, device, pfnNotify, userData, errcode));
    }

    public static int releaseContext(long context) {
        return wrap(() -> clReleaseContext(context));
    }

    // ========== Command Queue Operations ==========

    public static long createCommandQueue(long context, long device, long properties, IntBuffer errcode) {
        return wrap(() -> clCreateCommandQueue(context, device, properties, errcode));
    }

    public static int releaseCommandQueue(long commandQueue) {
        return wrap(() -> clReleaseCommandQueue(commandQueue));
    }

    public static int finish(long commandQueue) {
        return wrap(() -> clFinish(commandQueue));
    }

    // ========== Buffer Operations ==========

    public static long createBuffer(long context, int flags, FloatBuffer hostData, IntBuffer errcode) {
        return wrap(() -> clCreateBuffer(context, flags, hostData, errcode));
    }

    public static long createBuffer(long context, int flags, long size, IntBuffer errcode) {
        return wrap(() -> clCreateBuffer(context, flags, size, errcode));
    }

    public static int releaseMemObject(long memobj) {
        return wrap(() -> clReleaseMemObject(memobj));
    }

    public static int enqueueReadBuffer(long commandQueue, long buffer, boolean blockingRead,
                                        long offset, FloatBuffer ptr, PointerBuffer eventWaitList,
                                        PointerBuffer event) {
        return wrap(() -> clEnqueueReadBuffer(commandQueue, buffer, blockingRead, offset, ptr, eventWaitList, event));
    }

    public static int enqueueWriteBuffer(long commandQueue, long buffer, boolean blockingWrite,
                                         long offset, FloatBuffer ptr, PointerBuffer eventWaitList,
                                         PointerBuffer event) {
        return wrap(() -> clEnqueueWriteBuffer(commandQueue, buffer, blockingWrite, offset, ptr, eventWaitList, event));
    }

    // ========== Program Operations ==========

    public static long createProgramWithSource(long context, CharSequence source, IntBuffer errcode) {
        return wrap(() -> clCreateProgramWithSource(context, source, errcode));
    }

    public static int buildProgram(long program, long device, CharSequence options,
                                   org.lwjgl.opencl.CLProgramCallback pfnNotify, long userData) {
        return wrap(() -> clBuildProgram(program, device, options, pfnNotify, userData));
    }

    public static int getProgramBuildInfo(long program, long device, int paramName,
                                          PointerBuffer paramValue, PointerBuffer paramValueSizeRet) {
        return wrap(() -> clGetProgramBuildInfo(program, device, paramName, paramValue, paramValueSizeRet));
    }

    public static int releaseProgram(long program) {
        return wrap(() -> clReleaseProgram(program));
    }

    // ========== Kernel Operations ==========

    public static long createKernel(long program, CharSequence kernelName, IntBuffer errcode) {
        return wrap(() -> clCreateKernel(program, kernelName, errcode));
    }

    public static int releaseKernel(long kernel) {
        return wrap(() -> clReleaseKernel(kernel));
    }

    public static int setKernelArg(long kernel, int argIndex, IntBuffer argValue) {
        return wrap(() -> clSetKernelArg(kernel, argIndex, argValue));
    }

    public static int setKernelArg(long kernel, int argIndex, FloatBuffer argValue) {
        return wrap(() -> clSetKernelArg(kernel, argIndex, argValue));
    }


    public static int setKernelArg(long kernel, int argIndex, PointerBuffer argValue) {
        return wrap(() -> clSetKernelArg(kernel, argIndex, argValue));
    }

    public static int setKernelArg(long kernel, int argIndex, LongBuffer argValue) {
        return wrap(() -> clSetKernelArg(kernel, argIndex, argValue));
    }

    public static int setKernelArg1p(long kernel, int argIndex, long argValue) {
        return wrap(() -> clSetKernelArg1p(kernel, argIndex, argValue));
    }

    public static int setKernelArg1i(long kernel, int argIndex, int argValue) {
        return wrap(() -> clSetKernelArg1i(kernel, argIndex, argValue));
    }

    public static int setKernelArg1f(long kernel, int argIndex, float argValue) {
        return wrap(() -> clSetKernelArg1f(kernel, argIndex, argValue));
    }

    public static int setKernelArg1l(long kernel, int argIndex, long argValue) {
        return wrap(() -> clSetKernelArg1l(kernel, argIndex, argValue));
    }

    public static int getKernelInfo(long kernel, int paramName, IntBuffer paramValue,
                                    PointerBuffer paramValueSizeRet) {
        return wrap(() -> clGetKernelInfo(kernel, paramName, paramValue, paramValueSizeRet));
    }

    public static int getKernelArgInfo(long kernel, int argIndx, int paramName,
                                       ByteBuffer paramValue, PointerBuffer paramValueSizeRet) {
        return wrap(() -> clGetKernelArgInfo(kernel, argIndx, paramName, paramValue, paramValueSizeRet));
    }

    public static int enqueueNDRangeKernel(long commandQueue, long kernel, int workDim,
                                           PointerBuffer globalWorkOffset, PointerBuffer globalWorkSize,
                                           PointerBuffer localWorkSize, PointerBuffer eventWaitList,
                                           PointerBuffer event) {
        return wrap(() -> clEnqueueNDRangeKernel(commandQueue, kernel, workDim, globalWorkOffset,
                globalWorkSize, localWorkSize, eventWaitList, event));
    }

    // ========== Helper Methods ==========

    private static int wrap(ClIntCall call) {
        ClWorker.assertOnClThread();
        return call.dispatch();
    }

    private static long wrap(ClLongCall call) {
        ClWorker.assertOnClThread();
        return call.dispatch();
    }

    // ========== Functional Interfaces ==========

    private interface ClIntCall {
        int dispatch();
    }

    private interface ClLongCall {
        long dispatch();
    }
}