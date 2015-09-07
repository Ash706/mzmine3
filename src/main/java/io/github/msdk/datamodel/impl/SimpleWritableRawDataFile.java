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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFileType;
import io.github.msdk.datamodel.rawdata.WritableRawDataFile;

/**
 * Implementation of the RawDataFile interface.
 */
class SimpleWritableRawDataFile implements WritableRawDataFile {

    private @Nonnull String rawDataFileName;
    private @Nullable File originalRawDataFile;
    private @Nonnull RawDataFileType rawDataFileType;
    private @Nonnull ArrayList<MsScan> scans;
    private @Nonnull ArrayList<Chromatogram> chromatograms;
    private @Nonnull DataPointStore dataPointStore;

    SimpleWritableRawDataFile(@Nonnull String rawDataFileName,
            @Nullable File originalRawDataFile,
            @Nonnull RawDataFileType rawDataFileType,
            @Nonnull DataPointStore dataPointStore) {
        this.rawDataFileName = rawDataFileName;
        this.originalRawDataFile = originalRawDataFile;
        this.rawDataFileType = rawDataFileType;
        this.dataPointStore = dataPointStore;
        this.scans = new ArrayList<MsScan>();
        this.chromatograms = new ArrayList<Chromatogram>();
    }

    @Override
    public @Nonnull String getName() {
        return rawDataFileName;
    }

    @Override
    public void setName(@Nonnull String name) {
        this.rawDataFileName = name;
    }

    @Override
    @Nullable
    public File getOriginalFile() {
        return originalRawDataFile;
    }

    @Override
    public void setOriginalFile(@Nullable File newOriginalFile) {
        this.originalRawDataFile = newOriginalFile;
    }

    @Override
    public @Nonnull RawDataFileType getRawDataFileType() {
        return rawDataFileType;
    }

    @Override
    public void setRawDataFileType(@Nonnull RawDataFileType rawDataFileType) {
        this.rawDataFileType = rawDataFileType;
    }

    @Override
    @Nonnull
    public List<MsFunction> getMsFunctions() {
        ArrayList<MsFunction> msFunctionList = new ArrayList<MsFunction>();
        synchronized (scans) {
            for (MsScan scan : scans) {
                MsFunction f = scan.getMsFunction();
                if (f != null)
                    msFunctionList.add(f);
            }
        }
        return msFunctionList;
    }

    @SuppressWarnings("null")
    @Override
    public @Nonnull List<MsScan> getScans() {
        return ImmutableList.copyOf(scans);
    }

    @Override
    @Nonnull
    public List<MsScan> getScans(MsFunction msFunction) {
        ArrayList<MsScan> msScanList = new ArrayList<MsScan>();
        synchronized (scans) {
            for (MsScan scan : scans) {
                if (scan.getMsFunction().equals(msFunction))
                    msScanList.add(scan);
            }
        }
        return msScanList;
    }

    @Override
    @Nonnull
    public List<MsScan> getScans(
            @Nonnull Range<ChromatographyInfo> chromatographyRange) {
        // TODO Auto-generated method stub
        return new ArrayList<MsScan>();
    }

    @Override
    @Nonnull
    public List<MsScan> getScans(@Nonnull MsFunction function,
            @Nonnull Range<ChromatographyInfo> chromatographyRange) {
        // TODO Auto-generated method stub
        return new ArrayList<MsScan>();
    }

    @Override
    public void addScan(@Nonnull MsScan scan) {
        scans.add(scan);
    }

    @Override
    public void removeScan(@Nonnull MsScan scan) {
        scans.remove(scan);
    }

    @SuppressWarnings("null")
    @Override
    @Nonnull
    public List<Chromatogram> getChromatograms() {
        return ImmutableList.copyOf(chromatograms);
    }

    @Override
    public void addChromatogram(@Nonnull Chromatogram chromatogram) {
        chromatograms.add(chromatogram);
    }

    @Override
    public void removeChromatogram(@Nonnull Chromatogram chromatogram) {
        chromatograms.remove(chromatogram);
    }

    @Override
    public void dispose() {
        dataPointStore.dispose();
    }

}