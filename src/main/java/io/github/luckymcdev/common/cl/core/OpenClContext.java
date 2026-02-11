package io.github.luckymcdev.common.cl.core;

import com.mojang.logging.LogUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class OpenClContext {
    private static final Logger LOGGER = LogUtils.getLogger();
    private long platform;
    private long device;
    private long context;
    private CLCapabilities platformCaps;
    private CLCapabilities deviceCaps;

    public OpenClContext(boolean printInfoLog) {
        initialize(printInfoLog);
    }

    private void initialize(boolean printInfoLog) {
        try (MemoryStack stack = stackPush()) {
            // Get platform
            IntBuffer numPlatforms = stack.mallocInt(1);
            clGetPlatformIDs(null, numPlatforms);

            if (numPlatforms.get(0) == 0) {
                throw new RuntimeException("No OpenCL platforms found");
            }

            PointerBuffer platforms = stack.mallocPointer(numPlatforms.get(0));
            clGetPlatformIDs(platforms, (IntBuffer) null);
            platform = platforms.get(0);

            // Create platform capabilities
            platformCaps = CL.createPlatformCapabilities(platform);

            // Get device (try GPU first, fallback to CPU)
            device = getDevice(stack, CL_DEVICE_TYPE_GPU);
            if (device == 0) {
                LOGGER.warn("No GPU found, falling back to CPU");
                device = getDevice(stack, CL_DEVICE_TYPE_CPU);
            }

            if (device == 0) {
                throw new RuntimeException("No OpenCL devices found");
            }

            // Create device capabilities
            deviceCaps = CL.createDeviceCapabilities(device, platformCaps);

            // Print device info
            if(printInfoLog) printDeviceInfo(stack);

            // Create context
            IntBuffer errcode = stack.mallocInt(1);
            context = clCreateContext(null, device, null, NULL, errcode);
            checkCLError(errcode.get(0), "Failed to create context");
        }
    }

    private long getDevice(MemoryStack stack, long deviceType) {
        IntBuffer numDevices = stack.mallocInt(1);
        int result = clGetDeviceIDs(platform, deviceType, null, numDevices);

        if (result != CL_SUCCESS || numDevices.get(0) == 0) {
            return 0;
        }

        PointerBuffer devices = stack.mallocPointer(numDevices.get(0));
        clGetDeviceIDs(platform, deviceType, devices, (IntBuffer) null);
        return devices.get(0);
    }

    private void printDeviceInfo(MemoryStack stack) {
        // Get device name
        PointerBuffer deviceNameSize = stack.mallocPointer(1);
        clGetDeviceInfo(device, CL_DEVICE_NAME, (PointerBuffer) null, deviceNameSize);

        ByteBuffer deviceNameBuffer = stack.malloc((int) deviceNameSize.get(0));
        clGetDeviceInfo(device, CL_DEVICE_NAME, deviceNameBuffer, null);
        String deviceName = memUTF8(deviceNameBuffer);

        // Get device type
        LongBuffer deviceTypeLongBuffer = stack.mallocLong(1);
        clGetDeviceInfo(device, CL_DEVICE_TYPE, deviceTypeLongBuffer, null);
        long deviceTypeValue = deviceTypeLongBuffer.get(0);

        String typeName;
        if ((deviceTypeValue & CL_DEVICE_TYPE_GPU) != 0) {
            typeName = "GPU";
        } else if ((deviceTypeValue & CL_DEVICE_TYPE_CPU) != 0) {
            typeName = "CPU";
        } else if ((deviceTypeValue & CL_DEVICE_TYPE_ACCELERATOR) != 0) {
            typeName = "Accelerator";
        } else {
            typeName = "Unknown (0x" + Long.toHexString(deviceTypeValue) + ")";
        }

        String vendor = getDeviceInfoString(stack, CL_DEVICE_VENDOR);
        String version = getDeviceInfoString(stack, CL_DEVICE_VERSION);
        String driverVersion = getDeviceInfoString(stack, CL_DRIVER_VERSION);
        int computeUnits = getDeviceInfoInt(stack, CL_DEVICE_MAX_COMPUTE_UNITS);
        long maxWorkGroupSize = getDeviceInfoLong(stack, CL_DEVICE_MAX_WORK_GROUP_SIZE);

        LOGGER.info("OpenCL Device: {} ({})", deviceName, typeName);
        LOGGER.info("Compute Units: {}", computeUnits);
        LOGGER.info("Max Work Group Size: {}", maxWorkGroupSize);
        LOGGER.info("Vendor: {}", vendor);
        LOGGER.info("Version: {}", version);
        LOGGER.info("Driver Version: {}", driverVersion);
    }

    private String getDeviceInfoString(MemoryStack stack, int param) {
        PointerBuffer size = stack.mallocPointer(1);
        clGetDeviceInfo(device, param, (ByteBuffer) null, size);

        ByteBuffer buffer = stack.malloc((int) size.get(0));
        clGetDeviceInfo(device, param, buffer, null);

        return memUTF8(buffer);
    }

    // Helper methods to clean up the code
    private int getDeviceInfoInt(MemoryStack stack, int param) {
        IntBuffer buffer = stack.mallocInt(1);
        clGetDeviceInfo(device, param, buffer, null);
        return buffer.get(0);
    }

    private long getDeviceInfoLong(MemoryStack stack, int param) {
        LongBuffer buffer = stack.mallocLong(1);
        clGetDeviceInfo(device, param, buffer, null);
        return buffer.get(0);
    }

    public long getContext() {
        return context;
    }

    public long getDevice() {
        return device;
    }

    public CLCapabilities getPlatformCapabilities() {
        return platformCaps;
    }

    public CLCapabilities getDeviceCapabilities() {
        return deviceCaps;
    }

    public long getPlatform() {
        return platform;
    }

    public void cleanup() {
        if (context != 0) {
            clReleaseContext(context);
            context = 0;
        }
    }

    public static void checkCLError(int errcode, String message) {
        if (errcode != CL_SUCCESS) {
            throw new RuntimeException(message + " (error code: " + errcode + ")");
        }
    }
}