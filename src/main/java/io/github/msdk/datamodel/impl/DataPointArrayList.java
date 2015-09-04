/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.datamodel.impl;

import java.util.AbstractList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.rawdata.SpectrumDataPointList;

/**
 * Basic implementation of DataPointList.
 * 
 * Important: this class is not thread-safe.
 */
class DataPointArrayList extends AbstractList<SpectrumDataPointList>
        implements SpectrumDataPointList {

    /**
     * Array for m/z values. Its length defines the capacity of this list.
     */
    private @Nonnull double[] mzBuffer;

    /**
     * Array for intensity values. Its length is always the same as the m/z
     * buffer length.
     */
    private @Nonnull float[] intensityBuffer;

    /**
     * Current size of the list
     */
    private int size;

    /**
     * Creates a new data point list with internal array capacity of 100.
     */
    DataPointArrayList() {
        this(100);
    }

    /**
     * Creates a new data point list with given internal array capacity.
     * 
     * @param initialCapacity
     *            Initial size of the m/z and intensity arrays.
     */
    DataPointArrayList(@Nonnull Integer initialCapacity) {
        mzBuffer = new double[initialCapacity];
        intensityBuffer = new float[initialCapacity];
        size = 0;
    }

    /**
     * Creates a copy of a given data point list with new array capacity.
     * 
     * @param sourceList
     *            list to copy the data from
     * @param initialCapacity
     *            initial size of the m/z and intensity arrays
     * @throws IllegalArgumentException
     *             if the initial capacity < size of the sourceList
     */
    DataPointArrayList(@Nonnull SpectrumDataPointList sourceList,
            @Nonnull Integer initialCapacity) {

        if (initialCapacity < sourceList.size()) {
            throw new IllegalArgumentException(
                    "Requested capacity must be >= size of the source list");
        }

        mzBuffer = new double[initialCapacity];
        intensityBuffer = new float[initialCapacity];

        // Copy data
        copyFrom(sourceList);

    }

    /**
     * Creates a new data point list backed by given arrays. Arrays are
     * referenced, not cloned.
     * 
     * @param mzBuffer
     *            array of m/z values
     * @param intensityBuffer
     *            array of intensity values
     * @param size
     *            size of the list, must be <= length of both arrays
     * @throws IllegalArgumentException
     *             if the initial array length < size
     */
    DataPointArrayList(@Nonnull double mzBuffer[],
            @Nonnull float intensityBuffer[], int size) {
        Preconditions.checkArgument(mzBuffer.length >= size);
        Preconditions.checkArgument(intensityBuffer.length >= size);
        this.mzBuffer = mzBuffer;
        this.intensityBuffer = intensityBuffer;
        this.size = size;
    }

    /**
     * Returns the current m/z array
     */
    @Override
    @Nonnull
    public double[] getMzBuffer() {
        return mzBuffer;
    }

    /**
     * Returns the current intensity array
     */
    @Override
    @Nonnull
    public float[] getIntensityBuffer() {
        return intensityBuffer;
    }

    @Override
    public void setSize(int newSize) {

        if (newSize > mzBuffer.length) {
            throw new IllegalArgumentException(
                    "Size of the list cannot be larger than the length of the m/z and intensity arrays");
        }

        for (int i = 0; i < newSize - 1; i++) {
            if (mzBuffer[i] > mzBuffer[i + 1]) {
                throw new IllegalStateException(
                        "The m/z array must be sorted in ascending order");
            }
        }

        this.size = newSize;
    }

    /**
     * Replaces the m/z and intensity arrays with new ones
     */
    @Override
    public void setBuffers(@Nonnull double[] mzBuffer,
            @Nonnull float[] intensityBuffer, int newSize) {

        if (mzBuffer.length != intensityBuffer.length) {
            throw new IllegalArgumentException(
                    "The length of the m/z and intensity arrays must be equal");
        }

        // Update arrays
        this.mzBuffer = mzBuffer;
        this.intensityBuffer = intensityBuffer;

        // Update the size and check if the m/z array is properly sorted
        setSize(newSize);

    }

    /**
     * Copy data from another DataPointList
     */
    @Override
    public void copyFrom(@Nonnull SpectrumDataPointList list) {
        if (mzBuffer.length < list.size()) {
            mzBuffer = new double[list.size()];
            intensityBuffer = new float[list.size()];
        }

        // Copy data
        System.arraycopy(list.getMzBuffer(), 0, mzBuffer, 0, list.size());
        System.arraycopy(list.getIntensityBuffer(), 0, intensityBuffer, 0,
                list.size());

        // Update the size and check if the m/z array is properly sorted
        setSize(list.size());

    }

    /**
     * Copy data to another DataPointList
     */
    @Override
    public void copyTo(@Nonnull SpectrumDataPointList list) {
        double targetMzBuffer[] = list.getMzBuffer();
        if (targetMzBuffer.length < size)
            targetMzBuffer = new double[size];
        float targetIntensityBuffer[] = list.getIntensityBuffer();
        if (targetIntensityBuffer.length < size)
            targetIntensityBuffer = new float[size];
        System.arraycopy(mzBuffer, 0, targetMzBuffer, 0, size);
        System.arraycopy(intensityBuffer, 0, targetIntensityBuffer, 0, size);
        list.setBuffers(targetMzBuffer, targetIntensityBuffer, size);
    }

    /**
     * Returns the m/z range, assuming the m/z array is sorted.
     */
    @Override
    @Nullable
    public Range<Double> getMzRange() {
        if (size == 0)
            return null;
        return Range.closed(mzBuffer[0], mzBuffer[size - 1]);
    }

    /**
     * AbstractList method implementation
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * AbstractList method implementation
     */
    @Override
    public void add(int index, SpectrumDataPoint dp) {
        this.add(index, dp.getMz(), dp.getIntensity());
    }

    /**
     * Insert into the right position
     */
    public boolean add(SpectrumDataPoint newDataPoint) {
        this.add(newDataPoint.getMz(), newDataPoint.getIntensity());
        return true;
    }

    /**
     * Insert into the right position
     */
    public void add(double newMz, float newIntensity) {
        int targetPosition;
        for (targetPosition = 0; targetPosition < size; targetPosition++) {
            if (mzBuffer[targetPosition] > newMz)
                break;
        }
        this.add(targetPosition, newMz, newIntensity);
    }

    public void add(int index, double newMz, float newIntensity) {

        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Cannot insert at position "
                    + index + " (list size is " + size + ")");

        if ((index > 0) && (mzBuffer[index - 1] > newMz)) {
            throw new IllegalArgumentException(
                    "Setting the data point at this position would break the m/z ordering of the data points");
        }

        if ((index < size) && (mzBuffer[index] < newMz)) {
            throw new IllegalArgumentException(
                    "Setting the data point at this position would break the m/z ordering of the data points");
        }

        if (index >= mzBuffer.length) {

            final int newCapacity = Math.max(mzBuffer.length * 2, index * 2);

            // Create new m/z buffer
            double newMzBuffer[] = new double[newCapacity];
            System.arraycopy(mzBuffer, 0, newMzBuffer, 0, size);
            mzBuffer = newMzBuffer;

            // Create new intensity buffer
            float newIntensityBuffer[] = new float[newCapacity];
            System.arraycopy(intensityBuffer, 0, newIntensityBuffer, 0, size);
            intensityBuffer = newIntensityBuffer;
        }

        if (index < size) {
            System.arraycopy(mzBuffer, index, mzBuffer, index + 1,
                    size - index);
            System.arraycopy(intensityBuffer, index, intensityBuffer, index + 1,
                    size - index);
        }

        mzBuffer[index] = newMz;
        intensityBuffer[index] = newIntensity;

        size++;

    }

    /**
     * 
     */
    @Override
    public SpectrumDataPoint remove(int index) {

        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        SpectrumDataPoint current = get(index);

        if (index < size - 1) {
            System.arraycopy(mzBuffer, index + 1, mzBuffer, index,
                    size - index - 1);
            System.arraycopy(intensityBuffer, index + 1, intensityBuffer, index,
                    size - index - 1);
        }

        size--;

        return current;

    }

    /**
     * AbstractList method implementation
     */
    @Override
    public SpectrumDataPoint get(int index) {

        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        return MSDKObjectBuilder.getDataPoint(mzBuffer[index],
                intensityBuffer[index]);
    }

    /**
     * AbstractList method implementation
     */
    @Override
    public SpectrumDataPoint set(int index, SpectrumDataPoint dp) {

        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        if (dp == null) {
            throw new NullPointerException(
                    "DataPointList does not permit null elements");
        }

        if ((index > 0) && (mzBuffer[index - 1] > dp.getMz())) {
            throw new IllegalArgumentException(
                    "Setting the data point at this position would break the m/z ordering of the data points");
        }

        if ((index < (size - 1)) && (mzBuffer[index + 1] < dp.getMz())) {
            throw new IllegalArgumentException(
                    "Setting the data point at this position would break the m/z ordering of the data points");
        }

        final SpectrumDataPoint current = get(index);

        mzBuffer[index] = dp.getMz();
        intensityBuffer[index] = dp.getIntensity();

        return current;

    }

    /**
     * The equals() method compares the contents of the two data point lists,
     * and ignores their internal array sizes (capacities).
     */
    @Override
    public boolean equals(Object o) {

        // o must be a non-null DataPointList
        if ((o == null) || (!(o instanceof SpectrumDataPointList)))
            return false;

        // Cast o to DataPointlist
        SpectrumDataPointList otherList = (SpectrumDataPointList) o;

        // Size must be equal
        if (otherList.size() != size)
            return false;

        // Get the arrays of the other list
        final double otherMzBuffer[] = otherList.getMzBuffer();
        final float otherIntensityBuffer[] = otherList.getIntensityBuffer();

        // Check the array contents
        for (int i = 0; i < size; i++) {
            if (mzBuffer[i] != otherMzBuffer[i])
                return false;
            if (intensityBuffer[i] != otherIntensityBuffer[i])
                return false;
        }

        // No difference found, return true
        return true;
    }

    /**
     * The hashCode() code is inspired by Arrays.hashCode(double[] or float[])
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            long bits = Double.doubleToLongBits(mzBuffer[i]);
            result = 31 * result + (int) (bits ^ (bits >>> 32));
            result = 31 * result + Float.floatToIntBits(intensityBuffer[i]);
        }
        return result;
    }

    /**
     * toString() method
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < size; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(mzBuffer[i]);
            builder.append(":");
            builder.append(intensityBuffer[i]);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    @Nullable
    public SpectrumDataPoint getHighestDataPoint() {
        if (size == 0)
            return null;
        int maxIndex = 0;
        for (int i = 1; i < size; i++) {
            if (intensityBuffer[i] > intensityBuffer[maxIndex])
                maxIndex = i;
        }
        final SpectrumDataPoint newDP = MSDKObjectBuilder
                .getDataPoint(mzBuffer[maxIndex], intensityBuffer[maxIndex]);
        return newDP;
    }

    @Override
    @Nonnull
    public Float getTIC() {
        float tic = 0f;
        for (int i = 0; i < size; i++) {
            tic += intensityBuffer[i];
        }
        return tic;
    }

    @Override
    public SpectrumDataPointList selectDataPoints(@Nonnull Range<Double> mzRange,
            @Nonnull Range<Float> intensityRange) {

        final SpectrumDataPointList newList = MSDKObjectBuilder.getDataPointList();

        for (int i = 0; i < size; i++) {
            if (mzRange.contains(mzBuffer[i])
                    && intensityRange.contains(intensityBuffer[i]))
                newList.add(mzBuffer[i], intensityBuffer[i]);
        }

        return newList;
    }

    @Override
    public SpectrumDataPointList get(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpectrumDataPointList remove(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getHighestDataPointIndex() {
        // TODO Auto-generated method stub
        return null;
    }
}
