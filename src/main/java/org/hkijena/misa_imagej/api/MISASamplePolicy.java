package org.hkijena.misa_imagej.api;

/**
 * For loading samples from parameters
 */
public enum MISASamplePolicy {
    /**
     * Missing samples should be created
     */
    createMissingSamples,
    /**
     * Missing samples should be ignored
     */
    ignoreMissingSamples
}
