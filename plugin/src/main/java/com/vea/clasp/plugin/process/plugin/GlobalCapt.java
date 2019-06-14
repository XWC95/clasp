package com.vea.clasp.plugin.process.plugin;

import com.android.build.gradle.BaseExtension;
import com.vea.clasp.plugin.api.Arguments;
import com.vea.clasp.plugin.api.CaptInternal;
import com.vea.clasp.plugin.api.Context;
import com.vea.clasp.plugin.api.OutputProvider;
import com.vea.clasp.plugin.api.graph.ClassGraph;
import com.vea.clasp.plugin.api.log.Logger;
import com.vea.clasp.plugin.api.logger.LoggerFactory;
import com.vea.clasp.plugin.resource.GlobalResource;
import com.vea.clasp.plugin.resource.VariantResource;

import org.gradle.api.Project;

import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class GlobalCapt implements CaptInternal, Context {

    private final ClassGraph classGraph;
    private final GlobalResource global;
    private final VariantResource variantResource;

    public GlobalCapt(ClassGraph classGraph, GlobalResource global, VariantResource variantResource) {
        this.classGraph = classGraph;
        this.global = global;
        this.variantResource = variantResource;
    }

    @Override
    public Project getProject() {
        return global.project();
    }

    @Override
    public BaseExtension getAndroid() {
        return global.android();
    }

    @Override
    public URLClassLoader captLoader() {
        return variantResource.loader();
    }

    @Override
    public boolean isIncremental() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public ClassGraph getClassGraph() {
        return classGraph;
    }

    @Override
    public Arguments getArgs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputProvider getOutputs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVariantName() {
        return variantResource.variant();
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    @Override
    public ForkJoinPool getComputation() {
        return global.computation();
    }

    @Override
    public ExecutorService getIo() {
        return global.io();
    }
}
